-- V66 — Module deepening round 39 (Catalog v6, tier B):
-- radiology & medical imaging 93; child health, immunization
-- & growth 96. Facility-scoped, platform RLS pattern.

-- ============ 93 Radiology & imaging ============
CREATE TABLE IF NOT EXISTS imaging_orders (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT, exam TEXT, justification TEXT, priority TEXT,
  contraindications_checked BOOLEAN NOT NULL DEFAULT false,
  contrast_planned BOOLEAN NOT NULL DEFAULT false,
  egfr_value DOUBLE PRECISION, nephrology_override BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'ordered' CHECK (status IN ('ordered','performed','reported','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS imaging_reports (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  order_id UUID NOT NULL REFERENCES imaging_orders(id),
  report_type TEXT CHECK (report_type IN ('preliminary','final','correction')),
  signed_by TEXT, supersedes_id UUID,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS imaging_critical_results (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  order_id UUID NOT NULL REFERENCES imaging_orders(id),
  finding TEXT, acknowledged_by TEXT, acknowledged BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 96 Child health & immunization ============
CREATE TABLE IF NOT EXISTS child_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  child_ref TEXT, birth_date DATE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS immunization_doses (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  child_id UUID NOT NULL REFERENCES child_records(id),
  vaccine TEXT, batch_ref TEXT, cold_chain_ok BOOLEAN NOT NULL DEFAULT false,
  given_date DATE, valid BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS aefi_reports (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  dose_id UUID NOT NULL REFERENCES immunization_doses(id),
  description TEXT, investigated BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['imaging_orders','imaging_reports','imaging_critical_results',
                           'child_records','immunization_doses','aefi_reports']
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
