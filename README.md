# المنظومة الوطنية المصرية للصحة — إعادة بناء كاملة (Catalog v6)

إعادة بناء هندسية **من الصفر** للمنظومة الوطنية لإدارة الأزمات الصحية والطوارئ
الطبية وإدارة المنشآت الصحية، طبقًا للكتالوج الوظيفي الموسع v6 — Deep
Engineering، وبالتركيبة التقنية المرجعية المعتمدة في ملحق الكتالوج نفسه.
الكود القديم (v2.4.1 — Node.js) مرجع وظيفي فقط ولا يُستخدم أساسًا للبناء.

## التركيبة التقنية المعتمدة (من ملحق الكتالوج v6)

| المكوّن | التقنية |
|---|---|
| واجهة الويب المؤسسية | TypeScript + React + Next.js |
| الخدمات الأساسية | Java 21 + Spring Boot 3 (معيار موحد) |
| الذكاء الاصطناعي والتحليلات | Python 3.12 + FastAPI (استشاري فقط — لا قرار تنفيذي منفرد) |
| قاعدة البيانات | PostgreSQL 16 + PostGIS — RLS، قيود اتساق، Flyway migrations |
| تطبيقات الهاتف | Dart + Flutter |
| التبادل الطبي | HL7 FHIR R4 + HL7 v2 + DICOM/DICOMweb (محولات موثقة) |
| العقود | REST/OpenAPI + أحداث AsyncAPI — idempotency, correlation, ACK/retry/DLQ |
| الهوية والأمن | OAuth 2.1/OIDC + MFA + JWT — عزل صارم بين المنشآت (Multi-Tenancy + RLS) |
| التشغيل | Docker/OCI + docker-compose — OpenTelemetry, Prometheus/Grafana |

## البنية

- `services/platform-api/` — النواة Java/Spring Boot: وحدات مجال (Bounded
  Contexts) لكل مجموعة وحدات في الكتالوج، داخل تطبيق معياري واحد قابل للفصل
  لاحقًا إلى خدمات مستقلة بحدود الملكية نفسها (قرار معماري ADR-002).
- `services/analytics-ai/` — خدمة Python/FastAPI: إنذار مبكر وبائي، توقع
  السعة، ترصّد AMR، دعم فرز NEWS2 — مخرجاتها استشارية وتتطلب مراجعة بشرية.
- `apps/web/` — واجهة Next.js عربية RTL أولًا مع إنجليزية.
- `apps/mobile/` — تطبيق Flutter للميدان والمواطن.
- `infra/` — docker-compose للبنية الكاملة (PostgreSQL/PostGIS, Keycloak,
  Kafka, Redis, النواة، التحليلات، الويب).
- `docs/` — المعمارية، خريطة الوحدات والدورات، قرارات ADR، وحالة التنفيذ بصدق.

## حالة التنفيذ

انظر `docs/IMPLEMENTATION_STATUS.md` — يُحدَّث مع كل دفعة، ولا يُدَّعى اكتمال
وحدة لم تُبنَ وتُختبر فعليًا (قاعدة الكتالوج: وجود شاشة أو endpoint لا يثبت
اكتمال دورة العمل).
