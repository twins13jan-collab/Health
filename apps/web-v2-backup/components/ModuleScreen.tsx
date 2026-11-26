"use client";

/**
 * ModuleScreen — the config-driven module page (successor of the old
 * ModulePage, rebuilt on design system v2). Every module page gets:
 * live data only, toolbar search + status filter, client pagination
 * (15/page), loading skeletons, honest error state with retry,
 * contextual empty states, a create drawer (when the config declares
 * a create form), styled row/page actions, and toast feedback.
 * Writes are never faked: failures surface, and a network failure
 * queues the write into the cycle-237 offline outbox for later sync.
 */
import { useCallback, useEffect, useMemo, useState } from "react";
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
  createForm?: { endpoint: string; fields: ModuleField[]; submitAr: string; submitEn: string };
  rowActions?: ModuleAction[];
  pageActions?: ModuleAction[];
}

type Row = Record<string, unknown> & { id: string };

const PAGE_SIZE = 15;

export default function ModuleScreen({ config }: { config: ModuleConfig }) {
  const { lang, t } = useLang();
  const ar = lang === "ar";
  const [rows, setRows] = useState<Row[]>([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState<Record<string, string>>({});
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [toast, setToast] = useState<{ text: string; err?: boolean } | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [query, setQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const [page, setPage] = useState(1);

  const load = useCallback(() => {
    setLoading(true);
    api
      .moduleList<Record<string, unknown>>(config.endpoint, config.envelopeKey)
      .then((r) => {
        setRows(r.data.map((d, i) => ({ id: String(d.id ?? `row-${i}`), ...d })));
        setError(null);
      })
      .catch((e) => {
        setError(e instanceof Error ? e.message : String(e));
      })
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => { load(); }, [load]);

  useEffect(() => {
    if (!toast) return;
    const id = setTimeout(() => setToast(null), 4500);
    return () => clearTimeout(id);
  }, [toast]);

  const statusValues = useMemo(() => {
    const statusCols = config.columns.filter((c) => c.status);
    const values = new Set<string>();
    for (const row of rows) {
      for (const c of statusCols) {
        const v = row[c.key];
        if (typeof v === "string" && v) values.add(v);
      }
    }
    return [...values].sort();
  }, [rows, config.columns]);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    return rows.filter((row) => {
      if (statusFilter) {
        const statusCols = config.columns.filter((c) => c.status);
        if (!statusCols.some((c) => String(row[c.key] ?? "") === statusFilter)) return false;
      }
      if (!q) return true;
      return config.columns.some((c) => {
        const v = row[c.key];
        return v !== null && v !== undefined && String(v).toLowerCase().includes(q);
      });
    });
  }, [rows, query, statusFilter, config.columns]);

  const pageCount = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const safePage = Math.min(page, pageCount);
  const pageRows = filtered.slice((safePage - 1) * PAGE_SIZE, safePage * PAGE_SIZE);

  async function runAction(action: ModuleAction, rowId?: string) {
    setBusy(true); setError(null);
    try {
      const path = rowId ? action.endpoint.replace("{id}", rowId) : action.endpoint;
      await api.postJson(path, action.body ?? {});
      setToast({ text: ar ? "تم التنفيذ بنجاح" : "Done" });
      load();
    } catch (e) {
      if (e instanceof TypeError) {
        queueOffline("record_action", "administrative", `${action.labelEn} on ${config.endpoint}`, action.body ?? {});
        setToast({ text: ar ? "لا اتصال — حُفظ الإجراء في طابور العمل دون اتصال وسيُزامَن عند عودة الشبكة" : "Offline — action queued for sync" });
      } else {
        setError(ar ? `فشل التنفيذ: ${e instanceof Error ? e.message : e}` : `Action failed: ${e instanceof Error ? e.message : e}`);
      }
    } finally { setBusy(false); }
  }

  async function submitCreate(ev: React.FormEvent) {
    ev.preventDefault();
    if (!config.createForm) return;
    setBusy(true); setError(null);
    const body: Record<string, unknown> = {};
    try {
      for (const f of config.createForm.fields) {
        const v = form[f.key] ?? "";
        body[f.key] = v === "true" ? true : v === "false" ? false : v !== "" && !Number.isNaN(Number(v)) && /^-?\d+(\.\d+)?$/.test(v) ? Number(v) : v;
      }
      await api.postJson(config.createForm.endpoint, body);
      setForm({});
      setDrawerOpen(false);
      setToast({ text: ar ? "تم الإنشاء بنجاح" : "Created" });
      load();
    } catch (e) {
      if (e instanceof TypeError) {
        queueOffline("create_record", "administrative", `create on ${config.createForm.endpoint}`, body);
        setForm({});
        setDrawerOpen(false);
        setToast({ text: ar ? "لا اتصال — حُفظ السجل في طابور العمل دون اتصال وسيُزامَن عند عودة الشبكة" : "Offline — record queued for sync" });
      } else {
        setError(ar ? `فشل الإنشاء: ${e instanceof Error ? e.message : e}` : `Create failed: ${e instanceof Error ? e.message : e}`);
      }
    } finally { setBusy(false); }
  }

  const columns: Column<Row>[] = config.columns.map((c) => ({
    key: c.key,
    header: ar ? c.ar : c.en,
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
      header: ar ? "إجراءات" : "Actions",
      render: (row) => (
        <span className="l-actions">
          {config.rowActions!.map((a) => (
            <button key={a.labelEn} className="btn btn-secondary btn-small" disabled={busy} onClick={() => runAction(a, row.id)}>
              {ar ? a.labelAr : a.labelEn}
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
        <p>{ar ? config.subtitleAr : config.subtitleEn}</p>
      </div>

      <div className="toolbar">
        <div className="field grow-field">
          <input
            className="input"
            value={query}
            onChange={(e) => { setQuery(e.target.value); setPage(1); }}
            placeholder={ar ? "بحث في السجلات…" : "Search records…"}
            aria-label={ar ? "بحث" : "Search"}
          />
        </div>
        {statusValues.length > 0 ? (
          <div className="field filter-field">
            <select
              className="input"
              value={statusFilter}
              onChange={(e) => { setStatusFilter(e.target.value); setPage(1); }}
              aria-label={ar ? "تصفية بالحالة" : "Filter by status"}
            >
              <option value="">{ar ? "كل الحالات" : "All statuses"}</option>
              {statusValues.map((v) => <option key={v} value={v}>{v}</option>)}
            </select>
          </div>
        ) : null}
        <button type="button" className="btn btn-secondary" onClick={load} disabled={loading}>
          {ar ? "تحديث" : "Refresh"}
        </button>
        {config.pageActions?.map((a) => (
          <button key={a.labelEn} type="button" className="btn btn-secondary" disabled={busy} onClick={() => runAction(a)}>
            {ar ? a.labelAr : a.labelEn}
          </button>
        ))}
        {config.createForm ? (
          <button type="button" className="btn" onClick={() => setDrawerOpen(true)}>
            ＋ {ar ? config.createForm.submitAr : config.createForm.submitEn}
          </button>
        ) : null}
        <span className="count-note">
          {ar ? `إجمالي ${filtered.length} سجل` : `${filtered.length} records`}
        </span>
      </div>

      {error ? (
        <div className="error-banner">
          {error}{" "}
          <button type="button" className="btn btn-small btn-secondary" onClick={load}>
            {ar ? "إعادة المحاولة" : "Retry"}
          </button>
        </div>
      ) : null}

      {loading ? (
        <div className="skel-stack">
          <div className="skeleton skel-row" />
          <div className="skeleton skel-row" />
          <div className="skeleton skel-row" />
          <div className="skeleton skel-row" />
        </div>
      ) : filtered.length === 0 ? (
        <div className="card">
          <div className="empty">
            {rows.length === 0
              ? ar ? "لا توجد سجلات بعد في هذه الوحدة" : "No records yet in this module"
              : ar ? "لا نتائج مطابقة للبحث أو التصفية الحالية" : "No records match the current search or filter"}
            {config.createForm && rows.length === 0 ? (
              <div className="l-mt">
                <button type="button" className="btn" onClick={() => setDrawerOpen(true)}>
                  ＋ {ar ? config.createForm.submitAr : config.createForm.submitEn}
                </button>
              </div>
            ) : null}
          </div>
        </div>
      ) : (
        <>
          <DataTable columns={columns} rows={pageRows} />
          {pageCount > 1 ? (
            <div className="pager">
              <button type="button" className="btn btn-secondary btn-small" disabled={safePage <= 1} onClick={() => setPage(safePage - 1)}>
                {ar ? "السابق" : "Previous"}
              </button>
              <span>{ar ? `صفحة ${safePage} من ${pageCount}` : `Page ${safePage} of ${pageCount}`}</span>
              <button type="button" className="btn btn-secondary btn-small" disabled={safePage >= pageCount} onClick={() => setPage(safePage + 1)}>
                {ar ? "التالي" : "Next"}
              </button>
            </div>
          ) : null}
        </>
      )}

      {drawerOpen && config.createForm ? (
        <div className="drawer-overlay" onClick={() => setDrawerOpen(false)}>
          <div className="drawer" onClick={(e) => e.stopPropagation()}>
            <h2>{ar ? config.createForm.submitAr : config.createForm.submitEn}</h2>
            <form onSubmit={submitCreate}>
              <div className="form-grid cols-1">
                {config.createForm.fields.map((f) => (
                  <div className="field" key={f.key}>
                    <label>{ar ? f.ar : f.en}</label>
                    <input value={form[f.key] ?? ""} onChange={(e) => setForm({ ...form, [f.key]: e.target.value })} />
                  </div>
                ))}
              </div>
              <div className="form-actions">
                <button type="submit" className="btn" disabled={busy}>
                  {busy ? "…" : ar ? config.createForm.submitAr : config.createForm.submitEn}
                </button>
                <button type="button" className="btn btn-ghost" onClick={() => setDrawerOpen(false)}>
                  {ar ? "إلغاء" : "Cancel"}
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}

      {toast ? (
        <div className="toast-stack">
          <div className={`toast ${toast.err ? "err" : ""}`}>{toast.text}</div>
        </div>
      ) : null}

      <p className="page-foot-note">
        {ar
          ? `المصدر: ${config.endpoint} — النواة Java/Spring Boot`
          : `Source: ${config.endpoint} — Java/Spring Boot core`}
      </p>
    </>
  );
}
