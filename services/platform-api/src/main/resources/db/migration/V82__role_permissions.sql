-- V82 — Completion batch 4: the role-permission matrix as
-- DATA. Until now, deepening controllers mostly verified the
-- facility tenant; who may WRITE in a sensitive domain is now
-- governed by this table, enforced by an interceptor on every
-- write (POST/PUT/PATCH/DELETE): longest matching path prefix
-- wins, admin bypasses, unmatched paths stay open to any
-- authenticated facility user (tenancy still applies). The
-- matrix is visible and editable as data — extending coverage
-- to another domain is a row, not a release.

CREATE TABLE IF NOT EXISTS role_permissions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  path_prefix TEXT NOT NULL UNIQUE,
  allowed_roles TEXT NOT NULL,
  domain_ar TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

INSERT INTO role_permissions (path_prefix, allowed_roles, domain_ar) VALUES
  ('/api/billing', 'finance_manager', 'الفوترة والمالية'),
  ('/api/insurance-deep', 'finance_manager', 'التأمين والمطالبات'),
  ('/api/pharmacy', 'pharmacist', 'الصيدلة'),
  ('/api/purchasing', 'storekeeper,finance_manager', 'المشتريات'),
  ('/api/vendor-monitoring', 'storekeeper,finance_manager', 'مراقبة الموردين'),
  ('/api/medical-assets', 'biomedical_engineer', 'الأصول والأجهزة الطبية'),
  ('/api/oncology', 'oncologist', 'الأورام'),
  ('/api/infection-control', 'ipc_officer', 'مكافحة العدوى'),
  ('/api/facility-safety', 'safety_officer', 'سلامة المنشأة'),
  ('/api/social-services', 'social_worker', 'الخدمة الاجتماعية'),
  ('/api/school-health', 'school_counselor', 'الصحة المدرسية'),
  ('/api/patients', 'receptionist', 'تسجيل المرضى'),
  ('/api/forensics', 'forensic_physician', 'الطب الشرعي'),
  ('/api/hr-deep', 'admin', 'الموارد البشرية')
ON CONFLICT (path_prefix) DO NOTHING;

GRANT SELECT ON role_permissions TO app_user;
