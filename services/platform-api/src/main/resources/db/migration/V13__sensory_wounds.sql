-- V13 — Batch 12 (Catalog v6): ophthalmology, audiology & ENT 249,
-- burns, wounds & advanced tissue care 250.
-- Every clinical table is facility-scoped with the platform RLS pattern.

-- ============ 249 Sensory (eyes / hearing / ENT) ============
CREATE TABLE IF NOT EXISTS sensory_assessments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  organ TEXT NOT NULL CHECK (organ IN ('eye','ear','nose_throat')),
  side TEXT NOT NULL CHECK (side IN ('left','right','bilateral')),
  measurements TEXT, method TEXT,
  device_code TEXT, unit TEXT,
  supersedes_id UUID REFERENCES sensory_assessments(id),
  status TEXT NOT NULL DEFAULT 'valid' CHECK (status IN ('valid','invalid_calibration','corrected')),
  measured_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_sensory_patient ON sensory_assessments (patient_id, created_at DESC);

CREATE TABLE IF NOT EXISTS ophthalmic_exams (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  eye TEXT NOT NULL CHECK (eye IN ('left','right')),
  visual_acuity DOUBLE PRECISION,
  iop_mmhg DOUBLE PRECISION,
  iop_suspect BOOLEAN NOT NULL DEFAULT false,
  fundus TEXT, visual_field TEXT, examiner TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS audiology_tests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  ear TEXT NOT NULL CHECK (ear IN ('left','right','bilateral')),
  test_type TEXT NOT NULL, device_code TEXT,
  calibration_valid BOOLEAN NOT NULL DEFAULT false,
  calibration_date DATE,
  result_summary TEXT,
  status TEXT NOT NULL DEFAULT 'valid' CHECK (status IN ('valid','invalid_calibration')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS ent_procedures (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  procedure_code TEXT NOT NULL, site TEXT, side TEXT,
  anesthesia TEXT, complications TEXT,
  status TEXT NOT NULL DEFAULT 'reported' CHECK (status IN ('reported','complicated','follow_up')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS assistive_devices (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  device_type TEXT NOT NULL, specification TEXT,
  fitting_status TEXT NOT NULL DEFAULT 'pending' CHECK (fitting_status IN ('pending','fitted','refit_required','delivered')),
  fitting_notes TEXT,
  delivered_at TIMESTAMPTZ, maintenance_due DATE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 250 Burns & wounds ============
CREATE TABLE IF NOT EXISTS wound_cases (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  cause TEXT, site TEXT, duration_days INT,
  classification TEXT NOT NULL,
  burn_degree INT CHECK (burn_degree BETWEEN 1 AND 3),
  tbsa_percent DOUBLE PRECISION CHECK (tbsa_percent BETWEEN 0 AND 100),
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','healing_review','closed','referred')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_wound_patient ON wound_cases (patient_id, created_at DESC);

CREATE TABLE IF NOT EXISTS wound_assessments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  case_id UUID NOT NULL REFERENCES wound_cases(id),
  length_cm DOUBLE PRECISION, width_cm DOUBLE PRECISION, depth_cm DOUBLE PRECISION,
  area_cm2 DOUBLE PRECISION,
  tissue_type TEXT, exudate TEXT,
  pain_score INT CHECK (pain_score BETWEEN 0 AND 10),
  infection_suspected BOOLEAN NOT NULL DEFAULT false,
  assessed_by TEXT,
  assessed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_wound_assess_case ON wound_assessments (case_id, assessed_at ASC);

CREATE TABLE IF NOT EXISTS clinical_images (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  case_id UUID NOT NULL REFERENCES wound_cases(id),
  assessment_id UUID REFERENCES wound_assessments(id),
  consent_basis TEXT NOT NULL,
  purpose TEXT, body_site TEXT, photographer TEXT,
  access_policy TEXT NOT NULL DEFAULT 'restricted',
  captured_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS dressing_episodes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  case_id UUID NOT NULL REFERENCES wound_cases(id),
  material TEXT NOT NULL, material_lot TEXT NOT NULL,
  lot_status TEXT NOT NULL DEFAULT 'ok' CHECK (lot_status IN ('ok','expired','recalled')),
  quantity INT NOT NULL DEFAULT 1,
  applied_by TEXT, response TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS burn_resuscitation_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  case_id UUID NOT NULL REFERENCES wound_cases(id),
  weight_kg DOUBLE PRECISION NOT NULL, tbsa_percent DOUBLE PRECISION NOT NULL,
  planned_ml_24h DOUBLE PRECISION NOT NULL, planned_ml_first_8h DOUBLE PRECISION NOT NULL,
  actual_ml DOUBLE PRECISION, urine_output_ml_hr DOUBLE PRECISION,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['sensory_assessments','ophthalmic_exams','audiology_tests','ent_procedures','assistive_devices',
                           'wound_cases','wound_assessments','clinical_images','dressing_episodes','burn_resuscitation_records']
  LOOP
    EXECUTE format('ALTER TABLE %I ENABLE ROW LEVEL SECURITY', t);
    EXECUTE format('ALTER TABLE %I FORCE ROW LEVEL SECURITY', t);
    EXECUTE format('DROP POLICY IF EXISTS %I ON %I', t || '_facility_isolation', t);
    EXECUTE format($p$CREATE POLICY %I ON %I FOR ALL
      USING (current_setting('app.current_facility_id', true) = 'national'
             OR facility_id::text = current_setting('app.current_facility_id', true)
             OR (current_setting('app.current_facility_id', true) LIKE 'gov:%%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)))
      WITH CHECK (current_setting('app.current_facility_id', true) = 'national'
             OR facility_id::text = current_setting('app.current_facility_id', true)
             OR (current_setting('app.current_facility_id', true) LIKE 'gov:%%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)))$p$,
      t || '_facility_isolation', t);
    EXECUTE format('GRANT SELECT, INSERT, UPDATE, DELETE ON %I TO app_user', t);
  END LOOP;
END $$;
