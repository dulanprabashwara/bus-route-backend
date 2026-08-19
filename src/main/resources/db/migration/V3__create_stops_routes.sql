-- ============================================================
-- V3: Operators, stops, stop aliases, routes, route patterns, route stops
-- ============================================================

-- operators: Bus service operators (SLTB, private companies, etc.)
CREATE TABLE operators (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    operator_type       VARCHAR(50)  NOT NULL DEFAULT 'UNKNOWN',  -- SLTB, PRIVATE, UNKNOWN
    province            VARCHAR(100),
    external_reference  VARCHAR(255),
    active              BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- stops: Canonical bus stop / location
CREATE TABLE stops (
    id              BIGSERIAL PRIMARY KEY,
    name_en         VARCHAR(255),
    name_si         VARCHAR(255),           -- Sinhala
    name_ta         VARCHAR(255),           -- Tamil
    normalized_name VARCHAR(255) NOT NULL,  -- Lowercase, trimmed, for search
    district        VARCHAR(100),
    province        VARCHAR(100),
    latitude        DOUBLE PRECISION,
    longitude       DOUBLE PRECISION,
    location        GEOMETRY(Point, 4326),  -- PostGIS point (WGS84)
    active          BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_stops_normalized_name ON stops(normalized_name);
CREATE INDEX idx_stops_name_trgm ON stops USING gin(normalized_name gin_trgm_ops);
CREATE INDEX idx_stops_location ON stops USING gist(location) WHERE location IS NOT NULL;

-- stop_aliases: Alternate spellings / names that map to a canonical stop
CREATE TABLE stop_aliases (
    id                BIGSERIAL PRIMARY KEY,
    stop_id           BIGINT       NOT NULL REFERENCES stops(id) ON DELETE CASCADE,
    alias             VARCHAR(255) NOT NULL,
    language          VARCHAR(10),           -- en, si, ta
    normalized_alias  VARCHAR(255) NOT NULL,
    source            VARCHAR(100),          -- e.g., 'NTC_TIMETABLE', 'OSM', 'MANUAL'
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_stop_aliases_stop ON stop_aliases(stop_id);
CREATE INDEX idx_stop_aliases_normalized ON stop_aliases(normalized_alias);
CREATE INDEX idx_stop_aliases_trgm ON stop_aliases USING gin(normalized_alias gin_trgm_ops);

-- routes: A public-facing bus route
CREATE TABLE routes (
    id                  BIGSERIAL PRIMARY KEY,
    route_number        VARCHAR(50)  NOT NULL,  -- TEXT, not integer! e.g. '1', '17/2', '87-2'
    name                VARCHAR(500),
    origin_stop_id      BIGINT       REFERENCES stops(id),
    destination_stop_id BIGINT       REFERENCES stops(id),
    operator_id         BIGINT       REFERENCES operators(id),
    service_type        VARCHAR(50)  NOT NULL DEFAULT 'NORMAL',  -- NORMAL, SEMI_LUXURY, LUXURY, etc.
    province            VARCHAR(100),
    inter_provincial    BOOLEAN,
    active              BOOLEAN      NOT NULL DEFAULT TRUE,
    source_file_id      BIGINT       REFERENCES source_files(id),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_routes_number ON routes(route_number);
CREATE INDEX idx_routes_origin ON routes(origin_stop_id);
CREATE INDEX idx_routes_destination ON routes(destination_stop_id);

-- route_patterns: Variants of a route (direction, short working, alternate path)
CREATE TABLE route_patterns (
    id                  BIGSERIAL PRIMARY KEY,
    route_id            BIGINT       NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    direction           VARCHAR(20)  NOT NULL DEFAULT 'OUTBOUND',  -- OUTBOUND, INBOUND
    pattern_name        VARCHAR(500),
    origin_stop_id      BIGINT       REFERENCES stops(id),
    destination_stop_id BIGINT       REFERENCES stops(id),
    active              BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_route_patterns_route ON route_patterns(route_id);

-- route_stops: Ordered list of stops for a route pattern
CREATE TABLE route_stops (
    id                      BIGSERIAL PRIMARY KEY,
    route_pattern_id        BIGINT   NOT NULL REFERENCES route_patterns(id) ON DELETE CASCADE,
    stop_id                 BIGINT   NOT NULL REFERENCES stops(id),
    stop_sequence           INT      NOT NULL,
    fare_stage              INT,
    distance_from_origin_km DOUBLE PRECISION,
    pickup_allowed          BOOLEAN  NOT NULL DEFAULT TRUE,
    dropoff_allowed         BOOLEAN  NOT NULL DEFAULT TRUE,

    CONSTRAINT uq_route_stops_pattern_seq UNIQUE (route_pattern_id, stop_sequence)
);

CREATE INDEX idx_route_stops_pattern ON route_stops(route_pattern_id);
CREATE INDEX idx_route_stops_stop ON route_stops(stop_id);
