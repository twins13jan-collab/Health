"use client";

import { useEffect, useState } from "react";
import DataTable from "@/components/DataTable";
import StatusBadge from "@/components/StatusBadge";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";
import type { Facility } from "@/lib/types";

export default function FacilitiesPage() {
  const { lang, t } = useLang();
  const [rows, setRows] = useState<Facility[]>([]);
  const [demo, setDemo] = useState(false);

  useEffect(() => {
    void (async () => {
      const r = await api.facilities();
      setRows(r.data);
      setDemo(r.demo);
    })();
  }, []);

  const pct = (used: number, total: number) => (total ? Math.round((used / total) * 1000) / 10 : 0);

  return (
    <>
      <div className="page-head">
        <h1>{t("facilities")}</h1>
        <p>{lang === "ar" ? "كل منشأة ترى بياناتها فقط — العزل الصارم بين المنشآت (Multi-Tenancy + RLS)" : "Each facility sees only its own data — strict multi-tenancy + RLS"}</p>
      </div>
      {demo ? <div className="demo-banner">{t("demoMode")}</div> : null}
      <section className="card">
        <DataTable<Facility>
          rows={rows}
          columns={[
            { key: "code", header: lang === "ar" ? "الكود" : "Code", render: (r) => r.code },
            { key: "name", header: lang === "ar" ? "المنشأة" : "Facility", render: (r) => (lang === "ar" ? r.nameAr : r.nameEn) },
            { key: "type", header: lang === "ar" ? "النوع" : "Type", render: (r) => r.type.replace(/_/g, " ") },
            { key: "level", header: lang === "ar" ? "المستوى" : "Level", render: (r) => r.level },
            { key: "gov", header: lang === "ar" ? "المحافظة" : "Governorate", render: (r) => r.governorate },
            {
              key: "beds",
              header: lang === "ar" ? "إشغال الأسرّة" : "Bed occupancy",
              render: (r) => (
                <span>
                  {r.occupiedBeds}/{r.totalBeds} ({pct(r.occupiedBeds, r.totalBeds)}%)
                  <span className={`progress ${pct(r.occupiedBeds, r.totalBeds) > 85 ? "hot" : ""}`}>
                    <div style={{ width: `${pct(r.occupiedBeds, r.totalBeds)}%` }} />
                  </span>
                </span>
              ),
            },
            { key: "icu", header: "ICU", render: (r) => `${r.icuOccupied}/${r.icuBeds}` },
            { key: "status", header: t("status"), render: (r) => <StatusBadge status={r.status} /> },
          ]}
        />
      </section>
    </>
  );
}
