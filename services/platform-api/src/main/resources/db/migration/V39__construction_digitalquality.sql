-- V39 — Module deepening round 15 (Catalog v6): health-facility
-- construction & handover 172; digital experience quality 175.
-- Facility-scoped, platform RLS pattern. Closes audit tier D.

-- ============ 172 Facility construction & handover ============
CREATE TABLE IF NOT EXISTS construction_projects (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  name TEXT, location TEXT,
  need_study_ref TEXT, health_map_ref TEXT,
  status TEXT NOT NULL DEFAULT 'planning' CHECK (status IN ('planning','design','construction','testing','handover','operational')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS design_approvals (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  project_id UUID NOT NULL REFERENCES construction_projects(id),
  discipline TEXT CHECK (discipline IN ('medical','engineering','infection_control')),
  approved_by TEXT, approved BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS contract_change_orders (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  project_id UUID NOT NULL REFERENCES construction_projects(id),
  description TEXT, cost_delta NUMERIC(14,2),
  approved_by TEXT, approved BOOLEAN NOT NULL DEFAULT false, applied BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS commissioning_tests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  project_id UUID NOT NULL REFERENCES construction_projects(id),
  system_name TEXT CHECK (system_name IN ('medical_gas','power','hvac','water','it_network')),
  result TEXT CHECK (result IN ('pass','fail')),
  tested_by TEXT, tested_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS handover_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  project_id UUID NOT NULL REFERENCES construction_projects(id),
  assets_received BOOLEAN NOT NULL DEFAULT false,
  documents_received BOOLEAN NOT NULL DEFAULT false,
  licenses_received BOOLEAN NOT NULL DEFAULT false,
  accepted_by TEXT, accepted_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 175 Digital experience quality ============
CREATE TABLE IF NOT EXISTS ux_quality_checks (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  surface TEXT, environment TEXT CHECK (environment IN ('production','demo')),
  task_completion_pct NUMERIC(5,2), error_count INT,
  p95_load_ms INT, api_connectivity_verified BOOLEAN NOT NULL DEFAULT false,
  passed BOOLEAN NOT NULL DEFAULT false,
  checked_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS demo_data_incidents (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  surface TEXT, description TEXT,
  resolved BOOLEAN NOT NULL DEFAULT false,
  detected_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS interface_user_tests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  surface TEXT, participants INT, task_success_pct NUMERIC(5,2), findings TEXT,
  tested_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['construction_projects','design_approvals','contract_change_orders','commissioning_tests','handover_records',
                           'ux_quality_checks','demo_data_incidents','interface_user_tests']
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
