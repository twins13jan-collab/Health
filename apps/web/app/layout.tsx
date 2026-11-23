import type { Metadata } from "next";
import type { ReactNode } from "react";
import Sidebar from "@/components/Sidebar";
import { LanguageProvider } from "@/lib/i18n";
import "./globals.css";

export const metadata: Metadata = {
  title: "المنظومة الوطنية لإدارة الأزمات الصحية والطوارئ الطبية — مصر",
  description:
    "Egypt National Health Crisis & Emergency Management System — Catalog v6 rebuild: TypeScript + React + Next.js over a Java/Spring Boot core.",
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="ar" dir="rtl">
      <body>
        <LanguageProvider>
          <div className="shell">
            <Sidebar />
            <main className="main">{children}</main>
          </div>
        </LanguageProvider>
      </body>
    </html>
  );
}
