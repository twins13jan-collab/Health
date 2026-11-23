import uuid
from _common import call, login, report
R = []
admin = login('smoke@health.gov.eg')
for i in range(3):
    nid = '2950501' + uuid.uuid4().hex[:7]
    call('POST', '/api/patients', {'nationalId': nid, 'fullName': f'مريض ترقيم {i}', 'birthDate': '1995-05-01', 'gender': 'M'}, admin)
s, b = call('GET', '/api/patients', None, admin)
total = b.get('count') or 0
R.append(('legacy list shape intact (full list + total count)', s == 200 and total >= 3 and len(b.get('patients') or []) == total, s))
s, b = call('GET', '/api/patients?size=2&page=0', None, admin)
R.append(('page 0 with size 2 returns exactly 2 rows + same total + pages metadata', s == 200 and len(b.get('patients') or []) == 2 and b.get('count') == total and (b.get('pages') or 0) >= 2, s))
s, b = call('GET', '/api/patients?size=2&page=1', None, admin)
R.append(('page 1 returns the next slice', s == 200 and len(b.get('patients') or []) == 2, s))
report(R)
