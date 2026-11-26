"""Pydantic v2 request/response schemas for the Analytics & AI service."""
from __future__ import annotations

from datetime import date
from enum import Enum
from typing import Any

from pydantic import BaseModel, Field, field_validator


# ---------------------------------------------------------------- outbreak
class CasePoint(BaseModel):
    date: date
    cases: int = Field(ge=0)

    @field_validator("cases")
    @classmethod
    def _cases_int(cls, v: int) -> int:
        return int(v)


class OutbreakForecastRequest(BaseModel):
    governorate: str = Field(min_length=1, examples=["القاهرة", "Cairo"])
    disease: str = Field(default="unspecified", examples=["influenza-like illness"])
    series: list[CasePoint] = Field(min_length=7, description="Daily case counts, chronological; at least 7 points")
    population: int | None = Field(default=None, gt=0, description="Optional governorate population for per-100k rates")


class ForecastDay(BaseModel):
    day_offset: int
    predicted_cases: float


class OutbreakForecastResponse(BaseModel):
    ok: bool = True
    advisory_only: bool = True
    governorate: str
    disease: str
    observations: int
    latest_cases: int
    moving_average_7d: float
    moving_average_14d: float | None
    trend_slope_per_day: float
    trend_direction: str  # rising | falling | stable
    growth_rate_pct: float | None
    doubling_time_days: float | None
    forecast_next_7_days: list[ForecastDay]
    forecast_total_next_7_days: float
    incidence_per_100k_latest: float | None
    risk_level: str  # low | medium | high | critical
    risk_level_ar: str
    factors: list[str]
    factors_ar: list[str]


# ---------------------------------------------------------------- capacity
class OccupancyPoint(BaseModel):
    date: date
    occupied: int = Field(ge=0)


class CapacityForecastRequest(BaseModel):
    facility_id: str = Field(min_length=1)
    unit: str = Field(default="ICU", examples=["ICU", "general ward", "ER"])
    total_beds: int = Field(gt=0)
    series: list[OccupancyPoint] = Field(min_length=3, description="Daily occupied-bed counts, chronological")

    @field_validator("series")
    @classmethod
    def _occupied_within_total(cls, v: list[OccupancyPoint]) -> list[OccupancyPoint]:
        return v


class CapacityForecastResponse(BaseModel):
    ok: bool = True
    advisory_only: bool = True
    facility_id: str
    unit: str
    total_beds: int
    current_occupied: int
    current_occupancy_pct: float
    trend_slope_beds_per_day: float
    forecast_next_7_days: list[dict[str, Any]]
    forecast_next_14_days_occupancy_pct: float
    days_to_full: float | None
    days_to_85_pct: float | None
    risk_level: str
    risk_level_ar: str
    factors: list[str]
    factors_ar: list[str]


# --------------------------------------------------------------------- AMR
class ResistanceRecord(BaseModel):
    organism: str = Field(min_length=1)
    antibiotic: str = Field(min_length=1)
    result: str = Field(description="R (resistant), S (susceptible) or I (intermediate)")

    @field_validator("result")
    @classmethod
    def _normalise_result(cls, v: str) -> str:
        u = v.strip().upper()
        if u not in {"R", "S", "I", "RESISTANT", "SUSCEPTIBLE", "INTERMEDIATE"}:
            raise ValueError("result must be R, S or I")
        return {"RESISTANT": "R", "SUSCEPTIBLE": "S", "INTERMEDIATE": "I"}.get(u, u)


class AmrSummaryRequest(BaseModel):
    records: list[ResistanceRecord] = Field(min_length=1)
    alert_threshold_pct: float = Field(default=20.0, ge=0, le=100)
    min_isolates_for_alert: int = Field(default=10, ge=1)


class AmrPairSummary(BaseModel):
    organism: str
    antibiotic: str
    isolates: int
    resistant: int
    intermediate: int
    susceptible: int
    resistance_pct: float
    alert: bool


class AmrSummaryResponse(BaseModel):
    ok: bool = True
    advisory_only: bool = True
    total_records: int
    pairs: list[AmrPairSummary]
    alerts: list[AmrPairSummary]
    alert_threshold_pct: float


# ------------------------------------------------------------------ triage
class Consciousness(str, Enum):
    alert = "A"
    confusion = "C"
    voice = "V"
    pain = "P"
    unresponsive = "U"


class TriageSupportRequest(BaseModel):
    """Adult vitals for NEWS2 scoring. SpO2 scale 2 is for patients with
    a target range of 88-92% (e.g. hypercapnic respiratory failure)."""

    age: int = Field(ge=18, description="NEWS2 is validated for adults (16+); this service requires 18+")
    respiratory_rate: int = Field(ge=0, le=80)
    spo2_pct: float = Field(ge=0, le=100)
    spo2_scale: int = Field(default=1, ge=1, le=2)
    on_supplemental_oxygen: bool = False
    systolic_bp: int = Field(ge=30, le=350)
    pulse: int = Field(ge=0, le=300)
    consciousness: Consciousness = Consciousness.alert
    temperature_c: float = Field(ge=25.0, le=45.0)


class News2Component(BaseModel):
    parameter: str
    parameter_ar: str
    value: str
    score: int


class TriageSupportResponse(BaseModel):
    ok: bool = True
    advisory_only: bool = True
    requires_human_review: bool = True  # ALWAYS true — hard guardrail
    final_decision: None = None  # NEVER a final decision from this service
    news2_total: int
    components: list[News2Component]
    risk_category: str  # low | low-medium | medium | high
    risk_category_ar: str
    suggested_response: str
    suggested_response_ar: str
    red_flag_parameters: list[str]
    disclaimer: str
    disclaimer_ar: str


# ---------------------------------------------------------------- ai chat
class ChatMessage(BaseModel):
    role: str = Field(pattern="^(user|assistant)$")
    content: str = Field(min_length=1, max_length=4000)


class ProviderConfig(BaseModel):
    """External AI provider configuration, injected by the platform
    proxy from the admin control panel — never sent by browsers."""
    name: str
    baseUrl: str
    model: str
    apiKey: str
    authStyle: str = "bearer"


class ChatRequest(BaseModel):
    messages: list[ChatMessage] = Field(min_length=1, max_length=20)
    provider: ProviderConfig | None = None


class ChatResponse(BaseModel):
    reply: str | None = None
    providerUsed: str | None = None
    toolsUsed: list[str] = []
    model: str | None = None
    advisory: bool = True
    requiresHumanReview: bool = True
    error: str | None = None
    message: str | None = None
    detail: str | None = None
