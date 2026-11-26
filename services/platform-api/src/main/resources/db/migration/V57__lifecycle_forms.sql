-- V57 — Module deepening round 31 (Catalog v6, tier C): software
-- lifecycle & releases 135; structured clinical forms &
-- documents 138. Facility-scoped, platform RLS pattern.

-- ============ 135 Software lifecycle ============
CREATE TABLE IF NOT EXISTS software_components (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  name TEXT, kind TEXT CHECK (kind IN ('application','service','component')),
  third_party BOOLEAN NOT NULL DEFAULT false, licence_ref TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS software_releases (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  component_id UUID NOT NULL REFERENCES software_components(id),
  version TEXT, environment TEXT CHECK (environment IN ('dev','test','staging','production')),
  code_reviewed BOOLEAN NOT NULL DEFAULT false, build_signed BOOLEAN NOT NULL DEFAULT false,
  config_drift BOOLEAN NOT NULL DEFAULT false, rollback_reason TEXT,
  status TEXT NOT NULL DEFAULT 'built' CHECK (status IN ('built','deployed','rolled_back')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 138 Structured clinical forms ============
CREATE TABLE IF NOT EXISTS form_templates (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  name TEXT, specialty TEXT, fields_spec TEXT, version TEXT,
  status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft','active','retired')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS form_instances (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  template_id UUID NOT NULL REFERENCES form_templates(id),
  patient_ref TEXT, author_ref TEXT, signed BOOLEAN NOT NULL DEFAULT false,
  structured_data BOOLEAN NOT NULL DEFAULT false, supersedes_id UUID,
  status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft','filed','superseded')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['software_components','software_releases','form_templates','form_instances']
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
