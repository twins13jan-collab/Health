-- V62 — Module deepening round 36 (Catalog v6, tier C):
-- organizational learning & procedural knowledge 80; module
-- specification & accreditation model 81. Facility-scoped,
-- platform RLS pattern.

-- ============ 80 Organizational learning ============
CREATE TABLE IF NOT EXISTS knowledge_documents (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  title TEXT, owner_ref TEXT, version TEXT, audience TEXT,
  effective_from DATE, review_due DATE, mandatory BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS read_acknowledgments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  document_id UUID NOT NULL REFERENCES knowledge_documents(id),
  user_ref TEXT, acknowledged BOOLEAN NOT NULL DEFAULT false, comprehension_assessed BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS lessons_learned (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  source_type TEXT CHECK (source_type IN ('incident','crisis','audit','complaint')),
  source_ref TEXT, lesson TEXT, applied BOOLEAN NOT NULL DEFAULT false, applied_in TEXT,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 81 Module specification ============
CREATE TABLE IF NOT EXISTS module_specifications (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  module_ref TEXT, purpose TEXT, scope TEXT, owner_org TEXT, users_roles TEXT,
  workflow_documented BOOLEAN NOT NULL DEFAULT false,
  entities_documented BOOLEAN NOT NULL DEFAULT false,
  sensitive_fields_documented BOOLEAN NOT NULL DEFAULT false,
  interfaces_documented BOOLEAN NOT NULL DEFAULT false,
  permissions_documented BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft','complete','accredited')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['knowledge_documents','read_acknowledgments','lessons_learned','module_specifications']
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
