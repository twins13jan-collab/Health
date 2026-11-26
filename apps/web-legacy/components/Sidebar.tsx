"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { api, getSessionUser, type SessionUser } from "@/lib/api";
import { useEffect, useState } from "react";
import { useLang, type DictKey } from "@/lib/i18n";
import { NAV, NAV_SECTIONS, sectionOf } from "@/lib/nav";


export default function Sidebar() {
  const pathname = usePathname();
  const router = useRouter();
  const { lang, setLang, t } = useLang();
  const [user, setUser] = useState<SessionUser | null>(null);
  useEffect(() => { setUser(getSessionUser()); }, [pathname]);
  const isAdmin = user?.role === "admin" || user?.role === "national_admin";

  if (["/login", "/register", "/", "/about", "/contact", "/privacy", "/download-app", "/services"].includes(pathname)) return null;

  return (
    <aside className="sidebar">
      <div className="brand">
        <div className="brand-mark"><img src="/emblem.png" alt="خطاب" width={46} height={46} /></div>
        <div className="brand-name">{t("appName")}</div>
        <div className="brand-sub">{lang === "ar" ? "Khattab for Public Health & Crisis Management" : "خطاب للصحة العامة وإدارة الأزمات"}</div>
      </div>
      <nav className="nav">
        {[...NAV_SECTIONS.map((s) => s.id), "all"].map((sectionId) => {
          const items = NAV.filter((item) => (!item.adminOnly || isAdmin) && sectionOf(item.href).id === sectionId);
          if (!items.length) return null;
          const section = NAV_SECTIONS.find((s) => s.id === sectionId);
          return (
            <div key={sectionId}>
              <div className="nav-section">{section ? (lang === "ar" ? section.ar : section.en) : lang === "ar" ? "كل الوحدات" : "All Modules"}</div>
              {items.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`nav-item ${pathname === item.href ? "nav-item-active" : ""}`}
                  onClick={() => document.body.classList.remove("nav-open")}
                >
                  <span aria-hidden className="nav-ico">{item.icon}</span> {t(item.key as DictKey)}
                </Link>
              ))}
            </div>
          );
        })}
      </nav>
      {user ? (
        <div className="user-chip">
          <div className="user-name">{user.fullName || user.email}</div>
          <div className="user-role">{user.role}{user.email ? ` · ${user.email}` : ""}</div>
        </div>
      ) : null}
      <div className="sidebar-foot">
        <button
          type="button"
          className="btn btn-ghost btn-block"
          onClick={() => setLang(lang === "ar" ? "en" : "ar")}
        >
          {lang === "ar" ? "English" : "العربية"}
        </button>
        <button
          type="button"
          className="btn btn-ghost btn-block"
          onClick={() => api.logout()}
        >
          {t("logout")}
        </button>
      </div>
    </aside>
  );
}
