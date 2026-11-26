"use client";

import { useEffect, useRef, useState } from "react";
import { PageHead } from "@/components/ui";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";

interface Msg { role: "user" | "assistant"; content: string; toolsUsed?: string[] }
interface ChatReply { reply?: string; toolsUsed?: string[]; error?: string; message?: string }

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

/** Khattab AI assistant: an AI model with tool access to the
 *  platform's live data under the signed-in user's own permissions.
 *  Advisory only — it never invents figures and never decides
 *  clinically. The answering provider is never displayed. */
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
        setMessages([...next, { role: "assistant", content: r.reply ?? "", toolsUsed: r.toolsUsed }]);
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    } finally {
      setBusy(false);
    }
  }

  return (
    <>
      <PageHead
        eyebrow={ar ? "المساعد الذكي" : "AI Assistant"}
        title={`🤖 ${ar ? "مساعد خطاب الذكي" : "Khattab AI Assistant"}`}
        sub={ar
          ? "نموذج ذكاء اصطناعي متصل بأدوات تجلب بيانات المنظومة الحية بصلاحيات حسابك أنت. دوره استشاري لدعم القرار — لا يخترع أرقامًا، والقرار السريري للطبيب دائمًا."
          : "An AI model connected to tools that fetch the platform's live data under your own account's permissions. Advisory decision support — it never invents figures, and clinical decisions always belong to the physician."}
      />
      {error && <div className="alert-error">{error}</div>}
      <div className="card">
        <div className="chat-log chat-page-log" ref={boxRef}>
          {messages.length === 0 && (
            <div>
              <p className="muted">{ar ? "جرّب أحد الأسئلة:" : "Try a question:"}</p>
              <div className="chat-sugg mt">
                {(ar ? SUGGESTIONS_AR : SUGGESTIONS_EN).map((s) => (
                  <button key={s} type="button" className="btn btn-soft btn-sm" onClick={() => send(s)}>{s}</button>
                ))}
              </div>
            </div>
          )}
          {messages.map((m, i) => (
            <div key={i} className={`msg ${m.role === "user" ? "msg-me" : "msg-bot"}`}>
              {m.content}
              {m.toolsUsed && m.toolsUsed.length > 0 ? (
                <span className="msg-tools">🔧 {ar ? "أدوات استُخدمت" : "Tools used"}: {m.toolsUsed.join("، ")}</span>
              ) : null}
            </div>
          ))}
          {busy && <div className="msg msg-bot">{ar ? "المساعد يفكر ويجلب البيانات…" : "The assistant is thinking and fetching data…"}</div>}
        </div>
        <div className="chat-form">
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
        <p className="chat-note">
          {ar
            ? "الإجابات استشارية وتتطلب مراجعة بشرية — تُبنى على بيانات المنظومة الحية فقط."
            : "Answers are advisory and require human review — built only on the platform's live data."}
        </p>
      </div>
    </>
  );
}
