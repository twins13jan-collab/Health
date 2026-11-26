-- Phase 4.10: central sterilization joins the governed write
-- domains. The reprocessing chain is run by nursing/technologist
-- staff and physicians under the facility manager.
INSERT INTO role_permissions (path_prefix, allowed_roles, domain_ar)
VALUES ('/api/cssd', 'nurse,physician,facility_manager', 'التعقيم المركزي وإعادة معالجة الأدوات')
ON CONFLICT (path_prefix) DO NOTHING;
