import os
import psycopg2
from psycopg2.extras import RealDictCursor
from dotenv import load_dotenv
import logging
import hashlib

load_dotenv()

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

def get_connection():
    """Establish and return a connection to the PostgreSQL database."""
    try:
        conn = psycopg2.connect(
            host=os.getenv("DB_HOST"),
            port=os.getenv("DB_PORT", "5432"),
            database=os.getenv("DB_NAME"),
            user=os.getenv("DB_USERNAME"),
            password=os.getenv("DB_PASSWORD")
        )
        return conn
    except Exception as e:
        logger.error(f"Failed to connect to database: {e}")
        raise

def ensure_data_source(conn):
    """Ensure the NTC data source exists and return its ID."""
    with conn.cursor() as cur:
        cur.execute("""
            SELECT id FROM data_sources WHERE name = 'National Transport Commission'
        """)
        row = cur.fetchone()
        if row:
            return row[0]
        
        cur.execute("""
            INSERT INTO data_sources (name, organization, source_type, priority)
            VALUES ('National Transport Commission', 'NTC', 'FILE', 100)
            RETURNING id
        """)
        conn.commit()
        return cur.fetchone()[0]

def calculate_checksum(filepath):
    """Calculate SHA256 checksum of a file."""
    sha256_hash = hashlib.sha256()
    with open(filepath, "rb") as f:
        for byte_block in iter(lambda: f.read(4096), b""):
            sha256_hash.update(byte_block)
    return sha256_hash.hexdigest()

def register_source_file(conn, ds_id, filename, filepath):
    """Register the file in source_files or return existing ID. If existing, clear trips."""
    file_size = os.path.getsize(filepath)
    checksum = calculate_checksum(filepath)
    
    with conn.cursor() as cur:
        # Check by checksum first for strong idempotency
        cur.execute("""
            SELECT id FROM source_files WHERE checksum_sha256 = %s
        """, (checksum,))
        row = cur.fetchone()
        
        if row:
            file_id = row[0]
            # Clear existing trips associated with this file to avoid duplicates on re-run
            cur.execute("DELETE FROM trips WHERE source_file_id = %s", (file_id,))
            cur.execute("""
                UPDATE source_files 
                SET import_status = 'PROCESSING', updated_at = NOW()
                WHERE id = %s
            """, (file_id,))
            conn.commit()
            return file_id, True # True means it existed and was cleared
            
        cur.execute("""
            INSERT INTO source_files (data_source_id, filename, file_size, checksum_sha256, import_status)
            VALUES (%s, %s, %s, %s, 'PROCESSING')
            RETURNING id
        """, (ds_id, filename, file_size, checksum))
        conn.commit()
        return cur.fetchone()[0], False # False means it's new

def update_file_status(conn, file_id, status, error_msg=None):
    with conn.cursor() as cur:
        cur.execute("""
            UPDATE source_files 
            SET import_status = %s, error_message = %s, updated_at = NOW(), imported_at = CASE WHEN %s = 'SUCCESS' THEN NOW() ELSE imported_at END
            WHERE id = %s
        """, (status, error_msg, status, file_id))
        conn.commit()

def get_or_create_stop(conn, stop_name_data):
    """Finds a stop by normalized name or creates it.
    stop_name_data can be a string or a tuple (canonical_name, raw_corrupted_name).
    """
    if not stop_name_data:
        return None
        
    if isinstance(stop_name_data, tuple):
        name_en, raw_corrupted = stop_name_data
    else:
        name_en, raw_corrupted = stop_name_data, None
        
    if not name_en:
        return None
    
    # Do not normalize if it's explicitly marked as corrupted fallback
    if name_en == "Unknown Stop (Corrupted)" and raw_corrupted:
        normalized = raw_corrupted.strip().lower()
    else:
        normalized = name_en.strip().lower()
    
    with conn.cursor() as cur:
        cur.execute("SELECT id FROM stops WHERE normalized_name = %s", (normalized,))
        row = cur.fetchone()
        if row:
            return row[0]
            
        cur.execute("""
            INSERT INTO stops (name_en, normalized_name, raw_corrupted_name)
            VALUES (%s, %s, %s)
            RETURNING id
        """, (name_en.strip(), normalized, raw_corrupted))
        stop_id = cur.fetchone()[0]
        conn.commit()
        return stop_id

def ensure_route_stops(conn, route_pattern_id, stop_ids):
    """Populates route_stops table for a pattern if it hasn't been populated yet."""
    if not stop_ids: return
    with conn.cursor() as cur:
        cur.execute("SELECT id FROM route_stops WHERE route_pattern_id = %s", (route_pattern_id,))
        if cur.fetchone():
            return
            
        from psycopg2.extras import execute_batch
        query = """
            INSERT INTO route_stops (route_pattern_id, stop_id, stop_sequence)
            VALUES (%s, %s, %s)
        """
        data = [(route_pattern_id, stop_id, seq) for seq, stop_id in enumerate(stop_ids, 1)]
        execute_batch(cur, query, data)
        conn.commit()


