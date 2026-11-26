"use client";

import { useEffect, useRef, useState } from "react";
import { getToken } from "@/lib/api";
import { useLang } from "@/lib/i18n";

interface Msg { role: "user" | "assistant"; content: string }

/** The Khattab assistant, floating on every page — the public
 *  homepage included. Anonymous visitors reach the public agent
 *  (public tools only: services, facilities, capacity); signed-in
 *  users reach the full agent under their own permissions. The
 *  answering provider is an internal detail and is never shown. */
export default function ChatWidget() {
  const { lang } = useLang();
  const ar = lang === "ar";
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState<Msg[]>([]);
  const [input, setInput] = useState("");
  const [busy, setBusy] = useState(false);
  const boxRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    boxRef.current?.scrollTo({ top: boxRef.current.scrollHeight });
  }, [messages, open]);

  async function send(text: string) {
    const content = text.trim();
    if (!content || busy) return;
    const next: Msg[] = [...messages, { role: "user", content }];
    setMessages(next);
    setInput("");
    setBusy(true);
    try {
      const token = getToken();
      const res = await fetch(token ? "/api/analytics/ai/chat" : "/api/public/chat", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: JSON.stringify({ messages: next.map((m) => ({ role: m.role, content: m.content })) }),
      });
      const data = await res.json().catch(() => ({}));
      if (!res.ok) throw new Error(data.message || `HTTP ${res.status}`);
      setMessages([...next, { role: "assistant", content: data.reply || "…" }]);
    } catch {
      setMessages([...next, { role: "assistant", content: ar ? "المساعد غير متاح الآن — حاول بعد قليل." : "The assistant is unavailable right now — try again shortly." }]);
    } finally { setBusy(false); }
  }

  return (
    <>
      <button type="button" className="chat-fab" aria-label={ar ? "مساعد خطاب الذكي" : "Khattab assistant"} onClick={() => setOpen(!open)}>
        {open ? "✕" : "💬"}
      </button>
      {open && (
        <div className="chat-panel">
          <div className="chat-head">
            <span aria-hidden>🤖</span> {ar ? "مساعد خطاب الذكي" : "Khattab Assistant"}
          </div>
          <div className="chat-log" ref={boxRef}>
            {messages.length === 0 && (
              <div>
                <p className="muted">{ar ? "اسأل عن الخدمات والمنشآت والسعة المتاحة، أو جرّب:" : "Ask about services, facilities and live capacity, or try:"}</p>
                <div className="chat-sugg mt">
                  {(ar
                    ? ["ما المنشآت المتاحة في القاهرة؟", "ما السعة السريرية الحالية؟", "كيف أحجز موعد عيادة؟"]
                    : ["Which facilities are in Cairo?", "What is the current bed capacity?", "How do I book a clinic appointment?"]
                  ).map((q) => (
                    <button key={q} type="button" className="btn btn-soft btn-sm" onClick={() => send(q)}>{q}</button>
                  ))}
                </div>
              </div>
            )}
            {messages.map((m, i) => (
              <div key={i} className={`msg ${m.role === "user" ? "msg-me" : "msg-bot"}`}>{m.content}</div>
            ))}
            {busy && <div className="msg msg-bot muted">{ar ? "جارٍ التفكير…" : "Thinking…"}</div>}
          </div>
          <div className="chat-form">
            <input
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={(e) => { if (e.key === "Enter") send(input); }}
              placeholder={ar ? "اكتب سؤالك…" : "Type your question…"}
            />
            <button type="button" className="btn" disabled={busy || !input.trim()} onClick={() => send(input)}>
              {ar ? "إرسال" : "Send"}
            </button>
          </div>
          <p className="chat-note">
            {ar ? "إجابات استشارية من بيانات المنظومة الحية — القرار السريري للطبيب دائمًا." : "Advisory answers from live system data — clinical decisions always rest with the physician."}
          </p>
        </div>
      )}
    </>
  );
}
