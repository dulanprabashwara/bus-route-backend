import os
import json
import psycopg2
from psycopg2.extras import RealDictCursor
from dotenv import load_dotenv

load_dotenv()

def get_db():
    return psycopg2.connect(
        host=os.getenv("DB_HOST"),
        port=os.getenv("DB_PORT", "5432"),
        database=os.getenv("DB_NAME"),
        user=os.getenv("DB_USERNAME"),
        password=os.getenv("DB_PASSWORD")
    )

def safe_str(val):
    if val is None:
        return "None"
    return str(val).encode('ascii', 'backslashreplace').decode('ascii')

def run_audit():
    conn = get_db()
    cur = conn.cursor(cursor_factory=RealDictCursor)
    
    output = []
    def log(msg=""):
        print(safe_str(msg))
        output.append(str(msg))
        
    log("==================================================")
    log(" 1. DATABASE COUNTS AUDIT")
    log("==================================================")
    
    queries = {
        "v_routable_stops": "SELECT COUNT(*) FROM v_routable_stops",
        "v_routable_patterns": "SELECT COUNT(*) FROM v_routable_patterns",
        "v_routable_trips": "SELECT COUNT(*) FROM v_routable_trips",
        "v_routable_stop_times": "SELECT COUNT(*) FROM v_routable_stop_times",
        "routable_routes": "SELECT COUNT(DISTINCT route_id) FROM v_routable_patterns",
        "total_routes": "SELECT COUNT(*) FROM routes",
        "total_patterns": "SELECT COUNT(*) FROM route_patterns",
        "total_trips": "SELECT COUNT(*) FROM trips",
        "total_stop_times": "SELECT COUNT(*) FROM stop_times",
        "valid_stop_times": "SELECT COUNT(*) FROM stop_times WHERE time_accuracy = 'VALID' AND departure_time IS NOT NULL",
        "quarantined_stop_times": "SELECT COUNT(*) FROM stop_times WHERE time_accuracy = 'INVALID' OR departure_time IS NULL",
        "total_fares": "SELECT COUNT(*) FROM route_fares",
        "exact_fares": "SELECT COUNT(*) FROM route_fares WHERE fare_type = 'EXACT_POINT_TO_POINT'",
        "endpoint_fares": "SELECT COUNT(*) FROM route_fares WHERE fare_type = 'FULL_ENDPOINT_ONLY'"
    }
    
    counts = {}
    for key, q in queries.items():
        cur.execute(q)
        counts[key] = cur.fetchone()["count"]
        log(f"  {key:<25}: {counts[key]}")
        
    log("\n==================================================")
    log(" 2. ROUTE 01 (KANDY <-> COLOMBO) INVESTIGATION")
    log("==================================================")
    
    cur.execute("""
        SELECT r.id as route_id, r.route_number, r.name as route_name, r.service_type
        FROM routes r
        WHERE r.route_number IN ('01', '1', '1-1', '01-1') OR r.name ILIKE '%colombo%kandy%' OR r.name ILIKE '%kandy%colombo%'
    """)
    r01_routes = cur.fetchall()
    log(f"Found {len(r01_routes)} route records matching Route 01:")
    for r in r01_routes:
        log(f"\n---> Route ID: {r['route_id']}, Num: '{r['route_number']}', Name: '{r['route_name']}', Service: {r['service_type']}")
        
        # Check patterns
        cur.execute("""
            SELECT rp.id as pattern_id, rp.direction, rp.pattern_name,
                   s1.id as origin_id, s1.name_en as origin_name,
                   s2.id as dest_id, s2.name_en as dest_name
            FROM route_patterns rp
            LEFT JOIN stops s1 ON rp.origin_stop_id = s1.id
            LEFT JOIN stops s2 ON rp.destination_stop_id = s2.id
            WHERE rp.route_id = %s
        """, (r["route_id"],))
        patterns = cur.fetchall()
        for p in patterns:
            log(f"    Pattern ID: {p['pattern_id']}, Dir: {p['direction']}, Name: '{p['pattern_name']}', Origin: [{p['origin_id']}] {p['origin_name']} -> Dest: [{p['dest_id']}] {p['dest_name']}")
            
            # Check route stops
            cur.execute("""
                SELECT rs.stop_sequence, s.id as stop_id, s.name_en as stop_name
                FROM route_stops rs
                JOIN stops s ON rs.stop_id = s.id
                WHERE rs.route_pattern_id = %s
                ORDER BY rs.stop_sequence
            """, (p["pattern_id"],))
            r_stops = cur.fetchall()
            log(f"      Ordered route_stops ({len(r_stops)} stops):")
            for rs in r_stops:
                log(f"        Seq {rs['stop_sequence']}: [{rs['stop_id']}] {rs['stop_name']}")
                
            # Check trips for this pattern
            cur.execute("""
                SELECT t.id as trip_id, t.running_number, t.source_file_id
                FROM trips t
                WHERE t.route_pattern_id = %s
            """, (p["pattern_id"],))
            trips = cur.fetchall()
            log(f"      Trips linked to pattern: {len(trips)}")
            
            for t in trips[:3]: # Sample up to 3 trips
                cur.execute("""
                    SELECT st.stop_sequence, st.departure_time, st.arrival_time, st.time_accuracy, s.name_en as stop_name
                    FROM stop_times st
                    JOIN stops s ON st.stop_id = s.id
                    WHERE st.trip_id = %s
                    ORDER BY st.stop_sequence
                """, (t["trip_id"],))
                stimes = cur.fetchall()
                valid_stimes = [st for st in stimes if st['time_accuracy'] == 'VALID' and st['departure_time'] is not None]
                log(f"        Trip {t['trip_id']} (Num: '{t['running_number']}'): {len(stimes)} total stop_times ({len(valid_stimes)} VALID)")
                if valid_stimes:
                    for st in valid_stimes:
                        log(f"          Seq {st['stop_sequence']}: {st['stop_name']} Dep: {st['departure_time']} Arr: {st['arrival_time']}")

    log("\n==================================================")
    log(" 3. DIRECT ROUTE DISCOVERY AUDIT (ALL 29 PILOT ROUTES)")
    log("==================================================")
    
    cur.execute("""
        SELECT DISTINCT r.id as route_id, r.route_number, r.name as route_name, r.service_type
        FROM routes r
        JOIN route_patterns rp ON rp.route_id = r.id
        ORDER BY r.route_number
    """)
    all_routes = cur.fetchall()
    log(f"Total Routes in Database: {len(all_routes)}")
    
    audit_results = []
    
    for r in all_routes:
        route_id = r["route_id"]
        route_num = r["route_number"]
        
        cur.execute("""
            SELECT rp.id as pattern_id, rp.direction, rp.pattern_name,
                   s1.id as origin_id, s1.name_en as origin_name,
                   s2.id as dest_id, s2.name_en as dest_name
            FROM route_patterns rp
            LEFT JOIN stops s1 ON rp.origin_stop_id = s1.id
            LEFT JOIN stops s2 ON rp.destination_stop_id = s2.id
            WHERE rp.route_id = %s
        """, (route_id,))
        patterns = cur.fetchall()
        
        for p in patterns:
            pattern_id = p["pattern_id"]
            origin_id = p["origin_id"]
            dest_id = p["dest_id"]
            
            if not origin_id or not dest_id:
                log(f"  Route {route_num:<6} ({r['service_type']:<12}) Dir {p['direction']:<8}: Missing Origin/Dest -> Status: NO_VALID_TIMETABLE")
                continue
                
            # Check sequence in route_stops
            cur.execute("""
                SELECT stop_id, stop_sequence FROM route_stops
                WHERE route_pattern_id = %s AND stop_id IN (%s, %s)
            """, (pattern_id, origin_id, dest_id))
            rs_seqs = {row["stop_id"]: row["stop_sequence"] for row in cur.fetchall()}
            
            origin_seq = rs_seqs.get(origin_id)
            dest_seq = rs_seqs.get(dest_id)
            
            seq_valid = (origin_seq is not None and dest_seq is not None and origin_seq < dest_seq)
            
            # Single SQL check for valid times
            cur.execute("""
                SELECT st1.departure_time as dep, st2.arrival_time as arr
                FROM v_routable_stop_times st1
                JOIN v_routable_stop_times st2 ON st1.trip_id = st2.trip_id
                JOIN v_routable_trips vt ON vt.id = st1.trip_id
                WHERE vt.route_pattern_id = %s
                  AND st1.stop_id = %s AND st2.stop_id = %s
                  AND st1.stop_sequence < st2.stop_sequence
                LIMIT 1
            """, (pattern_id, origin_id, dest_id))
            pair = cur.fetchone()
            
            has_valid_times = pair is not None
            sample_dep = str(pair["dep"]) if pair else None
            sample_arr = str(pair["arr"]) if pair else None
                    
            status = "UNKNOWN"
            if not has_valid_times:
                status = "NO_VALID_TIMETABLE"
            elif seq_valid and has_valid_times:
                status = "PASS"
            else:
                status = "FAIL"
                
            res = {
                "route_number": route_num,
                "route_name": r["route_name"],
                "service_type": r["service_type"],
                "pattern_id": pattern_id,
                "direction": p["direction"],
                "origin": p["origin_name"],
                "destination": p["dest_name"],
                "seq_valid": seq_valid,
                "sample_dep": sample_dep,
                "sample_arr": sample_arr,
                "status": status
            }
            audit_results.append(res)
            log(f"  Route {route_num:<6} ({r['service_type']:<12}) Dir {p['direction']:<8}: {p['origin_name']} -> {p['dest_name']} | Status: {status}")

    with open("audit_full_log.txt", "w", encoding="utf-8") as f:
        f.write("\n".join(output))

    with open("audit_results.json", "w", encoding="utf-8") as f:
        json.dump(audit_results, f, indent=2)

    cur.close()
    conn.close()

if __name__ == "__main__":
    run_audit()
