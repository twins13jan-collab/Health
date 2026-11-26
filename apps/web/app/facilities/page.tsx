"use client";

import { useEffect, useState } from "react";
import DataTable from "@/components/DataTable";
import StatusBadge from "@/components/StatusBadge";
import { PageHead, ProgressBar } from "@/components/ui";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";
import { facilityTypeLabel, type Facility } from "@/lib/types";

export default function FacilitiesPage() {
  const { lang, t } = useLang();
  const ar = lang === "ar";
  const [rows, setRows] = useState<Facility[]>([]);
  const [loadError, setLoadError] = useState<string | null>(null);

  useEffect(() => {
    void (async () => {
      try {
        const r = await api.facilities();
        setRows(r.data);
      } catch (e) {
        setLoadError(e instanceof Error ? e.message : String(e));
      }
    })();
  }, []);

  const pct = (used: number, total: number) => (total ? Math.round((used / total) * 1000) / 10 : 0);

  return (
    <>
      <PageHead
        eyebrow={ar ? "المنشآت" : "Facilities"}
        title={t("facilities")}
        sub={ar ? "كل منشأة ترى بياناتها فقط — العزل الصارم بين المنشآت (Multi-Tenancy + RLS)" : "Each facility sees only its own data — strict multi-tenancy + RLS"}
      />
      {loadError ? <div className="alert-error">{loadError}</div> : null}
      <section className="card">
        <DataTable<Facility>
          rows={rows}
          columns={[
            { key: "code", header: ar ? "الكود" : "Code", render: (r) => r.code },
            { key: "name", header: ar ? "المنشأة" : "Facility", render: (r) => (ar ? r.nameAr : r.nameEn) },
            { key: "type", header: ar ? "النوع" : "Type", render: (r) => facilityTypeLabel(r.type, lang) },
            { key: "level", header: ar ? "المستوى" : "Level", render: (r) => r.level },
            { key: "gov", header: ar ? "المحافظة" : "Governorate", render: (r) => r.governorate },
            {
              key: "beds",
              header: ar ? "إشغال الأسرّة" : "Bed occupancy",
              render: (r) => (
                <span className="row">
                  <span>{r.occupiedBeds}/{r.totalBeds} ({pct(r.occupiedBeds, r.totalBeds)}%)</span>
                  <ProgressBar pct={pct(r.occupiedBeds, r.totalBeds)} />
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
