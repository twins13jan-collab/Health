"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import DataTable from "@/components/DataTable";
import KpiCard from "@/components/KpiCard";
import StatusBadge from "@/components/StatusBadge";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";
import type { CycleInstance, GovernorateCapacity, NationalKpis } from "@/lib/types";

const QUICK_LINKS: { href: string; ar: string; en: string; icon: string }[] = [
  { href: "/patients", ar: "تسجيل مريض", en: "Register patient", icon: "☰" },
  { href: "/encounters", ar: "الاستقبال والفرز", en: "Reception & triage", icon: "✚" },
  { href: "/dispatch", ar: "إرسال الإسعاف", en: "Ambulance dispatch", icon: "🚑" },
  { href: "/capacity", ar: "السعة والأسرّة", en: "Capacity & beds", icon: "🛏" },
  { href: "/labs", ar: "المعامل", en: "Laboratories", icon: "🧪" },
  { href: "/pharmacy", ar: "الصيدلية", en: "Pharmacy", icon: "💊" },
  { href: "/blood-bank", ar: "بنك الدم", en: "Blood bank", icon: "🩸" },
  { href: "/referrals", ar: "الإحالات", en: "Referrals", icon: "↗" },
];

export default function DashboardPage() {
  const { lang, t } = useLang();
  const ar = lang === "ar";
  const [kpis, setKpis] = useState<NationalKpis | null>(null);
  const [capacity, setCapacity] = useState<GovernorateCapacity[]>([]);
  const [instances, setInstances] = useState<CycleInstance[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);

  useEffect(() => {
    void (async () => {
      try {
        const [k, c, i] = await Promise.all([api.kpis(), api.capacity(), api.cycleInstances()]);
        setKpis(k.data);
        setCapacity(c.data);
        setInstances(i.data.slice(0, 8));
      } catch (e) {
        setLoadError(e instanceof Error ? e.message : String(e));
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const pct = (used: number, total: number) => (total ? Math.round((used / total) * 1000) / 10 : 0);

  return (
    <>
      <div className="page-head">
        <h1>{t("dashboard")}</h1>
        <p>{ar ? "الصورة التشغيلية المجمّعة لنطاق صلاحيتك — بيانات حية من المنصة" : "The aggregated operational picture for your authorised scope — live platform data"}</p>
      </div>
      {loadError ? <div className="error-banner">{loadError}</div> : null}

      {loading ? (
        <div className="kpi-grid">
          {Array.from({ length: 8 }).map((_, i) => <div key={i} className="skeleton skel-104" />)}
        </div>
      ) : (
        <div className="kpi-grid">
          <KpiCard label={ar ? "منشآت نشطة" : "Active facilities"} value={kpis?.facilitiesActive.toLocaleString("ar-EG") ?? "—"} />
          <KpiCard label={ar ? "مرضى مسجلون (MPI)" : "Registered patients (MPI)"} value={kpis?.patientsRegistered.toLocaleString("ar-EG") ?? "—"} />
          <KpiCard label={ar ? "لقاءات اليوم" : "Encounters today"} value={kpis?.encountersToday.toLocaleString("ar-EG") ?? "—"} />
          <KpiCard label={ar ? "إشغال الأسرّة" : "Bed occupancy"} value={kpis ? `${kpis.bedOccupancyPct}%` : "—"} tone={kpis && kpis.bedOccupancyPct > 85 ? "danger" : "warn"} />
          <KpiCard label={ar ? "إشغال العناية المركزة" : "ICU occupancy"} value={kpis ? `${kpis.icuOccupancyPct}%` : "—"} tone={kpis && kpis.icuOccupancyPct > 85 ? "danger" : "warn"} />
          <KpiCard label={ar ? "سيارات إسعاف متاحة" : "Ambulances available"} value={kpis?.ambulancesAvailable ?? "—"} tone="ok" />
          <KpiCard label={ar ? "دورات عمل مفتوحة" : "Open cycle instances"} value={kpis?.openCycleInstances.toLocaleString("ar-EG") ?? "—"} />
          <KpiCard label={ar ? "تجاوزات SLA" : "SLA breaches"} value={kpis?.slaBreaches ?? "—"} tone="danger" />
        </div>
      )}

      <section className="card">
        <h2>{ar ? "إجراءات سريعة" : "Quick actions"}</h2>
        <div className="quick-grid">
          {QUICK_LINKS.map((l) => (
            <Link key={l.href} href={l.href} className="quick-link">
              <span aria-hidden>{l.icon}</span> {ar ? l.ar : l.en}
            </Link>
          ))}
        </div>
      </section>

      <section className="card">
        <h2>{ar ? "السعة حسب المحافظة" : "Capacity by governorate"}</h2>
        <DataTable<GovernorateCapacity & { id: string }>
          rows={capacity.map((c) => ({ ...c, id: c.governorate }))}
          columns={[
            { key: "gov", header: ar ? "المحافظة" : "Governorate", render: (r) => r.governorate },
            { key: "fac", header: ar ? "منشآت" : "Facilities", render: (r) => r.facilities },
            {
              key: "beds",
              header: ar ? "إشغال الأسرّة" : "Bed occupancy",
              render: (r) => (
                <span>
                  {r.occupiedBeds.toLocaleString()} / {r.totalBeds.toLocaleString()} ({pct(r.occupiedBeds, r.totalBeds)}%)
                  <span className={`progress ${pct(r.occupiedBeds, r.totalBeds) > 85 ? "hot" : pct(r.occupiedBeds, r.totalBeds) > 70 ? "warm" : ""}`}>
                    <div style={{ width: `${pct(r.occupiedBeds, r.totalBeds)}%` }} />
                  </span>
                </span>
              ),
            },
            { key: "icu", header: "ICU", render: (r) => `${r.icuOccupied} / ${r.icuBeds}` },
            { key: "amb", header: ar ? "إسعاف متاح" : "Ambulances", render: (r) => r.ambulancesAvailable },
          ]}
        />
      </section>

      <section className="card">
        <h2>{ar ? "أحدث دورات العمل" : "Latest cycle instances"}</h2>
        {instances.length === 0 && !loading ? (
          <div className="empty">{ar ? "لا توجد دورات عمل بعد في نطاقك" : "No cycle instances yet in your scope"}</div>
        ) : (
          <DataTable<CycleInstance>
            rows={instances}
            columns={[
              { key: "code", header: ar ? "الدورة" : "Cycle", render: (r) => r.definitionCode },
              { key: "title", header: ar ? "العنوان" : "Title", render: (r) => r.title },
              { key: "step", header: ar ? "الخطوة الحالية" : "Current step", render: (r) => r.currentStep ?? "—" },
              { key: "status", header: ar ? "الحالة" : "Status", render: (r) => <StatusBadge status={r.status} /> },
              { key: "sla", header: ar ? "موعد SLA" : "SLA due", render: (r) => (r.slaDueAt ? r.slaDueAt.slice(0, 16).replace("T", " ") : "—") },
            ]}
          />
        )}
      </section>
    </>
  );
}
