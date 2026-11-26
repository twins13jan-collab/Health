"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useMemo, useRef, useState, type ReactNode } from "react";
import { api, getSessionUser, type SessionUser } from "@/lib/api";
import { useLang, type DictKey } from "@/lib/i18n";
import { NAV, NAV_SECTIONS, sectionOf } from "@/lib/nav";

/**
 * The application shell (v3) — the ONLY chrome authenticated routes
 * get: a white sidebar with small-caps section headings and a soft
 * green pill (gold dot) for the active item, a topbar with a
 * breadcrumb-style title, global quick-nav search and a user chip,
 * and a slim footer line. Content sits on the paper background.
 */

export function Sidebar() {
  const pathname = usePathname();
  const { lang, setLang, t } = useLang();
  const [user, setUser] = useState<SessionUser | null>(null);
  useEffect(() => { setUser(getSessionUser()); }, [pathname]);
  const isAdmin = user?.role === "super_admin" || user?.role === "admin" || user?.role === "national_admin";
  const isSuper = user?.role === "super_admin";

  return (
    <aside className="side">
      <div className="side-brand">
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img src="/emblem.png" alt="خطاب" width={46} height={46} />
        <div>
          <div className="sb-name">{t("appName")}</div>
          <div className="sb-sub">
            {lang === "ar" ? "Khattab for Public Health & Crisis Management" : "خطاب للصحة العامة وإدارة الأزمات"}
          </div>
        </div>
      </div>
      <nav className="side-nav">
        {[...NAV_SECTIONS.map((s) => s.id), "all"].map((sectionId) => {
          const sectionDef = NAV_SECTIONS.find((s) => s.id === sectionId);
          const oversight = isAdmin || user?.role === "governorate_admin" || user?.role === "directorate_admin" || user?.role === "authority_admin";
          const sectionVisible = oversight || !sectionDef?.roles || (user?.role ? sectionDef.roles.includes(user.role) : false);
          const itemVisible = (item: (typeof NAV)[number]) => {
            if (item.superOnly) return isSuper;
            if (item.roles) return isAdmin || (user?.role ? item.roles.includes(user.role) : false);
            return !item.adminOnly || isAdmin;
          };
          const items = sectionVisible
            ? NAV.filter((item) => itemVisible(item) && sectionOf(item.href).id === sectionId
                && !(user?.role === "patient" && item.href === "/dashboard"))
            : [];
          if (!items.length) return null;
          const section = NAV_SECTIONS.find((s) => s.id === sectionId);
          return (
            <div key={sectionId}>
              <div className="side-sec">
                {section ? (lang === "ar" ? section.ar : section.en) : lang === "ar" ? "كل الوحدات" : "All Modules"}
              </div>
              {items.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`side-item${pathname === item.href ? " on" : ""}`}
                  onClick={() => document.body.classList.remove("nav-open")}
                >
                  <span aria-hidden className="si-ico">{item.icon}</span> {t(item.key as DictKey)}
                </Link>
              ))}
            </div>
          );
        })}
      </nav>
      {user ? (
        <div className="side-user">
          <div className="su-name">{user.fullName || user.email}</div>
          <div className="su-meta">{user.role}{user.email ? ` · ${user.email}` : ""}</div>
        </div>
      ) : null}
      <div className="side-foot">
        <button type="button" className="btn btn-ghost" onClick={() => setLang(lang === "ar" ? "en" : "ar")}>
          {lang === "ar" ? "English" : "العربية"}
        </button>
        <button type="button" className="btn btn-ghost" onClick={() => api.logout()}>
          {t("logout")}
        </button>
      </div>
    </aside>
  );
}

export function Topbar() {
  const pathname = usePathname();
  const { lang, setLang, t } = useLang();
  const [user, setUser] = useState<SessionUser | null>(null);
  const [query, setQuery] = useState("");
  const [open, setOpen] = useState(false);
  const searchRef = useRef<HTMLDivElement>(null);

  useEffect(() => { setUser(getSessionUser()); }, [pathname]);
  useEffect(() => {
    const onClick = (e: MouseEvent) => {
      if (searchRef.current && !searchRef.current.contains(e.target as Node)) setOpen(false);
    };
    document.addEventListener("mousedown", onClick);
    return () => document.removeEventListener("mousedown", onClick);
  }, []);

  const results = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return [];
    return NAV.filter((item) => {
      const label = t(item.key as DictKey);
      return label.toLowerCase().includes(q) || item.href.toLowerCase().includes(q);
    }).slice(0, 9);
  }, [query, t]);

  const current = NAV.find((item) => item.href === pathname);
  const section = current ? sectionOf(current.href) : null;
  const context = user
    ? [user.governorate, user.directorate].filter(Boolean).join(" · ") || (lang === "ar" ? "نطاق وطني" : "National scope")
    : "";

  return (
    <header className="topbar">
      <button
        type="button"
        className="icon-btn menu-btn"
        aria-label="القائمة"
        onClick={() => document.body.classList.toggle("nav-open")}
      >
        ☰
      </button>
      <div className="crumb">
        <div className="crumb-sec">
          {section ? (lang === "ar" ? section.ar : section.en) : lang === "ar" ? "منظومة خطاب" : "Khattab"}
          {context ? ` · ${context}` : ""}
        </div>
        <div className="crumb-page">{current ? t(current.key as DictKey) : t("appName")}</div>
      </div>
      <div className="spacer" />
      <div className="quick-search" ref={searchRef}>
        <input
          value={query}
          onChange={(e) => { setQuery(e.target.value); setOpen(true); }}
          onFocus={() => setOpen(true)}
          placeholder={lang === "ar" ? "تنقّل سريع إلى أي وحدة…" : "Jump to any module…"}
          aria-label={lang === "ar" ? "بحث الوحدات" : "Module search"}
        />
        {open && results.length > 0 ? (
          <div className="search-pop">
            {results.map((item) => (
              <Link key={item.href} href={item.href} onClick={() => { setOpen(false); setQuery(""); }}>
                <span aria-hidden>{item.icon}</span> {t(item.key as DictKey)}
              </Link>
            ))}
          </div>
        ) : null}
      </div>
      <button
        type="button"
        className="icon-btn"
        aria-label={lang === "ar" ? "Switch to English" : "التبديل إلى العربية"}
        onClick={() => setLang(lang === "ar" ? "en" : "ar")}
      >
        {lang === "ar" ? "EN" : "ع"}
      </button>
      <Link href="/account" className="user-chip" title={user ? `${user.fullName || user.email} — ${user.role}` : ""}>
        <span className="avatar">{(user?.fullName || user?.email || "؟").slice(0, 1)}</span>
        <span>{user?.fullName || (lang === "ar" ? "حسابي" : "My account")}</span>
      </Link>
    </header>
  );
}

export function AppFooter() {
  const { t } = useLang();
  return (
    <footer className="app-foot">
      <span>© 2026 {t("appName")} · khattab-ai.com</span>
      <span className="fl">
        <Link href="/about">{t("navAbout")}</Link>
        <Link href="/contact">{t("navContact")}</Link>
        <Link href="/privacy">{t("navPrivacy")}</Link>
      </span>
    </footer>
  );
}

export default function AppShell({ children }: { children: ReactNode }) {
  return (
    <div className="app-shell">
      <Sidebar />
      <div className="app-col">
        <Topbar />
        <main className="content">{children}</main>
        <AppFooter />
      </div>
    </div>
  );
}
