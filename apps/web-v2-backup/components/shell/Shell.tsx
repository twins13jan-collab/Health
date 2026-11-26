"use client";

import { usePathname } from "next/navigation";
import type { ReactNode } from "react";
import AppShell from "./AppShell";
import PublicShell from "./PublicShell";

/**
 * The single public-routes list for the whole app. Every route is
 * rendered inside EXACTLY ONE shell: PublicShell for these routes,
 * AppShell for everything else. No page renders its own chrome and
 * no page is ever standalone.
 */
export const PUBLIC_ROUTES = [
  "/",
  "/login",
  "/register",
  "/about",
  "/contact",
  "/privacy",
  "/services",
  "/download-app",
];

export default function Shell({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  if (PUBLIC_ROUTES.includes(pathname)) return <PublicShell>{children}</PublicShell>;
  return <AppShell>{children}</AppShell>;
}
