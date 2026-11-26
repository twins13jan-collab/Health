# مراجعة شاملة من البداية إلى النهاية — منظومة خطاب (2026-11-23)

مراجعة قراءة-فقط (read-only) أُجريت 2026-11-23 ~16:08–16:25 EET. كل رقم أدناه موثّق بمصدر: ملف في المستودع، أو استعلام حي على قاعدة بيانات السيرفر، أو تشغيل فعلي لمجموعات الاختبار أثناء المراجعة. لم يُغيَّر أي كود ولم يُنشر أو يُعاد تشغيل أي شيء.

> ملاحظة توقيت مهمة: أثناء هذه المراجعة كان العمل على دفعة 4.6 (لوحة العناية المركزة) يُكتب فعليًا داخل المستودع (طوابع ملفات criticalcare بين 16:09 و16:12 EET). حالة ICU أدناه تعكس ذلك بدقة.

---

## 1. الخادم الخلفي — platform-api

### 1.1 هجرات Flyway
- المستودع: **95 ملف هجرة، V1–V95** (`services/platform-api/src/main/resources/db/migration/`، الأحدث `V95__egypt_administrative_division.sql`).
- السيرفر: `select max(version) from flyway_schema_history where success` = **95**.
- **النتيجة: متطابقان — لا هجرات معلّقة.**

### 1.2 جرد المتحكمات والوحدات
- **181 ملف `*Controller.java`** وأكثر من 150 حزمة Java تحت `eg.gov.health.platform`.
- `docs/MODULE_GAP_AUDIT.md`: الحصيلة النهائية **145 A / 0 B / 0 C / 0 D** — كل وحدة في الكتالوج (46–190) لها تنفيذ خلفي مخصص. **لا توجد وحدة بلا backend.**

### 1.3 الاختبارات (تشغيل فعلي أثناء المراجعة)
- بيئة العمل المحلية (sandbox) **لا يوجد فيها JDK ولا Maven إطلاقًا** (`java: MISSING`)، فشُغّلت المجموعة على السيرفر حيث توجد الأدوات (دون أي نشر أو إعادة تشغيل للخدمة).
- النتيجة من تقارير surefire الطازجة لهذا التشغيل: **400 اختبار، 0 فشل، 0 خطأ، 0 متخطى — 161 فئة اختبار**.
- الزيادة عن آخر رقم معروف (395) سببها 5 اختبارات جديدة `criticalcare/IcuBoardRulesTest` من دفعة 4.6 الجارية.

### 1.4 أسلاك غير مكتملة (موثّقة)
| البند | الحالة | الدليل |
|---|---|---|
| Kafka للـ outbox | **غير مربوط برمجيًا** | لا اعتماد kafka في `pom.xml`، ولا أي فئة producer/consumer؛ الحاوية تعمل لكن الكود لا يستخدمها — صندوق `domain_events` يُستطلع فقط |
| بوابات SMS/البريد | **غير مهيأة** | `notify/NotificationService.java`: القناتان تقرآن `app.notify.sms-gateway` / `app.notify.email-gateway` وكلاهما فارغ → كل إرسال يفشل بأمانة `gateway_not_configured`. يحتاج مزودًا وبيانات اعتماد من الإدارة |
| Keycloak | **وضع تطوير** | `infra/docker-compose.yml`: `command: start-dev` (وتعليق داخل الملف نفسه يقرّ بأن الإنتاج يحتاج `start` بقاعدة حقيقية وhostname)؛ المنفذ 8180 منشور |
| النسخ الاحتياطي المجدول لقاعدة البيانات | **غير موجود** | crontab بلا أي pg_dump؛ مؤقّتات systemd المعروضة dpkg/apt فقط؛ `~/backups/` لا يحمل إلا نسخ ووردبريس من 2026-11-23. لا توجد أي مهمة نسخ احتياطي لقاعدة المنظومة |
| كلمة مرور app_user | مديونية معروفة | V1 يثبّت كلمة مرور ابتدائية (`APP_DB_PASSWORD=change-me` في بيئة السيرفر) وتدويرها بهجرة لاحقة لم يتم بعد (من تدقيق سابق) |
| مصفوفة الصلاحيات | **19 نطاق كتابة محكومًا من 181 متحكمًا** | `role_permissions` حيًا: /api/adt, /api/billing, /api/clinics, /api/facility-safety, /api/forensics, /api/hr-deep, /api/infection-control, /api/insurance-deep, /api/medical-assets, /api/nursing, /api/oncology, /api/or, /api/patients, /api/pharmacy, /api/portal, /api/purchasing, /api/school-health, /api/social-services, /api/vendor-monitoring. **`/api/encounters` غير محكوم** (استعلام مباشر: 0 قواعد) → كتابات اللقاءات مفتوحة لأي دور مصادَق |
| مفاتيح الذكاء الخارجي | سليم | `APP_SETTINGS_KEY` مضبوط في حاوية المنصة؛ المفاتيح مشفرة AES-256-GCM (V90) |

