-- V48 — Module deepening round 23 (Catalog v6, tier C):
-- occupational health & worker safety 56; environmental &
-- climate health services 69. Facility-scoped, platform RLS.

-- ============ 56 Occupational health & worker safety ============
CREATE TABLE IF NOT EXISTS worker_health_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  worker_ref TEXT, worker_role TEXT,
  last_periodic_exam_date DATE, vaccinations_up_to_date BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS worker_exposure_incidents (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  worker_ref TEXT,
  exposure_type TEXT CHECK (exposure_type IN ('blood','infection','chemical','radiation','gas','biological')),
  investigation_done BOOLEAN NOT NULL DEFAULT false,
  follow_up_done BOOLEAN NOT NULL DEFAULT false,
  compensation_filed BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'reported' CHECK (status IN ('reported','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS ppe_issues (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  worker_ref TEXT, item TEXT, training_done BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS worker_support_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  worker_ref TEXT, support_type TEXT, notes TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 69 Environmental & climate health ============
CREATE TABLE IF NOT EXISTS env_data_feeds (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  source_type TEXT CHECK (source_type IN ('weather','air_quality','water','pollution')),
  source_name TEXT, reliable BOOLEAN NOT NULL DEFAULT false,
  last_value TEXT, recorded_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS env_risk_alerts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  hazard_type TEXT CHECK (hazard_type IN ('heatwave','flood','storm','pollution')),
  level TEXT CHECK (level IN ('watch','warning','emergency')),
  affected_population TEXT, feed_id UUID REFERENCES env_data_feeds(id),
  response_plan_activated BOOLEAN NOT NULL DEFAULT false,
  published BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS water_safety_checks (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  source_ref TEXT, check_type TEXT CHECK (check_type IN ('drinking','sewage','waste')),
  result TEXT CHECK (result IN ('safe','unsafe')),
  action_taken TEXT,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['worker_health_records','worker_exposure_incidents','ppe_issues','worker_support_records',
                           'env_data_feeds','env_risk_alerts','water_safety_checks']
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
