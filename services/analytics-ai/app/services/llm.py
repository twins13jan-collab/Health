"""Local open-source LLM integration (Ollama) + tool-using agent.

The model runs INSIDE the platform's own infrastructure (a local
Ollama container serving an open-weight model, default
qwen2.5:3b) — no patient data ever leaves the deployment for AI.
The agent's tools read the platform's own APIs: public aggregates
without auth, and scoped operational endpoints with the caller's
own Authorization header forwarded, so the agent can only ever
see what the signed-in user is allowed to see.

Advisory only: answers support decisions; clinical content always
requires human review, and the agent must never invent numbers —
live figures come from tools or are stated as unavailable.
"""
import json
import os
import urllib.request
import urllib.error

OLLAMA_BASE_URL = os.environ.get("OLLAMA_BASE_URL", "http://ollama:11434")
AI_MODEL = os.environ.get("AI_MODEL", "qwen2.5:3b")
PLATFORM_API_BASE_URL = os.environ.get("PLATFORM_API_BASE_URL", "http://platform-api:8080")

SYSTEM_PROMPT = """أنت «مساعد خطاب» — مساعد ذكاء اصطناعي داخل منظومة خطاب للصحة العامة وإدارة الأزمات.
- أجب بالعربية افتراضيًا ما لم يكتب المستخدم بالإنجليزية.
- لأي أرقام أو حالة حية (سعة، أسرّة، انتظار طوارئ، جلسات عيادات، منشآت) استخدم الأدوات المتاحة دائمًا ولا تخترع أرقامًا أبدًا. إن تعذّرت الأداة قل ذلك صراحة.
- أنت دعم قرار استشاري: لا تصدر تشخيصًا أو قرارًا سريريًا نهائيًا، وذكّر بأن القرار للطبيب عند المحتوى السريري.
- كن مختصرًا وعمليًا، واستخدم الأرقام كما ترد من الأدوات حرفيًا."""

TOOLS = [
    {"type": "function", "function": {
        "name": "national_summary",
        "description": "المؤشرات الوطنية الحية: المنشآت العاملة، المرضى المسجلون، زيارات اليوم، إشغال الأسرّة والعناية، الإسعاف المتاح، والمنشآت حسب النوع والمحافظة",
        "parameters": {"type": "object", "properties": {}}}},
    {"type": "function", "function": {
        "name": "facility_directory",
        "description": "دليل المنشآت العاملة مع السعة السريرية والمقيمين حاليًا ونسبة الإشغال وانتظار الطوارئ لكل منشأة",
        "parameters": {"type": "object", "properties": {}}}},
    {"type": "function", "function": {
        "name": "capacity_by_governorate",
        "description": "السعة السريرية حسب المحافظة: الأسرّة المشغولة والكلية وأسرّة العناية المركزة وسيارات الإسعاف المتاحة",
        "parameters": {"type": "object", "properties": {}}}},
    {"type": "function", "function": {
        "name": "er_board",
        "description": "لوحة طوارئ منشأة المستخدم الآن: طابور الانتظار بمستويات الفرز وأزمنة الانتظار والتجاوزات (تتطلب صلاحية المستخدم)",
        "parameters": {"type": "object", "properties": {}}}},
    {"type": "function", "function": {
        "name": "clinic_sessions_today",
        "description": "جلسات العيادات الخارجية اليوم في منشأة المستخدم: العيادة والطبيب والوقت والمحجوز والسعة والمنتظرون",
        "parameters": {"type": "object", "properties": {}}}},
]

_TOOL_ROUTES = {
    "national_summary": ("/api/public/summary", False),
    "facility_directory": ("/api/public/facilities", False),
    "capacity_by_governorate": ("/api/public/capacity", False),
    "er_board": ("/api/encounters/er-board", True),
    "clinic_sessions_today": ("/api/clinics/sessions", True),
}


