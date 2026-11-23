/**
 * Typed API client for the Java core (Spring Boot, port 8080 by default).
 *
 * Every read falls back to typed demo data when the core is unreachable
 * (network error / non-2xx), so the UI is always demonstrable. Writes are
 * attempted against the core first; on failure they are applied to the
 * in-memory demo store and flagged with `demo: true`.
 *
 * Base URL: NEXT_PUBLIC_API_BASE_URL (default http://localhost:8080).
 */
import type {
  CycleDefinition,
  CycleInstance,
  CycleStatus,
  Encounter,
  Facility,
  GovernorateCapacity,
  LoginRequest,
  LoginResponse,
  NationalKpis,
  Patient,
} from "./types";

export const API_BASE_URL: string =
  process.env.NEXT_PUBLIC_API_BASE_URL?.replace(/\/$/, "") ?? "http://localhost:8080";

export interface ApiResult<T> {
  data: T;
  /** true when the payload came from the local demo fallback. */
  demo: boolean;
}

const TOKEN_KEY = "ehs.accessToken";

export function getToken(): string | null {
  if (typeof window === "undefined") return null;
  return window.localStorage.getItem(TOKEN_KEY);
}
export function setToken(token: string | null): void {
  if (typeof window === "undefined") return;
  if (token) window.localStorage.setItem(TOKEN_KEY, token);
  else window.localStorage.removeItem(TOKEN_KEY);
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const token = getToken();
  const res = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(init?.headers ?? {}),
    },
    // Never let a hung core freeze the UI.
    signal: init?.signal ?? AbortSignal.timeout(4000),
  });
  if (!res.ok) throw new Error(`API ${res.status} on ${path}`);
  return (await res.json()) as T;
}

/** Read helper: real API first, demo fallback on any failure. */
async function read<T>(path: string, demo: () => T): Promise<ApiResult<T>> {
  try {
    return { data: await request<T>(path), demo: false };
  } catch {
    return { data: demo(), demo: true };
  }
}

// ---------------------------------------------------------------- demo data
export const DEMO_FACILITY_ID = "11111111-1111-4111-8111-111111111111";

export const demoFacilities: Facility[] = [
  { id: DEMO_FACILITY_ID, code: "CAI-GEN-001", nameAr: "مستشفى القاهرة العام", nameEn: "Cairo General Hospital", type: "general_hospital", level: "tertiary", governorate: "القاهرة", directorate: "مديرية القاهرة", city: "القاهرة", totalBeds: 620, occupiedBeds: 512, icuBeds: 48, icuOccupied: 39, status: "active" },
  { id: "22222222-2222-4222-8222-222222222222", code: "GIZ-UH-001", nameAr: "مستشفى الجيزة الجامعي", nameEn: "Giza University Hospital", type: "university_hospital", level: "quaternary", governorate: "الجيزة", directorate: "مديرية الجيزة", city: "الجيزة", totalBeds: 840, occupiedBeds: 701, icuBeds: 72, icuOccupied: 61, status: "active" },
  { id: "33333333-3333-4333-8333-333333333333", code: "ALX-GEN-001", nameAr: "مستشفى الإسكندرية العام", nameEn: "Alexandria General Hospital", type: "general_hospital", level: "secondary", governorate: "الإسكندرية", directorate: "مديرية الإسكندرية", city: "الإسكندرية", totalBeds: 410, occupiedBeds: 296, icuBeds: 30, icuOccupied: 18, status: "active" },
  { id: "44444444-4444-4444-8444-444444444444", code: "QEN-HU-001", nameAr: "وحدة صحة قنا الريفية", nameEn: "Qena Rural Health Unit", type: "health_unit", level: "primary", governorate: "قنا", directorate: "مديرية قنا", city: "قنا", totalBeds: 24, occupiedBeds: 11, icuBeds: 0, icuOccupied: 0, status: "active" },
];

export const demoPatients: Patient[] = [
  { id: "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa", nationalId: "29001011234567", fullNameAr: "أحمد محمد علي حسن", fullNameEn: "Ahmed Mohamed Ali Hassan", dateOfBirth: "1990-01-01", gender: "male", governorate: "القاهرة", phone: "01001234567", bloodType: "O+", chronicConditions: ["السكري"], registeredFacilityId: DEMO_FACILITY_ID, createdAt: "2026-11-20T09:00:00Z" },
  { id: "bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb", nationalId: "29505159876543", fullNameAr: "فاطمة الزهراء إبراهيم", fullNameEn: "Fatma Elzahraa Ibrahim", dateOfBirth: "1995-05-15", gender: "female", governorate: "الجيزة", phone: "01119876543", bloodType: "A-", chronicConditions: [], registeredFacilityId: DEMO_FACILITY_ID, createdAt: "2026-11-21T11:30:00Z" },
];

