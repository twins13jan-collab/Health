-- V84: production identity hardening (Phase 1 of the production overhaul).
-- Login lockout state + last-login tracking for app_users, backing the
-- admin user-management API (/api/admin/users) and the hardened login flow.
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS failed_login_attempts integer NOT NULL DEFAULT 0;
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS locked_until timestamptz;
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS last_login_at timestamptz;
