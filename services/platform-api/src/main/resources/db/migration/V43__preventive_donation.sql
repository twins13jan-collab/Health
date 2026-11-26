-- V43 — Module deepening round 19 (Catalog v6, tier C): preventive
-- medicine & mass screening 151; blood donation campaigns 155.
-- Facility-scoped, platform RLS pattern.

-- ============ 151 Preventive medicine & screening ============
CREATE TABLE IF NOT EXISTS screening_programs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  name TEXT, target_condition TEXT, eligibility_criteria TEXT,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS screening_invitations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  program_id UUID NOT NULL REFERENCES screening_programs(id),
  person_ref TEXT, eligible BOOLEAN NOT NULL DEFAULT false,
  consent_given BOOLEAN NOT NULL DEFAULT false,
  appointment_at TIMESTAMPTZ,
  attendance TEXT NOT NULL DEFAULT 'invited' CHECK (attendance IN ('invited','attended','no_show')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS screening_results (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  invitation_id UUID NOT NULL REFERENCES screening_invitations(id),
  result TEXT CHECK (result IN ('negative','positive')),
  confirmatory_done BOOLEAN NOT NULL DEFAULT false,
  referral_created BOOLEAN NOT NULL DEFAULT false,
  pathway_closed BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 155 Blood donation campaigns ============
CREATE TABLE IF NOT EXISTS donation_campaigns (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  name TEXT, site TEXT, capacity INT, campaign_date DATE,
  units_collected INT NOT NULL DEFAULT 0,
  status TEXT NOT NULL DEFAULT 'planned' CHECK (status IN ('planned','running','completed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS donor_registrations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  campaign_id UUID NOT NULL REFERENCES donation_campaigns(id),
  donor_ref TEXT, last_donation_date DATE,
  age_ok BOOLEAN, weight_ok BOOLEAN, hemoglobin_ok BOOLEAN, health_ok BOOLEAN,
  eligible BOOLEAN NOT NULL DEFAULT false, deferred_reason TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS donation_results (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  registration_id UUID NOT NULL REFERENCES donor_registrations(id),
  test_result TEXT CHECK (test_result IN ('cleared','reactive')),
  donor_notified_confidentially BOOLEAN NOT NULL DEFAULT false,
  referral_created BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS unit_allocations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  campaign_id UUID NOT NULL REFERENCES donation_campaigns(id),
  allocated_to TEXT, units_allocated INT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['screening_programs','screening_invitations','screening_results',
                           'donation_campaigns','donor_registrations','donation_results','unit_allocations']
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
