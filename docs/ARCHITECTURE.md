# المعمارية — المنظومة الوطنية للصحة (إعادة بناء v6)

## مبادئ حاكمة (من الكتالوج)
1. سلامة المريض أولًا، وخصوصية البيانات، والعزل الصارم بين المنشآت.
2. لا قرار سريري أو مالي تنفيذي من الذكاء الاصطناعي منفردًا — مخرجات Python استشارية بمراجعة بشرية إلزامية.
3. لا انتقال غير قانوني في أي دورة عمل، ولا تخطي خطوة إلزامية، وكل انتقال موثق السبب والفاعل.
4. كل معلومة لها مصدر ووقت ومالك؛ التعارض يظهر ولا يُستبدل بصمت؛ التصحيح إصدار جديد لا طمس.
5. العقود قبل التنفيذ: OpenAPI للمتزامن، وأحداث بإصدارات مخطط (schema versioning) مع idempotency وACK وإعادة محاولة وDLQ ومصالحة.

## المكونات وربطها
```
[Next.js Web] ──REST/OpenAPI──┐
[Flutter App] ──REST──────────┤
                              ▼
                    [platform-api — Java/Spring Boot]
                    modules: identity | patient-mpi | facility |
                             clinical | diagnostics | pharmacy |
                             emergency-command | operations |
                             governance | integration-fhir |
                             cycle-engine | notifications
                              │  ▲
              JDBC/RLS        │  │ REST (advisory, correlation-id)
                              ▼  │
                    [PostgreSQL 16 + PostGIS]   [analytics-ai — Python/FastAPI]
                              │
                    [Kafka events: clinical.*, operations.*, integration.*]
                              │
                    [Keycloak OIDC] [Redis cache/locks] [PACS DICOMweb adapter]
```

## العزل (Multi-Tenancy)
- كل طلب يحمل سياق منشأة من JWT (لا من العميل). النواة تضبط
  `app.current_facility_id` على اتصال قاعدة البيانات لكل معاملة.
- RLS مفعّل ومفروض (FORCE) على كل جدول متعدد المنشآت؛ دور التطبيق `app_user`
  مقيد (NOBYPASSRLS) وليس مالك القاعدة.
- الأدوار الوطنية ترى مجاميع فقط عبر واجهات مخصصة، لا تفاصيل مرضى منشآت أخرى.

## قرارات معمارية (ADR)
- ADR-001 اللغات: TypeScript للويب، Java للنواة، Python للتحليلات، SQL/PostgreSQL، Dart للهاتف — طبقًا لملحق الكتالوج؛ لا لغات إضافية للنواة.
- ADR-002 بداية كتطبيق معياري واحد (Modular Monolith) بحدود سياقات صارمة (خريطة CATALOG_MAPPING.md)، والفصل إلى خدمات عند وجود حاجة تشغيلية مثبتة — تنفيذًا لقاعدة الكتالوج ضد تفتيت الخدمات.
- ADR-003 محرك دورات موحد: الحالات والانتقالات المسموحة معرفة مركزيًا وتُفرض في الخدمة وقاعدة البيانات معًا، وتستخدمه كل الدورات الـ38.
- ADR-004 FHIR R4 واجهة تبادل (موارد Patient/Encounter/Observation...) فوق نموذج المجال الداخلي، مع محولات HL7 v2 للأنظمة القديمة.

## الأمان
OAuth 2.1/OIDC (Keycloak) + MFA/TOTP، JWT قصير العمر، مصفوفة صلاحيات
(الدور × المورد × العملية) رفض افتراضي، Break-Glass بسبب ومدة ومراجعة لاحقة،
سجل تدقيق append-only، تشفير PII بمفتاح خارجي عند التفعيل.
