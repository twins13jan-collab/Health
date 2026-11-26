"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { getSessionUser, type SessionUser } from "@/lib/api";
import { useLang } from "@/lib/i18n";
import { facilityTypeLabel } from "@/lib/types";
import { SectionHead, SegChips } from "@/components/ui";

type ServiceFacility = {
  id: string; nameAr: string; nameEn: string | null; type: string; level: string;
  governorate: string; city: string | null; bedCapacity: number | null; services: string[];
};
type BookableSvc = { key: string; nameAr: string; nameEn: string; mode: string; portalType: string | null; facilityTypes: string[] };

const SERVICE_LABELS: Record<string, { ar: string; en: string }> = {
  clinics: { ar: "عيادات خارجية", en: "Outpatient clinics" },
  emergency: { ar: "طوارئ", en: "Emergency" },
  lab: { ar: "تحاليل معملية", en: "Laboratory tests" },
  pharmacy: { ar: "طلب أدوية", en: "Medication request" },
  radiology: { ar: "أشعة وتصوير طبي", en: "Radiology & imaging" },
  dialysis: { ar: "غسيل كلوي", en: "Dialysis" },
  home_visit: { ar: "زيارة منزلية", en: "Home visit" },
  vaccination: { ar: "تطعيمات", en: "Vaccinations" },
  blood: { ar: "تبرع / طلب دم", en: "Blood donation / request" },
};
const TYPE_FILTERS = ["", "hospital", "health_unit", "family_center", "lab", "pharmacy", "blood_bank", "dialysis_center", "oncology_center"];

/** ابحث واحجز خدمة — the public service finder, living on the
 *  Services page: ALL 27 governorates, the bookable-services
 *  catalog (hospital and private clinics, medical centres,
 *  radiology centres, analysis labs, pharmacies for medication
 *  requests, dialysis, home visits, vaccinations, blood), and the
 *  facility types — which narrow to only the types offering the
 *  chosen service, with no duplicate labels. Booking deep-links
 *  into the patient portal with facility + service preselected. */
