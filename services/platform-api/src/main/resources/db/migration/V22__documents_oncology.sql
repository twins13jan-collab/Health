-- V22 — Batch 21 (Catalog v6): documents, signatures & retention 239,
-- oncology, chemotherapy, immunotherapy & radiotherapy 243.
-- Facility-scoped with the platform RLS pattern.

-- ============ 239 Documents, versions, signatures, retention ============
CREATE TABLE IF NOT EXISTS document_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  doc_type TEXT NOT NULL, title TEXT, owner TEXT, classification TEXT,
  confidentiality TEXT, source TEXT, current_version INT NOT NULL DEFAULT 1,
  approved_version INT, corrupted BOOLEAN NOT NULL DEFAULT false,
  restoration_ref TEXT,
  status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft','approved','archived')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS document_versions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  document_id UUID NOT NULL REFERENCES document_records(id),
  version INT NOT NULL, content_ref TEXT, content_hash TEXT NOT NULL,
  author TEXT, change_reason TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_doc_version ON document_versions (document_id, version);

CREATE TABLE IF NOT EXISTS signature_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  document_id UUID NOT NULL REFERENCES document_records(id),
  version INT NOT NULL, version_hash TEXT,
  signer TEXT, certificate_ref TEXT, certificate_valid BOOLEAN NOT NULL DEFAULT false,
  scope TEXT, signed_at TIMESTAMPTZ,
  status TEXT NOT NULL DEFAULT 'recorded' CHECK (status IN ('recorded','valid','invalid','superseded')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS retention_rules (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  doc_type TEXT NOT NULL, retention_years INT NOT NULL, legal_basis TEXT,
  managed_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_retention_type ON retention_rules (facility_id, doc_type);

CREATE TABLE IF NOT EXISTS legal_holds (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  document_id UUID NOT NULL REFERENCES document_records(id),
  case_ref TEXT, reason TEXT, placed_by TEXT, released_by TEXT,
  active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS document_access_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  document_id UUID NOT NULL REFERENCES document_records(id),
  actor TEXT, action TEXT NOT NULL CHECK (action IN ('view','download','print','share')),
  purpose TEXT, sensitive BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS deletion_requests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  document_id UUID NOT NULL REFERENCES document_records(id),
  requested_by TEXT, basis TEXT, blocked_reason TEXT,
  status TEXT NOT NULL DEFAULT 'held' CHECK (status IN ('held','approved','executed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 243 Oncology ============
CREATE TABLE IF NOT EXISTS cancer_cases (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT, tumor_type TEXT, stage TEXT, biomarkers TEXT,
  diagnosed_by TEXT,
  status TEXT NOT NULL DEFAULT 'diagnosed' CHECK (status IN ('diagnosed','board_reviewed','on_treatment','follow_up','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS tumor_board_decisions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  case_id UUID NOT NULL REFERENCES cancer_cases(id),
  decision TEXT, participants TEXT, decided_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS protocol_versions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  regimen_name TEXT NOT NULL, version TEXT NOT NULL, conditions TEXT,
  approval_source TEXT, approved_at DATE, valid_until DATE,
  medically_approved BOOLEAN NOT NULL DEFAULT false,
  max_weight_change_pct NUMERIC(5,2),
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','superseded','withdrawn')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_protocol_version ON protocol_versions (facility_id, regimen_name, version);

CREATE TABLE IF NOT EXISTS treatment_cycles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  case_id UUID NOT NULL REFERENCES cancer_cases(id),
  protocol_version_id UUID NOT NULL REFERENCES protocol_versions(id),
  cycle_number INT NOT NULL DEFAULT 1, drugs TEXT,
  approved_dose TEXT, prescribed_by TEXT,
  postponed BOOLEAN NOT NULL DEFAULT false, postpone_reason TEXT, postpone_impact TEXT,
  status TEXT NOT NULL DEFAULT 'planned' CHECK (status IN ('planned','authorized','verified','administered','postponed','held')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS pretreatment_verifications (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  cycle_id UUID NOT NULL REFERENCES treatment_cycles(id),
  weight_kg NUMERIC(6,2), bsa NUMERIC(4,2), labs_summary TEXT, labs_within_conditions BOOLEAN NOT NULL DEFAULT false,
  weight_change_pct NUMERIC(5,2), allergy_checked BOOLEAN NOT NULL DEFAULT false,
  consent_confirmed BOOLEAN NOT NULL DEFAULT false,
  verifier TEXT, pharmacy_review TEXT,
  suspended BOOLEAN NOT NULL DEFAULT false, suspend_reason TEXT,
  status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending','verified','suspended')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS chemo_administrations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  cycle_id UUID NOT NULL REFERENCES treatment_cycles(id),
  prescribed_dose TEXT, administered_dose TEXT,
  variance NUMERIC(8,2), variance_reason TEXT, variance_approved_by TEXT,
  drug_available BOOLEAN NOT NULL DEFAULT true, recalled BOOLEAN NOT NULL DEFAULT false,
  substitute_drug TEXT, substitute_decision_by TEXT,
  administered_by TEXT, identity_checked BOOLEAN NOT NULL DEFAULT false,
  batch_ref TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS radiotherapy_plans (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  case_id UUID NOT NULL REFERENCES cancer_cases(id),
  total_dose TEXT, fractions_planned INT NOT NULL DEFAULT 0, organs_at_risk TEXT,
  approved_by TEXT,
  status TEXT NOT NULL DEFAULT 'planned' CHECK (status IN ('planned','approved','in_progress','completed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS radiotherapy_fractions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  plan_id UUID NOT NULL REFERENCES radiotherapy_plans(id),
  fraction_number INT NOT NULL, delivered_dose TEXT, verified_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS toxicity_assessments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  case_id UUID NOT NULL REFERENCES cancer_cases(id),
  cycle_id UUID REFERENCES treatment_cycles(id),
  grade INT NOT NULL DEFAULT 0, description TEXT, intervention TEXT,
  continuation_decision TEXT CHECK (continuation_decision IN ('continue','modify','stop')),
  decided_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['document_records','document_versions','signature_records','retention_rules','legal_holds','document_access_events','deletion_requests',
                           'cancer_cases','tumor_board_decisions','protocol_versions','treatment_cycles','pretreatment_verifications','chemo_administrations','radiotherapy_plans','radiotherapy_fractions','toxicity_assessments']
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
