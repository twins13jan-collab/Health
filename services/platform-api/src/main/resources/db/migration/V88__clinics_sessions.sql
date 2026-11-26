-- V88 — Phase 4.3: outpatient clinics & scheduling. Clinics, dated
-- doctor sessions with slot grids and capacity, and a per-session
-- waiting list with promotion on cancellation. Appointments gain a
-- session link so session bookings live in the same record as
-- free-form bookings.

CREATE TABLE IF NOT EXISTS clinics (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL,
  governorate TEXT,
  name_ar TEXT NOT NULL,
  name_en TEXT,
  specialty TEXT,
  room TEXT,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS clinic_sessions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  clinic_id UUID NOT NULL REFERENCES clinics(id),
  facility_id UUID NOT NULL,
  governorate TEXT,
  doctor_name TEXT NOT NULL,
  session_date DATE NOT NULL,
  starts_at TIME NOT NULL,
  ends_at TIME NOT NULL,
  slot_minutes INT NOT NULL DEFAULT 15,
  capacity INT NOT NULL,
  status TEXT NOT NULL DEFAULT 'scheduled',  -- scheduled, open, closed, cancelled
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_clinic_sessions_facility_date ON clinic_sessions(facility_id, session_date);

CREATE TABLE IF NOT EXISTS appointment_waitlist (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  session_id UUID NOT NULL REFERENCES clinic_sessions(id),
  facility_id UUID NOT NULL,
  governorate TEXT,
  patient_id UUID NOT NULL,
  reason TEXT,
  priority TEXT NOT NULL DEFAULT 'normal',  -- normal, urgent
  status TEXT NOT NULL DEFAULT 'waiting',   -- waiting, promoted, cancelled
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_waitlist_session ON appointment_waitlist(session_id, status);

ALTER TABLE appointments ADD COLUMN IF NOT EXISTS session_id UUID;
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS cancel_reason TEXT;

GRANT SELECT, INSERT, UPDATE, DELETE ON clinics, clinic_sessions, appointment_waitlist TO app_user;

INSERT INTO role_permissions (path_prefix, allowed_roles, domain_ar)
VALUES ('/api/clinics', 'receptionist,physician,nurse,facility_manager', 'العيادات الخارجية')
ON CONFLICT (path_prefix) DO NOTHING;
