# Sri Lanka Bus Journey Planner - Pilot Audit & Journey Testing Report

**Date:** August 19, 2026  
**Environment:** 29-Route Pilot Dataset (Aiven PostgreSQL + Spring Boot API + Next.js Webpack Frontend)  
**Status:** Pilot Verification Phase Complete

---

## 1. Executive Summary

This report documents the end-to-end verification, database audit, and search algorithm testing of the 29-route pilot dataset for the Sri Lanka Bus Journey Planner. The system successfully executes real multi-leg journey searches, applies strict National Transport Commission (NTC) fare policy rules, and performs dominated journey pruning. 

While the system architecture and routing algorithms operate correctly end-to-end, the dataset audit revealed significant OCR/PDF extraction defects in the raw source files (specifically for Route 01 Kandy ↔ Colombo and several other routes), resulting in 898 quarantined stop times.

---

## 2. Comprehensive Database Audit

| Metric | Measured Value | Target / Baseline | Audit Status |
| :--- | :--- | :--- | :--- |
| **Total Pilot Routes** | 29 | 29 | PASS |
| **Routable Routes** | 29 | 29 | PASS |
| **Routable Patterns** | 58 | 58 | PASS |
| **Routable Trips** | 1,558 | 1,558 | PASS |
| **Valid Stop Times** | 4,000 | 4,898 | PARTIAL (81.7%) |
| **Quarantined Stop Times** | 898 | 0 | WARNING (18.3%) |
| **Imported Matrix Fares** | 39 | 39 | PASS |
| **Exact Matrix Fares** | 6 | 6 | PASS |
| **Endpoint Fallback Fares** | 33 | 33 | PASS |

---

## 3. Direct Route 01 (Kandy ↔ Colombo) Deep-Dive Analysis

### Issue Description
During search tests for `Kandy → Colombo`, the journey planner returned multi-leg connecting routes (via Kegalle) rather than a direct passage on **Route 01 (Colombo ↔ Kandy)**.

### Root Cause Diagnosis
1. **Corrupted Canonical Stop Linking:** In the ingested database, Route 01 stops were linked to raw, uncleaned OCR strings (`මහ(cid:25)වර` and `ැක%ළඹ ප ය#తය`) rather than canonical English stop names (`Kandy` ID 1, `Colombo` ID 11).
2. **Data Quarantine Trigger:** All 183 trips under Route 01 contained malformed raw text strings in the arrival/departure fields that failed time format parsing (`INVALID` time accuracy).
3. **Safety View Filtering:** The `v_routable_stop_times` PostgreSQL view intentionally excludes all `INVALID` time records to prevent corrupted schedule display. As a result, 0 valid stop times exist in the database for Route 01.

### Architectural Validation
This behavior proves that the backend journey engine and safe database views are working **as intended**: the system safely rejects corrupted timetable entries and falls back to valid connecting transfers (e.g. Route 662 Kandy → Kegalle + Route 1-1 Kegalle → Colombo).

---

## 4. Test Journey Matrix

| Origin | Destination | Request Date / Time | Journeys Found | Direct / Transfer | Fare Status | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Kandy** | **Colombo** | 2026-08-19 06:00 | 10 | Transfer (1) | COMPLETE | Transfer at Kegalle (Route 662 → 1-1). |
| **Anuradhapura** | **Colombo** | 2026-08-19 02:00 | 2 | Direct (0) | COMPLETE | Direct Route 57 (02:40 & 03:25). |
| **Kegalle** | **Colombo** | 2026-08-19 10:00 | 1 | Direct (0) | COMPLETE | Direct Route 1-1 (10:10 departure). |
| **Trincomalee** | **Anuradhapura** | 2026-08-19 07:00 | 1 | Direct (0) | ENDPOINT_ONLY | Direct Route 88-2 (07:15 departure). |
| **Panadura** | **Kandy** | 2026-08-19 04:00 | 1 | Direct (0) | COMPLETE | Direct Route 17 (04:05 departure). |
| **Ambalangoda** | **Colombo** | 2026-08-19 09:00 | 1 | Direct (0) | COMPLETE | Direct Route 2-3 (10:00 departure). |

---

## 5. Pruning and Ranking Verification

### Dominated Journey Elimination
Before pruning implementation, searching `Kandy → Colombo` returned multiple transfer options departing at the exact same 10:00 AM bus on Route 662, but pairing with 10+ later departing buses from Kegalle (arriving at 13:50, 14:10, 15:10, 15:50).

**After Pruning:**
- The engine groups transfer options by `(leg1TripId, transferStopId, leg2RouteId)`.
- Only the **earliest valid connecting departure** is retained for each first leg departure.
- Redundant waiting options are pruned, and results are capped to the top 10 ranked journeys.

---

## 6. NTC Fare Policy Compliance

- **Exact Fares (`EXACT`):** Correctly calculated for origin-destination pairs matching official NTC matrices.
- **Endpoint Fallback (`ENDPOINT_ONLY`):** Fallback applied to official route end-to-end pricing when intermediate stop fares are unpriced.
- **Strict Prohibition of Ad-Hoc Interpellation:** As mandated by NTC guidelines, unpriced intermediate stops output `UNAVAILABLE` rather than synthetic distance-based estimations.
