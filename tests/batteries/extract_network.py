import pymupdf, json, re
from collections import Counter

PDF = "/home/hatch/workspace/user/files/Arabic-Network-update-in-August-2020.pdf"

# column x-ranges (from header calibration on page 1)
COLS = [
    ("phone", 0, 285), ("address", 285, 470), ("specialty", 470, 640),
    ("name", 640, 800), ("ptype", 800, 915), ("card", 915, 1015),
    ("city", 1015, 1113), ("gov", 1113, 1300),
]
def col_of(xc):
    for name, lo, hi in COLS:
        if lo <= xc < hi: return name
    return None

doc = pymupdf.open(PDF)
rows = []
for pno in range(doc.page_count):
    page = doc[pno]
    words = page.get_text("words")
    # bands: cluster words by rounded y0
    bands = {}
    for w in words:
        x0, y0, x1, y1, text = w[0], w[1], w[2], w[3], w[4]
        if y0 < 132:  # page title + header zone
            continue
        key = round(y0 / 4.0)
        bands.setdefault(key, []).append(w)
    band_list = sorted(bands.items())  # by y
    # anchors: bands containing a gov-column word
    def band_cells(ws):
        cells = {}
        for w in ws:
            c = col_of((w[0] + w[2]) / 2)
            if c: cells.setdefault(c, []).append(w)
        return cells
    anchors = []
    for i, (k, ws) in enumerate(band_list):
        cells = band_cells(ws)
        if "gov" in cells:
            anchors.append(i)
    if not anchors:
        continue
    # assign each band to nearest anchor band index
    def nearest_anchor(i):
        return min(anchors, key=lambda a: abs(band_list[a][0] - band_list[i][0]))
    grouped = {a: [] for a in anchors}
    for i in range(len(band_list)):
        grouped[nearest_anchor(i)].append(band_list[i][1])
    for a in anchors:
        row = {}
        # process bands in y order; within a cell, words right-to-left
        cell_words = {}
        for ws in grouped[a]:
            for w in ws:
                c = col_of((w[0] + w[2]) / 2)
                if c: cell_words.setdefault(c, []).append(w)
        for c, ws in cell_words.items():
            ws_sorted = sorted(ws, key=lambda w: (round(w[1] / 4.0), -w[0]))
            row[c] = " ".join(w[4] for w in ws_sorted).strip()
        if row.get("gov") and row.get("name"):
            rows.append(row)

print("total rows:", len(rows))
print("provider types:", Counter(r.get("ptype", "") for r in rows).most_common())
print("governorates:", Counter(r.get("gov", "") for r in rows).most_common())
with open("/home/hatch/workspace/network_rows.json", "w") as f:
    json.dump(rows, f, ensure_ascii=False)
print("saved network_rows.json")
