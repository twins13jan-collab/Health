-- V54 — Module deepening round 29 (Catalog v6, tier C): disease
-- registries & real-world research 174; clinical protocol
-- quality monitoring 187. Facility-scoped, platform RLS pattern.

-- ============ 174 Disease registries ============
CREATE TABLE IF NOT EXISTS disease_registries (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  name TEXT, case_definition_ref TEXT,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS registry_cases (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  registry_id UUID NOT NULL REFERENCES disease_registries(id),
  patient_ref TEXT, meets_definition BOOLEAN NOT NULL DEFAULT false,
  outcome_recorded BOOLEAN NOT NULL DEFAULT false,
  followup_status TEXT NOT NULL DEFAULT 'active' CHECK (followup_status IN ('active','lost','completed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS registry_publications (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  registry_id UUID NOT NULL REFERENCES disease_registries(id),
  title TEXT, bias_note TEXT, lost_rate_pct DOUBLE PRECISION,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 187 Protocol quality monitoring ============
CREATE TABLE IF NOT EXISTS monitored_protocols (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  name TEXT, source_ref TEXT, approving_body TEXT, scope TEXT, contraindications TEXT,
  version TEXT, review_due DATE,
  status TEXT NOT NULL DEFAULT 'current' CHECK (status IN ('current','under_review','withdrawn')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS protocol_deviations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  protocol_id UUID NOT NULL REFERENCES monitored_protocols(id),
  deviation_type TEXT CHECK (deviation_type IN ('justified','unjustified')),
  reason TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS protocol_adherence_reports (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  protocol_id UUID NOT NULL REFERENCES monitored_protocols(id),
  total_cases INT, adherent_cases INT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['disease_registries','registry_cases','registry_publications',
                           'monitored_protocols','protocol_deviations','protocol_adherence_reports']
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
