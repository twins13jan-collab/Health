-- V37 — Module deepening round 13 (Catalog v6): health technology
-- assessment HTA 166; national congenital anomalies registry 169.
-- Facility-scoped, platform RLS pattern.

-- ============ 166 Health technology assessment ============
CREATE TABLE IF NOT EXISTS hta_assessments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  technology_name TEXT, tech_type TEXT CHECK (tech_type IN ('drug','device','procedure','digital')),
  requester TEXT,
  effectiveness TEXT, safety TEXT, cost_effectiveness TEXT, budget_impact TEXT, ethical_impact TEXT,
  evidence_quality TEXT CHECK (evidence_quality IN ('high','moderate','low','very_low')),
  applicability TEXT, stakeholders_consulted BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'intake' CHECK (status IN ('intake','evidence_review','appraisal','decided')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS hta_evidence_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  assessment_id UUID NOT NULL REFERENCES hta_assessments(id),
  source TEXT, study_type TEXT, quality_rating TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS hta_panel_members (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  assessment_id UUID NOT NULL REFERENCES hta_assessments(id),
  member_name TEXT, coi_declared BOOLEAN NOT NULL DEFAULT false, coi_detail TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS hta_recommendations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  assessment_id UUID NOT NULL REFERENCES hta_assessments(id),
  recommendation TEXT CHECK (recommendation IN ('adopt','adopt_with_restrictions','reject','research_only')),
  conditions TEXT, decided_by TEXT, decided_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 169 Congenital anomalies registry ============
CREATE TABLE IF NOT EXISTS anomaly_cases (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  case_code TEXT NOT NULL, newborn_ref TEXT, anomaly_code TEXT,
  classification TEXT CHECK (classification IN ('confirmed','suspected')),
  source_type TEXT CHECK (source_type IN ('delivery','prenatal','postnatal','genetics_lab')),
  reported_by TEXT,
  status TEXT NOT NULL DEFAULT 'registered' CHECK (status IN ('registered','under_review','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_anomaly_case ON anomaly_cases (facility_id, case_code);

CREATE TABLE IF NOT EXISTS anomaly_linkages (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  case_id UUID NOT NULL REFERENCES anomaly_cases(id),
  link_type TEXT CHECK (link_type IN ('pregnancy','birth','newborn')),
  linked_ref TEXT, linked_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS family_referrals (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  case_id UUID NOT NULL REFERENCES anomaly_cases(id),
  service TEXT CHECK (service IN ('genetic_counseling','rehabilitation','early_intervention')),
  accepted BOOLEAN, referred_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS trend_analyses (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  period TEXT, region TEXT, confirmed_cases INT, suspected_cases INT,
  note TEXT, causal_claim TEXT, supporting_evidence TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['hta_assessments','hta_evidence_items','hta_panel_members','hta_recommendations',
                           'anomaly_cases','anomaly_linkages','family_referrals','trend_analyses']
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
