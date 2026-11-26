-- V25 — CSSD reprocessing table under its own name: V24's
-- reprocessing_cycles name was already owned by endoscopy (241)
-- scope reprocessing, so the CSSD table is created here as
-- cssd_reprocessing_cycles with the full module-105 schema.

CREATE TABLE IF NOT EXISTS cssd_reprocessing_cycles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  set_id UUID NOT NULL REFERENCES instrument_sets(id),
  received_from TEXT, sorted_by TEXT,
  cleaning_done BOOLEAN NOT NULL DEFAULT false, disinfection_done BOOLEAN NOT NULL DEFAULT false,
  dried BOOLEAN NOT NULL DEFAULT false, inspected BOOLEAN NOT NULL DEFAULT false,
  packed_by TEXT, sterilizer_code TEXT, cycle_no TEXT,
  chemical_indicator TEXT CHECK (chemical_indicator IN ('pass','fail')),
  biological_indicator TEXT CHECK (biological_indicator IN ('pass','fail','pending')),
  released_by TEXT, released_at TIMESTAMPTZ,
  status TEXT NOT NULL DEFAULT 'in_progress' CHECK (status IN ('in_progress','released','quarantined','failed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

DO $$
DECLARE t TEXT := 'cssd_reprocessing_cycles';
BEGIN
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
END $$;

-- sterilization_failures.cycle_id now references the CSSD table.
ALTER TABLE sterilization_failures DROP CONSTRAINT IF EXISTS sterilization_failures_cycle_id_fkey;
ALTER TABLE sterilization_failures ADD CONSTRAINT sterilization_failures_cycle_id_fkey
  FOREIGN KEY (cycle_id) REFERENCES cssd_reprocessing_cycles(id);
