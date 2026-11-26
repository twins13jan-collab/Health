-- V27 — Module deepening round 4 (Catalog v6): citizen portal &
-- multi-channel communication 111; post-discharge follow-up &
-- continuity of care 184. Facility-scoped, platform RLS pattern.

-- ============ 111 Citizen portal ============
CREATE TABLE IF NOT EXISTS citizen_accounts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  citizen_ref TEXT NOT NULL, full_name TEXT,
  identity_verified BOOLEAN NOT NULL DEFAULT false, verification_method TEXT,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','suspended')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_citizen_ref ON citizen_accounts (facility_id, citizen_ref);

CREATE TABLE IF NOT EXISTS citizen_representatives (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  account_id UUID NOT NULL REFERENCES citizen_accounts(id),
  representative_name TEXT, relationship TEXT,
  scope TEXT CHECK (scope IN ('results','prescriptions','appointments','all')),
  valid_until DATE, approved BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS portal_disclosures (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  account_id UUID NOT NULL REFERENCES citizen_accounts(id),
  resource_type TEXT CHECK (resource_type IN ('lab_result','prescription','summary','vaccination')),
  resource_ref TEXT, accessed_by TEXT, channel TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS citizen_requests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  account_id UUID REFERENCES citizen_accounts(id),
  kind TEXT CHECK (kind IN ('inquiry','service_request','complaint')),
  subject TEXT, body TEXT, assigned_to TEXT, response TEXT,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','in_progress','answered','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS communication_preferences (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  account_id UUID NOT NULL REFERENCES citizen_accounts(id),
  channel TEXT CHECK (channel IN ('sms','whatsapp','email','call','app')),
  language TEXT, accessible_format TEXT CHECK (accessible_format IN ('plain','large_print','audio','braille')),
  clinical_alerts BOOLEAN NOT NULL DEFAULT false, enabled BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_pref_channel ON communication_preferences (account_id, channel);

CREATE TABLE IF NOT EXISTS outbound_messages (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  account_id UUID NOT NULL REFERENCES citizen_accounts(id),
  channel TEXT, template TEXT, clinical BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'queued' CHECK (status IN ('queued','sent','delivered','failed','blocked')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 184 Post-discharge follow-up ============
CREATE TABLE IF NOT EXISTS followup_plans (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT, discharge_ref TEXT, responsible TEXT, plan_summary TEXT,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','completed','escalated','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS followup_tasks (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  plan_id UUID NOT NULL REFERENCES followup_plans(id),
  kind TEXT CHECK (kind IN ('call','visit','lab_review','medication_check','education_check')),
  due_at TIMESTAMPTZ, completed_at TIMESTAMPTZ, outcome TEXT,
  contact_attempts INT NOT NULL DEFAULT 0,
  status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending','done','missed','escalated','unreachable')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS pending_results_reviews (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  plan_id UUID NOT NULL REFERENCES followup_plans(id),
  test_name TEXT, resulted_at TIMESTAMPTZ, abnormal BOOLEAN NOT NULL DEFAULT false,
  reviewed_by TEXT, action_taken TEXT, patient_notified BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS high_risk_medication_checks (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  plan_id UUID NOT NULL REFERENCES followup_plans(id),
  medication TEXT, check_type TEXT CHECK (check_type IN ('adherence','side_effects','interaction','dose')),
  finding TEXT, action TEXT, checked_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS continuity_referrals (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  plan_id UUID NOT NULL REFERENCES followup_plans(id),
  target TEXT CHECK (target IN ('primary_care','home_care','specialist')),
  referral_ref TEXT, accepted BOOLEAN NOT NULL DEFAULT false, accepted_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['citizen_accounts','citizen_representatives','portal_disclosures','citizen_requests','communication_preferences','outbound_messages',
                           'followup_plans','followup_tasks','pending_results_reviews','high_risk_medication_checks','continuity_referrals']
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
