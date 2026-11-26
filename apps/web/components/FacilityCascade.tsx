"use client";

import { useEffect, useMemo, useState } from "react";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";
import { FACILITY_TYPE_LABELS } from "@/lib/types";

export interface FacilityRow {
  id: string; nameAr: string; nameEn?: string; type: string;
  governorate: string; directorate?: string; city?: string; ownership?: string;
}
interface GovRow { code: string; nameAr: string; nameEn: string; directorateAr?: string; directorateEn?: string }
interface TypeRow { value: string; nameAr: string; nameEn: string }

export const OWNERSHIPS: { v: string; ar: string; en: string }[] = [
  { v: "moh", ar: "وزارة الصحة والسكان", en: "Ministry of Health & Population" },
  { v: "university", ar: "الجامعات والمستشفيات الجامعية والتعليمية", en: "Universities & teaching hospitals" },
  { v: "insurance", ar: "هيئة التأمين الصحي", en: "Health Insurance Organisation" },
  { v: "private", ar: "القطاع الخاص", en: "Private sector" },
  { v: "military", ar: "القوات المسلحة", en: "Armed Forces" },
  { v: "police", ar: "وزارة الداخلية (الشرطة)", en: "Police" },
  { v: "ngo", ar: "منظمات أهلية", en: "NGOs" },
  { v: "sectoral", ar: "جهات قطاعية أخرى", en: "Other sectoral bodies" },
];

export function ownershipLabel(v: string | undefined, lang: "ar" | "en"): string {
  const o = OWNERSHIPS.find((x) => x.v === v);
  return o ? (lang === "ar" ? o.ar : o.en) : (v ?? "—");
}

/** Governorate values are stored across the platform in three
 *  forms (code / Arabic / English) — a facility matches its
 *  governorate in any of them. */
function govMatches(f: FacilityRow, g: GovRow): boolean {
  const v = (f.governorate ?? "").trim();
  return v === g.code || v === g.nameAr || v.toLowerCase() === g.nameEn.toLowerCase();
}

/** The hierarchy picker used by registration and sign-in alike:
 *  entity → ALL 27 governorates → the governorate's health
 *  directorate (named after it) → the platform's complete
 *  facility-type taxonomy → the facility itself by name. The
 *  governorate, directorate and type lists are complete by
 *  construction (from /api/public/hierarchy); only the final
 *  facility step depends on registered facilities. */
export default function FacilityCascade({ onPick }: { onPick: (f: FacilityRow | null) => void }) {
  const { lang } = useLang();
  const ar = lang === "ar";
  const [rows, setRows] = useState<FacilityRow[]>([]);
  const [govs, setGovs] = useState<GovRow[]>([]);
  const [types, setTypes] = useState<TypeRow[]>([]);
  const [entity, setEntity] = useState("");
  const [govCode, setGovCode] = useState("");
  const [dir, setDir] = useState("");
  const [type, setType] = useState("");
  const [facId, setFacId] = useState("");

  useEffect(() => {
    api.getJson<{ facilities?: FacilityRow[]; rows?: FacilityRow[] }>("/api/public/facilities")
      .then((r) => setRows(r.facilities ?? r.rows ?? []))
      .catch(() => setRows([]));
    api.getJson<{ governorates: GovRow[]; facilityTypes: TypeRow[] }>("/api/public/hierarchy")
      .then((r) => { setGovs(r.governorates ?? []); setTypes(r.facilityTypes ?? []); })
      .catch(() => {
        setTypes(Object.keys(FACILITY_TYPE_LABELS).map((v) => ({ value: v, nameAr: FACILITY_TYPE_LABELS[v].ar, nameEn: FACILITY_TYPE_LABELS[v].en })));
      });
  }, []);

  const gov = govs.find((g) => g.code === govCode) ?? null;
  const byEntity = useMemo(() => rows.filter((r) => !entity || r.ownership === entity), [rows, entity]);
  const byGov = useMemo(() => byEntity.filter((r) => !gov || govMatches(r, gov)), [byEntity, gov]);
  const dirOptions = useMemo(() => {
    const present = [...new Set(byGov.map((r) => r.directorate).filter(Boolean))] as string[];
    const official = gov?.directorateAr;
    return official && !present.includes(official) ? [official, ...present] : present;
  }, [byGov, gov]);
  const byDir = useMemo(() => byGov.filter((r) => !dir || r.directorate === dir), [byGov, dir]);
  const facs = useMemo(() => byDir.filter((r) => !type || r.type === type), [byDir, type]);

  const reset = (level: number) => {
    if (level <= 2) { setDir(""); }
    setFacId(""); onPick(null);
  };

  return (
    <div className="form-grid">
      <div className="field"><label>{ar ? "الجهة / الكيان التابع له" : "Entity / authority"}</label>
        <select value={entity} onChange={(e) => { setEntity(e.target.value); reset(1); }}>
          <option value="">{ar ? "كل الجهات" : "All entities"}</option>
          {OWNERSHIPS.map((o) => <option key={o.v} value={o.v}>{ar ? o.ar : o.en}</option>)}
        </select></div>
      <div className="field"><label>{ar ? "المحافظة" : "Governorate"}</label>
        <select value={govCode} onChange={(e) => { setGovCode(e.target.value); reset(2); }}>
          <option value="">{ar ? "كل المحافظات" : "All governorates"}</option>
          {govs.map((g) => <option key={g.code} value={g.code}>{ar ? g.nameAr : g.nameEn}</option>)}
        </select></div>
      <div className="field"><label>{ar ? "مديرية الصحة" : "Health directorate"}</label>
        <select value={dir} onChange={(e) => { setDir(e.target.value); setFacId(""); onPick(null); }}>
          <option value="">{ar ? "كل المديريات" : "All directorates"}</option>
          {dirOptions.map((d) => <option key={d} value={d}>{d}</option>)}
        </select></div>
      <div className="field"><label>{ar ? "نوع المنشأة" : "Facility type"}</label>
        <select value={type} onChange={(e) => { setType(e.target.value); setFacId(""); onPick(null); }}>
          <option value="">{ar ? "كل الأنواع" : "All types"}</option>
          {types.map((t) => <option key={t.value} value={t.value}>{ar ? t.nameAr : t.nameEn}</option>)}
        </select></div>
      <div className="field"><label>{ar ? "اسم المنشأة" : "Facility name"}</label>
        <select value={facId} onChange={(e) => { setFacId(e.target.value); onPick(facs.find((f) => f.id === e.target.value) ?? null); }}>
          <option value="">{ar ? "اختر المنشأة…" : "Choose the facility…"}</option>
          {facs.map((f) => <option key={f.id} value={f.id}>{f.nameAr}{f.city ? ` — ${f.city}` : ""}</option>)}
        </select></div>
    </div>
  );
}
