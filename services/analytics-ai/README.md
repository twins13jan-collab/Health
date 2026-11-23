# خدمة التحليلات والذكاء الاصطناعي — Egypt Health Analytics & AI Service

> **Advisory only — للاسترشاد فقط.** No output of this service is an executive
> clinical or financial decision. Per Catalog v6, AI supports decision-makers;
> a qualified human always reviews and decides.
> مخرجات هذه الخدمة استرشادية فقط وليست قرارات تنفيذية سريرية أو مالية؛
> فالذكاء الاصطناعي يدعم متخذ القرار ولا يحل محله أبدًا.

Python 3.12 + FastAPI + pydantic v2 micro-service for the rebuilt Egypt
National Health System (Catalog v6). It is the Python/FastAPI analytics &
AI component of the reference stack (TypeScript/Next.js web · Java/Spring
Boot core · **Python/FastAPI analytics & AI** · PostgreSQL 16 + PostGIS ·
Dart/Flutter mobile · FHIR/HL7/DICOM interoperability).

## التكامل — How it integrates

```
Java/Spring Boot core ──REST/JSON + X-Correlation-Id──▶ this service
        ▲                                                      │
        └──────────── risk alerts / forecasts ◀──────────────┘
                     (advisory; core persists & routes them)
```

- The **Java core** calls this service synchronously over REST for
  forecasts, surveillance summaries and triage support, sending an
  `X-Correlation-Id` header that this service echoes back on every
  response (and generates when absent) for end-to-end tracing.
- The service owns **no system of record**: it computes from the payload
  it is given and returns results; the core persists them (PostgreSQL)
  and routes **risk alerts** (outbreak risk levels, capacity breaches,
  AMR alerts) through the national command centre / notification stack.
- Every response carries `advisory_only: true`. Triage support
  additionally always returns `requires_human_review: true` and
  `final_decision: null` — enforced in code and covered by tests.
- Errors use one structured model everywhere:
  `{"ok": false, "error": "<code>", "message": "<detail>"}`.
- OpenAPI contract: `GET /openapi.json`, Swagger UI at `/docs`.

## نقاط النهاية — Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/health`, `/v1/health` | Liveness probe |
| POST | `/v1/forecast/outbreak` | Epidemic early-warning: trend, growth rate, 7/14-day moving averages, 7-day exponential-blend forecast, doubling time, explainable risk level (low/medium/high/critical) — الإنذار المبكر بتفشي الأوبئة |
| POST | `/v1/forecast/capacity` | Bed/ICU occupancy forecast, days-to-full and days-to-85% estimates — التنبؤ بإشغال الأسرة والعناية المركزة |
| POST | `/v1/amr/summary` | AMR aggregation by organism × antibiotic, resistance %, threshold alerts (with a minimum-isolates guard) — ترصد مقاومة مضادات الميكروبات |
| POST | `/v1/ai/triage-support` | NEWS2 scoring (adults, SpO2 scales 1 & 2, all boundary tables) as decision *support* — دعم الفرز، يتطلب دائمًا مراجعة بشرية |

Statistics are pure Python (`statistics` module + hand-written OLS /
log-linear regression in `app/services/stats.py`) — no numpy/torch —
so the image stays light and every number is reproducible and explainable
(the factors behind each risk level are returned in English **and** Arabic).

## التشغيل — Run

```bash
cd services/analytics-ai
python3 -m venv .venv && . .venv/bin/activate
pip install -r requirements-dev.txt   # runtime + httpx/pytest for tests
uvicorn app.main:app --host 0.0.0.0 --port 8000
# or: docker build -t egypt-health/analytics-ai . && docker run -p 8000:8000 egypt-health/analytics-ai
```

## الاختبارات — Tests

```bash
python3 -m pytest        # forecasting, capacity, AMR, NEWS2 boundaries, guardrails, error model
```

## مثال — Example

```bash
curl -s -X POST localhost:8000/v1/ai/triage-support -H 'Content-Type: application/json' -d '{
  "age": 67, "respiratory_rate": 24, "spo2_pct": 93, "spo2_scale": 1,
  "on_supplemental_oxygen": true, "systolic_bp": 104, "pulse": 108,
  "consciousness": "A", "temperature_c": 38.6}'
# -> news2_total, per-parameter components, risk_category, suggested_response,
#    requires_human_review: true, final_decision: null
```
