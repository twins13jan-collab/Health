-- V36 — Fix for V35: the integrations framework (cycle 234) already
-- owns a table named delivery_attempts with a different schema, so
-- V35's CREATE TABLE IF NOT EXISTS silently skipped ours. The
-- medication-delivery attempts get their own table.
CREATE TABLE IF NOT EXISTS med_delivery_attempts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  order_id UUID NOT NULL REFERENCES delivery_orders(id),
  attempt_no INT, outcome TEXT CHECK (outcome IN ('delivered','failed')),
  reason TEXT, temperature_ok BOOLEAN,
  received_by TEXT, identity_verified BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

ALTER TABLE med_delivery_attempts ENABLE ROW LEVEL SECURITY;
ALTER TABLE med_delivery_attempts FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS med_delivery_attempts_facility_isolation ON med_delivery_attempts;
CREATE POLICY med_delivery_attempts_facility_isolation ON med_delivery_attempts FOR ALL
  USING (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)))
  WITH CHECK (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)));
GRANT SELECT, INSERT, UPDATE, DELETE ON med_delivery_attempts TO app_user;
