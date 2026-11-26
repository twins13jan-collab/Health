-- Private-sector management: facilities carry their operating
-- licence (number, issuing authority, expiry) so the sector can be
-- overseen — approvals, renewals and expiries — from the platform.
ALTER TABLE facilities ADD COLUMN IF NOT EXISTS license_no TEXT;
ALTER TABLE facilities ADD COLUMN IF NOT EXISTS license_authority TEXT;
ALTER TABLE facilities ADD COLUMN IF NOT EXISTS license_expiry DATE;
