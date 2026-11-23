from datetime import date, timedelta


def series_payload(values, governorate="القاهرة", **kw):
    start = date(2026, 11, 1)
    return {
        "governorate": governorate,
        "disease": "influenza-like illness",
        "series": [{"date": str(start + timedelta(days=i)), "cases": v} for i, v in enumerate(values)],
        **kw,
    }


def test_flat_series_is_low_risk(client):
    r = client.post("/v1/forecast/outbreak", json=series_payload([10] * 21))
    assert r.status_code == 200
    b = r.json()
    assert b["ok"] is True and b["advisory_only"] is True
    assert b["risk_level"] == "low"
    assert b["moving_average_7d"] == 10.0
    assert b["trend_direction"] == "stable"
    assert len(b["forecast_next_7_days"]) == 7
    assert b["factors"] and b["factors_ar"]


def test_explosive_growth_is_high_or_critical(client):
    values = [5, 6, 5, 7, 8, 9, 10, 12, 15, 19, 24, 30, 38, 48, 60, 75, 95, 120, 150, 190, 240]
    r = client.post("/v1/forecast/outbreak", json=series_payload(values, population=10_000_000))
    b = r.json()
    assert b["risk_level"] in {"high", "critical"}
    assert b["growth_rate_pct"] is not None and b["growth_rate_pct"] > 50
    assert b["doubling_time_days"] is not None and b["doubling_time_days"] <= 14
    assert b["forecast_total_next_7_days"] > sum(values[-7:])
    assert b["incidence_per_100k_latest"] is not None
    assert any("growth" in f.lower() or "doubling" in f.lower() for f in b["factors"])


def test_declining_series_not_high_risk(client):
    values = list(range(100, 58, -2))  # 100 .. 60 over 21 days
    r = client.post("/v1/forecast/outbreak", json=series_payload(values))
    b = r.json()
    assert b["trend_direction"] == "falling"
    assert b["risk_level"] in {"low", "medium"}
    assert all(f["predicted_cases"] >= 0 for f in b["forecast_next_7_days"])


def test_short_series_rejected(client):
    r = client.post("/v1/forecast/outbreak", json=series_payload([1, 2, 3]))
    assert r.status_code == 422
    assert r.json()["ok"] is False


def test_negative_cases_rejected(client):
    p = series_payload([10] * 10)
    p["series"][3]["cases"] = -1
    r = client.post("/v1/forecast/outbreak", json=p)
    assert r.status_code == 422


def test_unsorted_series_is_handled(client):
    p = series_payload([10, 12, 11, 13, 12, 14, 13, 15, 14, 16, 15, 17, 16, 18])
    p["series"] = list(reversed(p["series"]))
    r = client.post("/v1/forecast/outbreak", json=p)
    assert r.status_code == 200
    assert r.json()["latest_cases"] == 18
