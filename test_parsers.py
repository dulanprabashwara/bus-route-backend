import os
import pdfplumber

DATA_DIR = r"D:\Bus route\bus-route-backend\Normal - Semi Luxury"
LAYOUT_B = "01 Kandy - Colombo Normal Panal.pdf"
LAYOUT_C = "10 Kataragama - Kandy  (10) New Imp 2025.08.12.pdf"

print("----- LAYOUT B TEST -----")
with pdfplumber.open(os.path.join(DATA_DIR, LAYOUT_B)) as pdf:
    for i, page in enumerate(pdf.pages[:1]):
        tables = page.extract_tables()
        for t_idx, table in enumerate(tables):
            print(f"Page {i+1}, Table {t_idx+1}, Columns: {len(table[0]) if table else 0}")
            for row in table[:10]:
                print(row)

print("\n----- LAYOUT C TEST -----")
with pdfplumber.open(os.path.join(DATA_DIR, LAYOUT_C)) as pdf:
    for i, page in enumerate(pdf.pages[:1]):
        tables = page.extract_tables()
        for t_idx, table in enumerate(tables):
            print(f"Page {i+1}, Table {t_idx+1}, Columns: {len(table[0]) if table else 0}")
            for row in table[:10]:
                print(row)
