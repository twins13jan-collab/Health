"use client";

import { usePathname, useRouter } from "next/navigation";
import { useEffect, type ReactNode } from "react";
import { api, getToken } from "@/lib/api";

/** Session guard: any route other than /login requires a real session
 *  token; without one the user is sent to the login page. Also refreshes
 *  the stored session profile from /api/auth/me once per page load. */
export default function AuthGate({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();

  useEffect(() => {
    if (["/login", "/register", "/download-app", "/", "/about", "/contact", "/privacy", "/services"].includes(pathname)) return;
    if (!getToken()) {
      router.replace("/login");
      return;
    }
    void api.me().catch(() => {
      /* 401 is handled inside the API client (clears + redirects). */
    });
  }, [pathname, router]);

  return <>{children}</>;
}
