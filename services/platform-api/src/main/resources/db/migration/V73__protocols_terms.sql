-- V73 — Module deepening round 46 (Catalog v6, tier B):
-- protocol, policy & clinical-rule versions 61; unified
-- event & terminology definitions 137. Facility-scoped,
-- platform RLS pattern.

-- ============ 61 Protocol versions ============
CREATE TABLE IF NOT EXISTS clinical_protocol_versions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  code TEXT, title TEXT, version TEXT, scientific_ref TEXT, owner_body TEXT,
  committee_approved BOOLEAN NOT NULL DEFAULT false,
  effective_from DATE, review_due DATE,
  status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft','active','withdrawn')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS clinical_protocol_usages (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  protocol_version_id UUID NOT NULL REFERENCES clinical_protocol_versions(id),
  service_ref TEXT, service_date DATE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 137 Terminology ============
CREATE TABLE IF NOT EXISTS term_mappings (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  local_term TEXT, standard_system TEXT, standard_code TEXT,
  meaning_verified BOOLEAN NOT NULL DEFAULT false,
  specialist_reviewed BOOLEAN NOT NULL DEFAULT false,
  supported BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['clinical_protocol_versions','clinical_protocol_usages','term_mappings']
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
