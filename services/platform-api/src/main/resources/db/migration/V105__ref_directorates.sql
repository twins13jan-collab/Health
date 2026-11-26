-- One health directorate per governorate, named after its
-- governorate («مديرية صحة القاهرة» …) — the level between the
-- governorate and its facilities in the platform hierarchy, used
-- by the sign-in / registration cascades and oversight scopes.
CREATE TABLE IF NOT EXISTS ref_directorates (
  governorate_code TEXT PRIMARY KEY REFERENCES ref_governorates(code),
  name_ar TEXT NOT NULL,
  name_en TEXT NOT NULL
);
INSERT INTO ref_directorates (governorate_code, name_ar, name_en)
SELECT code, 'مديرية صحة ' || name_ar, name_en || ' Health Directorate'
FROM ref_governorates
ON CONFLICT (governorate_code) DO NOTHING;
GRANT SELECT, INSERT, UPDATE, DELETE ON ref_directorates TO app_user;
