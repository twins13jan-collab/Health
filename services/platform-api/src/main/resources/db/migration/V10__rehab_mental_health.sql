-- V10 — Batch 9 (Catalog v6): rehabilitation & therapy 246,
-- mental health & addiction 252 (strict confidentiality).
-- Every clinical table is facility-scoped with the platform RLS pattern.

-- ============ 246 Rehabilitation ============
CREATE TABLE IF NOT EXISTS rehab_referrals (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  reason TEXT NOT NULL, diagnosis TEXT, restrictions TEXT,
  referring_physician TEXT,
  status TEXT NOT NULL DEFAULT 'received' CHECK (status IN ('received','assessed','plan_active','discharged','referred','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_rehab_referral_patient ON rehab_referrals (patient_id, created_at DESC);

CREATE TABLE IF NOT EXISTS functional_assessments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  referral_id UUID NOT NULL REFERENCES rehab_referrals(id),
  kind TEXT NOT NULL DEFAULT 'baseline' CHECK (kind IN ('baseline','follow_up')),
  mobility_score INT, balance_score INT, pain_score INT CHECK (pain_score BETWEEN 0 AND 10),
  adl_score INT, cognition_score INT, communication_score INT,
  assessed_by TEXT,
  assessed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS rehab_plans (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  referral_id UUID NOT NULL REFERENCES rehab_referrals(id),
  goals TEXT NOT NULL, baseline_summary TEXT, responsible TEXT,
  target_adl_score INT,
  status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft','approved','active','completed','closed')),
  approved_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS therapy_sessions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  plan_id UUID NOT NULL REFERENCES rehab_plans(id),
  session_type TEXT NOT NULL CHECK (session_type IN ('physical','occupational','speech')),
  intervention TEXT, duration_min INT,
  pain_during INT CHECK (pain_during BETWEEN 0 AND 10),
  response TEXT, incident TEXT NOT NULL DEFAULT 'none',
  status TEXT NOT NULL DEFAULT 'completed' CHECK (status IN ('completed','missed','stopped_escalated')),
  missed_reason TEXT, therapist TEXT,
  session_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS outcome_measures (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  plan_id UUID NOT NULL REFERENCES rehab_plans(id),
  instrument TEXT NOT NULL, instrument_version TEXT NOT NULL,
  score DOUBLE PRECISION NOT NULL, assessor TEXT,
  measured_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS discharge_recommendations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  plan_id UUID NOT NULL REFERENCES rehab_plans(id),
  decision TEXT NOT NULL CHECK (decision IN ('discharge','continue','refer')),
  home_plan TEXT, assistive_devices TEXT, follow_up TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 252 Mental health & addiction (strict confidentiality) ============
CREATE TABLE IF NOT EXISTS mental_health_episodes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  referral_reason TEXT NOT NULL,
  urgency TEXT NOT NULL DEFAULT 'routine' CHECK (urgency IN ('routine','urgent','imminent_risk')),
  provider TEXT,
  diagnosis_code TEXT, -- restricted: never exposed in list views or public boards
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','in_treatment','follow_up','closed','referred')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_mh_episode_patient ON mental_health_episodes (patient_id, created_at DESC);

CREATE TABLE IF NOT EXISTS assessment_instruments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  episode_id UUID NOT NULL REFERENCES mental_health_episodes(id),
  instrument TEXT NOT NULL, instrument_version TEXT NOT NULL,
  total_score INT NOT NULL, severity TEXT,
  item9_positive BOOLEAN NOT NULL DEFAULT false,
  assessor TEXT,
  assessed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS mh_risk_assessments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  episode_id UUID NOT NULL REFERENCES mental_health_episodes(id),
  risk_level TEXT NOT NULL CHECK (risk_level IN ('low','moderate','high','imminent')),
  protective_factors TEXT, safety_plan TEXT,
  safety_protocol_activated BOOLEAN NOT NULL DEFAULT false,
  assessed_by TEXT,
  assessed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS mh_care_plans (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  episode_id UUID NOT NULL REFERENCES mental_health_episodes(id),
  goals TEXT, interventions TEXT, medications_summary TEXT,
  review_date DATE,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('draft','active','completed','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS substance_use_episodes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  episode_id UUID REFERENCES mental_health_episodes(id),
  substance TEXT NOT NULL, use_pattern TEXT,
  ciwa_score INT, withdrawal_band TEXT,
  intervention TEXT,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','in_treatment','follow_up','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['rehab_referrals','functional_assessments','rehab_plans','therapy_sessions','outcome_measures','discharge_recommendations',
                           'mental_health_episodes','assessment_instruments','mh_risk_assessments','mh_care_plans','substance_use_episodes']
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
