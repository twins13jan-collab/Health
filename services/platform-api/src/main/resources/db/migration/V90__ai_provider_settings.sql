-- External AI providers (decision (ج), full group): one row per
-- provider with its model choice and its API key ENCRYPTED at rest
-- (AES-GCM, key from the server environment — never in the DB in
-- plain text, never returned by the API except masked). Keys are
-- managed by the system administrator from the control panel
-- (/admin/ai); exactly one provider may be active at a time and
-- the local open-source model remains the fallback.
CREATE TABLE IF NOT EXISTS ai_provider_settings (
  provider TEXT PRIMARY KEY,
  enabled BOOLEAN NOT NULL DEFAULT false,
  is_active BOOLEAN NOT NULL DEFAULT false,
  model TEXT,
  api_key_enc TEXT,
  updated_by TEXT,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now());

INSERT INTO ai_provider_settings (provider, model) VALUES
  ('openai', 'gpt-4o-mini'),
  ('anthropic', 'claude-sonnet-4-5'),
  ('gemini', 'gemini-2.0-flash'),
  ('openrouter', 'openai/gpt-4o-mini'),
  ('groq', 'llama-3.3-70b-versatile')
ON CONFLICT (provider) DO NOTHING;

GRANT SELECT, INSERT, UPDATE, DELETE ON ai_provider_settings TO app_user;
