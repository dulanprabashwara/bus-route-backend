-- ============================================================
-- V2: Data sources, source files, and import tracking
-- ============================================================

-- data_sources: Represents an official data provider (NTC, SLTB, CPTSA, etc.)
CREATE TABLE data_sources (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    organization    VARCHAR(100) NOT NULL,  -- NTC, SLTB, CPTSA, SPRPTA
    source_type     VARCHAR(50)  NOT NULL DEFAULT 'FILE',  -- FILE, WEBSITE, API
    url             TEXT,
    priority        INT          NOT NULL DEFAULT 50,  -- Higher = more authoritative
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- source_files: Every imported official timetable/fare file
CREATE TABLE source_files (
    id                BIGSERIAL PRIMARY KEY,
    data_source_id    BIGINT       NOT NULL REFERENCES data_sources(id),
    external_file_id  VARCHAR(255),           -- Google Drive file ID, etc.
    filename          VARCHAR(500) NOT NULL,
    source_url        TEXT,
    mime_type         VARCHAR(100),
    file_size         BIGINT,
    checksum_sha256   VARCHAR(64),
    source_modified_at TIMESTAMPTZ,
    downloaded_at     TIMESTAMPTZ,
    imported_at       TIMESTAMPTZ,
    import_status     VARCHAR(50)  NOT NULL DEFAULT 'PENDING',  -- PENDING, PROCESSING, SUCCESS, FAILED, SKIPPED
    effective_from    DATE,
    effective_to      DATE,
    error_message     TEXT,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_source_files_checksum UNIQUE (checksum_sha256)
);

CREATE INDEX idx_source_files_data_source ON source_files(data_source_id);
CREATE INDEX idx_source_files_status ON source_files(import_status);
CREATE INDEX idx_source_files_filename ON source_files(filename);

-- import_runs: Track each import execution
CREATE TABLE import_runs (
    id                BIGSERIAL PRIMARY KEY,
    data_source_id    BIGINT       REFERENCES data_sources(id),
    started_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    completed_at      TIMESTAMPTZ,
    status            VARCHAR(50)  NOT NULL DEFAULT 'RUNNING',  -- RUNNING, SUCCESS, PARTIAL, FAILED
    files_discovered  INT          NOT NULL DEFAULT 0,
    files_changed     INT          NOT NULL DEFAULT 0,
    records_inserted  INT          NOT NULL DEFAULT 0,
    records_updated   INT          NOT NULL DEFAULT 0,
    records_rejected  INT          NOT NULL DEFAULT 0,
    notes             TEXT
);
