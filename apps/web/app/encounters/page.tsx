"use client";

import { useEffect, useMemo, useState } from "react";
import DataTable from "@/components/DataTable";
import StatusBadge from "@/components/StatusBadge";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";
import type { Encounter, TriageLevel } from "@/lib/types";

const TRIAGE_ORDER: TriageLevel[] = ["resuscitation", "emergent", "urgent", "less_urgent", "non_urgent"];

export default function EncountersPage() {
  const { lang, t } = useLang();
  const [rows, setRows] = useState<Encounter[]>([]);
  const [demo, setDemo] = useState(false);
  const [triageFilter, setTriageFilter] = useState<string>("all");
  const [statusFilter, setStatusFilter] = useState<string>("all");

  useEffect(() => {
    void (async () => {
      const r = await api.encounters();
      setRows(r.data);
      setDemo(r.demo);
    })();
  }, []);

  const filtered = useMemo(
    () =>
      rows
        .filter((e) => triageFilter === "all" || e.triageLevel === triageFilter)
        .filter((e) => statusFilter === "all" || e.status === statusFilter)
        .sort((a, b) => TRIAGE_ORDER.indexOf(a.triageLevel) - TRIAGE_ORDER.indexOf(b.triageLevel)),
    [rows, triageFilter, statusFilter],
  );

  const counts = useMemo(() => {
    const c: Record<string, number> = {};
    for (const e of rows) c[e.triageLevel] = (c[e.triageLevel] ?? 0) + 1;
    return c;
  }, [rows]);

  return (
    <>
      <div className="page-head">
        <h1>{t("encounters")}</h1>
        <p>{lang === "ar" ? "الاستقبال والفرز والمسار السريري — مرتبة حسب درجة الاستعجال" : "Reception, triage and the clinical pathway — sorted by urgency"}</p>
      </div>
      {demo ? <div className="demo-banner">{t("demoMode")}</div> : null}

      <section className="card">
        <h2>{lang === "ar" ? "لوحة الفرز" : "Triage board"}</h2>
        <div className="kpi-grid">
          {TRIAGE_ORDER.map((level) => (
            <div key={level} className="kpi-card">
              <div className="kpi-label"><StatusBadge status={level} /></div>
              <div className="kpi-value">{counts[level] ?? 0}</div>
            </div>
          ))}
        </div>
        <div className="toolbar">
          <div className="field">
            <label htmlFor="tf">{lang === "ar" ? "درجة الفرز" : "Triage level"}</label>
            <select id="tf" value={triageFilter} onChange={(e) => setTriageFilter(e.target.value)}>
              <option value="all">{lang === "ar" ? "الكل" : "All"}</option>
              {TRIAGE_ORDER.map((l) => (<option key={l} value={l}>{l}</option>))}
            </select>
          </div>
          <div className="field">
            <label htmlFor="sf">{t("status")}</label>
            <select id="sf" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
              <option value="all">{lang === "ar" ? "الكل" : "All"}</option>
              {["registered", "triaged", "in_consultation", "admitted", "discharged", "referred", "closed"].map((s) => (
                <option key={s} value={s}>{s}</option>
              ))}
            </select>
          </div>
        </div>
        <DataTable<Encounter>
          rows={filtered}
          columns={[
            { key: "patient", header: lang === "ar" ? "المريض" : "Patient", render: (r) => r.patientNameAr },
            { key: "complaint", header: lang === "ar" ? "سبب الحضور" : "Chief complaint", render: (r) => r.chiefComplaint },
            { key: "triage", header: lang === "ar" ? "الفرز" : "Triage", render: (r) => <StatusBadge status={r.triageLevel} /> },
            { key: "news2", header: "NEWS2", render: (r) => r.news2Score ?? "—" },
            { key: "facility", header: lang === "ar" ? "المنشأة" : "Facility", render: (r) => r.facilityNameAr },
            { key: "arrived", header: lang === "ar" ? "وقت الوصول" : "Arrived", render: (r) => new Date(r.arrivedAt).toLocaleString(lang === "ar" ? "ar-EG" : "en-GB") },
            { key: "status", header: t("status"), render: (r) => <StatusBadge status={r.status} /> },
          ]}
        />
      </section>
    </>
  );
}
