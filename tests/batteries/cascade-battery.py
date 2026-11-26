import json, urllib.request, urllib.error

BASE = "https://khattab-ai.com"
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
types = b.get("facilityTypes") or []
vals = [t["value"] for t in types]
ars = [t["nameAr"] for t in types]
check("taxonomy is now 18 unique types (synonym merged)", len(types) == 18 and len(set(vals)) == 18, f"count={len(types)}")
check("no duplicate Arabic labels remain", len(set(ars)) == len(ars), f"{ars}")
check("'laboratory' no longer offered as a separate type", "laboratory" not in vals and "lab" in vals, "")

s, b = call("GET", "/api/public/services?type=lab")
labs = b.get("facilities") or []
s2, b2 = call("GET", "/api/public/services?type=laboratory")
labs2 = b2.get("facilities") or []
check("type=lab finds the labs; legacy type=laboratory returns the same set",
      len(labs) >= 1 and len(labs) == len(labs2) and all(f["type"] in ("lab", "laboratory") for f in labs),
      f"lab={len(labs)} laboratory={len(labs2)}")

s, b = call("GET", "/api/public/bookable-services")
rad = next((x for x in (b.get("services") or []) if x["key"] == "radiology"), {})
check("radiology service narrows to its 7 offering types",
      set(rad.get("facilityTypes") or []) == {"radiology_center", "hospital", "general_hospital", "university_hospital", "emergency_hospital", "specialized_center", "center"},
      f"{rad.get('facilityTypes')}")

fails = [r for r in R if not r[1]]
print(f"TOTAL: {len(R) - len(fails)} / {len(R)}")