export const demoEncounters: Encounter[] = [
  { id: "e1111111-1111-4111-8111-111111111111", patientId: demoPatients[0].id, patientNameAr: demoPatients[0].fullNameAr, facilityId: DEMO_FACILITY_ID, facilityNameAr: "مستشفى القاهرة العام", chiefComplaint: "ألم صدري", triageLevel: "emergent", status: "in_consultation", arrivedAt: "2026-11-22T08:15:00Z", news2Score: 5 },
  { id: "e2222222-2222-4222-8222-222222222222", patientId: demoPatients[1].id, patientNameAr: demoPatients[1].fullNameAr, facilityId: DEMO_FACILITY_ID, facilityNameAr: "مستشفى القاهرة العام", chiefComplaint: "حمى وصداع", triageLevel: "urgent", status: "triaged", arrivedAt: "2026-11-22T09:40:00Z", news2Score: 2 },
];

export const demoCycleDefinitions: CycleDefinition[] = [
  { code: "CYCLE-217", catalogNumber: 217, titleAr: "دورة السجل الصحي الطولي ومصالحة بيانات المريض", titleEn: "Longitudinal health record & patient data reconciliation cycle", domain: "clinical-data", specialisedRoute: null, slaHours: 72, status: "active" },
  { code: "CYCLE-219", catalogNumber: 219, titleAr: "دورة الطلب الطبي والعينة والنتيجة الحرجة", titleEn: "Medical order, specimen & critical result cycle", domain: "clinical", specialisedRoute: "lab-cycle", slaHours: 24, status: "active" },
  { code: "CYCLE-220", catalogNumber: 220, titleAr: "دورة العمليات والتخدير والتعافي", titleEn: "Surgery, anaesthesia & recovery cycle", domain: "clinical", specialisedRoute: "surgery-cycle", slaHours: 48, status: "active" },
  { code: "CYCLE-221", catalogNumber: 221, titleAr: "دورة العناية المركزة ومراقبة الأجهزة الطبية", titleEn: "Intensive care & medical device monitoring cycle", domain: "clinical", specialisedRoute: "icu-cycle", slaHours: 4, status: "active" },
  { code: "CYCLE-223", catalogNumber: 223, titleAr: "دورة بنك الدم والتوافق والنقل والتفاعلات", titleEn: "Blood bank, compatibility, transfusion & reactions cycle", domain: "clinical", specialisedRoute: "bloodbank-cycle", slaHours: 12, status: "active" },
  { code: "CYCLE-224", catalogNumber: 224, titleAr: "دورة الإحالة والنقل الطبي بين المنشآت", titleEn: "Inter-facility referral & medical transport cycle", domain: "operations", specialisedRoute: "emergency-cycle", slaHours: 6, status: "active" },
  { code: "CYCLE-226", catalogNumber: 226, titleAr: "دورة الاستدعاء الدوائي والتشغيلة والمخزون", titleEn: "Drug recall, batch & stock cycle", domain: "operations", specialisedRoute: "recall-cycle", slaHours: 24, status: "active" },
  { code: "CYCLE-236", catalogNumber: 236, titleAr: "دورة التقارير القومية والبيانات المفتوحة", titleEn: "National reports & open data cycle", domain: "governance", specialisedRoute: "national-report-cycle", slaHours: 72, status: "active" },
  { code: "CYCLE-237", catalogNumber: 237, titleAr: "دورة العمل دون اتصال والمزامنة الآمنة", titleEn: "Offline work & secure sync cycle", domain: "platform", specialisedRoute: "offline-sync-cycle", slaHours: 24, status: "active" },
  { code: "CYCLE-239", catalogNumber: 239, titleAr: "دورة الوثائق والسجلات والتوقيع والاحتفاظ", titleEn: "Documents, records, signatures & retention cycle", domain: "governance", specialisedRoute: "document-cycle", slaHours: 72, status: "active" },
  { code: "CYCLE-243", catalogNumber: 243, titleAr: "دورة الأورام والعلاج الكيماوي والمناعي والإشعاعي", titleEn: "Oncology, chemo, immuno & radiotherapy cycle", domain: "clinical", specialisedRoute: "oncology-cycle", slaHours: 48, status: "active" },
  { code: "CYCLE-238", catalogNumber: 238, titleAr: "دورة الحوادث الأمنية والخصوصية", titleEn: "Security & privacy incidents cycle", domain: "governance", specialisedRoute: "security-incident-cycle", slaHours: 4, status: "active" },
  { code: "CYCLE-227", catalogNumber: 227, titleAr: "دورة المشتريات والعقود والاستلام والموردين", titleEn: "Procurement, contracts, receiving & suppliers cycle", domain: "operations", specialisedRoute: "purchasing-cycle", slaHours: 168, status: "active" },
  { code: "CYCLE-230", catalogNumber: 230, titleAr: "دورة الأصول الطبية والصيانة والمعايرة", titleEn: "Medical assets, maintenance & calibration cycle", domain: "operations", specialisedRoute: "asset-cycle", slaHours: 72, status: "active" },
  { code: "CYCLE-228", catalogNumber: 228, titleAr: "دورة الإيرادات والفوترة والتأمين والمطالبات", titleEn: "Revenue, billing, insurance & claims cycle", domain: "finance", specialisedRoute: "billing", slaHours: 120, status: "active" },
  { code: "CYCLE-237", catalogNumber: 237, titleAr: "دورة العمل دون اتصال والمزامنة الآمنة", titleEn: "Offline work & safe synchronisation cycle", domain: "digital", specialisedRoute: null, slaHours: 24, status: "active" },
  { code: "CYCLE-240", catalogNumber: 240, titleAr: "دورة علم الأمراض والأنسجة والخلايا", titleEn: "Pathology, histology & cytology cycle", domain: "clinical", specialisedRoute: "pathology-cycle", slaHours: 120, status: "active" },
  { code: "CYCLE-241", catalogNumber: 241, titleAr: "دورة المناظير والإجراءات التشخيصية والتداخلية", titleEn: "Endoscopy & interventional procedures cycle", domain: "clinical", specialisedRoute: "endoscopy-cycle", slaHours: 48, status: "active" },
  { code: "CYCLE-242", catalogNumber: 242, titleAr: "دورة غسيل الكلى والعلاج الكلوي البديل", titleEn: "Dialysis & renal replacement therapy cycle", domain: "clinical", specialisedRoute: "dialysis-cycle", slaHours: 24, status: "active" },
  { code: "CYCLE-244", catalogNumber: 244, titleAr: "دورة الأمومة والحمل والولادة وما بعد الولادة", titleEn: "Maternity, pregnancy, delivery & postpartum cycle", domain: "clinical", specialisedRoute: "maternity-cycle", slaHours: 24, status: "active" },
  { code: "CYCLE-249", catalogNumber: 249, titleAr: "دورة طب العيون والسمع والأنف والأذن والحنجرة", titleEn: "Ophthalmology, audiology & ENT cycle", domain: "clinical", specialisedRoute: "sensory-cycle", slaHours: 72, status: "active" },
  { code: "CYCLE-250", catalogNumber: 250, titleAr: "دورة الحروق والجروح والعناية المتقدمة بالأنسجة", titleEn: "Burns, wounds & advanced tissue care cycle", domain: "clinical", specialisedRoute: "wounds-cycle", slaHours: 24, status: "active" },
  { code: "CYCLE-248", catalogNumber: 248, titleAr: "دورة طب الأسنان وصحة الفم والفكين", titleEn: "Dentistry, oral & maxillofacial health cycle", domain: "clinical", specialisedRoute: "dental-cycle", slaHours: 48, status: "active" },
  { code: "CYCLE-254", catalogNumber: 254, titleAr: "دورة مراكز السموم والتعرضات الكيميائية والدوائية", titleEn: "Poison centres & chemical/drug exposures cycle", domain: "clinical", specialisedRoute: "toxicology-cycle", slaHours: 1, status: "active" },
  { code: "CYCLE-247", catalogNumber: 247, titleAr: "دورة الرعاية التلطيفية وإدارة الألم ونهاية الحياة", titleEn: "Palliative care, pain & end-of-life cycle", domain: "clinical", specialisedRoute: "palliative-cycle", slaHours: 24, status: "active" },
  { code: "CYCLE-253", catalogNumber: 253, titleAr: "دورة الرعاية المنزلية والتمريض المجتمعي والأجهزة المنزلية", titleEn: "Home care, community nursing & home devices cycle", domain: "clinical", specialisedRoute: "home-care-cycle", slaHours: 48, status: "active" },
  { code: "CYCLE-246", catalogNumber: 246, titleAr: "دورة التأهيل الطبي والعلاج الطبيعي والوظيفي والنطق", titleEn: "Rehabilitation, physical, occupational & speech therapy cycle", domain: "clinical", specialisedRoute: "rehab-cycle", slaHours: 72, status: "active" },
  { code: "CYCLE-251", catalogNumber: 251, titleAr: "دورة التغذية العلاجية والتغذية المعوية والوريدية", titleEn: "Clinical nutrition, enteral & parenteral feeding cycle", domain: "clinical", specialisedRoute: "nutrition-cycle", slaHours: 24, status: "active" },
  { code: "CYCLE-252", catalogNumber: 252, titleAr: "دورة الصحة النفسية وعلاج الإدمان", titleEn: "Mental health & addiction treatment cycle", domain: "clinical", specialisedRoute: "mental-health-cycle", slaHours: 24, status: "active" },
  { code: "CYCLE-245", catalogNumber: 245, titleAr: "دورة حديثي الولادة والفحص المبكر والحضانة", titleEn: "Newborn, early screening & NICU cycle", domain: "clinical", specialisedRoute: "neonatal-cycle", slaHours: 24, status: "active" },
];

