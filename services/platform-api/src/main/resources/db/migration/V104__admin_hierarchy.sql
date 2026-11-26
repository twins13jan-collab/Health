-- Admin hierarchy: oversight accounts carry an authority remit
-- (moh = Ministry of Health, drug = drug regulatory authority,
-- insurance = health insurance organisation, higher_education =
-- universities & teaching hospitals) used to scope what they
-- oversee. The tiers themselves are roles on app_users:
-- super_admin (everything incl. settings & variables) >
-- admin (platform administration, no settings) > national_admin /
-- authority_admin / governorate_admin / directorate_admin
-- (scoped oversight) > facility_manager > facility staff > patient.
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS authority TEXT;
