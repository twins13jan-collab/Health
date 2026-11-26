-- V52 — Module deepening round 27 (Catalog v6, tier C):
-- institutional risk & decisions 134; clinical & medication
-- risk matrix 178. Facility-scoped, platform RLS pattern.

-- ============ 134 Institutional risk & decisions ============
CREATE TABLE IF NOT EXISTS institutional_risks (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  category TEXT CHECK (category IN ('clinical','operational','financial','legal','technical')),
  title TEXT, likelihood INT, impact INT, controls TEXT,
  owner_ref TEXT, treatment_plan TEXT,
  residual_likelihood INT, residual_impact INT,
  accepted_by TEXT,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS risk_treatments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  risk_id UUID NOT NULL REFERENCES institutional_risks(id),
  action TEXT, due_date DATE, done BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS risk_committee_decisions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  decision_ref TEXT, summary TEXT, owner_ref TEXT, due_date DATE,
  implemented BOOLEAN NOT NULL DEFAULT false, impact_measured BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 178 Clinical & medication risk matrix ============
CREATE TABLE IF NOT EXISTS matrix_risks (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  hazard_domain TEXT CHECK (hazard_domain IN ('identity','medication','dose','procedure','critical_results','delay','transport','handover','device_outage')),
  title TEXT, likelihood INT, severity INT,
  controls_prevention TEXT, controls_detection TEXT, controls_response TEXT,
  controlled BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS matrix_alert_rules (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  matrix_risk_id UUID NOT NULL REFERENCES matrix_risks(id),
  alert_name TEXT, total_fired INT, false_positive_count INT,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','under_review','suspended')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['institutional_risks','risk_treatments','risk_committee_decisions',
                           'matrix_risks','matrix_alert_rules']
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
