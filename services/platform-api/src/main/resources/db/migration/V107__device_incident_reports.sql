-- Fault reporting is a clinical act: the nurse/physician at the
-- bedside reports a device fault, while everything after the
-- report (work orders, calibration, release) stays with
-- biomedical engineering under /api/medical-assets. The report
-- endpoint therefore lives at its own top-level path with the
-- clinical reporters named.
INSERT INTO role_permissions (path_prefix, allowed_roles, domain_ar) VALUES
  ('/api/device-incidents', 'nurse,physician,receptionist,biomedical_engineer,facility_manager', 'بلاغات أعطال الأجهزة')
ON CONFLICT (path_prefix) DO NOTHING;
