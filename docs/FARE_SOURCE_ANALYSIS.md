# Fare Source & Stage Coverage Analysis Report

> **Revision**: 2026-08-19 — Corrected document roles, full structure analysis, route matching results.

---

## Executive Summary

This document provides a complete analysis of the five official NTC fare documents downloaded from `https://www.ntc.gov.lk/Bus_info/bus_fare.php`, their structural differences, extraction reliability, route-to-fare matching against our 29 routable database routes, and stop-name cross-referencing.

**Key Finding**: The route-specific fare PDFs (Documents C & D) contain **point-to-point fare matrices** with official fare-stage positions and exact inter-stop fares. These are the **preferred fare source** — not the master stage chart. However, their Sinhala-only stop names and the 906-page size of the Normal document present extraction challenges.

---

## 1. Official NTC Fare Revision & Metadata

- **Revision Identifier**: `Revision — July 2026`
- **Effective Date**: `06th July 2026 (from 00.01 hrs)`
- **Primary Web Source**: `https://www.ntc.gov.lk/Bus_info/bus_fare.php`
- **Cross-Check Source**: `https://www.sltb.lk/notice/busFees`

---

## 2. Document Inventory & Corrected Roles

| # | Filename | Purpose | Pages | Size | Text Extractable? |
|---|---------|---------|-------|------|-------------------|
| **A** | `Normal Sinhala 350.pdf` | **Master NORMAL fare-stage price chart** (stages 1–350) | 2 | 1.2 MB | ❌ **Image-only** (0 chars, rendered as vector curves) |
| **B** | `Semi Luxury Sinhala 350.pdf` | **Master SEMI_LUXURY fare-stage price chart** (stages 1–350) | 2 | 1.2 MB | ❌ **Image-only** (0 chars, rendered as vector curves) |
| **C** | `Normal Fares (Effect from 2026-07-06).pdf` | **Route-specific point-to-point NORMAL fare matrices** | 906 | 13.7 MB | ✅ Sinhala text extractable |
| **D** | `Semi Fares (Effect From 2026-07-06).pdf` | **Route-specific point-to-point SEMI_LUXURY fare matrices** | 84 | 5.0 MB | ✅ Sinhala text extractable |
| **E** | `Inter Provincial Full Bus Fare.pdf` | **Full-route endpoint fares** (origin→destination totals) | 3 | 0.5 MB | ✅ Sinhala text extractable |

### SHA256 Checksums
| Filename | SHA256 |
|----------|--------|
| `Normal Fares (Effect from 2026-07-06).pdf` | `7df652c22ae985cdf56aa9e73d38025441ebee23303eb5f171236ecf4c7df3d8` |
| `Semi Fares (Effect From 2026-07-06).pdf` | `63da0ed43f666b49d0df05f2df15d74c0ac170e35327a82f9e3adecc6fdaac7f` |
| `Normal Sinhala 350.pdf` | `cf1a8f60e63e8dd77bb1d23b97243516b7cfc946e9ec24efe853a05dfc1a69a1` |
| `Semi Luxury Sinhala 350.pdf` | `0de1ffd846ee4fd0c39b062db3b8b8871efbd09258fc0704346407d1ef729b9e` |
| `Inter Provincial Full Bus Fare.pdf` | `059f266575a50cbb808f0846d42dedd9c9b9c0753a53b02b0843581ab4b96462` |

---

## 3. Document Structure Analysis

### Document A: Normal Sinhala 350.pdf — MASTER STAGE CHART

- **Purpose**: Official gazetted fare-stage price lookup table for NORMAL service (stages 1–350).
- **Format**: 2-page document rendered entirely as **vector curves** (not text characters).
- **Chars**: 0 | **Images**: 0 | **Curves**: ~9,600 | **Rects**: ~1,100
- **Extraction Reliability**: ❌ **NOT text-extractable with pdfplumber/pdfminer**. Requires OCR (e.g. Tesseract + Sinhala language pack) or manual transcription.
- **Stage Range**: Nominally stages 1–350 (based on document title). Cannot verify programmatically.
- **Route-Specific**: No — this is a universal lookup table.
- **English Names**: No.

### Document B: Semi Luxury Sinhala 350.pdf — MASTER STAGE CHART

- **Purpose**: Official gazetted fare-stage price lookup table for SEMI_LUXURY service (stages 1–350).
- **Format**: Identical vector-curve rendering as Document A.
- **Chars**: 0 | **Images**: 0 | **Curves**: ~9,700 | **Rects**: ~1,100
- **Extraction Reliability**: ❌ **NOT text-extractable**. Same OCR requirement.
- **Stage Range**: Nominally stages 1–350.

### Document C: Normal Fares (Effect from 2026-07-06).pdf — ROUTE-SPECIFIC MATRICES

