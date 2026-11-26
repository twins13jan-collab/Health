-- V56 — Follow-up to V55: the reports framework (cycle 236)
-- already owns table `indicator_definitions` and the JPA entity
-- name `IndicatorDefinition`, so V55's CREATE TABLE IF NOT EXISTS
-- silently skipped. The data-quality module (122) gets its own
-- standalone table here. Same pattern as V36/V45.

CREATE TABLE IF NOT EXISTS dq_indicator_definitions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  name TEXT, definition TEXT, numerator TEXT, denominator TEXT, owner_ref TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

DO $$
BEGIN
  ALTER TABLE dq_indicator_definitions ENABLE ROW LEVEL SECURITY;
  ALTER TABLE dq_indicator_definitions FORCE ROW LEVEL SECURITY;
  DROP POLICY IF EXISTS dq_indicator_definitions_facility_isolation ON dq_indicator_definitions;
  CREATE POLICY dq_indicator_definitions_facility_isolation ON dq_indicator_definitions FOR ALL
    USING (current_setting('app.current_facility_id', true) = 'national'
           OR facility_id::text = current_setting('app.current_facility_id', true)
           OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)))
    WITH CHECK (current_setting('app.current_facility_id', true) = 'national'
           OR facility_id::text = current_setting('app.current_facility_id', true)
           OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)));
  GRANT SELECT, INSERT, UPDATE, DELETE ON dq_indicator_definitions TO app_user;
END $$;

-- indicator_publications was created in V55 with its FK bound to
-- the reports-owned indicator_definitions table; drop that FK so
-- publications reference the module's own definitions logically.
ALTER TABLE indicator_publications DROP CONSTRAINT IF EXISTS indicator_publications_indicator_id_fkey;
