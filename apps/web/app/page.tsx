"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { getSessionUser, type SessionUser } from "@/lib/api";
import { useLang } from "@/lib/i18n";
import { SectionHead } from "@/components/ui";

/**
 * Public homepage (v3 composition) — every number appears exactly
 * once: (1) a deep-green hero with a live stat band overlapping its
 * bottom edge, (2) the platform's real service domains with live
 * availability hints, (3) the capacity explorer — every governorate
 * as a labeled occupancy bar, expanding to its facilities and a KPI
 * detail row, (4) about + app bands. The service finder
 * (ابحث واحجز خدمة) lives on the Services page, per the owner's
 * direction — the homepage links to it from the domains section.
 * All data comes from /api/public/* — nothing is invented.
 */
type Kpis = {
  facilitiesActive: number; patientsRegistered: number; encountersToday: number;
  bedOccupancyPct: number; icuOccupancyPct: number; ambulancesAvailable: number;
  openCycleInstances: number; slaBreaches: number;
};
type CapacityRow = {
  governorate: string; facilities: number; totalBeds: number; occupiedBeds: number;
  icuOccupied: number; icuBeds: number; ambulancesAvailable: number;
};
type FacilityRow = {
  id: string; nameAr: string; nameEn: string | null; type: string; governorate: string;
  city: string | null; bedCapacity: number | null; wardBeds: number; inpatients: number;
  occupancyPct: number | null; erWaiting: number;
};
/** The platform's real service domains — the same six domains the
 *  /services page details, each backed by live modules. */
const DOMAINS: { icon: string; ar: string; en: string; arBody: string; enBody: string; hint?: "beds" | "er" | "units" }[] = [
  { icon: "🏥", ar: "إدارة المستشفيات", en: "Hospital Management", arBody: "استقبال وقبول وخروج وتحويلات داخلية، طوارئ وفرز، عيادات خارجية، أجنحة ولوحة أسرّة حية، تمريض وعمليات وعناية مركزة — تشغيل كامل لكل قسم وإدارة.", enBody: "Admissions, discharges and internal transfers, ER and triage, outpatient clinics, live bed boards, nursing, theatres and intensive care — every department fully operated.", hint: "beds" },
  { icon: "🚑", ar: "الطوارئ والأزمات", en: "Emergencies & Crises", arBody: "إرسال الإسعاف ومتابعة السعة لحظة بلحظة وتنسيق الإحالات بين المنشآت والمحافظات وإدارة الحوادث الكبرى بدورات عمل موثقة.", enBody: "Ambulance dispatch, live capacity tracking, referral coordination across facilities and governorates, and major-incident management with audited workflows.", hint: "er" },
  { icon: "🧪", ar: "المعامل وبنوك الدم", en: "Labs & Blood Banks", arBody: "نظام معامل متكامل: استقبال العينات وقوائم العمل ومراقبة الجودة واعتماد النتائج والقيم الحرجة، وبنوك دم بتتبع كامل للوحدات.", enBody: "A full laboratory system: sample reception, worklists, quality control, result validation and critical values, plus fully traced blood-bank units.", hint: "units" },
  { icon: "💊", ar: "الصيدليات والمخازن", en: "Pharmacies & Stores", arBody: "طابور وصفات وصرف بالتشغيلات والأقدم انتهاءً، تنبيهات مخزون وصلاحية، سجل مواد مراقبة، ومشتريات ومخازن بحركات مسببة.", enBody: "Prescription queues and batch/FEFO dispensing, stock and expiry alerts, controlled-drug registers, and reasoned procurement and stores movements." },
  { icon: "🩺", ar: "المراكز والوحدات الصحية", en: "Health Centres & Units", arBody: "ملفات طب الأسرة وصحة الأم والطفل ومتابعة الحمل والتطعيمات وسلسلة التبريد وعيادات الأمراض المزمنة والإحالة لأعلى.", enBody: "Family health files, maternal and child health, pregnancy follow-up, immunisation and cold chain, chronic-disease clinics and upward referral." },
  { icon: "📊", ar: "التحليلات والتنبؤ", en: "Analytics & Forecasting", arBody: "مؤشرات وطنية حية وتنبؤات بالتفشي والسعة ومقاومة المضادات — دعم قرار استشاري يراجعه الإنسان دائمًا قبل أي إجراء.", enBody: "Live national indicators with outbreak, capacity and antimicrobial-resistance forecasting — advisory decision support that always keeps a human in the loop." },
];

