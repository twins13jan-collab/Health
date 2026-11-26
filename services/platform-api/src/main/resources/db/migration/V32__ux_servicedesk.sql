-- V32 — Module deepening round 9 (Catalog v6): user requirements &
-- UX governance 136; service centre & technical support 139.
-- Facility-scoped, platform RLS pattern.

-- ============ 136 User requirements & UX ============
CREATE TABLE IF NOT EXISTS ux_scenarios (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  user_role TEXT, scenario_name TEXT,
  critical_task BOOLEAN NOT NULL DEFAULT false,
  target_seconds INT, measured_seconds INT,
  status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft','testing','validated','needs_work')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS usability_test_sessions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  scenario_id UUID NOT NULL REFERENCES ux_scenarios(id),
  tester TEXT, participant_role TEXT,
  task_completed BOOLEAN NOT NULL DEFAULT false,
  completion_seconds INT, errors_count INT,
  notes TEXT, satisfaction_score NUMERIC(3,1),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS ux_findings (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  scenario_id UUID NOT NULL REFERENCES ux_scenarios(id),
  severity TEXT CHECK (severity IN ('blocker','major','minor')),
  description TEXT, resolution TEXT,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','addressed','wont_fix')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 139 Service centre & technical support ============
CREATE TABLE IF NOT EXISTS support_tickets (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  reporter TEXT, service_ref TEXT, device_ref TEXT,
  category TEXT CHECK (category IN ('hardware','connectivity','printing','barcode','software','account','security','clinical_safety')),
  severity TEXT CHECK (severity IN ('low','medium','high','critical')),
  track TEXT CHECK (track IN ('standard','security','clinical')),
  description TEXT, assigned_to TEXT,
  first_response_at TIMESTAMPTZ, resolved_at TIMESTAMPTZ,
  resolution TEXT, satisfaction_score INT,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','assigned','escalated','resolved','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS kb_articles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  title TEXT, category TEXT, solution TEXT,
  published BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['ux_scenarios','usability_test_sessions','ux_findings','support_tickets','kb_articles']
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
