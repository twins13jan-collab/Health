-- Self-registration (V92) creates a citizen's unified patient
-- record BEFORE authentication, with facility_id NULL — the same
-- pre-auth write trap V85 fixed for audit_logs. The V1 FOR ALL
-- isolation policy governed INSERT too, so the anonymous insert
-- was rejected and registration returned 500. Split the policy:
-- reads/updates/deletes keep strict isolation; inserts also
-- allow facility-less citizen records (which no facility can see
-- until the citizen is linked through care).
DROP POLICY IF EXISTS patients_facility_isolation ON patients;

CREATE POLICY patients_read_isolation ON patients FOR SELECT USING (
  current_setting('app.current_facility_id', true) = 'national'
  OR facility_id::text = current_setting('app.current_facility_id', true)
  OR (current_setting('app.current_facility_id', true) LIKE 'gov:%'
      AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)));

CREATE POLICY patients_update_isolation ON patients FOR UPDATE USING (
  current_setting('app.current_facility_id', true) = 'national'
  OR facility_id::text = current_setting('app.current_facility_id', true)
  OR (current_setting('app.current_facility_id', true) LIKE 'gov:%'
      AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)))
WITH CHECK (
  current_setting('app.current_facility_id', true) = 'national'
  OR facility_id::text = current_setting('app.current_facility_id', true)
  OR (current_setting('app.current_facility_id', true) LIKE 'gov:%'
      AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)));

CREATE POLICY patients_delete_isolation ON patients FOR DELETE USING (
  current_setting('app.current_facility_id', true) = 'national'
  OR facility_id::text = current_setting('app.current_facility_id', true)
  OR (current_setting('app.current_facility_id', true) LIKE 'gov:%'
      AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)));

CREATE POLICY patients_insert ON patients FOR INSERT WITH CHECK (
  current_setting('app.current_facility_id', true) = 'national'
  OR facility_id::text = current_setting('app.current_facility_id', true)
  OR (current_setting('app.current_facility_id', true) LIKE 'gov:%'
      AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2))
  OR facility_id IS NULL);