---

## 2. الواجهة — apps/web (الإصدار v3 الحي)

- **175 ملف page.tsx = 181 مسارًا** في بناء Next.js.
- **29 شاشة مخصصة**: `/` (الرئيسية)، about, account, admin, admin/ai, admin/facilities, admin/users, adt, analytics, assistant, clinics, contact, cycles, dashboard, download-app, encounters, er, facilities, hospital, login, my, nursing, offline, operating-rooms, patients, portal-inbox, privacy, register, services.
- **146 صفحة ModuleScreen عامة** (قائمة CRUD موحدة).
- صفحات ModuleScreen التي تقابل أقسامًا تشغيلية بلا شاشة مخصصة بعد: `/icu`، `/neonatal`، `/maternity`، `/radiology` + `/radiology-deep`، `/documents` (السجلات)، `/cssd`، `/porterage` + `/medical-waste` + `/facilities-infra` (مساندة)، `/medical-assets` (طبية/صيانة)، `/billing` + `/general-ledger` + `/insurance-deep` (مالية)، `/hr-deep` + `/workforce` (موارد بشرية)، `/inventory` + `/purchasing` (مخازن)، `/labs`، `/pharmacy`، `/blood-bank`، `/programs` (غسيل كلوي)، `/oncology`، `/child-health` + `/preventive` (مراكز صحية).

---

## 3. خدمة التحليلات والذكاء — analytics-ai

- **pytest أثناء المراجعة: 45 passed, 1 warning (0.39s)** — أخضر بالكامل.
- النقاط: `GET /health`، `GET /v1/health`، `POST /v1/forecast/outbreak`، `POST /v1/forecast/capacity`، `POST /v1/amr/summary`، `POST /v1/ai/triage-support` (NEWS2 كامل، `requires_human_review=true` دائمًا)، `POST /v1/ai/chat`.
- المسار المحلي: Ollama ‏(qwen2.5:3b) بوكيل أدوات حية. المسار الخارجي: 5 مزودين من لوحة /admin/ai. **تنقية المعرّفات مؤكدة**: `scrub_identifiers()` في `app/services/llm.py` تُطبق على نتائج الأدوات قبل أي إرسال خارجي، مع سقوط تلقائي للمسار المحلي عند فشل الخارجي. اسم المزود لا يظهر في الواجهة.

---

## 4. تطبيق الهاتف — apps/mobile

- الإصدار في `pubspec.yaml`: **6.0.2+3** (نفس الـAPK الحي، 21,487,942 بايت).
- 6 شاشات فقط: login, home, patient_search, dispatch_queue, cycle_tasks, critical_alerts — موجهة لفرق الميدان/الإرسال.
- **لا تغطية** لـ: لوحة الأسرّة ADT، لوحة الطوارئ، العيادات، بوابة المريض (/my) — كلها موجودة خلفيًا وويبيًا وغائبة هاتفيًا.
- **هيكل `android/` غير موجود في المستودع المحلي أصلًا** — موجود على السيرفر فقط حيث بُني الـAPK. فجوة اكتمال للمستودع.
- **التوقيع الإنتاجي غير موجود**: `android/app/build.gradle` على السيرفر يوقّع الـrelease بمفاتيح debug (تعليق TODO داخل الملف).

---

## 5. النظام الحي — https://khattab-ai.com

