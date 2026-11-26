/**
 * Typed API client for the Java core (Spring Boot).
 *
 * PRODUCTION client — there is no demo mode and no fabricated data:
 * every call goes to the core, and every failure surfaces as an
 * ApiError the UI must show honestly. A 401 clears the session and
 * returns the user to the login page. Offline *writes* are handled by
 * the cycle-237 offline queue (lib/offline.ts), never by faking data.
 *
 * Base URL: NEXT_PUBLIC_API_BASE_URL (same-origin in production,
 * where nginx proxies /api to the core).
 */
import type {
  CycleDefinition,
  CycleInstance,
  CycleStatus,
  Encounter,
  Facility,
  GovernorateCapacity,
  LoginResponse,
  NationalKpis,
  Patient,
} from "./types";

export const API_BASE_URL: string =
  process.env.NEXT_PUBLIC_API_BASE_URL?.replace(/\/$/, "") ?? "http://localhost:8080";

export interface ApiResult<T> {
  data: T;
}

/** An error the UI can present: HTTP status + the core's message. */
export class ApiError extends Error {
  status: number;
  code: string;
  constructor(status: number, code: string, message: string) {
    super(message);
    this.status = status;
    this.code = code;
  }
}

const TOKEN_KEY = "ehs.accessToken";
const USER_KEY = "ehs.sessionUser";

export interface SessionUser {
  id: string;
  email: string;
  fullName: string;
  role: string;
  facilityId: string;
  governorate: string;
  directorate: string;
}

export function getToken(): string | null {
  if (typeof window === "undefined") return null;
  return window.localStorage.getItem(TOKEN_KEY);
}
export function setToken(token: string | null): void {
  if (typeof window === "undefined") return;
  if (token) window.localStorage.setItem(TOKEN_KEY, token);
  else window.localStorage.removeItem(TOKEN_KEY);
}
export function getSessionUser(): SessionUser | null {
  if (typeof window === "undefined") return null;
  try {
    const raw = window.localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as SessionUser) : null;
  } catch {
    return null;
  }
}
export function setSessionUser(user: SessionUser | null): void {
  if (typeof window === "undefined") return;
  if (user) window.localStorage.setItem(USER_KEY, JSON.stringify(user));
  else window.localStorage.removeItem(USER_KEY);
}
export function clearSession(): void {
  setToken(null);
  setSessionUser(null);
}

function toSessionUser(u: Record<string, unknown>): SessionUser {
  return {
    id: String(u.id ?? ""),
    email: String(u.email ?? ""),
    fullName: String(u.full_name ?? u.fullName ?? ""),
    role: String(u.role ?? ""),
    facilityId: String(u.facility_id ?? u.facilityId ?? ""),
    governorate: String(u.governorate ?? ""),
    directorate: String(u.directorate ?? ""),
  };
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const token = getToken();
  let res: Response;
  try {
    res = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      headers: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...(init?.headers ?? {}),
      },
      signal: init?.signal ?? AbortSignal.timeout(15000),
    });
  } catch (e) {
    // Network-level failure (core unreachable / offline). Distinct from
    // an HTTP error so callers like the offline queue can recognise it.
    throw e instanceof Error ? e : new Error("network_error");
  }
  if (res.status === 401 && !path.startsWith("/api/auth/login")) {
    clearSession();
    if (typeof window !== "undefined" && window.location.pathname !== "/login") {
      window.location.assign("/login");
    }
    throw new ApiError(401, "unauthorized", "انتهت الجلسة — سجّل الدخول من جديد");
  }
  if (!res.ok) {
    let code = `http_${res.status}`;
    let message = `خطأ من الخادم (${res.status})`;
    try {
      const body = (await res.json()) as Record<string, unknown>;
      if (typeof body.code === "string") code = body.code;
      if (typeof body.message === "string" && body.message) message = body.message;
      else if (typeof body.error === "string" && body.error) message = body.error;
    } catch {
      /* keep defaults */
    }
    throw new ApiError(res.status, code, message);
  }
  return (await res.json()) as T;
}

/** Read helper: real API only — failures throw ApiError. */
async function read<T>(path: string): Promise<ApiResult<T>> {
  return { data: await request<T>(path) };
}

