-- V49 — Module deepening round 24 (Catalog v6, tier C): business
-- continuity & technical recovery 123; accreditation, licensing
-- & inspection 131. Facility-scoped, platform RLS pattern.

-- ============ 123 Business continuity & recovery ============
CREATE TABLE IF NOT EXISTS critical_services (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  name TEXT, impact_level TEXT,
  rto_minutes INT, rpo_minutes INT,
  backup_configured BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS restore_tests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  service_id UUID NOT NULL REFERENCES critical_services(id),
  tested_at DATE, actual_rto_minutes INT, actual_rpo_minutes INT,
  success BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS failover_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  service_id UUID NOT NULL REFERENCES critical_services(id),
  mode TEXT CHECK (mode IN ('alternate','primary_restored')),
  reason TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 131 Accreditation, licensing & inspection ============
CREATE TABLE IF NOT EXISTS licence_files (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  scope TEXT CHECK (scope IN ('facility','department','service','practice')),
  holder_ref TEXT, standard_ref TEXT,
  issued_at DATE, expires_at DATE,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','suspended','restricted','revoked')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS licence_inspections (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  licence_id UUID NOT NULL REFERENCES licence_files(id),
  inspector TEXT, findings TEXT,
  corrective_actions TEXT, corrective_done BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS service_restrictions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  licence_id UUID NOT NULL REFERENCES licence_files(id),
  restriction_type TEXT CHECK (restriction_type IN ('suspend','restrict')),
  decided_by TEXT, authority TEXT, reason TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['critical_services','restore_tests','failover_events',
                           'licence_files','licence_inspections','service_restrictions']
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
