"""Pure-Python statistics helpers (no numpy dependency).

Implements means, moving averages, ordinary least-squares linear
regression and log-linear (exponential) trend fitting used by the
forecasting modules. Everything is deterministic and explainable:
callers surface the fitted coefficients to the user as factors.
"""
from __future__ import annotations

import math
import statistics
from typing import Sequence


def mean(values: Sequence[float]) -> float:
    return float(statistics.fmean(values)) if values else 0.0


def moving_average(values: Sequence[float], window: int) -> list[float]:
    """Trailing moving average; early points average what is available."""
    if window <= 0:
        raise ValueError("window must be positive")
    out: list[float] = []
    for i in range(len(values)):
        start = max(0, i - window + 1)
        out.append(mean(values[start : i + 1]))
    return out


def linear_regression(xs: Sequence[float], ys: Sequence[float]) -> tuple[float, float, float]:
    """OLS fit y = slope*x + intercept. Returns (slope, intercept, r_squared)."""
    n = len(xs)
    if n == 0:
        return 0.0, 0.0, 0.0
    if n == 1:
        return 0.0, float(ys[0]), 0.0
    mx, my = mean(xs), mean(ys)
    sxx = sum((x - mx) ** 2 for x in xs)
    if sxx == 0:
        return 0.0, my, 0.0
    sxy = sum((x - mx) * (y - my) for x, y in zip(xs, ys))
    slope = sxy / sxx
    intercept = my - slope * mx
    ss_tot = sum((y - my) ** 2 for y in ys)
    ss_res = sum((y - (slope * x + intercept)) ** 2 for x, y in zip(xs, ys))
    r2 = 1.0 - (ss_res / ss_tot) if ss_tot > 0 else 0.0
    return slope, intercept, max(0.0, min(1.0, r2))


def exponential_growth_rate(values: Sequence[float]) -> float:
    """Daily growth rate from a log-linear fit over values > 0.

    Fits ln(y) = a + g*t and returns g (e.g. 0.10 ~= +10%/day).
    Falls back to 0.0 when there is not enough positive data.
    """
    pts = [(i, math.log(v)) for i, v in enumerate(values) if v > 0]
    if len(pts) < 2:
        return 0.0
    slope, _, _ = linear_regression([p[0] for p in pts], [p[1] for p in pts])
    return slope


def pct_change(new: float, old: float) -> float | None:
    """Percentage change from old to new; None when old is zero."""
    if old == 0:
        return None
    return (new - old) / old * 100.0
