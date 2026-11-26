-- V85: audit_logs write policy for system/pre-auth events.
-- The V1 policy is FOR ALL with a facility USING clause, which also governs
-- INSERT: an audit row written before a session exists (login success /
-- failure / lockout) carries facility_id NULL and an empty tenant setting,
-- so the insert was rejected and broke authentication itself once login
-- auditing was enabled. Split the policy: reads keep the exact V1
-- isolation; inserts keep facility/national/governorate checks and
-- additionally allow system rows with no facility. Audit rows remain
-- append-only for the application role (no UPDATE/DELETE policy).
DROP POLICY IF EXISTS audit_logs_facility_isolation ON audit_logs;
CREATE POLICY audit_logs_read_isolation ON audit_logs FOR SELECT
  USING (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)));
CREATE POLICY audit_logs_insert ON audit_logs FOR INSERT
  WITH CHECK (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2))
         OR facility_id IS NULL);
