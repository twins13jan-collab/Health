"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";

/** The central admin control panel: one hub for every
 *  administrative surface — accounts, facilities and registration
 *  approvals, AI providers, reference data, and the audit trail. */
export default function AdminPanelPage() {
  const { lang } = useLang();
  const ar = lang === "ar";
  const [pending, setPending] = useState<number | null>(null);
  const [usersCount, setUsersCount] = useState<number | null>(null);

  useEffect(() => {
    api.getJson<{ pending: number }>("/api/admin/facilities").then((r) => setPending(r.pending)).catch(() => setPending(null));
    api.getJson<{ total?: number; count?: number }>("/api/admin/users").then((r) => setUsersCount(r.total ?? r.count ?? null)).catch(() => setUsersCount(null));
  }, []);

  const cards = [
    { href: "/admin/facilities", icon: "🏥", ar: "المنشآت والتسجيلات", en: "Facilities & registrations",
      dAr: "اعتماد المنشآت المسجلة ذاتيًا، وتفعيل/إيقاف أي منشأة وتعديل حالتها.", dEn: "Approve self-registered facilities; activate, suspend and manage every facility.",
      badge: pending ? `${pending} ${ar ? "بانتظار الاعتماد" : "pending"}` : null },
    { href: "/admin/users", icon: "⚿", ar: "الحسابات والمستخدمون", en: "Accounts & users",
      dAr: "إنشاء الحسابات وتعديلها وتفعيلها وتعطيلها وإعادة تعيين كلمات المرور لكل الأدوار والمستويات.", dEn: "Create, edit, activate, disable and reset accounts for every role and level.",
      badge: usersCount != null ? `${usersCount}` : null },
    { href: "/admin/ai", icon: "🧠", ar: "الذكاء الاصطناعي والمزودون", en: "AI & providers",
      dAr: "مفاتيح المزودين الخارجيين والنماذج والمزود النشط — تُدار مشفرة من اللوحة.", dEn: "External provider keys, models and the active provider — managed encrypted from the panel.", badge: null },
    { href: "/master-data", icon: "▸", ar: "البيانات الرئيسية والمرجعية", en: "Master & reference data",
      dAr: "المحافظات والأدلة والأكواد المرجعية الوطنية التي تعتمد عليها كل الوحدات.", dEn: "Governorates, directories and national reference codes every module relies on.", badge: null },
    { href: "/governance", icon: "▸", ar: "الحوكمة والموافقات", en: "Governance & approvals",
      dAr: "سياسات وموافقات ولجان المنظومة عبر كل المستويات.", dEn: "Policies, approvals and committees across all levels.", badge: null },
    { href: "/audit-compliance", icon: "▸", ar: "التدقيق والامتثال", en: "Audit & compliance",
      dAr: "سجل التدقيق الكامل لكل إجراء في المنظومة وفحوص الامتثال.", dEn: "The full audit trail of every action in the system, and compliance checks.", badge: null },
  ];

  return (
    <div>
      <div className="page-head">
        <h1>⚙ {ar ? "لوحة التحكم الإدارية" : "Admin Control Panel"}</h1>
        <p>{ar ? "مركز إدارة المنظومة كاملة: أي بيان يمكن إضافته أو تعديله من هنا — الحسابات والمنشآت والاعتمادات والإعدادات والبيانات المرجعية." : "The whole system is managed from here — any record can be added or edited: accounts, facilities, approvals, settings and reference data."}</p>
      </div>
      <div className="finder-grid">
        {cards.map((c) => (
          <div key={c.href} className="finder-card">
            <strong>{c.icon} {ar ? c.ar : c.en}</strong>
            {c.badge && <span className="badge warn ms-8">{c.badge}</span>}
            <p className="muted">{ar ? c.dAr : c.dEn}</p>
            <Link className="btn btn-small" href={c.href}>{ar ? "فتح" : "Open"}</Link>
          </div>
        ))}
      </div>
    </div>
  );
}
