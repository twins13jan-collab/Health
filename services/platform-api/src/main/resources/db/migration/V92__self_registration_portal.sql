-- Restructure wave (public portal): self-registration links a user
-- account to the citizen's unified patient record; facilities that
-- register themselves start as 'pending' until an administrator
-- approves them (Facility.status already exists). Service requests
-- from the patient portal (lab tests, pharmacy refills, other)
-- land in the facility's inbox.
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS patient_id UUID;

CREATE TABLE IF NOT EXISTS portal_service_requests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL,
  governorate TEXT,
  patient_id UUID NOT NULL,
  requested_by TEXT,
  service_type TEXT NOT NULL,
  details TEXT,
  preferred_date DATE,
  status TEXT NOT NULL DEFAULT 'requested',
  staff_note TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_portal_requests_facility ON portal_service_requests (facility_id, status, created_at);

GRANT SELECT, INSERT, UPDATE, DELETE ON portal_service_requests TO app_user;

INSERT INTO role_permissions (path_prefix, allowed_roles, domain_ar)
VALUES ('/api/portal', 'patient,receptionist,physician,nurse,facility_manager', 'بوابة المريض والخدمات')
ON CONFLICT (path_prefix) DO NOTHING;