- **Purpose**: Per-route point-to-point fare matrix for NORMAL service. Each page contains a **triangular fare matrix** showing exact fares between every pair of stops on a specific route.
- **Pages**: 906 (one page per route variant)
- **Format**: Each page contains:
  - Route number (e.g. `මාර්ග අංකය 2/4-3`)
  - A triangular fare matrix where:
    - Column 0: **Fare stage number** (official NTC fare-stage position)
    - Column 1: **Stop name** (Sinhala)
    - Remaining columns: **Exact point-to-point fares** (triangular matrix)
  - A secondary header box showing route endpoints and service type
- **Columns Available**: fare_stage, stop_name_si, point-to-point fares
- **Stage Range**: Varies per route (e.g. Route 01: stages 0–60, longer routes: stages 0–116+)
- **Route Numbers Available**: ~450+ distinct route numbers
- **English Names**: ❌ No — all stop names are in Sinhala
- **Point-to-Point Fares Extractable**: ✅ **YES** — this is the primary exact fare source
- **Extraction Reliability**: ⚠️ **MODERATE** — text extraction works but causes `MemoryError` on full 906-page sequential scan. Requires page-by-page extraction with garbage collection.

### Document D: Semi Fares (Effect From 2026-07-06).pdf — ROUTE-SPECIFIC MATRICES

- **Purpose**: Per-route point-to-point fare matrix for SEMI_LUXURY service.
- **Pages**: 84
- **Format**: Identical triangular matrix structure as Document C.
- **Columns Available**: fare_stage, stop_name_si, point-to-point fares
- **Stage Range**: Varies per route
- **Route Numbers Available**: ~80+ distinct routes
- **English Names**: ❌ No — Sinhala only
- **Point-to-Point Fares Extractable**: ✅ **YES**
- **Extraction Reliability**: ✅ **GOOD** — 84 pages, manageable size

**Sample page structure (Semi Fares, page 41):**
```
Stage | Stop Name (Sinhala)  | Fare Matrix (triangular)
──────┼──────────────────────┼──────────────────────────
  0   | කොළඹ               |
  6   | කිරිබත්ගොඩ        | 143
  8   | ඩවත                | 173   66
 15   | යක්ල               | 255  185  162
 20   | නිට්ටඹුව           | 311  245  221  123
 ...  | ...                 | ...
```

### Document E: Inter Provincial Full Bus Fare.pdf — ENDPOINT FARES

- **Purpose**: Official full-route endpoint-to-endpoint fares for all inter-provincial routes.
- **Pages**: 3
- **Format**: Simple table with columns:
  - Route Number | Origin | Destination | Normal Fare | Semi-Luxury Fare | AC Fare
- **Total Entries**: 450 route-endpoint fare entries (439 unique route numbers)
- **Route-Specific**: Yes — one row per route
- **Point-to-Point**: ❌ **No** — only full-route origin→destination total fare
- **English Names**: ❌ No — Sinhala only
- **Extraction Reliability**: ✅ **HIGH** — small document, clean table extraction

---

## 4. Fare Priority Hierarchy (Corrected)

Based on the document analysis, the fare lookup priority should be:

| Priority | Source | Method | Accuracy |
|----------|--------|--------|----------|
| **1** | Document C/D (Route-specific matrices) | Exact stop-to-stop fare from official triangular matrix | **EXACT** |
| **2** | Documents A/B (Master stage chart) + route stage positions from C/D | Calculate fare = `stage_price[to_stage] - stage_price[from_stage]` | **EXACT** (when stages known) |
| **3** | Document E (Inter Provincial Full Bus Fare) | Full origin→destination fare only | **ENDPOINT_ONLY** |
| **4** | N/A | `FARE_UNAVAILABLE` | N/A |

> **IMPORTANT**: Do NOT infer fare stages from number of stops, kilometres, or timetable sequence.

---

## 5. Master Stage Chart Status

### Extracted from Document C (Normal Fares PDF, page 1)

The **partial** master stage table was extracted from the first page of the route-specific PDF (stages 0–60). Additional stages (61–116) were collected by sampling other route pages. This is **NOT the complete 350-stage table**.

| Metric | Value |
|--------|-------|
| Extracted stages | 115 (stages 0–116, missing 102, 103) |
| Min fare (stage 1) | Rs. 34.00 |
| Max fare (stage 116) | Rs. 997.00 |
| Monotonicity | ✅ Non-decreasing |
| Completeness | ❌ Incomplete — missing stages 102–103 and 117–350 |

### Full 350-Stage Extraction Status

| Document | Extractable? | Status |
|----------|-------------|--------|
| `Normal Sinhala 350.pdf` | ❌ Image/vector only | **NEEDS_OCR** |
| `Semi Luxury Sinhala 350.pdf` | ❌ Image/vector only | **NEEDS_OCR** |
| Route-specific PDFs | ✅ Text (partial) | Stages 0–116 extracted from sampled pages |

