# النشر على EC2 — خطوات التنفيذ

الحالة: ملف `infra/docker-compose.yml` كان بصيغة تجريبية لا تُنشر كما هي؛
أُصلح في هذا الإصدار (ملف Dockerfile للويب، تقييد المنافذ الداخلية على
localhost، إعادة تشغيل تلقائية، تمرير عنوان الـAPI كـbuild arg).
لا يُبنى من نسخة GitHub قبل اكتمال الرفع الجاري + رفع هذه الملفات
(`apps/web/Dockerfile` و`infra/.env.example` وهذا الملف وتعديل compose).

## 1. مواصفات الـinstance

- **2 vCPU / 8GB RAM على الأقل** (t3.large أو ما يعادلها)، قرص 40GB، ‏Ubuntu 24.04.
- Security Group: افتح **22 و80 و443 فقط** للعامّة. لا تفتح 5432 أو 6379
  أو 9092 أو 8000 — وهي الآن مربوطة على `127.0.0.1` في compose.
  المنافذ العامة للتجربة: 3000 (الويب) و8080 (الـAPI) و8180 (Keycloak) —
  والأفضل إغلاقها خلف Nginx + HTTPS لاحقًا.

## 2. الدخول (طريقة واحدة)

- إضافة المفتاح العام لـMuse على الـinstance (سطر واحد في `~/.ssh/authorized_keys`):
  `ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIK4wduR5VJfb1vG4Jo/UlHRrGruTQwht9KKBKV6Jbt7A hatch`
  ثم إرسال عنوان الـinstance واسم المستخدم (ubuntu أو ec2-user).
- أو ملف `.pem` عبر بطاقة الإدخال الآمن — **أبدًا في الشات**.

## 3. خطوات النشر على السيرفر (بالترتيب)

```bash
# أدوات
sudo apt update && sudo apt install -y docker.io docker-compose-plugin git openjdk-21-jdk maven
sudo usermod -aG docker $USER   # ثم تسجيل خروج/دخول

# الكود (من GitHub بعد اكتمال الرفع)
git clone https://github.com/twins13jan-collab/Health.git
cd Health

# الأسرار — انسخ infra/.env.example إلى infra/.env واملأها
cp infra/.env.example infra/.env
# ⚠️ اترك APP_DB_PASSWORD=change-me كما هو (فخ موثق في .env.example:
# هجرة V1 أنشأت دور app_user بكلمة السر change-me حرفيًا).

# بناء الـjar أولًا — Dockerfile الخاص بالـAPI ينسخ jar جاهزًا
# (target/platform-api-*.jar) ولا يبنيه بنفسه
cd services/platform-api && mvn -B package -DskipTests && cd ../..

# البناء والتشغيل (NEXT_PUBLIC_API_BASE_URL من .env يُخبز في صورة الويب)
cd infra && docker compose up -d --build
```

## 4. التحقق بعد التشغيل

```bash
docker compose ps            # كل الخدمات healthy/up
curl -s http://localhost:8080/actuator/health || curl -s http://localhost:8080/health
curl -s http://localhost:3000 | head -c 200   # صفحة الويب
curl -s http://localhost:8000/health          # التحليلات
```

- تسجيل الدخول من المتصفح على `http://<EC2_PUBLIC_IP>:3000`، والـAPI على `:8080`.
- ملاحظة: Keycloak يعمل بوضع `start-dev` (تجريبي) — الإنتاج يحتاج `start`
  مع قاعدة بيانات وhostname حقيقي؛ لم يُنفذ بعد.

## 5. ما لم يُنفذ بعد

- Nginx + HTTPS (يحتاج دومين)، تحويل platform-api إلى multi-stage build
  يغني عن `mvn package` اليدوي، تغيير كلمة سر `app_user` بهجرة جديدة،
  ونسخ احتياطي لمجلد `db-data`.

## النشر والتكملة على السيرفر نفسه (قرار 2026-11-23)
الخطة المعتمدة: بيئة السيرفر هي بيئة البناء والتحقق الأولى —
لا إعادة كتابة للوحدات المتحقق منها (363 اختبارًا + 64 فحصًا
حيًا)، بل نقل الكود المتحقق ثم **استكمال الهندسة على السيرفر**:
1. `infra/provision-ec2.sh audit` — تدقيق البيئة أولًا (Docker،
   Java 21، Node 20، Python، Flutter، GPU إن وُجد).
2. `infra/provision-ec2.sh install` — تثبيت الناقص (Idempotent).
3. البناء الكلي على السيرفر: صور Docker تُبنى فعليًا لأول مرة،
   و`mvn package` قبل صورة الـAPI، وبناء Flutter فعلي لأول مرة.
4. ربط Kafka الحقيقي: تصريف صندوق الأحداث domain_events إلى
   Kafka بمنتج/مستهلك فعليين (اليوم الوحدات تستطلع الصندوق).
5. شاشات التشغيل: استكمال ربط الكتابة بصفحات الوحدات.
6. اختبارات حمل وأمن على السيرفر قبل أي وصف بـ«إنتاج».
7. تقوية: Keycloak بوضع الإنتاج، HTTPS عبر Nginx/certbot،
   أسرار في .env على السيرفر فقط، نسخ احتياطي مجدول للقاعدة.

## قرار الذكاء الاصطناعي (2026-11-23)
قرار المستخدم: (أ) + (ج) — النماذج الإحصائية المحلية كما هي
(t3.large يكفي، لا GPU)، **إضافةً إلى** ربط ذكاء اصطناعي عبر
API خارجي كمسار مخطط. المزود ومفتاح API يختارهما المستخدم
لاحقًا عند التنفيذ؛ المفتاح يعيش في .env على السيرفر فقط،
ونقطة الربط هي خدمة analytics-ai (محول مزود جديد بجانب
النماذج الإحصائية، وكل مخرجاته استشارية بمراجعة بشرية عبر
وحدة حوكمة الذكاء السريري 233). تنبيه الخصوصية موثق: أي
استدعاء خارجي يخرج بيانات من السيرفر، فيُصمم الربط بإخفاء
الهوية وتقليل البيانات قبل الإرسال.
