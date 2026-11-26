-- V50 — Module deepening round 25 (Catalog v6, tier C): health
-- services for special populations & sites 70; health equity &
-- service access 74. Facility-scoped, platform RLS pattern.

-- ============ 70 Special populations & sites ============
CREATE TABLE IF NOT EXISTS special_service_points (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  site_type TEXT CHECK (site_type IN ('school','university','youth_centre','sports','detention','refugee','port','event')),
  name TEXT, safeguards_verified BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'planned' CHECK (status IN ('planned','active','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS detention_care_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  point_id UUID NOT NULL REFERENCES special_service_points(id),
  patient_ref TEXT, legal_safeguards_confirmed BOOLEAN NOT NULL DEFAULT false,
  rights_informed BOOLEAN NOT NULL DEFAULT false, care_summary TEXT,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS mobile_population_encounters (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  point_id UUID NOT NULL REFERENCES special_service_points(id),
  person_ref TEXT,
  barrier_flag TEXT CHECK (barrier_flag IN ('none','documents','fees','language','distance')),
  care_provided BOOLEAN NOT NULL DEFAULT false,
  denial_review_required BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 74 Health equity & access ============
CREATE TABLE IF NOT EXISTS equity_measurements (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  region TEXT, population INT, service_units INT,
  disease_burden_index DOUBLE PRECISION,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS access_gap_analyses (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  region TEXT, dimension TEXT, gap_pct DOUBLE PRECISION, sample_size INT,
  published BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS coverage_programs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  region TEXT, program_type TEXT, target_gap_pct DOUBLE PRECISION,
  latest_measured_gap_pct DOUBLE PRECISION,
  status TEXT NOT NULL DEFAULT 'running' CHECK (status IN ('running','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['special_service_points','detention_care_records','mobile_population_encounters',
                           'equity_measurements','access_gap_analyses','coverage_programs']
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
