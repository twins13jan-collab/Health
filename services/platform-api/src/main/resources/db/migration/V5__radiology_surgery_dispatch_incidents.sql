-- V5 — Batch 4 (Catalog v6): radiology (module 93), surgery 220, dispatch 224, incidents (module 156).
CREATE TABLE IF NOT EXISTS radiology_orders (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id), encounter_id UUID REFERENCES encounters(id),
  modality TEXT NOT NULL CHECK (modality IN ('XR','CT','MRI','US','MAMMO','ANGIO')),
  body_part TEXT, clinical_question TEXT NOT NULL,
  priority TEXT NOT NULL DEFAULT 'routine',
  status TEXT NOT NULL DEFAULT 'ordered' CHECK (status IN ('ordered','scheduled','in_progress','imaged','reported','verified','cancelled')),
  report_text TEXT, reported_by TEXT, ordered_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_rad_orders_facility_status ON radiology_orders (facility_id, status);
CREATE TABLE IF NOT EXISTS surgical_cases (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id), encounter_id UUID REFERENCES encounters(id),
  procedure_name TEXT NOT NULL, operating_room TEXT, scheduled_at TIMESTAMPTZ,
  checklist_stage TEXT NOT NULL DEFAULT 'sign_in' CHECK (checklist_stage IN ('sign_in','time_out','sign_out','completed')),
  confirmed_items TEXT NOT NULL DEFAULT '',
  status TEXT NOT NULL DEFAULT 'scheduled' CHECK (status IN ('scheduled','in_progress','completed','postponed','cancelled')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_surgical_facility ON surgical_cases (facility_id, scheduled_at);
CREATE TABLE IF NOT EXISTS ambulance_units (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID REFERENCES facilities(id), governorate TEXT,
  unit_code TEXT NOT NULL UNIQUE, lat DOUBLE PRECISION NOT NULL, lon DOUBLE PRECISION NOT NULL,
  status TEXT NOT NULL DEFAULT 'available' CHECK (status IN ('available','dispatched','on_scene','transporting','returning','offline')),
  capabilities TEXT NOT NULL DEFAULT '',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE TABLE IF NOT EXISTS dispatch_requests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID REFERENCES facilities(id), governorate TEXT,
  patient_id UUID REFERENCES patients(id),
  lat DOUBLE PRECISION NOT NULL, lon DOUBLE PRECISION NOT NULL, complaint TEXT NOT NULL,
  required_capability TEXT, assigned_unit_id UUID REFERENCES ambulance_units(id),
  status TEXT NOT NULL DEFAULT 'received' CHECK (status IN ('received','assigned','en_route','on_scene','transporting','completed','cancelled')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_dispatch_status ON dispatch_requests (status, created_at);
CREATE TABLE IF NOT EXISTS incident_reports (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID REFERENCES patients(id),
  category TEXT NOT NULL, severity TEXT NOT NULL CHECK (severity IN ('near_miss','minor','moderate','major','catastrophic')),
  description TEXT NOT NULL, reported_by TEXT,
  status TEXT NOT NULL DEFAULT 'reported' CHECK (status IN ('reported','under_review','action_assigned','closed')),
  root_cause TEXT, corrective_action TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_incidents_facility_status ON incident_reports (facility_id, status);
ALTER TABLE radiology_orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE radiology_orders FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS radiology_orders_facility_isolation ON radiology_orders;
CREATE POLICY radiology_orders_facility_isolation ON radiology_orders FOR ALL
  USING (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)))
  WITH CHECK (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)));
ALTER TABLE surgical_cases ENABLE ROW LEVEL SECURITY;
ALTER TABLE surgical_cases FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS surgical_cases_facility_isolation ON surgical_cases;
CREATE POLICY surgical_cases_facility_isolation ON surgical_cases FOR ALL
  USING (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)))
  WITH CHECK (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)));
ALTER TABLE incident_reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE incident_reports FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS incident_reports_facility_isolation ON incident_reports;
CREATE POLICY incident_reports_facility_isolation ON incident_reports FOR ALL
  USING (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)))
  WITH CHECK (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)));
GRANT SELECT, INSERT, UPDATE, DELETE ON radiology_orders, surgical_cases, ambulance_units, dispatch_requests, incident_reports TO app_user;
