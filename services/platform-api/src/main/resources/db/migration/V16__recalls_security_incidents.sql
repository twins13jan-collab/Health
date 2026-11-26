-- V16 — Batch 15 (Catalog v6): drug recall, batches & stock 226,
-- security & privacy incidents 238.
-- Every table is facility-scoped with the platform RLS pattern.

-- ============ 226 Drug batches, stock ledger & recalls ============
CREATE TABLE IF NOT EXISTS drug_catalog_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  generic_name TEXT NOT NULL, trade_name TEXT, form TEXT, strength TEXT,
  barcode TEXT, registration_ref TEXT, source TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_drug_barcode ON drug_catalog_items (facility_id, barcode) WHERE barcode IS NOT NULL;

CREATE TABLE IF NOT EXISTS drug_batches (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  item_id UUID NOT NULL REFERENCES drug_catalog_items(id),
  batch_number TEXT NOT NULL, manufacturer TEXT, supplier TEXT,
  production_date DATE, expiry_date DATE NOT NULL,
  release_status TEXT NOT NULL DEFAULT 'released' CHECK (release_status IN ('released','quarantined','recalled')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_batch_item ON drug_batches (item_id, expiry_date ASC);

-- Append-only ledger: balances are rebuilt from movements, never edited.
CREATE TABLE IF NOT EXISTS stock_movements (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  batch_id UUID NOT NULL REFERENCES drug_batches(id),
  location TEXT NOT NULL,
  movement_type TEXT NOT NULL CHECK (movement_type IN ('receive','dispense','adjustment','quarantine','return','destroy')),
  quantity INT NOT NULL, unit TEXT NOT NULL DEFAULT 'unit',
  cost NUMERIC(12,2), document_ref TEXT,
  fefo_exception BOOLEAN NOT NULL DEFAULT false, exception_reason TEXT,
  approved_by TEXT,
  created_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_movement_batch ON stock_movements (batch_id, created_at ASC);

CREATE TABLE IF NOT EXISTS recall_notices (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  authority TEXT NOT NULL, reason TEXT NOT NULL,
  severity TEXT NOT NULL CHECK (severity IN ('class1','class2','class3')),
  effective_date DATE NOT NULL,
  published_by TEXT,
  regulator_ack BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft','published','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS recall_scopes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  notice_id UUID NOT NULL REFERENCES recall_notices(id),
  batch_id UUID NOT NULL REFERENCES drug_batches(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_recall_scope ON recall_scopes (notice_id, batch_id);

CREATE TABLE IF NOT EXISTS recall_receipts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  notice_id UUID NOT NULL REFERENCES recall_notices(id),
  location TEXT NOT NULL, acknowledged_by TEXT NOT NULL,
  quarantined_qty INT NOT NULL DEFAULT 0,
  acknowledged_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_recall_receipt ON recall_receipts (notice_id, location);

CREATE TABLE IF NOT EXISTS recall_dispositions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  notice_id UUID NOT NULL REFERENCES recall_notices(id),
  batch_id UUID NOT NULL REFERENCES drug_batches(id),
  disposition TEXT NOT NULL CHECK (disposition IN ('returned','destroyed')),
  quantity INT NOT NULL, approved_by TEXT NOT NULL,
  executed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 238 Security & privacy incidents ============
CREATE TABLE IF NOT EXISTS security_incidents (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  type TEXT NOT NULL CHECK (type IN ('compromised_account','data_leak','ransomware','monitoring_gap','other')),
  systems TEXT, assets TEXT,
  discovered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  source TEXT, impact TEXT, severity TEXT,
  sessions_revoked BOOLEAN NOT NULL DEFAULT false,
  clean_backup_verified BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'reported' CHECK (status IN ('reported','contained','eradicated','recovered','closed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- Evidence is immutable: hash + custody chain, no update path.
CREATE TABLE IF NOT EXISTS evidence_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  incident_id UUID NOT NULL REFERENCES security_incidents(id),
  kind TEXT NOT NULL, description TEXT,
  sha256_hash TEXT NOT NULL,
  custody_chain TEXT NOT NULL DEFAULT '[]',
  collected_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS incident_actions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  incident_id UUID NOT NULL REFERENCES security_incidents(id),
  phase TEXT NOT NULL CHECK (phase IN ('containment','eradication','recovery')),
  action TEXT NOT NULL, owner TEXT,
  clean_backup_verified BOOLEAN NOT NULL DEFAULT false,
  done_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS privacy_impacts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  incident_id UUID NOT NULL REFERENCES security_incidents(id),
  data_types TEXT,
  affected_persons_count INT, -- NULL means unconfirmed; never publish a guessed number
  disclosure_likelihood TEXT, assessed_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS notification_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  incident_id UUID NOT NULL REFERENCES security_incidents(id),
  audience TEXT NOT NULL CHECK (audience IN ('regulator','affected_persons','internal')),
  decision TEXT NOT NULL CHECK (decision IN ('notify','not_notify')),
  justification TEXT NOT NULL, decided_by TEXT,
  notified_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS lessons_learned (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  incident_id UUID NOT NULL REFERENCES security_incidents(id),
  lessons TEXT NOT NULL, corrective_actions TEXT,
  corrective_closed BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['drug_catalog_items','drug_batches','stock_movements','recall_notices','recall_scopes','recall_receipts','recall_dispositions',
                           'security_incidents','evidence_items','incident_actions','privacy_impacts','notification_records','lessons_learned']
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