def _http_json(method, url, body=None, headers=None, timeout=30):
    data = json.dumps(body).encode("utf-8") if body is not None else None
    req = urllib.request.Request(url, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    for k, v in (headers or {}).items():
        req.add_header(k, v)
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        return json.loads(resp.read().decode("utf-8"))


def run_tool(name, authorization=None):
    """Execute one agent tool against the platform API. Returns a
    JSON-serialisable result or an error dict the model can report."""
    route = _TOOL_ROUTES.get(name)
    if route is None:
        return {"error": "unknown_tool", "tool": name}
    path, needs_auth = route
    headers = {}
    if needs_auth:
        if not authorization:
            return {"error": "not_authenticated", "tool": name,
                    "message": "هذه الأداة تتطلب مستخدمًا مسجلًا بمنشأة"}
        headers["Authorization"] = authorization
    try:
        return _http_json("GET", PLATFORM_API_BASE_URL + path, headers=headers, timeout=12)
    except Exception as exc:  # tool failure is data for the model, not a crash
        return {"error": "tool_failed", "tool": name, "detail": str(exc)[:200]}


def ollama_chat(messages, tools=None, authorization=None):
    """One raw Ollama chat call. Separated so tests can monkeypatch it."""
    payload = {"model": AI_MODEL, "messages": messages, "stream": False}
    if tools:
        payload["tools"] = tools
    return _http_json("POST", OLLAMA_BASE_URL + "/api/chat", body=payload, timeout=180)


# ------------------------------------------------------- external AI
# The full external provider group (OpenAI, Anthropic, Gemini,
# OpenRouter, Groq) speaks the OpenAI chat-completions format, so
# one client serves them all. Configuration arrives per-request
# from the platform proxy (sourced from the admin control panel);
# keys never persist in this service.
_IDENTIFIER_KEYS = {"patientname", "fullname", "fullnamear", "fullnameen", "name",
                    "nationalid", "phone", "recordedby", "email", "patientref",
                    "attendingname", "doctorname"}


def scrub_identifiers(value):
    """De-identification before anything leaves the deployment:
    tool results sent to an external model lose every direct
    identifier (names, national ids, phones, staff names). Counts,
    capacities, statuses and times — the operational substance —
    are untouched."""
    if isinstance(value, dict):
        return {k: scrub_identifiers(v) for k, v in value.items()
                if k.replace("_", "").lower() not in _IDENTIFIER_KEYS}
    if isinstance(value, list):
        return [scrub_identifiers(v) for v in value]
    return value


def external_http(messages, tools, cfg):
    """One OpenAI-compatible chat-completions call. Separated so
    tests can monkeypatch it."""
    payload = {"model": cfg["model"], "messages": messages}
    if tools:
        payload["tools"] = tools
    headers = {}
    if cfg.get("authStyle") == "x-api-key":
        headers["x-api-key"] = cfg["apiKey"]
    else:
        headers["Authorization"] = "Bearer " + cfg["apiKey"]
    return _http_json("POST", cfg["baseUrl"].rstrip("/") + "/chat/completions",
                      body=payload, headers=headers, timeout=120)


def _agent_external(user_messages, cfg, authorization=None):
    messages = [{"role": "system", "content": SYSTEM_PROMPT}] + list(user_messages)
    tools_used = []
    for _ in range(3):
        resp = external_http(messages, TOOLS, cfg)
        choices = resp.get("choices") or []
        msg = (choices[0].get("message") if choices else None) or {}
        calls = msg.get("tool_calls") or []
        if not calls:
            return ({
                "reply": (msg.get("content") or "").strip(),
                "toolsUsed": tools_used,
                "model": cfg["model"],
                "providerUsed": cfg["name"],
                "advisory": True,
                "requiresHumanReview": True,
            }, 200)
        messages.append({"role": "assistant", "content": msg.get("content") or "",
                         "tool_calls": calls})
        for call in calls:
            fn = (call.get("function") or {})
            name = fn.get("name") or ""
            result = scrub_identifiers(run_tool(name, authorization=authorization))
            tools_used.append(name)
            messages.append({"role": "tool", "tool_call_id": call.get("id") or name,
                             "content": json.dumps(result, ensure_ascii=False)[:6000]})
    resp = external_http(messages, None, cfg)
    choices = resp.get("choices") or []
    msg = (choices[0].get("message") if choices else None) or {}
    return ({
        "reply": (msg.get("content") or "").strip(),
        "toolsUsed": tools_used,
        "model": cfg["model"],
        "providerUsed": cfg["name"],
        "advisory": True,
        "requiresHumanReview": True,
    }, 200)


def _agent_local(user_messages, authorization=None):
    """Bounded tool-using loop on the local model: at most 3 tool
    rounds, then a final answer. Returns (reply_dict, http_status)."""
    messages = [{"role": "system", "content": SYSTEM_PROMPT}] + list(user_messages)
    tools_used = []
    try:
        for _ in range(3):
            resp = ollama_chat(messages, tools=TOOLS, authorization=authorization)
            msg = resp.get("message") or {}
            calls = msg.get("tool_calls") or []
            if not calls:
                return ({
                    "reply": (msg.get("content") or "").strip(),
                    "toolsUsed": tools_used,
                    "model": AI_MODEL,
                    "providerUsed": "local",
                    "advisory": True,
                    "requiresHumanReview": True,
                }, 200)
            messages.append({"role": "assistant", "content": msg.get("content") or "",
                             "tool_calls": calls})
            for call in calls:
                fn = (call.get("function") or {})
                name = fn.get("name") or ""
                result = run_tool(name, authorization=authorization)
                tools_used.append(name)
                messages.append({"role": "tool", "content": json.dumps(result, ensure_ascii=False)[:6000]})
        # Tool budget exhausted: one last answer without tools.
        resp = ollama_chat(messages, tools=None, authorization=authorization)
        msg = resp.get("message") or {}
        return ({
            "reply": (msg.get("content") or "").strip(),
            "toolsUsed": tools_used,
            "model": AI_MODEL,
            "providerUsed": "local",
            "advisory": True,
            "requiresHumanReview": True,
        }, 200)
    except (urllib.error.URLError, TimeoutError, OSError, ValueError) as exc:
        return ({
            "error": "llm_unavailable",
            "message": "نموذج الذكاء الاصطناعي المحلي غير متاح الآن — لم تُعرض أي إجابة بديلة",
            "detail": str(exc)[:200],
        }, 503)


def agent_chat(user_messages, authorization=None, provider=None):
    """Route: the admin-activated external provider when configured,
    with the local open-source model as the always-available
    fallback. An external failure falls back instead of failing the
    user; only when both are down does the honest 503 surface."""
    if provider:
        try:
            return _agent_external(user_messages, provider, authorization=authorization)
        except Exception:
            body, status = _agent_local(user_messages, authorization=authorization)
            if status == 200:
                body["providerUsed"] = "local-fallback"
                body["externalError"] = provider.get("name")
            return body, status
    return _agent_local(user_messages, authorization=authorization)
