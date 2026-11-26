"use client";

import { useEffect, useState, type FormEvent } from "react";
import DataTable from "@/components/DataTable";
import StatusBadge from "@/components/StatusBadge";
import { PageHead } from "@/components/ui";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";
import {
  CYCLE_TRANSITIONS,
  REASON_REQUIRED,
  type CycleDefinition,
  type CycleInstance,
  type CycleStatus,
} from "@/lib/types";

export default function CyclesPage() {
  const { lang, t } = useLang();
  const ar = lang === "ar";
  const [definitions, setDefinitions] = useState<CycleDefinition[]>([]);
  const [instances, setInstances] = useState<CycleInstance[]>([]);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [msg, setMsg] = useState<{ ok: boolean; text: string } | null>(null);
  const [form, setForm] = useState({ definitionCode: "", title: "", subjectType: "patient", subjectRef: "", protocolVersion: "" });
  const [busy, setBusy] = useState<string | null>(null);

  const load = async () => {
    try {
      const [d, i] = await Promise.all([api.cycleDefinitions(), api.cycleInstances()]);
      setDefinitions(d.data);
      setInstances(i.data);
      setLoadError(null);
      if (!form.definitionCode && d.data.length) setForm((f) => ({ ...f, definitionCode: d.data[0].code }));
    } catch (e) {
      setLoadError(e instanceof Error ? e.message : String(e));
    }
  };
  useEffect(() => { void load(); /* eslint-disable-next-line react-hooks/exhaustive-deps */ }, []);

  const onCreate = async (e: FormEvent) => {
    e.preventDefault();
    setMsg(null);
    try {
      await api.createCycleInstance({
        definitionCode: form.definitionCode,
        title: form.title,
        subjectType: form.subjectType,
        subjectRef: form.subjectRef || undefined,
        protocolVersion: form.protocolVersion || undefined,
      });
      setMsg({ ok: true, text: ar ? "أُنشئت دورة عمل جديدة بحالة مسودة" : "New cycle instance created (draft)" });
      setForm((f) => ({ ...f, title: "", subjectRef: "" }));
      void load();
    } catch (e) {
      setMsg({ ok: false, text: e instanceof Error ? e.message : String(e) });
    }
  };

  const onTransition = async (inst: CycleInstance, to: CycleStatus) => {
    let reason: string | undefined;
    if (REASON_REQUIRED.has(to)) {
      const entered = window.prompt(
        ar ? `توثيق سبب الانتقال إلى ${to} إلزامي (كتالوج v6):` : `A reason is required to move to ${to} (Catalog v6):`,
      );
      if (!entered) return;
      reason = entered;
    }
    setBusy(inst.id);
    try {
      const r = await api.transitionCycleInstance(inst.id, to, reason);
      setInstances((prev) => prev.map((x) => (x.id === inst.id ? r.data : x)));
    } catch (e) {
      setMsg({ ok: false, text: e instanceof Error ? e.message : String(e) });
    } finally {
      setBusy(null);
    }
  };

  return (
    <>
      <PageHead
        eyebrow={ar ? "دورات العمل 217–254" : "Operating cycles 217–254"}
        title={t("cycles")}
        sub={ar
          ? "الدورات المرقّمة 217–254 بمنهج توصيف موحد: انتقالات مسموحة فقط، ومنع تخطي خطوة إلزامية"
          : "Numbered cycles 217–254 under one specification method: allowed transitions only, no skipping mandatory steps"}
      />
      {loadError ? <div className="alert-error">{loadError}</div> : null}

      <section className="card">
        <h2>{ar ? "تعريفات الدورات (كتالوج v6)" : "Cycle definitions (Catalog v6)"}</h2>
        <DataTable<CycleDefinition & { id: string }>
          rows={definitions.map((d) => ({ ...d, id: d.code }))}
          columns={[
            { key: "code", header: ar ? "الكود" : "Code", render: (r) => r.code },
            { key: "title", header: ar ? "الدورة" : "Cycle", render: (r) => (ar ? r.titleAr : r.titleEn ?? r.titleAr) },
            { key: "domain", header: ar ? "المجال" : "Domain", render: (r) => r.domain },
            { key: "route", header: ar ? "المسار المتخصص" : "Specialised route", render: (r) => r.specialisedRoute ?? "—" },
            { key: "sla", header: "SLA (h)", render: (r) => r.slaHours },
            { key: "status", header: t("status"), render: (r) => <StatusBadge status={r.status} /> },
          ]}
        />
      </section>

      <section className="card">
        <h2>{ar ? "فتح دورة عمل جديدة" : "Open a new cycle instance"}</h2>
        <form onSubmit={onCreate}>
          <div className="form-grid">
            <div className="field">
              <label htmlFor="def">{ar ? "تعريف الدورة *" : "Cycle definition *"}</label>
              <select id="def" required value={form.definitionCode} onChange={(e) => setForm({ ...form, definitionCode: e.target.value })}>
                {definitions.map((d) => (
                  <option key={d.code} value={d.code}>{d.code} — {ar ? d.titleAr : d.titleEn ?? d.titleAr}</option>
                ))}
              </select>
            </div>
            <div className="field">
              <label htmlFor="title">{ar ? "العنوان *" : "Title *"}</label>
              <input id="title" required value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
            </div>
            <div className="field">
              <label htmlFor="stype">{ar ? "نوع الموضوع" : "Subject type"}</label>
              <select id="stype" value={form.subjectType} onChange={(e) => setForm({ ...form, subjectType: e.target.value })}>
                <option value="patient">patient</option>
                <option value="encounter">encounter</option>
                <option value="facility">facility</option>
                <option value="order">order</option>
              </select>
            </div>
            <div className="field">
              <label htmlFor="sref">{ar ? "مرجع الموضوع" : "Subject reference"}</label>
              <input id="sref" value={form.subjectRef} onChange={(e) => setForm({ ...form, subjectRef: e.target.value })} />
            </div>
            <div className="field">
              <label htmlFor="proto">{ar ? "إصدار البروتوكول" : "Protocol version"}</label>
              <input id="proto" value={form.protocolVersion} onChange={(e) => setForm({ ...form, protocolVersion: e.target.value })} placeholder="مثال: REF-2026.1" />
            </div>
          </div>
          <div className="form-actions">
            <button className="btn" type="submit">{ar ? "فتح الدورة" : "Open instance"}</button>
          </div>
          {msg ? <div className={`form-msg ${msg.ok ? "ok" : "err"}`}>{msg.text}</div> : null}
        </form>
      </section>

      <section className="card">
        <h2>{ar ? "الدورات الجارية" : "Running instances"}</h2>
        <DataTable<CycleInstance>
          rows={instances}
          columns={[
            { key: "def", header: ar ? "الدورة" : "Cycle", render: (r) => r.definitionCode },
            { key: "title", header: ar ? "العنوان" : "Title", render: (r) => r.title },
            { key: "step", header: ar ? "الخطوة الحالية" : "Current step", render: (r) => r.currentStep ?? "—" },
            { key: "sla", header: "SLA", render: (r) => (r.slaDueAt ? new Date(r.slaDueAt).toLocaleString(ar ? "ar-EG" : "en-GB") : "—") },
            { key: "esc", header: ar ? "مصعّدة" : "Escalated", render: (r) => (r.escalated ? "⚠" : "—") },
            { key: "status", header: t("status"), render: (r) => <StatusBadge status={r.status} /> },
            {
              key: "actions",
              header: t("actions"),
              render: (r) => {
                const allowed = CYCLE_TRANSITIONS[r.status] ?? [];
                if (!allowed.length) return <span className="badge">{ar ? "منتهية" : "terminal"}</span>;
                return (
                  <span className="row">
                    {allowed.map((to) => (
                      <button
                        key={to}
                        type="button"
                        className="btn btn-soft btn-sm"
                        disabled={busy === r.id}
                        onClick={() => void onTransition(r, to)}
                      >
                        → {to}
                      </button>
                    ))}
                  </span>
                );
              },
            },
          ]}
        />
      </section>
    </>
  );
}
