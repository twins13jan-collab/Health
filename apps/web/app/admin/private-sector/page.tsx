"use client";

import { useCallback, useEffect, useState } from "react";
import KpiCard from "@/components/KpiCard";
import { Button, Drawer, PageHead, Toasts } from "@/components/ui";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";
import { facilityTypeLabel } from "@/lib/types";

interface FacRow { id: string; name_ar: string; name_en?: string; type: string; type_group: string; governorate: string; city?: string; status: string; bed_capacity?: number; license_no?: string; license_authority?: string; license_expiry?: string; license_state: string; days_to_expiry?: number }
interface BoardResp { facilities: FacRow[]; count: number; summary: { total: number; by_group: Record<string, number>; pending: number; active: number; suspended: number; license_expired: number; license_expiring_soon: number; license_not_recorded: number } }

const LIC_TONE: Record<string, string> = { expired: "danger", expiring_soon: "warn", not_recorded: "warn", valid: "ok" };
const LIC_LABEL: Record<string, { ar: string; en: string }> = {
  expired: { ar: "ترخيص منتهٍ", en: "Licence expired" },
  expiring_soon: { ar: "ترخيص يقترب من الانتهاء", en: "Licence expiring soon" },
  not_recorded: { ar: "ترخيص غير مسجل", en: "Licence not recorded" },
  valid: { ar: "ترخيص سارٍ", en: "Licence valid" },
};
const STATUS_TONE: Record<string, string> = { pending: "warn", active: "ok", suspended: "danger" };
const GROUP_LABEL: Record<string, { ar: string; en: string }> = {
  hospitals: { ar: "مستشفيات خاصة", en: "Private hospitals" },
  clinics: { ar: "عيادات خاصة", en: "Private clinics" },
  labs: { ar: "معامل خاصة", en: "Private laboratories" },
  pharmacies: { ar: "صيدليات خاصة", en: "Private pharmacies" },
  centers: { ar: "مراكز طبية خاصة", en: "Private medical centres" },
  other: { ar: "منشآت خاصة أخرى", en: "Other private facilities" },
};

/** Private-sector management console: every privately owned facility
 *  on the platform — hospitals, clinics, labs, pharmacies, medical
 *  centres — grouped by family and ranked so expired licences and
 *  pending approvals come first. Approving a facility activates its
 *  manager account and with it the full operational system for its
 *  type (hospital operations, clinics, labs, pharmacy…). */
