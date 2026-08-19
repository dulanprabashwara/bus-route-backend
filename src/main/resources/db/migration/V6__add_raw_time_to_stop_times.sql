-- V6: Add raw time string for provenance of invalid times
ALTER TABLE stop_times ADD COLUMN IF NOT EXISTS raw_time_string VARCHAR(50);
