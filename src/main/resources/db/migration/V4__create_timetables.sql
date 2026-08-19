-- ============================================================
-- V4: Service calendars, trips, and stop_times
-- ============================================================

-- service_calendars: Operating day patterns
CREATE TABLE service_calendars (
    id         BIGSERIAL PRIMARY KEY,
    monday     BOOLEAN NOT NULL DEFAULT TRUE,
    tuesday    BOOLEAN NOT NULL DEFAULT TRUE,
    wednesday  BOOLEAN NOT NULL DEFAULT TRUE,
    thursday   BOOLEAN NOT NULL DEFAULT TRUE,
    friday     BOOLEAN NOT NULL DEFAULT TRUE,
    saturday   BOOLEAN NOT NULL DEFAULT TRUE,
    sunday     BOOLEAN NOT NULL DEFAULT TRUE,
    start_date DATE,
    end_date   DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- calendar_dates: Exceptional additions/removals for specific dates
CREATE TABLE calendar_dates (
    id                  BIGSERIAL PRIMARY KEY,
    service_calendar_id BIGINT      NOT NULL REFERENCES service_calendars(id) ON DELETE CASCADE,
    date                DATE        NOT NULL,
    exception_type      VARCHAR(20) NOT NULL DEFAULT 'REMOVED'  -- ADDED, REMOVED
);

CREATE INDEX idx_calendar_dates_calendar ON calendar_dates(service_calendar_id);

-- trips: One scheduled bus journey
CREATE TABLE trips (
    id                    BIGSERIAL PRIMARY KEY,
    route_pattern_id      BIGINT      NOT NULL REFERENCES route_patterns(id) ON DELETE CASCADE,
    operator_id           BIGINT      REFERENCES operators(id),
    service_calendar_id   BIGINT      REFERENCES service_calendars(id),
    running_number        VARCHAR(100),          -- Timetable running number (e.g. 'SLTB', 'C-1', 'K-5')
    bus_registration      VARCHAR(50),           -- Vehicle registration if known (e.g. 'ND-3662')
    ntc_permit_number     VARCHAR(50),           -- NTC permit/reference (e.g. 'NTC-11896')
    service_type          VARCHAR(50) NOT NULL DEFAULT 'NORMAL',
    trip_headsign         VARCHAR(255),
    source_file_id        BIGINT      REFERENCES source_files(id),
    active                BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_trips_pattern ON trips(route_pattern_id);
CREATE INDEX idx_trips_source ON trips(source_file_id);
CREATE INDEX idx_trips_running_number ON trips(running_number);

-- stop_times: Scheduled arrival/departure at each stop for a trip
-- Times stored as TIME without timezone.
-- For overnight trips, times can exceed 24:00 conceptually,
-- but PostgreSQL TIME wraps at 24:00. We store a day_offset column to handle this.
CREATE TABLE stop_times (
    id              BIGSERIAL PRIMARY KEY,
    trip_id         BIGINT      NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    stop_id         BIGINT      NOT NULL REFERENCES stops(id),
    stop_sequence   INT         NOT NULL,
    arrival_time    TIME WITHOUT TIME ZONE,
    departure_time  TIME WITHOUT TIME ZONE,
    day_offset      INT         NOT NULL DEFAULT 0,   -- 0 = same day, 1 = next day (for overnight trips)
    time_accuracy   VARCHAR(20) NOT NULL DEFAULT 'EXACT',  -- EXACT, ESTIMATED, UNKNOWN

    CONSTRAINT uq_stop_times_trip_seq UNIQUE (trip_id, stop_sequence)
);

CREATE INDEX idx_stop_times_trip ON stop_times(trip_id);
CREATE INDEX idx_stop_times_stop ON stop_times(stop_id);
CREATE INDEX idx_stop_times_departure ON stop_times(departure_time);
CREATE INDEX idx_stop_times_composite ON stop_times(stop_id, departure_time);
