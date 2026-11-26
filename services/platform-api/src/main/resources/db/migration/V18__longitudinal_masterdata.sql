-- V18 — Batch 17 (Catalog v6): longitudinal record & patient identity
-- reconciliation 217, master data & reference catalogues 235.
-- Every table is facility-scoped with the platform RLS pattern.

-- ============ 217 Unified identity, links & duplicate cases ============
CREATE TABLE IF NOT EXISTS patient_identities (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID, -- the unified patient this identity belongs to (MPI)
  identity_kind TEXT NOT NULL DEFAULT 'temporary' CHECK (identity_kind IN ('verified','temporary','unknown')),
  proof_source TEXT,
  verification_status TEXT NOT NULL DEFAULT 'pending' CHECK (verification_status IN ('verified','pending','rejected')),
  merged_into UUID,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','merged')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS identity_links (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  identity_id UUID NOT NULL REFERENCES patient_identities(id),
  link_type TEXT NOT NULL, issuer TEXT,
  protected_value TEXT, -- stored protected (hashed/masked), never plain national numbers in payloads
  valid_from DATE, valid_to DATE,
  link_status TEXT NOT NULL DEFAULT 'active' CHECK (link_status IN ('active','superseded')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS clinical_record_links (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  identity_id UUID NOT NULL REFERENCES patient_identities(id),
  record_type TEXT NOT NULL, record_ref TEXT NOT NULL,
  owner_facility TEXT, source_system TEXT, field_name TEXT, field_value TEXT,
  recorded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_crl_identity ON clinical_record_links (identity_id, record_type);

CREATE TABLE IF NOT EXISTS duplicate_cases (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  identity_a UUID NOT NULL REFERENCES patient_identities(id),
  identity_b UUID NOT NULL REFERENCES patient_identities(id),
  similarity_score NUMERIC(4,3) NOT NULL DEFAULT 0,
  similarity_reasons TEXT,
  decided_by TEXT, second_approver TEXT, decision_evidence TEXT,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','merged','rejected','unmerged')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 235 Master entities, codes, mappings, stewardship ============
CREATE TABLE IF NOT EXISTS master_entities (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  entity_type TEXT NOT NULL, code TEXT NOT NULL,
  name_ar TEXT, name_en TEXT, owner_domain TEXT,
  version INT NOT NULL DEFAULT 1,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','deprecated')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_master_entity ON master_entities (facility_id, entity_type, code);

CREATE TABLE IF NOT EXISTS reference_codes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  code_system TEXT NOT NULL, code TEXT NOT NULL,
  description TEXT, domain TEXT,
  national BOOLEAN NOT NULL DEFAULT true,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','deprecated','unknown_review')),
  effective_from DATE, effective_to DATE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_reference_code ON reference_codes (facility_id, code_system, code);

CREATE TABLE IF NOT EXISTS mapping_sets (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  local_code TEXT NOT NULL, standard_code TEXT NOT NULL,
  equivalence TEXT NOT NULL DEFAULT 'exact' CHECK (equivalence IN ('exact','broader','narrower','unmatched')),
  conversion_reason TEXT, version INT NOT NULL DEFAULT 1,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','deprecated')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_mapping ON mapping_sets (facility_id, local_code, standard_code);

CREATE TABLE IF NOT EXISTS stewardship_cases (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  change_type TEXT NOT NULL CHECK (change_type IN ('add','modify','deprecate','exception_use','map_unknown')),
  target_ref TEXT NOT NULL, justification TEXT NOT NULL,
  requested_by TEXT, decided_by TEXT,
  decision TEXT NOT NULL DEFAULT 'pending' CHECK (decision IN ('pending','approved','rejected')),
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS data_quality_rules (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  rule_type TEXT NOT NULL CHECK (rule_type IN ('completeness','consistency','uniqueness','freshness')),
  target TEXT NOT NULL, expression TEXT, threshold NUMERIC(8,2),
  unknown_rate NUMERIC(5,2), duplicate_rate NUMERIC(5,2), conflict_rate NUMERIC(5,2),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['patient_identities','identity_links','clinical_record_links','duplicate_cases',
                           'master_entities','reference_codes','mapping_sets','stewardship_cases','data_quality_rules']
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
