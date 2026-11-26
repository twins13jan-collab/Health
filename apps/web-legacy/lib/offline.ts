/**
 * Offline work client (completion batch 9) — the browser half of
 * cycle 237. The device (this browser) registers once and works
 * inside a bounded server-issued package. Writes attempted while
 * the network is down are queued locally with stable local ids
 * and synced as ONE idempotent envelope: replaying it never
 * duplicates a transaction, and clinical/financial conflicts
 * become server-side cases for a manager — never silent
 * last-write-wins.
 */
import { api, getToken } from "./api";

const DEVICE_CODE_KEY = "ehs.deviceCode";
const DEVICE_ID_KEY = "ehs.deviceId";
const PACKAGE_ID_KEY = "ehs.packageId";
const QUEUE_KEY = "ehs.offlineQueue";
export const REFERENCE_DATA_VERSION = 3;

export interface OfflineTx {
  localId: string;
  operationType: string;
  domain: string;
  payloadHash: string;
  clientTime: string;
  referenceDataVersion: number;
  conflictsWithServer: boolean;
  localSummary: string;
  serverSummary?: string;
}

function uuid(): string {
  return typeof crypto !== "undefined" && "randomUUID" in crypto
    ? crypto.randomUUID()
    : `tx-${Date.now()}-${Math.floor(Math.random() * 1e9)}`;
}

function hash(text: string): string {
  let h = 0;
  for (let i = 0; i < text.length; i++) h = (h * 31 + text.charCodeAt(i)) | 0;
  return `h${(h >>> 0).toString(16)}`;
}

export function pendingOffline(): OfflineTx[] {
  if (typeof window === "undefined") return [];
  try { return JSON.parse(window.localStorage.getItem(QUEUE_KEY) ?? "[]") as OfflineTx[]; } catch { return []; }
}

export function queueOffline(operationType: string, domain: string, summary: string, payload: unknown): OfflineTx {
  const tx: OfflineTx = {
    localId: uuid(),
    operationType,
    domain,
    payloadHash: hash(JSON.stringify(payload ?? {})),
    clientTime: new Date().toISOString(),
    referenceDataVersion: REFERENCE_DATA_VERSION,
    conflictsWithServer: false,
    localSummary: summary,
  };
  const q = pendingOffline();
  q.push(tx);
  window.localStorage.setItem(QUEUE_KEY, JSON.stringify(q));
  return tx;
}

interface IdEnvelope { id?: string }
interface Wrapped<T> { [key: string]: T | unknown }

async function ensureDeviceAndPackage(): Promise<string> {
  const existingPkg = window.localStorage.getItem(PACKAGE_ID_KEY);
  if (existingPkg) return existingPkg;
  let deviceCode = window.localStorage.getItem(DEVICE_CODE_KEY);
  if (!deviceCode) { deviceCode = `web-${uuid()}`; window.localStorage.setItem(DEVICE_CODE_KEY, deviceCode); }
  let deviceId = window.localStorage.getItem(DEVICE_ID_KEY);
  if (!deviceId) {
    const res = await api.postJson<Wrapped<IdEnvelope>>("/api/offline/devices", {
      deviceCode, userRef: "web-client", encryptionEnabled: true,
    });
    deviceId = (res.device as IdEnvelope)?.id ?? "";
    window.localStorage.setItem(DEVICE_ID_KEY, deviceId);
  }
  const validUntil = new Date(Date.now() + 7 * 24 * 3600 * 1000).toISOString();
  const res = await api.postJson<Wrapped<IdEnvelope>>("/api/offline/packages", {
    deviceId, userRef: "web-client", scope: "*", allowedData: "registrations,encounters",
    referenceDataVersion: REFERENCE_DATA_VERSION, validUntil,
  });
  const pkgId = (res.package as IdEnvelope)?.id ?? "";
  window.localStorage.setItem(PACKAGE_ID_KEY, pkgId);
  return pkgId;
}

export interface SyncResult { applied?: number; conflicted?: number; rejected?: number; duplicates?: number; duplicate?: boolean }

export async function syncOfflineQueue(): Promise<{ result: SyncResult; sent: number }> {
  const q = pendingOffline();
  if (!getToken()) throw new Error("not_authenticated");
  if (q.length === 0) return { result: {}, sent: 0 };
  const pkgId = await ensureDeviceAndPackage();
  const res = await api.postJson<SyncResult & Wrapped<unknown>>(`/api/offline/packages/${pkgId}/sync`, {
    syncId: uuid(),
    contentHash: hash(q.map((t) => t.localId).join(",")),
    transactions: q,
  });
  // The envelope was accepted (applied or filed as conflicts/duplicates
  // server-side): the local copies have done their job.
  window.localStorage.setItem(QUEUE_KEY, "[]");
  return { result: res as SyncResult, sent: q.length };
}
