"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { api, getToken } from "@/lib/api";
import { useLang } from "@/lib/i18n";
import { Card } from "@/components/ui";

type Capability = { labelAr: string; labelEn: string; path: string };
type Goal = { seq: number; titleAr: string };
type Indicator = { key: string; labelAr: string; labelEn: string; unit: string; value: number | null };
type Priority = { code: string; seq: number; titleAr: string; titleEn: string; goals: Goal[]; capabilities: Capability[]; indicators?: Indicator[] };
type Strategy = { vision: string; mission: string; principles: string[]; priorities: Priority[]; source: string };

export default function StrategyPage() {
  const { lang } = useLang();
  const ar = lang === "ar";
  const [data, setData] = useState<Strategy | null>(null);
  const [board, setBoard] = useState<Record<string, Indicator[]> | null>(null);
  const [err, setErr] = useState("");

  useEffect(() => {
    api.getJson<Strategy>("/api/public/strategy").then(setData).catch((e) => setErr(e instanceof Error ? e.message : String(e)));
    if (getToken()) {
      api.getJson<{ priorities: Priority[] }>("/api/strategy/board").then((b) => {
        const map: Record<string, Indicator[]> = {};
        for (const p of b.priorities) map[p.code] = p.indicators ?? [];
        setBoard(map);
      }).catch(() => setBoard(null));
    }
  }, []);

  return (
    <div>
      <section className="page-hero">
        <p className="eyebrow">{ar ? "جمهورية مصر العربية — وزارة الصحة والسكان" : "Arab Republic of Egypt — Ministry of Health & Population"}</p>
        <h1>{ar ? "الاستراتيجية الوطنية للصحة 2024–2030" : "National Health Strategy 2024–2030"}</h1>
        <p className="hero-sub">{ar
          ? "تعمل منظومة خطاب داخل إطار الاستراتيجية الوطنية للصحة: أولوياتها السبع وأهدافها الأربعة والأربعون هي إطار التشغيل والقياس هنا — كل قدرة في المنظومة تخدم أولوية، وكل أولوية تُقاس بمؤشرات حية من بيانات التشغيل نفسها."
          : "Khattab operates inside the National Health Strategy frame: its seven priorities and forty-four goals structure operations and measurement here — every platform capability serves a priority, and every priority is measured with live indicators from operational data itself."}</p>
      </section>

      {err ? <p className="muted">{err}</p> : null}
      {!data ? <p className="muted">{ar ? "جارٍ التحميل…" : "Loading…"}</p> : (
        <>
          <div className="grid-2">
            <Card><h3 className="card-h">{ar ? "الرؤية" : "Vision"}</h3><p>{data.vision}</p></Card>
            <Card><h3 className="card-h">{ar ? "الرسالة" : "Mission"}</h3><p>{data.mission}</p></Card>
          </div>
          <Card><h3 className="card-h">{ar ? "القيم الأساسية والمبادئ التوجيهية" : "Core values & guiding principles"}</h3>
            <div className="row">{data.principles.map((p) => <span key={p} className="badge">{p}</span>)}</div>
          </Card>

          {data.priorities.map((p) => (
            <Card key={p.code}>
              <p className="eyebrow">{ar ? `الأولوية ${p.seq} من 7` : `Priority ${p.seq} of 7`}</p>
              <h3 className="card-h">{ar ? p.titleAr : p.titleEn}</h3>
              <div className="grid-2">
                <div>
                  <strong>{ar ? "الأهداف" : "Goals"}</strong>
                  <ol className="strat-goals">
                    {p.goals.map((g) => <li key={g.seq}>{g.titleAr}</li>)}
                  </ol>
                </div>
                <div>
                  <strong>{ar ? "كيف تخدمها المنظومة" : "How the platform serves it"}</strong>
                  <div className="row">
                    {p.capabilities.map((c) => (
                      <Link key={c.path} href={c.path} className="badge link-badge">{ar ? c.labelAr : c.labelEn}</Link>
                    ))}
                  </div>
                  {board && board[p.code] ? (
                    <>
                      <strong>{ar ? "مؤشرات حية من تشغيل المنظومة" : "Live indicators from platform operations"}</strong>
                      <div className="strat-inds">
                        {board[p.code].map((i) => (
                          <div key={i.key} className="strat-ind">
                            <span className="strat-ind-v">{i.value == null ? "—" : `${Number(i.value).toLocaleString(ar ? "ar-EG" : "en-US")}${i.unit === "%" ? "٪" : i.unit === "/27" ? "/27" : ""}`}</span>
                            <span className="muted">{ar ? i.labelAr : i.labelEn}</span>
                          </div>
                        ))}
                      </div>
                    </>
                  ) : null}
                </div>
              </div>
            </Card>
          ))}
          <p className="muted">{ar ? `المصدر: ${data.source}. المؤشرات المعروضة للمسجلين هي مؤشرات تشغيلية حية من بيانات المنظومة؛ المستهدفات الوطنية الرسمية كما وردت في الخطة التنفيذية للاستراتيجية.` : `Source: ${data.source}. Indicators shown to signed-in users are live operational measures from platform data; official national targets are as published in the strategy's executive plan.`}</p>
        </>
      )}
    </div>
  );
}
