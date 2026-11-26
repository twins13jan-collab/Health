import json, time, urllib.request, urllib.error
from datetime import date, timedelta

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
mgr = login("mgr438396@example.com", "Manager#2026")
nurse = login("nurse@khattab-ai.com", "Nurse#2026")
check("facility manager session", bool(mgr), "")

s, _ = call("GET", "/api/hr-deep/board", token=nurse)
check("nurse cannot read the HR board (403)", s == 403, f"HTTP {s}")
s, b = call("GET", "/api/hr-deep/board", token=mgr)
check("facility manager reads the HR board", s == 200, f"HTTP {s}")

s, b = call("POST", "/api/hr-deep/employees", {"staffRef": f"DR-{tag}", "role": "physician", "clinical": True, "licenceValid": False}, mgr)
check("clinical employee with lapsed licence is NOT assignable", s == 200 and b.get("assignable") is False, f"HTTP {s}")
s, b = call("POST", "/api/hr-deep/employees", {"staffRef": f"NR-{tag}", "role": "nurse", "clinical": True, "licenceValid": True}, mgr)
check("licensed clinician is assignable", s == 200 and b.get("assignable") is True, f"HTTP {s}")

past = (date.today() - timedelta(days=10)).isoformat()
future = (date.today() + timedelta(days=10)).isoformat()
s, b = call("POST", "/api/hr-deep/trainings", {"staffRef": f"NR-{tag}", "course": "IPC basics", "mandatory": True, "completed": False, "dueDate": past}, mgr)
check("overdue mandatory training is non-compliant", s == 200 and b.get("compliant") is False, f"HTTP {s}")
s, b = call("POST", "/api/hr-deep/trainings", {"staffRef": f"NR-{tag}", "course": "Fire safety", "mandatory": True, "completed": False, "dueDate": future}, mgr)
check("future mandatory training is compliant (not yet due)", s == 200 and b.get("compliant") is True, f"HTTP {s}")

s, b = call("GET", "/api/hr-deep/board", token=mgr)
staff = {r["staff_ref"]: r for r in (b.get("staff") or [])}
check("lapsed-licence doctor tops the staff queue (licence_blocked, rank 0)",
      staff.get(f"DR-{tag}", {}).get("state") == "licence_blocked" and (b.get("staff") or [{}])[0].get("staff_ref") == f"DR-{tag}",
      f"{staff.get(f'DR-{tag}', {})}")
check("nurse flagged training_overdue with 1 overdue course",
      staff.get(f"NR-{tag}", {}).get("state") == "training_overdue" and staff.get(f"NR-{tag}", {}).get("overdue_trainings") == 1, "")
tr = {r["course"]: r for r in (b.get("trainings") or [])}
check("training queue: IPC overdue, Fire due_soon", tr.get("IPC basics", {}).get("state") == "overdue" and tr.get("Fire safety", {}).get("state") == "due_soon", "")

s, b = call("POST", "/api/hr-deep/grievances", {"staffRef": f"NR-{tag}", "subject": f"Shift dispute {tag}", "reviewed": False, "outcome": None}, mgr)
g = (b.get("grievance") or {}).get("id")
s, _ = call("POST", f"/api/hr-deep/grievances/{g}/close", {}, mgr)
check("unreviewed grievance cannot close (409)", s == 409, f"HTTP {s}")
s, _ = call("POST", f"/api/hr-deep/grievances/{g}/review", {"outcome": ""}, mgr)
check("review without an outcome is refused (400)", s == 400, f"HTTP {s}")
s, b = call("POST", f"/api/hr-deep/grievances/{g}/review", {"outcome": "schedule corrected"}, mgr)
check("review records the outcome and makes it closable", s == 200 and b.get("closable") is True, f"HTTP {s}")
s, _ = call("POST", f"/api/hr-deep/grievances/{g}/close", {}, mgr)
check("reviewed grievance closes", s == 200, f"HTTP {s}")
s, b = call("GET", "/api/hr-deep/board", token=mgr)
gids = [r["id"] for r in (b.get("grievances") or [])]
check("closed grievance leaves the queue", g not in gids, "")
sm = b.get("summary") or {}
check("summary counts licence blocks and overdue trainings", sm.get("licence_blocked", 0) >= 1 and sm.get("trainings_overdue", 0) >= 1, f"{sm}")

fails = [r for r in R if not r[1]]
print(f"TOTAL: {len(R) - len(fails)} / {len(R)}")
