# Sri Lanka Bus Journey Planner - Pilot System Readiness Report

**Date:** August 19, 2026  
**System Evaluated:** 29-Route Pilot Implementation  
**Final System Verdict:** **NOT_READY_FOR_DATASET_SCALING**

---

## 1. System Readiness Summary

The **Sri Lanka Bus Journey Planner** end-to-end web application (Spring Boot API + Next.js Webpack Frontend + Aiven PostgreSQL Database) is fully functional and successfully serves inter-provincial bus journey routing, stop departures, route details, and NTC fare calculations.

However, based on our automated pilot database audit, the overall system readiness status is evaluated as **NOT_READY_FOR_DATASET_SCALING**. 

Scaling to the national dataset (hundreds of inter-provincial routes and thousands of provincial routes) at this time will compound data ingestion defects, leading to missing direct routes and degraded search quality.

---

## 2. Key Diagnostic Metrics

```text
================================================================================
PILOT DATABASE METRICS & AUDIT RESULTS
================================================================================
Routable Patterns       : 58
Routable Trips          : 1,558
Valid Stop Times        : 4,000 (81.7%)
Quarantined Stop Times  : 898   (18.3%)
--------------------------------------------------------------------------------
Matrix Fares Ingested   : 39
  - Exact Fares         : 6
  - Endpoint Fallback   : 33
--------------------------------------------------------------------------------
Direct Timetable Passage Audit (29 Routes):
  - Directions Passing  : 11 directions
  - Directions Blocked  : 47 directions (Quarantined or unparsed OCR times)
================================================================================
```

---

## 3. Primary Bottlenecks & Data Defect Analysis

### 1. High Data Quarantine Rate (18.3% of Stop Times)
* **898 stop time entries** failed format validation during PDF ingestion.
* Common causes include malformed OCR strings (e.g. `99:95`, `(cid:11)` font character corruption, and missing departure fields).
* Impact: Safe database views (`v_routable_stop_times`) correctly filter these out, causing 47 route directions to lack valid routable timetables.

### 2. Core Trunk Route Data Corruption (Route 01: Kandy ↔ Colombo)
* Route 01 is the single most critical inter-provincial bus route in Sri Lanka.
* Due to PDF character map encoding issues, origin and destination stops for Route 01 were ingested as `මහ(cid:25)වර` and `ැක%ළඹ` instead of canonical `Kandy` and `Colombo`.
* All 183 trips under Route 01 have 0 valid stop times, causing direct searches for `Kandy → Colombo` to fall back to transfers via Kegalle.

### 3. Intermediate Fare Matrix Coverage (Sinhala Stop Reconciliation)
* Document C/D NTC matrix PDFs contain 695 unmatched Sinhala stop instances.
* Out of 39 active fare records, only **6 exact matrix fares** are currently matched. The remaining 33 rely on end-to-end route fallback (`ENDPOINT_ONLY`).

---

## 4. Architectural Readiness Assessment

| System Layer | Readiness Status | Findings & Evaluation |
| :--- | :--- | :--- |
| **PostgreSQL Database & Views** | **READY** | Schema, spatial PostGIS indexing, trigram matching, and `v_routable_` safety views isolate clean data cleanly. |
| **Spring Boot Backend Services** | **READY** | Journey engine, multi-leg transfer solver, NTC fare calculator, and dominated journey pruning operate accurately. |
| **Next.js Webpack Frontend** | **READY** | Fully responsive, dark mode UI, Webpack-configured Next.js dev/production build, clean stop autocompletion. |
| **Timetable & Fare Ingestion Pipeline** | **NOT READY** | PDF/OCR parsing for Sinhala text and non-standard time formats requires structural correction before scaling. |

---

## 5. Mandatory Action Plan Before Scaling to National Dataset

1. **Re-ingest Route 01 & Corrupted Pilot Timetables:** Fix character-map decoding in `importer_db.py` / OCR script to clean raw PDF strings into valid `HH:mm:ss` timestamps and map to canonical stops.
2. **Execute Fare Stop Canonical Reconciliation:** Complete automated matching of the 695 Sinhala fare-matrix stops against canonical stop IDs to expand exact fare coverage beyond 6 records.
3. **Automate Quality-Control Validation Checks:** Introduce an automated pre-ingestion validation pipeline that rejects PDF files with >5% time quarantine rates before writing to production database tables.
4. **Gradual Expansion:** Once all 29 pilot routes achieve 100% valid timetable passage and >80% exact fare coverage, commence national-scale dataset ingestion.
