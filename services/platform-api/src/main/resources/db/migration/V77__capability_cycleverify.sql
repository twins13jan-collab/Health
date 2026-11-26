-- V77 — Module deepening round 50 (Catalog v6, tier B):
-- service accreditation & availability by capability 160;
-- end-to-end service-cycle completeness verification 180.
-- Facility-scoped, platform RLS pattern.

-- ============ 160 Capability-based availability ============
CREATE TABLE IF NOT EXISTS service_capabilities (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  service_ref TEXT,
  staff_ready BOOLEAN NOT NULL DEFAULT false,
  equipment_ready BOOLEAN NOT NULL DEFAULT false,
  licence_valid BOOLEAN NOT NULL DEFAULT false,
  protocol_linked BOOLEAN NOT NULL DEFAULT false,
  last_verified_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 180 Cycle completeness ============
CREATE TABLE IF NOT EXISTS cycle_verifications (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  cycle_ref TEXT, instance_ref TEXT,
  request_done BOOLEAN NOT NULL DEFAULT false, triage_done BOOLEAN NOT NULL DEFAULT false,
  approval_done BOOLEAN NOT NULL DEFAULT false, execution_done BOOLEAN NOT NULL DEFAULT false,
  result_done BOOLEAN NOT NULL DEFAULT false, followup_done BOOLEAN NOT NULL DEFAULT false,
  evidence_refs TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['service_capabilities','cycle_verifications']
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
