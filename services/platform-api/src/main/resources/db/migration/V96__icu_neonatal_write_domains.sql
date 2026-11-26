-- Phase 4.6: ICU and neonatal operational boards join the governed
-- write domains of the access matrix (writes restricted to the clinical
-- roles that run these units; reads stay open to authenticated staff).
INSERT INTO role_permissions (path_prefix, allowed_roles, domain_ar)
VALUES ('/api/icu', 'physician,nurse,facility_manager', 'العناية المركزة ومراقبة الأجهزة')
ON CONFLICT (path_prefix) DO NOTHING;
INSERT INTO role_permissions (path_prefix, allowed_roles, domain_ar)
VALUES ('/api/neonatal', 'physician,nurse,facility_manager', 'حديثي الولادة والحضانات')
ON CONFLICT (path_prefix) DO NOTHING;
