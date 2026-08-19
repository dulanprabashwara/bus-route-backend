# NTC Web Source Analysis Report

## Executive Summary
This document analyzes the feasibility of recovering missing bus timetable data directly from the official National Transport Commission (NTC) website (`https://www.ntc.gov.lk/Bus_info/time_table.php` and `https://www.ntc.gov.lk/times/`) as an alternative to PDF parsing and OCR/manual transcription.

---

## 1. Web Architecture Investigation

### Page Inspection Results
- **URL**: `https://www.ntc.gov.lk/Bus_info/time_table.php`
  - **Structure**: Static HTML page (`24KB`) built with Bootstrap and custom styling.
  - **Dynamic Elements**: 0 `<form>` elements, 0 `<select>` dropdowns, 0 AJAX/fetch endpoints.
  - **Content**: Contains static HTML tables wrapping embedded hyperlinks (`<a href="...">`) to downloadable PDF files stored on the web server (under `/timetable2023/HighWay/`, `/timetable/2019/`, etc.).
- **URL**: `https://www.ntc.gov.lk/times/`
  - **Structure**: Basic HTML index page (`15KB`).
  - **Dynamic Elements**: No API or form search endpoints found.

### Technical Assessment
| Data Retrieval Method | Available on NTC Site? | Details |
| :--- | :---: | :--- |
| **HTML Tabular Timetables** | ❌ No | Schedules are not rendered inline in HTML tables; only PDF download links are listed. |
| **GET / POST Forms** | ❌ No | No search filters or form submit handlers exist. |
| **AJAX / Fetch Endpoints** | ❌ No | No client-side dynamic queries or XHR requests are performed. |
| **JSON / REST / GraphQL APIs** | ❌ No | No public or hidden JSON endpoints available. |
| **Server-Rendered Search Results** | ❌ No | Search capabilities do not exist on the timetable pages. |

---

## 2. Comparison: Web vs. Local PDF Dataset

1. **Source Identity**: The PDF files hosted on the NTC web server are identical in content, naming, and layout to the 49 local PDF files currently in the dataset.
2. **Missing PDF Recovery**: The 18 PDFs classified with **"No tables found"** in the ingestion pipeline correspond directly to the downloadable PDFs linked on the official website.
3. **Conclusion on Recovery**: Because the web source is merely a static file directory listing for the same PDF files, **the missing route data cannot be obtained from HTML/API sources on the NTC website**.

---

3. **Findings & Recommendations**

### Summary of Findings
1. **Route Patterns & Database Fix**:
   - **Authoritative Database State**: Verified directly via PostgreSQL query. The database contains **30 routes**, **60 route patterns** (58 routable with valid sequences), **1,558 trips** (1,558 routable), **4,898 stop times** (4,000 valid, 898 quarantined), and **134 canonical stops**.
   - **Route Stops**: Pipeline populates `route_stops` for each pattern upon import.

2. **Data Safety & Quarantine**:
   - **Suspicious Times**: Modulo-based automatic time correction disabled. Invalid time formats (e.g. `93:00`, `99:95`) are retained as `raw_time_string` for provenance with `time_accuracy = 'INVALID'`. They are excluded from journey search indexing via `v_routable_stop_times`.
   - **Corrupted Text (`(cid:XX`)**: Strings with font decoding corruption are preserved in `raw_corrupted_name` in `stops`, with `name_en` falling back to `Unknown Stop (Corrupted)` until normalized. Excluded from journey search indexing via `v_routable_stops`.
   - **Status of 18 Failed PDFs**: Updated in `source_files` table to `NEEDS_OCR_OR_MANUAL_REVIEW`.

### Authoritative Database Summary Table
| Metric | Total Database Records | Routable / Trusted Data | Quarantined / Excluded Data |
| :--- | :---: | :---: | :---: |
| **Routes** | 30 | 29 | 1 |
| **Route Patterns** | 60 | 58 | 2 |
| **Trips** | 1,558 | 1,558 | 0 |
| **Stop Times** | 4,898 | 4,000 | 898 |
| **Stops** | 134 | 134 | 0 |
| **Unparseable Files** | 49 total | 31 Succeeded | 18 Marked `NEEDS_OCR_OR_MANUAL_REVIEW` |

3. **Recommended Primary Data Source**:
   - **Primary Source**: Standardized layout PDFs + PostgreSQL database routines/views (`v_routable_*`).
   - **Secondary/Web Source**: The NTC site serves strictly as a static host for PDF file downloads, not dynamic API data.
   - **18 Unparseable Files**: OCR preprocessing or manual CSV transcription will be required when scheduled.

