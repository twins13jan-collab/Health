"use client";

import { useCallback, useEffect, useState } from "react";
import KpiCard from "@/components/KpiCard";
import { Button, PageHead, Toasts } from "@/components/ui";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";

interface SettingRow { key: string; type: string; group: string; public: boolean; value: string | null }

const LABELS: Record<string, { ar: string; en: string }> = {
  "site.name_ar": { ar: "اسم المنظومة (عربي)", en: "Platform name (Arabic)" },
  "site.name_en": { ar: "اسم المنظومة (إنجليزي)", en: "Platform name (English)" },
  "site.descriptor": { ar: "الوصف الرسمي للمنظومة", en: "Official descriptor" },
  "site.contact_name": { ar: "اسم مسؤول التواصل", en: "Contact name" },
  "site.contact_phone": { ar: "هاتف التواصل", en: "Contact phone" },
  "site.contact_whatsapp": { ar: "واتساب التواصل", en: "Contact WhatsApp" },
  "security.lockout_max_failures": { ar: "محاولات الدخول الفاشلة قبل قفل الحساب", en: "Failed logins before lockout" },
  "security.lockout_minutes": { ar: "مدة قفل الحساب (دقيقة)", en: "Lockout duration (minutes)" },
  "security.password_min_length": { ar: "الحد الأدنى لطول كلمة المرور", en: "Minimum password length" },
  "registration.patient_enabled": { ar: "تسجيل المواطنين والمرضى", en: "Citizen / patient registration" },
  "registration.facility_enabled": { ar: "تسجيل المنشآت", en: "Facility registration" },
  "system.maintenance_mode": { ar: "وضع الصيانة (إيقاف الكتابات)", en: "Maintenance mode (writes paused)" },
  "system.maintenance_message": { ar: "رسالة الصيانة", en: "Maintenance message" },
  "public.show_capacity": { ar: "إظهار السعة السريرية في الموقع العام", en: "Show capacity on the public site" },
};
const GROUPS: { id: string; ar: string; en: string }[] = [
  { id: "identity", ar: "هوية المنظومة", en: "Platform identity" },
  { id: "contact", ar: "بيانات التواصل الرسمية", en: "Official contact" },
  { id: "security", ar: "الأمان وكلمات المرور", en: "Security & passwords" },
  { id: "registration", ar: "مفاتيح التسجيل", en: "Registration switches" },
  { id: "system", ar: "النظام والصيانة", en: "System & maintenance" },
  { id: "public", ar: "الموقع العام", en: "Public website" },
];

/** Platform settings & variables — the super-admin's control
 *  panel. Every value here is live: lockout thresholds and the
 *  password policy read them at sign-in, the registration switches
 *  gate the public sign-up doors, maintenance mode pauses writes
 *  platform-wide, and the public website renders the identity and
 *  contact values. Reserved to the super-admin tier. */
export default function AdminSettingsPage() {
  const { lang, t } = useLang();
  const ar = lang === "ar";
  const [rows, setRows] = useState<SettingRow[]>([]);
  const [draft, setDraft] = useState<Record<string, string>>({});
  const [error, setError] = useState<string | null>(null);
  const [toasts, setToasts] = useState<{ text: string; err?: boolean }[]>([]);
  const [busy, setBusy] = useState(false);

  const toast = (text: string, err?: boolean) => { setToasts((x) => [...x, { text, err }]); setTimeout(() => setToasts((x) => x.slice(1)), 4500); };

  const load = useCallback(async () => {
    try {
      const r = await api.getJson<{ settings: SettingRow[] }>("/api/admin/settings");
      setRows(r.settings);
      setDraft(Object.fromEntries(r.settings.map((s) => [s.key, s.value ?? ""])));
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); }
  }, []);
  useEffect(() => { load(); }, [load]);

  async function save(key: string) {
    setBusy(true);
    try {
      await api.putJson(`/api/admin/settings/${encodeURIComponent(key)}`, { value: draft[key] ?? "" });
      toast(ar ? "تم حفظ الإعداد وتفعيله" : "Setting saved and live");
      await load();
    } catch (e) { toast(e instanceof Error ? e.message : String(e), true); }
    finally { setBusy(false); }
  }

  const label = (k: string) => (ar ? LABELS[k]?.ar ?? k : LABELS[k]?.en ?? k);

  return (
    <div>
      <PageHead eyebrow={ar ? "إدارة المنصة" : "Platform administration"} title={t("settingsAdmin")}
        sub={ar ? "كل إعدادات ومتغيرات المنظومة من مكان واحد — الهوية والتواصل والأمان والتسجيل والصيانة والموقع العام. القيم حيّة: تُطبق فور حفظها دون إعادة تشغيل، وهذه اللوحة للأدمن الموسّع فقط."
               : "Every platform setting and variable in one place — identity, contact, security, registration, maintenance and the public site. Values are live the moment they are saved, and this panel belongs to the super-admin tier alone."}
        actions={<Button onClick={load}>{ar ? "تحديث" : "Refresh"}</Button>} />
      {error ? <div className="alert-error">{error}</div> : null}
      <div className="kpi-grid">
        <KpiCard label={ar ? "إعدادات مُدارة" : "Managed settings"} value={rows.length || "—"} />
        <KpiCard label={ar ? "وضع الصيانة" : "Maintenance mode"} value={draft["system.maintenance_mode"] === "true" ? (ar ? "مفعّل" : "ON") : (ar ? "متوقف" : "OFF")} />
        <KpiCard label={ar ? "تسجيل المواطنين" : "Citizen registration"} value={draft["registration.patient_enabled"] === "true" ? (ar ? "مفتوح" : "Open") : (ar ? "مغلق" : "Closed")} />
        <KpiCard label={ar ? "تسجيل المنشآت" : "Facility registration"} value={draft["registration.facility_enabled"] === "true" ? (ar ? "مفتوح" : "Open") : (ar ? "مغلق" : "Closed")} />
      </div>
      {GROUPS.map((g) => {
        const items = rows.filter((r) => r.group === g.id);
        if (!items.length) return null;
        return (
          <section key={g.id} className="card">
            <h3>{ar ? g.ar : g.en}</h3>
            {items.map((s) => (
              <div key={s.key} className="l-row">
                <span>{label(s.key)}{s.public ? <span className="badge info">{ar ? "يظهر للعامة" : "public"}</span> : null}</span>
                {s.type === "bool" ? (
                  <select value={draft[s.key] ?? "false"} onChange={(e) => setDraft({ ...draft, [s.key]: e.target.value })}>
                    <option value="true">{ar ? "مفعّل" : "Enabled"}</option>
                    <option value="false">{ar ? "معطّل" : "Disabled"}</option>
                  </select>
                ) : s.type === "int" ? (
                  <input type="number" dir="ltr" style={{ maxWidth: 140 }} value={draft[s.key] ?? ""} onChange={(e) => setDraft({ ...draft, [s.key]: e.target.value })} />
                ) : (
                  <input style={{ minWidth: 260 }} value={draft[s.key] ?? ""} onChange={(e) => setDraft({ ...draft, [s.key]: e.target.value })} />
                )}
                <Button disabled={busy || draft[s.key] === (s.value ?? "")} onClick={() => save(s.key)}>{ar ? "حفظ" : "Save"}</Button>
              </div>
            ))}
          </section>
        );
      })}
      <Toasts items={toasts} />
    </div>
  );
}
