import pdfplumber
import json, re, time, sys

sys.stdout.reconfigure(encoding='utf-8')

print("Building Document C page index...")
t0 = time.time()
route_index = {}

with pdfplumber.open("fare_sources/Normal Fares (Effect from 2026-07-06).pdf") as pdf:
    for p_idx, page in enumerate(pdf.pages):
        text = page.extract_text(layout=False) or ""
        for l in text.split("\n")[:5]:  # Check first 5 lines only!
            m = re.search(r'මාර්ග\s*අං\s*(?:ය|කය)\s*([\d\-\/A-Za-z]+)', l)
            if not m:
                m = re.search(r'අංකය\s*:\s*([\d\-\/A-Za-z]+)', l)
            if m:
                r_num = m.group(1).strip().replace(" ", "")
                if r_num not in route_index:
                    route_index[r_num] = []
                route_index[r_num].append(p_idx)
                break

with open("scratch_doc_c_index.json", "w", encoding="utf-8") as f:
    json.dump(route_index, f, indent=2)

print(f"Indexed {len(route_index)} route numbers across 906 pages in {time.time()-t0:.2f}s!")
print("Saved scratch_doc_c_index.json")
