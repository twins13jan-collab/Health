import json, time, urllib.request, urllib.error

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

def login(email, pw):
    s, b = call("POST", "/api/auth/login", {"email": email, "password": pw})
    return b.get("access_token")

R = []
def check(name, ok, extra=""):
    R.append((name, ok)); print(("PASS" if ok else "FAIL"), "|", name, "|", extra)

tag = str(int(time.time()))[-6:]
bio = login("biomed@khattab-ai.com", "Smoke#2026")
nurse = login("nurse@khattab-ai.com", "Nurse#2026")
pharm = login("pharmacist@khattab-ai.com", "Smoke#2026")

s, _ = call("GET", "/api/medical-assets/board", token=pharm)
check("pharmacist cannot read the biomed board (403)", s == 403, f"HTTP {s}")
s, b = call("GET", "/api/medical-assets/board", token=bio)
check("biomedical engineer reads the board", s == 200, f"HTTP {s}")

import datetime
today = datetime.date.today()
yday = (today - datetime.timedelta(days=1)).isoformat()
soon = (today + datetime.timedelta(days=10)).isoformat()
later = (today + datetime.timedelta(days=90)).isoformat()
s, b = call("POST", "/api/medical-assets", {"assetCode": f"BIO-A-{tag}", "type": "ventilator", "manufacturer": "Acme", "model": "V9", "serialNumber": f"SN-A-{tag}", "location": "ICU-1", "owner": "biomed", "calibrationDue": yday}, bio)
a_id = (b.get("asset") or {}).get("id")
check("asset A registered with overdue calibration", s == 200 and bool(a_id), f"HTTP {s}")
s, b = call("POST", "/api/medical-assets", {"assetCode": f"BIO-B-{tag}", "type": "monitor", "manufacturer": "Acme", "model": "M2", "serialNumber": f"SN-B-{tag}", "location": "ER-2", "owner": "biomed", "calibrationDue": soon}, bio)
b_id = (b.get("asset") or {}).get("id")
check("asset B registered (calibration due soon)", s == 200 and bool(b_id), f"HTTP {s}")
s, b = call("POST", "/api/medical-assets", {"assetCode": f"BIO-C-{tag}", "type": "pump", "manufacturer": "Acme", "model": "P1", "serialNumber": f"SN-C-{tag}", "location": "Ward-3", "owner": "biomed", "calibrationDue": later}, bio)
c_id = (b.get("asset") or {}).get("id")
s, b = call("POST", f"/api/medical-assets/{c_id}/maintenance-plans", {"planType": "preventive", "intervalDays": 90, "nextDue": yday}, bio)
check("asset C gets an overdue preventive plan", s == 200, f"HTTP {s}")

s, b = call("GET", "/api/medical-assets/board", token=bio)
rows = {r["asset_code"]: r for r in (b.get("assets") or [])}
order = [r["asset_code"] for r in (b.get("assets") or []) if r["asset_code"].endswith(tag)]
check("board ranks calibration-overdue (A) before due-soon (B) before PM-overdue (C)",
      order == [f"BIO-A-{tag}", f"BIO-B-{tag}", f"BIO-C-{tag}"], f"order={order}")
check("states computed honestly (A overdue, B due_soon, C maintenance overdue)",
      rows.get(f"BIO-A-{tag}", {}).get("calibration_state") == "overdue"
      and rows.get(f"BIO-B-{tag}", {}).get("calibration_state") == "due_soon"
      and rows.get(f"BIO-C-{tag}", {}).get("maintenance_state") == "overdue", "")
check("summary counts the overdue calibration", (b.get("summary") or {}).get("calibration_overdue", 0) >= 1, "")

s, b = call("GET", f"/api/medical-assets/{a_id}/usability", token=bio)
check("out-of-calibration device is UNFIT even in service", s == 200 and b.get("usable") is False, f"{b.get('flag')}")

s, _ = call("POST", "/api/medical-assets", {"assetCode": f"BIO-X-{tag}", "type": "x", "location": "L", "owner": "o"}, nurse)
check("nurse still cannot register assets (403)", s == 403, f"HTTP {s}")
s, _ = call("POST", "/api/device-incidents", {"assetId": b_id, "description": "alarm failed", "patientAffected": True, "safetyCaseRef": ""}, nurse)
check("patient-affecting fault without a safety case is refused (400)", s == 400, f"HTTP {s}")
s, b = call("POST", "/api/device-incidents", {"assetId": b_id, "description": "alarm failed during use", "patientAffected": True, "safetyCaseRef": f"SAFE-{tag}"}, nurse)
check("with a safety case the incident records and takes the device out", s == 200 and b.get("asset_status") == "out_of_service", f"HTTP {s}")
s, b = call("GET", "/api/medical-assets/board", token=bio)
first = (b.get("assets") or [{}])[0]
check("patient-affecting incident now tops the board", first.get("asset_code") == f"BIO-B-{tag}" and first.get("rank") == 0, f"first={first.get('asset_code')} rank={first.get('rank')}")

s, b = call("POST", f"/api/medical-assets/{a_id}/work-orders", {"type": "corrective", "technician": "tech-1"}, bio)
wo_id = (b.get("work_order") or {}).get("id")
check("work order opens and takes A under maintenance", s == 200 and bool(wo_id), f"HTTP {s}")
s, _ = call("POST", f"/api/medical-assets/work-orders/{wo_id}/complete", {"spareParts": "valve", "sparePartShortage": True, "sparePurchaseRef": "", "downtimeHours": 5, "result": "replaced"}, bio)
check("shortage without a purchase ref is refused (400)", s == 400, f"HTTP {s}")
s, b = call("POST", f"/api/medical-assets/work-orders/{wo_id}/complete", {"spareParts": "valve", "sparePartShortage": True, "sparePurchaseRef": f"PO-{tag}", "downtimeHours": 5, "result": "replaced"}, bio)
check("completion with the ref creates the purchase request", s == 200 and b.get("purchase_request_created") is True, f"HTTP {s}")
s, _ = call("POST", f"/api/medical-assets/work-orders/{wo_id}/release", {}, nurse)
check("the clinical side cannot release a device (403)", s == 403, f"HTTP {s}")
s, b = call("POST", f"/api/medical-assets/work-orders/{wo_id}/release", {}, bio)
check("biomedical release returns the device to service", s == 200 and b.get("asset_status") == "in_service", f"HTTP {s}")

s, b = call("POST", f"/api/medical-assets/{a_id}/calibrations", {"referenceStandard": "STD-1", "certificateRef": f"CERT-{tag}", "deviation": "0.1%", "accepted": True, "dueDate": later}, bio)
check("accepted calibration renews the due date", s == 200, f"HTTP {s}")
s, b = call("GET", f"/api/medical-assets/{a_id}/usability", token=bio)
check("device is fit again after release + calibration", s == 200 and b.get("usable") is True, "")
s, b = call("GET", "/api/medical-assets/board", token=bio)
codes = [r["asset_code"] for r in (b.get("assets") or [])]
check("healthy asset A leaves the attention queue", f"BIO-A-{tag}" not in codes, f"queue has {len(codes)}")

fails = [r for r in R if not r[1]]
print(f"TOTAL: {len(R) - len(fails)} / {len(R)}")
