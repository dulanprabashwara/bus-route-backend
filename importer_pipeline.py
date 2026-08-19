import os
import re
import pdfplumber
import logging
from importer_db import get_connection, ensure_data_source, register_source_file, update_file_status, get_or_create_stop, ensure_route_stops

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

# Global metrics
metrics = {
    "files_processed": 0,
    "files_succeeded": 0,
    "files_skipped": 0,
    "files_failed": 0,
    "routes": set(),
    "patterns": set(),
    "unique_stops": set(),
    "trips": 0,
    "stop_times": 0,
    "operator_types": set(),
    "service_types": set(),
    "warnings": []
}

def clean_stop_name(raw_name, filename="Unknown"):
    if not raw_name:
        return "Unknown Stop", None
    name = str(raw_name).strip()
    name = name.replace('\n', ' ').replace('\r', '')
    if not name:
        return "Unknown Stop", None
        
    if "(cid:" in name:
        if "corrupted_text" not in metrics:
            metrics["corrupted_text"] = []
        metrics["corrupted_text"].append(f"{name} in {filename}")
        # Preserve raw value but do not use as canonical name
        return "Unknown Stop (Corrupted)", name
        
    return name, None

def extract_route_metadata(filename):
    """
    Extract route number and origin-destination from filename.
    """
    name = os.path.splitext(filename)[0]
    route_match = re.match(r'^([\d\-/\s_,a-zA-Z]+?)\s+(.+)$', name)
    
    if route_match and any(c.isdigit() for c in route_match.group(1)):
        route_num = route_match.group(1).strip()
        rest = route_match.group(2).strip()
    else:
        route_num = "Unknown"
        rest = name.strip()
    
    rest = re.sub(r'\(.*?\)', '', rest)
    rest = re.sub(r'(?i)Panel.*', '', rest)
    rest = re.sub(r'(?i)Panal.*', '', rest)
    rest = re.sub(r'(?i)New Imp.*', '', rest)
    rest = re.sub(r'(?i)Normal.*', '', rest)
    rest = re.sub(r'\d{4}\.\d{2}\.\d{2}.*', '', rest)
    
    places = [p.strip() for p in rest.split('-') if p.strip()]
    origin = places[0] if len(places) > 0 else "Unknown"
    dest = places[1] if len(places) > 1 else "Unknown"
    origin = re.sub(r'^N\s+SL\s+', '', origin)
    
    return route_num, origin, dest

def clean_time(time_str, filename="Unknown"):
    if not time_str or str(time_str).strip() == '':
        return None, 0, None, 'UNKNOWN'
        
    time_str = str(time_str).strip().replace('.', ':')
    if len(time_str) == 4 and ':' not in time_str:
        time_str = time_str[:2] + ":" + time_str[2:]
    
    if re.match(r'^\d{1,2}:\d{2}$', time_str):
        hours, minutes = map(int, time_str.split(':'))
        
        if hours > 28 or minutes >= 60:
            if "suspicious_times" not in metrics:
                metrics["suspicious_times"] = []
            metrics["suspicious_times"].append(f"{time_str} in {filename}")
            return None, 0, time_str, 'INVALID'
            
        day_offset = hours // 24
        hours = hours % 24
        return f"{hours:02d}:{minutes:02d}:00", day_offset, time_str, 'EXACT'
    
    if "suspicious_times" not in metrics:
        metrics["suspicious_times"] = []
    metrics["suspicious_times"].append(f"{time_str} in {filename}")
    return None, 0, time_str, 'INVALID'

def insert_trip(conn, route_pattern_id, source_file_id, running_number=None):
    with conn.cursor() as cur:
        cur.execute("""
            INSERT INTO trips (route_pattern_id, source_file_id, running_number)
            VALUES (%s, %s, %s)
            RETURNING id
        """, (route_pattern_id, source_file_id, running_number))
        trip_id = cur.fetchone()[0]
        metrics["trips"] += 1
        return trip_id

