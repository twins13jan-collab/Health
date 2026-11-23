"""Egypt Health Analytics & AI Service — FastAPI application.

Catalog v6 role in the rebuilt system (TypeScript/Next.js web,
Java/Spring Boot core services, Python/FastAPI analytics & AI,
PostgreSQL, Flutter mobile):

* The Java core calls this service over REST (JSON, correlation-id on
  every request) for forecasts, surveillance summaries and clinical
  decision *support*.
* This service owns NO system of record and makes NO executive
  clinical or financial decision: every output is advisory
  (`advisory_only: true`) and triage output always requires human
  review (`requires_human_review: true`).
* Risk alerts it computes are returned to the caller, which persists
  and routes them through the core's notification/command centre.

Run:  uvicorn app.main:app --host 0.0.0.0 --port 8000
Docs: /docs (Swagger UI) and /openapi.json
"""
from __future__ import annotations

import logging
import time
import uuid

from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from starlette.exceptions import HTTPException as StarletteHTTPException

from . import __version__
from .models import (
    AmrSummaryRequest,
    AmrSummaryResponse,
    CapacityForecastRequest,
    CapacityForecastResponse,
    OutbreakForecastRequest,
    OutbreakForecastResponse,
    TriageSupportRequest,
    TriageSupportResponse,
)
from .services.amr import summarise_amr
from .services.forecast import forecast_capacity, forecast_outbreak
from .services.triage import score_news2

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s %(message)s")
logger = logging.getLogger("analytics-ai")

app = FastAPI(
    title="Egypt Health Analytics & AI Service",
    description=(
        "منظومة الصحة الوطنية المصرية — خدمة التحليلات والذكاء الاصطناعي. "
        "Epidemic early-warning, capacity forecasting, AMR surveillance and "
        "NEWS2 triage support. All outputs are advisory only; AI never makes "
        "an executive clinical or financial decision alone."
    ),
    version=__version__,
    docs_url="/docs",
    openapi_url="/openapi.json",
)


# ------------------------------------------------------------- middleware
@app.middleware("http")
async def correlation_id_middleware(request: Request, call_next):
    """Accept or mint X-Correlation-Id, echo it back, and log with it."""
    correlation_id = request.headers.get("x-correlation-id") or str(uuid.uuid4())
    request.state.correlation_id = correlation_id
    started = time.perf_counter()
    response = await call_next(request)
    elapsed_ms = (time.perf_counter() - started) * 1000
    response.headers["X-Correlation-Id"] = correlation_id
    logger.info(
        "method=%s path=%s status=%s correlation_id=%s duration_ms=%.1f",
        request.method, request.url.path, response.status_code, correlation_id, elapsed_ms,
    )
    return response


# --------------------------------------------------------- structured errors
def error_body(error: str, message: str) -> dict:
    return {"ok": False, "error": error, "message": message}


@app.exception_handler(RequestValidationError)
async def validation_error_handler(request: Request, exc: RequestValidationError):
    first = exc.errors()[0] if exc.errors() else {}
    loc = ".".join(str(p) for p in first.get("loc", []))
    message = f"{loc}: {first.get('msg', 'invalid request')}" if loc else "invalid request"
    return JSONResponse(status_code=422, content=error_body("validation_error", message))


@app.exception_handler(StarletteHTTPException)
async def http_error_handler(request: Request, exc: StarletteHTTPException):
    return JSONResponse(
        status_code=exc.status_code,
        content=error_body("http_error", str(exc.detail)),
    )


@app.exception_handler(Exception)
async def unhandled_error_handler(request: Request, exc: Exception):  # pragma: no cover
    logger.exception("unhandled error correlation_id=%s", getattr(request.state, "correlation_id", "?"))
    return JSONResponse(status_code=500, content=error_body("internal_error", "internal error"))


# ------------------------------------------------------------------ routes
@app.get("/health", tags=["platform"])
@app.get("/v1/health", tags=["platform"])
def health(request: Request):
    """Liveness/readiness probe for the Java core and orchestrators."""
    return {
        "ok": True,
        "service": "analytics-ai",
        "version": __version__,
        "status": "up",
        "advisory_only": True,
        "correlation_id": getattr(request.state, "correlation_id", None),
    }


@app.post("/v1/forecast/outbreak", response_model=OutbreakForecastResponse, tags=["forecasting"],
          summary="Epidemic early-warning / التنبؤ المبكر بتفشي الأوبئة")
def outbreak(req: OutbreakForecastRequest):
    return forecast_outbreak(req)


@app.post("/v1/forecast/capacity", response_model=CapacityForecastResponse, tags=["forecasting"],
          summary="Bed/ICU capacity forecast / التنبؤ بإشغال الأسرة والعناية المركزة")
def capacity(req: CapacityForecastRequest):
    return forecast_capacity(req)


@app.post("/v1/amr/summary", response_model=AmrSummaryResponse, tags=["surveillance"],
          summary="AMR surveillance summary / ملخص ترصد مقاومة مضادات الميكروبات")
def amr_summary(req: AmrSummaryRequest):
    return summarise_amr(req)


@app.post("/v1/ai/triage-support", response_model=TriageSupportResponse, tags=["clinical-ai"],
          summary="NEWS2 triage support (advisory only) / دعم الفرز NEWS2 — استرشادي فقط")
def triage_support(req: TriageSupportRequest):
    return score_news2(req)
