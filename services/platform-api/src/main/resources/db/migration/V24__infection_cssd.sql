-- V24 — Module deepening round 2 (Catalog v6): infectious diseases,
-- isolation & infection control 91; central sterilization (CSSD) &
-- instrument reprocessing 105. Facility-scoped, platform RLS pattern.

-- ============ 91 Infection prevention & control ============
CREATE TABLE IF NOT EXISTS infection_cases (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT, organism TEXT,
  case_type TEXT NOT NULL DEFAULT 'suspected' CHECK (case_type IN ('suspected','confirmed')),
  source TEXT CHECK (source IN ('community','hai')),
  hai_type TEXT CHECK (hai_type IN ('CLABSI','CAUTI','SSI','VAP','other')),
  unit TEXT, onset_date DATE,
  notifiable BOOLEAN NOT NULL DEFAULT false, notified_at TIMESTAMPTZ,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','isolated','resolved','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS isolation_orders (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  case_id UUID REFERENCES infection_cases(id),
  patient_ref TEXT,
  precaution_type TEXT NOT NULL CHECK (precaution_type IN ('standard','contact','droplet','airborne')),
  location TEXT, location_type TEXT,
  ppe_required TEXT, ordered_by TEXT,
  discontinued_by TEXT, discontinued_at TIMESTAMPTZ,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','discontinued')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS contact_tracings (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  case_id UUID NOT NULL REFERENCES infection_cases(id),
  contact_ref TEXT, exposure_type TEXT, risk_level TEXT,
  follow_up_status TEXT NOT NULL DEFAULT 'pending' CHECK (follow_up_status IN ('pending','monitoring','tested','cleared','converted')),
  test_result TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS outbreak_investigations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  title TEXT, unit TEXT, suspected_organism TEXT, cases_count INT NOT NULL DEFAULT 0,
  control_measures TEXT, opened_by TEXT,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','controlled','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS hai_surveillance (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  unit TEXT, period_key TEXT, hai_type TEXT,
  device_days INT NOT NULL DEFAULT 0, infections_count INT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_hai_period ON hai_surveillance (facility_id, unit, period_key, hai_type);

CREATE TABLE IF NOT EXISTS antimicrobial_reviews (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT, antibiotic TEXT, indication TEXT,
  culture_result TEXT, narrower_option BOOLEAN NOT NULL DEFAULT false,
  decision TEXT CHECK (decision IN ('continue','de_escalate','stop','change')),
  reviewed_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 105 CSSD ============
CREATE TABLE IF NOT EXISTS instrument_sets (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  set_code TEXT NOT NULL, name TEXT, contains_implants BOOLEAN NOT NULL DEFAULT false,
  storage_location TEXT, expiry_date DATE,
  status TEXT NOT NULL DEFAULT 'in_use' CHECK (status IN ('in_use','in_process','sterile','issued','quarantined','recalled')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_set_code ON instrument_sets (facility_id, set_code);

CREATE TABLE IF NOT EXISTS reprocessing_cycles (
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

CREATE TABLE IF NOT EXISTS sterilizer_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  sterilizer_code TEXT NOT NULL, maintenance_due DATE, calibration_due DATE,
  status TEXT NOT NULL DEFAULT 'operational' CHECK (status IN ('operational','maintenance','out_of_service')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_sterilizer ON sterilizer_records (facility_id, sterilizer_code);

CREATE TABLE IF NOT EXISTS set_usage_links (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  set_id UUID NOT NULL REFERENCES instrument_sets(id),
  procedure_ref TEXT, patient_ref TEXT, used_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS sterilization_failures (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  cycle_id UUID REFERENCES reprocessing_cycles(id),
  sterilizer_code TEXT, failure_type TEXT, affected_sets TEXT,
  recall_issued BOOLEAN NOT NULL DEFAULT false, investigation TEXT,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','investigating','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['infection_cases','isolation_orders','contact_tracings','outbreak_investigations','hai_surveillance','antimicrobial_reviews',
                           'instrument_sets','reprocessing_cycles','sterilizer_records','set_usage_links','sterilization_failures']
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
