"use client";

import { useEffect, useState } from "react";
import DataTable from "@/components/DataTable";
import KpiCard from "@/components/KpiCard";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";
import type { GovernorateCapacity, NationalKpis } from "@/lib/types";

export default function DashboardPage() {
  const { lang, t } = useLang();
  const [kpis, setKpis] = useState<NationalKpis | null>(null);
  const [capacity, setCapacity] = useState<GovernorateCapacity[]>([]);
  const [demo, setDemo] = useState(false);

  useEffect(() => {
    void (async () => {
      const [k, c] = await Promise.all([api.kpis(), api.capacity()]);
      setKpis(k.data);
      setCapacity(c.data);
      setDemo(k.demo || c.demo);
    })();
  }, []);

  const pct = (used: number, total: number) => (total ? Math.round((used / total) * 1000) / 10 : 0);

  return (
    <>
      <div className="page-head">
        <h1>{t("dashboard")}</h1>
        <p>{lang === "ar" ? "الصورة الوطنية المجمّعة — للأدوار المركزية المخوّلة فقط" : "National aggregate view — authorised central roles only"}</p>
      </div>
      {demo ? <div className="demo-banner">{t("demoMode")}</div> : null}

      <div className="kpi-grid">
        <KpiCard label={lang === "ar" ? "منشآت نشطة" : "Active facilities"} value={kpis?.facilitiesActive.toLocaleString("ar-EG") ?? "…"} />
        <KpiCard label={lang === "ar" ? "مرضى مسجلون (MPI)" : "Registered patients (MPI)"} value={kpis?.patientsRegistered.toLocaleString("ar-EG") ?? "…"} />
        <KpiCard label={lang === "ar" ? "لقاءات اليوم" : "Encounters today"} value={kpis?.encountersToday.toLocaleString("ar-EG") ?? "…"} />
        <KpiCard label={lang === "ar" ? "إشغال الأسرّة" : "Bed occupancy"} value={kpis ? `${kpis.bedOccupancyPct}%` : "…"} tone="warn" />
        <KpiCard label={lang === "ar" ? "إشغال العناية المركزة" : "ICU occupancy"} value={kpis ? `${kpis.icuOccupancyPct}%` : "…"} tone="warn" />
        <KpiCard label={lang === "ar" ? "سيارات إسعاف متاحة" : "Ambulances available"} value={kpis?.ambulancesAvailable ?? "…"} tone="ok" />
        <KpiCard label={lang === "ar" ? "دورات عمل مفتوحة" : "Open cycle instances"} value={kpis?.openCycleInstances.toLocaleString("ar-EG") ?? "…"} />
        <KpiCard label={lang === "ar" ? "تجاوزات SLA" : "SLA breaches"} value={kpis?.slaBreaches ?? "…"} tone="danger" />
      </div>

      <section className="card">
        <h2>{lang === "ar" ? "السعة حسب المحافظة" : "Capacity by governorate"}</h2>
        <DataTable<GovernorateCapacity & { id: string }>
          rows={capacity.map((c) => ({ ...c, id: c.governorate }))}
          columns={[
            { key: "gov", header: lang === "ar" ? "المحافظة" : "Governorate", render: (r) => r.governorate },
            { key: "fac", header: lang === "ar" ? "منشآت" : "Facilities", render: (r) => r.facilities },
            {
              key: "beds",
              header: lang === "ar" ? "إشغال الأسرّة" : "Bed occupancy",
              render: (r) => (
                <span>
                  {r.occupiedBeds.toLocaleString()} / {r.totalBeds.toLocaleString()} ({pct(r.occupiedBeds, r.totalBeds)}%)
                  <span className={`progress ${pct(r.occupiedBeds, r.totalBeds) > 85 ? "hot" : ""}`}>
                    <div style={{ width: `${pct(r.occupiedBeds, r.totalBeds)}%` }} />
                  </span>
                </span>
              ),
            },
            { key: "icu", header: "ICU", render: (r) => `${r.icuOccupied} / ${r.icuBeds}` },
            { key: "amb", header: lang === "ar" ? "إسعاف متاح" : "Ambulances", render: (r) => r.ambulancesAvailable },
          ]}
        />
      </section>
    </>
  );
}
