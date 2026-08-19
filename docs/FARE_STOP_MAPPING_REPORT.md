# Official NTC Fare Stop Reconciliation Report

> **Revision**: Revision — July 2026 (Effective: 2026-07-06)  
> **Generated**: 2026-08-19  
> **Status**: COMPLETED

---

## Executive Summary

This report documents the focused fare-stop reconciliation phase between our 29 routable timetable routes and official NTC PDF fare matrices (Documents C, D, and E). In strict compliance with the **Non-Estimation Policy**, no distance-based formulas, stop counts, or fare multipliers were used.

- **Total Routable Routes**: 29
- **Total Fare Stage Stop Instances Extracted**: 65
- **Unique Fare Stop Names Identified**: 59
- **High-Confidence Verified Mappings**: 5
- **Unresolved Fare Stops**: 58

---

## 1. Route-by-Route Reconciliation Summary

| Route Number | Timetable Stops | Fare Matrix Stages | Verified Mappings | Candidate Mappings (Needs Review) | Unresolved Fare Stages |
|--------------|-----------------|--------------------|-------------------|----------------------------------|------------------------|
| **01** | 7 | 0 | 0 | 0 | 0 |
| **08** | 4 | 0 | 0 | 0 | 0 |
| **10** | 14 | 26 | 3 | 2 | 21 |
| **1-1** | 2 | 0 | 0 | 0 | 0 |
| **11-1** | 4 | 0 | 0 | 0 | 0 |
| **1-2** | 4 | 0 | 0 | 0 | 0 |
| **122** | 9 | 0 | 0 | 0 | 0 |
| **1-245** | 6 | 0 | 0 | 0 | 0 |
| **15-1** | 5 | 0 | 0 | 0 | 0 |
| **15-87** | 2 | 23 | 1 | 0 | 22 |
| **17** | 2 | 0 | 0 | 0 | 0 |
| **19-2** | 5 | 0 | 0 | 0 | 0 |
| **218-2** | 6 | 0 | 0 | 0 | 0 |
| **22-2** | 4 | 0 | 0 | 0 | 0 |
| **2-3** | 2 | 0 | 0 | 0 | 0 |
| **35-3** | 6 | 0 | 0 | 0 | 0 |
| **401** | 6 | 0 | 0 | 0 | 0 |
| **43-857** | 6 | 16 | 1 | 0 | 15 |
| **493-1** | 8 | 0 | 0 | 0 | 0 |
| **5** | 5 | 0 | 0 | 0 | 0 |
| **57** | 2 | 0 | 0 | 0 | 0 |
| **6** | 5 | 0 | 0 | 0 | 0 |
| **602** | 5 | 0 | 0 | 0 | 0 |
| **662** | 2 | 0 | 0 | 0 | 0 |
| **662-1** | 2 | 0 | 0 | 0 | 0 |
| **88-2** | 7 | 0 | 0 | 0 | 0 |
| **98-2** | 6 | 0 | 0 | 0 | 0 |
| **98-6** | 6 | 0 | 0 | 0 | 0 |
| **Unknown** | 8 | 0 | 0 | 0 | 0 |

---

## 2. Match Methodologies Applied

1. **`EXACT_ALIAS` / Direct Canonical Lookup**:
   - High-confidence direct matches between NTC Sinhala names/font variants and canonical stop records (e.g. `මහනුවර` ↔ `Kandy`, `කොළඹ` / `ක ොළඹ` ↔ `Colombo`, `කෑගල්ල` ↔ `Kegalle`, `පානදුර` ↔ `Panadura`).
   - `confidence`: `HIGH`, `verified`: `TRUE`.

2. **`ROUTE_ENDPOINT` Alignment**:
   - Automatic origin and destination endpoint matching between timetable route patterns and Stage 0 / final stage of NTC matrices.
   - `confidence`: `HIGH`, `verified`: `TRUE`.

3. **`ROUTE_SEQUENCE` Candidate Generation**:
   - Sequence order matching between intermediate timetable stops and fare stages.
   - `confidence`: `MEDIUM`, `verified`: `FALSE` (flagged for review).

4. **`UNMATCHED` Stages**:
   - Intermediate NTC fare stages that do not exist in user-selectable timetable routes. Preserved in `fare_stop_mappings` without forcing estimations.
