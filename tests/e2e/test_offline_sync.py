import uuid
from datetime import datetime, timedelta, timezone
from _common import call, login, find_id, report
R = []
admin = login('smoke@khattab-ai.com')
R.append(('admin logged in', bool(admin), 200))
code = 'web-test-' + uuid.uuid4().hex[:8]
s, b = call('POST', '/api/offline/devices', {'deviceCode': code, 'userRef': 'web-client', 'encryptionEnabled': True}, admin)
dev = find_id(b, 'device')
R.append(('device registered and trusted', s == 200 and bool(dev), s))
until = (datetime.now(timezone.utc) + timedelta(days=7)).isoformat()
s, b = call('POST', '/api/offline/packages', {'deviceId': dev, 'userRef': 'web-client', 'scope': '*', 'allowedData': 'registrations,encounters', 'referenceDataVersion': 3, 'validUntil': until}, admin)
pkg = find_id(b, 'package')
R.append(('bounded work package issued (scope + 7-day validity)', s == 200 and bool(pkg), s))
now = datetime.now(timezone.utc).isoformat()
txs = [
  {'localId': 'ltx-' + uuid.uuid4().hex[:8], 'operationType': 'create_record', 'domain': 'administrative', 'payloadHash': 'h1', 'clientTime': now, 'referenceDataVersion': 3, 'conflictsWithServer': False, 'localSummary': 'تسجيل زيارة ميدانية'},
  {'localId': 'ltx-' + uuid.uuid4().hex[:8], 'operationType': 'dose_check', 'domain': 'clinical', 'payloadHash': 'h2', 'clientTime': now, 'referenceDataVersion': 3, 'conflictsWithServer': True, 'localSummary': 'جرعة محلية', 'serverSummary': 'جرعة مركزية مختلفة'},
]
sync_id = 'sync-' + uuid.uuid4().hex[:8]
s, b = call('POST', f'/api/offline/packages/{pkg}/sync', {'syncId': sync_id, 'contentHash': 'hc1', 'transactions': txs}, admin)
R.append(('sync applied the clean tx and filed the clinical conflict (no last-write-wins)', s == 200 and b.get('applied') == 1 and b.get('conflicted') == 1, s))
s, b = call('POST', f'/api/offline/packages/{pkg}/sync', {'syncId': sync_id, 'contentHash': 'hc1', 'transactions': txs}, admin)
R.append(('REPLAYING the same envelope -> duplicate, nothing applied twice', s == 200 and b.get('duplicate') is True, s))
stale = [{'localId': 'ltx-' + uuid.uuid4().hex[:8], 'operationType': 'dispense', 'domain': 'clinical', 'payloadHash': 'h3', 'clientTime': now, 'referenceDataVersion': 1, 'conflictsWithServer': False, 'localSummary': 'صرف ببيانات مرجعية قديمة'}]
s, b = call('POST', f'/api/offline/packages/{pkg}/sync', {'syncId': 'sync-' + uuid.uuid4().hex[:8], 'contentHash': 'hc2', 'transactions': stale}, admin)
R.append(('dispense on STALE reference data (v1 < v3) -> rejected, not applied', s == 200 and (b.get('rejected') or 0) >= 1 and b.get('applied') == 0, s))
s, b = call('GET', '/api/offline/conflicts/open', None, admin)
confs = b.get('conflicts') or []
R.append(('the clinical conflict waits OPEN for the facility manager', s == 200 and len(confs) >= 1, s))
report(R)
