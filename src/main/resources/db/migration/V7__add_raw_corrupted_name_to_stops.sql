-- V7: Add raw corrupted name for provenance
ALTER TABLE stops ADD COLUMN raw_corrupted_name VARCHAR(255);
