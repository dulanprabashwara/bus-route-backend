# Implementation Status — Sri Lanka Bus Route Journey Planner

## Project Status: 29-Route Pilot Application Complete (Phase A – L)

### ✅ Completed Phases

- **Phase A — Pilot Scope Lockdown**
  - Confirmed 29 routable bus routes in the pilot dataset.
  - Verified PostGIS, `pg_trgm`, and schema version V10 in Aiven PostgreSQL.

- **Phase B & C — Database Views & Domain Entities**
  - Created `v_routable_routes`, `v_routable_route_stops`, `v_routable_trips`, `v_routable_stop_times`, `v_routable_direct_legs`, `v_routable_fare_stage_lookup`, and `v_routable_route_fares`.
  - Implemented JPA Entities: `Stop`, `Route`, `RoutePattern`, `RouteStop`, `Trip`, `StopTime`, `RouteFare`, `FareVersion`.

- **Phase D & E & F — Repositories & Services**
  - **Stop Service**: Trigram similarity search and lookup.
  - **Route Service**: Active route patterns and ordered stopping sequences.
  - **Departure Service**: Live timetable search using `java.time.Clock` configured for `Asia/Colombo`.
  - **Fare Engine Service**: Strictly enforced official July 2026 NTC policy:
    1. Exact Point-to-Point Fares (`EXACT_POINT_TO_POINT`)
    2. Endpoint-Only Fares (`FULL_ENDPOINT_ONLY`)
    3. Intermediate Unpriced Fares (`FARE_UNAVAILABLE` - No estimation as per NTC policy).

- **Phase G — Journey Planner Service**
  - Direct journeys (0 transfers) and 1-transfer connecting journeys.
  - Multi-factor scoring and ranking (`journey.score.*`): duration, transfers, wait time, fare.
  - Returns journey badges: `RECOMMENDED`, `FASTEST`, `CHEAPEST`, `FEWEST_TRANSFERS`, `NEXT_AVAILABLE`.

- **Phase H — REST API Controllers**
  - `/api/v1/stops/search`, `/api/v1/stops/{id}`
  - `/api/v1/routes`, `/api/v1/routes/{id}`
  - `/api/v1/departures`
  - `/api/v1/journeys/search`

- **Phase I & J & K — Validation & Testing**
  - Unit tests for `StopServiceTest` and `FareServiceTest` passing cleanly.
  - Maven build verified (`BUILD SUCCESS`).

- **Phase L — Next.js Frontend Foundation**
  - Created Next.js 16 App Router application (`bus-route-frontend`).
  - Dark glassmorphic design system with vibrant emerald, cyan, amber, and rose accents.
  - Components: `Navbar`, `StopAutocomplete` (trigram backend search), `JourneyCard`, `FareBadge`.
  - Pages:
    - `/`: Journey Search & Results Dashboard.
    - `/routes`: Routable Routes Directory.
    - `/routes/[id]`: Route Details & Stop Sequence.
    - `/stops/[id]/departures`: Stop Live Timetable.
  - Production build compiled successfully (`npx next build --webpack`).

---

### 🚀 Launch / Execution Commands

1. **Start Backend API (Spring Boot)**:
   ```bash
   cd bus-route-backend
   ./mvnw spring-boot:run
   ```

2. **Start Frontend App (Next.js)**:
   ```bash
   cd bus-route-frontend
   npm run dev
   ```
