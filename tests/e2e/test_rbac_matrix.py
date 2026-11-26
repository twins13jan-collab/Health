import uuid
from _common import call, login, report
R = []
admin = login('smoke@khattab-ai.com'); recep = login('reception@khattab-ai.com')
fin = login('finance@khattab-ai.com'); pharm = login('pharmacist@khattab-ai.com')
R.append(('four role users logged in', all([admin, recep, fin, pharm]), 200))
s, b = call('GET', '/api/access-matrix', None, admin)
R.append(('matrix visible: 171 governed domains', s == 200 and len(b.get('rules') or []) == 171, s))
nid = '2970401' + uuid.uuid4().hex[:7]
s, b = call('POST', '/api/patients', {'nationalId': nid, 'fullName': 'مريض صلاحيات', 'birthDate': '1997-04-01', 'gender': 'M'}, recep)
pid = None
for v in b.values():
    if isinstance(v, dict) and v.get('id'): pid = v['id']
R.append(('receptionist CAN register a patient (her domain)', s in (200, 201) and bool(pid), s))
s, b = call('POST', '/api/billing/invoices', {'patientId': pid, 'payer': 'cash'}, recep)
R.append(('receptionist CANNOT write billing -> 403 role_forbidden', s == 403 and b.get('error') == 'role_forbidden', s))
s, b = call('POST', '/api/billing/invoices', {'patientId': pid, 'payer': 'cash'}, fin)
R.append(('finance manager CAN write billing', s in (200, 201), s))
s, b = call('POST', '/api/pharmacy/prescriptions', {'patientId': pid, 'drugCode': 'PAR-500', 'drugName': 'باراسيتامول', 'dose': '500mg', 'frequency': 'tid', 'duration': '5d'}, fin)
R.append(('finance manager CANNOT write pharmacy -> 403', s == 403, s))
s, b = call('POST', '/api/pharmacy/prescriptions', {'patientId': pid, 'drugCode': 'PAR-500', 'drugName': 'باراسيتامول', 'dose': '500mg', 'frequency': 'tid', 'duration': '5d'}, pharm)
R.append(('pharmacist CAN write pharmacy', s in (200, 201), s))
s, b = call('GET', '/api/billing/invoices', None, recep)
R.append(('reads are governed too: receptionist CANNOT read billing -> 403', s == 403 and b.get('error') == 'role_forbidden', s))
s, b = call('GET', '/api/billing/invoices', None, fin)
R.append(('finance manager CAN read billing (his domain)', s == 200, s))
s, b = call('GET', '/api/pharmacy/prescriptions', None, fin)
R.append(('finance manager CANNOT read pharmacy domain -> 403', s == 403, s))
s, b = call('POST', '/api/journeys', {'patientId': pid}, recep)
R.append(('write outside the role domain refused: receptionist cannot write journeys -> 403', s == 403, s))
s, b = call('POST', '/api/billing/invoices', {'patientId': pid, 'payer': 'cash'}, admin)
R.append(('admin bypasses the matrix', s in (200, 201), s))
report(R)
