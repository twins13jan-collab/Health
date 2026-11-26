-- V68 — Module deepening round 41 (Catalog v6, tier B):
-- outpatient clinics & specialist care 83; admission, entry,
-- transfer & discharge 84. Facility-scoped, platform RLS.

-- ============ 83 Outpatient clinics ============
CREATE TABLE IF NOT EXISTS clinic_visits (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT, specialty TEXT,
  visit_type TEXT CHECK (visit_type IN ('in_person','virtual','home','phone')),
  identity_verified BOOLEAN NOT NULL DEFAULT false,
  eligibility_verified BOOLEAN NOT NULL DEFAULT false,
  plan_documented BOOLEAN NOT NULL DEFAULT false,
  followup_owner TEXT,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','closed','no_show')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS clinic_no_shows (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  visit_id UUID NOT NULL REFERENCES clinic_visits(id),
  reschedule_offered BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 84 Admissions & discharge ============
CREATE TABLE IF NOT EXISTS admission_requests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT, justification TEXT, priority TEXT, specialty TEXT,
  transfer BOOLEAN NOT NULL DEFAULT false, receiving_acceptance BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'requested' CHECK (status IN ('requested','admitted','discharged')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS discharge_summaries (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  admission_id UUID NOT NULL REFERENCES admission_requests(id),
  diagnoses TEXT, procedures TEXT, instructions TEXT, warnings TEXT,
  medications_plan TEXT, followup_plan TEXT, finalized BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS ama_discharges (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  admission_id UUID NOT NULL REFERENCES admission_requests(id),
  risks_explained BOOLEAN NOT NULL DEFAULT false, signed BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['clinic_visits','clinic_no_shows','admission_requests','discharge_summaries','ama_discharges']
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
