-- Directory provenance for facilities loaded from external
-- registers (e.g. the August-2020 provider-network list): where a
-- row came from, plus the address and phone the source carried.
-- Directory rows stay distinguishable — and removable — forever.
ALTER TABLE facilities ADD COLUMN IF NOT EXISTS source TEXT;
ALTER TABLE facilities ADD COLUMN IF NOT EXISTS address TEXT;
ALTER TABLE facilities ADD COLUMN IF NOT EXISTS phone TEXT;
CREATE INDEX IF NOT EXISTS idx_facilities_source ON facilities (source);
