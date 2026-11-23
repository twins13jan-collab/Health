# استرداد بيئة التشغيل المحلية بعد استبدال الجهاز الافتراضي
/tmp مؤقت: قاعدة PostgreSQL المحلية (zonky) تعيش في /tmp/pgdata وقد
تُمسح عند استبدال الـVM. الاسترداد:
1. انسخ الثنائيات: cp -r ~/tools/pg/dist /tmp/pgbin && chmod -R a+rX /tmp/pgbin
2. mkdir -p /tmp/pgdata /tmp/pgrun; chown nobody:nogroup; chmod 700 /tmp/pgdata
3. initdb + pg_ctl start كالمستخدم nobody (منفذ 5433، مقبس /tmp/pgrun)
4. أنشئ قاعدة egypt_health_live ودور app_user
5. أقلع التطبيق — Flyway يطبق V1..Vn تلقائيًا على القاعدة الجديدة
6. ازرع منشأة الاختبار والمستخدمين (كلمة المرور Smoke#2026 بـBCrypt)
حصل هذا فعليًا في 2026-11-22 19:38 واستُعيدت البيئة كاملة.
