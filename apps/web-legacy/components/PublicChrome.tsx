"use client";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";
import { useLang } from "@/lib/i18n";

/**
 * Traditional public-website chrome: a utility bar, a main header
 * with the official logo, a green navigation bar, a side menu for
 * inner pages, and a full-width multi-column footer.
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

export function PublicHeader() {
  const { t, lang, setLang } = useLang();
  const pathname = usePathname();
  const [loggedIn, setLoggedIn] = useState(false);
  useEffect(() => { setLoggedIn(!!window.localStorage.getItem("ehs_token")); }, []);
  const ar = lang === "ar";
  return (
    <div className="bleed bleed-top">
      <div className="util-bar">
        <div className="bleed-inner util-in">
          <span>{t("sysDesc")}</span>
          <span style={{ display: "inline-flex", gap: 12, alignItems: "center", flexWrap: "wrap" }}>
            <a href="tel:+201556626662" dir="ltr">📞 +20 155 662 6662</a>
            <a href="https://wa.me/201556626662" target="_blank" rel="noreferrer">{t("contactWhatsapp")}</a>
            <button type="button" className="util-lang" onClick={() => setLang(ar ? "en" : "ar")}>
              {ar ? "English" : "العربية"}
            </button>
          </span>
        </div>
      </div>
      <div className="main-header">
        <div className="bleed-inner main-header-in">
          <Link href="/" className="mh-brand">
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img src="/logo.png" alt={t("appName")} className="mh-logo" />
            <span>
              <span className="mh-name">{t("appName")}</span>
              <br />
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
        <div className="bleed-inner nav-in">
          {NAV.map((n) => (
            <Link key={n.href} href={n.href} className={pathname === n.href ? "active" : undefined}>
              {t(n.key as never)}
            </Link>
          ))}
        </div>
      </nav>
    </div>
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
    <div className="bleed bleed-bottom" style={{ marginTop: 30 }}>
      <footer className="site-footer">
        <div className="bleed-inner footer-in">
          <div className="site-footer-grid">
            <div>
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img src="/emblem.png" alt="" width={54} height={54} style={{ background: "#fff", borderRadius: 10, padding: 3, marginBottom: 10 }} />
              <h3 style={{ marginTop: 0 }}>{t("appName")}</h3>
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
                <a href="tel:+201556626662" dir="ltr" style={{ display: "inline" }}>+20 155 662 6662</a>
                {" · "}
                <a href="https://wa.me/201556626662" target="_blank" rel="noreferrer" style={{ display: "inline" }}>{t("contactWhatsapp")}</a>
              </p>
              <p>khattab-ai.com</p>
            </div>
          </div>
        </div>
        <div className="footer-bottom-bar">
          <div className="bleed-inner footer-bottom-in">
            <span>© 2026 {t("appName")}</span>
            <span>{t("sysDesc")}</span>
            <span style={{ display: "inline-flex", gap: 12 }}>
              <Link href="/privacy">{t("navPrivacy")}</Link>
              <Link href="/contact">{t("navContact")}</Link>
            </span>
          </div>
        </div>
      </footer>
    </div>
  );
}
