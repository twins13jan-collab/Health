-- V8 — Batch 7 (clinical deepening): allergies, problem list, clinical notes.
CREATE TABLE IF NOT EXISTS allergies (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  allergen TEXT NOT NULL, allergen_class TEXT,
  severity TEXT NOT NULL DEFAULT 'moderate' CHECK (severity IN ('mild','moderate','severe','life_threatening')),
  reaction TEXT,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','resolved','entered_in_error')),
  recorded_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_allergies_patient ON allergies (patient_id, status);
CREATE TABLE IF NOT EXISTS problem_list (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  icd10_code TEXT NOT NULL, description TEXT NOT NULL, onset_date DATE,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','resolved','inactive')),
  recorded_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_problems_patient ON problem_list (patient_id, status);
CREATE TABLE IF NOT EXISTS clinical_notes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id), encounter_id UUID REFERENCES encounters(id),
  note_type TEXT NOT NULL DEFAULT 'progress',
  subjective TEXT, objective TEXT, assessment TEXT, plan TEXT,
  author_id TEXT,
  status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft','signed','amended')),
  signed_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_notes_patient ON clinical_notes (patient_id, created_at);
GRANT SELECT, INSERT, UPDATE, DELETE ON allergies, problem_list, clinical_notes TO app_user;