def insert_trips_batch(conn, trips_data):
    """
    Batch inserts trips.
    trips_data is a list of tuples: (route_pattern_id, source_file_id, running_number)
    Returns a list of trip_ids in the same order.
    """
    if not trips_data:
        return []
    from psycopg2.extras import execute_values
    query = """
        INSERT INTO trips (route_pattern_id, source_file_id, running_number)
        VALUES %s RETURNING id
    """
    with conn.cursor() as cur:
        res = execute_values(cur, query, trips_data, fetch=True)
        metrics["trips"] += len(trips_data)
        return [r[0] for r in res]

def insert_stop_times_batch(conn, stop_times_data):
    """
    Batch inserts stop times.
    stop_times_data is a list of tuples: 
    (trip_id, stop_id, sequence, arrival, departure, day_offset, raw_time_string, time_accuracy)
    """
    if not stop_times_data:
        return
    
    # Filter out empty ones and ensure arr/dep fallbacks
    clean_data = []
    for trip_id, stop_id, sequence, arrival, departure, day_offset, raw_time_string, time_accuracy in stop_times_data:
        if not arrival and not departure and not raw_time_string:
            continue
        if not arrival: arrival = departure
        if not departure: departure = arrival
        
        clean_data.append((trip_id, stop_id, sequence, arrival, departure, day_offset, raw_time_string, time_accuracy))
        metrics["unique_stops"].add(stop_id)
        
    if not clean_data:
        return
        
    from psycopg2.extras import execute_batch
    query = """
        INSERT INTO stop_times (trip_id, stop_id, stop_sequence, arrival_time, departure_time, day_offset, raw_time_string, time_accuracy)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
    """
    with conn.cursor() as cur:
        execute_batch(cur, query, clean_data)
        metrics["stop_times"] += len(clean_data)

def get_or_create_route(conn, route_num, origin_id, dest_id, file_id):
    metrics["routes"].add(route_num)
    with conn.cursor() as cur:
        cur.execute("SELECT id FROM routes WHERE route_number = %s", (route_num,))
        row = cur.fetchone()
        if row: route_id = row[0]
        else:
            cur.execute("""
                INSERT INTO routes (route_number, origin_stop_id, destination_stop_id, source_file_id)
                VALUES (%s, %s, %s, %s) RETURNING id
            """, (route_num, origin_id, dest_id, file_id))
            route_id = cur.fetchone()[0]
            
        # Outbound pattern
        cur.execute("SELECT id FROM route_patterns WHERE route_id = %s AND direction = 'OUTBOUND'", (route_id,))
        row = cur.fetchone()
        if row: out_id = row[0]
        else:
            cur.execute("""
                INSERT INTO route_patterns (route_id, direction, origin_stop_id, destination_stop_id)
                VALUES (%s, 'OUTBOUND', %s, %s) RETURNING id
            """, (route_id, origin_id, dest_id))
            out_id = cur.fetchone()[0]
        metrics["patterns"].add(out_id)
            
        # Inbound pattern
        cur.execute("SELECT id FROM route_patterns WHERE route_id = %s AND direction = 'INBOUND'", (route_id,))
        row = cur.fetchone()
        if row: in_id = row[0]
        else:
            cur.execute("""
                INSERT INTO route_patterns (route_id, direction, origin_stop_id, destination_stop_id)
                VALUES (%s, 'INBOUND', %s, %s) RETURNING id
            """, (route_id, dest_id, origin_id))
            in_id = cur.fetchone()[0]
        metrics["patterns"].add(in_id)
            
        conn.commit()
        return route_id, out_id, in_id

