from datetime import date, timedelta


def payload(occupied, total=20, unit="ICU"):
    start = date(2026, 11, 1)
    return {
        "facility_id": "FAC-CAIRO-001", "unit": unit, "total_beds": total,
        "series": [{"date": str(start + timedelta(days=i)), "occupied": v} for i, v in enumerate(occupied)],
    }


def test_half_full_steady_is_not_critical(client):
    r = client.post("/v1/forecast/capacity", json=payload([10] * 7))
    b = r.json()
    assert r.status_code == 200
    assert b["current_occupancy_pct"] == 50.0
    assert b["days_to_full"] is None  # flat trend never fills
    assert b["risk_level"] in {"low", "medium"}
    assert len(b["forecast_next_7_days"]) == 7


def test_rising_to_full_estimates_days(client):
    # +1 bed/day from 10 -> reaches 20 in 10 days
    r = client.post("/v1/forecast/capacity", json=payload([10, 11, 12, 13, 14, 15, 16]))
    b = r.json()
    assert b["trend_slope_beds_per_day"] == 1.0
    assert b["days_to_full"] == 4.0  # from 16, +1/day
    assert b["forecast_next_7_days"][-1]["predicted_occupied"] == 20.0  # capped at total


def test_nearly_full_is_critical(client):
    r = client.post("/v1/forecast/capacity", json=payload([19, 19, 20, 19, 20, 19, 19]))
    b = r.json()
    assert b["current_occupancy_pct"] == 95.0
    assert b["risk_level"] == "critical"
    assert b["factors_ar"]


def test_days_to_85_reported(client):
    r = client.post("/v1/forecast/capacity", json=payload([8, 9, 10, 11, 12], total=20))
    b = r.json()
    assert b["days_to_85_pct"] == 5.0  # 85% of 20 = 17, from 12 at +1/day


def test_invalid_total_beds_rejected(client):
    r = client.post("/v1/forecast/capacity", json=payload([1, 2, 3], total=0))
    assert r.status_code == 422
