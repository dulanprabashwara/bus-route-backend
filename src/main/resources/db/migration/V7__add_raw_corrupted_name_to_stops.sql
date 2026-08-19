-- V7: Add raw corrupted name for provenance
ALTER TABLE stops ADD COLUMN IF NOT EXISTS raw_corrupted_name VARCHAR(255);
