-- V51 — Module deepening round 26 (Catalog v6, tier C): medical
-- committees & ethics 141; legal requirements & compliance
-- matrix 164. Facility-scoped, platform RLS pattern.

-- ============ 141 Committees & ethics ============
CREATE TABLE IF NOT EXISTS committees (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  name TEXT, mandate TEXT, quorum_required INT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS committee_members (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  committee_id UUID NOT NULL REFERENCES committees(id),
  member_ref TEXT, coi_declared BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS ethics_cases (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  committee_id UUID NOT NULL REFERENCES committees(id),
  case_type TEXT CHECK (case_type IN ('ethical_conflict','technology_review','ai_review','research','high_risk_protocol')),
  summary TEXT,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','decided')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS committee_decisions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  case_id UUID NOT NULL REFERENCES ethics_cases(id),
  decision TEXT, votes_present INT, voter_refs TEXT, recused_members TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 164 Legal requirements & compliance ============
CREATE TABLE IF NOT EXISTS legal_requirements (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  title TEXT, source_type TEXT CHECK (source_type IN ('law','regulation','standard')),
  effective_from DATE, effective_to DATE,
  policy_ref TEXT, procedure_ref TEXT, control_ref TEXT, evidence_ref TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS compliance_exceptions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  requirement_id UUID NOT NULL REFERENCES legal_requirements(id),
  justification TEXT, authority_basis TEXT, expires_at DATE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS compliance_reviews (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  requirement_id UUID NOT NULL REFERENCES legal_requirements(id),
  compliant BOOLEAN NOT NULL DEFAULT false,
  corrective_actions TEXT, corrective_done BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['committees','committee_members','ethics_cases','committee_decisions',
                           'legal_requirements','compliance_exceptions','compliance_reviews']
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
