-- V2 — Unified Operating Cycle Engine (Catalog v6 cycles 217-254).
-- Definitions seeded verbatim from the catalog; instances are
-- facility-isolated (RLS); events are the append-only transition audit.
CREATE TABLE IF NOT EXISTS cycle_definitions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID, governorate TEXT, directorate TEXT,
  code TEXT NOT NULL UNIQUE,
  catalog_number INT NOT NULL UNIQUE,
  title_ar TEXT NOT NULL,
  domain TEXT NOT NULL,
  purpose_ar TEXT,
  specialised_route TEXT,
  sla_hours INT NOT NULL DEFAULT 72,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','archived')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS cycle_instances (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id),
  governorate TEXT, directorate TEXT,
  definition_code TEXT NOT NULL REFERENCES cycle_definitions(code),
  subject_type TEXT, subject_ref TEXT, patient_id UUID REFERENCES patients(id),
  title TEXT NOT NULL, current_step TEXT, protocol_version TEXT,
  correlation_id TEXT NOT NULL,
  idempotency_key TEXT,
  sla_due_at TIMESTAMPTZ,
  escalated BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft','open','in_progress','on_hold','pending_review','completed','closed','cancelled','failed','corrected','reconciled')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_cycle_instances_facility_status ON cycle_instances (facility_id, status);
CREATE UNIQUE INDEX IF NOT EXISTS uq_cycle_instances_idempotency ON cycle_instances (facility_id, idempotency_key) WHERE idempotency_key IS NOT NULL;
CREATE TABLE IF NOT EXISTS cycle_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL, governorate TEXT, directorate TEXT,
  instance_id UUID NOT NULL REFERENCES cycle_instances(id),
  event_type TEXT NOT NULL, from_status TEXT, to_status TEXT,
  actor_id TEXT, actor_role TEXT, reason TEXT,
  status TEXT NOT NULL DEFAULT 'recorded',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_cycle_events_instance ON cycle_events (instance_id, created_at);

ALTER TABLE cycle_instances ENABLE ROW LEVEL SECURITY;
ALTER TABLE cycle_instances FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS cycle_instances_facility_isolation ON cycle_instances;
CREATE POLICY cycle_instances_facility_isolation ON cycle_instances FOR ALL
  USING (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)))
  WITH CHECK (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)));
ALTER TABLE cycle_events ENABLE ROW LEVEL SECURITY;
ALTER TABLE cycle_events FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS cycle_events_facility_isolation ON cycle_events;
CREATE POLICY cycle_events_facility_isolation ON cycle_events FOR ALL
  USING (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)))
  WITH CHECK (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)));
GRANT SELECT, INSERT, UPDATE, DELETE ON cycle_definitions, cycle_instances, cycle_events TO app_user;

