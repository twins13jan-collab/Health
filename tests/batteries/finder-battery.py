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

s, b = call("GET", "/api/public/bookable-services")
svcs = {x["key"]: x for x in (b.get("services") or [])}
check("bookable-services catalog live with all 9 services", s == 200 and len(svcs) == 9, f"HTTP {s} count={len(svcs)}")
check("radiology files as a radiology portal request; clinics book sessions; emergency is info-only",
      svcs.get("radiology", {}).get("portalType") == "radiology"
      and svcs.get("clinics", {}).get("mode") == "session"
      and svcs.get("emergency", {}).get("mode") == "info", "")

s, b = call("GET", "/api/public/services")
facs = b.get("facilities") or []
check("directory now carries the loaded public hospitals (>= 19 facilities)", len(facs) >= 19, f"count={len(facs)}")
seed = next((f for f in facs if f["id"].startswith("11111111")), {})
check("seed hospital offers the full in-hospital set (radiology + dialysis included)",
      {"clinics", "lab", "pharmacy", "radiology", "dialysis"} <= set(seed.get("services") or []), f"{seed.get('services')}")

s, b = call("GET", "/api/public/services?service=radiology")
rad = b.get("facilities") or []
check("service filter: every radiology result really offers radiology",
      len(rad) >= 3 and all("radiology" in (f.get("services") or []) for f in rad), f"count={len(rad)}")
check("loaded university hospitals appear under radiology in Cairo",
      any("القصر العيني" in (f.get("nameAr") or "") for f in rad), "")

s, b = call("GET", "/api/public/services?governorate=CAI")
cai = b.get("facilities") or []
s2, b2 = call("GET", "/api/public/services?governorate=%D8%A7%D9%84%D9%82%D8%A7%D9%87%D8%B1%D8%A9")
cai2 = b2.get("facilities") or []
check("governorate matches by code AND by Arabic name equally",
      len(cai) >= 3 and len(cai) == len(cai2), f"code={len(cai)} name={len(cai2)}")

s, b = call("GET", "/api/public/services?type=university_hospital")
uni = b.get("facilities") or []
check("type filter: university hospitals only (>= 4 loaded/found)",
      len(uni) >= 4 and all(f.get("type") == "university_hospital" for f in uni), f"count={len(uni)}")

# citizen files the NEW request types against a loaded hospital
s, b = call("POST", "/api/auth/login", {"email": "citizen438677@example.com", "password": "Citizen#2026"})
ct = b.get("access_token")
target = rad[0]["id"] if rad else seed["id"]
s, b = call("POST", "/api/portal/service-requests", {"facilityId": target, "serviceType": "radiology", "details": "أشعة مقطعية على الصدر"}, ct)
check("citizen can file a RADIOLOGY request (new portal type)", s == 200, f"HTTP {s}")
s, b = call("POST", "/api/portal/service-requests", {"facilityId": target, "serviceType": "dialysis", "details": "جلسة غسيل أسبوعية"}, ct)
check("citizen can file a DIALYSIS request (new portal type)", s == 200, f"HTTP {s}")
s, b = call("GET", "/api/portal/my-requests", token=ct)
types = {(r.get("serviceType")) for r in (b.get("requests") or [])}
check("both new request types persist in the citizen's list", {"radiology", "dialysis"} <= types, f"{types}")

fails = [r for r in R if not r[1]]
print(f"TOTAL: {len(R) - len(fails)} / {len(R)}")
