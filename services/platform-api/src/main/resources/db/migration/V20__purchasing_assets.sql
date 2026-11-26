-- V20 — Batch 19 (Catalog v6): procurement, contracts, receiving &
-- suppliers 227, medical assets, maintenance & calibration 230.
-- Every table is facility-scoped with the platform RLS pattern.

-- ============ 227 Suppliers, requests, tenders, orders, receipts ============
CREATE TABLE IF NOT EXISTS suppliers (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  legal_name TEXT NOT NULL, registration_no TEXT,
  bank_account_ref TEXT, bank_verified BOOLEAN NOT NULL DEFAULT false,
  bank_changed_by TEXT, bank_verified_by TEXT,
  risk_level TEXT, status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','suspended')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS purchase_requests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  requester TEXT NOT NULL, item_description TEXT NOT NULL, specs TEXT,
  quantity INT NOT NULL DEFAULT 1, justification TEXT,
  approved_by TEXT,
  status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft','approved','rejected')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS tenders (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  request_id UUID REFERENCES purchase_requests(id),
  title TEXT NOT NULL, committee_minutes TEXT,
  awarded_bid_id UUID,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','evaluated','awarded','cancelled')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS bids (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  tender_id UUID NOT NULL REFERENCES tenders(id),
  supplier_id UUID NOT NULL REFERENCES suppliers(id),
  amount NUMERIC(14,2) NOT NULL DEFAULT 0, score NUMERIC(5,2),
  scored_by TEXT,
  status TEXT NOT NULL DEFAULT 'submitted' CHECK (status IN ('submitted','evaluated','awarded','rejected')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS proc_purchase_orders (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  request_id UUID REFERENCES purchase_requests(id),
  tender_id UUID REFERENCES tenders(id),
  supplier_id UUID NOT NULL REFERENCES suppliers(id),
  items TEXT, total_amount NUMERIC(14,2) NOT NULL DEFAULT 0, taxes NUMERIC(14,2) NOT NULL DEFAULT 0,
  delivery_terms TEXT, warranty TEXT,
  issued_by TEXT, version INT NOT NULL DEFAULT 1, amendment_of UUID,
  status TEXT NOT NULL DEFAULT 'issued' CHECK (status IN ('issued','amended','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS contracts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  po_id UUID NOT NULL REFERENCES proc_purchase_orders(id),
  terms TEXT, version INT NOT NULL DEFAULT 1, supersedes_id UUID,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','superseded')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS goods_receipts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  po_id UUID NOT NULL REFERENCES proc_purchase_orders(id),
  received_by TEXT NOT NULL, quantity_received INT NOT NULL DEFAULT 0,
  partial BOOLEAN NOT NULL DEFAULT false, substitute BOOLEAN NOT NULL DEFAULT false,
  technical_approval TEXT, contract_approval TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS inspections (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  receipt_id UUID NOT NULL REFERENCES goods_receipts(id),
  inspector TEXT NOT NULL, result TEXT NOT NULL CHECK (result IN ('accepted','rejected','conditional')),
  notes TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS supplier_invoices (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  po_id UUID NOT NULL REFERENCES proc_purchase_orders(id),
  receipt_id UUID REFERENCES goods_receipts(id),
  amount NUMERIC(14,2) NOT NULL DEFAULT 0,
  held_reason TEXT, paid_by TEXT,
  status TEXT NOT NULL DEFAULT 'received' CHECK (status IN ('received','matched','held','paid')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS credit_notes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  invoice_id UUID NOT NULL REFERENCES supplier_invoices(id),
  amount NUMERIC(14,2) NOT NULL DEFAULT 0, reason TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 230 Medical assets, lifecycle, maintenance, calibration ============
CREATE TABLE IF NOT EXISTS medical_assets (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  asset_code TEXT NOT NULL, type TEXT, manufacturer TEXT, model TEXT,
  serial_number TEXT, location TEXT, owner TEXT,
  calibration_due DATE,
  status TEXT NOT NULL DEFAULT 'in_service' CHECK (status IN ('in_service','out_of_service','under_maintenance','quarantined','retired')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_asset_code ON medical_assets (facility_id, asset_code);
CREATE UNIQUE INDEX IF NOT EXISTS uq_asset_serial ON medical_assets (facility_id, serial_number) WHERE serial_number IS NOT NULL;

CREATE TABLE IF NOT EXISTS asset_lifecycle_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  asset_id UUID NOT NULL REFERENCES medical_assets(id),
  event_type TEXT NOT NULL CHECK (event_type IN ('purchase','receipt','install','transfer','commission','maintenance','calibration','retire')),
  details TEXT, actor TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS maintenance_plans (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  asset_id UUID NOT NULL REFERENCES medical_assets(id),
  plan_type TEXT NOT NULL CHECK (plan_type IN ('preventive','corrective')),
  interval_days INT, next_due DATE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS work_orders (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  asset_id UUID NOT NULL REFERENCES medical_assets(id),
  type TEXT NOT NULL, technician TEXT, spare_parts TEXT,
  spare_part_shortage BOOLEAN NOT NULL DEFAULT false,
  spare_purchase_ref TEXT,
  downtime_hours NUMERIC(8,2), result TEXT,
  released_by TEXT,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','completed','released')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS calibration_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  asset_id UUID NOT NULL REFERENCES medical_assets(id),
  reference_standard TEXT, certificate_ref TEXT, deviation TEXT,
  accepted BOOLEAN NOT NULL DEFAULT false, due_date DATE,
  approved_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS device_incidents (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  asset_id UUID NOT NULL REFERENCES medical_assets(id),
  reporter TEXT, description TEXT,
  patient_affected BOOLEAN NOT NULL DEFAULT false,
  safety_case_ref TEXT, investigation TEXT,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','investigating','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['suppliers','purchase_requests','tenders','bids','proc_purchase_orders','contracts','goods_receipts','inspections','supplier_invoices','credit_notes',
                           'medical_assets','asset_lifecycle_events','maintenance_plans','work_orders','calibration_records','device_incidents']
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