> **Action Required**: To extract the full 350-stage master table, OCR (Tesseract with Sinhala support) must be applied to Documents A and B.  
> **However**: For practical fare calculation, the route-specific matrices (Documents C/D) are the preferred source since they provide exact point-to-point fares directly.

---

## 6. Route Matching Analysis: DB Routes ↔ Fare Documents

### Classification Summary

| Classification | Count | Percentage |
|---------------|-------|-----------|
| `EXACT_ROUTE_FARE_DATA_AVAILABLE` (from Doc C/D) | 0* | 0% |
| `FULL_ENDPOINT_FARE_ONLY` (from Doc E) | 28 | 96.6% |
| `NO_FARE_DATA_FOUND` | 1 | 3.4% |
| **Total Routable Routes** | **29** | 100% |

> \* Note: The route-specific PDFs (C/D, 906+84 pages) **do contain our routes** based on the Inter Provincial cross-check, but matching by route number failed because:
> 1. Route numbers in the PDFs use Sinhala text context (e.g. `මාර්ග අංකය 2/4-3`)
> 2. Full 906-page scan causes `MemoryError`
> 3. A page-by-page targeted extraction is needed (deferred to implementation phase)

### Per-Route Fare Classification

| DB Route | Fare Source Match | Normal Fare (LKR) | Semi-Luxury Fare (LKR) | Fare Origin | Fare Destination |
|----------|-------------------|-------------------|----------------------|-------------|-----------------|
| `01` | Doc E | 521.00 | — | කොළඹ | මහනුවර |
| `08` | Doc E | 632.00 | — | කොළඹ | මාතලේ |
| `10` | Doc E | 1,197.00 | 1,796.00 | කතරගම | මහනුවර |
| `1-1` | Doc E | 361.00 | — | කොළඹ | කෑගල්ල |
| `11-1` | Doc E | 413.00 | — | මාතර | ඇඹිලිපිටිය |
| `1-2` | Doc E | 413.00 | — | කොළඹ | මාවනැල්ල |
| `122` | Doc E | 453.00 | — | කොළඹ | රත්නපුර |
| `1-245` | Doc E | 513.00 | — | මීගමුව | මහනුවර |
| `15-1` | Doc E | 989.00 | — | කොළඹ | අනුරාධපුරය |
| `15-87` | Doc E | 1,733.00 | 2,600.00 | කොළඹ | යාපනය |
| `17` | Doc E | 640.00 | 960.00 | පානදුර | මහනුවර |
| `19-2` | Doc E | 555.00 | — | කොළඹ | ගම්පොල |
| `218-2` | Doc E | 406.00 | — | මහියංගනය | කදුරුවෙල |
| `22-2` | Doc E | 913.00 | — | මහනුවර | අම්පාර |
| `2-3` | Doc E | 376.00 | — | කොළඹ | අම්බලන්ගොඩ |
| `35-3` | Doc E | 879.00 | — | මාතර | මොණරාගල |
| `401` | Doc E | 398.00 | — | කොළඹ | ඇල්පිටිය |
| `43-857` | Doc E | 767.00 | 1,151.00 | මහනුවර | වේනියාව |
| `493-1` | Doc E | 261.00 | — | ඇඹිලිපිටිය | බෙලිඅත්ත |
| `5` | Doc E | 413.00 | — | කොළඹ | කුරුණෑගල |
| `57` | Doc E | 896.00 | — | කොළඹ | අනුරාධපුරය |
| `6` | Doc E | 413.00 | — | කොළඹ | කුරුණෑගල |
| `602` | Doc E | 223.00 | — | මහනුවර | කුරුණෑගල |
| `662` | Doc E | 207.00 | — | මහනුවර | කෑගල්ල |
| `662-1` | Doc E | 155.00 | — | මහනුවර | මාවනැල්ල |
| `88-2` | Doc E | 445.00 | 668.00 | වේනියාව | ත්‍රිකුණාමලය |
| `98-2` | Doc E | 1,157.00 | — | කොළඹ | මොණරාගල |
| `98-6` | Doc E | 590.00 | — | කොළඹ | බලංගොඩ |
| `Unknown` | ❌ None | N/A | N/A | — | — |

### Semi-Luxury Fare Coverage

Of 28 matched routes, only **5 routes** have official Semi-Luxury endpoint fares:
- Route `10` (1,796.00), Route `15-87` (2,600.00), Route `17` (960.00), Route `43-857` (1,151.00), Route `88-2` (668.00)

The remaining 23 routes show `--` for Semi-Luxury in Document E.

---

## 7. Stop-Name Matching Analysis

### Endpoint Cross-Check: DB Stops ↔ Fare PDF Origins/Destinations

