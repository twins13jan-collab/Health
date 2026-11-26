-- V46 — Module deepening round 21 (Catalog v6, tier C):
-- environmental, chemical & radiological emergencies 173;
-- interfacility transfer safety 186. Facility-scoped, RLS.

-- ============ 173 Hazmat / CBRN emergencies ============
CREATE TABLE IF NOT EXISTS hazmat_incidents (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  agent_type TEXT CHECK (agent_type IN ('chemical','biological','radiological','environmental')),
  agent_name TEXT, exposure_scope TEXT,
  zones_established BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'declared' CHECK (status IN ('declared','responding','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS exposure_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  incident_id UUID NOT NULL REFERENCES hazmat_incidents(id),
  person_ref TEXT, exposure_level TEXT,
  contaminated BOOLEAN NOT NULL DEFAULT false,
  decontaminated BOOLEAN NOT NULL DEFAULT false,
  in_treatment BOOLEAN NOT NULL DEFAULT false,
  follow_up_required BOOLEAN NOT NULL DEFAULT false,
  follow_up_done BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS specialist_coordination (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  incident_id UUID NOT NULL REFERENCES hazmat_incidents(id),
  coordinated_with TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 186 Interfacility transfer safety ============
CREATE TABLE IF NOT EXISTS transfer_assessments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT, patient_stable BOOLEAN,
  stability_confirmed BOOLEAN NOT NULL DEFAULT false,
  escort_level TEXT CHECK (escort_level IN ('none','nurse','physician','icu_team')),
  equipment_needed TEXT, medications_needed TEXT,
  receiving_facility TEXT,
  receiving_acceptance_confirmed BOOLEAN NOT NULL DEFAULT false,
  receiving_capacity_confirmed BOOLEAN NOT NULL DEFAULT false,
  critical_results_summary TEXT,
  status TEXT NOT NULL DEFAULT 'assessed' CHECK (status IN ('assessed','departed','completed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS transfer_logs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  assessment_id UUID NOT NULL REFERENCES transfer_assessments(id),
  departed_at TIMESTAMPTZ, arrived_at TIMESTAMPTZ,
  handover_completed BOOLEAN NOT NULL DEFAULT false, handover_to TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['hazmat_incidents','exposure_records','specialist_coordination',
                           'transfer_assessments','transfer_logs']
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
