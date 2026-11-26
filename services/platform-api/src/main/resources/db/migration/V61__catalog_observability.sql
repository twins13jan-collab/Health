-- V61 — Module deepening round 35 (Catalog v6, tier C): unified
-- definitions & service catalogs 78; national observability &
-- operational transparency 62. Facility-scoped, platform RLS.

-- ============ 78 Service catalogs ============
CREATE TABLE IF NOT EXISTS catalog_services (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  code TEXT, name TEXT, category TEXT, resource_requirements TEXT, delivery_location TEXT,
  price DOUBLE PRECISION, insurance_coverage TEXT,
  effective_from DATE, effective_to DATE,
  status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft','active','retired')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS catalog_changes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  service_id UUID NOT NULL REFERENCES catalog_services(id),
  change_description TEXT, approved_by TEXT, effective_from DATE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 62 Observability ============
CREATE TABLE IF NOT EXISTS observed_components (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  component_ref TEXT,
  component_type TEXT CHECK (component_type IN ('service','api','database','queue','integration','device')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS component_metrics (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  component_id UUID NOT NULL REFERENCES observed_components(id),
  metric TEXT, value DOUBLE PRECISION, threshold DOUBLE PRECISION,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS ops_incidents (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  component_id UUID NOT NULL REFERENCES observed_components(id),
  severity TEXT CHECK (severity IN ('minor','major','critical')),
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','escalated','closed')),
  resolution_note TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['catalog_services','catalog_changes','observed_components','component_metrics','ops_incidents']
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
