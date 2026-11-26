"""Chat agent tests: the Ollama call and tool execution are
monkeypatched — what is verified is the agent loop contract:
plain answers pass through, tool calls are executed and fed back,
and an unreachable model yields an honest 503 (never a fake reply)."""
import urllib.error

from app.services import llm


def _client():
    from fastapi.testclient import TestClient
    from app.main import app
    return TestClient(app)


def test_plain_reply(monkeypatch):
    monkeypatch.setattr(llm, "ollama_chat",
                        lambda messages, tools=None, authorization=None:
                        {"message": {"role": "assistant", "content": "أهلًا بك في منظومة خطاب"}})
    r = _client().post("/v1/ai/chat", json={"messages": [{"role": "user", "content": "مرحبا"}]})
    assert r.status_code == 200
    body = r.json()
    assert "خطاب" in body["reply"]
    assert body["toolsUsed"] == []
    assert body["advisory"] is True and body["requiresHumanReview"] is True


def test_tool_roundtrip(monkeypatch):
    calls = {"n": 0}

    def fake_chat(messages, tools=None, authorization=None):
        calls["n"] += 1
        if calls["n"] == 1:
            return {"message": {"role": "assistant", "content": "",
                                "tool_calls": [{"function": {"name": "national_summary", "arguments": {}}}]}}
        # second round: the tool result must have been fed back
        tool_msgs = [m for m in messages if m.get("role") == "tool"]
        assert tool_msgs and "facilitiesActive" in tool_msgs[0]["content"]
        return {"message": {"role": "assistant", "content": "المنشآت العاملة: 1"}}

    monkeypatch.setattr(llm, "ollama_chat", fake_chat)
    monkeypatch.setattr(llm, "run_tool",
                        lambda name, authorization=None: {"kpis": {"facilitiesActive": 1}})
    r = _client().post("/v1/ai/chat", json={"messages": [{"role": "user", "content": "كم منشأة عاملة؟"}]})
    assert r.status_code == 200
    body = r.json()
    assert body["toolsUsed"] == ["national_summary"]
    assert "1" in body["reply"]


def test_llm_unavailable_is_honest_503(monkeypatch):
    def boom(messages, tools=None, authorization=None):
        raise urllib.error.URLError("connection refused")

    monkeypatch.setattr(llm, "ollama_chat", boom)
    r = _client().post("/v1/ai/chat", json={"messages": [{"role": "user", "content": "مرحبا"}]})
    assert r.status_code == 503
    assert r.json()["error"] == "llm_unavailable"


def test_scoped_tool_without_auth_reports_not_authenticated():
    out = llm.run_tool("er_board", authorization=None)
    assert out["error"] == "not_authenticated"


# ------------------------------------------------------- external AI
CFG = {"name": "openrouter", "baseUrl": "https://openrouter.ai/api/v1",
       "model": "openai/gpt-4o-mini", "apiKey": "sk-test", "authStyle": "bearer"}


def test_external_plain_reply(monkeypatch):
    monkeypatch.setattr(llm, "external_http",
                        lambda messages, tools, cfg: {"choices": [{"message": {"content": "إجابة خارجية"}}]})
    r = _client().post("/v1/ai/chat", json={
        "messages": [{"role": "user", "content": "مرحبا"}], "provider": CFG})
    assert r.status_code == 200
    body = r.json()
    assert body["reply"] == "إجابة خارجية"
    assert body["providerUsed"] == "openrouter"


def test_external_tool_results_are_scrubbed(monkeypatch):
    seen = {}
    calls = {"n": 0}

    def fake_http(messages, tools, cfg):
        calls["n"] += 1
        if calls["n"] == 1:
            return {"choices": [{"message": {"content": "", "tool_calls": [
                {"id": "c1", "function": {"name": "er_board", "arguments": "{}"}}]}}]}
        tool_msgs = [m for m in messages if m.get("role") == "tool"]
        seen["tool_content"] = tool_msgs[0]["content"]
        return {"choices": [{"message": {"content": "يوجد مريض واحد منتظر"}}]}

    monkeypatch.setattr(llm, "external_http", fake_http)
    monkeypatch.setattr(llm, "run_tool", lambda name, authorization=None: {
        "queue": [{"patientName": "اسم سري", "nationalId": "29901011234567", "waitMinutes": 30}]})
    r = _client().post("/v1/ai/chat", json={
        "messages": [{"role": "user", "content": "حالة الطوارئ؟"}], "provider": CFG},
        headers={"Authorization": "Bearer token"})
    assert r.status_code == 200
    assert "اسم سري" not in seen["tool_content"]
    assert "29901011234567" not in seen["tool_content"]
    assert "30" in seen["tool_content"]  # operational substance survives


def test_external_failure_falls_back_to_local(monkeypatch):
    def boom(messages, tools, cfg):
        raise urllib.error.URLError("provider down")

    monkeypatch.setattr(llm, "external_http", boom)
    monkeypatch.setattr(llm, "ollama_chat",
                        lambda messages, tools=None, authorization=None:
                        {"message": {"role": "assistant", "content": "إجابة محلية احتياطية"}})
    r = _client().post("/v1/ai/chat", json={
        "messages": [{"role": "user", "content": "مرحبا"}], "provider": CFG})
    assert r.status_code == 200
    assert r.json()["providerUsed"] == "local-fallback"


def test_scrub_recursion():
    out = llm.scrub_identifiers({"a": [{"patientName": "x", "count": 3}], "facility": {"name": "y", "beds": 10}})
    assert out == {"a": [{"count": 3}], "facility": {"beds": 10}}