const demoInstances: CycleInstance[] = [
  { id: "c1111111-1111-4111-8111-111111111111", facilityId: DEMO_FACILITY_ID, definitionCode: "CYCLE-224", title: "إحالة حالة قلب عاجلة إلى مركز قسطرة", status: "in_progress", currentStep: "انتظار قبول الجهة المستقبلة", protocolVersion: "REF-2026.1", correlationId: "corr-demo-224-1", idempotencyKey: "demo-224-1", slaDueAt: "2026-11-22T20:00:00Z", escalated: false, createdAt: "2026-11-22T08:20:00Z", updatedAt: "2026-11-22T09:00:00Z" },
  { id: "c2222222-2222-4222-8222-222222222222", facilityId: DEMO_FACILITY_ID, definitionCode: "CYCLE-219", title: "نتيجة حرجة: بوتاسيوم 6.1", status: "pending_review", currentStep: "مراجعة الطبيب المعالج", protocolVersion: "LAB-2026.3", correlationId: "corr-demo-219-1", idempotencyKey: "demo-219-1", slaDueAt: "2026-11-22T18:00:00Z", escalated: true, createdAt: "2026-11-22T07:10:00Z", updatedAt: "2026-11-22T08:45:00Z" },
];

export const demoCapacity: GovernorateCapacity[] = [
  { governorate: "القاهرة", facilities: 148, totalBeds: 18240, occupiedBeds: 14910, icuBeds: 1210, icuOccupied: 986, ambulancesAvailable: 96 },
  { governorate: "الجيزة", facilities: 121, totalBeds: 13980, occupiedBeds: 11230, icuBeds: 940, icuOccupied: 731, ambulancesAvailable: 78 },
  { governorate: "الإسكندرية", facilities: 86, totalBeds: 9240, occupiedBeds: 6810, icuBeds: 610, icuOccupied: 402, ambulancesAvailable: 54 },
  { governorate: "الدقهلية", facilities: 94, totalBeds: 8610, occupiedBeds: 6430, icuBeds: 480, icuOccupied: 351, ambulancesAvailable: 47 },
  { governorate: "أسوان", facilities: 38, totalBeds: 3120, occupiedBeds: 1980, icuBeds: 170, icuOccupied: 96, ambulancesAvailable: 22 },
];

