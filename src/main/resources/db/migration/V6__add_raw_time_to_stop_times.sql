-- V6: Add raw time string for provenance of invalid times
ALTER TABLE stop_times ADD COLUMN raw_time_string VARCHAR(50);
