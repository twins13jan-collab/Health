import json, urllib.request, urllib.error

BASE = "https://khattab-ai.com"
FAC = "11111111-1111-1111-1111-111111111111"
def call(method, path, body=None, token=None):
    req = urllib.request.Request(BASE + path, method=method,
        data=json.dumps(body).encode() if body is not None else None,
        headers={"Content-Type": "application/json", **({"Authorization": "Bearer " + token} if token else {})})
    try:
        with urllib.request.urlopen(req, timeout=30) as r:
            return r.status, json.loads(r.read().decode() or "{}")
    except urllib.error.HTTPError as e:
        try: return e.code, json.loads(e.read().decode() or "{}")
        except Exception: return e.code, {}

R = []
def check(name, ok, extra=""):
    R.append((name, ok)); print(("PASS" if ok else "FAIL"), "|", name, "|", extra)

s, b = call("GET", "/api/public/hierarchy")
govs = b.get("governorates") or []
types = b.get("facilityTypes") or []
check("hierarchy endpoint live", s == 200, f"HTTP {s}")
check("ALL 27 governorates present", len(govs) == 27, f"count={len(govs)}")
check("every governorate has its directorate named after it",
      all((g.get("directorateAr") or "").startswith("مديرية صحة ") and g.get("nameAr") in (g.get("directorateAr") or "") for g in govs),
      "")
cai = next((g for g in govs if g.get("code") == "CAI"), {})
check("Cairo directorate is مديرية صحة القاهرة", cai.get("directorateAr") == "مديرية صحة القاهرة", f"{cai.get('directorateAr')}")
check("complete facility taxonomy (19 types)", len(types) == 19, f"count={len(types)}")
tvals = {t.get("value") for t in types}
check("taxonomy includes radiology_center + ambulance_station + mobile_clinic + health_center",
      {"radiology_center", "ambulance_station", "mobile_clinic", "health_center"} <= tvals, "")

s, b = call("POST", "/api/auth/login", {"email": "governorate@khattab-ai.com", "password": "Smoke#2026", "facilityId": FAC})
check("governorate admin (CAI) now enters under the Cairo seed facility (name==code)", s == 200, f"HTTP {s}")
s, b = call("POST", "/api/auth/login", {"email": "governorate@khattab-ai.com", "password": "Smoke#2026"})
govt = b.get("access_token")
s, b = call("GET", "/api/admin/private-sector/board", token=govt)
check("governorate board still scoped (200)", s == 200, f"HTTP {s}")

fails = [r for r in R if not r[1]]
print(f"TOTAL: {len(R) - len(fails)} / {len(R)}")
