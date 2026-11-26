-- V40 — Module deepening round 16 (Catalog v6, tier C): nursing &
-- care plans 85; pain, procedural sedation & analgesia 86.
-- Facility-scoped, platform RLS pattern.

-- ============ 85 Nursing ============
CREATE TABLE IF NOT EXISTS nursing_assessments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT,
  fall_risk TEXT CHECK (fall_risk IN ('low','moderate','high')),
  pressure_ulcer_risk TEXT, pain_score INT, nutrition_risk TEXT, mobility TEXT,
  assessed_by TEXT, assessed_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS nursing_care_plans (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT, goal TEXT, interventions TEXT,
  reassessment_due DATE,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','completed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS nursing_tasks (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  care_plan_id UUID REFERENCES nursing_care_plans(id),
  patient_ref TEXT, task_type TEXT CHECK (task_type IN ('medication','fluids','blood','catheter','wound','assessment')),
  assigned_to TEXT, due_at TIMESTAMPTZ, completed_at TIMESTAMPTZ,
  status TEXT NOT NULL DEFAULT 'assigned' CHECK (status IN ('assigned','done','rejected','reassigned')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS shift_handovers (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  from_nurse TEXT, to_nurse TEXT, critical_cases_note TEXT,
  open_tasks_count INT, accepted BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 86 Pain & procedural sedation ============
CREATE TABLE IF NOT EXISTS pain_assessments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT, scale TEXT CHECK (scale IN ('NRS','VAS','FLACC','WONGBAKER')),
  score INT, age_group TEXT CHECK (age_group IN ('adult','child','preverbal')),
  assessed_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS analgesia_plans (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT, plan_type TEXT CHECK (plan_type IN ('acute','chronic','postoperative','palliative')),
  controlled_medication BOOLEAN NOT NULL DEFAULT false,
  prescribed_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS sedation_sessions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT, procedure_name TEXT,
  pre_assessment_done BOOLEAN NOT NULL DEFAULT false,
  airway_assessed BOOLEAN NOT NULL DEFAULT false,
  consent_documented BOOLEAN NOT NULL DEFAULT false,
  risk_class TEXT, aldrete_score INT,
  complications TEXT,
  status TEXT NOT NULL DEFAULT 'planned' CHECK (status IN ('planned','in_progress','recovery','discharged','complicated')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['nursing_assessments','nursing_care_plans','nursing_tasks','shift_handovers',
                           'pain_assessments','analgesia_plans','sedation_sessions']
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
