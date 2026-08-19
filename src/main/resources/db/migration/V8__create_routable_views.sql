-- ============================================================
-- V8: Views for Routable/Trusted Timetable Data
-- Excludes quarantined invalid times, corrupted stops, unparsed sources
-- ============================================================

CREATE OR REPLACE VIEW v_routable_stops AS
SELECT s.*
FROM stops s
WHERE s.active = TRUE
  AND (s.name_en IS NULL OR s.name_en NOT LIKE '%Corrupted%')
  AND s.raw_corrupted_name IS NULL;

CREATE OR REPLACE VIEW v_routable_patterns AS
SELECT rp.*
FROM route_patterns rp
JOIN routes r ON r.id = rp.route_id
WHERE rp.active = TRUE
  AND r.active = TRUE
  AND EXISTS (
      SELECT 1 FROM route_stops rs WHERE rs.route_pattern_id = rp.id
  );

CREATE OR REPLACE VIEW v_routable_trips AS
SELECT t.*
FROM trips t
JOIN route_patterns rp ON rp.id = t.route_pattern_id
JOIN routes r ON r.id = rp.route_id
LEFT JOIN source_files sf ON sf.id = t.source_file_id
WHERE t.active = TRUE
  AND rp.active = TRUE
  AND r.active = TRUE
  AND (sf.id IS NULL OR sf.import_status = 'SUCCESS')
  AND EXISTS (
      SELECT 1 FROM route_stops rs WHERE rs.route_pattern_id = rp.id
  );

CREATE OR REPLACE VIEW v_routable_stop_times AS
SELECT st.*
FROM stop_times st
JOIN v_routable_trips t ON t.id = st.trip_id
JOIN v_routable_stops s ON s.id = st.stop_id
WHERE st.time_accuracy != 'INVALID'
  AND st.departure_time IS NOT NULL;
