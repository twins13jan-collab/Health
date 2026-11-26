"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useMemo, useRef, useState, type ReactNode } from "react";
import { api, getSessionUser, type SessionUser } from "@/lib/api";
import { useLang, type DictKey } from "@/lib/i18n";
import { NAV, NAV_SECTIONS, sectionOf } from "@/lib/nav";

/**
 * The application shell — the ONLY chrome authenticated routes get:
 * grouped sidebar, topbar with quick-nav search, slim footer.
 */

export function Sidebar() {
  const pathname = usePathname();
  const { lang, setLang, t } = useLang();
  const [user, setUser] = useState<SessionUser | null>(null);
  useEffect(() => { setUser(getSessionUser()); }, [pathname]);
  const isAdmin = user?.role === "admin" || user?.role === "national_admin";

  return (
    <aside className="sidebar">
      <div className="brand">
        <div className="brand-mark">
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img src="/emblem.png" alt="خطاب" width={46} height={46} />
        </div>
        <div className="brand-name">{t("appName")}</div>
        <div className="brand-sub">
          {lang === "ar" ? "Khattab for Public Health & Crisis Management" : "خطاب للصحة العامة وإدارة الأزمات"}
        </div>
      </div>
      <nav className="nav">
        {[...NAV_SECTIONS.map((s) => s.id), "all"].map((sectionId) => {
          const items = NAV.filter((item) => (!item.adminOnly || isAdmin) && sectionOf(item.href).id === sectionId);
          if (!items.length) return null;
          const section = NAV_SECTIONS.find((s) => s.id === sectionId);
          return (
            <div key={sectionId}>
              <div className="nav-section">
                {section ? (lang === "ar" ? section.ar : section.en) : lang === "ar" ? "كل الوحدات" : "All Modules"}
              </div>
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
        <button type="button" className="btn btn-ghost btn-block" onClick={() => setLang(lang === "ar" ? "en" : "ar")}>
          {lang === "ar" ? "English" : "العربية"}
        </button>
        <button type="button" className="btn btn-ghost btn-block" onClick={() => api.logout()}>
          {t("logout")}
        </button>
      </div>
    </aside>
  );
}

export function Topbar() {
  const pathname = usePathname();
  const router = useRouter();
  const { lang, t } = useLang();
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
      <div className="topbar-title">{lang === "ar" ? "خطاب" : "Khattab"}</div>
      <div className="topbar-context">{context}</div>
      <div className="topbar-spacer" />
      <div className="topbar-search" ref={searchRef}>
        <input
          value={query}
          onChange={(e) => { setQuery(e.target.value); setOpen(true); }}
          onFocus={() => setOpen(true)}
          placeholder={lang === "ar" ? "تنقّل سريع إلى أي وحدة…" : "Jump to any module…"}
          aria-label={lang === "ar" ? "بحث الوحدات" : "Module search"}
        />
        {open && results.length > 0 ? (
          <div className="search-results">
            {results.map((item) => (
              <Link key={item.href} href={item.href} onClick={() => { setOpen(false); setQuery(""); }}>
                {item.icon} {t(item.key as DictKey)}
              </Link>
            ))}
          </div>
        ) : null}
      </div>
      <button
        type="button"
        className="icon-btn"
        aria-label={lang === "ar" ? "حسابي" : "My account"}
        title={user ? `${user.fullName || user.email} — ${user.role}` : ""}
        onClick={() => router.push("/account")}
      >
        👤
      </button>
    </header>
  );
}

export function AppFooter() {
  const { t } = useLang();
  return (
    <footer className="app-footer">
      <span>© 2026 {t("appName")} · khattab-ai.com</span>
      <span className="footer-links-inline">
        <Link href="/about">{t("navAbout")}</Link>
        <Link href="/contact">{t("navContact")}</Link>
        <Link href="/privacy">{t("navPrivacy")}</Link>
      </span>
    </footer>
  );
}

export default function AppShell({ children }: { children: ReactNode }) {
  return (
    <div className="shell">
      <Sidebar />
      <div className="main-col">
        <Topbar />
        <main className="main">{children}</main>
        <AppFooter />
      </div>
    </div>
  );
}
