"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import KpiCard from "@/components/KpiCard";
import { Button, PageHead } from "@/components/ui";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";

interface Rule { pathPrefix: string; allowedRoles: string; domainAr?: string }

/** The live access matrix, visible as the governance fact it is:
 *  every governed domain, the roles allowed to write in it, and
 *  the semantics that bind the whole platform — writes are closed
 *  by default outside a domain's roles, reads are governed by the
 *  same domains, and oversight tiers read without writing. */
export default function AccessMatrixPage() {
  const { lang, t } = useLang();
  const ar = lang === "ar";
  const [rules, setRules] = useState<Rule[]>([]);
  const [q, setQ] = useState("");
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      const r = await api.getJson<{ rules: Rule[] }>("/api/access-matrix");
      setRules(r.rules ?? []);
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); }
  }, []);
  useEffect(() => { load(); }, [load]);

  const shown = useMemo(() => {
    const needle = q.trim().toLowerCase();
    if (!needle) return rules;
    return rules.filter((r) => r.pathPrefix.toLowerCase().includes(needle)
      || (r.domainAr ?? "").includes(q.trim())
      || r.allowedRoles.toLowerCase().includes(needle));
  }, [rules, q]);

  const adminOnly = rules.filter((r) => !r.allowedRoles).length;

  return (
    <div>
      <PageHead eyebrow={ar ? "حوكمة المنصة" : "Platform governance"} title={t("accessMatrix")}
        sub={ar ? "من يحق له أن يكتب ويقرأ أين — كما تُطبَّق فعليًا الآن: الكتابة خارج أدوار النطاق مرفوضة افتراضيًا، والقراءة محكومة بنفس النطاقات، ومستويات الإشراف (وطني/محافظة/مديرية/جهات رقابية) تقرأ ولا تكتب تشغيليًا، والأدمن الموسّع وأدمن المنظومة فوق المصفوفة."
               : "Who may write and read where — as actually enforced now: writes outside a domain's roles are refused by default, reads are governed by the same domains, oversight tiers read without operating, and the super-admin and platform admin sit above the matrix."}
        actions={<Button onClick={load}>{ar ? "تحديث" : "Refresh"}</Button>} />
      {error ? <div className="alert-error">{error}</div> : null}
      <div className="kpi-grid">
        <KpiCard label={ar ? "نطاقات محكومة" : "Governed domains"} value={rules.length || "—"} />
        <KpiCard label={ar ? "نطاقات للإدارة فقط" : "Admin-only domains"} value={adminOnly || "—"} />
        <KpiCard label={ar ? "معروض الآن" : "Shown"} value={shown.length || "—"} />
      </div>
      <div className="field">
        <input value={q} onChange={(e) => setQ(e.target.value)} placeholder={ar ? "ابحث بمسار أو نطاق أو دور…" : "Search by path, domain or role…"} />
      </div>
      <section className="card">
        <table className="tbl">
          <thead>
            <tr><th>{ar ? "مسار النطاق" : "Domain path"}</th><th>{ar ? "النطاق" : "Domain"}</th><th>{ar ? "الأدوار المسموحة" : "Allowed roles"}</th></tr>
          </thead>
          <tbody>
            {shown.map((r) => (
              <tr key={r.pathPrefix}>
                <td dir="ltr">{r.pathPrefix}</td>
                <td>{r.domainAr ?? "—"}</td>
                <td>{r.allowedRoles ? r.allowedRoles.split(",").map((x) => <span key={x} className="badge info">{x.trim()}</span>) : <span className="badge warn">{ar ? "الإدارة فقط" : "Admin tiers only"}</span>}</td>
              </tr>
            ))}
          </tbody>
        </table>
        {shown.length === 0 ? <div className="empty">{ar ? "لا نطاقات مطابقة" : "No matching domains"}</div> : null}
      </section>
    </div>
  );
}
