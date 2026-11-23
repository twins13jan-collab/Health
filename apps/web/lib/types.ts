/**
 * Core domain types mirroring the Java core (Spring Boot) contracts.
 * Catalog v6 — Expanded Functional Catalog, Deep Engineering appendices.
 * Field names follow the REST/OpenAPI contract of the core services.
 */

export type UUID = string;

export type FacilityLevel = "primary" | "secondary" | "tertiary" | "quaternary";
export type FacilityType =
  | "general_hospital"
  | "university_hospital"
  | "health_unit"
  | "family_center"
  | "specialized_center"
  | "emergency_hospital";

export interface Facility {
  id: UUID;
  code: string;
  nameAr: string;
  nameEn: string;
  type: FacilityType;
  level: FacilityLevel;
  governorate: string;
  directorate?: string;
  city?: string;
  totalBeds: number;
  occupiedBeds: number;
  icuBeds: number;
  icuOccupied: number;
  status: "active" | "suspended" | "under_construction";
}

export type Gender = "male" | "female";

export interface Patient {
  id: UUID;
  /** Egyptian national ID (الرقم القومي) — the MPI linkage key. */
  nationalId: string;
  fullNameAr: string;
  fullNameEn?: string;
  dateOfBirth: string; // ISO date
  gender: Gender;
  governorate?: string;
  phone?: string;
  bloodType?: string;
  chronicConditions?: string[];
  registeredFacilityId?: UUID;
  createdAt?: string;
}

export type TriageLevel = "resuscitation" | "emergent" | "urgent" | "less_urgent" | "non_urgent";
export type EncounterStatus = "registered" | "triaged" | "in_consultation" | "admitted" | "discharged" | "referred" | "closed";

export interface Encounter {
  id: UUID;
  patientId: UUID;
  patientNameAr: string;
  facilityId: UUID;
  facilityNameAr: string;
  chiefComplaint: string;
  triageLevel: TriageLevel;
  status: EncounterStatus;
  arrivedAt: string; // ISO datetime
  news2Score?: number;
}

/** Operating-cycle lifecycle, Catalog v6 mandatory specification method. */
export type CycleStatus =
  | "draft"
  | "open"
  | "in_progress"
  | "on_hold"
  | "pending_review"
  | "completed"
  | "closed"
  | "cancelled"
  | "failed"
  | "corrected"
  | "reconciled";

export interface CycleDefinition {
  code: string; // e.g. "CYCLE-217"
  catalogNumber: number; // 217..254
  titleAr: string;
  titleEn?: string;
  domain: string;
  specialisedRoute?: string | null;
  slaHours: number;
  status: "active" | "archived";
}

export interface CycleInstance {
  id: UUID;
  facilityId: UUID;
  definitionCode: string;
  title: string;
  subjectType?: string | null;
  subjectRef?: string | null;
  patientId?: UUID | null;
  status: CycleStatus;
  currentStep?: string | null;
  protocolVersion?: string | null;
  correlationId: string;
  idempotencyKey?: string | null;
  slaDueAt?: string | null;
  escalated: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CycleEvent {
  id: UUID;
  instanceId: UUID;
  eventType: "created" | "transition" | "escalated";
  fromStatus: CycleStatus | null;
  toStatus: CycleStatus | null;
  actorRole?: string | null;
  reason?: string | null;
  createdAt: string;
}

export interface GovernorateCapacity {
  governorate: string;
  facilities: number;
  totalBeds: number;
  occupiedBeds: number;
  icuOccupied: number;
  icuBeds: number;
  ambulancesAvailable: number;
}

export interface NationalKpis {
  facilitiesActive: number;
  patientsRegistered: number;
  encountersToday: number;
  bedOccupancyPct: number;
  icuOccupancyPct: number;
  ambulancesAvailable: number;
  openCycleInstances: number;
  slaBreaches: number;
}

export interface LoginRequest {
  username: string;
  password: string;
  facilityCode?: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
  user: { id: UUID; fullNameAr: string; role: string; facilityId?: UUID };
}

/** Allowed transitions — mirror of the unified state machine in the core. */
export const CYCLE_TRANSITIONS: Record<CycleStatus, CycleStatus[]> = {
  draft: ["open"],
  open: ["in_progress", "cancelled"],
  in_progress: ["on_hold", "pending_review", "failed"],
  on_hold: ["in_progress"],
  pending_review: ["in_progress", "completed"],
  completed: ["closed", "corrected"],
  corrected: ["closed"],
  failed: ["reconciled"],
  reconciled: ["closed"],
  cancelled: ["reconciled"],
  closed: [],
};

export const REASON_REQUIRED: ReadonlySet<CycleStatus> = new Set([
  "cancelled",
  "failed",
  "corrected",
  "reconciled",
  "on_hold",
]);
