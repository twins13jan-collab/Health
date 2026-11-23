import urllib.request, urllib.error, uuid
from _common import call, login, find_id, report, BASE
def get_pdf(path, token):
    req = urllib.request.Request(BASE + path, method='GET')
    req.add_header('Authorization', 'Bearer ' + token)
    try:
        with urllib.request.urlopen(req, timeout=20) as r:
            return r.status, r.headers.get_content_type(), r.read()
    except urllib.error.HTTPError as e:
        return e.code, '', b''
R = []
admin = login('smoke@health.gov.eg')
nid = '2980301' + uuid.uuid4().hex[:7]
s, b = call('POST', '/api/patients', {'nationalId': nid, 'fullName': 'مريض مستندات رسمية', 'birthDate': '1998-03-01', 'gender': 'F'}, admin)
pid = find_id(b, 'patient')
s, b = call('POST', '/api/encounters', {'patientId': pid, 'type': 'outpatient', 'chiefComplaint': 'متابعة', 'triageLevel': 4}, admin)
eid = find_id(b, 'encounter')
s, b = call('POST', '/api/pharmacy/prescriptions', {'patientId': pid, 'encounterId': eid, 'drugCode': 'MET-500', 'drugName': 'ميتفورمين 500', 'dose': '500mg', 'frequency': 'twice daily', 'duration': '90d'}, admin)
rx = find_id(b, 'prescription')
s, b = call('POST', '/api/labs/orders', {'patientId': pid, 'encounterId': eid, 'testCode': 'HBA1C', 'testName': 'هيموجلوبين سكري', 'priority': 'routine'}, admin)
oid = find_id(b, 'order')
call('POST', f'/api/labs/orders/{oid}/results', {'value': 6.8, 'unit': '%', 'refLow': 4.0, 'refHigh': 5.6}, admin)
s, b = call('POST', '/api/billing/invoices', {'patientId': pid, 'encounterId': eid, 'payer': 'insurance'}, admin)
iid = find_id(b, 'invoice')
call('POST', f'/api/billing/invoices/{iid}/lines', {'description': 'كشف عيادة', 'unitPrice': 300, 'quantity': 1, 'coveragePct': 80}, admin)
s, b = call('POST', '/api/admissions-deep/admissions', {'patientRef': pid, 'justification': 'ملاحظة سكري', 'priority': 'routine', 'specialty': 'internal', 'transfer': False, 'receivingAccepted': True}, admin)
aid = find_id(b, 'admission')
call('POST', f'/api/admissions-deep/admissions/{aid}/admit', None, admin)
s, b = call('POST', f'/api/admissions-deep/admissions/{aid}/discharge-summary', {'diagnoses': 'سكري نوع ثانٍ', 'procedures': 'تحاليل دورية', 'instructions': 'غذاء متوازن ورياضة', 'warnings': 'أعراض هبوط السكر', 'medications': 'ميتفورمين 500 مرتين', 'followUp': 'عيادة باطنة بعد شهر'}, admin)
dsid = find_id(b, 'summary') or aid
def check(name, path):
    st, ct, data = get_pdf(path, admin)
    ok = st == 200 and ct == 'application/pdf' and data[:4] == b'%PDF' and len(data) > 1200
    R.append((f'{name} renders a REAL Arabic PDF ({len(data)} bytes)', ok, st))
check('prescription', f'/api/print-documents/prescription/{rx}')
check('lab report', f'/api/print-documents/lab-report/{oid}')
check('invoice', f'/api/print-documents/invoice/{iid}')
check('discharge summary', f'/api/print-documents/discharge-summary/{dsid}')
st, ct, data = get_pdf(f'/api/print-documents/prescription/{uuid.uuid4()}', admin)
R.append(('unknown document id -> 404, not a fake PDF', st == 404, st))
report(R)
