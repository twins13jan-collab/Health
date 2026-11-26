-- V35 — Module deepening round 12 (Catalog v6): medication delivery
-- to the patient 152; organizational change & adoption 162.
-- Facility-scoped, platform RLS pattern.

-- ============ 152 Medication delivery to the patient ============
CREATE TABLE IF NOT EXISTS delivery_orders (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT, prescription_ref TEXT, medication_name TEXT,
  eligibility_verified BOOLEAN NOT NULL DEFAULT false,
  pharmacist_reviewed_by TEXT, pharmacist_approved BOOLEAN NOT NULL DEFAULT false,
  special_handling BOOLEAN NOT NULL DEFAULT false,
  cold_chain_required BOOLEAN NOT NULL DEFAULT false,
  address_ref TEXT,
  status TEXT NOT NULL DEFAULT 'requested' CHECK (status IN ('requested','reviewed','prepared','dispatched','delivered','failed','returned')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS delivery_attempts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  order_id UUID NOT NULL REFERENCES delivery_orders(id),
  attempt_no INT, outcome TEXT CHECK (outcome IN ('delivered','failed')),
  reason TEXT, temperature_ok BOOLEAN,
  received_by TEXT, identity_verified BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS delivery_returns (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  order_id UUID NOT NULL REFERENCES delivery_orders(id),
  reason TEXT, disposition TEXT CHECK (disposition IN ('restock','quarantine','destroy')),
  pharmacist_received TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 162 Organizational change & adoption ============
CREATE TABLE IF NOT EXISTS change_initiatives (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  title TEXT, scope TEXT, impact_analysis TEXT,
  sop_updated BOOLEAN NOT NULL DEFAULT false,
  affected_users INT,
  status TEXT NOT NULL DEFAULT 'planned' CHECK (status IN ('planned','communicating','training','live','adopted')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS change_communications (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  initiative_id UUID NOT NULL REFERENCES change_initiatives(id),
  audience TEXT, channel TEXT, sent_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS training_completions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  initiative_id UUID NOT NULL REFERENCES change_initiatives(id),
  user_ref TEXT, user_role TEXT,
  completed BOOLEAN NOT NULL DEFAULT false, completed_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS change_champions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  initiative_id UUID NOT NULL REFERENCES change_initiatives(id),
  champion TEXT, department TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS user_feedback_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  initiative_id UUID NOT NULL REFERENCES change_initiatives(id),
  user_ref TEXT, sentiment TEXT CHECK (sentiment IN ('positive','neutral','negative')),
  comment TEXT, addressed BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS adoption_metrics (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  initiative_id UUID NOT NULL REFERENCES change_initiatives(id),
  active_users_pct NUMERIC(5,2), workaround_reports INT,
  measured_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['delivery_orders','delivery_attempts','delivery_returns',
                           'change_initiatives','change_communications','training_completions','change_champions','user_feedback_items','adoption_metrics']
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
