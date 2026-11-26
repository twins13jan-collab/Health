import json, time, uuid, urllib.request, urllib.error

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
fin = login("finance@khattab-ai.com", "Smoke#2026")
nurse = login("nurse@khattab-ai.com", "Nurse#2026")
rec = login("reception@khattab-ai.com", "Smoke#2026")
s0, b0 = call("GET", "/api/patients?size=1", token=rec)
rows0 = b0.get("patients") or b0.get("items") or []
pat = rows0[0]["id"] if rows0 else None
print("real patient:", pat)

s, _ = call("GET", "/api/billing/board", token=nurse)
check("nurse cannot read the finance board (403)", s == 403, f"HTTP {s}")
s, b = call("GET", "/api/billing/board", token=fin)
check("finance manager reads the finance board", s == 200, f"HTTP {s}")

s, b = call("POST", "/api/billing/invoices", {"patientId": pat, "payer": f"HIO-{tag}"}, fin)
inv = (b.get("invoice") or {}).get("id")
check("invoice created as draft", s == 200 and bool(inv), f"HTTP {s}")
s, b = call("POST", f"/api/billing/invoices/{inv}/lines", {"description": "ICU day", "unitPrice": 1000, "quantity": 1, "coveragePct": 80}, fin)
tot = (b.get("invoice") or {})
check("server computes totals + shares (1000 = 800 insurer + 200 patient)",
      float(tot.get("totalAmount", 0)) == 1000 and float(tot.get("insurerAmount", 0)) == 800 and float(tot.get("patientAmount", 0)) == 200,
      f"{tot.get('totalAmount')}/{tot.get('insurerAmount')}/{tot.get('patientAmount')}")
s, _ = call("POST", f"/api/billing/invoices/{inv}/status", {"status": "paid"}, fin)
check("illegal jump draft->paid refused (409)", s == 409, f"HTTP {s}")
s, _ = call("POST", f"/api/billing/invoices/{inv}/status", {"status": "issued"}, fin)
check("draft->issued allowed", s == 200, f"HTTP {s}")
s, _ = call("POST", f"/api/billing/invoices/{inv}/lines", {"description": "late", "unitPrice": 5, "quantity": 1, "coveragePct": 0}, fin)
check("issued invoice is locked against new lines (409)", s == 409, f"HTTP {s}")
s, _ = call("POST", f"/api/billing/invoices/{inv}/status", {"status": "claimed"}, fin)
check("issued->claimed allowed", s == 200, f"HTTP {s}")
s, _ = call("POST", f"/api/billing/invoices/{inv}/status", {"status": "rejected"}, fin)
check("claimed->rejected allowed", s == 200, f"HTTP {s}")
s, b = call("GET", "/api/billing/board", token=fin)
first = (b.get("invoices") or [{}])[0]
check("rejected invoice tops the finance queue", first.get("id") == inv and first.get("rank") == 0, f"rank={first.get('rank')}")
check("summary counts the rejection", (b.get("summary") or {}).get("rejected", 0) >= 1, "")

s, _ = call("GET", "/api/insurance-deep/board", token=nurse)
check("nurse cannot read the claims board (403)", s == 403, f"HTTP {s}")
s, b = call("GET", "/api/insurance-deep/board", token=fin)
check("finance manager reads the claims board", s == 200, f"HTTP {s}")

s, b = call("POST", "/api/insurance-deep/claims", {"patientRef": f"P-{tag}", "serviceRef": "SVC-X", "documentationComplete": False, "tariffMatch": True, "medicalNecessity": True, "fraudFlag": False}, fin)
cx = (b.get("claim") or {}).get("id")
s, _ = call("POST", f"/api/insurance-deep/claims/{cx}/decide", {"decision": "approved"}, fin)
check("undocumented claim cannot be adjudicated (409)", s == 409, f"HTTP {s}")
s, b = call("GET", "/api/insurance-deep/board", token=fin)
rowx = next((r for r in (b.get("claims") or []) if r["id"] == cx), {})
check("board names the gap (documentation_incomplete)", rowx.get("state") == "not_adjudicable" and "documentation_incomplete" in (rowx.get("gaps") or []), f"{rowx.get('gaps')}")

s, b = call("POST", "/api/insurance-deep/claims", {"patientRef": f"P2-{tag}", "serviceRef": "SVC-Y", "documentationComplete": True, "tariffMatch": True, "medicalNecessity": True, "fraudFlag": True}, fin)
cy = (b.get("claim") or {}).get("id")
s, _ = call("POST", f"/api/insurance-deep/claims/{cy}/settle", {}, fin)
check("fraud-flagged claim cannot settle before human review (409)", s == 409, f"HTTP {s}")
s, b = call("GET", "/api/insurance-deep/board", token=fin)
first = (b.get("claims") or [{}])[0]
check("unreviewed fraud tops the claims queue", first.get("id") == cy and first.get("state") == "fraud_review_pending", f"{first.get('state')}")
s, _ = call("POST", f"/api/insurance-deep/claims/{cy}/review-fraud", {}, fin)
check("human fraud review recorded", s == 200, f"HTTP {s}")
s, b = call("POST", f"/api/insurance-deep/claims/{cy}/decide", {"decision": "approved"}, fin)
check("after review the claim adjudicates; necessity stays recorded", s == 200 and b.get("medical_necessity_recorded") is True, f"HTTP {s}")
s, _ = call("POST", f"/api/insurance-deep/claims/{cy}/settle", {}, fin)
check("reviewed + decided claim settles", s == 200, f"HTTP {s}")
s, b = call("GET", "/api/insurance-deep/board", token=fin)
ids = [r["id"] for r in (b.get("claims") or [])]
check("settled claim leaves the queue", cy not in ids, "")

s, b = call("POST", "/api/insurance-deep/claims", {"patientRef": f"P3-{tag}", "serviceRef": "SVC-Z", "documentationComplete": True, "tariffMatch": True, "medicalNecessity": True, "fraudFlag": False}, fin)
cz = (b.get("claim") or {}).get("id")
s, _ = call("POST", f"/api/insurance-deep/claims/{cz}/decide", {"decision": "denied"}, fin)
s, _ = call("POST", f"/api/insurance-deep/claims/{cz}/appeals", {"grounds": "documentation attached on appeal"}, fin)
check("denied claim can be appealed", s == 200, f"HTTP {s}")
s, b = call("GET", "/api/insurance-deep/board", token=fin)
rowz = next((r for r in (b.get("claims") or []) if r["id"] == cz), {})
check("appealed claim shows as appeal_open", rowz.get("state") == "appeal_open", f"{rowz.get('state')}")

fails = [r for r in R if not r[1]]
print(f"TOTAL: {len(R) - len(fails)} / {len(R)}")
