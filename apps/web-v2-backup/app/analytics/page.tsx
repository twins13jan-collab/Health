"use client";
import { useEffect, useState } from "react";
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
    <div className="content-narrow">
      <h1>التحليلات والتنبؤات الاستشارية</h1>
      <p>تنبؤات خدمة الذكاء والتحليلات (Python/FastAPI) عبر منصة التشغيل — <strong>استشارية فقط</strong> وتتطلب مراجعة بشرية قبل أي قرار.</p>
      <button onClick={run} disabled={busy} className="btn l-mb">{busy ? "جارٍ الحساب..." : "إعادة الحساب"}</button>
      {error && <p className="form-msg err">{error}</p>}
      {capacity && (
        <section className="card">
          <h2>تنبؤ سعة العناية المركزة</h2>
          <p>الإشغال الحالي: <strong>{capacity.current_occupancy_pct}%</strong></p>
          <p>أيام حتى الامتلاء: <strong>{capacity.days_to_full ?? "—"}</strong></p>
          <p>مستوى الخطر: <strong>{capacity.risk_level_ar}</strong></p>
          <ul>{(capacity.factors_ar ?? []).map((f, i) => <li key={i}>{f}</li>)}</ul>
        </section>
      )}
      {outbreak && (
        <section className="card">
          <h2>تنبؤ تفشي — القاهرة (أعراض شبيهة بالإنفلونزا)</h2>
          <p>اتجاه المنحنى: <strong>{outbreak.trend_direction}</strong></p>
          <p>معدل النمو: <strong>{outbreak.growth_rate_pct ?? "—"}%</strong></p>
          <p>مستوى الخطر: <strong>{outbreak.risk_level_ar}</strong></p>
          <ul>{(outbreak.factors_ar ?? []).map((f, i) => <li key={i}>{f}</li>)}</ul>
        </section>
      )}
    </div>
  );
}
