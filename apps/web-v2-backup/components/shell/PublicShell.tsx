"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState, type ReactNode } from "react";
import { getToken } from "@/lib/api";
import { useLang } from "@/lib/i18n";

/**
 * The public website shell — the ONLY chrome public routes ever get:
 * utility bar, main header with the official logo, green navigation
 * bar, an optional side menu for inner pages, and the full footer.
 */
const NAV: { href: string; key: string }[] = [
  { href: "/", key: "navHome" },
  { href: "/services", key: "navServices" },
  { href: "/#facilities", key: "navFacilities" },
  { href: "/about", key: "navAbout" },
  { href: "/contact", key: "navContact" },
  { href: "/privacy", key: "navPrivacy" },
  { href: "/download-app", key: "appDownload" },
];

/** Inner public pages (everything public except home and login) get
 *  the side-menu layout, like a traditional website. */
export const INNER_PUBLIC_ROUTES = ["/services", "/about", "/contact", "/privacy", "/download-app", "/register"];

export function PublicHeader() {
  const { t, lang, setLang } = useLang();
  const pathname = usePathname();
  const [loggedIn, setLoggedIn] = useState(false);
  useEffect(() => { setLoggedIn(!!getToken()); }, []);
  const ar = lang === "ar";
  return (
    <>
      <div className="util-bar">
        <div className="util-in">
          <span>{t("sysDesc")}</span>
          <span className="util-actions">
            <a href="tel:+201556626662" dir="ltr">📞 +20 155 662 6662</a>
            <a href="https://wa.me/201556626662" target="_blank" rel="noreferrer">{t("contactWhatsapp")}</a>
            <button type="button" className="util-lang" onClick={() => setLang(ar ? "en" : "ar")}>
              {ar ? "English" : "العربية"}
            </button>
          </span>
        </div>
      </div>
      <div className="main-header">
        <div className="main-header-in">
          <Link href="/" className="mh-brand">
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img src="/logo.png" alt={t("appName")} className="mh-logo" />
            <span>
              <span className="mh-name">{t("appName")}</span>
              <span className="mh-sub">{t("sysDesc")} · khattab-ai.com</span>
            </span>
          </Link>
          <div className="mh-actions">
            <Link className="btn btn-secondary" href="/download-app">⬇ {t("appDownload")}</Link>
            {loggedIn
              ? <Link className="btn" href="/dashboard">{t("dashboard")}</Link>
              : <Link className="btn" href="/login">{t("login")}</Link>}
          </div>
        </div>
      </div>
      <nav className="nav-bar">
        <div className="nav-in">
          {NAV.map((n) => (
            <Link key={n.href} href={n.href} className={pathname === n.href ? "active" : undefined}>
              {t(n.key as never)}
            </Link>
          ))}
        </div>
      </nav>
    </>
  );
}

export function PublicSideMenu() {
  const { t } = useLang();
  const pathname = usePathname();
  return (
    <aside>
      <div className="card side-menu">
        <div className="side-menu-title">{t("sideMenuTitle")}</div>
        {NAV.map((n) => (
          <Link key={n.href} href={n.href} className={pathname === n.href ? "active" : undefined}>
            {t(n.key as never)}
          </Link>
        ))}
        <div className="side-contact">
          <strong>{t("contactPerson")}</strong>
          <br />
          <a href="tel:+201556626662" dir="ltr">+20 155 662 6662</a>
          <br />
          <a href="https://wa.me/201556626662" target="_blank" rel="noreferrer">{t("contactWhatsapp")}</a>
        </div>
      </div>
    </aside>
  );
}

export function PublicFooter() {
  const { t } = useLang();
  return (
    <footer className="site-footer">
      <div className="footer-in">
        <div className="site-footer-grid">
          <div>
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img src="/emblem.png" alt="" width={54} height={54} className="footer-emblem" />
            <h3>{t("appName")}</h3>
            <p>{t("footerAbout")}</p>
          </div>
          <div>
            <h3>{t("footerLinks")}</h3>
            <Link href="/">{t("navHome")}</Link>
            <Link href="/about">{t("navAbout")}</Link>
            <Link href="/contact">{t("navContact")}</Link>
            <Link href="/privacy">{t("navPrivacy")}</Link>
            <Link href="/download-app">{t("appDownload")}</Link>
          </div>
          <div>
            <h3>{t("navServices")}</h3>
            <Link href="/services">{t("navServices")}</Link>
            <Link href="/#facilities">{t("navFacilities")}</Link>
            <Link href="/#facilities">{t("capTitle")}</Link>
          </div>
          <div>
            <h3>{t("navContact")}</h3>
            <p>{t("contactPerson")}</p>
            <p>
              <a href="tel:+201556626662" dir="ltr">+20 155 662 6662</a>
              <a href="https://wa.me/201556626662" target="_blank" rel="noreferrer">{t("contactWhatsapp")}</a>
            </p>
            <p>khattab-ai.com</p>
          </div>
        </div>
      </div>
      <div className="footer-bottom-bar">
        <div className="footer-bottom-in">
          <span>© 2026 {t("appName")}</span>
          <span>{t("sysDesc")}</span>
          <span className="footer-links-inline">
            <Link href="/privacy">{t("navPrivacy")}</Link>
            <Link href="/contact">{t("navContact")}</Link>
          </span>
        </div>
      </div>
    </footer>
  );
}

export default function PublicShell({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  const inner = INNER_PUBLIC_ROUTES.includes(pathname);
  return (
    <div className="public-shell">
      <PublicHeader />
      <main className="public-main">
        {inner ? (
          <div className="page-grid">
            <PublicSideMenu />
            <div className="public-content">{children}</div>
          </div>
        ) : (
          children
        )}
      </main>
      <PublicFooter />
    </div>
  );
}
