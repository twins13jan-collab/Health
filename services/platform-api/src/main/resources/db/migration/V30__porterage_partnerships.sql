-- V30 — Module deepening round 7 (Catalog v6): internal patient
-- transport & hotel services 109; partnerships & external service
-- providers 132. Facility-scoped, platform RLS pattern.

-- ============ 109 Internal transport & hotel services ============
CREATE TABLE IF NOT EXISTS transport_requests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT, from_location TEXT, to_location TEXT,
  priority TEXT CHECK (priority IN ('routine','urgent','critical')),
  escort_required BOOLEAN NOT NULL DEFAULT false,
  oxygen_required BOOLEAN NOT NULL DEFAULT false,
  escort_name TEXT, assigned_porter TEXT,
  requested_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  picked_up_at TIMESTAMPTZ, delivered_at TIMESTAMPTZ,
  status TEXT NOT NULL DEFAULT 'requested' CHECK (status IN ('requested','assigned','in_transit','completed','cancelled')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS room_turnover_tasks (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  room_ref TEXT, discharged_at TIMESTAMPTZ,
  cleaning_done BOOLEAN NOT NULL DEFAULT false,
  bed_ready BOOLEAN NOT NULL DEFAULT false,
  inspected_by TEXT, ready_at TIMESTAMPTZ,
  status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending','cleaning','ready')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS laundry_batches (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  batch_ref TEXT, batch_type TEXT CHECK (batch_type IN ('linen','uniform')),
  sent_count INT, returned_count INT,
  status TEXT NOT NULL DEFAULT 'sent' CHECK (status IN ('sent','returned','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS patient_service_requests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT, request_type TEXT CHECK (request_type IN ('meal','blanket','maintenance','other')),
  detail TEXT, fulfilled_at TIMESTAMPTZ,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','fulfilled','cancelled')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 132 Partnerships & external providers ============
CREATE TABLE IF NOT EXISTS partner_organizations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  name TEXT, partner_type TEXT CHECK (partner_type IN ('contractor','service_provider','concession')),
  coi_declared BOOLEAN NOT NULL DEFAULT false,
  ownership_note TEXT,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','suspended','terminated')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS partner_contracts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  partner_id UUID NOT NULL REFERENCES partner_organizations(id),
  scope TEXT, access_scope TEXT, purpose TEXT,
  starts_on DATE, ends_on DATE, sla_ref TEXT,
  privacy_terms BOOLEAN NOT NULL DEFAULT false,
  security_terms BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft','active','expired','terminated')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS partner_access_grants (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  partner_id UUID NOT NULL REFERENCES partner_organizations(id),
  system_ref TEXT, access_level TEXT, purpose TEXT,
  granted_by TEXT, expires_at TIMESTAMPTZ, revoked_at TIMESTAMPTZ,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','expired','revoked')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS partner_performance_reviews (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  partner_id UUID NOT NULL REFERENCES partner_organizations(id),
  period TEXT, sla_score NUMERIC(5,2), incidents_count INT,
  audit_result TEXT, reviewed_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['transport_requests','room_turnover_tasks','laundry_batches','patient_service_requests',
                           'partner_organizations','partner_contracts','partner_access_grants','partner_performance_reviews']
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
