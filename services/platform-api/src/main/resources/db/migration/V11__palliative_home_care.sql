-- V11 — Batch 10 (Catalog v6): palliative care, pain & end-of-life 247,
-- home care & community nursing 253.
-- Every clinical table is facility-scoped with the platform RLS pattern.

-- ============ 247 Palliative care ============
CREATE TABLE IF NOT EXISTS palliative_assessments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  symptoms TEXT, pain_score INT CHECK (pain_score BETWEEN 0 AND 10),
  functional_status TEXT, needs TEXT, assessed_by TEXT,
  assessed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_palliative_patient ON palliative_assessments (patient_id, assessed_at DESC);

CREATE TABLE IF NOT EXISTS symptom_plans (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  symptom TEXT NOT NULL, goal TEXT, intervention TEXT, responsible TEXT,
  pain_before INT CHECK (pain_before BETWEEN 0 AND 10),
  pain_after INT CHECK (pain_after BETWEEN 0 AND 10),
  side_effects TEXT,
  review_due_at TIMESTAMPTZ,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','responded','escalated','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS goals_of_care (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  preferences TEXT NOT NULL,
  representative_name TEXT,
  representative_verified BOOLEAN NOT NULL DEFAULT false,
  documentation_source TEXT NOT NULL,
  review_date DATE,
  disagreement_parties TEXT, disagreement_legal_basis TEXT,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','contested','superseded','reviewed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS advance_care_documents (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  document_type TEXT NOT NULL, version INT NOT NULL DEFAULT 1,
  legal_verified BOOLEAN NOT NULL DEFAULT false,
  scope TEXT,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','superseded')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS bereavement_supports (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  family_contact TEXT, -- minimum necessary only
  request TEXT, referral TEXT,
  status TEXT NOT NULL DEFAULT 'requested' CHECK (status IN ('requested','referred','in_support','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 253 Home care ============
CREATE TABLE IF NOT EXISTS home_care_referrals (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  reason TEXT NOT NULL, need TEXT,
  zone TEXT, -- coarse location only: the exact home address is never stored here
  safety_requirements TEXT,
  status TEXT NOT NULL DEFAULT 'received' CHECK (status IN ('received','planned','active','closed','referred')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_homecare_patient ON home_care_referrals (patient_id, created_at DESC);

CREATE TABLE IF NOT EXISTS home_care_plans (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  referral_id UUID NOT NULL REFERENCES home_care_referrals(id),
  goals TEXT, frequency TEXT, instructions TEXT, escalation_signs TEXT,
  approved_by TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','completed','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS home_visits (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  plan_id UUID NOT NULL REFERENCES home_care_plans(id),
  scheduled_at TIMESTAMPTZ NOT NULL, team TEXT,
  arrived_at TIMESTAMPTZ, left_at TIMESTAMPTZ,
  services TEXT, outcome TEXT, access_notes TEXT,
  follow_up_required BOOLEAN NOT NULL DEFAULT false,
  escalation_triggered BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'scheduled' CHECK (status IN ('scheduled','in_progress','completed','missed_no_access','reassigned')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS home_devices (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  device_type TEXT NOT NULL, serial_number TEXT NOT NULL,
  condition_on_delivery TEXT NOT NULL,
  maintenance_due DATE,
  delivered_at TIMESTAMPTZ NOT NULL DEFAULT now(), retrieved_at TIMESTAMPTZ,
  status TEXT NOT NULL DEFAULT 'delivered' CHECK (status IN ('delivered','in_use','maintenance','retrieved')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_home_device_serial_active ON home_devices (facility_id, serial_number) WHERE status <> 'retrieved';

CREATE TABLE IF NOT EXISTS remote_observations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  device_code TEXT, kind TEXT NOT NULL, value DOUBLE PRECISION NOT NULL, unit TEXT,
  source_time TIMESTAMPTZ NOT NULL, received_time TIMESTAMPTZ NOT NULL DEFAULT now(),
  quality TEXT NOT NULL DEFAULT 'good',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_remote_obs_patient ON remote_observations (patient_id, source_time DESC);

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['palliative_assessments','symptom_plans','goals_of_care','advance_care_documents','bereavement_supports',
                           'home_care_referrals','home_care_plans','home_visits','home_devices','remote_observations']
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
