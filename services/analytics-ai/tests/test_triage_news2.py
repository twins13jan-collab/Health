"""NEWS2 scoring: normal, boundary and red-flag cases, plus guardrails."""

NORMAL = {
    "age": 45, "respiratory_rate": 16, "spo2_pct": 98, "spo2_scale": 1,
    "on_supplemental_oxygen": False, "systolic_bp": 120, "pulse": 72,
    "consciousness": "A", "temperature_c": 36.8,
}


def post(client, **over):
    return client.post("/v1/ai/triage-support", json={**NORMAL, **over})


def test_normal_vitals_score_zero_low(client):
    r = post(client)
    b = r.json()
    assert r.status_code == 200
    assert b["news2_total"] == 0
    assert b["risk_category"] == "low"
    assert b["red_flag_parameters"] == []
    assert all(c["score"] == 0 for c in b["components"])


def test_guardrails_always_apply(client):
    for over in [{}, {"respiratory_rate": 30}, {"consciousness": "U", "spo2_pct": 85}]:
        b = post(client, **over).json()
        assert b["requires_human_review"] is True
        assert b["final_decision"] is None
        assert b["advisory_only"] is True


def test_single_red_flag_is_low_medium_not_low(client):
    # RR 26 alone scores 3 -> aggregate 3, but single-parameter rule applies.
    b = post(client, respiratory_rate=26).json()
    assert b["news2_total"] == 3
    assert b["risk_category"] == "low-medium"
    assert b["red_flag_parameters"] == ["Respiratory rate"]


def test_medium_band(client):
    # SpO2 92 on scale 1 (2) + on oxygen (2) + pulse 115 (2) = 6
    b = post(client, spo2_pct=92, on_supplemental_oxygen=True, pulse=115).json()
    assert b["news2_total"] == 6
    assert b["risk_category"] == "medium"


def test_high_band_critical_patient(client):
    b = post(client, respiratory_rate=28, spo2_pct=88, on_supplemental_oxygen=True,
             systolic_bp=85, pulse=135, consciousness="P", temperature_c=39.5).json()
    # RR3 + SpO2 3 + O2 2 + SBP 3 + pulse 3 + ACVPU 3 + temp 2 = 19
    assert b["news2_total"] == 19
    assert b["risk_category"] == "high"
    assert len(b["red_flag_parameters"]) >= 4


def test_rr_boundaries(client):
    cases = [(8, 3), (9, 1), (11, 1), (12, 0), (20, 0), (21, 2), (24, 2), (25, 3)]
    for rr, expected in cases:
        b = post(client, respiratory_rate=rr).json()
        comp = next(c for c in b["components"] if c["parameter"] == "Respiratory rate")
        assert comp["score"] == expected, f"RR {rr}"


def test_spo2_scale1_boundaries(client):
    cases = [(91, 3), (92, 2), (93, 2), (94, 1), (95, 1), (96, 0)]
    for spo2, expected in cases:
        b = post(client, spo2_pct=spo2).json()
        comp = next(c for c in b["components"] if c["parameter"] == "SpO2")
        assert comp["score"] == expected, f"SpO2 {spo2}"


def test_spo2_scale2_on_oxygen_above_target_scores(client):
    # Scale 2: 95% on oxygen scores 2 (above the 88-92 target range);
    # the same value on air scores 0.
    b_o2 = post(client, spo2_scale=2, spo2_pct=95, on_supplemental_oxygen=True).json()
    comp_o2 = next(c for c in b_o2["components"] if c["parameter"] == "SpO2")
    assert comp_o2["score"] == 2
    b_air = post(client, spo2_scale=2, spo2_pct=95, on_supplemental_oxygen=False).json()
    comp_air = next(c for c in b_air["components"] if c["parameter"] == "SpO2")
    assert comp_air["score"] == 0


def test_sbp_boundaries(client):
    cases = [(90, 3), (91, 2), (100, 2), (101, 1), (110, 1), (111, 0), (219, 0), (220, 3)]
    for sbp, expected in cases:
        b = post(client, systolic_bp=sbp).json()
        comp = next(c for c in b["components"] if c["parameter"] == "Systolic BP")
        assert comp["score"] == expected, f"SBP {sbp}"


def test_pulse_boundaries(client):
    cases = [(40, 3), (41, 1), (50, 1), (51, 0), (90, 0), (91, 1), (110, 1), (111, 2), (130, 2), (131, 3)]
    for pulse, expected in cases:
        b = post(client, pulse=pulse).json()
        comp = next(c for c in b["components"] if c["parameter"] == "Pulse")
        assert comp["score"] == expected, f"pulse {pulse}"


def test_temperature_boundaries(client):
    cases = [(35.0, 3), (35.1, 1), (36.0, 1), (36.1, 0), (38.0, 0), (38.1, 1), (39.0, 1), (39.1, 2)]
    for temp, expected in cases:
        b = post(client, temperature_c=temp).json()
        comp = next(c for c in b["components"] if c["parameter"] == "Temperature")
        assert comp["score"] == expected, f"temp {temp}"


def test_consciousness_non_alert_scores_3(client):
    for level in ["C", "V", "P", "U"]:
        b = post(client, consciousness=level).json()
        comp = next(c for c in b["components"] if "Consciousness" in c["parameter"])
        assert comp["score"] == 3, level


def test_oxygen_adds_2(client):
    b = post(client, on_supplemental_oxygen=True).json()
    comp = next(c for c in b["components"] if c["parameter"] == "Supplemental oxygen")
    assert comp["score"] == 2
    assert b["news2_total"] == 2


def test_under_18_rejected(client):
    r = post(client, age=12)
    assert r.status_code == 422
