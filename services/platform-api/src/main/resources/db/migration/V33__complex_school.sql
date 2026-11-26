-- V33 — Module deepening round 10 (Catalog v6): complex health
-- cases 142; school & university health 143. Facility-scoped,
-- platform RLS pattern.

-- ============ 142 Complex health cases ============
CREATE TABLE IF NOT EXISTS complex_care_plans (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_ref TEXT, coordinator TEXT, conditions_summary TEXT,
  quality_of_life_score NUMERIC(4,1),
  status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft','active','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS care_team_members (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  plan_id UUID NOT NULL REFERENCES complex_care_plans(id),
  member_name TEXT, member_role TEXT, responsibility TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS care_transitions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  plan_id UUID NOT NULL REFERENCES complex_care_plans(id),
  from_setting TEXT, to_setting TEXT,
  handover_documented BOOLEAN NOT NULL DEFAULT false,
  equipment_needed BOOLEAN NOT NULL DEFAULT false,
  equipment_arranged BOOLEAN NOT NULL DEFAULT false,
  transition_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS home_equipment_orders (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  plan_id UUID NOT NULL REFERENCES complex_care_plans(id),
  equipment TEXT, supplier TEXT, delivered BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS individual_emergency_plans (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  plan_id UUID NOT NULL REFERENCES complex_care_plans(id),
  triggers TEXT, actions TEXT, contacts TEXT, reviewed_at DATE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 143 School & university health ============
CREATE TABLE IF NOT EXISTS student_health_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  student_code TEXT NOT NULL, institution_ref TEXT,
  guardian_consent BOOLEAN NOT NULL DEFAULT false,
  chronic_conditions TEXT, allergy_notes TEXT,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','transferred','graduated')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_student_code ON student_health_records (facility_id, student_code);

CREATE TABLE IF NOT EXISTS school_screenings (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  student_id UUID NOT NULL REFERENCES student_health_records(id),
  screening_type TEXT CHECK (screening_type IN ('vision','hearing','growth','dental','general')),
  result TEXT CHECK (result IN ('normal','abnormal')),
  referral_needed BOOLEAN NOT NULL DEFAULT false,
  referral_ref TEXT, guardian_notified BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS school_vaccinations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  student_id UUID NOT NULL REFERENCES student_health_records(id),
  vaccine TEXT, dose TEXT, batch TEXT,
  consent_verified BOOLEAN NOT NULL DEFAULT false,
  administered_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS school_health_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  institution_ref TEXT,
  event_type TEXT CHECK (event_type IN ('outbreak','gathering','emergency','mental_health_support')),
  description TEXT, action_taken TEXT,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','managed','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['complex_care_plans','care_team_members','care_transitions','home_equipment_orders','individual_emergency_plans',
                           'student_health_records','school_screenings','school_vaccinations','school_health_events']
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
