-- V14 — Batch 13 (Catalog v6): clinical nutrition, enteral & parenteral 251.
-- Facility-scoped with the platform RLS pattern.

CREATE TABLE IF NOT EXISTS nutrition_assessments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  weight_kg DOUBLE PRECISION NOT NULL, height_cm DOUBLE PRECISION NOT NULL,
  bmi DOUBLE PRECISION NOT NULL,
  weight_loss_percent DOUBLE PRECISION,
  risk_level TEXT NOT NULL CHECK (risk_level IN ('low','moderate','high')),
  assessed_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_nutrition_patient ON nutrition_assessments (patient_id, created_at DESC);

CREATE TABLE IF NOT EXISTS nutrition_plans (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  assessment_id UUID REFERENCES nutrition_assessments(id),
  estimated_needs_kcal INT, route TEXT NOT NULL CHECK (route IN ('oral','enteral','parenteral')),
  goal TEXT, restrictions TEXT,
  proposed_by TEXT, approved_by TEXT,
  status TEXT NOT NULL DEFAULT 'proposed' CHECK (status IN ('proposed','approved','active','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS feed_orders (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  plan_id UUID NOT NULL REFERENCES nutrition_plans(id),
  kind TEXT NOT NULL CHECK (kind IN ('enteral','parenteral')),
  formula TEXT, components TEXT, route_detail TEXT,
  rate_ml_hr DOUBLE PRECISION,
  pharmacist_reviewed BOOLEAN NOT NULL DEFAULT false,
  reviewed_by TEXT,
  status TEXT NOT NULL DEFAULT 'ordered' CHECK (status IN ('ordered','started','held','stopped','completed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS administration_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  order_id UUID NOT NULL REFERENCES feed_orders(id),
  prescribed_ml DOUBLE PRECISION NOT NULL, actual_ml DOUBLE PRECISION NOT NULL,
  remaining_ml DOUBLE PRECISION NOT NULL DEFAULT 0,
  stopped BOOLEAN NOT NULL DEFAULT false, stop_reason TEXT,
  recorded_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS nutrition_complications (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  order_id UUID REFERENCES feed_orders(id),
  type TEXT NOT NULL CHECK (type IN ('intolerance','aspiration','metabolic')),
  severity TEXT, action_taken TEXT,
  status TEXT NOT NULL DEFAULT 'reported' CHECK (status IN ('reported','managed','resolved')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['nutrition_assessments','nutrition_plans','feed_orders','administration_records','nutrition_complications']
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
