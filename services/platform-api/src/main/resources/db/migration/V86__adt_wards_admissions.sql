-- V86 — Phase 4 (hospital operations): real ADT — wards, inpatient
-- admissions with bed-level occupancy, and internal ward transfers.
-- The platform is the hospital's system of record: a bed cannot hold
-- two active admissions, discharge requires a disposition, and every
-- internal transfer carries a reason and an audit row.

CREATE TABLE IF NOT EXISTS wards (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  name_ar TEXT NOT NULL, name_en TEXT,
  ward_type TEXT NOT NULL DEFAULT 'general',  -- general, icu, nicu, er, observation, maternity, or_recovery, isolation
  total_beds INTEGER NOT NULL DEFAULT 0,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS inpatient_admissions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL, encounter_id UUID,
  ward_id UUID NOT NULL REFERENCES wards(id),
  bed_label TEXT NOT NULL,
  attending_name TEXT, admission_reason TEXT,
  source TEXT NOT NULL DEFAULT 'clinic',  -- er, clinic, referral, direct, transfer_in
  status TEXT NOT NULL DEFAULT 'admitted',  -- admitted, discharged
  admitted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  discharged_at TIMESTAMPTZ, discharge_disposition TEXT,  -- home, transfer_out, deceased, ama, rehab
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE INDEX IF NOT EXISTS idx_admissions_facility_status ON inpatient_admissions (facility_id, status);
CREATE INDEX IF NOT EXISTS idx_admissions_ward_status ON inpatient_admissions (ward_id, status);
CREATE INDEX IF NOT EXISTS idx_wards_facility ON wards (facility_id);

CREATE TABLE IF NOT EXISTS ward_transfers (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  admission_id UUID NOT NULL REFERENCES inpatient_admissions(id),
  from_ward_id UUID, to_ward_id UUID NOT NULL,
  from_bed TEXT, to_bed TEXT NOT NULL,
  reason TEXT NOT NULL, transferred_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

GRANT SELECT, INSERT, UPDATE ON wards, inpatient_admissions, ward_transfers TO app_user;

INSERT INTO role_permissions (path_prefix, allowed_roles, domain_ar) VALUES
  ('/api/adt', 'receptionist,physician,nurse,facility_manager', 'القبول والإقامة والتحويلات (ADT)')
ON CONFLICT (path_prefix) DO NOTHING;
