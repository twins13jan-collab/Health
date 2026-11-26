-- V12 — Batch 11 (Catalog v6): dentistry & oral health 248,
-- poison centres & chemical/drug exposures 254.
-- Every clinical table is facility-scoped with the platform RLS pattern.

-- ============ 248 Dentistry ============
CREATE TABLE IF NOT EXISTS dental_charts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  version INT NOT NULL DEFAULT 1,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','superseded')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_dental_chart_patient ON dental_charts (patient_id, version DESC);

CREATE TABLE IF NOT EXISTS dental_chart_entries (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  chart_id UUID NOT NULL REFERENCES dental_charts(id),
  tooth_number INT NOT NULL, -- FDI two-digit notation
  condition TEXT, surfaces TEXT, notes TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS dental_treatment_plans (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  diagnosis TEXT NOT NULL, plan TEXT,
  consent_documented BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'proposed' CHECK (status IN ('proposed','approved','in_progress','completed','cancelled')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS sterilization_cycles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  instrument_set TEXT NOT NULL, cycle_number TEXT,
  status TEXT NOT NULL CHECK (status IN ('passed','failed')),
  sterilized_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  expires_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS dental_procedures (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  plan_id UUID REFERENCES dental_treatment_plans(id),
  tooth_number INT NOT NULL,
  site_verified BOOLEAN NOT NULL DEFAULT false,
  procedure_code TEXT NOT NULL,
  anesthesia_type TEXT, anesthesia_risk BOOLEAN NOT NULL DEFAULT false,
  physician_reviewed BOOLEAN NOT NULL DEFAULT false,
  materials TEXT, material_lot TEXT,
  sterilization_cycle_id UUID REFERENCES sterilization_cycles(id),
  dentist TEXT, complications TEXT, discharge_instructions TEXT,
  status TEXT NOT NULL DEFAULT 'performed' CHECK (status IN ('performed','complicated','cancelled')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS dental_images (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  procedure_id UUID REFERENCES dental_procedures(id),
  image_type TEXT NOT NULL, device TEXT, region TEXT,
  taken_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS implant_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  manufacturer TEXT NOT NULL, lot_number TEXT NOT NULL, serial_number TEXT,
  tooth_number INT,
  status TEXT NOT NULL DEFAULT 'implanted' CHECK (status IN ('implanted','recalled','removed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_implant_lot ON implant_records (lot_number);

-- ============ 254 Toxicology / poison centres ============
CREATE TABLE IF NOT EXISTS exposure_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  location_zone TEXT, affected_count INT NOT NULL DEFAULT 0, suspected_source TEXT,
  status TEXT NOT NULL DEFAULT 'reported' CHECK (status IN ('reported','under_investigation','contained','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS poisoning_cases (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID REFERENCES patients(id),
  exposure_event_id UUID REFERENCES exposure_events(id),
  substance_name TEXT,
  substance_unknown BOOLEAN NOT NULL DEFAULT false,
  container_description TEXT,
  estimated_amount TEXT, amount_uncertain BOOLEAN NOT NULL DEFAULT false,
  route TEXT, exposure_time TIMESTAMPTZ,
  age_years INT, weight_kg DOUBLE PRECISION,
  symptoms TEXT, severity TEXT,
  status TEXT NOT NULL DEFAULT 'reported' CHECK (status IN ('reported','consulted','referred','follow_up','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_poison_case_substance ON poisoning_cases (substance_name);

CREATE TABLE IF NOT EXISTS substance_references (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  name TEXT NOT NULL, concentration TEXT, packaging TEXT,
  source TEXT NOT NULL, review_date DATE NOT NULL,
  active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS toxicology_consultations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  case_id UUID NOT NULL REFERENCES poisoning_cases(id),
  consultant TEXT, assessment TEXT, recommendation TEXT,
  reference_id UUID REFERENCES substance_references(id),
  center_unavailable BOOLEAN NOT NULL DEFAULT false,
  alternative_path TEXT,
  contact_time TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS referral_outcomes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  case_id UUID NOT NULL REFERENCES poisoning_cases(id),
  receiving_facility TEXT NOT NULL, transport TEXT,
  outcome TEXT, follow_up TEXT,
  status TEXT NOT NULL DEFAULT 'initiated' CHECK (status IN ('initiated','arrived','outcome_recorded')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['dental_charts','dental_chart_entries','dental_treatment_plans','sterilization_cycles','dental_procedures','dental_images','implant_records',
                           'exposure_events','poisoning_cases','substance_references','toxicology_consultations','referral_outcomes']
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
