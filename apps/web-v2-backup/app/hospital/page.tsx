"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";

interface WardRow {
  id: string; nameAr: string; wardType: string; totalBeds: number;
  occupied: number; free: number; occupancyPct: number | null;
}
interface AdmissionRow { id: string; dischargedAt?: string; status: string }

const GROUPS: { ar: string; en: string; links: { href: string; ar: string; en: string; icon: string }[] }[] = [
  {
    ar: "الأقسام السريرية", en: "Clinical Departments",
    links: [
      { href: "/adt", ar: "القبول والإقامة والتحويلات", en: "Admissions & ADT", icon: "🛏" },
      { href: "/encounters", ar: "الاستقبال والطوارئ والفرز", en: "Reception, ER & Triage", icon: "✚" },
      { href: "/appointments", ar: "العيادات الخارجية والجدولة", en: "Outpatient Clinics", icon: "🗓" },
      { href: "/surgery", ar: "العمليات والتخدير", en: "Surgery & Anaesthesia", icon: "🔪" },
      { href: "/icu", ar: "العناية المركزة", en: "Intensive Care", icon: "🫀" },
      { href: "/maternity", ar: "الأمومة والولادة", en: "Maternity", icon: "🤰" },
      { href: "/neonatal", ar: "حديثو الولادة والحضانات", en: "Neonatal & NICU", icon: "👶" },
      { href: "/referrals", ar: "الإحالات", en: "Referrals", icon: "↗" },
    ],
  },
  {
    ar: "الدعم السريري", en: "Clinical Support",
    links: [
      { href: "/labs", ar: "المعامل", en: "Laboratories", icon: "🧪" },
      { href: "/radiology", ar: "الأشعة", en: "Radiology", icon: "🩻" },
      { href: "/pharmacy", ar: "الصيدلية", en: "Pharmacy", icon: "💊" },
      { href: "/blood-bank", ar: "بنك الدم", en: "Blood Bank", icon: "🩸" },
      { href: "/pathology", ar: "الباثولوجي", en: "Pathology", icon: "🔬" },
      { href: "/endoscopy", ar: "المناظير", en: "Endoscopy", icon: "🎥" },
      { href: "/longitudinal-record", ar: "السجل الطبي الموحد", en: "Longitudinal Record", icon: "🗂" },
    ],
  },
  {
    ar: "الإدارة والتشغيل", en: "Administration & Operations",
    links: [
      { href: "/facilities", ar: "إدارة المنشأة", en: "Facility Administration", icon: "🏢" },
      { href: "/workforce", ar: "الموارد البشرية والجدولة", en: "Workforce & Rostering", icon: "👥" },
      { href: "/billing", ar: "الفوترة والمالية", en: "Billing & Finance", icon: "🧾" },
      { href: "/inventory", ar: "المخازن", en: "Stores", icon: "📦" },
      { href: "/purchasing", ar: "المشتريات", en: "Procurement", icon: "🛒" },
      { href: "/medical-assets", ar: "الأجهزة والصيانة الطبية", en: "Medical Assets & Maintenance", icon: "🛠" },
      { href: "/quality", ar: "الجودة وسلامة المرضى", en: "Quality & Patient Safety", icon: "✅" },
      { href: "/complaints", ar: "الشكاوى وحقوق المرضى", en: "Complaints & Patient Rights", icon: "💬" },
    ],
  },
];

/** Hospital command: the director's live picture of THIS facility —
 *  census, beds, today's movement, and every department one tap away. */
