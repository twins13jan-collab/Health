"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { getSessionUser, type SessionUser } from "@/lib/api";
import { useLang } from "@/lib/i18n";
import { PublicHeader, PublicFooter } from "@/components/PublicChrome";

/**
 * Public homepage of the Khattab system — restructured so every
 * fact appears exactly once: one live stats strip, one service
 * finder (facilities by governorate with their bookable services),
 * and one capacity chart. Booking and service requests start here
 * and continue in the patient portal (after a free registration).
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
type ServiceFacility = {
  id: string; nameAr: string; nameEn: string | null; type: string; level: string;
  governorate: string; city: string | null; bedCapacity: number | null; services: string[];
};

const TYPE_KEYS: Record<string, string> = {
  hospital: "typeHospital", health_unit: "typeHealthUnit", family_center: "typeFamilyCenter",
  lab: "typeLab", laboratory: "typeLab", pharmacy: "typePharmacy", blood_bank: "typeBloodBank", center: "typeCenter",
  dialysis_center: "typeDialysisCenter", oncology_center: "typeOncologyCenter", clinic: "typeClinic",
};
const SERVICE_LABELS: Record<string, { ar: string; en: string }> = {
  clinics: { ar: "عيادات خارجية", en: "Outpatient clinics" },
  emergency: { ar: "طوارئ", en: "Emergency" },
  lab: { ar: "معامل تحاليل", en: "Laboratory" },
  pharmacy: { ar: "صيدلية", en: "Pharmacy" },
  radiology: { ar: "أشعة", en: "Radiology" },
  surgery: { ar: "جراحات", en: "Surgery" },
  blood: { ar: "بنك دم", en: "Blood bank" },
  dialysis: { ar: "غسيل كلوي", en: "Dialysis" },
  oncology: { ar: "أورام", en: "Oncology" },
};
const TYPE_FILTERS = ["", "hospital", "health_unit", "family_center", "lab", "pharmacy", "blood_bank", "dialysis_center", "oncology_center"];

export default function HomePage() {
  const { lang, t } = useLang();
  const ar = lang === "ar";
  const [user, setUser] = useState<SessionUser | null>(null);
  const [kpis, setKpis] = useState<Kpis | null>(null);
  const [capacity, setCapacity] = useState<CapacityRow[]>([]);
  const [updatedAt, setUpdatedAt] = useState<string>("");
  const [governorates, setGovernorates] = useState<{ governorate: string; facilities: number }[]>([]);
  const [gov, setGov] = useState("");
  const [typeFilter, setTypeFilter] = useState("");
  const [finder, setFinder] = useState<ServiceFacility[]>([]);
  const [directory, setDirectory] = useState<FacilityRow[]>([]);
  const [capGov, setCapGov] = useState("");
  const [capFacility, setCapFacility] = useState("");
  const [finderLoading, setFinderLoading] = useState(false);

  const load = useCallback(async () => {
    try {
      const [s, c, g, f] = await Promise.all([
        fetch("/api/public/summary").then((r) => (r.ok ? r.json() : null)),
        fetch("/api/public/capacity").then((r) => (r.ok ? r.json() : null)),
        fetch("/api/public/governorates").then((r) => (r.ok ? r.json() : null)),
        fetch("/api/public/facilities").then((r) => (r.ok ? r.json() : null)),
      ]);
      if (f?.facilities) setDirectory(f.facilities);
      if (s?.kpis) setKpis(s.kpis);
      if (c?.capacity) setCapacity(c.capacity);
      if (g?.governorates) setGovernorates(g.governorates);
      setUpdatedAt(new Date().toLocaleTimeString(ar ? "ar-EG" : "en-GB"));
    } catch { /* the static content still renders */ }
  }, [ar]);

  const loadFinder = useCallback(async () => {
    setFinderLoading(true);
    try {
      const qs = new URLSearchParams();
      if (gov) qs.set("governorate", gov);
      if (typeFilter) qs.set("type", typeFilter);
      const r = await fetch(`/api/public/services?${qs.toString()}`);
      const data = r.ok ? await r.json() : null;
      setFinder(data?.facilities ?? []);
    } catch { setFinder([]); } finally { setFinderLoading(false); }
  }, [gov, typeFilter]);

  useEffect(() => { setUser(getSessionUser()); }, []);
  useEffect(() => {
    load();
    const id = window.setInterval(load, 60000);
    return () => window.clearInterval(id);
  }, [load]);
  useEffect(() => { loadFinder(); }, [loadFinder]);
  useEffect(() => {
    if (!capGov && governorates.length > 0) setCapGov(governorates[0].governorate);
  }, [governorates, capGov]);

  const typeLabel = (ty: string) => (TYPE_KEYS[ty] ? t(TYPE_KEYS[ty] as never) : ty);
  const portalHref = user ? "/my" : "/register";

  return (
    <>
      <PublicHeader />
      <div className="landing">

      <section className="landing-hero">
        <h1>{t("sysDesc")}</h1>
        <p>{t("heroText")}</p>
        <div className="landing-cta">
          {user ? (
            <Link className="btn landing-btn" href="/dashboard">{t("dashboard")}</Link>
          ) : (
            <>
              <Link className="btn landing-btn" href="/login">{t("login")}</Link>
              <Link className="btn landing-btn" href="/register" style={{ background: "var(--gold-bright)", color: "#20301f" }}>{ar ? "إنشاء حساب مجاني" : "Create a free account"}</Link>
            </>
          )}
          <Link className="btn btn-secondary landing-btn" href="/download-app">⬇ {t("appDownload")}</Link>
        </div>
        <div>
          <span className="live-line">
            <span className="live-dot" /> {t("liveNow")}
            {updatedAt ? <> · {t("updatedAt")}: <span dir="ltr">{updatedAt}</span></> : null}
          </span>
        </div>
        <div className="landing-stats">
          <div className="landing-stat"><div className="landing-stat-value">{kpis ? kpis.facilitiesActive : "—"}</div><div className="landing-stat-label">{t("stFacilities")}</div></div>
          <div className="landing-stat"><div className="landing-stat-value">{kpis ? kpis.patientsRegistered : "—"}</div><div className="landing-stat-label">{t("stPatients")}</div></div>
          <div className="landing-stat"><div className="landing-stat-value">{kpis ? `${kpis.bedOccupancyPct}%` : "—"}</div><div className="landing-stat-label">{t("stBeds")}</div></div>
          <div className="landing-stat"><div className="landing-stat-value">{kpis ? `${kpis.icuOccupancyPct}%` : "—"}</div><div className="landing-stat-label">{ar ? "إشغال العناية المركزة" : "ICU occupancy"}</div></div>
          <div className="landing-stat"><div className="landing-stat-value">{kpis ? kpis.ambulancesAvailable : "—"}</div><div className="landing-stat-label">{t("stAmb")}</div></div>
          <div className="landing-stat"><div className="landing-stat-value">{kpis ? kpis.encountersToday : "—"}</div><div className="landing-stat-label">{t("stToday")}</div></div>
        </div>
      </section>

      <h2 className="section-title">{ar ? "ابحث واحجز خدمة" : "Find & book a service"}</h2>
      <p className="section-sub">{ar
        ? "اختر المحافظة ونوع المنشأة لعرض الخدمات المتاحة لحظيًا — الحجز وطلبات الخدمة من حسابك الصحي."
        : "Pick a governorate and facility type to see services available right now — booking and service requests live in your health account."}</p>
      <div className="card">
        <div style={{ display: "flex", gap: 8, flexWrap: "wrap", alignItems: "center" }}>
          <select value={gov} onChange={(e) => setGov(e.target.value)} style={{ minWidth: 200 }}>
            <option value="">{ar ? "كل المحافظات" : "All governorates"}</option>
            {governorates.map((g) => (
              <option key={g.governorate} value={g.governorate}>{g.governorate} ({g.facilities})</option>
            ))}
          </select>
          <span style={{ display: "inline-flex", gap: 6, flexWrap: "wrap" }}>
            {TYPE_FILTERS.map((ty) => (
              <button key={ty || "all"} type="button"
                className={`btn btn-small ${typeFilter === ty ? "" : "btn-secondary"}`}
                onClick={() => setTypeFilter(ty)}>
                {ty === "" ? (ar ? "الكل" : "All") : typeLabel(ty)}
              </button>
            ))}
          </span>
        </div>
        {finderLoading ? <p className="muted">{ar ? "جارٍ التحميل…" : "Loading…"}</p>
        : finder.length === 0 ? <p className="muted">{ar ? "لا منشآت مطابقة حاليًا." : "No matching facilities right now."}</p>
        : (
          <div className="finder-grid">
            {finder.map((f) => (
              <div key={f.id} className="finder-card">
                <strong>{ar ? f.nameAr : (f.nameEn || f.nameAr)}</strong>
                <div className="muted" style={{ fontSize: 12 }}>
                  {typeLabel(f.type)} · {f.governorate}{f.city ? ` · ${f.city}` : ""}{f.bedCapacity ? ` · ${f.bedCapacity} ${ar ? "سرير" : "beds"}` : ""}
                </div>
                <div style={{ display: "flex", gap: 5, flexWrap: "wrap", margin: "8px 0" }}>
                  {f.services.map((sv) => (
                    <span key={sv} className="badge">{ar ? SERVICE_LABELS[sv]?.ar ?? sv : SERVICE_LABELS[sv]?.en ?? sv}</span>
                  ))}
                </div>
                <div style={{ display: "flex", gap: 6, flexWrap: "wrap" }}>
                  {f.services.includes("clinics") && (
                    <Link className="btn btn-small" href={portalHref}>{ar ? "احجز موعد عيادة" : "Book a clinic appointment"}</Link>
                  )}
                  <Link className="btn btn-small btn-secondary" href={portalHref}>{ar ? "اطلب خدمة" : "Request a service"}</Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      <h2 className="section-title">{t("capTitle")}</h2>
      <p className="section-sub">{ar ? "اختر المحافظة ثم المنشأة لعرض سعتها السريرية لحظيًا." : "Pick a governorate, then a facility, to see its live clinical capacity."}</p>
      <div className="card">
        <div className="l-row l-mb">
          <select value={capGov} onChange={(e) => { setCapGov(e.target.value); setCapFacility(""); }} className="w-select">
            {governorates.map((g) => (
              <option key={g.governorate} value={g.governorate}>{g.governorate}</option>
            ))}
          </select>
          <select value={capFacility} onChange={(e) => setCapFacility(e.target.value)} className="w-select">
            <option value="">{ar ? "كل منشآت المحافظة" : "All facilities in the governorate"}</option>
            {directory.filter((f) => f.governorate === capGov).map((f) => (
              <option key={f.id} value={f.id}>{ar ? f.nameAr : (f.nameEn || f.nameAr)}</option>
            ))}
          </select>
        </div>
        {(() => {
          const fac = directory.find((f) => f.id === capFacility);
          if (fac) {
            const beds = fac.wardBeds > 0 ? fac.wardBeds : (fac.bedCapacity ?? 0);
            return (
              <div>
                <div className="kpi-grid">
                  <div className="kpi-card"><span className="kpi-value">{beds}</span><span className="kpi-label">{ar ? "إجمالي الأسرّة" : "Total beds"}</span></div>
                  <div className="kpi-card"><span className="kpi-value">{fac.inpatients}</span><span className="kpi-label">{ar ? "مرضى مقيمون" : "Inpatients"}</span></div>
                  <div className="kpi-card"><span className="kpi-value">{fac.occupancyPct != null ? `${fac.occupancyPct}%` : "—"}</span><span className="kpi-label">{ar ? "نسبة الإشغال" : "Occupancy"}</span></div>
                  <div className="kpi-card"><span className="kpi-value">{fac.erWaiting}</span><span className="kpi-label">{ar ? "منتظرون بالطوارئ" : "Waiting in ER"}</span></div>
                </div>
                {fac.occupancyPct != null && (
                  <div className="hbar-row">
                    <span>{ar ? fac.nameAr : (fac.nameEn || fac.nameAr)}</span>
                    <span className="hbar-track"><span className="hbar-occ" style={{ width: `${Math.min(100, fac.occupancyPct)}%` }} /></span>
                    <span className="hbar-val">{fac.inpatients}/{beds}</span>
                  </div>
                )}
              </div>
            );
          }
          const g = capacity.find((x) => x.governorate === capGov);
          if (!g) return <p className="muted">—</p>;
          const pctv = g.totalBeds > 0 ? Math.round((g.occupiedBeds / g.totalBeds) * 1000) / 10 : 0;
          return (
            <div>
              <div className="kpi-grid">
                <div className="kpi-card"><span className="kpi-value">{g.facilities}</span><span className="kpi-label">{ar ? "منشآت" : "Facilities"}</span></div>
                <div className="kpi-card"><span className="kpi-value">{g.totalBeds}</span><span className="kpi-label">{ar ? "إجمالي الأسرّة" : "Total beds"}</span></div>
                <div className="kpi-card"><span className="kpi-value">{g.occupiedBeds}</span><span className="kpi-label">{ar ? "أسرّة مشغولة" : "Occupied beds"}</span></div>
                <div className="kpi-card"><span className="kpi-value">{g.icuOccupied}/{g.icuBeds}</span><span className="kpi-label">{ar ? "العناية المركزة" : "ICU"}</span></div>
                <div className="kpi-card"><span className="kpi-value">{g.ambulancesAvailable}</span><span className="kpi-label">{ar ? "إسعاف متاح" : "Ambulances available"}</span></div>
              </div>
              <div className="hbar-row">
                <span>{g.governorate}</span>
                <span className="hbar-track"><span className="hbar-occ" style={{ width: `${Math.min(100, pctv)}%` }} /></span>
                <span className="hbar-val">{pctv}%</span>
              </div>
              <div className="l-stack l-mt">
                {directory.filter((f) => f.governorate === capGov).map((f) => {
                  const beds = f.wardBeds > 0 ? f.wardBeds : (f.bedCapacity ?? 0);
                  return (
                    <div key={f.id} className="hbar-row">
                      <span>{ar ? f.nameAr : (f.nameEn || f.nameAr)}</span>
                      <span className="hbar-track"><span className="hbar-occ" style={{ width: `${Math.min(100, f.occupancyPct ?? 0)}%` }} /></span>
                      <span className="hbar-val">{f.inpatients}/{beds}{f.occupancyPct != null ? ` · ${f.occupancyPct}%` : ""}</span>
                    </div>
                  );
                })}
              </div>
            </div>
          );
        })()}
      </div>

      </div>
      <PublicFooter />
    </>
  );
}
