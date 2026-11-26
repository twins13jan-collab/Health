-- V23 — Module deepening round 1 (Catalog v6): cardiology, cath lab &
-- vascular interventions 87; stroke & acute neurology 88.
-- Facility-scoped with the platform RLS pattern.

-- ============ 87 Cardiology & cath ============
CREATE TABLE IF NOT EXISTS chest_pain_pathways (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT, arrival_at TIMESTAMPTZ NOT NULL,
  ecg_at TIMESTAMPTZ, ecg_finding TEXT CHECK (ecg_finding IN ('STEMI','NSTEMI','other')),
  troponin_result TEXT, troponin_at TIMESTAMPTZ,
  pathway TEXT CHECK (pathway IN ('primary_pci','thrombolysis','medical')),
  balloon_at TIMESTAMPTZ,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','in_lab','completed','transferred')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS cath_bookings (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  pathway_id UUID REFERENCES chest_pain_pathways(id),
  patient_ref TEXT, lab TEXT, scheduled_at TIMESTAMPTZ,
  consent_confirmed BOOLEAN NOT NULL DEFAULT false,
  allergy_checked BOOLEAN NOT NULL DEFAULT false,
  renal_function TEXT, antiplatelet_loaded BOOLEAN NOT NULL DEFAULT false,
  team_confirmed BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'booked' CHECK (status IN ('booked','prepared','in_procedure','done','cancelled')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS cath_procedures (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  booking_id UUID NOT NULL REFERENCES cath_bookings(id),
  findings TEXT, operator TEXT,
  contrast_volume_ml INT,
  started_at TIMESTAMPTZ, ended_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS cath_implants (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  procedure_id UUID NOT NULL REFERENCES cath_procedures(id),
  item_type TEXT NOT NULL CHECK (item_type IN ('stent','pacemaker','icd','crt','valve','other_device')),
  description TEXT, serial_lot TEXT NOT NULL,
  cost_ref TEXT, claim_ref TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS cath_complications (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  procedure_id UUID NOT NULL REFERENCES cath_procedures(id),
  kind TEXT NOT NULL CHECK (kind IN ('bleeding','aki','arrhythmia','other')),
  severity TEXT, management TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS cardiac_rehab_enrollments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT, program TEXT,
  statin BOOLEAN NOT NULL DEFAULT false, antiplatelet BOOLEAN NOT NULL DEFAULT false,
  beta_blocker BOOLEAN NOT NULL DEFAULT false, smoking_cessation BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'enrolled' CHECK (status IN ('enrolled','in_program','completed','dropped')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 88 Stroke ============
CREATE TABLE IF NOT EXISTS stroke_activations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT,
  last_known_well_at TIMESTAMPTZ NOT NULL, arrival_at TIMESTAMPTZ NOT NULL,
  nihss INT, ct_at TIMESTAMPTZ, ct_result TEXT,
  hemorrhage_excluded BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'activated' CHECK (status IN ('activated','imaged','decided','treated','transferred','admitted')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS thrombolysis_decisions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  activation_id UUID NOT NULL REFERENCES stroke_activations(id),
  recent_surgery BOOLEAN NOT NULL DEFAULT false,
  active_bleeding BOOLEAN NOT NULL DEFAULT false,
  uncontrolled_bp BOOLEAN NOT NULL DEFAULT false,
  anticoagulant_use BOOLEAN NOT NULL DEFAULT false,
  low_glucose_mimic BOOLEAN NOT NULL DEFAULT false,
  decision TEXT NOT NULL CHECK (decision IN ('given','withheld')),
  reason TEXT, decided_by TEXT, needle_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS stroke_transfers (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  activation_id UUID NOT NULL REFERENCES stroke_activations(id),
  destination_center TEXT, acceptance_documented BOOLEAN NOT NULL DEFAULT false,
  accepted_by TEXT, departed_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS stroke_monitoring (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  activation_id UUID NOT NULL REFERENCES stroke_activations(id),
  swallow_screen TEXT CHECK (swallow_screen IN ('passed','failed','not_done')),
  oral_intake BOOLEAN NOT NULL DEFAULT false,
  consciousness TEXT, mobility TEXT, hemorrhage_flag BOOLEAN NOT NULL DEFAULT false,
  notes TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS stroke_discharge_plans (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  activation_id UUID NOT NULL REFERENCES stroke_activations(id),
  rehab_referral TEXT, secondary_prevention TEXT, follow_up TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['chest_pain_pathways','cath_bookings','cath_procedures','cath_implants','cath_complications','cardiac_rehab_enrollments',
                           'stroke_activations','thrombolysis_decisions','stroke_transfers','stroke_monitoring','stroke_discharge_plans']
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