- **8 حاويات تعمل**: infra-db-1 ‏(healthy)، infra-analytics-ai-1 ‏(healthy)، infra-platform-api-1، infra-web-1، infra-redis-1، infra-kafka-1، infra-keycloak-1، infra-ollama-1.
- القرص: **48G — مستخدم 80%، متاح 9.9G** (بند مراقبة: نماذج Ollama وصور Docker والسجلات تنمو).
- الشهادة: Let's Encrypt، **تنتهي 2027-02-21** — سارية.
- API عام حي: `GET /api/public/summary` يرد: منشأتان عاملتان، 154 مريضًا مسجلًا، 51 لقاء اليوم، إشغال أسرّة 50%.
- **E2E أُعيد تشغيله على السيرفر أثناء المراجعة: 64/64** — (analytics ‏4/4، journeys ‏21/21، notifications ‏9/9، offline ‏7/7، pagination ‏3/3، print-documents ‏5/5، RBAC ‏9/9، reference-data ‏6/6)، صفر فشل.

---

## 6. البيانات الحية (استعلامات مباشرة على قاعدة السيرفر)

| البند | العدد | ملاحظة |
|---|---|---|
| ref_regions | 7 | ✓ كاملة (V95) |
| ref_governorates | 27 | ✓ مرتبطة بالأقاليم |
| ref_cities | 396 | ✓ (القاهرة 57) |
| ref_icd10_codes | 104 | **مجموعة بداية فقط** — لم يُحمَّل ICD-10 الرسمي الكامل |
| ref_essential_medicines | 45 | **مجموعة بداية WHO** — لم تُحمَّل قائمة الأدوية الوطنية الكاملة |
| facilities | 2 (كلاهما active) | منشأة البذرة + معمل الاختبار المعتمد — لا بيانات تشغيل حقيقية بعد (قرار المستخدم) |
| app_users | 18 | 13 بذرة + ممرضة + مدير معمل + حسابات اختبار |
| patients | 154 | غالبيتها تسجيلات اختبار من بطاريات التحقق |
| partner_organizations | **0** | **دليل الشبكة الطبية (PDF أغسطس 2020) لم يُستخرج بعد** — جداول الشراكات الإطارية موجودة (partner_organizations/contracts/access_grants/performance_reviews) لكن لا جدول لمقدمي الشبكة ولا أي صفوف |

---

## 7. مصفوفة اكتمال المراحل

التعريف: **DONE** = طبقة تشغيل مخصصة + شاشة مخصصة + تحقق حي. **FRAMEWORK-ONLY** = نهايات الوحدة الإطارية موجودة (دورة كاملة بعمق الكتالوج) لكن بلا طبقة/شاشة تشغيل يومية مخصصة. **MISSING** = لا شيء.

### المرحلة 4 — إدارة المستشفى
| القسم | الحالة | الدليل |
|---|---|---|
| القبول والإقامة ADT | ✅ DONE | حزمة adt ‏(V86)، شاشة /adt، متحقق حيًا |
| الطوارئ والفرز ER | ✅ DONE | ErRules + ‏POST /triage + ‏er-board، شاشة /er |
| العيادات الخارجية | ✅ DONE | حزمة clinics ‏(V88)، شاشة /clinics |
| التمريض | ✅ DONE | ‏V89 ‏(MAR + جولات)، شاشة /nursing |
| العمليات والتخدير | ✅ DONE | حزمة surgeryops ‏(V91)، شاشة /operating-rooms، بطارية حية 11/11 |
| العناية المركزة ICU | 🔶 **قيد التنفيذ — مبنية في المستودع، غير منشورة** | `IcuBoardRules.java` + نهاية board داخل `IcuController.java` (كُتبت 16:09–16:12 EET أثناء المراجعة) + 5 اختبارات جديدة (mvn صار 400)؛ صفحة /icu ما زالت ModuleScreen؛ لا نشر ولا تحقق حي بعد |
| الحضانات NICU | FRAMEWORK-ONLY | حزمة neonatal (دورة 245، NicuStay)؛ /neonatal ModuleScreen؛ لا لوحة تشغيل |
| الأمومة | FRAMEWORK-ONLY | حزمة maternity (دورة 244)؛ /maternity ModuleScreen |
| الأشعة | FRAMEWORK-ONLY | حزمة radiologydeep + diagnostics؛ /radiology و/radiology-deep ModuleScreen؛ لا قائمة عمل تشغيلية |
| السجلات الطبية والترميز | FRAMEWORK-ONLY | recordsdocs + longitudinal؛ /documents ModuleScreen |
| التعقيم المركزي CSSD | FRAMEWORK-ONLY | حزمة cssd؛ /cssd ModuleScreen |
| الخدمات المساندة | FRAMEWORK-ONLY | porterage + facilityenvironment + facilitiesinfra؛ صفحات ModuleScreen |
| الصيانة الطبية/الأصول | FRAMEWORK-ONLY | حزمة medicalassets (دورة 230)؛ /medical-assets ModuleScreen (محكوم في المصفوفة) |
| المالية والفوترة والتأمين | FRAMEWORK-ONLY | finance + ledger + insurancedeep؛ /billing و/general-ledger و/insurance-deep ModuleScreen |
| الموارد البشرية والجدولة | FRAMEWORK-ONLY | hrdeep + workforce؛ صفحات ModuleScreen |
| المخازن والمشتريات | FRAMEWORK-ONLY | purchasing + supplychaindeep؛ /inventory و/purchasing ModuleScreen |

