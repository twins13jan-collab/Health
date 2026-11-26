"use client";

import { useCallback, useEffect, useState } from "react";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";

interface Provider {
  provider: string; displayAr: string; displayEn: string; baseUrl: string;
  enabled: boolean; active: boolean; model?: string | null; defaultModel?: string | null;
  keyMasked?: string | null; hasKey: boolean; updatedBy?: string | null;
}
interface ListResp { providers: Provider[]; encryptionReady: boolean; localModel: string }

/** AI control panel (admins only): the full external provider
 *  group. Keys are write-only — entered here, stored AES-GCM
 *  encrypted on the server, and only ever shown masked. Exactly
 *  one provider is active; the local open-source model stays as
 *  the automatic fallback, and tool results are de-identified
 *  before anything reaches an external provider. */
export default function AiSettingsPage() {
  const { lang } = useLang();
  const ar = lang === "ar";
  const [data, setData] = useState<ListResp | null>(null);
  const [drafts, setDrafts] = useState<Record<string, { model: string; apiKey: string }>>({});
  const [testResult, setTestResult] = useState<Record<string, string>>({});
  const [error, setError] = useState<string | null>(null);
  const [toast, setToast] = useState<string | null>(null);
  const [busy, setBusy] = useState<string | null>(null);

  const showToast = (m: string) => { setToast(m); setTimeout(() => setToast(null), 3500); };

  const load = useCallback(async () => {
    try {
      const d = await api.getJson<ListResp>("/api/admin/ai/providers");
      setData(d);
      const dr: Record<string, { model: string; apiKey: string }> = {};
      d.providers.forEach((p) => { dr[p.provider] = { model: p.model ?? "", apiKey: "" }; });
      setDrafts(dr);
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); }
  }, []);
  useEffect(() => { load(); }, [load]);

  async function save(p: Provider, opts: { makeActive?: boolean; enabled?: boolean; clearKey?: boolean } = {}) {
    setBusy(p.provider);
    try {
      const d = drafts[p.provider] ?? { model: "", apiKey: "" };
      await api.putJson(`/api/admin/ai/providers/${p.provider}`, {
        model: d.model || undefined,
        apiKey: d.apiKey || undefined,
        makeActive: opts.makeActive ?? undefined,
        enabled: opts.enabled ?? undefined,
        clearKey: opts.clearKey ?? undefined,
      });
      showToast(ar ? `حُفظت إعدادات ${p.displayAr}` : `${p.displayEn} settings saved`);
      await load();
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); } finally { setBusy(null); }
  }

  async function test(p: Provider) {
    setBusy(p.provider);
    try {
      const r = await api.postJson<{ ok: boolean; message: string }>(`/api/admin/ai/providers/${p.provider}/test`, {});
      setTestResult((t) => ({ ...t, [p.provider]: r.message }));
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); } finally { setBusy(null); }
  }

  return (
    <div>
      <div className="page-head">
        <h1>🧠 {ar ? "إعدادات الذكاء الاصطناعي" : "AI Settings"}</h1>
        <p>{ar
          ? "مجموعة المزودين الخارجيين كاملة: أدخل مفتاح كل مزود هنا فيُحفظ مشفرًا على الخادم ولا يظهر إلا مقنّعًا، وفعّل مزودًا واحدًا ليكون مسار المساعد الأساسي. النموذج المحلي المفتوح المصدر يبقى احتياطيًا تلقائيًا، ونتائج الأدوات تُنقّى من معرّفات المرضى قبل أن تصل لأي مزود خارجي."
          : "The full external provider group: enter each provider's key here — it is stored encrypted on the server and only ever shown masked — and activate one as the assistant's primary route. The local open-source model remains the automatic fallback, and tool results are de-identified before reaching any external provider."}</p>
      </div>
      {error && <div className="error-banner">{error} <button type="button" className="btn btn-small btn-secondary" onClick={() => setError(null)}>{ar ? "إغلاق" : "Dismiss"}</button></div>}
      {toast && <div className="toast-stack"><div className="toast">{toast}</div></div>}
      {data && !data.encryptionReady && (
        <div className="error-banner">{ar ? "مفتاح تشفير الخادم غير مهيأ — لن تُحفظ أي مفاتيح حتى يُضبط APP_SETTINGS_KEY على الخادم." : "The server encryption key is not configured — no keys will be stored until APP_SETTINGS_KEY is set on the server."}</div>
      )}
      {data && (
        <div className="card">
          <strong>{ar ? "النموذج المحلي (احتياطي دائم)" : "Local model (permanent fallback)"}</strong>
          <p className="muted mb-0">{data.localModel} — Ollama {ar ? "داخل بنية المنظومة، يعمل عند تعذر المزود الخارجي النشط" : "inside the deployment; serves whenever the active external provider is unreachable"}</p>
        </div>
      )}
      <div className="l-stack">
        {data?.providers.map((p) => (
          <div className="card" key={p.provider}>
            <div className="l-row-between">
              <h2 className="m-0">{ar ? p.displayAr : p.displayEn}</h2>
              <span className="l-actions">
                {p.active && <span className="badge ok">{ar ? "النشط" : "Active"}</span>}
                <span className={`badge ${p.enabled ? "ok" : "muted"}`}>{p.enabled ? (ar ? "مفعّل" : "Enabled") : (ar ? "معطّل" : "Disabled")}</span>
                <span className={`badge ${p.hasKey ? "ok" : "warn"}`}>{p.hasKey ? (ar ? "مفتاح محفوظ" : "Key stored") : (ar ? "بلا مفتاح" : "No key")}</span>
              </span>
            </div>
            <p className="muted text-start" dir="ltr">{p.baseUrl}</p>
            <div className="form-grid">
              <div className="field">
                <label>{ar ? "النموذج" : "Model"}</label>
                <input dir="ltr" value={drafts[p.provider]?.model ?? ""} placeholder={p.defaultModel ?? ""}
                  onChange={(e) => setDrafts((d) => ({ ...d, [p.provider]: { ...(d[p.provider] ?? { apiKey: "" }), model: e.target.value } }))} />
              </div>
              <div className="field">
                <label>{ar ? "مفتاح API (يُحفظ مشفرًا — اتركه فارغًا للإبقاء على الحالي)" : "API key (stored encrypted — leave blank to keep the current one)"}</label>
                <input dir="ltr" type="password" autoComplete="new-password" value={drafts[p.provider]?.apiKey ?? ""}
                  placeholder={p.keyMasked ?? (ar ? "الصق المفتاح هنا" : "Paste the key here")}
                  onChange={(e) => setDrafts((d) => ({ ...d, [p.provider]: { ...(d[p.provider] ?? { model: "" }), apiKey: e.target.value } }))} />
                {p.keyMasked && <span className="muted" dir="ltr">{p.keyMasked}</span>}
              </div>
            </div>
            {testResult[p.provider] && <p className="muted">{testResult[p.provider]}</p>}
            <div className="l-row mt-8">
              <button type="button" className="btn btn-small" disabled={busy === p.provider} onClick={() => save(p)}>{ar ? "حفظ" : "Save"}</button>
              <button type="button" className="btn btn-small btn-secondary" disabled={busy === p.provider || !p.hasKey} onClick={() => test(p)}>{ar ? "اختبار الاتصال" : "Test connection"}</button>
              {!p.active && <button type="button" className="btn btn-small btn-secondary" disabled={busy === p.provider || !p.hasKey} onClick={() => save(p, { makeActive: true })}>{ar ? "جعله النشط" : "Make active"}</button>}
              <button type="button" className="btn btn-small btn-secondary" disabled={busy === p.provider} onClick={() => save(p, { enabled: !p.enabled })}>{p.enabled ? (ar ? "تعطيل" : "Disable") : (ar ? "تفعيل" : "Enable")}</button>
              {p.hasKey && <button type="button" className="btn btn-small btn-danger" disabled={busy === p.provider} onClick={() => save(p, { clearKey: true })}>{ar ? "مسح المفتاح" : "Clear key"}</button>}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