def parse_layout_a(pdf, filename, conn, file_id, route_num, origin, dest):
    """Simple 7 column layout"""
    origin_stop_id = get_or_create_stop(conn, clean_stop_name(origin, filename))
    dest_stop_id = get_or_create_stop(conn, clean_stop_name(dest, filename))
    route_id, out_pattern_id, in_pattern_id = get_or_create_route(conn, route_num, origin_stop_id, dest_stop_id, file_id)
    ensure_route_stops(conn, out_pattern_id, [origin_stop_id, dest_stop_id])
    ensure_route_stops(conn, in_pattern_id, [dest_stop_id, origin_stop_id])

    stop_times_to_insert = []
    
    for page in pdf.pages:
        tables = page.extract_tables()
        if not tables or len(tables[0][0]) < 3:
            tables = page.extract_tables({'vertical_strategy': 'text', 'horizontal_strategy': 'text'})
        for table in tables:
            if not table or len(table[0]) != 7: continue
            
            start_row = 0
            for i, row in enumerate(table):
                if row and len(row) > 2 and row[1] and re.search(r'\d{2}[:\.]\d{2}', row[1]):
                    start_row = i
                    break
                    
            for row in table[start_row:]:
                if not row or len(row) < 7: continue
                
                out_dep, out_dep_off, out_dep_raw, out_dep_acc = clean_time(row[1], filename)
                out_arr, out_arr_off, out_arr_raw, out_arr_acc = clean_time(row[2], filename)
                in_dep, in_dep_off, in_dep_raw, in_dep_acc = clean_time(row[4], filename)
                in_arr, in_arr_off, in_arr_raw, in_arr_acc = clean_time(row[5], filename)
                
                if out_dep_acc != 'UNKNOWN' or out_arr_acc != 'UNKNOWN':
                    trip_id = insert_trip(conn, out_pattern_id, file_id)
                    stop_times_to_insert.append((trip_id, origin_stop_id, 1, out_dep, out_dep, out_dep_off, out_dep_raw, out_dep_acc))
                    stop_times_to_insert.append((trip_id, dest_stop_id, 2, out_arr, out_arr, out_arr_off, out_arr_raw, out_arr_acc))
                    
                if in_dep_acc != 'UNKNOWN' or in_arr_acc != 'UNKNOWN':
                    trip_id = insert_trip(conn, in_pattern_id, file_id)
                    stop_times_to_insert.append((trip_id, dest_stop_id, 1, in_dep, in_dep, in_dep_off, in_dep_raw, in_dep_acc))
                    stop_times_to_insert.append((trip_id, origin_stop_id, 2, in_arr, in_arr, in_arr_off, in_arr_raw, in_arr_acc))
                    
    insert_stop_times_batch(conn, stop_times_to_insert)
    conn.commit()
    return True

