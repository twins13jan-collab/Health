-- V26 — Module deepening round 3 (Catalog v6): hygiene, environment &
-- medical waste 106; facility security, safety & civil protection
-- 107. Facility-scoped, platform RLS pattern.

-- ============ 106 Hygiene, environment & medical waste ============
CREATE TABLE IF NOT EXISTS cleaning_plans (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  area TEXT, risk_level TEXT CHECK (risk_level IN ('low','medium','high','critical')),
  frequency TEXT, responsible TEXT,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','suspended')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS cleaning_audits (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  plan_id UUID REFERENCES cleaning_plans(id),
  area TEXT, score_pct NUMERIC(5,2), passed BOOLEAN, auditor TEXT, findings TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS waste_manifests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  waste_type TEXT NOT NULL CHECK (waste_type IN ('infectious','sharps','chemical','pharmaceutical','pathological','general')),
  source_unit TEXT, weight_kg NUMERIC(10,2), containers INT,
  generated_at TIMESTAMPTZ, generated_by TEXT,
  contractor TEXT, contractor_license TEXT, license_valid_until DATE,
  collected_at TIMESTAMPTZ, collected_by TEXT,
  received_by TEXT, disposed_at TIMESTAMPTZ, disposal_method TEXT,
  status TEXT NOT NULL DEFAULT 'generated' CHECK (status IN ('generated','collected','disposed','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS environmental_checks (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  check_type TEXT NOT NULL CHECK (check_type IN ('air_quality','water_quality','pest_control')),
  location TEXT, result TEXT CHECK (result IN ('pass','fail')),
  measured_value TEXT, standard_ref TEXT, checked_by TEXT, action_taken TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS spill_incidents (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  substance TEXT, location TEXT, severity TEXT,
  reported_by TEXT, reported_at TIMESTAMPTZ,
  contained_at TIMESTAMPTZ, decontaminated BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'reported' CHECK (status IN ('reported','responding','contained','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 107 Facility security, safety & civil protection ============
CREATE TABLE IF NOT EXISTS fire_safety_plans (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  building TEXT, evacuation_routes TEXT, assembly_points TEXT,
  approved_by TEXT, review_due DATE,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','under_review')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS safety_inspections (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  system_type TEXT NOT NULL CHECK (system_type IN ('fire_alarm','extinguisher','medical_gas','generator','exit_lights','other')),
  location TEXT, result TEXT CHECK (result IN ('pass','fail')),
  defects TEXT, inspected_by TEXT, next_due DATE, corrective_action TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS hot_work_permits (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  work_type TEXT NOT NULL CHECK (work_type IN ('welding','electrical_isolation','confined_space','other_hot_work')),
  location TEXT, requested_by TEXT, approved_by TEXT,
  fire_watch BOOLEAN NOT NULL DEFAULT false, loto_applied BOOLEAN NOT NULL DEFAULT false,
  valid_from TIMESTAMPTZ, valid_until TIMESTAMPTZ,
  status TEXT NOT NULL DEFAULT 'requested' CHECK (status IN ('requested','approved','active','closed','rejected')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS facility_security_incidents (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  kind TEXT NOT NULL CHECK (kind IN ('assault','theft','asset_loss','intrusion','other')),
  location TEXT, severity TEXT, reported_by TEXT, action_taken TEXT,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','investigating','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS sensitive_access_grants (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  person TEXT, person_type TEXT CHECK (person_type IN ('visitor','contractor','staff')),
  sensitive_area TEXT, escort_required BOOLEAN NOT NULL DEFAULT true,
  escorted_by TEXT, approved_by TEXT,
  valid_from TIMESTAMPTZ, valid_until TIMESTAMPTZ,
  status TEXT NOT NULL DEFAULT 'granted' CHECK (status IN ('granted','revoked','expired')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS evacuation_drills (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  building TEXT, drill_date DATE, participants INT,
  evacuation_minutes NUMERIC(6,2), result TEXT CHECK (result IN ('pass','fail')),
  deficiencies TEXT, conducted_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['cleaning_plans','cleaning_audits','waste_manifests','environmental_checks','spill_incidents',
                           'fire_safety_plans','safety_inspections','hot_work_permits','facility_security_incidents','sensitive_access_grants','evacuation_drills']
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
