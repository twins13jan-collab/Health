from _common import call, login, report
R = []
admin = login('smoke@health.gov.eg')
s, b = call('GET', '/api/analytics/health', None, admin)
R.append(('analytics service reachable THROUGH the platform', s == 200 and b.get('status') == 'up', s))
series = [{'date': f'2026-11-{d:02d}', 'cases': c} for d, c in zip(range(9, 23), [12,14,13,18,22,25,31,36,42,51,58,66,74,83])]
s, b = call('POST', '/api/analytics/forecast/outbreak', {'governorate': 'القاهرة', 'disease': 'influenza-like illness', 'series': series, 'population': 10000000}, admin)
R.append(('outbreak forecast through the proxy: advisory flag intact', s == 200 and b.get('advisory_only') is True and b.get('trend_direction') in ('rising','falling','stable'), s))
occ = [{'date': f'2026-11-{d:02d}', 'occupied': o} for d, o in zip(range(16, 23), [88, 92, 95, 99, 103, 106, 110])]
s, b = call('POST', '/api/analytics/forecast/capacity', {'facility_id': '11111111-1111-1111-1111-111111111111', 'unit': 'ICU', 'total_beds': 120, 'series': occ}, admin)
R.append(('capacity forecast through the proxy: occupancy computed', s == 200 and (b.get('current_occupancy_pct') or 0) > 80, s))
s, b = call('POST', '/api/analytics/forecast/outbreak', {'governorate': 'القاهرة', 'disease': 'x', 'series': series}, None)
R.append(('proxy requires platform authentication (no token -> 401/403)', s in (401, 403), s))
report(R)
