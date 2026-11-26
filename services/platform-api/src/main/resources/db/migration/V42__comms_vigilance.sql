-- V42 — Module deepening round 18 (Catalog v6, tier C): clinical
-- communication & critical-information handover 161; medical
-- device vigilance 168. Facility-scoped, platform RLS pattern.

-- ============ 161 Clinical communication ============
CREATE TABLE IF NOT EXISTS critical_alerts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT, from_role TEXT, to_role TEXT,
  message_type TEXT CHECK (message_type IN ('critical_result','urgent_task','handoff')),
  content TEXT, channel TEXT CHECK (channel IN ('system','phone','in_person')),
  fallback_used BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'sent' CHECK (status IN ('sent','acknowledged','read_back','closed')),
  sent_at TIMESTAMPTZ, acknowledged_at TIMESTAMPTZ,
  read_back_confirmed BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS alert_escalations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  alert_id UUID NOT NULL REFERENCES critical_alerts(id),
  escalated_to TEXT, reason TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 168 Device vigilance ============
CREATE TABLE IF NOT EXISTS device_incident_reports (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  device_ref TEXT, serial_no TEXT, lot_no TEXT,
  incident_type TEXT CHECK (incident_type IN ('malfunction','harm','near_miss')),
  severity TEXT CHECK (severity IN ('minor','serious','death')),
  description TEXT, reported_by TEXT,
  linked_maintenance_ref TEXT, training_need BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'reported' CHECK (status IN ('reported','assessed','action_taken','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS vigilance_assessments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  report_id UUID NOT NULL REFERENCES device_incident_reports(id),
  risk_level TEXT, regulatory_report_required BOOLEAN NOT NULL DEFAULT false,
  regulatory_reported BOOLEAN NOT NULL DEFAULT false, regulatory_reported_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS field_safety_actions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  report_id UUID NOT NULL REFERENCES device_incident_reports(id),
  action_type TEXT CHECK (action_type IN ('correction','recall','quarantine')),
  scope TEXT, status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','completed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS device_tracking (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  device_ref TEXT, serial_no TEXT, location TEXT,
  status TEXT NOT NULL DEFAULT 'in_service' CHECK (status IN ('in_service','quarantined','recalled','in_maintenance')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['critical_alerts','alert_escalations',
                           'device_incident_reports','vigilance_assessments','field_safety_actions','device_tracking']
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
