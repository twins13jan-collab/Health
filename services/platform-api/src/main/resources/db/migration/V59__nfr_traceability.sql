-- V59 — Module deepening round 33 (Catalog v6, tier C):
-- non-functional requirements 176; inter-module traceability
-- matrix 177. Facility-scoped, platform RLS pattern.

-- ============ 176 Non-functional requirements ============
CREATE TABLE IF NOT EXISTS nfr_requirements (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  service_ref TEXT,
  category TEXT CHECK (category IN ('performance','scalability','availability','resilience','privacy','security','audit','retention','compatibility','portability','usability','offline','limits')),
  metric TEXT, target_value DOUBLE PRECISION, comparison TEXT CHECK (comparison IN ('at_most','at_least')),
  critical BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS nfr_measurements (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  requirement_id UUID NOT NULL REFERENCES nfr_requirements(id),
  measured_value DOUBLE PRECISION, measured_on DATE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 177 Traceability matrix ============
CREATE TABLE IF NOT EXISTS data_flows (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  data_entity TEXT, producer_module TEXT, consumer_module TEXT, owner_module TEXT,
  authorized BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS responsibility_transfers (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  data_entity TEXT, from_level TEXT, to_level TEXT, authorized_roles TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['nfr_requirements','nfr_measurements','data_flows','responsibility_transfers']
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
