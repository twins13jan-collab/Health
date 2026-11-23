def test_health(client):
    r = client.get("/health")
    assert r.status_code == 200
    body = r.json()
    assert body["ok"] is True
    assert body["status"] == "up"
    assert body["service"] == "analytics-ai"


def test_correlation_id_echoed_and_generated(client):
    r = client.get("/health", headers={"X-Correlation-Id": "corr-123-abc"})
    assert r.headers["X-Correlation-Id"] == "corr-123-abc"
    assert r.json()["correlation_id"] == "corr-123-abc"
    r2 = client.get("/health")
    assert r2.headers["X-Correlation-Id"]  # generated when absent


def test_openapi_available(client):
    r = client.get("/openapi.json")
    assert r.status_code == 200
    paths = r.json()["paths"]
    for p in ["/v1/forecast/outbreak", "/v1/forecast/capacity", "/v1/amr/summary", "/v1/ai/triage-support"]:
        assert p in paths


def test_validation_error_shape(client):
    r = client.post("/v1/forecast/outbreak", json={"governorate": "Cairo", "series": []})
    assert r.status_code == 422
    body = r.json()
    assert body["ok"] is False
    assert body["error"] == "validation_error"
    assert "message" in body


def test_not_found_error_shape(client):
    r = client.get("/v1/does-not-exist")
    assert r.status_code == 404
    body = r.json()
    assert body["ok"] is False
    assert body["error"] == "http_error"
