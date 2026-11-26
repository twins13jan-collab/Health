-- V55 — Module deepening round 30 (Catalog v6, tier C): data
-- quality, definitions & indicators 122; facilities, energy &
-- infrastructure 108. Facility-scoped, platform RLS pattern.

-- ============ 122 Data quality & indicators ============
CREATE TABLE IF NOT EXISTS data_assets (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  name TEXT, owner_ref TEXT, source_of_truth TEXT,
  completeness_pct DOUBLE PRECISION, validity_pct DOUBLE PRECISION,
  consistency_pct DOUBLE PRECISION, freshness_hours DOUBLE PRECISION,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS indicator_definitions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  name TEXT, definition TEXT, numerator TEXT, denominator TEXT, owner_ref TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS indicator_publications (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  indicator_id UUID NOT NULL REFERENCES indicator_definitions(id),
  asset_id UUID NOT NULL REFERENCES data_assets(id),
  value DOUBLE PRECISION, confidence_score DOUBLE PRECISION, caveat TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS data_corrections (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  asset_id UUID NOT NULL REFERENCES data_assets(id),
  issue_type TEXT CHECK (issue_type IN ('missing','conflicting','duplicate')),
  description TEXT, resolution_basis TEXT, corrected BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 108 Facilities, energy & infrastructure ============
CREATE TABLE IF NOT EXISTS infra_systems (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  system_type TEXT CHECK (system_type IN ('power','water','fuel','cooling','ventilation','medical_gas','air_quality')),
  name TEXT, critical BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'operational' CHECK (status IN ('operational','degraded','down')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS infra_readings (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  system_id UUID NOT NULL REFERENCES infra_systems(id),
  metric TEXT, value DOUBLE PRECISION, within_limits BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS infra_work_orders (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  system_id UUID NOT NULL REFERENCES infra_systems(id),
  criticality TEXT CHECK (criticality IN ('critical','routine')),
  description TEXT, fallback_plan TEXT,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['data_assets','indicator_definitions','indicator_publications','data_corrections',
                           'infra_systems','infra_readings','infra_work_orders']
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
