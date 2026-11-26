-- V79 — Module deepening round 52 (Catalog v6, tier B FINALE):
-- vendor monitoring & supply-chain risk 189; facility
-- operational readiness for services 190. Facility-scoped,
-- platform RLS pattern.

-- ============ 189 Vendor monitoring ============
CREATE TABLE IF NOT EXISTS vendor_performance (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  vendor_ref TEXT, on_time_percent DOUBLE PRECISION, quality_score DOUBLE PRECISION,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS exceptional_purchases (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  item_ref TEXT, vendor_ref TEXT, reason TEXT,
  documented BOOLEAN NOT NULL DEFAULT false, reviewed BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 190 Facility readiness ============
CREATE TABLE IF NOT EXISTS readiness_assessments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  service_ref TEXT,
  licences_ok BOOLEAN NOT NULL DEFAULT false, staff_ok BOOLEAN NOT NULL DEFAULT false,
  equipment_ok BOOLEAN NOT NULL DEFAULT false, stock_ok BOOLEAN NOT NULL DEFAULT false,
  infrastructure_ok BOOLEAN NOT NULL DEFAULT false, protocols_ok BOOLEAN NOT NULL DEFAULT false,
  training_ok BOOLEAN NOT NULL DEFAULT false, referral_paths_ok BOOLEAN NOT NULL DEFAULT false,
  remediation_plan TEXT, decided_by TEXT, decision TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['vendor_performance','exceptional_purchases','readiness_assessments']
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
