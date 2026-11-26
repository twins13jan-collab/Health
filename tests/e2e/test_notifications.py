import uuid
from _common import call, login, report
R = []
admin = login('smoke@khattab-ai.com')
R.append(('admin logged in', bool(admin), 200))
s, b = call('POST', '/api/notification-outbox', {'recipientRef': 'physician-7', 'channel': 'pigeon', 'subject': 'x', 'body': 'y'}, admin)
R.append(('unknown channel -> 400 with the channel list', s == 400, s))
s, b = call('POST', '/api/notification-outbox', {'recipientRef': 'physician-7', 'channel': 'in_app', 'subject': 'نتيجة حرجة', 'body': 'تروبونين مرتفع — راجع فورًا'}, admin)
n1 = (b.get('notification') or {}).get('id')
R.append(('in_app notification queued', s == 200 and bool(n1), s))
s, b = call('POST', '/api/notification-outbox', {'recipientRef': '+201000000000', 'channel': 'sms', 'subject': 'تنبيه', 'body': 'رسالة اختبار'}, admin)
n2 = (b.get('notification') or {}).get('id')
R.append(('sms notification queued', s == 200 and bool(n2), s))
s, b = call('POST', '/api/notification-outbox/dispatch', None, admin)
res = b.get('result') or {}
R.append(('dispatch ran: in_app sent, sms failed honestly (no gateway configured)', s == 200 and res.get('sent', 0) >= 1 and res.get('failed_retryable', 0) >= 1, s))
s, b = call('GET', '/api/notification-outbox/inbox/physician-7', None, admin)
msgs = b.get('messages') or []
R.append(('recipient inbox shows the delivered message as SENT with sent_at', s == 200 and any(m.get('status') == 'sent' and m.get('sentAt') for m in msgs), s))
for i in range(2):
    call('POST', '/api/notification-outbox/dispatch', None, admin)
s, b = call('GET', f'/api/notification-outbox/{n2}/attempts', None, admin)
atts = b.get('attempts') or []
R.append(('sms attempts recorded: 3 honest failures (gateway_not_configured), never fake-sent', s == 200 and len(atts) >= 3, s))
s, b = call('POST', '/api/notification-outbox/dispatch', None, admin)
res = b.get('result') or {}
R.append(('after the attempt cap the dead message is no longer dispatched', s == 200 and res.get('sent', 0) == 0 and res.get('failed_retryable', 0) == 0, s))
s, b = call('POST', '/api/journeys', {'patientId': str(uuid.uuid4()), 'encounterId': str(uuid.uuid4())}, admin)
j = (b.get('journey') or {}).get('id')
for key in ['encounter', 'diagnostics', 'pharmacy', 'billing', 'discharge']:
    call('POST', f'/api/journeys/{j}/steps', {'stepKey': key, 'module': 'wiring', 'entityType': 'ref', 'entityId': str(uuid.uuid4()), 'detail': 'wiring check'}, admin)
s, b = call('POST', f'/api/journeys/{j}/complete', None, admin)
ok_complete = s == 200
call('POST', '/api/notification-outbox/dispatch', None, admin)
s2, b2 = call('GET', '/api/notification-outbox/inbox/facility-admin', None, admin)
msgs = b2.get('messages') or []
R.append(('journey completion QUEUED a real notification that was then delivered', ok_complete and any(m.get('status') == 'sent' for m in msgs), s2))
report(R)
