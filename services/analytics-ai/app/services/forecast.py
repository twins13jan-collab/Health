"""Epidemic early-warning and bed/ICU capacity forecasting.

Both forecasters combine a linear trend (OLS) with a log-linear
(exponential) growth estimate, and explain — in English and Arabic —
exactly which factors drove the risk level. Outputs are advisory only.
"""
from __future__ import annotations

import math

from ..models import (
    CapacityForecastRequest,
    CapacityForecastResponse,
    ForecastDay,
    OutbreakForecastRequest,
    OutbreakForecastResponse,
)
from .stats import exponential_growth_rate, linear_regression, mean, moving_average, pct_change

RISK_AR = {"low": "منخفض", "medium": "متوسط", "high": "مرتفع", "critical": "حرج"}


# ------------------------------------------------------------------ outbreak
def forecast_outbreak(req: OutbreakForecastRequest) -> OutbreakForecastResponse:
    series = sorted(req.series, key=lambda p: p.date)
    values = [float(p.cases) for p in series]
    n = len(values)

    ma7_series = moving_average(values, 7)
    ma14_series = moving_average(values, 14)
    ma7 = ma7_series[-1]
    ma14 = ma14_series[-1] if n >= 7 else None

    xs = list(range(n))
    slope, _intercept, r2 = linear_regression(xs, values)
    if slope > 0.5:
        direction = "rising"
    elif slope < -0.5:
        direction = "falling"
    else:
        direction = "stable"

    # Growth: last-7-day mean vs the 7 days before it.
    growth_pct: float | None = None
    if n >= 14:
        growth_pct = pct_change(mean(values[-7:]), mean(values[-14:-7]))

    # Exponential forecast anchored on the 7-day moving average, with the
    # fitted daily growth rate damped (halved beyond day 3) to avoid
    # runaway projections; floored at zero.
    g = exponential_growth_rate(values[-14:])
    forecast: list[ForecastDay] = []
    for d in range(1, 8):
        eff_g = g if d <= 3 else g * 0.5
        pred = ma7 * math.exp(eff_g * d)
        # Blend with the linear trend so a purely exponential model does
        # not dominate when the series is really linear.
        linear_pred = max(0.0, values[-1] + slope * d)
        blended = 0.6 * pred + 0.4 * linear_pred
        forecast.append(ForecastDay(day_offset=d, predicted_cases=round(max(0.0, blended), 1)))

    doubling: float | None = None
    if g > 0.001:
        doubling = round(math.log(2) / g, 1)

    incidence = None
    if req.population:
        incidence = round(values[-1] / req.population * 100_000, 2)

    # ---- explainable risk scoring -------------------------------------
    factors: list[str] = []
    factors_ar: list[str] = []
    points = 0
    if growth_pct is not None:
        if growth_pct >= 50:
            points += 3
            factors.append(f"Case growth of {growth_pct:.0f}% week-over-week (>= 50%)")
            factors_ar.append(f"نمو الحالات بنسبة {growth_pct:.0f}% أسبوعيًا (٥٠٪ فأكثر)")
        elif growth_pct >= 20:
            points += 2
            factors.append(f"Case growth of {growth_pct:.0f}% week-over-week (>= 20%)")
            factors_ar.append(f"نمو الحالات بنسبة {growth_pct:.0f}% أسبوعيًا (٢٠٪ فأكثر)")
        elif growth_pct >= 5:
            points += 1
            factors.append(f"Case growth of {growth_pct:.0f}% week-over-week")
            factors_ar.append(f"نمو الحالات بنسبة {growth_pct:.0f}% أسبوعيًا")
        elif growth_pct <= -10:
            factors.append(f"Cases falling {abs(growth_pct):.0f}% week-over-week")
            factors_ar.append(f"انخفاض الحالات بنسبة {abs(growth_pct):.0f}% أسبوعيًا")
    if direction == "rising" and r2 >= 0.3:
        points += 1
        factors.append(f"Sustained rising linear trend (slope {slope:.1f}/day, R² {r2:.2f})")
        factors_ar.append(f"اتجاه تصاعدي مستمر (الميل {slope:.1f}/يوم)")
    if ma14 is not None and ma14 > 0 and ma7 > ma14 * 1.25:
        points += 1
        factors.append("7-day average is >25% above the 14-day average (acceleration)")
        factors_ar.append("متوسط ٧ أيام أعلى بأكثر من ٢٥٪ من متوسط ١٤ يومًا (تسارع)")
    if doubling is not None and doubling <= 7:
        points += 2
        factors.append(f"Estimated doubling time {doubling} days (<= 7)")
        factors_ar.append(f"زمن التضاعف التقديري {doubling} يوم (٧ أيام أو أقل)")
    elif doubling is not None and doubling <= 14:
        points += 1
        factors.append(f"Estimated doubling time {doubling} days (<= 14)")
        factors_ar.append(f"زمن التضاعف التقديري {doubling} يوم (١٤ يومًا أو أقل)")
    if incidence is not None and incidence >= 50:
        points += 1
        factors.append(f"Latest incidence {incidence}/100k population")
        factors_ar.append(f"معدل الإصابة الأخير {incidence} لكل ١٠٠ ألف نسمة")
    if not factors:
        factors.append("No accelerating signal detected in the supplied series")
        factors_ar.append("لا توجد إشارة تسارع في السلسلة المُدخلة")

    risk = "critical" if points >= 5 else "high" if points >= 3 else "medium" if points >= 1 else "low"

    return OutbreakForecastResponse(
        governorate=req.governorate,
        disease=req.disease,
        observations=n,
        latest_cases=int(values[-1]),
        moving_average_7d=round(ma7, 2),
        moving_average_14d=round(ma14, 2) if ma14 is not None else None,
        trend_slope_per_day=round(slope, 3),
        trend_direction=direction,
        growth_rate_pct=round(growth_pct, 1) if growth_pct is not None else None,
        doubling_time_days=doubling,
        forecast_next_7_days=forecast,
        forecast_total_next_7_days=round(sum(f.predicted_cases for f in forecast), 1),
        incidence_per_100k_latest=incidence,
        risk_level=risk,
        risk_level_ar=RISK_AR[risk],
        factors=factors,
        factors_ar=factors_ar,
    )


