"use client";

/**
 * Config-driven module page. Renders live rows from the Java core
 * endpoint (envelope key) with a clearly-flagged demo fallback.
 * Completion batch 7: pages may also declare operational writes —
 * a create form and per-row actions that POST to the real API and
 * refresh. Writes are never faked: a failed action shows its error.
 */
import { useCallback, useEffect, useState } from "react";
import DataTable, { type Column } from "@/components/DataTable";
import StatusBadge from "@/components/StatusBadge";
import { api } from "@/lib/api";
import { queueOffline } from "@/lib/offline";
import { useLang, type DictKey } from "@/lib/i18n";

export interface ModuleColumn {
  key: string;
  ar: string;
  en: string;
  status?: boolean;
}

export interface ModuleField {
  key: string;
  ar: string;
  en: string;
}

export interface ModuleAction {
  labelAr: string;
  labelEn: string;
  /** Endpoint template; {id} is replaced with the row id. Omit for page-level actions. */
  endpoint: string;
  /** Static JSON body for the action (row actions may leave it empty). */
  body?: Record<string, unknown>;
}

export interface ModuleConfig {
  titleKey: DictKey;
  subtitleAr: string;
  subtitleEn: string;
  endpoint: string;
  envelopeKey: string;
  columns: ModuleColumn[];
  demoRows: Record<string, unknown>[];
  createForm?: { endpoint: string; fields: ModuleField[]; submitAr: string; submitEn: string };
  rowActions?: ModuleAction[];
  pageActions?: ModuleAction[];
}

type Row = Record<string, unknown> & { id: string };

export default function ModulePage({ config }: { config: ModuleConfig }) {
  const { lang, t } = useLang();
  const [rows, setRows] = useState<Row[]>([]);
  const [demo, setDemo] = useState(false);
  const [form, setForm] = useState<Record<string, string>>({});
  const [notice, setNotice] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const load = useCallback(() => {
    api
      .moduleList<Record<string, unknown>>(config.endpoint, config.envelopeKey, () => config.demoRows)
      .then((r) => {
        setRows(r.data.map((d, i) => ({ id: String(d.id ?? `row-${i}`), ...d })));
        setDemo(r.demo);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => { load(); }, [load]);

  async function runAction(action: ModuleAction, rowId?: string) {
    setBusy(true); setError(null); setNotice(null);
    try {
      const path = rowId ? action.endpoint.replace("{id}", rowId) : action.endpoint;
      await api.postJson(path, action.body ?? {});
      setNotice(lang === "ar" ? "تم التنفيذ بنجاح" : "Done");
      load();
    } catch (e) {
      if (e instanceof TypeError) {
        queueOffline("record_action", "administrative", `${action.labelEn} on ${config.endpoint}`, action.body ?? {});
        setNotice(lang === "ar" ? "لا اتصال — حُفظ الإجراء في طابور العمل دون اتصال وسيُزامَن عند عودة الشبكة" : "Offline — action queued for sync");
      } else {
        setError(lang === "ar" ? `فشل التنفيذ: ${e instanceof Error ? e.message : e}` : `Action failed: ${e instanceof Error ? e.message : e}`);
      }
    } finally { setBusy(false); }
  }

  async function submitCreate(ev: React.FormEvent) {
    ev.preventDefault();
    if (!config.createForm) return;
    setBusy(true); setError(null); setNotice(null);
    const body: Record<string, unknown> = {};
    try {
      for (const f of config.createForm.fields) {
        const v = form[f.key] ?? "";
        body[f.key] = v === "true" ? true : v === "false" ? false : v !== "" && !Number.isNaN(Number(v)) && /^-?\d+(\.\d+)?$/.test(v) ? Number(v) : v;
      }
      await api.postJson(config.createForm.endpoint, body);
      setForm({});
      setNotice(lang === "ar" ? "تم الإنشاء بنجاح" : "Created");
      load();
    } catch (e) {
      if (e instanceof TypeError) {
        queueOffline("create_record", "administrative", `create on ${config.createForm.endpoint}`, body);
        setForm({});
        setNotice(lang === "ar" ? "لا اتصال — حُفظ السجل في طابور العمل دون اتصال وسيُزامَن عند عودة الشبكة" : "Offline — record queued for sync");
      } else {
        setError(lang === "ar" ? `فشل الإنشاء: ${e instanceof Error ? e.message : e}` : `Create failed: ${e instanceof Error ? e.message : e}`);
      }
    } finally { setBusy(false); }
  }

  const columns: Column<Row>[] = config.columns.map((c) => ({
    key: c.key,
    header: lang === "ar" ? c.ar : c.en,
    render: (row) => {
      const v = row[c.key];
      if (c.status && typeof v === "string") return <StatusBadge status={v} />;
      if (v === null || v === undefined) return "—";
      if (typeof v === "object") return JSON.stringify(v);
      return String(v);
    },
  }));

  if (config.rowActions?.length) {
    columns.push({
      key: "__actions",
      header: lang === "ar" ? "إجراءات" : "Actions",
      render: (row) => (
        <span style={{ display: "inline-flex", gap: 6 }}>
          {config.rowActions!.map((a) => (
            <button key={a.labelEn} disabled={busy} onClick={() => runAction(a, row.id)}>
              {lang === "ar" ? a.labelAr : a.labelEn}
            </button>
          ))}
        </span>
      ),
    });
  }

  return (
    <>
      <div className="page-head">
        <h1>{t(config.titleKey)}</h1>
        <p>{lang === "ar" ? config.subtitleAr : config.subtitleEn}</p>
      </div>
      {demo ? <div className="demo-banner">{t("demoMode")}</div> : null}
      {config.pageActions?.length ? (
        <div style={{ display: "flex", gap: 8, marginBottom: 12 }}>
          {config.pageActions.map((a) => (
            <button key={a.labelEn} disabled={busy} onClick={() => runAction(a)}>
              {lang === "ar" ? a.labelAr : a.labelEn}
            </button>
          ))}
        </div>
      ) : null}
      {config.createForm ? (
        <form onSubmit={submitCreate} style={{ display: "flex", flexWrap: "wrap", gap: 8, marginBottom: 16, alignItems: "end" }}>
          {config.createForm.fields.map((f) => (
            <label key={f.key} style={{ display: "flex", flexDirection: "column", gap: 4, fontSize: 13 }}>
              {lang === "ar" ? f.ar : f.en}
              <input value={form[f.key] ?? ""} onChange={(e) => setForm({ ...form, [f.key]: e.target.value })} style={{ padding: 6 }} />
            </label>
          ))}
          <button type="submit" disabled={busy} style={{ padding: "7px 16px" }}>
            {lang === "ar" ? config.createForm.submitAr : config.createForm.submitEn}
          </button>
        </form>
      ) : null}
      {notice ? <p style={{ color: "#177245" }}>{notice}</p> : null}
      {error ? <p style={{ color: "#b00" }}>{error}</p> : null}
      <DataTable columns={columns} rows={rows} />
      <p className="page-foot-note">
        {lang === "ar"
          ? `المصدر: ${config.endpoint} — النواة Java/Spring Boot`
          : `Source: ${config.endpoint} — Java/Spring Boot core`}
      </p>
    </>
  );
}
