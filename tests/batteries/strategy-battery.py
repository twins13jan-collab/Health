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

def login(email, pw):
    s, b = call("POST", "/api/auth/login", {"email": email, "password": pw})
    return b.get("access_token")

R = []
def check(name, ok, extra=""):
    R.append((name, ok)); print(("PASS" if ok else "FAIL"), "|", name, "|", extra)

s, b = call("GET", "/api/public/strategy")
check("public strategy endpoint answers anonymously", s == 200, f"HTTP {s}")
prs = b.get("priorities") or []
check("exactly 7 priorities", len(prs) == 7, f"{len(prs)}")
total_goals = sum(len(p.get("goals") or []) for p in prs)
check("exactly 44 goals across the priorities", total_goals == 44, f"{total_goals}")
check("vision and mission present", bool(b.get("vision")) and bool(b.get("mission")), "")
check("9 guiding principles", len(b.get("principles") or []) == 9, f"{len(b.get('principles') or [])}")
check("every priority maps to real platform capabilities", all(len(p.get("capabilities") or []) >= 3 for p in prs), "")

anon_s, _ = call("GET", "/api/strategy/board")
check("board requires sign-in (not public)", anon_s in (401, 403), f"HTTP {anon_s}")
nat = login("national@khattab-ai.com", "Smoke#2026")
s, b = call("GET", "/api/strategy/board", token=nat)
check("national oversight reads the strategy board", s == 200, f"HTTP {s}")
inds = {i["key"]: i["value"] for p in (b.get("priorities") or []) for i in (p.get("indicators") or [])}
for key in ["facilities_active", "governorates_covered", "patients_registered", "encounters_total",
            "ambulances_available", "audit_events_count", "matrix_domains", "portal_patients", "complaints_total"]:
    check(f"indicator {key} computes from live data", inds.get(key) is not None, f"{key}={inds.get(key)}")
check("matrix domains = 173 after V107+V108", inds.get("matrix_domains") == 173, f"{inds.get('matrix_domains')}")
check("patient share % is a sane number when present", inds.get("patient_share_pct") is None or 0 <= inds["patient_share_pct"] <= 100, f"{inds.get('patient_share_pct')}")

fails = [r for r in R if not r[1]]
print(f"TOTAL: {len(R) - len(fails)} / {len(R)}")
