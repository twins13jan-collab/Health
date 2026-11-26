-- Phase 4.4 (hospital operations): ward nursing operations —
-- a medication administration record (MAR) generated from the
-- patient's active prescriptions, and nursing rounds with vitals
-- and a server-computed NEWS2. Assessments, care plans, tasks and
-- handovers already exist in the nursing module; this batch adds
-- the medication and observation backbone of ward work.
CREATE TABLE IF NOT EXISTS med_administrations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL,
  governorate TEXT,
  patient_id UUID NOT NULL,
  admission_id UUID,
  prescription_id UUID,
  drug_name TEXT NOT NULL,
  dose TEXT,
  scheduled_at TIMESTAMPTZ NOT NULL,
  status TEXT NOT NULL DEFAULT 'scheduled',
  given_at TIMESTAMPTZ,
  recorded_by TEXT,
  reason TEXT,
  note TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_mar_patient_time ON med_administrations (patient_id, scheduled_at);
CREATE INDEX IF NOT EXISTS idx_mar_facility_time ON med_administrations (facility_id, scheduled_at);

CREATE TABLE IF NOT EXISTS nursing_rounds (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL,
  governorate TEXT,
  patient_id UUID NOT NULL,
  admission_id UUID,
  round_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  respiratory_rate INT,
  spo2 INT,
  on_oxygen BOOLEAN NOT NULL DEFAULT false,
  systolic_bp INT,
  pulse INT,
  temperature DOUBLE PRECISION,
  consciousness TEXT,
  pain_score INT,
  mobility TEXT,
  news2_total INT,
  news2_band TEXT,
  note TEXT,
  recorded_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_rounds_patient ON nursing_rounds (patient_id, round_at DESC);

GRANT SELECT, INSERT, UPDATE, DELETE ON med_administrations, nursing_rounds TO app_user;

INSERT INTO role_permissions (path_prefix, allowed_roles, domain_ar)
VALUES ('/api/nursing', 'nurse,physician,facility_manager', 'التمريض')
ON CONFLICT (path_prefix) DO NOTHING;
