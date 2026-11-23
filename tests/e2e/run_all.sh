#!/usr/bin/env bash
# Live end-to-end suite for the Egypt national health platform.
# Requires the platform running (platform-api on EHS_BASE, default
# http://localhost:8080) with the smoke fixtures seeded
# (facility + role users, password Smoke#2026 — see infra/recovery).
# Each test_*.py prints PASS/FAIL lines and a TOTAL; this runner
# aggregates and fails if any suite is not fully green.
set -u
cd "$(dirname "$0")"
fail=0
for t in test_*.py; do
  echo "=== $t"
  PYTHONPATH=. python3 "$t" || fail=1
done
exit $fail
