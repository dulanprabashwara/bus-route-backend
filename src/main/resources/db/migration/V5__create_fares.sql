-- ============================================================
-- V5: Fare tables
-- ============================================================

-- fare_versions: A specific fare revision (effective date range)
CREATE TABLE fare_versions (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    effective_from  DATE         NOT NULL,
    effective_to    DATE,
    source_file_id  BIGINT       REFERENCES source_files(id),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- fare_stage_prices: Official NTC fare-stage price chart
-- e.g., for NORMAL service, 5 stages = Rs. 42
CREATE TABLE fare_stage_prices (
    id              BIGSERIAL PRIMARY KEY,
    fare_version_id BIGINT       NOT NULL REFERENCES fare_versions(id) ON DELETE CASCADE,
    service_type    VARCHAR(50)  NOT NULL DEFAULT 'NORMAL',
    stage_count     INT          NOT NULL,
    amount_lkr      DECIMAL(10,2) NOT NULL,

    CONSTRAINT uq_fare_stage UNIQUE (fare_version_id, service_type, stage_count)
);

CREATE INDEX idx_fare_stage_prices_version ON fare_stage_prices(fare_version_id);

-- route_fares: Official route-specific fares between two stops
CREATE TABLE route_fares (
    id                BIGSERIAL PRIMARY KEY,
    route_pattern_id  BIGINT        REFERENCES route_patterns(id) ON DELETE CASCADE,
    from_stop_id      BIGINT        NOT NULL REFERENCES stops(id),
    to_stop_id        BIGINT        NOT NULL REFERENCES stops(id),
    service_type      VARCHAR(50)   NOT NULL DEFAULT 'NORMAL',
    amount_lkr        DECIMAL(10,2) NOT NULL,
    fare_version_id   BIGINT        REFERENCES fare_versions(id),
    source_file_id    BIGINT        REFERENCES source_files(id),
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_route_fares_pattern ON route_fares(route_pattern_id);
CREATE INDEX idx_route_fares_stops ON route_fares(from_stop_id, to_stop_id);