def parse_layout_b(pdf, filename, conn, file_id, route_num, origin, dest):
    """Panel 11-13 column layout"""
    logger.info("Entering parse_layout_b")
    full_table = []
    for p_idx, page in enumerate(pdf.pages):
        logger.info(f"Extracting tables from page {p_idx}")
        tables = page.extract_tables()
        if not tables or len(tables[0][0]) < 3:
            tables = page.extract_tables({'vertical_strategy': 'text', 'horizontal_strategy': 'text'})
        for table in tables:
            if table and len(table[0]) > 7:
                full_table.extend(table)
                
    if not full_table:
        metrics["warnings"].append(f"No valid tables found for Layout B in {filename}")
        return False

    logger.info(f"Aggregated {len(full_table)} rows.")
    cols = len(full_table[0])
    separator_idx = cols // 2
    
    headers = full_table[0]
    out_stop_names = [clean_stop_name(headers[i], filename) for i in range(1, separator_idx) if headers[i]]
    in_stop_names = [clean_stop_name(headers[i], filename) for i in range(separator_idx + 1, cols) if i < len(headers) and headers[i]]
    
    if not out_stop_names: out_stop_names = [origin, dest]
    if not in_stop_names: in_stop_names = [dest, origin]
        
    logger.info(f"Stop names extracted. Getting IDs...")
    out_stop_ids = [get_or_create_stop(conn, name) for name in out_stop_names]
    in_stop_ids = [get_or_create_stop(conn, name) for name in in_stop_names]
    
    logger.info("Getting route patterns...")
    route_id, out_pattern_id, in_pattern_id = get_or_create_route(
        conn, route_num, out_stop_ids[0], out_stop_ids[-1], file_id
    )
    ensure_route_stops(conn, out_pattern_id, out_stop_ids)
    ensure_route_stops(conn, in_pattern_id, in_stop_ids)

    logger.info("Finding start row...")
    start_row = 1
    for i, row in enumerate(full_table):
        if row and row[0] and (row[0].strip() == 'SLTB' or re.match(r'[A-Z]-\d+', row[0])):
            start_row = i
            break
            
    pending_trips = []
    pending_stop_times = []
    
    logger.info(f"Starting parsing at row {start_row}...")
    for row_idx, row in enumerate(full_table[start_row:]):
        if not row: continue
        
        out_running_num = row[0]
        if out_running_num and re.match(r'[A-Z\d\-]+', out_running_num):
            metrics["operator_types"].add("SLTB" if "SLTB" in out_running_num else "PRIVATE")
            outbound_times = []
            valid_outbound = False
            for i in range(1, separator_idx):
                if i < len(row):
                    t, off, raw, acc = clean_time(row[i], filename)
                    outbound_times.append((t, off, raw, acc))
                    if acc != 'UNKNOWN': valid_outbound = True
                else:
                    outbound_times.append((None, 0, None, 'UNKNOWN'))
                    
            if valid_outbound:
                pending_trips.append((out_pattern_id, file_id, out_running_num))
                pending_stop_times.append([
                    (stop_id, seq, t, t, off, raw, acc)
                    for seq, (stop_id, (t, off, raw, acc)) in enumerate(zip(out_stop_ids, outbound_times), 1) if acc != 'UNKNOWN'
                ])
        
        in_running_col = separator_idx + 1 if len(row) > separator_idx + 1 else -1
        if in_running_col != -1 and len(row) > in_running_col:
            in_running_num = row[in_running_col]
            if in_running_num and re.match(r'[A-Z\d\-]+', in_running_num):
                inbound_times = []
                valid_inbound = False
                for i in range(in_running_col + 1, cols):
                    if i < len(row):
                        t, off, raw, acc = clean_time(row[i], filename)
                        inbound_times.append((t, off, raw, acc))
                        if acc != 'UNKNOWN': valid_inbound = True
                    else:
                        inbound_times.append((None, 0, None, 'UNKNOWN'))
                        
                if valid_inbound:
                    pending_trips.append((in_pattern_id, file_id, in_running_num))
                    pending_stop_times.append([
                        (stop_id, seq, t, t, off, raw, acc)
                        for seq, (stop_id, (t, off, raw, acc)) in enumerate(zip(in_stop_ids, inbound_times), 1) if acc != 'UNKNOWN'
                    ])
                        
    logger.info("Batch inserting trips...")
    inserted_trip_ids = insert_trips_batch(conn, pending_trips)
    
    stop_times_to_insert = []
    for trip_id, stops in zip(inserted_trip_ids, pending_stop_times):
        for stop_id, seq, arr, dep, off, raw, acc in stops:
            stop_times_to_insert.append((trip_id, stop_id, seq, arr, dep, off, raw, acc))
            
    logger.info("Batch inserting stop times...")
    insert_stop_times_batch(conn, stop_times_to_insert)
    logger.info("Committing to DB...")
    conn.commit()
    logger.info("Exiting parse_layout_b")
    return True

