-- V53 — Module deepening round 28 (Catalog v6, tier C): traceable
-- decision registry 163; clinical costing & patient pathway 165.
-- Facility-scoped, platform RLS pattern.

-- ============ 163 Traceable decision registry ============
CREATE TABLE IF NOT EXISTS traceable_decisions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  decision_ref TEXT, title TEXT, decision_maker TEXT, authority_basis TEXT,
  context_summary TEXT, alternatives_considered TEXT, rationale TEXT,
  linked_approval_ref TEXT, linked_protocol_ref TEXT,
  committee_decision BOOLEAN NOT NULL DEFAULT false, coi_clear BOOLEAN NOT NULL DEFAULT false,
  supersedes_id UUID,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','superseded','revoked')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS decision_amendments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  decision_id UUID NOT NULL REFERENCES traceable_decisions(id),
  action TEXT CHECK (action IN ('review','amend','revoke')),
  reason TEXT, actor TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 165 Clinical costing & pathway ============
CREATE TABLE IF NOT EXISTS pathway_cost_cases (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT, pathway_ref TEXT, standard_cost DOUBLE PRECISION,
  variance_review_status TEXT NOT NULL DEFAULT 'none' CHECK (variance_review_status IN ('none','open','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS cost_entries (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  case_id UUID NOT NULL REFERENCES pathway_cost_cases(id),
  cost_type TEXT CHECK (cost_type IN ('direct','indirect')),
  category TEXT CHECK (category IN ('service','stay','medication','resource')),
  amount DOUBLE PRECISION, actually_consumed BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['traceable_decisions','decision_amendments','pathway_cost_cases','cost_entries']
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
