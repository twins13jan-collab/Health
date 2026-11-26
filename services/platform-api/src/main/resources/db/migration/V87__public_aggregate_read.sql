-- V87 — Public live-data API: the anonymous homepage aggregates
-- (facility counts, capacity, ER waiting) read through a dedicated
-- 'public' tenant setting. SELECT-only policies on the RLS tables the
-- aggregates touch; the public controller returns counts and facility
-- directory data only — never patient-level rows.

DROP POLICY IF EXISTS encounters_public_read ON encounters;
CREATE POLICY encounters_public_read ON encounters FOR SELECT
  USING (current_setting('app.current_facility_id', true) = 'public');

DROP POLICY IF EXISTS patients_public_read ON patients;
CREATE POLICY patients_public_read ON patients FOR SELECT
  USING (current_setting('app.current_facility_id', true) = 'public');

DROP POLICY IF EXISTS cycle_instances_public_read ON cycle_instances;
CREATE POLICY cycle_instances_public_read ON cycle_instances FOR SELECT
  USING (current_setting('app.current_facility_id', true) = 'public');