def parse_layout_c(pdf, filename, conn, file_id, route_num, origin, dest):
    """Vertical Detailed 13 col layout"""
    # 0: Outbound stops
    # 4,5: Outbound Trip 1, 2 times
    # 7: Inbound stops
    # 10,11: Inbound Trip 1, 2 times
    
    full_table = []
    for page in pdf.pages:
        tables = page.extract_tables()
        if not tables or len(tables[0][0]) < 3:
            tables = page.extract_tables({'vertical_strategy': 'text', 'horizontal_strategy': 'text'})
        for table in tables:
            if table and len(table[0]) == 13:
                full_table.extend(table)
                
    if not full_table or len(full_table) < 4:
        return False
        
    out_trip1_permit = clean_stop_name(full_table[2][4])
    out_trip2_permit = clean_stop_name(full_table[2][5])
    in_trip1_permit = clean_stop_name(full_table[2][10])
    in_trip2_permit = clean_stop_name(full_table[2][11])
    
    # Read stops
    out_stops = []
    in_stops = []
    
    for row in full_table[3:]:
        if not row: continue
        if row[0]: out_stops.append(clean_stop_name(row[0], filename))
        if row[7]: in_stops.append(clean_stop_name(row[7], filename))
        
    if not out_stops: out_stops = [origin, dest]
    if not in_stops: in_stops = [dest, origin]
        
    out_stop_ids = [get_or_create_stop(conn, name) for name in out_stops]
    in_stop_ids = [get_or_create_stop(conn, name) for name in in_stops]
    
    route_id, out_pattern_id, in_pattern_id = get_or_create_route(
        conn, route_num, out_stop_ids[0], out_stop_ids[-1], file_id
    )
    ensure_route_stops(conn, out_pattern_id, out_stop_ids)
    ensure_route_stops(conn, in_pattern_id, in_stop_ids)
    
    # Outbound Trips
    trip_out1 = insert_trip(conn, out_pattern_id, file_id, out_trip1_permit)
    trip_out2 = insert_trip(conn, out_pattern_id, file_id, out_trip2_permit)
    
    stop_times_to_insert = []
    
    seq = 1
    for row in full_table[3:]:
        if not row: continue
        if row[0]:
            t1, off1, raw1, acc1 = clean_time(row[4], filename)
            t2, off2, raw2, acc2 = clean_time(row[5], filename)
            if acc1 != 'UNKNOWN': stop_times_to_insert.append((trip_out1, out_stop_ids[seq-1], seq, t1, t1, off1, raw1, acc1))
            if acc2 != 'UNKNOWN': stop_times_to_insert.append((trip_out2, out_stop_ids[seq-1], seq, t2, t2, off2, raw2, acc2))
            seq += 1

    # Inbound Trips
    trip_in1 = insert_trip(conn, in_pattern_id, file_id, in_trip1_permit)
    trip_in2 = insert_trip(conn, in_pattern_id, file_id, in_trip2_permit)
    
    seq = 1
    for row in full_table[3:]:
        if not row: continue
        if row[7]:
            t1, off1, raw1, acc1 = clean_time(row[10], filename)
            t2, off2, raw2, acc2 = clean_time(row[11], filename)
            if acc1 != 'UNKNOWN': stop_times_to_insert.append((trip_in1, in_stop_ids[seq-1], seq, t1, t1, off1, raw1, acc1))
            if acc2 != 'UNKNOWN': stop_times_to_insert.append((trip_in2, in_stop_ids[seq-1], seq, t2, t2, off2, raw2, acc2))
            seq += 1
            
    insert_stop_times_batch(conn, stop_times_to_insert)
    conn.commit()
    return True

def process_file(filepath, filename, conn, ds_id):
    logger.info(f"Processing: {filename}")
    metrics["files_processed"] += 1
    
    try:
        logger.info("Registering file...")
        file_id, was_cleared = register_source_file(conn, ds_id, filename, filepath)
        if was_cleared:
            logger.info(f"File {filename} was already imported. Cleared old trips for idempotency.")
            
        logger.info("Extracting metadata...")
        route_num, origin, dest = extract_route_metadata(filename)
        if not route_num:
            update_file_status(conn, file_id, 'FAILED', 'No route metadata')
            metrics["files_failed"] += 1
            return
            
        logger.info("Opening PDF...")
        with pdfplumber.open(filepath) as pdf:
            if not pdf.pages:
                update_file_status(conn, file_id, 'FAILED', 'Empty PDF')
                metrics["files_failed"] += 1
                return
                
            logger.info("Detecting layout from page 0...")
            first_page = pdf.pages[0]
            tables = first_page.extract_tables()
            if not tables or len(tables[0][0]) < 3:
                tables = first_page.extract_tables({'vertical_strategy': 'text', 'horizontal_strategy': 'text'})
                
            if not tables:
                update_file_status(conn, file_id, 'FAILED', 'No tables found')
                metrics["files_failed"] += 1
                return
                
            first_table = tables[0]
            cols = len(first_table[0]) if first_table else 0
            logger.info(f"Detected columns: {cols}")
            
            success = False
            if cols == 7:
                logger.info("Detected Layout A")
                success = parse_layout_a(pdf, filename, conn, file_id, route_num, origin, dest)
            elif cols > 7:
                is_layout_c = False
                for row in first_table[:5]:
                    if row and len(row) > 1 and row[1] and 'දුර' in row[1]:
                        is_layout_c = True
                        break
                        
                if is_layout_c:
                    logger.info("Detected Layout C")
                    success = parse_layout_c(pdf, filename, conn, file_id, route_num, origin, dest)
                else:
                    logger.info("Detected Layout B")
                    success = parse_layout_b(pdf, filename, conn, file_id, route_num, origin, dest)
            else:
                metrics["warnings"].append(f"Unsupported layout ({cols} columns) in {filename}")
                update_file_status(conn, file_id, 'FAILED', f'Unsupported layout: {cols} cols')
                metrics["files_failed"] += 1
                return
                
            if success:
                update_file_status(conn, file_id, 'SUCCESS')
                metrics["files_succeeded"] += 1
            else:
                update_file_status(conn, file_id, 'FAILED', 'Parsing failed')
                metrics["files_failed"] += 1
                
    except Exception as e:
        conn.rollback()
        logger.error(f"Error processing {filename}: {e}", exc_info=True)
        metrics["warnings"].append(f"Exception in {filename}: {str(e)}")
        metrics["files_failed"] += 1

