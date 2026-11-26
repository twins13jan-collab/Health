-- Phase 4.5 (hospital operations): operating theatres as managed
-- resources, the surgical waiting list with clinical priority, and
-- the anaesthesia record that completes the case. The surgical
-- case itself and the WHO checklist already exist (cycle 220);
-- cases gain a room link and a documented status reason.
CREATE TABLE IF NOT EXISTS operating_rooms (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL,
  governorate TEXT,
  name_ar TEXT NOT NULL,
  name_en TEXT,
  specialty TEXT,
  is_active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS surgical_waitlist (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL,
  governorate TEXT,
  patient_id UUID NOT NULL,
  procedure_name TEXT NOT NULL,
  specialty TEXT,
  priority TEXT NOT NULL DEFAULT 'elective',
  requested_by TEXT,
  notes TEXT,
  status TEXT NOT NULL DEFAULT 'waiting',
  scheduled_case_id UUID,
  cancel_reason TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_waitlist_facility ON surgical_waitlist (facility_id, status, created_at);

CREATE TABLE IF NOT EXISTS anaesthesia_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL,
  case_id UUID NOT NULL UNIQUE,
  patient_id UUID NOT NULL,
  asa_class INT,
  anaesthesia_type TEXT,
  anaesthetist TEXT,
  preop_assessment BOOLEAN NOT NULL DEFAULT false,
  started_at TIMESTAMPTZ,
  ended_at TIMESTAMPTZ,
  complications TEXT,
  notes TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

ALTER TABLE surgical_cases ADD COLUMN IF NOT EXISTS room_id UUID;
ALTER TABLE surgical_cases ADD COLUMN IF NOT EXISTS status_reason TEXT;

GRANT SELECT, INSERT, UPDATE, DELETE ON operating_rooms, surgical_waitlist, anaesthesia_records TO app_user;

INSERT INTO role_permissions (path_prefix, allowed_roles, domain_ar)
VALUES ('/api/or', 'physician,nurse,facility_manager', 'غرف العمليات')
ON CONFLICT (path_prefix) DO NOTHING;
