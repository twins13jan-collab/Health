import type { Metadata } from "next";
import type { ReactNode } from "react";
import AuthGate from "@/components/AuthGate";
import ChatWidget from "@/components/ChatWidget";
import Shell from "@/components/shell/Shell";
import { LanguageProvider } from "@/lib/i18n";
import "./globals.css";

export const metadata: Metadata = {
  metadataBase: new URL("https://khattab-ai.com"),
  title: "خطاب للصحة العامة وإدارة الأزمات — Khattab",
  description:
    "منظومة خطاب لإدارة الصحة العامة والأزمات والطوارئ الطبية والمنشآت الصحية — Khattab System for Public Health, Crisis, Medical Emergency and Health Facility Management.",
  openGraph: {
    type: "website",
    url: "https://khattab-ai.com/",
    siteName: "خطاب — Khattab",
    title: "خطاب للصحة العامة وإدارة الأزمات",
    description:
      "منظومة خطاب لإدارة الصحة العامة والأزمات والطوارئ الطبية والمنشآت الصحية — منصة تشغيل واحدة تدير المستشفيات والمعامل والصيدليات والمراكز الصحية وبنوك الدم وخدمات الطوارئ.",
    images: [{ url: "/og-image.png", width: 1200, height: 630, alt: "خطاب للصحة العامة وإدارة الأزمات" }],
    locale: "ar_EG",
  },
  twitter: {
    card: "summary_large_image",
    title: "خطاب للصحة العامة وإدارة الأزمات",
    description:
      "منظومة خطاب لإدارة الصحة العامة والأزمات والطوارئ الطبية والمنشآت الصحية.",
    images: ["/og-image.png"],
  },
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="ar" dir="rtl">
      <body>
        <LanguageProvider>
          <AuthGate>
            <Shell>{children}</Shell>
            <ChatWidget />
          </AuthGate>
        </LanguageProvider>
      </body>
    </html>
  );
}
