-- V28 — Module deepening round 5 (Catalog v6): service eligibility
-- verification before booking 183; unknown-identity patients &
-- humanitarian cases 185. Facility-scoped, platform RLS pattern.

-- ============ 183 Eligibility before booking ============
CREATE TABLE IF NOT EXISTS service_availability_checks (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT, service_code TEXT, service_name TEXT,
  slot_at TIMESTAMPTZ,
  service_offered BOOLEAN NOT NULL DEFAULT false,
  license_valid BOOLEAN NOT NULL DEFAULT false,
  capacity_available BOOLEAN NOT NULL DEFAULT false,
  staff_available BOOLEAN NOT NULL DEFAULT false,
  equipment_available BOOLEAN NOT NULL DEFAULT false,
  consumables_available BOOLEAN NOT NULL DEFAULT false,
  preparation_met BOOLEAN NOT NULL DEFAULT false,
  clinical_eligible BOOLEAN NOT NULL DEFAULT false,
  coverage_status TEXT CHECK (coverage_status IN ('covered','preauth_required','not_covered','unknown')),
  result TEXT CHECK (result IN ('eligible','eligible_with_conditions','ineligible')),
  restrictions TEXT, checked_by TEXT, source TEXT,
  checked_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 185 Unknown-identity & humanitarian cases ============
CREATE TABLE IF NOT EXISTS temporary_identities (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  temp_code TEXT NOT NULL, description TEXT,
  circumstance TEXT CHECK (circumstance IN ('unconscious','found_person','separated_child','cognitive_impairment','other')),
  protection_flag BOOLEAN NOT NULL DEFAULT false,
  created_by TEXT,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','merged','deceased_unidentified','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_temp_code ON temporary_identities (facility_id, temp_code);

CREATE TABLE IF NOT EXISTS identity_merge_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  temp_id UUID NOT NULL REFERENCES temporary_identities(id),
  verified_patient_ref TEXT, verification_evidence TEXT,
  verified_by TEXT, merged_by TEXT, merged_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS identity_verification_chain (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  temp_id UUID NOT NULL REFERENCES temporary_identities(id),
  step TEXT, actor TEXT, detail TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS unidentified_death_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  temp_id UUID NOT NULL REFERENCES temporary_identities(id),
  legal_procedure_ref TEXT, authority_notified BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','identified','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['service_availability_checks','temporary_identities','identity_merge_records','identity_verification_chain','unidentified_death_records']
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
