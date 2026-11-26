-- V3 — Batch 2 (Catalog v6): diagnostics cycle 219, pharmacy 222,
-- blood bank 223, referrals 224. Capacity board (225) is read-model only.
CREATE TABLE IF NOT EXISTS lab_orders (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id), encounter_id UUID REFERENCES encounters(id),
  test_code TEXT NOT NULL, test_name TEXT,
  priority TEXT NOT NULL DEFAULT 'routine',
  status TEXT NOT NULL DEFAULT 'ordered' CHECK (status IN ('ordered','specimen_collected','received','in_analysis','resulted','verified','corrected','cancelled')),
  ordered_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_lab_orders_facility_status ON lab_orders (facility_id, status);
CREATE TABLE IF NOT EXISTS lab_results (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  order_id UUID NOT NULL REFERENCES lab_orders(id),
  value NUMERIC NOT NULL, unit TEXT, ref_low NUMERIC, ref_high NUMERIC,
  is_critical BOOLEAN NOT NULL DEFAULT false,
  version INT NOT NULL DEFAULT 1, supersedes_id UUID REFERENCES lab_results(id),
  status TEXT NOT NULL DEFAULT 'resulted' CHECK (status IN ('resulted','verified','corrected')),
  verified_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_lab_results_order ON lab_results (order_id, version);
CREATE TABLE IF NOT EXISTS prescriptions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id), encounter_id UUID REFERENCES encounters(id),
  drug_code TEXT NOT NULL, drug_name TEXT, dose TEXT, frequency TEXT, duration TEXT,
  status TEXT NOT NULL DEFAULT 'prescribed' CHECK (status IN ('prescribed','verified','dispensed','partially_dispensed','returned','cancelled')),
  prescribed_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_prescriptions_facility_status ON prescriptions (facility_id, status);
CREATE INDEX IF NOT EXISTS idx_prescriptions_patient ON prescriptions (patient_id, status);
CREATE TABLE IF NOT EXISTS blood_units (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  unit_code TEXT NOT NULL UNIQUE, blood_group TEXT NOT NULL, component TEXT NOT NULL DEFAULT 'RBC',
  donated_at DATE, expires_at DATE,
  status TEXT NOT NULL DEFAULT 'available' CHECK (status IN ('available','reserved','issued','transfused','expired','discarded')),
  reserved_for_patient UUID REFERENCES patients(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_blood_units_facility_status ON blood_units (facility_id, status);
CREATE TABLE IF NOT EXISTS referrals (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id), to_facility_id UUID NOT NULL REFERENCES facilities(id),
  reason TEXT NOT NULL, urgency TEXT NOT NULL DEFAULT 'routine',
  status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending','accepted','rejected','in_transit','arrived','completed','cancelled')),
  accepted_by TEXT, rejection_reason TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_referrals_facility_status ON referrals (facility_id, status);
CREATE INDEX IF NOT EXISTS idx_referrals_target ON referrals (to_facility_id, status);
ALTER TABLE lab_orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE lab_orders FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS lab_orders_facility_isolation ON lab_orders;
CREATE POLICY lab_orders_facility_isolation ON lab_orders FOR ALL
  USING (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)))
  WITH CHECK (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)));
ALTER TABLE lab_results ENABLE ROW LEVEL SECURITY;
ALTER TABLE lab_results FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS lab_results_facility_isolation ON lab_results;
CREATE POLICY lab_results_facility_isolation ON lab_results FOR ALL
  USING (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)))
  WITH CHECK (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)));
ALTER TABLE prescriptions ENABLE ROW LEVEL SECURITY;
ALTER TABLE prescriptions FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS prescriptions_facility_isolation ON prescriptions;
CREATE POLICY prescriptions_facility_isolation ON prescriptions FOR ALL
  USING (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)))
  WITH CHECK (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)));
ALTER TABLE blood_units ENABLE ROW LEVEL SECURITY;
ALTER TABLE blood_units FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS blood_units_facility_isolation ON blood_units;
CREATE POLICY blood_units_facility_isolation ON blood_units FOR ALL
  USING (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)))
  WITH CHECK (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)));
ALTER TABLE referrals ENABLE ROW LEVEL SECURITY;
ALTER TABLE referrals FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS referrals_facility_isolation ON referrals;
CREATE POLICY referrals_facility_isolation ON referrals FOR ALL
  USING (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)))
  WITH CHECK (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)));
GRANT SELECT, INSERT, UPDATE, DELETE ON lab_orders, lab_results, prescriptions, blood_units, referrals TO app_user;