// ------------------------------------------------------------------- reads
export const api = {
  /** Raw GET for custom envelopes (boards, summaries). */
  getJson: <T>(path: string) => request<T>(path),
  kpis: () => read<NationalKpis>("/api/dashboard/kpis"),
  capacity: () => read<GovernorateCapacity[]>("/api/dashboard/capacity-by-governorate"),
  facilities: () => read<Facility[]>("/api/facilities"),
  patients: (q = "") =>
    read<Patient[]>(`/api/patients${q ? `?q=${encodeURIComponent(q)}` : ""}`),
  encounters: () => read<Encounter[]>("/api/encounters"),
  cycleDefinitions: () => read<CycleDefinition[]>("/api/cycle-engine/definitions"),
  cycleInstances: () => read<CycleInstance[]>("/api/cycle-engine/instances"),

  // ----------------------------------------------------------------- writes
  async registerPatient(input: Omit<Patient, "id">): Promise<ApiResult<Patient>> {
    const data = await request<Patient>("/api/patients", {
      method: "POST",
      body: JSON.stringify(input),
    });
    return { data };
  },

  async createCycleInstance(input: {
    definitionCode: string;
    title: string;
    subjectType?: string;
    subjectRef?: string;
    protocolVersion?: string;
  }): Promise<ApiResult<CycleInstance>> {
    const user = getSessionUser();
    const data = await request<CycleInstance>("/api/cycle-engine/instances", {
      method: "POST",
      body: JSON.stringify({
        ...input,
        ...(user?.facilityId ? { facilityId: user.facilityId } : {}),
        idempotencyKey: crypto.randomUUID(),
      }),
    });
    return { data };
  },

  async transitionCycleInstance(
    id: string,
    toStatus: CycleStatus,
    reason?: string,
  ): Promise<ApiResult<CycleInstance>> {
    const data = await request<CycleInstance>(`/api/cycle-engine/instances/${id}/transition`, {
      method: "POST",
      body: JSON.stringify({ toStatus, reason: reason ?? null }),
    });
    return { data };
  },

  /** Operational write: real API only. A failed write throws. */
  async postJson<T>(path: string, body?: unknown): Promise<T> {
    return request<T>(path, {
      method: "POST",
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  },

  async putJson<T>(path: string, body?: unknown): Promise<T> {
    return request<T>(path, {
      method: "PUT",
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  },

  /** Generic envelope list reader for module endpoints: { ok, <key>: [...] }. */
  async moduleList<T>(path: string, key: string): Promise<ApiResult<T[]>> {
    const env = await request<Record<string, unknown>>(path);
    const rows = (env[key] as T[] | undefined) ?? [];
    return { data: rows };
  },

  // ------------------------------------------------------------------ auth
  async login(email: string, password: string, facilityId?: string): Promise<ApiResult<LoginResponse>> {
    const raw = await request<Record<string, unknown>>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password, facilityId: facilityId || undefined }),
    });
    const token = String(raw.access_token ?? "");
    const userRaw = (raw.user ?? {}) as Record<string, unknown>;
    const session = toSessionUser(userRaw);
    setToken(token);
    setSessionUser(session);
    const data: LoginResponse = {
      accessToken: token,
      tokenType: String(raw.token_type ?? "Bearer"),
      expiresInSeconds: 3600,
      user: {
        id: session.id,
        fullNameAr: session.fullName,
        role: session.role,
        facilityId: session.facilityId || undefined,
      },
    };
    return { data };
  },

  async registerAccount(body: Record<string, unknown>): Promise<ApiResult<LoginResponse>> {
    const raw = await request<Record<string, unknown>>("/api/auth/register-patient", {
      method: "POST",
      body: JSON.stringify(body),
    });
    const token = String(raw.access_token ?? "");
    const session = toSessionUser((raw.user ?? {}) as Record<string, unknown>);
    setToken(token);
    setSessionUser(session);
    return { data: { accessToken: token, tokenType: "Bearer", expiresInSeconds: 3600,
      user: { id: session.id, fullNameAr: session.fullName, role: session.role, facilityId: session.facilityId || undefined } } };
  },

  async registerFacility(body: Record<string, unknown>): Promise<Record<string, unknown>> {
    return request<Record<string, unknown>>("/api/auth/register-facility", {
      method: "POST",
      body: JSON.stringify(body),
    });
  },

  async me(): Promise<SessionUser> {
    const raw = await request<Record<string, unknown>>("/api/auth/me");
    const session = toSessionUser(raw);
    setSessionUser(session);
    return session;
  },

  async changePassword(currentPassword: string, newPassword: string): Promise<void> {
    await request("/api/auth/change-password", {
      method: "POST",
      body: JSON.stringify({ currentPassword, newPassword }),
    });
  },

  logout(): void {
    clearSession();
    if (typeof window !== "undefined") window.location.assign("/login");
  },
};

// ------------------------------------------------------- account admin API
export interface AdminUser {
  id: string;
  email: string;
  full_name: string;
  role: string;
  facility_id: string;
  governorate: string;
  directorate: string;
  is_active: boolean;
  mfa_enabled: boolean;
  last_login_at: string;
  created_at: string;
}

export interface AdminUserList {
  items: AdminUser[];
  total: number;
  page: number;
  size: number;
}

export const adminUsers = {
  list: (q = "", page = 0, size = 20) =>
    request<AdminUserList>(
      `/api/admin/users?page=${page}&size=${size}${q ? `&q=${encodeURIComponent(q)}` : ""}`,
    ),
  create: (body: {
    email: string;
    password: string;
    role: string;
    fullName?: string;
    facilityId?: string;
    governorate?: string;
    directorate?: string;
  }) => request<AdminUser>("/api/admin/users", { method: "POST", body: JSON.stringify(body) }),
  update: (
    id: string,
    body: { role?: string; facilityId?: string; governorate?: string; directorate?: string; fullName?: string },
  ) => request<AdminUser>(`/api/admin/users/${id}`, { method: "PUT", body: JSON.stringify(body) }),
  activate: (id: string) => request<AdminUser>(`/api/admin/users/${id}/activate`, { method: "POST" }),
  deactivate: (id: string) => request<AdminUser>(`/api/admin/users/${id}/deactivate`, { method: "POST" }),
  resetPassword: (id: string, newPassword: string) =>
    request<{ ok: boolean }>(`/api/admin/users/${id}/reset-password`, {
      method: "POST",
      body: JSON.stringify({ newPassword }),
    }),
};