INSERT INTO cycle_definitions (code, catalog_number, title_ar, domain, purpose_ar, specialised_route) VALUES
  ('CYCLE-217', 217, 'دورة السجل الصحي الطولي ومصالحة بيانات المريض', 'clinical-data', NULL, NULL),
  ('CYCLE-218', 218, 'دورة الموافقات المستنيرة والتفويض ومشاركة البيانات', 'governance', NULL, NULL),
  ('CYCLE-219', 219, 'دورة الطلب الطبي والعينة والنتيجة الحرجة', 'clinical', NULL, 'lab-cycle'),
  ('CYCLE-220', 220, 'دورة العمليات والتخدير والتعافي', 'clinical', NULL, 'surgery-cycle'),
  ('CYCLE-221', 221, 'دورة العناية المركزة ومراقبة الأجهزة الطبية', 'clinical', NULL, NULL),
  ('CYCLE-222', 222, 'دورة المصالحة الدوائية والصيدلة الإكلينيكية', 'clinical', NULL, 'pharmacy-cycle'),
  ('CYCLE-223', 223, 'دورة بنك الدم والتوافق والنقل والتفاعلات', 'clinical', NULL, 'bloodbank-cycle'),
  ('CYCLE-224', 224, 'دورة الإحالة والنقل الطبي بين المنشآت', 'operations', NULL, 'emergency-cycle'),
  ('CYCLE-225', 225, 'دورة السعة والأسرة والجاهزية التشغيلية', 'operations', NULL, NULL),
  ('CYCLE-226', 226, 'دورة الاستدعاء الدوائي والتشغيلة والمخزون', 'pharmacy', NULL, NULL),
  ('CYCLE-227', 227, 'دورة المشتريات والعقود والاستلام والموردين', 'operations', NULL, 'procurement'),
  ('CYCLE-228', 228, 'دورة الإيرادات والفوترة والتأمين والمطالبات', 'finance', NULL, 'billing'),
  ('CYCLE-229', 229, 'دورة الأستاذ العام والإقفال والمصالحة البنكية', 'finance', NULL, 'general-ledger'),
  ('CYCLE-230', 230, 'دورة الأصول الطبية والصيانة والمعايرة', 'operations', NULL, 'cmms'),
  ('CYCLE-231', 231, 'دورة القوى العاملة والتراخيص والكفاءة والورديات', 'workforce', NULL, 'hr-cycle'),
  ('CYCLE-232', 232, 'دورة الشكاوى وحقوق المريض والتظلمات', 'governance', NULL, 'ombudsman'),
  ('CYCLE-233', 233, 'دورة الذكاء الاصطناعي السريري والتحقق البشري', 'digital-ai', NULL, NULL),
  ('CYCLE-234', 234, 'دورة التكاملات ومراقبة الرسائل والتوافق', 'digital', NULL, NULL),
  ('CYCLE-235', 235, 'دورة البيانات الرئيسية والكتالوجات المرجعية', 'digital-data', NULL, NULL),
  ('CYCLE-236', 236, 'دورة التقارير القومية والبيانات المفتوحة', 'governance', NULL, NULL),
  ('CYCLE-237', 237, 'دورة العمل دون اتصال والمزامنة الآمنة', 'digital', NULL, NULL),
  ('CYCLE-238', 238, 'دورة الحوادث الأمنية والخصوصية', 'security', NULL, NULL),
  ('CYCLE-239', 239, 'دورة الوثائق والسجلات والتوقيع والاحتفاظ', 'governance', NULL, NULL),
  ('CYCLE-240', 240, 'دورة علم الأمراض والأنسجة والخلايا', 'clinical', 'إدارة العينة النسيجية/الخلوية من الطلب والاستلام والتحضير والفحص حتى التقرير المعتمد وربطه بالحالة.', 'pathology'),
  ('CYCLE-241', 241, 'دورة المناظير والإجراءات التشخيصية والتداخلية', 'clinical', 'إدارة الحجز والتحضير والتنفيذ والمراقبة وإعادة معالجة المنظار والتقرير والمتابعة.', 'endoscopy'),
  ('CYCLE-242', 242, 'دورة غسيل الكلى والعلاج الكلوي البديل', 'clinical', 'إدارة جلسات الغسيل الدموي والبريتوني ومتابعة الخطة والمستهلكات والمضاعفات.', 'dialysis-cycle'),
  ('CYCLE-243', 243, 'دورة الأورام والعلاج الكيماوي والمناعي والإشعاعي', 'clinical', 'إدارة التشخيص واللجنة متعددة التخصصات والبروتوكول والجلسات والسمية والمتابعة.', 'oncology-cycle'),
  ('CYCLE-244', 244, 'دورة الأمومة والحمل والولادة وما بعد الولادة', 'clinical', 'إدارة متابعة الحمل وتقييم الخطورة والولادة والنفاس وربط رعاية المولود.', 'obstetrics-gynecology'),
  ('CYCLE-245', 245, 'دورة حديثو الولادة والفحص المبكر والحضانة', 'clinical', 'إدارة تقييم المولود والفحص المبكر والتطعيم والتغذية والحضانة ومتابعة النمو.', 'neonatology'),
  ('CYCLE-246', 246, 'دورة التأهيل الطبي والعلاج الطبيعي والوظيفي والنطق', 'clinical', 'إدارة تقييم الوظيفة وخطة التأهيل والجلسات وقياس التحسن والخروج/الإحالة.', 'rehab'),
  ('CYCLE-247', 247, 'دورة الرعاية التلطيفية وإدارة الألم ونهاية الحياة', 'clinical', 'تنظيم تخفيف المعاناة والدعم النفسي والاجتماعي والتخطيط التشاركي للرعاية.', NULL),
  ('CYCLE-248', 248, 'دورة طب الأسنان وصحة الفم والفكين', 'clinical', 'إدارة الفحص والتشخيص وخطة العلاج والإجراءات والأشعة والمتابعة ومكافحة العدوى.', 'dentistry'),
  ('CYCLE-249', 249, 'دورة طب العيون والسمع والأنف والأذن والحنجرة', 'clinical', 'إدارة فحوص الوظائف الحسية والتشخيص والإجراءات والأجهزة المساعدة والمتابعة.', 'ophthalmology'),
  ('CYCLE-250', 250, 'دورة الحروق والجروح والعناية المتقدمة بالأنسجة', 'clinical', 'إدارة التقييم والقياس والتصنيف والتضميد والجراحة ومتابعة الالتئام.', 'burns'),
  ('CYCLE-251', 251, 'دورة التغذية العلاجية والتغذية المعوية والوريدية', 'clinical', 'إدارة تقييم الحالة التغذوية والخطة والتنفيذ والمراقبة والمضاعفات.', NULL),
  ('CYCLE-252', 252, 'دورة الصحة النفسية وعلاج الإدمان', 'clinical', 'إدارة التقييم النفسي وخطة العلاج والمتابعة وإدارة المخاطر والإحالات بسرية مشددة.', NULL),
  ('CYCLE-253', 253, 'دورة الرعاية المنزلية والتمريض المجتمعي والأجهزة المنزلية', 'clinical', 'تنظيم أهلية الرعاية المنزلية والزيارات والأجهزة والمتابعة والتصعيد.', NULL),
  ('CYCLE-254', 254, 'دورة مراكز السموم والتعرضات الكيميائية والدوائية', 'clinical', 'إدارة البلاغ والتقييم والاستشارة والتوجيه والإحالة ومتابعة التعرضات.', 'poison-center')
ON CONFLICT (code) DO NOTHING;
