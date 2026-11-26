-- V72 — Module deepening round 45 (Catalog v6, tier B):
-- critical care & transfer between care levels 153; death
-- data & its review 159. Facility-scoped, platform RLS.

-- ============ 153 Critical care & transfers ============
CREATE TABLE IF NOT EXISTS icu_level_transfers (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT, from_level TEXT, to_level TEXT,
  criteria_met BOOLEAN NOT NULL DEFAULT false,
  stability_reviewed BOOLEAN NOT NULL DEFAULT false,
  team_equipped BOOLEAN NOT NULL DEFAULT false,
  handover_documented BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'planned' CHECK (status IN ('planned','completed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 159 Death data ============
CREATE TABLE IF NOT EXISTS death_registrations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  deceased_ref TEXT, place TEXT, source TEXT,
  cause_code TEXT, cause_certified BOOLEAN NOT NULL DEFAULT false,
  statistics_linked BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS death_corrections (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  registration_id UUID NOT NULL REFERENCES death_registrations(id),
  field TEXT, old_value TEXT, new_value TEXT, reason TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['icu_level_transfers','death_registrations','death_corrections']
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