### المرحلة 5 — أنظمة المنشآت المستقلة
| النظام | الحالة | الدليل |
|---|---|---|
| المعامل (LIS) | FRAMEWORK-ONLY | دورة 219 إطارية + /labs ModuleScreen؛ لا استقبال عينات/باركود/قوائم عمل/QC/اعتماد نتائج كطبقة تشغيل |
| الصيدليات | FRAMEWORK-ONLY | دورة 222 إطارية + /pharmacy ModuleScreen؛ لا طابور صرف/FEFO/سجل مخدرات كشاشات تشغيل |
| المراكز والوحدات الصحية | FRAMEWORK-ONLY | childhealth + preventive + schoolhealth إطارية؛ لا ملف أسرة/تطعيمات كشاشات تشغيل (بوابة المواطن تغطي جانب الحجز فقط) |
| بنوك الدم | FRAMEWORK-ONLY | دورة 223 إطارية؛ /blood-bank ModuleScreen |
| مراكز الغسيل الكلوي | FRAMEWORK-ONLY | حزمة programs (دورة 242)؛ /programs ModuleScreen |
| مراكز الأورام | FRAMEWORK-ONLY | حزمة oncology (دورة 243)؛ /oncology ModuleScreen |

**لا يوجد أي قسم MISSING** — الفجوة كلها من نوع واحد: طبقة تشغيل يومية مخصصة فوق إطار مكتمل.

---

## 8. بنود معروفة من تدقيقات سابقة (لم تُعَد verification في هذه الجولة)
- رفع GitHub (twins13jan-collab/Health) متوقف عند ~149 commit باختيار المستخدم؛ ملفات platform-api ‏(1,619) لم تُرفع بعد؛ CI لم ينجح طرف-لطرف قط (الشجرة ناقصة).
- لا اختبارات حمل ولا اختراق (penetration) منفذة حتى الآن.
- HL7 v2/DICOMweb لم يُختبرا ضد نظام خارجي حقيقي قط.
- زمن استجابة النموذج المحلي ~54 ثانية (CPU بلا GPU) — قيد معروف ومقبول بقرار (أ)+(ج).

---

## 9. الخلاصة التنفيذية
النظام **حي وسليم ومختبَر**: Flyway متطابق (V95)، ‏400/400 اختبار Java، ‏45/45 ‏Python، ‏E2E ‏64/64 بتشغيل طازج، 8 حاويات مستقرة، شهادة سارية حتى 2027-02-21. ما ينقص ليس إصلاح أعطال بل **استكمال طبقات تشغيل**: بقية المرحلة 4 (بعد نشر 4.6)، والمرحلة 5 كلها، ثم تقوية الإنتاج (صلاحيات encounters، نسخ احتياطي، Keycloak، Kafka، بوابات الإرسال)، ثم البيانات المرجعية الكاملة واستخراج دليل الشبكة، ثم اكتمال الهاتف والمستودع.
