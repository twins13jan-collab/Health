-- Patient portal tenancy (companion to V92/V93): a registered
-- patient's RLS setting is 'patient:<userId>'. Policies below let
-- that setting reach EXACTLY the caller's own data — their unified
-- patient record (linked via app_users.patient_id) and their own
-- appointments (read, book for self, cancel own) — and nothing
-- else. Facility isolation for staff is unchanged.

CREATE POLICY patients_own_read ON patients FOR SELECT USING (
  current_setting('app.current_facility_id', true) LIKE 'patient:%'
  AND EXISTS (SELECT 1 FROM app_users au
              WHERE au.patient_id = patients.id
                AND au.id::text = split_part(current_setting('app.current_facility_id', true), ':', 2)));

DROP POLICY IF EXISTS appointments_facility_isolation ON appointments;

CREATE POLICY appointments_read_isolation ON appointments FOR SELECT USING (
  current_setting('app.current_facility_id', true) = 'national'
  OR facility_id::text = current_setting('app.current_facility_id', true)
  OR (current_setting('app.current_facility_id', true) LIKE 'gov:%'
      AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)));

CREATE POLICY appointments_patient_read ON appointments FOR SELECT USING (
  current_setting('app.current_facility_id', true) LIKE 'patient:%'
  AND EXISTS (SELECT 1 FROM app_users au
              WHERE au.patient_id = appointments.patient_id
                AND au.id::text = split_part(current_setting('app.current_facility_id', true), ':', 2)));

CREATE POLICY appointments_write_isolation ON appointments FOR INSERT WITH CHECK (
  current_setting('app.current_facility_id', true) = 'national'
  OR facility_id::text = current_setting('app.current_facility_id', true)
  OR (current_setting('app.current_facility_id', true) LIKE 'gov:%'
      AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)));

CREATE POLICY appointments_patient_insert ON appointments FOR INSERT WITH CHECK (
  current_setting('app.current_facility_id', true) LIKE 'patient:%'
  AND EXISTS (SELECT 1 FROM app_users au
              WHERE au.patient_id = appointments.patient_id
                AND au.id::text = split_part(current_setting('app.current_facility_id', true), ':', 2)));

CREATE POLICY appointments_update_isolation ON appointments FOR UPDATE USING (
  current_setting('app.current_facility_id', true) = 'national'
  OR facility_id::text = current_setting('app.current_facility_id', true)
  OR (current_setting('app.current_facility_id', true) LIKE 'gov:%'
      AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)))
WITH CHECK (
  current_setting('app.current_facility_id', true) = 'national'
  OR facility_id::text = current_setting('app.current_facility_id', true)
  OR (current_setting('app.current_facility_id', true) LIKE 'gov:%'
      AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)));

CREATE POLICY appointments_patient_update ON appointments FOR UPDATE USING (
  current_setting('app.current_facility_id', true) LIKE 'patient:%'
  AND EXISTS (SELECT 1 FROM app_users au
              WHERE au.patient_id = appointments.patient_id
                AND au.id::text = split_part(current_setting('app.current_facility_id', true), ':', 2)))
WITH CHECK (
  current_setting('app.current_facility_id', true) LIKE 'patient:%'
  AND EXISTS (SELECT 1 FROM app_users au
              WHERE au.patient_id = appointments.patient_id
                AND au.id::text = split_part(current_setting('app.current_facility_id', true), ':', 2)));

CREATE POLICY appointments_delete_isolation ON appointments FOR DELETE USING (
  current_setting('app.current_facility_id', true) = 'national'
  OR facility_id::text = current_setting('app.current_facility_id', true)
  OR (current_setting('app.current_facility_id', true) LIKE 'gov:%'
      AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)));
