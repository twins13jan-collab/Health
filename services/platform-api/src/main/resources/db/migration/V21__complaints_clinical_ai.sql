-- V21 — Batch 20 (Catalog v6): complaints, suggestions & closure 232,
-- clinical AI & human verification 233. Facility-scoped with the
-- platform RLS pattern. (patient_complaints is distinct from the
-- framework complaints table of the early quality module.)

-- ============ 232 Complaints ============
CREATE TABLE IF NOT EXISTS patient_complaints (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  complainant_name TEXT, anonymous BOOLEAN NOT NULL DEFAULT false,
  representation TEXT, contact TEXT, identity_verified BOOLEAN NOT NULL DEFAULT false,
  content TEXT NOT NULL, category TEXT,
  imminent_risk BOOLEAN NOT NULL DEFAULT false, safety_escalated BOOLEAN NOT NULL DEFAULT false,
  safety_case_ref TEXT,
  root_cause TEXT, corrective_action TEXT,
  closed_by TEXT,
  status TEXT NOT NULL DEFAULT 'received' CHECK (status IN ('received','investigating','resolved','closed','appealed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS complaint_investigations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  complaint_id UUID NOT NULL REFERENCES patient_complaints(id),
  investigator TEXT NOT NULL, conflict_declared BOOLEAN NOT NULL DEFAULT false,
  reassigned_from TEXT, timeline TEXT,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','completed','reassigned')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS complaint_evidence_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  complaint_id UUID NOT NULL REFERENCES patient_complaints(id),
  description TEXT, content_hash TEXT, added_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS complaint_access_log (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  complaint_id UUID NOT NULL REFERENCES patient_complaints(id),
  accessor TEXT, purpose TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS complaint_resolutions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  complaint_id UUID NOT NULL REFERENCES patient_complaints(id),
  decision TEXT NOT NULL, basis TEXT, decided_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS complaint_appeals (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  complaint_id UUID NOT NULL REFERENCES patient_complaints(id),
  grounds TEXT, reviewed_by TEXT, outcome TEXT,
  status TEXT NOT NULL DEFAULT 'filed' CHECK (status IN ('filed','upheld','overturned')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS disclosure_reviews (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  complaint_id UUID NOT NULL REFERENCES patient_complaints(id),
  requester TEXT, data_subject TEXT, privacy_review TEXT, legal_review TEXT,
  status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending','approved','refused')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 233 Clinical AI ============
CREATE TABLE IF NOT EXISTS ai_model_registry (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  name TEXT NOT NULL, version TEXT NOT NULL, vendor TEXT, purpose TEXT,
  risk_class TEXT NOT NULL DEFAULT 'moderate' CHECK (risk_class IN ('low','moderate','high')),
  training_data TEXT, limitations TEXT, owner TEXT,
  committee_approved BOOLEAN NOT NULL DEFAULT false, approved_scope TEXT,
  status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft','validated','approved','suspended','retired')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS ai_model_validations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  model_id UUID NOT NULL REFERENCES ai_model_registry(id),
  dataset_description TEXT, population TEXT, performance TEXT, bias_assessment TEXT,
  approved BOOLEAN NOT NULL DEFAULT false, committee TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS ai_inferences (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  model_id UUID NOT NULL REFERENCES ai_model_registry(id),
  model_version TEXT, patient_ref TEXT, inputs_summary TEXT,
  inputs_complete BOOLEAN NOT NULL DEFAULT false, in_scope BOOLEAN NOT NULL DEFAULT false,
  result TEXT, confidence NUMERIC(5,4),
  abstained BOOLEAN NOT NULL DEFAULT false, abstain_reason TEXT,
  requested_by TEXT, applied BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'pending_review' CHECK (status IN ('pending_review','reviewed','abstained')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS ai_human_reviews (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  inference_id UUID NOT NULL REFERENCES ai_inferences(id),
  reviewer TEXT, decision TEXT NOT NULL CHECK (decision IN ('accepted','rejected','modified')),
  reason TEXT, final_decision TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS ai_model_incidents (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  model_id UUID NOT NULL REFERENCES ai_model_registry(id),
  kind TEXT NOT NULL CHECK (kind IN ('incident','drift')),
  description TEXT, drift_metric NUMERIC(6,4), action TEXT, investigation TEXT,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','investigating','suspended','reapproved','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['patient_complaints','complaint_investigations','complaint_evidence_items','complaint_access_log','complaint_resolutions','complaint_appeals','disclosure_reviews',
                           'ai_model_registry','ai_model_validations','ai_inferences','ai_human_reviews','ai_model_incidents']
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
