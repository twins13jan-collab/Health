"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState, type ReactNode } from "react";
import { getToken } from "@/lib/api";
import { useLang } from "@/lib/i18n";

/**
 * The public website shell (v3) — slim dark-green utility strip,
 * white header with the medallion emblem and wordmark, horizontal
 * text nav with a gold underline on the active page, and the big
 * dark footer. Inner pages add a left rail menu card.
 */
const NAV: { href: string; key: string }[] = [
  { href: "/", key: "navHome" },
  { href: "/services", key: "navServices" },
  { href: "/#facilities", key: "navFacilities" },
  { href: "/about", key: "navAbout" },
  { href: "/strategy", key: "navStrategy" },
  { href: "/contact", key: "navContact" },
  { href: "/privacy", key: "navPrivacy" },
  { href: "/download-app", key: "appDownload" },
];

/** Inner public pages (everything public except home and login) get
 *  the rail layout, like a traditional website. */
export const INNER_PUBLIC_ROUTES = ["/services", "/about", "/contact", "/privacy", "/download-app", "/register", "/strategy"];

export function PublicHeader() {
  const { t, lang, setLang } = useLang();
  const pathname = usePathname();
  const [loggedIn, setLoggedIn] = useState(false);
  useEffect(() => { setLoggedIn(!!getToken()); }, []);
  const ar = lang === "ar";
  return (
    <>
      <div className="util-strip">
        <div className="util-in">
          <span>{t("sysDesc")}</span>
          <span className="u-actions">
            <a href="tel:+201556626662" dir="ltr">+20 155 662 6662</a>
            <a href="https://wa.me/201556626662" target="_blank" rel="noreferrer">{t("contactWhatsapp")}</a>
            <button type="button" className="util-lang" onClick={() => setLang(ar ? "en" : "ar")}>
              {ar ? "English" : "العربية"}
            </button>
          </span>
        </div>
      </div>
      <div className="pub-header">
        <div className="pub-header-in">
          <Link href="/" className="pub-brand">
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img src="/emblem.png" alt={t("appName")} width={58} height={58} />
            <span>
              <span className="pb-name">{t("appName")}</span>
              <span className="pb-sub">{t("sysDesc")} · khattab-ai.com</span>
            </span>
          </Link>
          <div className="pub-actions">
            <Link className="btn btn-soft" href="/download-app">⬇ {t("appDownload")}</Link>
            {loggedIn
              ? <Link className="btn" href="/dashboard">{t("dashboard")}</Link>
              : <Link className="btn" href="/login">{t("login")}</Link>}
          </div>
        </div>
      </div>
      <nav className="pub-nav">
        <div className="pub-nav-in">
          {NAV.map((n) => (
            <Link key={n.href} href={n.href} className={pathname === n.href ? "on" : undefined}>
              {t(n.key as never)}
            </Link>
          ))}
        </div>
      </nav>
    </>
  );
}

export function PublicRail() {
  const { t } = useLang();
  const pathname = usePathname();
  return (
    <aside>
      <div className="rail-card">
        <div className="rail-title">{t("sideMenuTitle")}</div>
        {NAV.map((n) => (
          <Link key={n.href} href={n.href} className={`rail-link${pathname === n.href ? " on" : ""}`}>
            {t(n.key as never)}
          </Link>
        ))}
        <div className="rail-contact">
          <strong>{t("contactPerson")}</strong>
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
    <footer className="pub-footer">
      <div className="footer-in">
        <div className="footer-grid">
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
            <Link href="/register">{t("register")}</Link>
          </div>
          <div>
            <h3>{t("navContact")}</h3>
            <p>{t("contactPerson")}</p>
            <a href="tel:+201556626662" dir="ltr">+20 155 662 6662</a>
            <a href="https://wa.me/201556626662" target="_blank" rel="noreferrer">{t("contactWhatsapp")}</a>
            <p>khattab-ai.com</p>
          </div>
        </div>
      </div>
      <div className="footer-bottom">
        <div className="footer-bottom-in">
          <span>© 2026 {t("appName")}</span>
          <span>{t("sysDesc")}</span>
          <span className="fl">
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
    <div>
      <PublicHeader />
      <main className="pub-main">
        {inner ? (
          <div className="pub-grid">
            <PublicRail />
            <div>{children}</div>
          </div>
        ) : (
          children
        )}
      </main>
      <PublicFooter />
    </div>
  );
}