export default function HomePage() {
  const { lang, t } = useLang();
  const ar = lang === "ar";
  const [user, setUser] = useState<SessionUser | null>(null);
  const [kpis, setKpis] = useState<Kpis | null>(null);
  const [capacity, setCapacity] = useState<CapacityRow[]>([]);
  const [updatedAt, setUpdatedAt] = useState<string>("");
  const [directory, setDirectory] = useState<FacilityRow[]>([]);
  const [capGov, setCapGov] = useState("");
  const [capFacility, setCapFacility] = useState("");

  const load = useCallback(async () => {
    try {
      const [s, c, f] = await Promise.all([
        fetch("/api/public/summary").then((r) => (r.ok ? r.json() : null)),
        fetch("/api/public/capacity").then((r) => (r.ok ? r.json() : null)),
        fetch("/api/public/facilities").then((r) => (r.ok ? r.json() : null)),
      ]);
      if (f?.facilities) setDirectory(f.facilities);
      if (s?.kpis) setKpis(s.kpis);
      if (c?.capacity) setCapacity(c.capacity);
      setUpdatedAt(new Date().toLocaleTimeString(ar ? "ar-EG" : "en-GB"));
    } catch { /* the static content still renders */ }
  }, [ar]);

  const totalErWaiting = directory.reduce((a, f) => a + (f.erWaiting || 0), 0);
  const totalFreeBeds = directory.reduce((a, f) => a + Math.max(0, (f.wardBeds || 0) - (f.inpatients || 0)), 0);
  const hintFor = (h?: string) => {
    if (!kpis || !h) return null;
    if (h === "beds") return `🛏 ${totalFreeBeds} ${t("bedsFreeWord")}`;
    if (h === "er") return `🚑 ${kpis.ambulancesAvailable} ${t("availableNow")} · ${totalErWaiting} ${t("erWaitingNow")}`;
    if (h === "units") return `🏥 ${kpis.facilitiesActive} ${t("unitsWord")}`;
    return null;
  };

  return (
    <>
      {/* 1 — hero + live stat band */}
      <section className="hero">
        <span className="eyebrow">{t("appName")}</span>
        <h1>{t("sysDesc")}</h1>
        <p className="hero-desc">{t("heroText")}</p>
        <div className="hero-cta">
          {user ? (
            <Link className="btn btn-gold" href="/dashboard">{t("dashboard")}</Link>
          ) : (
            <>
              <Link className="btn btn-gold" href="/register">{ar ? "إنشاء حساب مجاني" : "Create a free account"}</Link>
              <Link className="btn btn-outline-w" href="/login">{t("login")}</Link>
            </>
          )}
          <Link className="btn btn-outline-w" href="/download-app">⬇ {t("appDownload")}</Link>
        </div>
        <span className="live-line">
          <span className="live-dot" /> {t("liveNow")}
          {updatedAt ? <> · {t("updatedAt")}: <span dir="ltr">{updatedAt}</span></> : null}
        </span>
        <div className="hero-stats">
          <div className="hero-stat"><div className="hs-v">{kpis ? kpis.facilitiesActive : "—"}</div><div className="hs-l">{t("stFacilities")}</div></div>
          <div className="hero-stat"><div className="hs-v">{kpis ? kpis.patientsRegistered : "—"}</div><div className="hs-l">{t("stPatients")}</div></div>
          <div className="hero-stat"><div className="hs-v">{kpis ? `${kpis.bedOccupancyPct}%` : "—"}</div><div className="hs-l">{t("stBeds")}</div></div>
          <div className="hero-stat"><div className="hs-v">{kpis ? `${kpis.icuOccupancyPct}%` : "—"}</div><div className="hs-l">{ar ? "إشغال العناية المركزة" : "ICU occupancy"}</div></div>
          <div className="hero-stat"><div className="hs-v">{kpis ? kpis.ambulancesAvailable : "—"}</div><div className="hs-l">{t("stAmb")}</div></div>
          <div className="hero-stat"><div className="hs-v">{kpis ? kpis.encountersToday : "—"}</div><div className="hs-l">{t("stToday")}</div></div>
        </div>
      </section>
      <div className="hero-spacer" />

      {/* 2 — the platform's real service domains */}
      <SectionHead
        eyebrow={ar ? "خدمات المنظومة" : "Platform services"}
        title={t("servicesTitle")}
        sub={t("servicesSub")}
      />
      <div className="svc-grid">
        {DOMAINS.map((f) => {
          const hint = hintFor(f.hint);
          return (
            <div key={f.en} className="svc-card">
              <div className="svc-ico" aria-hidden>{f.icon}</div>
              <h3>{ar ? f.ar : f.en}</h3>
              <p>{ar ? f.arBody : f.enBody}</p>
              {hint ? <span className="live-hint">{hint}</span> : null}
            </div>
          );
        })}
      </div>
      <div className="row mt">
        <Link className="btn" href="/services#book">{ar ? "ابحث واحجز خدمة" : "Find & book a service"}</Link>
        <Link className="btn btn-soft" href="/services">{ar ? "كل الخدمات" : "All services"}</Link>
      </div>

      {/* 3 — capacity explorer */}
      <SectionHead
        eyebrow={ar ? "بيانات حية" : "Live data"}
        title={t("capTitle")}
        sub={ar ? "إشغال الأسرّة لحظيًا في كل محافظة — اضغط على أي محافظة لعرض منشآتها، وعلى أي منشأة لعرض تفاصيل سعتها." : "Live bed occupancy in every governorate — click a governorate to see its facilities, and a facility for its capacity detail."}
      />
      <div className="card">
        <div className="cap-list">
          {[...capacity]
            .sort((a, b) => (b.totalBeds > 0 ? b.occupiedBeds / b.totalBeds : 0) - (a.totalBeds > 0 ? a.occupiedBeds / a.totalBeds : 0))
            .map((g) => {
              const pctv = g.totalBeds > 0 ? Math.round((g.occupiedBeds / g.totalBeds) * 1000) / 10 : 0;
              const selected = capGov === g.governorate;
              return (
                <div key={g.governorate}>
                  <button
                    type="button"
                    className={`cap-row${selected ? " open" : ""}`}
                    onClick={() => { setCapGov(selected ? "" : g.governorate); setCapFacility(""); }}
                    aria-expanded={selected}
                  >
                    <span className="cap-name">{g.governorate}</span>
                    <span className="cap-track"><span className={`cap-fill${pctv > 85 ? " hot" : pctv > 70 ? " warm" : ""}`} style={{ width: `${Math.min(100, pctv)}%` }} /></span>
                    <span className="cap-val">{g.occupiedBeds}/{g.totalBeds} · {pctv}%</span>
                    <span className="cap-meta">{ar ? "عناية" : "ICU"} {g.icuOccupied}/{g.icuBeds} · {ar ? "إسعاف" : "Amb."} {g.ambulancesAvailable} · {g.facilities} {ar ? "منشأة" : "facilities"}</span>
                  </button>
                  {selected ? (
                    <div className="cap-sub">
                      {directory.filter((f) => f.governorate === g.governorate).map((f) => {
                        const beds = f.wardBeds > 0 ? f.wardBeds : (f.bedCapacity ?? 0);
                        const fsel = capFacility === f.id;
                        return (
                          <div key={f.id}>
                            <button
                              type="button"
                              className={`cap-row fac${fsel ? " open" : ""}`}
                              onClick={() => setCapFacility(fsel ? "" : f.id)}
                              aria-expanded={fsel}
                            >
                              <span className="cap-name">{ar ? f.nameAr : (f.nameEn || f.nameAr)}</span>
                              <span className="cap-track"><span className={`cap-fill${(f.occupancyPct ?? 0) > 85 ? " hot" : (f.occupancyPct ?? 0) > 70 ? " warm" : ""}`} style={{ width: `${Math.min(100, f.occupancyPct ?? 0)}%` }} /></span>
                              <span className="cap-val">{f.inpatients}/{beds}{f.occupancyPct != null ? ` · ${f.occupancyPct}%` : ""}</span>
                              <span className="cap-meta">{ar ? "منتظرون بالطوارئ" : "ER waiting"}: {f.erWaiting}</span>
                            </button>
                            {fsel ? (
                              <div className="cap-detail">
                                <div className="kpi-grid flush">
                                  <div className="kpi"><div className="kpi-label">{ar ? "إجمالي الأسرّة" : "Total beds"}</div><div className="kpi-value">{beds}</div></div>
                                  <div className="kpi"><div className="kpi-label">{ar ? "مرضى مقيمون" : "Inpatients"}</div><div className="kpi-value">{f.inpatients}</div></div>
                                  <div className="kpi"><div className="kpi-label">{ar ? "نسبة الإشغال" : "Occupancy"}</div><div className="kpi-value">{f.occupancyPct != null ? `${f.occupancyPct}%` : "—"}</div></div>
                                  <div className="kpi"><div className="kpi-label">{ar ? "منتظرون بالطوارئ" : "Waiting in ER"}</div><div className="kpi-value">{f.erWaiting}</div></div>
                                </div>
                              </div>
                            ) : null}
                          </div>
                        );
                      })}
                      {directory.filter((f) => f.governorate === g.governorate).length === 0
                        ? <p className="muted">{ar ? "لا منشآت مسجلة في هذه المحافظة بعد." : "No facilities registered in this governorate yet."}</p>
                        : null}
                    </div>
                  ) : null}
                </div>
              );
            })}
          {capacity.length === 0 && <p className="muted">—</p>}
        </div>
      </div>

      {/* 4 — about strip + app band */}
      <div className="about-strip mt">
        <div className="grow">
          <span className="eyebrow">{ar ? "من نحن" : "About"}</span>
          <h2 className="strip-title">{t("appName")}</h2>
          <p className="muted mt">{t("footerAbout")}</p>
        </div>
        <Link className="btn btn-soft" href="/about">{ar ? "المزيد عن المنظومة" : "More about the system"}</Link>
      </div>
      <div className="app-band mt">
        <div className="grow">
          <h2>{ar ? "تطبيق خطاب للأندرويد" : "Khattab Android App"}</h2>
          <p>{ar ? "للفرق الميدانية وطاقم المنشآت — نفس حسابك في المنظومة، ويعمل دون اتصال مع مزامنة آمنة." : "For field teams and facility staff — your same system account, with offline work and safe sync."}</p>
        </div>
        <Link className="btn btn-gold" href="/download-app">⬇ {t("appDownload")}</Link>
      </div>
    </>
  );
}
