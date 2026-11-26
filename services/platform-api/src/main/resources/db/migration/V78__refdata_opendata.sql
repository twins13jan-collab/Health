-- V78 — Module deepening round 51 (Catalog v6, tier B):
-- reference-data quality & semantic linking 182; open-data
-- governance & statistics 188. Facility-scoped, platform RLS.

-- ============ 182 Reference data quality ============
CREATE TABLE IF NOT EXISTS code_mappings (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  local_code TEXT, reference_code TEXT, code_system TEXT,
  sensitive BOOLEAN NOT NULL DEFAULT false, human_reviewed BOOLEAN NOT NULL DEFAULT false,
  active BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS mapping_changes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  mapping_id UUID NOT NULL REFERENCES code_mappings(id),
  old_reference_code TEXT, new_reference_code TEXT, reason TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 188 Open data ============
CREATE TABLE IF NOT EXISTS open_datasets (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  name TEXT, min_cell_size INT,
  reidentification_risk_assessed BOOLEAN NOT NULL DEFAULT false,
  anonymized BOOLEAN NOT NULL DEFAULT false,
  definitions_documented BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft','published','withdrawn')),
  withdrawn_reason TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['code_mappings','mapping_changes','open_datasets']
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