def generate_report():
    report_path = r"D:\Bus route\bus-route-backend\docs\DATA_IMPORT_REPORT.md"
    os.makedirs(os.path.dirname(report_path), exist_ok=True)
    with open(report_path, 'w', encoding='utf-8') as f:
        f.write("# Data Import Report\n\n")
        f.write(f"- **Files Processed**: {metrics['files_processed']}\n")
        f.write(f"- **Files Succeeded**: {metrics['files_succeeded']}\n")
        f.write(f"- **Files Failed**: {metrics['files_failed']}\n")
        f.write(f"- **Total Routes**: {len(metrics['routes'])}\n")
        f.write(f"- **Total Patterns**: {len(metrics['patterns'])}\n")
        f.write(f"- **Unique Stops**: {len(metrics['unique_stops'])}\n")
        f.write(f"- **Total Trips**: {metrics['trips']}\n")
        f.write(f"- **Total Stop Times**: {metrics['stop_times']}\n")
        f.write(f"- **Operator Types**: {', '.join(metrics['operator_types'])}\n")
        f.write("\n## Warnings / Errors\n")
        for w in metrics["warnings"]:
            f.write(f"- {w}\n")
            
        f.write("\n## Suspicious Times\n")
        if "suspicious_times" in metrics and metrics["suspicious_times"]:
            for t in set(metrics["suspicious_times"]):
                f.write(f"- {t}\n")
        else:
            f.write("- None found\n")
            
        f.write("\n## Corrupted Text\n")
        if "corrupted_text" in metrics and metrics["corrupted_text"]:
            for c in set(metrics["corrupted_text"]):
                f.write(f"- {c}\n")
        else:
            f.write("- None found\n")
            
        f.write("\n## Remaining Failed Files\n")
        try:
            with conn.cursor() as cur:
                cur.execute("SELECT filename, error_message FROM source_files WHERE import_status != 'SUCCESS'")
                failed = cur.fetchall()
                if failed:
                    for filename, error in failed:
                        f.write(f"- {filename}: {error}\n")
                else:
                    f.write("- None\n")
        except Exception as e:
            f.write(f"- Failed to query: {e}\n")
            
        f.write("\n## Duplicate Stop Candidates\n")
        try:
            with conn.cursor() as cur:
                cur.execute('SELECT id, name_en FROM stops')
                stops = cur.fetchall()
            candidates = []
            for i, s1 in enumerate(stops):
                for s2 in stops[i+1:]:
                    n1 = s1[1].lower().replace(' ', '')
                    n2 = s2[1].lower().replace(' ', '')
                    if n1 == n2 and s1[1] != s2[1]:
                        candidates.append((s1[1], s2[1]))
            if not candidates:
                f.write('- None found\n')
            else:
                for c in set(candidates):
                    f.write(f'- {c[0]} <-> {c[1]}\n')
        except Exception as e:
            f.write(f"- Failed to query: {e}\n")

if __name__ == "__main__":
    DATA_DIR = r"D:\Bus route\bus-route-backend\Normal - Semi Luxury"
    
    conn = get_connection()
    try:
        ds_id = ensure_data_source(conn)
        
        # We will process all PDFs now that the layout parsers are added.
        for filename in os.listdir(DATA_DIR):
            if filename.lower().endswith('.pdf'):
                filepath = os.path.join(DATA_DIR, filename)
                process_file(filepath, filename, conn, ds_id)
                
        generate_report()
        logger.info("Import completed and report generated.")
            
    finally:
        conn.close()
