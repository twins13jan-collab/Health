-- V9 — Batch 8 (Catalog v6): ICU & device monitoring 221, maternity &
-- pregnancy/delivery/postpartum 244, newborn screening & NICU 245.
-- Every clinical table is facility-scoped with the platform RLS pattern.

-- ============ 221 ICU ============
CREATE TABLE IF NOT EXISTS icu_stays (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  encounter_id UUID,
  admission_reason TEXT NOT NULL,
  care_level TEXT NOT NULL DEFAULT 'level_3' CHECK (care_level IN ('level_2','level_3')),
  team TEXT, primary_diagnosis TEXT,
  reference_weight_kg DOUBLE PRECISION,
  weight_unit TEXT NOT NULL DEFAULT 'kg' CHECK (weight_unit IN ('kg','lb')),
  weight_verified BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','discharged','transferred','deceased')),
  admitted_at TIMESTAMPTZ NOT NULL DEFAULT now(), ended_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_icu_stays_patient ON icu_stays (patient_id, admitted_at);

CREATE TABLE IF NOT EXISTS icu_observations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  stay_id UUID NOT NULL REFERENCES icu_stays(id),
  kind TEXT NOT NULL, value DOUBLE PRECISION NOT NULL, unit TEXT,
  device_code TEXT,
  source_time TIMESTAMPTZ NOT NULL, received_time TIMESTAMPTZ NOT NULL DEFAULT now(),
  quality TEXT NOT NULL DEFAULT 'good' CHECK (quality IN ('good','suspect','artifact')),
  recorded_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_icu_obs_stay ON icu_observations (stay_id, source_time DESC);

CREATE TABLE IF NOT EXISTS ventilation_episodes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  stay_id UUID NOT NULL REFERENCES icu_stays(id),
  device_code TEXT NOT NULL, mode TEXT NOT NULL,
  fio2 DOUBLE PRECISION, peep DOUBLE PRECISION, tidal_volume_ml DOUBLE PRECISION, rate_per_min DOUBLE PRECISION,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','ended')),
  started_at TIMESTAMPTZ NOT NULL DEFAULT now(), ended_at TIMESTAMPTZ,
  started_by TEXT, notes TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS icu_infusions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  stay_id UUID NOT NULL REFERENCES icu_stays(id),
  drug_code TEXT NOT NULL, concentration TEXT,
  dose_rate DOUBLE PRECISION NOT NULL, rate_unit TEXT NOT NULL,
  reference_weight_kg DOUBLE PRECISION,
  status TEXT NOT NULL DEFAULT 'running' CHECK (status IN ('running','stopped','completed')),
  started_at TIMESTAMPTZ NOT NULL DEFAULT now(), ended_at TIMESTAMPTZ,
  started_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS escalation_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  stay_id UUID NOT NULL REFERENCES icu_stays(id),
  reason TEXT NOT NULL, team_called TEXT NOT NULL,
  called_at TIMESTAMPTZ NOT NULL DEFAULT now(), responded_at TIMESTAMPTZ,
  decision TEXT, status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','responded','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 244 Maternity ============
CREATE TABLE IF NOT EXISTS pregnancy_episodes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  lmp_date DATE, edd DATE, dating_source TEXT CHECK (dating_source IN ('lmp','ultrasound','clinical')),
  fetus_count INT NOT NULL DEFAULT 1 CHECK (fetus_count >= 1),
  risk_level TEXT NOT NULL DEFAULT 'low' CHECK (risk_level IN ('low','high','critical')),
  risk_factors TEXT,
  status TEXT NOT NULL DEFAULT 'ongoing' CHECK (status IN ('ongoing','in_labor','delivered','postpartum','closed','referred')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_pregnancy_patient ON pregnancy_episodes (patient_id, created_at DESC);

CREATE TABLE IF NOT EXISTS antenatal_visits (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  pregnancy_id UUID NOT NULL REFERENCES pregnancy_episodes(id),
  visit_date DATE NOT NULL DEFAULT CURRENT_DATE,
  gestational_weeks INT, bp_systolic INT, bp_diastolic INT,
  weight_kg DOUBLE PRECISION, fundal_height_cm DOUBLE PRECISION, fetal_heart_rate INT,
  proteinuria BOOLEAN NOT NULL DEFAULT false, symptoms TEXT,
  labs_summary TEXT, education TEXT, plan TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS labor_episodes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  pregnancy_id UUID NOT NULL REFERENCES pregnancy_episodes(id),
  started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  cervix_dilation_cm DOUBLE PRECISION, team TEXT,
  status TEXT NOT NULL DEFAULT 'in_labor' CHECK (status IN ('in_labor','delivered','transferred','emergency_csection')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS delivery_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  pregnancy_id UUID NOT NULL REFERENCES pregnancy_episodes(id),
  labor_id UUID REFERENCES labor_episodes(id),
  delivery_type TEXT NOT NULL CHECK (delivery_type IN ('vaginal','assisted','cesarean')),
  delivered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  blood_loss_ml INT NOT NULL DEFAULT 0,
  complications TEXT, newborn_count INT NOT NULL DEFAULT 1,
  pph BOOLEAN NOT NULL DEFAULT false,
  signed_off BOOLEAN NOT NULL DEFAULT false, signed_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS postpartum_assessments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  pregnancy_id UUID NOT NULL REFERENCES pregnancy_episodes(id),
  assessed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  bleeding_status TEXT, bp_systolic INT, bp_diastolic INT, pain_score INT,
  breastfeeding TEXT, mental_state TEXT, danger_signs TEXT,
  discharge_ready BOOLEAN NOT NULL DEFAULT false, discharge_instructions TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 245 Neonatal ============
CREATE TABLE IF NOT EXISTS newborn_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  mother_patient_id UUID NOT NULL REFERENCES patients(id),
  pregnancy_id UUID REFERENCES pregnancy_episodes(id),
  delivery_id UUID REFERENCES delivery_records(id),
  patient_id UUID REFERENCES patients(id),
  birth_order INT NOT NULL DEFAULT 1,
  temporary_identifier BOOLEAN NOT NULL DEFAULT false,
  sex TEXT CHECK (sex IN ('male','female')),
  born_at TIMESTAMPTZ NOT NULL DEFAULT now(), birth_place TEXT,
  birth_weight_g INT, length_cm DOUBLE PRECISION, head_circumference_cm DOUBLE PRECISION,
  gestational_weeks INT, apgar_1min INT, apgar_5min INT,
  status TEXT NOT NULL DEFAULT 'with_mother' CHECK (status IN ('with_mother','nicu','transferred','deceased','discharged')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_newborn_mother ON newborn_records (mother_patient_id, born_at);

CREATE TABLE IF NOT EXISTS neonatal_screenings (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  newborn_id UUID NOT NULL REFERENCES newborn_records(id),
  screening_type TEXT NOT NULL,
  sample_at TIMESTAMPTZ,
  result TEXT NOT NULL DEFAULT 'pending' CHECK (result IN ('pending','normal','abnormal','invalid')),
  result_at TIMESTAMPTZ,
  recollect_required BOOLEAN NOT NULL DEFAULT false,
  referral_status TEXT NOT NULL DEFAULT 'none' CHECK (referral_status IN ('none','open','closed')),
  referral_outcome TEXT, acknowledged_by TEXT, acknowledged_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS nicu_stays (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  newborn_id UUID NOT NULL REFERENCES newborn_records(id),
  reason TEXT NOT NULL, respiratory_support TEXT, feeding_type TEXT,
  handover_notes TEXT,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','discharged','transferred','deceased')),
  started_at TIMESTAMPTZ NOT NULL DEFAULT now(), ended_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['icu_stays','icu_observations','ventilation_episodes','icu_infusions','escalation_events',
                           'pregnancy_episodes','antenatal_visits','labor_episodes','delivery_records','postpartum_assessments',
                           'newborn_records','neonatal_screenings','nicu_stays']
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
