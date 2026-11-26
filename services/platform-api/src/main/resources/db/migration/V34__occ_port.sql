-- V34 — Module deepening round 11 (Catalog v6): health of workers
-- in high-risk sectors 144; port health & quarantine 145.
-- Facility-scoped, platform RLS pattern.

-- ============ 144 Occupational health for high-risk sectors ============
CREATE TABLE IF NOT EXISTS worker_health_profiles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  worker_code TEXT NOT NULL, sector TEXT,
  risk_level TEXT CHECK (risk_level IN ('high','medium')),
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','suspended','transferred')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_worker_code ON worker_health_profiles (facility_id, worker_code);

CREATE TABLE IF NOT EXISTS occupational_examinations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  worker_id UUID NOT NULL REFERENCES worker_health_profiles(id),
  exam_type TEXT CHECK (exam_type IN ('baseline','periodic','post_exposure','vaccination','training')),
  exposure_type TEXT CHECK (exposure_type IN ('radiation','noise','heat','chemical','biological','general')),
  result TEXT CHECK (result IN ('fit','fit_with_restrictions','unfit','completed')),
  restrictions TEXT, diagnosis_note TEXT,
  examined_at DATE, next_due_at DATE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS exposure_incidents (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  worker_id UUID NOT NULL REFERENCES worker_health_profiles(id),
  exposure_type TEXT, dose_or_level TEXT, occurred_at TIMESTAMPTZ,
  action_taken TEXT, follow_up_exam_done BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS occupational_referrals (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  worker_id UUID NOT NULL REFERENCES worker_health_profiles(id),
  reason TEXT, report_shared BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 145 Port health & quarantine ============
CREATE TABLE IF NOT EXISTS traveler_health_reports (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  traveler_code TEXT NOT NULL, travel_ref TEXT, origin_country TEXT,
  symptoms TEXT, risk_level TEXT CHECK (risk_level IN ('low','medium','high')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS port_screenings (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  report_id UUID NOT NULL REFERENCES traveler_health_reports(id),
  temperature_c NUMERIC(4,1),
  screening_result TEXT CHECK (screening_result IN ('cleared','isolated','referred')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS quarantine_orders (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  report_id UUID NOT NULL REFERENCES traveler_health_reports(id),
  facility_ref TEXT, legal_basis TEXT,
  starts_at TIMESTAMPTZ, ends_at TIMESTAMPTZ,
  released_by TEXT, release_reason TEXT,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','completed','released_early')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS port_coordination_notes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  report_id UUID NOT NULL REFERENCES traveler_health_reports(id),
  authority TEXT, note TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['worker_health_profiles','occupational_examinations','exposure_incidents','occupational_referrals',
                           'traveler_health_reports','port_screenings','quarantine_orders','port_coordination_notes']
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
