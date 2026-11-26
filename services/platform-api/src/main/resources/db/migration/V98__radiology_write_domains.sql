-- Phase 4.8: radiology operations join the governed write domains.
-- The worklist flow (order -> schedule -> image -> report -> verify)
-- is run by physicians and nursing/technologist staff under the
-- facility manager; the deep layer (contrast governance, critical
-- results) stays physician + facility manager.
INSERT INTO role_permissions (path_prefix, allowed_roles, domain_ar)
VALUES ('/api/radiology', 'physician,nurse,facility_manager', 'الأشعة والتصوير')
ON CONFLICT (path_prefix) DO NOTHING;
INSERT INTO role_permissions (path_prefix, allowed_roles, domain_ar)
VALUES ('/api/radiology-deep', 'physician,facility_manager', 'الأشعة العميقة والنتائج الحرجة')
ON CONFLICT (path_prefix) DO NOTHING;
