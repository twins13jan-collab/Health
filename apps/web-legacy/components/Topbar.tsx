"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useMemo, useRef, useState } from "react";
import { getSessionUser, type SessionUser } from "@/lib/api";
import { useLang, type DictKey } from "@/lib/i18n";
import { NAV } from "@/lib/nav";

/** Application top bar: mobile nav toggle, quick navigation search over
 *  every module, the signed-in user's scope context, and account access. */
export default function Topbar() {
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

  if (["/login", "/register", "/", "/about", "/contact", "/privacy", "/download-app", "/services"].includes(pathname)) return null;

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
              <Link
                key={item.href}
                href={item.href}
                onClick={() => { setOpen(false); setQuery(""); }}
              >
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
