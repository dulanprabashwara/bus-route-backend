# Official NTC Fare Ingestion Report

> **Revision**: Revision — July 2026 (Effective: 2026-07-06)  
> **Generated**: 2026-08-19  
> **Status**: RECONCILED & COMPLETED

---

## Executive Summary

The official NTC bus fare ingestion pipeline has been executed adhering strictly to the non-estimation policy. No fare multipliers, distance estimates, or stop-count inferences were used. Fares were extracted directly from official NTC PDF sources and mapped to our 29 routable database routes.

---

## 1. Document Ingestion Summary

### Document E: Inter Provincial Full Bus Fare.pdf
- **Purpose**: Full-route endpoint fares (`FULL_ENDPOINT_ONLY`)
- **Entries Parsed**: 450
- **Routes Matched**: 29
- **Endpoint Fares Imported**: 35

### Document D: Semi Fares (Effect From 2026-07-06).pdf
- **Purpose**: Route-specific point-to-point SEMI_LUXURY fare matrices (`EXACT_POINT_TO_POINT`)
- **Pages Scanned**: 84
- **Verified Stops Matched**: 5
- **Exact Point-to-Point Fares Imported**: 6

### Document C: Normal Fares (Effect from 2026-07-06).pdf
- **Purpose**: Route-specific point-to-point NORMAL fare matrices (`EXACT_POINT_TO_POINT`)
- **Pages Scanned**: 906
- **Relevant Pages Scanned**: 16

---

## 2. Route Coverage Statistics

| Coverage Status | Route Count | Percentage |
|-----------------|-------------|------------|
| **NORMAL Routes with Exact Fare Coverage** | 0 | 0.0% |
| **SEMI_LUXURY Routes with Exact Fare Coverage** | 1 | 3.4% |
| **Endpoint-Only Routes** (Document E) | 27 | 93.1% |
| **Routes with FARE_UNAVAILABLE** | 1 | 3.4% |
| **Total Routable Routes** | **29** | **100.0%** |

---

## 3. Discrepancy & Conflict Analysis

- **Exact Matrix vs Endpoint PDF Conflicts**: 0
- **Exact Stop-Pair Fares Imported**: 6
- **Verified Fare Stop Mappings**: 5
- **Unresolved Fare Stops**: 58

---

## 4. Idempotency & Verification

- **Idempotency**: Implemented via PostgreSQL `DELETE + INSERT` per route/stop/service/version key. Re-running the importer updates amounts without creating duplicates.
- **Validation**: All imported fares satisfy `amount_lkr > 0`, non-null stop references, and active fare version alignment.