# ------------------------------------------------------------------ capacity
def forecast_capacity(req: CapacityForecastRequest) -> CapacityForecastResponse:
    series = sorted(req.series, key=lambda p: p.date)
    occupied = [float(p.occupied) for p in series]
    total = float(req.total_beds)
    n = len(occupied)

    slope, intercept, _r2 = linear_regression(list(range(n)), occupied)
    current = occupied[-1]
    current_pct = current / total * 100.0

    forecast_7: list[dict] = []
    for d in range(1, 8):
        pred = min(total, max(0.0, intercept + slope * (n - 1 + d)))
        forecast_7.append(
            {"day_offset": d, "predicted_occupied": round(pred, 1),
             "predicted_occupancy_pct": round(pred / total * 100.0, 1)}
        )
    pred14 = min(total, max(0.0, intercept + slope * (n - 1 + 14)))
    pct14 = round(pred14 / total * 100.0, 1)

    def days_to(target_beds: float) -> float | None:
        if current >= target_beds:
            return 0.0
        if slope <= 0:
            return None
        return round((target_beds - current) / slope, 1)

    days_full = days_to(total)
    days_85 = days_to(total * 0.85)

    factors: list[str] = []
    factors_ar: list[str] = []
    if current_pct >= 95:
        risk = "critical"
        factors.append(f"Current occupancy {current_pct:.0f}% (>= 95%)")
        factors_ar.append(f"الإشغال الحالي {current_pct:.0f}% (٩٥٪ فأكثر)")
    elif current_pct >= 85:
        risk = "high"
        factors.append(f"Current occupancy {current_pct:.0f}% (>= 85%)")
        factors_ar.append(f"الإشغال الحالي {current_pct:.0f}% (٨٥٪ فأكثر)")
    elif current_pct >= 70 or (days_full is not None and days_full <= 7):
        risk = "medium"
        factors.append(f"Current occupancy {current_pct:.0f}%")
        factors_ar.append(f"الإشغال الحالي {current_pct:.0f}%")
    else:
        risk = "low"
        factors.append(f"Current occupancy {current_pct:.0f}% with headroom")
        factors_ar.append(f"الإشغال الحالي {current_pct:.0f}% مع سعة متاحة")
    if slope > 0:
        factors.append(f"Occupancy rising by {slope:.2f} beds/day")
        factors_ar.append(f"الإشغال يرتفع بمعدل {slope:.2f} سرير/يوم")
        if days_full is not None:
            factors.append(f"Estimated {days_full} days until fully occupied at current trend")
            factors_ar.append(f"تقدير {days_full} يوم حتى الإشغال الكامل بالمعدل الحالي")
    elif slope < 0:
        factors.append(f"Occupancy falling by {abs(slope):.2f} beds/day")
        factors_ar.append(f"الإشغال ينخفض بمعدل {abs(slope):.2f} سرير/يوم")

    # A unit that will fill within 48h is critical regardless of today.
    if days_full is not None and days_full <= 2 and risk != "critical":
        risk = "critical"
        factors.append("Projected to reach 100% occupancy within 2 days")
        factors_ar.append("متوقع الوصول إلى إشغال كامل خلال يومين")

    return CapacityForecastResponse(
        facility_id=req.facility_id,
        unit=req.unit,
        total_beds=req.total_beds,
        current_occupied=int(current),
        current_occupancy_pct=round(current_pct, 1),
        trend_slope_beds_per_day=round(slope, 3),
        forecast_next_7_days=forecast_7,
        forecast_next_14_days_occupancy_pct=pct14,
        days_to_full=days_full,
        days_to_85_pct=days_85,
        risk_level=risk,
        risk_level_ar=RISK_AR[risk],
        factors=factors,
        factors_ar=factors_ar,
    )
