-- V15 — Batch 14 (Catalog v6): pathology, histology & cytology 240,
-- endoscopy & interventional procedures 241.
-- Every clinical table is facility-scoped with the platform RLS pattern.

-- ============ 240 Pathology ============
CREATE TABLE IF NOT EXISTS pathology_orders (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  requesting_physician TEXT, reason TEXT, site TEXT,
  specimen_type TEXT NOT NULL, urgency TEXT,
  status TEXT NOT NULL DEFAULT 'ordered' CHECK (status IN ('ordered','received','in_processing','reported','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_path_order_patient ON pathology_orders (patient_id, created_at DESC);

CREATE TABLE IF NOT EXISTS specimen_accessions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  order_id UUID NOT NULL REFERENCES pathology_orders(id),
  accession_number TEXT NOT NULL, container_type TEXT, fixative TEXT,
  collected_at TIMESTAMPTZ, received_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  identity_verified BOOLEAN NOT NULL DEFAULT false,
  insufficiency_note TEXT,
  status TEXT NOT NULL DEFAULT 'received' CHECK (status IN ('received','quarantined','insufficient','accepted','disposed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_accession_number ON specimen_accessions (facility_id, accession_number);

CREATE TABLE IF NOT EXISTS blocks_slides (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  accession_id UUID NOT NULL REFERENCES specimen_accessions(id),
  block_code TEXT, slide_code TEXT, stain TEXT, image_ref TEXT,
  status TEXT NOT NULL DEFAULT 'prepared' CHECK (status IN ('prepared','ready')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS pathology_reports (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  accession_id UUID NOT NULL REFERENCES specimen_accessions(id),
  kind TEXT NOT NULL CHECK (kind IN ('preliminary','final','addendum')),
  report_text TEXT, diagnosis TEXT, classification TEXT, certainty TEXT,
  author TEXT, reviewer TEXT, signed BOOLEAN NOT NULL DEFAULT false,
  version INT NOT NULL DEFAULT 1,
  supersedes_id UUID REFERENCES pathology_reports(id),
  amendment_reason TEXT,
  status TEXT NOT NULL DEFAULT 'current' CHECK (status IN ('current','superseded')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_path_report_acc ON pathology_reports (accession_id, created_at DESC);

CREATE TABLE IF NOT EXISTS pathology_second_opinions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  report_id UUID NOT NULL REFERENCES pathology_reports(id),
  pathologist TEXT NOT NULL, opinion TEXT NOT NULL, review_decision TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 241 Endoscopy ============
CREATE TABLE IF NOT EXISTS endoscopy_requests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  scope_type TEXT NOT NULL, indication TEXT, urgency TEXT, preparation_instructions TEXT,
  status TEXT NOT NULL DEFAULT 'requested' CHECK (status IN ('requested','prepared','in_procedure','completed','postponed','cancelled')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_endo_req_patient ON endoscopy_requests (patient_id, created_at DESC);

CREATE TABLE IF NOT EXISTS pre_procedure_checklists (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  request_id UUID NOT NULL REFERENCES endoscopy_requests(id),
  fasting_confirmed BOOLEAN NOT NULL DEFAULT false,
  medications_reviewed BOOLEAN NOT NULL DEFAULT false,
  allergy_checked BOOLEAN NOT NULL DEFAULT false,
  consent_documented BOOLEAN NOT NULL DEFAULT false,
  sedation_assessment TEXT,
  completed BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS scope_assets (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  scope_code TEXT NOT NULL, scope_type TEXT,
  status TEXT NOT NULL DEFAULT 'available' CHECK (status IN ('available','in_use','reprocessing','quarantined','maintenance')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_scope_code ON scope_assets (facility_id, scope_code);

CREATE TABLE IF NOT EXISTS reprocessing_cycles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  scope_id UUID NOT NULL REFERENCES scope_assets(id),
  cleaning_done BOOLEAN NOT NULL DEFAULT false, disinfection_done BOOLEAN NOT NULL DEFAULT false,
  leak_test_passed BOOLEAN NOT NULL DEFAULT false,
  operator TEXT,
  status TEXT NOT NULL CHECK (status IN ('passed','failed')),
  completed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_repro_scope ON reprocessing_cycles (scope_id, completed_at DESC);

CREATE TABLE IF NOT EXISTS endoscopy_procedures (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  request_id UUID NOT NULL REFERENCES endoscopy_requests(id),
  scope_id UUID NOT NULL REFERENCES scope_assets(id),
  reprocessing_cycle_id UUID REFERENCES reprocessing_cycles(id),
  physician TEXT, sites TEXT, findings TEXT, interventions TEXT, complications TEXT,
  specimen_sent BOOLEAN NOT NULL DEFAULT false,
  specimen_label_verified BOOLEAN NOT NULL DEFAULT false,
  follow_up_plan TEXT, discharge_instructions TEXT,
  started_at TIMESTAMPTZ NOT NULL DEFAULT now(), ended_at TIMESTAMPTZ,
  status TEXT NOT NULL DEFAULT 'in_progress' CHECK (status IN ('in_progress','completed','complicated')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS sedation_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  procedure_id UUID NOT NULL REFERENCES endoscopy_procedures(id),
  drugs TEXT, monitoring TEXT,
  aldrete_score INT CHECK (aldrete_score BETWEEN 0 AND 10),
  discharge_decision TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['pathology_orders','specimen_accessions','blocks_slides','pathology_reports','pathology_second_opinions',
                           'endoscopy_requests','pre_procedure_checklists','scope_assets','reprocessing_cycles','endoscopy_procedures','sedation_records']
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
