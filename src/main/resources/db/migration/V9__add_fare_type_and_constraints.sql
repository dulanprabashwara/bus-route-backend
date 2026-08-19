-- ============================================================
-- V9: Add fare_type to route_fares, add uniqueness constraint
-- ============================================================

-- fare_type: EXACT_POINT_TO_POINT | FULL_ENDPOINT_ONLY
ALTER TABLE route_fares
    ADD COLUMN IF NOT EXISTS fare_type VARCHAR(50) NOT NULL DEFAULT 'EXACT_POINT_TO_POINT';

-- route_id: direct FK for endpoint fares (when route_pattern_id is NULL)
ALTER TABLE route_fares
    ADD COLUMN IF NOT EXISTS route_id BIGINT REFERENCES routes(id);

-- Idempotency constraint: prevent duplicate fares for same route/stops/service/version
-- For EXACT_POINT_TO_POINT: keyed on route_pattern_id + stops + service + version
-- For FULL_ENDPOINT_ONLY: keyed on route_id + stops + service + version
CREATE UNIQUE INDEX IF NOT EXISTS uq_route_fares_exact
    ON route_fares (route_pattern_id, from_stop_id, to_stop_id, service_type, fare_version_id)
    WHERE fare_type = 'EXACT_POINT_TO_POINT' AND route_pattern_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_route_fares_endpoint
    ON route_fares (route_id, from_stop_id, to_stop_id, service_type, fare_version_id)
    WHERE fare_type = 'FULL_ENDPOINT_ONLY' AND route_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_route_fares_route ON route_fares(route_id);
CREATE INDEX IF NOT EXISTS idx_route_fares_version ON route_fares(fare_version_id);
