-- ============================================================
-- V1: Enable PostgreSQL extensions
-- ============================================================

-- PostGIS for geographic queries (stops near location)
CREATE EXTENSION IF NOT EXISTS postgis;

-- pg_trgm for fuzzy text search on stop names
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- uuid-ossp for generating UUIDs
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
