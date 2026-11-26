import json, urllib.request, urllib.error, random
from datetime import datetime, timedelta, timezone

BASE = "https://khattab-ai.com"
TZ = timezone(timedelta(hours=2))
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

def tok(email, pw):
    s, b = call("POST", "/api/auth/login", {"email": email, "password": pw})
    assert s == 200, (email, s, b)
    return b["access_token"]

R = []
def check(name, ok, extra=""):
    R.append((name, ok)); print(("PASS" if ok else "FAIL"), "|", name, "|", extra)

nurse = tok("nurse@khattab-ai.com", "Nurse#2026")
recep = tok("reception@khattab-ai.com", "Smoke#2026")
pharm = tok("pharmacist@khattab-ai.com", "Smoke#2026")
tag = "".join(random.choice("0123456789") for _ in range(5))
now = datetime.now(TZ)

s, b = call("GET", "/api/porterage/board", token=pharm)
check("pharmacist cannot read the support board (403)", s == 403, f"HTTP {s}")
s, b = call("GET", "/api/porterage/board", token=nurse)
check("nurse reads the support board", s == 200 and "summary" in b, f"HTTP {s}")

# breached routine (requested 60 min ago, target 45) vs fresh critical
s, b = call("POST", "/api/porterage/transports", {"patientRef": f"PAT-R{tag}", "fromLocation": "Ward A", "toLocation": "Radiology", "priority": "routine", "oxygenRequired": False, "requestedAt": (now - timedelta(minutes=60)).isoformat()}, nurse)
check("routine transport created", s == 200, f"HTTP {s}")
s, b = call("POST", "/api/porterage/transports", {"patientRef": f"PAT-C{tag}", "fromLocation": "ER", "toLocation": "ICU", "priority": "critical", "oxygenRequired": True, "requestedAt": now.isoformat()}, nurse)
crit_id = (b.get("transport") or {}).get("id")
check("critical oxygen transport created (escort mandatory)", s == 200 and b.get("escort_mandatory") is True, f"HTTP {s}")
s, b = call("GET", "/api/porterage/board", token=nurse)
rows = b.get("transports") or []
mine = [r for r in rows if r.get("patient_ref", "").endswith(tag)]
check("breached routine outranks fresh critical on the board",
      len(mine) == 2 and mine[0]["patient_ref"].startswith("PAT-R") and mine[0]["breached"] is True and mine[1]["patient_ref"].startswith("PAT-C"),
      f"order={[r.get('patient_ref') for r in mine]}")
check("summary counts the breach + critical", (b.get("summary") or {}).get("transports_breached", 0) >= 1 and (b.get("summary") or {}).get("transports_critical_open", 0) >= 1, "")

s, b = call("POST", f"/api/porterage/transports/{crit_id}/assign", {"porter": "حسن", "escortName": None}, nurse)
check("critical transport cannot move without a named escort (409)", s == 409, f"HTTP {s}")
s, b = call("POST", f"/api/porterage/transports/{crit_id}/assign", {"porter": "حسن", "escortName": "ممرضة سارة"}, nurse)
check("assign with escort works", s == 200, f"HTTP {s}")
s, b = call("POST", f"/api/porterage/transports/{crit_id}/pickup", {}, nurse)
check("pickup after assign", s == 200, f"HTTP {s}")
s, b = call("POST", f"/api/porterage/transports/{crit_id}/deliver", {"deliveredAt": datetime.now(TZ).isoformat()}, nurse)
check("deliver completes with response measured", s == 200 and b.get("response_breached") is False, f"HTTP {s} breached={b.get('response_breached')}")

# room turnover: discharged 3h ago -> overdue; partial progress stays; full progress readies
s, b = call("POST", "/api/porterage/rooms", {"roomRef": f"R-{tag}", "dischargedAt": (now - timedelta(hours=3)).isoformat()}, nurse)
room_id = (b.get("turnover") or {}).get("id")
check("room turnover created", s == 200, f"HTTP {s}")
s, b = call("GET", "/api/porterage/board", token=nurse)
rm = next((r for r in (b.get("rooms") or []) if r.get("room_ref") == f"R-{tag}"), {})
check("room flagged overdue after 3 hours", rm.get("overdue") is True, f"{rm}")
s, b = call("POST", f"/api/porterage/rooms/{room_id}/progress", {"cleaningDone": True, "bedReady": False, "inspectedBy": None}, nurse)
check("cleaning alone does not ready the room", s == 200 and b.get("room_ready") is False, f"HTTP {s}")
s, b = call("POST", f"/api/porterage/rooms/{room_id}/progress", {"cleaningDone": True, "bedReady": True, "inspectedBy": "مفتش أحمد"}, nurse)
check("cleaning + bed + signed inspection readies the room", s == 200 and b.get("room_ready") is True, f"HTTP {s}")
s, b = call("GET", "/api/porterage/board", token=nurse)
check("ready room leaves the pending queue; ready_today counted",
      all(r.get("room_ref") != f"R-{tag}" for r in (b.get("rooms") or [])) and (b.get("summary") or {}).get("rooms_ready_today", 0) >= 1, "")

# laundry: over-return refused; loss counted openly
s, b = call("POST", "/api/porterage/laundry", {"batchRef": f"L-{tag}", "batchType": "linen", "sentCount": 10}, nurse)
lb_id = (b.get("batch") or {}).get("id")
check("laundry batch sent", s == 200, f"HTTP {s}")
s, b = call("POST", f"/api/porterage/laundry/{lb_id}/receive", {"returnedCount": 11}, nurse)
check("over-return refused (400)", s == 400, f"HTTP {s}")
s, b = call("POST", f"/api/porterage/laundry/{lb_id}/receive", {"returnedCount": 8}, nurse)
check("loss of 2 counted and flagged", s == 200 and b.get("loss_count") == 2 and b.get("loss_flagged") is True, f"HTTP {s} loss={b.get('loss_count')}")
s, b = call("GET", "/api/porterage/board", token=nurse)
lb = next((x for x in (b.get("laundry") or []) if x.get("batch_ref") == f"L-{tag}"), {})
check("board shows the batch as received_with_loss", lb.get("state") == "received_with_loss", f"{lb.get('state')}")

# patient service request lifecycle
s, b = call("POST", "/api/porterage/service-requests", {"patientRef": f"PAT-R{tag}", "requestType": "meal", "detail": "وجبة غداء"}, recep)
sr_id = (b.get("service_request") or {}).get("id")
check("receptionist opens a patient service request", s == 200, f"HTTP {s}")
s, b = call("POST", f"/api/porterage/service-requests/{sr_id}/fulfill", {}, nurse)
check("request fulfilled", s == 200, f"HTTP {s}")

fails = [r for r in R if not r[1]]
print(f"TOTAL: {len(R) - len(fails)} / {len(R)}")
