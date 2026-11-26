-- V45 — Round 20 fix: porterage (module 109) already owns the
-- table name transport_requests with its own schema, so V44's
-- CREATE TABLE IF NOT EXISTS silently skipped. Module 154 gets
-- its own table here (same pattern as V36 for med delivery).
CREATE TABLE IF NOT EXISTS patient_transport_requests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT,
  purpose TEXT CHECK (purpose IN ('dialysis','therapy','rehab','appointment')),
  need_type TEXT CHECK (need_type IN ('seated','wheelchair','stretcher')),
  oxygen_needed BOOLEAN NOT NULL DEFAULT false,
  scheduled_at TIMESTAMPTZ, provider_id UUID REFERENCES transport_providers(id),
  status TEXT NOT NULL DEFAULT 'requested' CHECK (status IN ('requested','scheduled','completed','cancelled')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

DO $$
DECLARE t TEXT := 'patient_transport_requests';
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

-- V44 created transport_trips.request_id referencing the
-- porterage table of the same name; repoint it at module 154's
-- own requests table.
ALTER TABLE transport_trips DROP CONSTRAINT IF EXISTS transport_trips_request_id_fkey;
ALTER TABLE transport_trips ADD CONSTRAINT transport_trips_request_id_fkey
  FOREIGN KEY (request_id) REFERENCES patient_transport_requests(id);
