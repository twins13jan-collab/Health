"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { getSessionUser, type SessionUser } from "@/lib/api";
import { useLang } from "@/lib/i18n";
import { facilityTypeLabel } from "@/lib/types";

/**
 * Public homepage of the Khattab system — every fact appears
 * exactly once: one live stats strip, one service finder
 * (facilities by governorate with their bookable services), and
 * one capacity view driven by governorate + facility pickers.
 * Booking and service requests start here and continue in the
 * patient portal (after a free registration). Rendered inside
 * PublicShell — the page itself carries no chrome.
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

  const typeLabel = (ty: string) => facilityTypeLabel(ty, lang);
  const portalHref = user ? "/my" : "/register";

  return (
    <>
      <section className="landing-hero">
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img src="/logo.png" alt={t("appName")} className="landing-logo" />
        <h1>{t("sysDesc")}</h1>
        <p>{t("heroText")}</p>
        <div className="landing-cta">
          {user ? (
            <Link className="btn landing-btn" href="/dashboard">{t("dashboard")}</Link>
          ) : (
            <>
              <Link className="btn landing-btn" href="/login">{t("login")}</Link>
              <Link className="btn landing-btn btn-gold" href="/register">{ar ? "إنشاء حساب مجاني" : "Create a free account"}</Link>
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

      <h2 className="section-title" id="facilities">{ar ? "ابحث واحجز خدمة" : "Find & book a service"}</h2>
      <p className="section-sub">{ar
        ? "اختر المحافظة ونوع المنشأة لعرض الخدمات المتاحة لحظيًا — الحجز وطلبات الخدمة من حسابك الصحي."
        : "Pick a governorate and facility type to see services available right now — booking and service requests live in your health account."}</p>
      <div className="card">
        <div className="l-row">
          <select value={gov} onChange={(e) => setGov(e.target.value)} className="w-select" aria-label={ar ? "المحافظة" : "Governorate"}>
            <option value="">{ar ? "كل المحافظات" : "All governorates"}</option>
            {governorates.map((g) => (
              <option key={g.governorate} value={g.governorate}>{g.governorate} ({g.facilities})</option>
            ))}
          </select>
          <span className="l-actions">
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
                <div className="muted text-sm">
                  {typeLabel(f.type)} · {f.governorate}{f.city ? ` · ${f.city}` : ""}{f.bedCapacity ? ` · ${f.bedCapacity} ${ar ? "سرير" : "beds"}` : ""}
                </div>
                <div className="l-mb l-mt">
                  {f.services.map((sv) => (
                    <span key={sv} className="chip">{ar ? SERVICE_LABELS[sv]?.ar ?? sv : SERVICE_LABELS[sv]?.en ?? sv}</span>
                  ))}
                </div>
                <div className="l-actions">
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
      <p className="section-sub">{ar ? "إشغال الأسرّة لحظيًا في كل محافظة — اضغط على أي محافظة لعرض منشآتها، وعلى أي منشأة لعرض تفاصيل سعتها." : "Live bed occupancy in every governorate — click a governorate to see its facilities, and a facility for its capacity detail."}</p>
      <div className="card">
        <div className="cap-list">
          {[...capacity]
            .sort((a, b) => (b.totalBeds > 0 ? b.occupiedBeds / b.totalBeds : 0) - (a.totalBeds > 0 ? a.occupiedBeds / a.totalBeds : 0))
            .map((g) => {
              const pctv = g.totalBeds > 0 ? Math.round((g.occupiedBeds / g.totalBeds) * 1000) / 10 : 0;
              const selected = capGov === g.governorate;
              return (
                <button
                  key={g.governorate}
                  type="button"
                  className={`cap-row${selected ? " sel" : ""}`}
                  onClick={() => { setCapGov(selected ? "" : g.governorate); setCapFacility(""); }}
                  aria-expanded={selected}
                >
                  <span className="cap-name">{g.governorate}</span>
                  <span className="hbar-track"><span className="hbar-occ" style={{ width: `${Math.min(100, pctv)}%` }} /></span>
                  <span className="hbar-val">{g.occupiedBeds}/{g.totalBeds} · {pctv}%</span>
                  <span className="cap-meta">{ar ? "عناية" : "ICU"} {g.icuOccupied}/{g.icuBeds} · {ar ? "إسعاف" : "Amb."} {g.ambulancesAvailable}</span>
                </button>
              );
            })}
          {capacity.length === 0 && <p className="muted">—</p>}
        </div>
        {capGov && (
          <div className="l-divider">
            <h3 className="l-mb">{ar ? `منشآت ${capGov}` : `Facilities in ${capGov}`}</h3>
            <div className="cap-list">
              {directory.filter((f) => f.governorate === capGov).map((f) => {
                const beds = f.wardBeds > 0 ? f.wardBeds : (f.bedCapacity ?? 0);
                const selected = capFacility === f.id;
                return (
                  <button
                    key={f.id}
                    type="button"
                    className={`cap-row cap-row-fac${selected ? " sel" : ""}`}
                    onClick={() => setCapFacility(selected ? "" : f.id)}
                    aria-expanded={selected}
                  >
                    <span className="cap-name">{ar ? f.nameAr : (f.nameEn || f.nameAr)}</span>
                    <span className="hbar-track"><span className="hbar-occ" style={{ width: `${Math.min(100, f.occupancyPct ?? 0)}%` }} /></span>
                    <span className="hbar-val">{f.inpatients}/{beds}{f.occupancyPct != null ? ` · ${f.occupancyPct}%` : ""}</span>
                    <span className="cap-meta">{ar ? "منتظرون بالطوارئ" : "ER waiting"}: {f.erWaiting}</span>
                  </button>
                );
              })}
              {directory.filter((f) => f.governorate === capGov).length === 0 && <p className="muted">{ar ? "لا منشآت مسجلة في هذه المحافظة بعد." : "No facilities registered in this governorate yet."}</p>}
            </div>
            {(() => {
              const fac = directory.find((f) => f.id === capFacility);
              if (!fac) return null;
              const beds = fac.wardBeds > 0 ? fac.wardBeds : (fac.bedCapacity ?? 0);
              return (
                <div className="kpi-grid l-mt">
                  <div className="kpi-card"><span className="kpi-value">{beds}</span><span className="kpi-label">{ar ? "إجمالي الأسرّة" : "Total beds"}</span></div>
                  <div className="kpi-card"><span className="kpi-value">{fac.inpatients}</span><span className="kpi-label">{ar ? "مرضى مقيمون" : "Inpatients"}</span></div>
                  <div className="kpi-card"><span className="kpi-value">{fac.occupancyPct != null ? `${fac.occupancyPct}%` : "—"}</span><span className="kpi-label">{ar ? "نسبة الإشغال" : "Occupancy"}</span></div>
                  <div className="kpi-card"><span className="kpi-value">{fac.erWaiting}</span><span className="kpi-label">{ar ? "منتظرون بالطوارئ" : "Waiting in ER"}</span></div>
                </div>
              );
            })()}
          </div>
        )}
      </div>
    </>
  );
}