export default function HospitalPage() {
  const { lang } = useLang();
  const ar = lang === "ar";
  const [wards, setWards] = useState<WardRow[]>([]);
  const [census, setCensus] = useState(0);
  const [dischargedToday, setDischargedToday] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      api.moduleList<WardRow>("/api/adt/board", "wards"),
      api.moduleList<AdmissionRow>("/api/adt/admissions?status=admitted", "admissions"),
      api.moduleList<AdmissionRow>("/api/adt/admissions?status=discharged", "admissions"),
    ])
      .then(([board, adm, dis]) => {
        setWards(board.data);
        setCensus(adm.data.length);
        const today = new Date().toISOString().slice(0, 10);
        setDischargedToday(dis.data.filter((d) => d.dischargedAt?.slice(0, 10) === today).length);
      })
      .catch((e) => setError(e instanceof Error ? e.message : String(e)))
      .finally(() => setLoading(false));
  }, []);

  const totalBeds = wards.reduce((s, w) => s + w.totalBeds, 0);
  const freeBeds = wards.reduce((s, w) => s + w.free, 0);
  const occupancy = totalBeds ? Math.round((1000 * (totalBeds - freeBeds)) / totalBeds) / 10 : null;

  return (
    <>
      <div className="page-head">
        <h1>{ar ? "قيادة المستشفى" : "Hospital Command"}</h1>
        <p>{ar ? "الصورة الحية لمنشأتك: التعداد والأسرّة وحركة اليوم، وكل الأقسام والإدارات في متناولك" : "Your facility's live picture: census, beds, today's movement, and every department within reach"}</p>
      </div>
      {error ? <div className="error-banner">{error}</div> : null}

      {loading ? (
        <div className="kpi-grid">{Array.from({ length: 4 }).map((_, i) => <div key={i} className="skeleton skel-104" />)}</div>
      ) : (
        <div className="kpi-grid">
          <div className="kpi-card"><div className="kpi-label">{ar ? "مرضى مقيمون" : "Inpatients"}</div><div className="kpi-value">{census}</div></div>
          <div className="kpi-card kpi-ok"><div className="kpi-label">{ar ? "أسرّة شاغرة" : "Free beds"}</div><div className="kpi-value">{freeBeds}</div><div className="kpi-hint">{ar ? `من ${totalBeds} سريرًا` : `of ${totalBeds} beds`}</div></div>
          <div className="kpi-card"><div className="kpi-label">{ar ? "إشغال المنشأة" : "Occupancy"}</div><div className="kpi-value">{occupancy !== null ? `${occupancy}%` : "—"}</div></div>
          <div className="kpi-card"><div className="kpi-label">{ar ? "خروج اليوم" : "Discharged today"}</div><div className="kpi-value">{dischargedToday}</div></div>
        </div>
      )}

      <section className="card">
        <h2>{ar ? "إشغال الأجنحة" : "Ward occupancy"}</h2>
        {wards.length === 0 && !loading ? (
          <div className="empty">{ar ? "لا توجد أجنحة معرفة بعد — عرّف أجنحة منشأتك من شاشة القبول والإقامة" : "No wards defined yet — define your wards from the ADT screen"}</div>
        ) : (
          <div className="d-grid-10">
            {wards.map((w) => (
              <div key={w.id} className="ward-row">
                <div className="ward-name">{w.nameAr} <span className="badge badge-muted">{w.wardType}</span></div>
                <div className={`progress ${w.occupancyPct !== null && w.occupancyPct > 85 ? "hot" : w.occupancyPct !== null && w.occupancyPct > 70 ? "warm" : ""} grow`}>
                  <div style={{ width: `${w.occupancyPct ?? 0}%` }} />
                </div>
                <div className="text-md">{w.occupied}/{w.totalBeds} · {w.occupancyPct ?? 0}%</div>
              </div>
            ))}
          </div>
        )}
        <p className="l-mt">
          <Link className="btn btn-secondary" href="/adt">{ar ? "فتح لوحة الأسرّة والقبول" : "Open the bed board & ADT"}</Link>
        </p>
      </section>

      {GROUPS.map((g) => (
        <section className="card" key={g.en}>
          <h2>{ar ? g.ar : g.en}</h2>
          <div className="quick-grid">
            {g.links.map((l) => (
              <Link key={l.href} href={l.href} className="quick-link"><span aria-hidden>{l.icon}</span> {ar ? l.ar : l.en}</Link>
            ))}
          </div>
        </section>
      ))}
    </>
  );
}