export default function PrivateSectorPage() {
  const { lang, t } = useLang();
  const ar = lang === "ar";
  const [board, setBoard] = useState<BoardResp | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [toasts, setToasts] = useState<{ text: string; err?: boolean }[]>([]);
  const [busy, setBusy] = useState(false);
  const [licFor, setLicFor] = useState<FacRow | null>(null);
  const [lic, setLic] = useState({ licenseNo: "", licenseAuthority: "", licenseExpiry: "" });
  const [groupFilter, setGroupFilter] = useState<string>("all");

  const toast = (text: string, err?: boolean) => { setToasts((x) => [...x, { text, err }]); setTimeout(() => setToasts((x) => x.slice(1)), 4500); };

  const load = useCallback(async () => {
    try { setBoard(await api.getJson<BoardResp>("/api/admin/private-sector/board")); }
    catch (e) { setError(e instanceof Error ? e.message : String(e)); }
  }, []);
  useEffect(() => { load(); }, [load]);

  async function act(fn: () => Promise<unknown>, okMsg: string) {
    setBusy(true);
    try { await fn(); toast(okMsg); await load(); }
    catch (e) { toast(e instanceof Error ? e.message : String(e), true); }
    finally { setBusy(false); }
  }

  const s = board?.summary;
  const g = (k: string) => s?.by_group?.[k] ?? 0;
  const rows = (board?.facilities ?? []).filter((f) => groupFilter === "all" || f.type_group === groupFilter);
  const licLabel = (x: string) => (ar ? LIC_LABEL[x]?.ar ?? x : LIC_LABEL[x]?.en ?? x);
  const groupLabel = (x: string) => (ar ? GROUP_LABEL[x]?.ar ?? x : GROUP_LABEL[x]?.en ?? x);

  return (
    <div>
      <PageHead eyebrow={ar ? "إدارة المنصة" : "Platform administration"} title={t("privateSector")}
        sub={ar ? "كل منشآت القطاع الخاص على المنظومة — مستشفيات وعيادات ومعامل وصيدليات ومراكز طبية — مجمعة بعائلاتها ومرتبة بحيث تتصدر التراخيص المنتهية وطلبات الاعتماد. اعتماد المنشأة يفعّل حساب مديرها ومعه نظام التشغيل الكامل لنوعها."
               : "Every private facility on the platform — hospitals, clinics, labs, pharmacies, medical centres — grouped by family, with expired licences and pending approvals first. Approval activates the manager account and the full operational system for that facility type."}
        actions={<Button onClick={load}>{ar ? "تحديث" : "Refresh"}</Button>} />
      {error ? <div className="alert-error">{error}</div> : null}
      <div className="kpi-grid">
        <KpiCard label={groupLabel("hospitals")} value={g("hospitals")} />
        <KpiCard label={groupLabel("clinics")} value={g("clinics")} />
        <KpiCard label={groupLabel("labs")} value={g("labs")} />
        <KpiCard label={groupLabel("pharmacies")} value={g("pharmacies")} />
        <KpiCard label={groupLabel("centers")} value={g("centers")} />
        <KpiCard label={groupLabel("other")} value={g("other")} />
      </div>
      <div className="kpi-grid">
        <KpiCard label={ar ? "بانتظار الاعتماد" : "Pending approval"} value={s?.pending ?? "—"} />
        <KpiCard label={ar ? "عاملة" : "Active"} value={s?.active ?? "—"} />
        <KpiCard label={ar ? "موقوفة" : "Suspended"} value={s?.suspended ?? "—"} />
        <KpiCard label={ar ? "تراخيص منتهية" : "Licences expired"} value={s?.license_expired ?? "—"} />
        <KpiCard label={ar ? "تراخيص تقترب من الانتهاء" : "Licences expiring"} value={s?.license_expiring_soon ?? "—"} />
        <KpiCard label={ar ? "ترخيص غير مسجل" : "Licence not recorded"} value={s?.license_not_recorded ?? "—"} />
      </div>

      <div className="l-row">
        <button className={`badge ${groupFilter === "all" ? "gold" : "info"}`} onClick={() => setGroupFilter("all")}>{ar ? "الكل" : "All"}</button>
        {Object.keys(GROUP_LABEL).map((k) => (
          <button key={k} className={`badge ${groupFilter === k ? "gold" : "info"}`} onClick={() => setGroupFilter(k)}>{groupLabel(k)} · {g(k)}</button>
        ))}
      </div>

      {!board ? <div className="empty">{ar ? "جارٍ التحميل…" : "Loading…"}</div>
        : rows.length === 0 ? <div className="empty">{ar ? "لا منشآت خاصة في هذه المجموعة بعد" : "No private facilities in this group yet"}</div>
        : (
          <div className="cards-grid">
            {rows.map((f) => (
              <section key={f.id} className="card">
                <div className="l-row">
                  <strong>{f.name_ar}</strong>
                  <span className={`badge ${STATUS_TONE[f.status] ?? "info"}`}>{f.status}</span>
                  <span className="badge info">{facilityTypeLabel(f.type, lang)}</span>
                </div>
                <div className="l-row">
                  <span className={`badge ${LIC_TONE[f.license_state] ?? "info"}`}>{licLabel(f.license_state)}{f.license_expiry ? ` · ${f.license_expiry}` : ""}{typeof f.days_to_expiry === "number" && f.license_state !== "expired" ? ` (${f.days_to_expiry} ${ar ? "يوم" : "d"})` : ""}</span>
                  {f.license_no ? <span className="badge info">{ar ? "ترخيص" : "Lic."} {f.license_no}</span> : null}
                  <span className="badge info">{f.governorate}{f.city ? ` · ${f.city}` : ""}</span>
                  {f.bed_capacity ? <span className="badge info">{f.bed_capacity} {ar ? "سرير" : "beds"}</span> : null}
                </div>
                <div className="l-actions">
                  {f.status === "pending" ? (
                    <Button disabled={busy} onClick={() => act(() => api.postJson(`/api/admin/facilities/${f.id}/approve`, {}),
                      ar ? "تم اعتماد المنشأة وتفعيل حساب مديرها" : "Facility approved; manager activated")}>{ar ? "اعتماد وتفعيل" : "Approve & activate"}</Button>
                  ) : null}
                  {f.status === "active" ? (
                    <Button disabled={busy} onClick={() => act(() => api.postJson(`/api/admin/facilities/${f.id}/status`, { status: "suspended" }),
                      ar ? "تم إيقاف المنشأة" : "Facility suspended")}>{ar ? "إيقاف" : "Suspend"}</Button>
                  ) : null}
                  {f.status === "suspended" ? (
                    <Button disabled={busy} onClick={() => act(() => api.postJson(`/api/admin/facilities/${f.id}/status`, { status: "active" }),
                      ar ? "تمت إعادة تفعيل المنشأة" : "Facility reactivated")}>{ar ? "إعادة تفعيل" : "Reactivate"}</Button>
                  ) : null}
                  <Button disabled={busy} onClick={() => { setLicFor(f); setLic({ licenseNo: f.license_no ?? "", licenseAuthority: f.license_authority ?? "", licenseExpiry: f.license_expiry ?? "" }); }}>
                    {ar ? "بيانات الترخيص" : "Licence data"}
                  </Button>
                </div>
              </section>
            ))}
          </div>
        )}

      {licFor ? (
        <Drawer title={`${licFor.name_ar} — ${ar ? "بيانات الترخيص" : "Licence data"}`} onClose={() => setLicFor(null)}>
          <div className="form-grid">
            <div className="field"><label>{ar ? "رقم الترخيص" : "Licence no."}</label><input value={lic.licenseNo} onChange={(e) => setLic({ ...lic, licenseNo: e.target.value })} /></div>
            <div className="field"><label>{ar ? "جهة الترخيص" : "Licensing authority"}</label><input value={lic.licenseAuthority} onChange={(e) => setLic({ ...lic, licenseAuthority: e.target.value })} /></div>
            <div className="field"><label>{ar ? "تاريخ الانتهاء" : "Expiry date"}</label><input type="date" value={lic.licenseExpiry} onChange={(e) => setLic({ ...lic, licenseExpiry: e.target.value })} /></div>
          </div>
          <div className="form-actions">
            <Button disabled={busy} onClick={() => act(async () => {
              await api.postJson(`/api/admin/private-sector/${licFor.id}/license`, { licenseNo: lic.licenseNo || null, licenseAuthority: lic.licenseAuthority || null, licenseExpiry: lic.licenseExpiry || null });
              setLicFor(null);
            }, ar ? "تم تحديث بيانات الترخيص" : "Licence updated")}>{ar ? "حفظ الترخيص" : "Save licence"}</Button>
          </div>
        </Drawer>
      ) : null}

      <Toasts items={toasts} />
    </div>
  );
}
