"use client";

import { usePathname, useRouter } from "next/navigation";
import { useEffect, type ReactNode } from "react";
import { api, getToken } from "@/lib/api";
import { PUBLIC_ROUTES } from "@/components/shell/Shell";

/** Session guard: any route other than the public ones requires a
 *  real session token; without one the user is sent to the login
 *  page. Also refreshes the stored session profile from
 *  /api/auth/me once per page load. */
export default function AuthGate({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();

  useEffect(() => {
    if (PUBLIC_ROUTES.includes(pathname)) return;
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
