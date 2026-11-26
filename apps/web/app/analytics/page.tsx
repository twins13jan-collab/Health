"use client";
import { useEffect, useState } from "react";
import { PageHead } from "@/components/ui";
import { API_BASE_URL, getToken } from "@/lib/api";

type Capacity = { current_occupancy_pct?: number; days_to_full?: number | null; risk_level_ar?: string; factors_ar?: string[]; advisory_only?: boolean };
type Outbreak = { trend_direction?: string; risk_level_ar?: string; growth_rate_pct?: number | null; factors_ar?: string[]; advisory_only?: boolean };

async function post(path: string, body: unknown) {
  const token = getToken();
  const res = await fetch(`${API_BASE_URL}${path}`, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    body: JSON.stringify(body),
    signal: AbortSignal.timeout(15000),
  });
  if (!res.ok) throw new Error(String(res.status));
  return res.json();
}

export default function AnalyticsPage() {
  const [capacity, setCapacity] = useState<Capacity | null>(null);
  const [outbreak, setOutbreak] = useState<Outbreak | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function run() {
    setBusy(true); setError(null);
    try {
      const today = new Date();
      const day = (offset: number) => { const d = new Date(today); d.setDate(d.getDate() - offset); return d.toISOString().slice(0, 10); };
      const occSeries = [88, 92, 95, 99, 103, 106, 110].map((o, i) => ({ date: day(6 - i), occupied: o }));
      const caseSeries = [12, 14, 13, 18, 22, 25, 31, 36, 42, 51, 58, 66, 74, 83].map((c, i) => ({ date: day(13 - i), cases: c }));
      const [cap, out] = await Promise.all([
        post("/api/analytics/forecast/capacity", { facility_id: "current", unit: "ICU", total_beds: 120, series: occSeries }),
        post("/api/analytics/forecast/outbreak", { governorate: "القاهرة", disease: "influenza-like illness", series: caseSeries, population: 10000000 }),
      ]);
      setCapacity(cap); setOutbreak(out);
    } catch {
      setError("خدمة التحليلات غير متاحة الآن أو لم تسجل الدخول — لا تُعرض أرقام بديلة.");
    } finally { setBusy(false); }
  }
  useEffect(() => { run(); }, []);

  return (
    <>
      <PageHead
        eyebrow="التحليلات"
        title="التحليلات والتنبؤات الاستشارية"
        sub={<>تنبؤات خدمة الذكاء والتحليلات (Python/FastAPI) عبر منصة التشغيل — <strong>استشارية فقط</strong> وتتطلب مراجعة بشرية قبل أي قرار.</>}
        actions={<button onClick={run} disabled={busy} className="btn">{busy ? "جارٍ الحساب..." : "إعادة الحساب"}</button>}
      />
      {error && <div className="alert-error">{error}</div>}
      <div className="grid-2">
        {capacity && (
          <section className="card">
            <h2>تنبؤ سعة العناية المركزة</h2>
            <div className="kpi-grid flush">
              <div className="kpi"><div className="kpi-label">الإشغال الحالي</div><div className="kpi-value">{capacity.current_occupancy_pct}%</div></div>
              <div className="kpi"><div className="kpi-label">أيام حتى الامتلاء</div><div className="kpi-value">{capacity.days_to_full ?? "—"}</div></div>
              <div className="kpi"><div className="kpi-label">مستوى الخطر</div><div className="kpi-value">{capacity.risk_level_ar}</div></div>
            </div>
            <ul className="clean mt">{(capacity.factors_ar ?? []).map((f, i) => <li key={i}>{f}</li>)}</ul>
          </section>
        )}
        {outbreak && (
          <section className="card">
            <h2>تنبؤ تفشي — القاهرة (أعراض شبيهة بالإنفلونزا)</h2>
            <div className="kpi-grid flush">
              <div className="kpi"><div className="kpi-label">اتجاه المنحنى</div><div className="kpi-value">{outbreak.trend_direction}</div></div>
              <div className="kpi"><div className="kpi-label">معدل النمو</div><div className="kpi-value">{outbreak.growth_rate_pct ?? "—"}%</div></div>
              <div className="kpi"><div className="kpi-label">مستوى الخطر</div><div className="kpi-value">{outbreak.risk_level_ar}</div></div>
            </div>
            <ul className="clean mt">{(outbreak.factors_ar ?? []).map((f, i) => <li key={i}>{f}</li>)}</ul>
          </section>
        )}
      </div>
    </>
  );
}
