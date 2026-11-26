-- Phase 4.7 + full-review fix: maternity joins the governed write
-- domains, and the encounters write surface — found by the 2026-11-23
-- end-to-end review to have NO access-matrix rule (open to any
-- authenticated role) — is restricted to the roles that run reception,
-- triage and clinical encounters.
INSERT INTO role_permissions (path_prefix, allowed_roles, domain_ar)
VALUES ('/api/maternity', 'physician,nurse,facility_manager', 'الأمومة والحمل والولادة')
ON CONFLICT (path_prefix) DO NOTHING;
INSERT INTO role_permissions (path_prefix, allowed_roles, domain_ar)
VALUES ('/api/encounters', 'receptionist,physician,nurse,facility_manager', 'اللقاءات السريرية والفرز')
ON CONFLICT (path_prefix) DO NOTHING;