export default function ServiceFinder() {
  const { lang } = useLang();
  const ar = lang === "ar";
  const [user, setUser] = useState<SessionUser | null>(null);
  const [governorates, setGovernorates] = useState<{ governorate: string; facilities: number }[]>([]);
  const [gov, setGov] = useState("");
  const [typeFilter, setTypeFilter] = useState("");
  const [serviceFilter, setServiceFilter] = useState("");
  const [allGovs, setAllGovs] = useState<{ code: string; nameAr: string; nameEn: string }[]>([]);
  const [allTypes, setAllTypes] = useState<string[]>([]);
  const [catalog, setCatalog] = useState<BookableSvc[]>([]);
  const [finder, setFinder] = useState<ServiceFacility[]>([]);
  const [finderLoading, setFinderLoading] = useState(false);

  useEffect(() => { setUser(getSessionUser()); }, []);

  const load = useCallback(async () => {
    try {
      const [g, h, bs] = await Promise.all([
        fetch("/api/public/governorates").then((r) => (r.ok ? r.json() : null)),
        fetch("/api/public/hierarchy").then((r) => (r.ok ? r.json() : null)),
        fetch("/api/public/bookable-services").then((r) => (r.ok ? r.json() : null)),
      ]);
      if (g?.governorates) setGovernorates(g.governorates);
      if (h?.governorates) {
        setAllGovs(h.governorates);
        setAllTypes((h.facilityTypes ?? []).map((x: { value: string }) => x.value));
      }
      if (bs?.services) setCatalog(bs.services);
    } catch { /* filters fall back to the static lists */ }
  }, []);
  useEffect(() => { load(); }, [load]);

  const loadFinder = useCallback(async () => {
    setFinderLoading(true);
    try {
      const qs = new URLSearchParams();
      if (gov) qs.set("governorate", gov);
      if (typeFilter) qs.set("type", typeFilter);
      if (serviceFilter) qs.set("service", serviceFilter);
      const r = await fetch(`/api/public/services?${qs.toString()}`);
      const data = r.ok ? await r.json() : null;
      setFinder(data?.facilities ?? []);
    } catch { setFinder([]); } finally { setFinderLoading(false); }
  }, [gov, typeFilter, serviceFilter]);
  useEffect(() => { loadFinder(); }, [loadFinder]);

  const typeLabel = (ty: string) => facilityTypeLabel(ty, lang);
  const bookHref = (facilityId: string, serviceKey?: string) =>
    user ? `/my?facility=${facilityId}${serviceKey ? `&service=${serviceKey}` : ""}` : "/register";
  const svcLabel = (key: string) => {
    const c = catalog.find((x) => x.key === key);
    if (c) return ar ? c.nameAr : c.nameEn;
    return ar ? SERVICE_LABELS[key]?.ar ?? key : SERVICE_LABELS[key]?.en ?? key;
  };
  const svcMode = (key: string) => catalog.find((x) => x.key === key)?.mode ?? "request";
  /** Facility types narrow to the chosen service: only the types
   *  that actually offer it are shown. The lab/laboratory synonym
   *  pair collapses to the single canonical "lab". */
  const typeOptions = (() => {
    const base = serviceFilter
      ? catalog.find((x) => x.key === serviceFilter)?.facilityTypes ?? []
      : allTypes.length ? allTypes : TYPE_FILTERS.filter(Boolean);
    const out: string[] = [];
    for (const ty of base) {
      const v = ty === "laboratory" ? "lab" : ty;
      if (!out.includes(v)) out.push(v);
    }
    return out;
  })();
  useEffect(() => {
    if (typeFilter && !typeOptions.includes(typeFilter)) setTypeFilter("");
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [serviceFilter, catalog, allTypes]);

  return (
    <div id="book">
      <SectionHead
        eyebrow={ar ? "احجز خدمة" : "Book a service"}
        title={ar ? "ابحث واحجز خدمة" : "Find & book a service"}
        sub={ar ? "اختر الخدمة التي تريد حجزها والمحافظة ونوع المنشأة — عيادات المستشفيات والعيادات الخاصة والمراكز الطبية ومراكز الأشعة ومعامل التحاليل والصيدليات لطلب الأدوية وغيرها، والحجز نفسه يتم من حسابك الصحي." : "Choose the service you want to book, the governorate and the facility type — hospital clinics, private clinics, medical centres, radiology centres, analysis labs, pharmacies for medication requests and more; booking itself happens in your health account."}
      />
      <div className="card">
        <div className="row">
          <div className="field grow-field">
            <label>{ar ? "المحافظة" : "Governorate"}</label>
            <select value={gov} onChange={(e) => setGov(e.target.value)} aria-label={ar ? "المحافظة" : "Governorate"}>
              <option value="">{ar ? "كل المحافظات" : "All governorates"}</option>
              {(allGovs.length ? allGovs.map((g) => ({ value: g.nameAr, label: ar ? g.nameAr : g.nameEn })) : governorates.map((g) => ({ value: g.governorate, label: g.governorate }))).map((g) => {
                const count = governorates.find((x) => x.governorate === g.value)?.facilities;
                return <option key={g.value} value={g.value}>{g.label}{count ? ` (${count})` : ""}</option>;
              })}
            </select>
          </div>
        </div>
        <div className="mt">
          <label className="finder-label">{ar ? "الخدمة المطلوبة" : "Service"}</label>
          <SegChips
            value={serviceFilter}
            onChange={setServiceFilter}
            options={[{ value: "", label: ar ? "كل الخدمات" : "All services" },
              ...(catalog.length ? catalog.map((sv) => ({ value: sv.key, label: ar ? sv.nameAr : sv.nameEn }))
                                 : Object.keys(SERVICE_LABELS).map((k) => ({ value: k, label: svcLabel(k) })))]}
          />
        </div>
        <div className="mt">
          <label className="finder-label">{ar ? "نوع المنشأة" : "Facility type"}</label>
          <SegChips
            value={typeFilter}
            onChange={setTypeFilter}
            options={["", ...typeOptions].map((ty) => ({ value: ty, label: ty === "" ? (ar ? "كل الأنواع" : "All types") : typeLabel(ty) }))}
          />
          {serviceFilter ? <p className="muted">{ar ? "الأنواع المعروضة هي فقط التي تقدم الخدمة المختارة." : "Only the facility types that offer the chosen service are shown."}</p> : null}
        </div>
        {finderLoading ? <p className="muted">{ar ? "جارٍ التحميل…" : "Loading…"}</p>
        : finder.length === 0 ? <p className="muted">{ar ? "لا منشآت مطابقة حاليًا." : "No matching facilities right now."}</p>
        : (
          <div className="fac-grid">
            {finder.map((f) => (
              <div key={f.id} className="fac-card">
                <strong>{ar ? f.nameAr : (f.nameEn || f.nameAr)}</strong>
                <div className="fc-meta">
                  {typeLabel(f.type)} · {f.governorate}{f.city ? ` · ${f.city}` : ""}{f.bedCapacity ? ` · ${f.bedCapacity} ${ar ? "سرير" : "beds"}` : ""}
                </div>
                <div>
                  {f.services.map((sv) => (
                    <span key={sv} className="chip">{svcLabel(sv)}</span>
                  ))}
                </div>
                <div className="row mt">
                  {serviceFilter && f.services.includes(serviceFilter) ? (
                    svcMode(serviceFilter) === "info"
                      ? <span className="badge warn">{ar ? "متاحة على مدار الساعة — توجه مباشرة" : "Open 24/7 — walk in"}</span>
                      : <Link className="btn btn-sm" href={bookHref(f.id, serviceFilter)}>{svcMode(serviceFilter) === "session" ? (ar ? "احجز موعد" : "Book appointment") : (ar ? "اطلب الخدمة" : "Request service")}</Link>
                  ) : (
                    <>
                      {f.services.includes("clinics") && (
                        <Link className="btn btn-sm" href={bookHref(f.id, "clinics")}>{ar ? "احجز موعد عيادة" : "Book a clinic appointment"}</Link>
                      )}
                      <Link className="btn btn-soft btn-sm" href={bookHref(f.id)}>{ar ? "اطلب خدمة" : "Request a service"}</Link>
                    </>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
