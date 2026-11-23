def rec(org, abx, result, n=1):
    return [{"organism": org, "antibiotic": abx, "result": result}] * n


def test_aggregation_and_percentages(client):
    records = rec("E. coli", "Ciprofloxacin", "R", 3) + rec("E. coli", "Ciprofloxacin", "S", 7)
    r = client.post("/v1/amr/summary", json={"records": records})
    b = r.json()
    assert r.status_code == 200
    assert b["total_records"] == 10
    pair = b["pairs"][0]
    assert pair["isolates"] == 10 and pair["resistant"] == 3 and pair["susceptible"] == 7
    assert pair["resistance_pct"] == 30.0


def test_alert_above_threshold_with_enough_isolates(client):
    records = rec("MRSA", "Oxacillin", "R", 9) + rec("MRSA", "Oxacillin", "S", 1)
    r = client.post("/v1/amr/summary", json={"records": records, "alert_threshold_pct": 20.0})
    b = r.json()
    assert len(b["alerts"]) == 1
    assert b["alerts"][0]["resistance_pct"] == 90.0


def test_no_alert_when_too_few_isolates(client):
    # 100% resistance but only 2 isolates < default min of 10: no alert.
    records = rec("Candida auris", "Fluconazole", "R", 2)
    r = client.post("/v1/amr/summary", json={"records": records})
    b = r.json()
    assert b["pairs"][0]["resistance_pct"] == 100.0
    assert b["alerts"] == []


def test_no_alert_below_threshold(client):
    records = rec("S. pneumoniae", "Penicillin", "R", 1) + rec("S. pneumoniae", "Penicillin", "S", 19)
    r = client.post("/v1/amr/summary", json={"records": records})
    assert r.json()["alerts"] == []


def test_result_synonyms_and_intermediate(client):
    records = (
        rec("K. pneumoniae", "Meropenem", "resistant", 2)
        + rec("K. pneumoniae", "Meropenem", "I", 1)
        + rec("K. pneumoniae", "Meropenem", "susceptible", 7)
    )
    r = client.post("/v1/amr/summary", json={"records": records})
    pair = r.json()["pairs"][0]
    assert pair["resistant"] == 2 and pair["intermediate"] == 1 and pair["susceptible"] == 7


def test_multiple_pairs_sorted_by_resistance(client):
    records = (
        rec("A", "X", "R", 1) + rec("A", "X", "S", 9)
        + rec("B", "Y", "R", 8) + rec("B", "Y", "S", 2)
    )
    r = client.post("/v1/amr/summary", json={"records": records})
    pairs = r.json()["pairs"]
    assert pairs[0]["organism"] == "B" and pairs[0]["resistance_pct"] == 80.0


def test_invalid_result_rejected(client):
    r = client.post("/v1/amr/summary", json={"records": [{"organism": "A", "antibiotic": "X", "result": "maybe"}]})
    assert r.status_code == 422
    assert r.json()["ok"] is False
