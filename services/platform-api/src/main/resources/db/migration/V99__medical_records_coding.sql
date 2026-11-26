-- Phase 4.9: medical records & coding. Encounter-level closing
-- summaries and ICD-10 coding (distinct from admissionsdeep
-- discharge_summaries, which document inpatient admission requests).
CREATE TABLE IF NOT EXISTS encounter_summaries (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL,
  governorate TEXT,
  encounter_id UUID NOT NULL UNIQUE,
  patient_id UUID NOT NULL,
  summary_text TEXT NOT NULL,
  author TEXT,
  status TEXT NOT NULL DEFAULT 'draft',
  finalized_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_encounter_summaries_facility ON encounter_summaries (facility_id, status);

CREATE TABLE IF NOT EXISTS encounter_codings (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL,
  governorate TEXT,
  encounter_id UUID NOT NULL UNIQUE,
  patient_id UUID NOT NULL,
  primary_icd10 TEXT NOT NULL,
  secondary_icd10 TEXT,
  coded_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_encounter_codings_facility ON encounter_codings (facility_id);

GRANT SELECT, INSERT, UPDATE, DELETE ON encounter_summaries, encounter_codings TO app_user;

INSERT INTO role_permissions (path_prefix, allowed_roles, domain_ar)
VALUES ('/api/medical-records', 'receptionist,physician,nurse,facility_manager', 'السجلات الطبية والترميز')
ON CONFLICT (path_prefix) DO NOTHING;
