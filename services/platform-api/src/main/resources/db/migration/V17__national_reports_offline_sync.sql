-- V17 — Batch 16 (Catalog v6): national reports & open data 236,
-- offline work & secure sync 237.
-- Every table is facility-scoped with the platform RLS pattern.

-- ============ 236 National indicators, snapshots & publication ============
CREATE TABLE IF NOT EXISTS indicator_definitions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  code TEXT NOT NULL, name TEXT NOT NULL,
  formula TEXT, numerator_def TEXT, denominator_def TEXT, exclusions TEXT,
  source_system TEXT, owner TEXT,
  version INT NOT NULL DEFAULT 1,
  status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft','approved','retired')),
  effective_from DATE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_indicator_version ON indicator_definitions (facility_id, code, version);

CREATE TABLE IF NOT EXISTS data_snapshots (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  indicator_id UUID NOT NULL REFERENCES indicator_definitions(id),
  cutoff_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  participating_facilities INT NOT NULL DEFAULT 0,
  expected_facilities INT NOT NULL DEFAULT 0,
  coverage_pct NUMERIC(5,2) NOT NULL DEFAULT 0,
  data_class TEXT NOT NULL DEFAULT 'incomplete' CHECK (data_class IN ('confirmed','estimated','late','incomplete')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS aggregation_jobs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  snapshot_id UUID NOT NULL REFERENCES data_snapshots(id),
  rules_version INT NOT NULL DEFAULT 1, scope TEXT,
  numerator_value NUMERIC(16,2), denominator_value NUMERIC(16,2), computed_value NUMERIC(16,4),
  small_count_suppressed BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'computed' CHECK (status IN ('computed','validated','published')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS publication_datasets (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  job_id UUID NOT NULL REFERENCES aggregation_jobs(id),
  fields TEXT, aggregation_level TEXT,
  deidentified BOOLEAN NOT NULL DEFAULT false,
  license TEXT, approved_by TEXT NOT NULL,
  published_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS correction_notices (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  dataset_id UUID NOT NULL REFERENCES publication_datasets(id),
  reason TEXT NOT NULL, impact TEXT, replacement_version TEXT, issued_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 237 Offline work packages, devices & sync ============
CREATE TABLE IF NOT EXISTS device_trust (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  device_code TEXT NOT NULL, user_ref TEXT,
  encryption_enabled BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'trusted' CHECK (status IN ('trusted','revoked')),
  last_verified_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  revoked_by TEXT, revoke_reason TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_device_code ON device_trust (facility_id, device_code);

CREATE TABLE IF NOT EXISTS offline_work_packages (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  device_id UUID NOT NULL REFERENCES device_trust(id),
  user_ref TEXT, scope TEXT, allowed_data TEXT,
  reference_data_version INT NOT NULL DEFAULT 1,
  valid_from TIMESTAMPTZ NOT NULL DEFAULT now(),
  valid_until TIMESTAMPTZ NOT NULL,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','expired','revoked')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS local_transactions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  package_id UUID NOT NULL REFERENCES offline_work_packages(id),
  local_id TEXT NOT NULL, operation_type TEXT NOT NULL,
  domain TEXT NOT NULL DEFAULT 'administrative' CHECK (domain IN ('clinical','financial','administrative')),
  payload_hash TEXT, client_time TIMESTAMPTZ,
  reference_data_version INT NOT NULL DEFAULT 1,
  conflicts_with_server BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending','synced','conflicted','rejected','duplicate')),
  server_seq BIGINT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_local_tx ON local_transactions (package_id, local_id);

CREATE TABLE IF NOT EXISTS sync_envelopes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  package_id UUID NOT NULL REFERENCES offline_work_packages(id),
  sync_id TEXT NOT NULL, content_hash TEXT,
  event_count INT NOT NULL DEFAULT 0,
  applied_count INT NOT NULL DEFAULT 0,
  status TEXT NOT NULL DEFAULT 'received' CHECK (status IN ('received','applied','duplicate','rejected')),
  received_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_sync_id ON sync_envelopes (package_id, sync_id);

CREATE TABLE IF NOT EXISTS conflict_cases (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  transaction_id UUID NOT NULL REFERENCES local_transactions(id),
  domain TEXT NOT NULL,
  local_summary TEXT, server_summary TEXT,
  resolution_rule TEXT, resolved_by TEXT, outcome TEXT,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','resolved')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['indicator_definitions','data_snapshots','aggregation_jobs','publication_datasets','correction_notices',
                           'device_trust','offline_work_packages','local_transactions','sync_envelopes','conflict_cases']
  LOOP
    EXECUTE format('ALTER TABLE %I ENABLE ROW LEVEL SECURITY', t);
    EXECUTE format('ALTER TABLE %I FORCE ROW LEVEL SECURITY', t);
    EXECUTE format('DROP POLICY IF EXISTS %I ON %I', t || '_facility_isolation', t);
    EXECUTE format($p$CREATE POLICY %I ON %I FOR ALL
      USING (current_setting('app.current_facility_id', true) = 'national'
             OR facility_id::text = current_setting('app.current_facility_id', true)
             OR (current_setting('app.current_facility_id', true) LIKE 'gov:%%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)))
      WITH CHECK (current_setting('app.current_facility_id', true) = 'national'
             OR facility_id::text = current_setting('app.current_facility_id', true)
             OR (current_setting('app.current_facility_id', true) LIKE 'gov:%%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)))$p$,
      t || '_facility_isolation', t);
    EXECUTE format('GRANT SELECT, INSERT, UPDATE, DELETE ON %I TO app_user', t);
  END LOOP;
END $$;
