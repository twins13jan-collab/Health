-- V44 — Module deepening round 20 (Catalog v6, tier C): social &
-- psychological services 99; non-emergency patient transport 154.
-- Facility-scoped, platform RLS pattern.

-- ============ 99 Social & psychological services ============
CREATE TABLE IF NOT EXISTS social_assessments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT, support_needs TEXT,
  risk_level TEXT CHECK (risk_level IN ('none','moderate','high','crisis')),
  safeguarding_concern TEXT CHECK (safeguarding_concern IN ('none','child','vulnerable_adult')),
  safety_plan_created BOOLEAN NOT NULL DEFAULT false,
  assessed_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS support_plans (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  assessment_id UUID NOT NULL REFERENCES social_assessments(id),
  actions TEXT, follow_up_date DATE,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS safeguarding_referrals (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  assessment_id UUID NOT NULL REFERENCES social_assessments(id),
  concern_type TEXT, referred_to TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 154 Non-emergency patient transport ============
CREATE TABLE IF NOT EXISTS transport_providers (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  name TEXT, verified BOOLEAN NOT NULL DEFAULT false,
  licence_valid_until DATE, oxygen_capable BOOLEAN NOT NULL DEFAULT false,
  stretcher_capable BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS transport_requests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT,
  purpose TEXT CHECK (purpose IN ('dialysis','therapy','rehab','appointment')),
  need_type TEXT CHECK (need_type IN ('seated','wheelchair','stretcher')),
  oxygen_needed BOOLEAN NOT NULL DEFAULT false,
  scheduled_at TIMESTAMPTZ, provider_id UUID REFERENCES transport_providers(id),
  status TEXT NOT NULL DEFAULT 'requested' CHECK (status IN ('requested','scheduled','completed','cancelled')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS transport_trips (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  request_id UUID NOT NULL REFERENCES transport_requests(id),
  departed_at TIMESTAMPTZ, arrived_at TIMESTAMPTZ,
  delay_minutes INT, completed BOOLEAN NOT NULL DEFAULT false,
  complaint TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['social_assessments','support_plans','safeguarding_referrals',
                           'transport_providers','transport_requests','transport_trips']
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
