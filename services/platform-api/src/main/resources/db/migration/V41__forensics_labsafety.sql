-- V41 — Module deepening round 17 (Catalog v6, tier C): forensic
-- medicine & medico-legal reports 71; laboratory & diagnostic
-- device safety 158. Facility-scoped, platform RLS pattern.

-- ============ 71 Forensic medicine ============
CREATE TABLE IF NOT EXISTS forensic_cases (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  case_ref TEXT, requesting_authority TEXT, mandate_ref TEXT,
  authority_verified BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'received' CHECK (status IN ('received','in_examination','reported','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS forensic_evidence_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  case_id UUID NOT NULL REFERENCES forensic_cases(id),
  item_description TEXT, seal_no TEXT,
  status TEXT NOT NULL DEFAULT 'stored' CHECK (status IN ('stored','in_examination','transferred','released')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS chain_of_custody_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  evidence_id UUID NOT NULL REFERENCES forensic_evidence_items(id),
  event_type TEXT CHECK (event_type IN ('received','stored','transferred','released')),
  from_party TEXT, to_party TEXT, recorded_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS forensic_reports (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  case_id UUID NOT NULL REFERENCES forensic_cases(id),
  report_type TEXT, signed_by TEXT, countersigned_by TEXT,
  released_to TEXT, released_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 158 Laboratory & device safety ============
CREATE TABLE IF NOT EXISTS test_introductions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  test_name TEXT, analyzer TEXT,
  risk_assessment_done BOOLEAN NOT NULL DEFAULT false,
  accuracy_verified BOOLEAN NOT NULL DEFAULT false,
  sensitivity_verified BOOLEAN NOT NULL DEFAULT false,
  interferences_checked BOOLEAN NOT NULL DEFAULT false,
  approved BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS analyzer_comparisons (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  introduction_id UUID NOT NULL REFERENCES test_introductions(id),
  analyzer_a TEXT, analyzer_b TEXT, bias_pct NUMERIC(6,2), acceptable BOOLEAN,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS reagent_lots (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  introduction_id UUID REFERENCES test_introductions(id),
  lot_no TEXT, expiry_date DATE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS analyzer_failures (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  analyzer TEXT, failure_description TEXT, failed_at TIMESTAMPTZ,
  impact_assessed BOOLEAN NOT NULL DEFAULT false, affected_results_count INT,
  returned_to_service BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['forensic_cases','forensic_evidence_items','chain_of_custody_events','forensic_reports',
                           'test_introductions','analyzer_comparisons','reagent_lots','analyzer_failures']
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
