-- V31 — Module deepening round 8 (Catalog v6): medical content &
-- health education 120; geographic data & spatial analysis 121.
-- Facility-scoped, platform RLS pattern.

-- ============ 120 Medical content & health education ============
CREATE TABLE IF NOT EXISTS education_content_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  title TEXT, topic TEXT CHECK (topic IN ('disease','medication','preparation','home_care','prevention')),
  language TEXT, accessible_format BOOLEAN NOT NULL DEFAULT false,
  source_ref TEXT, references_text TEXT,
  author TEXT, clinical_reviewer TEXT,
  reviewed_at DATE, review_due_at DATE,
  status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft','in_review','approved','published','withdrawn')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS content_review_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  content_id UUID NOT NULL REFERENCES education_content_items(id),
  reviewer TEXT, decision TEXT CHECK (decision IN ('approved','rejected','changes_requested')),
  notes TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS awareness_campaigns (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  name TEXT, content_id UUID REFERENCES education_content_items(id),
  audience TEXT, starts_on DATE, ends_on DATE,
  comprehension_score NUMERIC(5,2),
  status TEXT NOT NULL DEFAULT 'planned' CHECK (status IN ('planned','running','completed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 121 Geographic data & spatial analysis ============
CREATE TABLE IF NOT EXISTS facility_locations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  facility_ref TEXT, name TEXT,
  latitude NUMERIC(10,7), longitude NUMERIC(10,7),
  accuracy_m NUMERIC(8,1), verified_at DATE, verified_by TEXT,
  district TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS coverage_zones (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  name TEXT, zone_type TEXT CHECK (zone_type IN ('coverage','referral','mobile')),
  facility_ref TEXT, radius_km NUMERIC(7,2), active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS access_time_analyses (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  origin_ref TEXT, facility_ref TEXT,
  straight_line_km NUMERIC(8,2), road_distance_km NUMERIC(8,2), road_time_min INT,
  road_network_source TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['education_content_items','content_review_records','awareness_campaigns',
                           'facility_locations','coverage_zones','access_time_analyses']
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
