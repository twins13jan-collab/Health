-- V7 — Batch 6 (Catalog v6): dialysis 242, oncology, telemedicine,
-- death registry, documents/retention, procurement, assets.
CREATE TABLE IF NOT EXISTS dialysis_sessions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  machine_code TEXT NOT NULL, starts_at TIMESTAMPTZ NOT NULL, ends_at TIMESTAMPTZ NOT NULL,
  dry_weight_kg DOUBLE PRECISION, ultrafiltration_ml INT,
  status TEXT NOT NULL DEFAULT 'scheduled' CHECK (status IN ('scheduled','in_session','completed','missed','cancelled')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT chk_dialysis_window CHECK (ends_at > starts_at));
CREATE INDEX IF NOT EXISTS idx_dialysis_machine ON dialysis_sessions (facility_id, machine_code, starts_at);
CREATE TABLE IF NOT EXISTS chemo_cycles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  protocol TEXT, drug_code TEXT NOT NULL, cycle_number INT NOT NULL,
  bsa DOUBLE PRECISION NOT NULL, dose_mg NUMERIC NOT NULL, scheduled_date DATE,
  status TEXT NOT NULL DEFAULT 'planned' CHECK (status IN ('planned','verified','administered','held','cancelled')),
  verified_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_chemo_patient ON chemo_cycles (patient_id, cycle_number);
CREATE TABLE IF NOT EXISTS teleconsultations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  consultant_name TEXT NOT NULL, specialty TEXT, scheduled_at TIMESTAMPTZ NOT NULL,
  status TEXT NOT NULL DEFAULT 'scheduled' CHECK (status IN ('scheduled','in_session','completed','no_show','cancelled','escalated_to_in_person')),
  session_notes TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE TABLE IF NOT EXISTS death_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL UNIQUE REFERENCES patients(id),
  died_at TIMESTAMPTZ NOT NULL, cause_icd10 TEXT NOT NULL, cause_description TEXT, certified_by TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'certified' CHECK (status IN ('certified','under_review','corrected')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE TABLE IF NOT EXISTS medical_documents (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  category TEXT NOT NULL, title TEXT NOT NULL, document_date DATE NOT NULL, storage_uri TEXT,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','archived','disposed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_documents_patient ON medical_documents (patient_id, document_date);
CREATE TABLE IF NOT EXISTS purchase_orders (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  supplier TEXT NOT NULL, item_code TEXT NOT NULL, quantity INT NOT NULL, unit_price NUMERIC NOT NULL,
  status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft','approved','ordered','partially_received','received','cancelled')),
  approved_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT chk_po_qty CHECK (quantity > 0));
CREATE TABLE IF NOT EXISTS assets (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  asset_code TEXT NOT NULL UNIQUE, name TEXT NOT NULL, next_maintenance DATE,
  status TEXT NOT NULL DEFAULT 'operational' CHECK (status IN ('operational','under_maintenance','out_of_service','retired')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
ALTER TABLE dialysis_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE dialysis_sessions FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS dialysis_sessions_facility_isolation ON dialysis_sessions;
CREATE POLICY dialysis_sessions_facility_isolation ON dialysis_sessions FOR ALL
  USING (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)))
  WITH CHECK (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)));
ALTER TABLE chemo_cycles ENABLE ROW LEVEL SECURITY;
ALTER TABLE chemo_cycles FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS chemo_cycles_facility_isolation ON chemo_cycles;
CREATE POLICY chemo_cycles_facility_isolation ON chemo_cycles FOR ALL
  USING (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)))
  WITH CHECK (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)));
ALTER TABLE purchase_orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE purchase_orders FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS purchase_orders_facility_isolation ON purchase_orders;
CREATE POLICY purchase_orders_facility_isolation ON purchase_orders FOR ALL
  USING (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)))
  WITH CHECK (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)));
ALTER TABLE assets ENABLE ROW LEVEL SECURITY;
ALTER TABLE assets FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS assets_facility_isolation ON assets;
CREATE POLICY assets_facility_isolation ON assets FOR ALL
  USING (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)))
  WITH CHECK (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)));
GRANT SELECT, INSERT, UPDATE, DELETE ON dialysis_sessions, chemo_cycles, teleconsultations, death_records, medical_documents, purchase_orders, assets TO app_user;
