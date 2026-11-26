-- V38 — Module deepening round 14 (Catalog v6): patient-reported
-- outcomes & experience PROMs/PREMs 170; temporary workforce
-- marketplace 171. Facility-scoped, platform RLS pattern.

-- ============ 170 PROMs / PREMs ============
CREATE TABLE IF NOT EXISTS prom_instruments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  name TEXT, version TEXT, language TEXT,
  validated BOOLEAN NOT NULL DEFAULT false,
  domain TEXT CHECK (domain IN ('outcome','experience')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS prom_schedules (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT, instrument_id UUID NOT NULL REFERENCES prom_instruments(id),
  care_pathway TEXT, timing_point TEXT CHECK (timing_point IN ('baseline','discharge','day_30','day_90')),
  due_at DATE,
  status TEXT NOT NULL DEFAULT 'scheduled' CHECK (status IN ('scheduled','responded','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS prom_responses (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  schedule_id UUID NOT NULL REFERENCES prom_schedules(id),
  score NUMERIC(6,2), urgent_signal BOOLEAN NOT NULL DEFAULT false,
  urgent_followed_up BOOLEAN NOT NULL DEFAULT false,
  responded_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS prom_analyses (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  care_pathway TEXT, period TEXT, sample_size INT, response_rate_pct NUMERIC(5,2),
  mean_score NUMERIC(6,2), bias_note TEXT, published BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 171 Temporary workforce marketplace ============
CREATE TABLE IF NOT EXISTS temp_providers (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  provider_ref TEXT, profession TEXT, license_no TEXT,
  license_valid_until DATE,
  identity_verified BOOLEAN NOT NULL DEFAULT false,
  qualifications_verified BOOLEAN NOT NULL DEFAULT false,
  practice_scope TEXT,
  status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending','verified','suspended')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS temp_availability (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  provider_id UUID NOT NULL REFERENCES temp_providers(id),
  available_from TIMESTAMPTZ, available_to TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS temp_assignments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  provider_id UUID NOT NULL REFERENCES temp_providers(id),
  department TEXT, scope_of_practice TEXT,
  shift_start TIMESTAMPTZ, shift_end TIMESTAMPTZ,
  status TEXT NOT NULL DEFAULT 'offered' CHECK (status IN ('offered','accepted','completed','cancelled')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS temp_evaluations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  assignment_id UUID NOT NULL REFERENCES temp_assignments(id),
  rating INT, note TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['prom_instruments','prom_schedules','prom_responses','prom_analyses',
                           'temp_providers','temp_availability','temp_assignments','temp_evaluations']
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
