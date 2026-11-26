from _common import call, login, report
R = []
admin = login('smoke@khattab-ai.com')
s, b = call('GET', '/api/ref-data/summary', None, admin)
R.append(('summary: 27 governorates, 22 ICD-10 chapters, >=100 codes, >=40 essential medicines',
  s == 200 and b.get('governorates') == 27 and b.get('icd10_chapters') == 22 and (b.get('icd10_codes') or 0) >= 100 and (b.get('essential_medicines') or 0) >= 40, s))
s, b = call('GET', '/api/ref-data/icd10?search=E11.9', None, admin)
codes = b.get('codes') or []
R.append(('E11.9 resolves to type 2 diabetes', s == 200 and any(c.get('code') == 'E11.9' and 'diabetes' in (c.get('title_en') or '').lower() for c in codes), s))
s, b = call('GET', '/api/ref-data/icd10?search=schistosomiasis', None, admin)
codes = b.get('codes') or []
R.append(('schistosomiasis (Egypt-endemic) present as B65.9', s == 200 and any(c.get('code') == 'B65.9' for c in codes), s))
s, b = call('GET', '/api/ref-data/icd10?search=سكري', None, admin)
codes = b.get('codes') or []
R.append(('Arabic search works (سكري finds diabetes codes)', s == 200 and len(codes) >= 2, s))
s, b = call('GET', '/api/ref-data/medicines?search=metformin', None, admin)
meds = b.get('medicines') or []
R.append(('metformin carries its real ATC code A10BA02', s == 200 and any(m.get('atc_code') == 'A10BA02' for m in meds), s))
s, b = call('GET', '/api/ref-data/governorates', None, admin)
govs = b.get('governorates') or []
R.append(('governorates include Cairo and North Sinai', s == 200 and any(g.get('code') == 'CAI' for g in govs) and any(g.get('code') == 'SIN_N' for g in govs), s))
report(R)
