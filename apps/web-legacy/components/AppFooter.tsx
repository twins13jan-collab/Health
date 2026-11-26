"use client";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useLang } from "@/lib/i18n";

const PUBLIC = ["/", "/login", "/register", "/about", "/contact", "/privacy", "/services", "/download-app"];

/** Slim footer for the authenticated application shell (the public
 *  pages carry their own full website footer). */
export default function AppFooter() {
  const pathname = usePathname();
  const { t } = useLang();
  if (PUBLIC.includes(pathname)) return null;
  return (
    <footer className="app-footer">
      <span>© 2026 {t("appName")} · khattab-ai.com</span>
      <span style={{ display: "inline-flex", gap: 14 }}>
        <Link href="/about">{t("navAbout")}</Link>
        <Link href="/contact">{t("navContact")}</Link>
        <Link href="/privacy">{t("navPrivacy")}</Link>
      </span>
    </footer>
  );
}