The fare PDFs use **Sinhala stop names** while our timetable data uses a mix of English, corrupted Sinhala (with `(cid:)` encoding artifacts), and clean Sinhala.

| Match Quality | Count | Examples |
|--------------|-------|---------|
| **Clear Match** (English↔Sinhala equivalent) | ~12 | Kandy↔මහනුවර, Colombo↔කොළඹ, Kegalle↔කෑගල්ල |
| **Corrupted DB Name** (contains `(cid:)`) | ~10 | `(cid:6)ග(cid:8)ව` should be මීගමුව (Negombo) |
| **Structural Mismatch** (DB has header text as stop) | ~5 | `ධාවන අංකය` (= "Running Number" header) |
| **No Fare Match** | 1 | Route `Unknown` |

### Safe Alias Candidates (verified by route context)

| DB Stop Name | Fare PDF Name (Sinhala) | English Equivalent | Safe? |
|-------------|------------------------|-------------------|-------|
| Kandy | මහනුවර | Kandy | ✅ |
| Colombo / කොළඹ | කොළඹ | Colombo | ✅ |
| Kegalle | කෑගල්ල | Kegalle | ✅ |
| Mawanella | මාවනැල්ල | Mawanella | ✅ |
| Panadura | පානදුර | Panadura | ✅ |
| Anuradhapura | අනුරාධපුරය | Anuradhapura | ✅ |
| Trincomalee | ත්‍රිකුණාමලය | Trincomalee | ✅ |

> **Policy**: Do NOT automatically merge or alias mismatched stop names. The above are documented as candidates only.

---

## 8. Fare-Stage Coverage in `route_stops` Table

| Metric | Count |
|--------|-------|
| Total `route_stops` entries | 236 |
| With non-null `fare_stage` | 0 |
| Patterns with complete fare-stage data | 0 / 58 |
| Patterns with partial fare-stage data | 0 / 58 |
| Patterns with NO fare-stage data | 58 / 58 (100%) |

**Root Cause**: Timetable PDFs (Layouts A, B, C) do not publish fare-stage numbers. Fare stages exist **only** in the route-specific fare PDFs (Documents C/D).

---

## 9. NTC Times Web Interface Re-Investigation

### URL: `https://www.ntc.gov.lk/times/`

| Field | Value |
|-------|-------|
| HTTP Status | 200 OK |
| HTML Length | 15,751 bytes |
| Form Action | `index.php` (POST) |
| Form Method | POST |

### Form Controls

| Control | Type | Options |
|---------|------|---------|
| `Origin` | `<select>` | 6 options: Colombo, Kadawatha, Kaduwela, Makubura, Galle, Matara |
| `Destination` | `<select>` | 122 options: Vavuniya, Badulla, Anuradhapura, Trincomalee, etc. |
| `Start_time` | `<select>` | 48 options (00:00–23:30, 30-min increments) |
| `End_time` | `<select>` | 49 options (00:00–24:00, 30-min increments) |
| `trvldate` | `<input>` (datepicker) | Date field |

### Search Test Result

A POST request with `Origin=Colombo, Destination=Kandy, Start_time=00:00:00, End_time=24:00:00` returned **0 table rows** — the same 15,751-byte page (the form itself). This suggests:
1. The search may require JavaScript execution (AJAX-based results), OR
2. The backend search service may be non-functional / not yet connected, OR
3. The form may require specific session cookies or CSRF tokens.

**Conclusion**: The `/times/` search interface exists as a **functional HTML form** with real dropdown data (6 origins, 122 destinations), but the server-side search endpoint does not return results via simple POST. This is **not a blocker** for our fare analysis — we have the official fare PDFs.

---

## 10. Recommended Next Steps

### Immediate (Before Fare Importer)
1. **Extract route-specific fare matrices from Document D** (Semi Fares, 84 pages — manageable size) using page-by-page pdfplumber with memory management.
2. **Map route numbers from Document D pages to our 29 DB routes** using the fare-stage + stop-name context.
3. **For routes found in Document D**: extract the full triangular fare matrix and store as `route_fares`.
4. **For routes only in Document E**: store endpoint fares as `FULL_ENDPOINT_FARE_ONLY`.

### Deferred (Requires OCR Infrastructure)
5. Apply Tesseract OCR with Sinhala language pack to Documents A & B to extract the full 350-stage master tables.
6. Attempt targeted page extraction from Document C (906 pages) for routes not covered by Document D.

### Policy Constraints
- ❌ Do NOT infer fare stages from stop count, distance, or timetable sequence.
- ❌ Do NOT use multipliers to estimate Semi-Luxury from Normal fares.
- ❌ Do NOT automatically merge mismatched stop names.
- ✅ Retain `FARE_UNAVAILABLE` for any route without verified official fare data.
