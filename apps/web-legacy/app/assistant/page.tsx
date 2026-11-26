"use client";

import { useEffect, useRef, useState } from "react";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";

interface Msg { role: "user" | "assistant"; content: string; toolsUsed?: string[]; providerUsedInternal?: string }
interface ChatReply { reply?: string; toolsUsed?: string[]; model?: string; providerUsedInternal?: string; error?: string; message?: string }

const SUGGESTIONS_AR = [
  "ما حالة السعة السريرية الآن؟",
  "كم منشأة عاملة وكم مريضًا مسجلًا؟",
  "ما جلسات العيادات الخارجية اليوم؟",
  "ما وضع طوارئ منشأتي الآن؟",
];
const SUGGESTIONS_EN = [
  "What is the current bed capacity status?",
  "How many active facilities and registered patients?",
  "What are today's outpatient clinic sessions?",
  "What is my facility's ER status now?",
];

/** Khattab AI assistant: a local open-source LLM (Qwen2.5 via
 *  Ollama, running inside the deployment) with tool access to the
 *  platform's live data under the signed-in user's own permissions.
 *  Advisory only — it never invents figures and never decides
 *  clinically. */
export default function AssistantPage() {
  const { lang } = useLang();
  const ar = lang === "ar";
  const [messages, setMessages] = useState<Msg[]>([]);
  const [input, setInput] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const boxRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    boxRef.current?.scrollTo({ top: boxRef.current.scrollHeight, behavior: "smooth" });
  }, [messages, busy]);

  async function send(text: string) {
    const content = text.trim();
    if (!content || busy) return;
    const next: Msg[] = [...messages, { role: "user", content }];
    setMessages(next); setInput(""); setBusy(true); setError(null);
    try {
      const r = await api.postJson<ChatReply>("/api/analytics/ai/chat", {
        messages: next.map((m) => ({ role: m.role, content: m.content })),
      });
      if (r.error) {
        setError(r.message ?? r.error);
      } else {
        setMessages([...next, { role: "assistant", content: r.reply ?? "", toolsUsed: r.toolsUsed, providerUsedInternal: r.providerUsedInternal }]);
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    } finally {
      setBusy(false);
    }
  }

  return (
    <div>
      <div className="page-head">
        <h1>🤖 {ar ? "مساعد خطاب الذكي" : "Khattab AI Assistant"}</h1>
        <p>
          {ar
            ? "نموذج ذكاء اصطناعي مفتوح المصدر يعمل محليًا داخل بنية المنظومة (لا تغادر البيانات البنية أبدًا)، ومتصل بأدوات تجلب بيانات المنظومة الحية بصلاحيات حسابك أنت. دوره استشاري لدعم القرار — لا يخترع أرقامًا، والقرار السريري للطبيب دائمًا."
            : "An open-source AI model running locally inside the platform's own infrastructure (data never leaves the deployment), connected to tools that fetch the platform's live data under your own account's permissions. Advisory decision support — it never invents figures, and clinical decisions always belong to the physician."}
        </p>
      </div>
      {error && <div className="error-banner">{error}</div>}
      <div className="card">
        <div className="chat-box" ref={boxRef}>
          {messages.length === 0 && (
            <div>
              <p className="muted">{ar ? "جرّب أحد الأسئلة:" : "Try a question:"}</p>
              <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
                {(ar ? SUGGESTIONS_AR : SUGGESTIONS_EN).map((s) => (
                  <button key={s} type="button" className="btn btn-small btn-secondary" onClick={() => send(s)}>{s}</button>
                ))}
              </div>
            </div>
          )}
          {messages.map((m, i) => (
            <div key={i} className={`chat-msg ${m.role === "user" ? "chat-user" : "chat-bot"}`}>
              {m.content}
              {(m.toolsUsed && m.toolsUsed.length > 0) || m.providerUsedInternal ? (
                <div className="chat-tools">
                  {m.toolsUsed && m.toolsUsed.length > 0 ? <>🔧 {ar ? "أدوات استُخدمت" : "Tools used"}: {m.toolsUsed.join("، ")} </> : null}
                </div>
              ) : null}
            </div>
          ))}
          {busy && <div className="chat-msg chat-bot">{ar ? "المساعد يفكر ويجلب البيانات…" : "The assistant is thinking and fetching data…"}</div>}
        </div>
        <div className="chat-input-row">
          <input
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => { if (e.key === "Enter") send(input); }}
            placeholder={ar ? "اسأل عن السعة أو الطوارئ أو الجلسات أو المنشآت…" : "Ask about capacity, ER, sessions or facilities…"}
          />
          <button type="button" className="btn" disabled={busy || !input.trim()} onClick={() => send(input)}>
            {ar ? "إرسال" : "Send"}
          </button>
        </div>
        <p className="muted" style={{ fontSize: 12, marginBottom: 0 }}>
          {ar
            ? "النموذج المحلي: Qwen2.5 (مفتوح المصدر) عبر Ollama داخل خادم المنظومة. الإجابات استشارية وتتطلب مراجعة بشرية."
            : "Local model: Qwen2.5 (open source) via Ollama on the platform's own server. Answers are advisory and require human review."}
        </p>
      </div>
    </div>
  );
}
