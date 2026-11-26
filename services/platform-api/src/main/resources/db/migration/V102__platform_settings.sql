-- Platform settings & variables, administered from the admin
-- control panel (/admin/settings): identity and public-site texts,
-- contact data, security parameters, registration switches and the
-- maintenance mode. Values take effect at runtime through
-- SettingsService — nothing here is display-only.
CREATE TABLE IF NOT EXISTS platform_settings (
  setting_key TEXT PRIMARY KEY,
  setting_value TEXT,
  updated_by TEXT,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
GRANT ALL ON platform_settings TO app_user;
INSERT INTO platform_settings (setting_key, setting_value) VALUES
  ('site.name_ar', 'خطاب للصحة العامة وإدارة الأزمات'),
  ('site.name_en', 'Khattab For Public Health and Crisis Management'),
  ('site.descriptor', 'منظومة خطاب لإدارة الصحة العامة والأزمات والطوارئ الطبية والمنشآت الصحية'),
  ('site.contact_name', 'أحمد خطاب'),
  ('site.contact_phone', '00201556626662'),
  ('site.contact_whatsapp', '00201556626662'),
  ('security.lockout_max_failures', '5'),
  ('security.lockout_minutes', '15'),
  ('security.password_min_length', '10'),
  ('registration.patient_enabled', 'true'),
  ('registration.facility_enabled', 'true'),
  ('system.maintenance_mode', 'false'),
  ('system.maintenance_message', 'المنظومة في صيانة مجدولة — نعود خلال وقت قصير'),
  ('public.show_capacity', 'true')
ON CONFLICT (setting_key) DO NOTHING;
