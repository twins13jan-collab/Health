import json, os, urllib.request, urllib.error, urllib.parse
BASE = os.environ.get('EHS_BASE', 'http://localhost:8080')
def call(method, path, body=None, token=None):
    req = urllib.request.Request(BASE + urllib.parse.quote(path, safe='/:?=&%'), method=method)
    data = None
    if body is not None:
        data = json.dumps(body).encode(); req.add_header('Content-Type', 'application/json')
    if token: req.add_header('Authorization', 'Bearer ' + token)
    try:
        with urllib.request.urlopen(req, data=data, timeout=20) as r:
            return r.status, json.loads(r.read().decode())
    except urllib.error.HTTPError as e:
        try: return e.code, json.loads(e.read().decode())
        except Exception: return e.code, {}
def login(email):
    s, b = call('POST', '/api/auth/login', {'email': email, 'password': 'Smoke#2026'})
    return b.get('access_token')
def find_id(b, *keys):
    for k in keys:
        v = b.get(k)
        if isinstance(v, dict) and v.get('id'): return v['id']
    if b.get('id'): return b['id']
    for v in b.values():
        if isinstance(v, dict) and v.get('id'): return v['id']
    return None
def report(R):
    for name, ok, code in R:
        print(('PASS' if ok else 'FAIL'), '|', name, '| HTTP', code)
    print('TOTAL:', sum(1 for _, ok, _ in R if ok), '/', len(R))
