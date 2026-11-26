-- V29 — Module deepening round 6 (Catalog v6): tissues, organs,
-- cells & biological products 129; clinical trials & medical
-- innovation 130. Facility-scoped, platform RLS pattern.

-- ============ 129 Organs, tissues & biological products ============
CREATE TABLE IF NOT EXISTS donation_cases (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  donor_code TEXT NOT NULL, donation_type TEXT CHECK (donation_type IN ('organ','tissue','cells','biological_product')),
  organ_or_tissue TEXT,
  consent_basis TEXT CHECK (consent_basis IN ('donor_card','family_consent','legal_authority')),
  consent_documented BOOLEAN NOT NULL DEFAULT false,
  coercion_screened BOOLEAN NOT NULL DEFAULT false,
  incentive_flag BOOLEAN NOT NULL DEFAULT false,
  medical_evaluation TEXT, evaluation_complete BOOLEAN NOT NULL DEFAULT false,
  committee_decision TEXT,
  status TEXT NOT NULL DEFAULT 'evaluation' CHECK (status IN ('evaluation','approved','allocated','transplanted','discarded','blocked')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_donor_code ON donation_cases (facility_id, donor_code);

CREATE TABLE IF NOT EXISTS allocation_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  donation_id UUID NOT NULL REFERENCES donation_cases(id),
  recipient_ref TEXT, match_basis TEXT,
  allocated_by TEXT, committee_approved BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS custody_chain_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  donation_id UUID NOT NULL REFERENCES donation_cases(id),
  stage TEXT CHECK (stage IN ('procurement','preservation','transport','implantation')),
  custodian TEXT, temperature_c NUMERIC(5,2), event_at TIMESTAMPTZ,
  ischemia_start BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS graft_outcomes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  donation_id UUID NOT NULL REFERENCES donation_cases(id),
  recipient_ref TEXT, outcome TEXT, follow_up_note TEXT, recorded_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS organ_discard_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  donation_id UUID NOT NULL REFERENCES donation_cases(id),
  reason TEXT, investigated_by TEXT, investigation TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 130 Clinical trials & innovation ============
CREATE TABLE IF NOT EXISTS trial_protocols (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  title TEXT, phase TEXT, sponsor TEXT, protocol_version TEXT,
  principal_investigator TEXT,
  ethics_approval_ref TEXT, ethics_approved BOOLEAN NOT NULL DEFAULT false,
  regulatory_approval_ref TEXT, regulatory_approved BOOLEAN NOT NULL DEFAULT false,
  coi_declared BOOLEAN NOT NULL DEFAULT false,
  funding_ref TEXT,
  status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft','approved','recruiting','suspended','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS trial_participants (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  trial_id UUID NOT NULL REFERENCES trial_protocols(id),
  participant_code TEXT NOT NULL,
  eligibility_confirmed BOOLEAN NOT NULL DEFAULT false,
  consent_status TEXT NOT NULL DEFAULT 'not_consented' CHECK (consent_status IN ('not_consented','consented','withdrawn')),
  consent_at TIMESTAMPTZ, withdrawn_at TIMESTAMPTZ, withdrawal_reason TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_participant_code ON trial_participants (trial_id, participant_code);

CREATE TABLE IF NOT EXISTS trial_visits (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  participant_id UUID NOT NULL REFERENCES trial_participants(id),
  visit_label TEXT, scheduled_at TIMESTAMPTZ, completed_at TIMESTAMPTZ, samples_ref TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS trial_adverse_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  participant_id UUID NOT NULL REFERENCES trial_participants(id),
  description TEXT, severity TEXT CHECK (severity IN ('mild','moderate','severe','serious')),
  reported_to_ethics BOOLEAN NOT NULL DEFAULT false, action_taken TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['donation_cases','allocation_records','custody_chain_events','graft_outcomes','organ_discard_records',
                           'trial_protocols','trial_participants','trial_visits','trial_adverse_events']
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