export const demoKpis: NationalKpis = {
  facilitiesActive: 3120,
  patientsRegistered: 4821930,
  encountersToday: 61240,
  bedOccupancyPct: 78.4,
  icuOccupancyPct: 71.2,
  ambulancesAvailable: 842,
  openCycleInstances: 19384,
  slaBreaches: 317,
};

// ------------------------------------------------------------------- reads
export const api = {
  kpis: () => read<NationalKpis>("/api/dashboard/kpis", () => demoKpis),
  capacity: () => read<GovernorateCapacity[]>("/api/dashboard/capacity-by-governorate", () => demoCapacity),
  facilities: () => read<Facility[]>("/api/facilities", () => [...demoFacilities]),
  patients: (q = "") =>
    read<Patient[]>(`/api/patients${q ? `?q=${encodeURIComponent(q)}` : ""}`, () =>
      demoPatients.filter((p) => !q || p.fullNameAr.includes(q) || p.nationalId.includes(q)),
    ),
  encounters: () => read<Encounter[]>("/api/encounters", () => [...demoEncounters]),
  cycleDefinitions: () =>
    read<CycleDefinition[]>("/api/cycle-engine/definitions", () => [...demoCycleDefinitions]),
  cycleInstances: () => read<CycleInstance[]>("/api/cycle-engine/instances", () => [...demoInstances]),

  // ----------------------------------------------------------------- writes
  async registerPatient(input: Omit<Patient, "id">): Promise<ApiResult<Patient>> {
    try {
      const data = await request<Patient>("/api/patients", { method: "POST", body: JSON.stringify(input) });
      return { data, demo: false };
    } catch {
      const patient: Patient = { ...input, id: crypto.randomUUID(), createdAt: new Date().toISOString() };
      demoPatients.unshift(patient);
      return { data: patient, demo: true };
    }
  },

  async createCycleInstance(input: {
    definitionCode: string;
    title: string;
    subjectType?: string;
    subjectRef?: string;
    protocolVersion?: string;
  }): Promise<ApiResult<CycleInstance>> {
    try {
      const data = await request<CycleInstance>("/api/cycle-engine/instances", {
        method: "POST",
        body: JSON.stringify({ ...input, facilityId: DEMO_FACILITY_ID, idempotencyKey: crypto.randomUUID() }),
      });
      return { data, demo: false };
    } catch {
      const now = new Date().toISOString();
      const inst: CycleInstance = {
        id: crypto.randomUUID(),
        facilityId: DEMO_FACILITY_ID,
        definitionCode: input.definitionCode,
        title: input.title,
        subjectType: input.subjectType ?? null,
        subjectRef: input.subjectRef ?? null,
        status: "draft",
        currentStep: null,
        protocolVersion: input.protocolVersion ?? null,
        correlationId: crypto.randomUUID(),
        idempotencyKey: crypto.randomUUID(),
        slaDueAt: null,
        escalated: false,
        createdAt: now,
        updatedAt: now,
      };
      demoInstances.unshift(inst);
      return { data: inst, demo: true };
    }
  },

  async transitionCycleInstance(
    id: string,
    toStatus: CycleStatus,
    reason?: string,
  ): Promise<ApiResult<CycleInstance>> {
    try {
      const data = await request<CycleInstance>(`/api/cycle-engine/instances/${id}/transition`, {
        method: "POST",
        body: JSON.stringify({ toStatus, reason: reason ?? null }),
      });
      return { data, demo: false };
    } catch {
      const inst = demoInstances.find((i) => i.id === id);
      if (!inst) throw new Error("instance_not_found");
      inst.status = toStatus;
      inst.updatedAt = new Date().toISOString();
      return { data: { ...inst }, demo: true };
    }
  },

  /**
   * Generic list reader for the module endpoints added in batches 2-6 of the
   * Java core (labs, pharmacy, blood bank, referrals, billing, inventory,
   * appointments, dialysis, oncology, dispatch, incidents, complaints,
   * workforce, consents, research, documents...). Each core response is an
   * envelope { ok, <key>: [...], count }; demo fallback returns sample rows.
   */
  /** Operational write: real API only. A failed write throws —
   *  operational actions are never faked into a demo store. */
  async postJson<T>(path: string, body?: unknown): Promise<T> {
    return request<T>(path, {
      method: "POST",
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  },

  async moduleList<T>(path: string, key: string, demoRows: () => T[]): Promise<ApiResult<T[]>> {
    try {
      const env = await request<Record<string, unknown>>(path);
      const rows = (env[key] as T[] | undefined) ?? [];
      return { data: rows, demo: false };
    } catch {
      return { data: demoRows(), demo: true };
    }
  },

  async login(body: LoginRequest): Promise<ApiResult<LoginResponse>> {
    try {
      const data = await request<LoginResponse>("/api/auth/login", {
        method: "POST",
        body: JSON.stringify(body),
      });
      setToken(data.accessToken);
      return { data, demo: false };
    } catch {
      // Demo session only — clearly flagged, never presented as a real OIDC token.
      const data: LoginResponse = {
        accessToken: "demo-session-token",
        tokenType: "Bearer",
        expiresInSeconds: 3600,
        user: { id: "99999999-9999-4999-8999-999999999999", fullNameAr: body.username, role: "facility_manager", facilityId: DEMO_FACILITY_ID },
      };
      setToken(data.accessToken);
      return { data, demo: true };
    }
  },
};
