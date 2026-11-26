-- V19 — Batch 18 (Catalog v6): general ledger, closing & bank
-- reconciliation 229, integrations & message monitoring 234.
-- Every table is facility-scoped with the platform RLS pattern.

-- ============ 229 Chart of accounts, journals, periods, bank rec ============
CREATE TABLE IF NOT EXISTS chart_of_accounts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  account_code TEXT NOT NULL, name TEXT NOT NULL,
  classification TEXT NOT NULL CHECK (classification IN ('asset','liability','equity','revenue','expense')),
  posting_rules TEXT, owner TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_account_code ON chart_of_accounts (facility_id, account_code);

CREATE TABLE IF NOT EXISTS accounting_periods (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  period_key TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','closed','reopened')),
  closed_by TEXT, reopen_reason TEXT, reopened_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_period_key ON accounting_periods (facility_id, period_key);

CREATE TABLE IF NOT EXISTS journals (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  source_system TEXT NOT NULL, source_ref TEXT,
  period_key TEXT NOT NULL,
  currency TEXT NOT NULL DEFAULT 'EGP',
  fx_rate_source TEXT, fx_rate_date DATE,
  reversal_of UUID,
  posted_by TEXT, created_by TEXT,
  status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft','posted','reversed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS journal_lines (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  journal_id UUID NOT NULL REFERENCES journals(id),
  account_code TEXT NOT NULL,
  debit NUMERIC(16,2) NOT NULL DEFAULT 0, credit NUMERIC(16,2) NOT NULL DEFAULT 0,
  cost_center TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS subledger_links (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  journal_id UUID NOT NULL REFERENCES journals(id),
  subledger_type TEXT NOT NULL CHECK (subledger_type IN ('inventory','sales','procurement','payroll','assets')),
  subledger_ref TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS bank_statements (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  account_code TEXT NOT NULL, statement_ref TEXT NOT NULL, period_key TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS bank_transactions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  statement_id UUID NOT NULL REFERENCES bank_statements(id),
  reference TEXT, -- NULL reference stays unmatched by rule
  tx_date DATE, amount NUMERIC(16,2) NOT NULL DEFAULT 0,
  matched_journal_id UUID,
  match_status TEXT NOT NULL DEFAULT 'unmatched' CHECK (match_status IN ('unmatched','matched')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS close_checklists (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  period_key TEXT NOT NULL, pending_entries INT NOT NULL DEFAULT 0,
  approvals TEXT, status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','ready','done')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ 234 Endpoints, contracts, messages, attempts, ACKs ============
CREATE TABLE IF NOT EXISTS integration_endpoints (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  vendor TEXT, system_name TEXT NOT NULL, version TEXT,
  protocol TEXT NOT NULL CHECK (protocol IN ('FHIR','REST','HL7v2','DICOM','FILE')),
  owner TEXT, environment TEXT NOT NULL DEFAULT 'test' CHECK (environment IN ('test','prod')),
  compatibility_tested BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'suspended' CHECK (status IN ('active','suspended')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS interface_contracts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  endpoint_id UUID NOT NULL REFERENCES integration_endpoints(id),
  message_type TEXT NOT NULL, fields TEXT, validation_rules TEXT, version INT NOT NULL DEFAULT 1,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS integration_messages (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  message_id TEXT NOT NULL, -- idempotency key
  source_system TEXT, destination TEXT,
  content_hash TEXT, trace_id TEXT,
  payload_valid BOOLEAN NOT NULL DEFAULT true,
  semantic_verified BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'received' CHECK (status IN ('received','acked','nacked','quarantined','dead_letter')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX IF NOT EXISTS uq_integration_message ON integration_messages (facility_id, message_id);

CREATE TABLE IF NOT EXISTS delivery_attempts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  message_ref UUID NOT NULL REFERENCES integration_messages(id),
  attempt_no INT NOT NULL, result TEXT, error_code TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS acknowledgements (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  message_ref UUID NOT NULL REFERENCES integration_messages(id),
  ack_type TEXT NOT NULL CHECK (ack_type IN ('ACK','NACK')),
  semantic_verified BOOLEAN NOT NULL DEFAULT false,
  received_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS mapping_rules (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  endpoint_id UUID NOT NULL REFERENCES integration_endpoints(id),
  source_field TEXT, target_field TEXT, version INT NOT NULL DEFAULT 1,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS reconciliation_cases (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  message_ref UUID NOT NULL REFERENCES integration_messages(id),
  difference TEXT, handling TEXT,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','resolved')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- ============ RLS + grants (platform pattern) ============
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY['chart_of_accounts','accounting_periods','journals','journal_lines','subledger_links','bank_statements','bank_transactions','close_checklists',
                           'integration_endpoints','interface_contracts','integration_messages','delivery_attempts','acknowledgements','mapping_rules','reconciliation_cases']
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
