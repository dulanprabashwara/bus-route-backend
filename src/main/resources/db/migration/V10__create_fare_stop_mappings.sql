-- V10__create_fare_stop_mappings.sql
-- Dedicated fare stop reconciliation mapping table

CREATE TABLE IF NOT EXISTS fare_stop_mappings (
    id BIGSERIAL PRIMARY KEY,
    source_file_id BIGINT REFERENCES source_files(id) ON DELETE SET NULL,
    route_id BIGINT REFERENCES routes(id) ON DELETE CASCADE,
    service_type VARCHAR(20) NOT NULL,
    source_stop_name VARCHAR(255) NOT NULL,
    source_stop_name_normalized VARCHAR(255) NOT NULL,
    source_fare_stage INT,
    canonical_stop_id BIGINT REFERENCES stops(id) ON DELETE SET NULL,
    match_method VARCHAR(50) NOT NULL,
    confidence VARCHAR(20) NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_fare_stop_mapping UNIQUE (route_id, service_type, source_stop_name_normalized, source_fare_stage)
);

CREATE INDEX IF NOT EXISTS idx_fare_stop_mappings_route ON fare_stop_mappings(route_id, service_type);
CREATE INDEX IF NOT EXISTS idx_fare_stop_mappings_stop ON fare_stop_mappings(canonical_stop_id);
