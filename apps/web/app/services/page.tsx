"use client";

import { useCallback, useEffect, useState } from "react";
import { useLang } from "@/lib/i18n";
import { SectionHead } from "@/components/ui";
import ServiceFinder from "@/components/ServiceFinder";

/**
 * الخدمات — the services the Khattab platform actually operates,
 * each with its live availability read from the public aggregate
 * API. No domain is listed that the backend does not run.
 */
const FEATURES: { icon: string; ar: string; en: string; arBody: string; enBody: string }[] = [
  { icon: "🏥", ar: "إدارة المستشفيات", en: "Hospital Management", arBody: "استقبال وقبول وخروج وتحويلات داخلية، طوارئ وفرز، عيادات خارجية، أجنحة ولوحة أسرّة حية، تمريض وعمليات وعناية مركزة — تشغيل كامل لكل قسم وإدارة.", enBody: "Admissions, discharges and internal transfers, ER and triage, outpatient clinics, live bed boards, nursing, theatres and intensive care — every department fully operated." },
  { icon: "🚑", ar: "الطوارئ والأزمات", en: "Emergencies & Crises", arBody: "إرسال الإسعاف ومتابعة السعة لحظة بلحظة وتنسيق الإحالات بين المنشآت والمحافظات وإدارة الحوادث الكبرى بدورات عمل موثقة.", enBody: "Ambulance dispatch, live capacity tracking, referral coordination across facilities and governorates, and major-incident management with audited workflows." },
  { icon: "🧪", ar: "المعامل وبنوك الدم", en: "Labs & Blood Banks", arBody: "نظام معامل متكامل: استقبال العينات وقوائم العمل ومراقبة الجودة واعتماد النتائج والقيم الحرجة، وبنوك دم بتتبع كامل للوحدات.", enBody: "A full laboratory system: sample reception, worklists, quality control, result validation and critical values, plus fully traced blood-bank units." },
  { icon: "💊", ar: "الصيدليات والمخازن", en: "Pharmacies & Stores", arBody: "طابور وصفات وصرف بالتشغيلات والأقدم انتهاءً، تنبيهات مخزون وصلاحية، سجل مواد مراقبة، ومشتريات ومخازن بحركات مسببة.", enBody: "Prescription queues and batch/FEFO dispensing, stock and expiry alerts, controlled-drug registers, and reasoned procurement and stores movements." },
  { icon: "🩺", ar: "المراكز والوحدات الصحية", en: "Health Centres & Units", arBody: "ملفات طب الأسرة وصحة الأم والطفل ومتابعة الحمل والتطعيمات وسلسلة التبريد وعيادات الأمراض المزمنة والإحالة لأعلى.", enBody: "Family health files, maternal and child health, pregnancy follow-up, immunisation and cold chain, chronic-disease clinics and upward referral." },
  { icon: "📊", ar: "التحليلات والتنبؤ", en: "Analytics & Forecasting", arBody: "مؤشرات وطنية حية وتنبؤات بالتفشي والسعة ومقاومة المضادات — دعم قرار استشاري يراجعه الإنسان دائمًا قبل أي إجراء.", enBody: "Live national indicators with outbreak, capacity and antimicrobial-resistance forecasting — advisory decision support that always keeps a human in the loop." },
];

type Kpis = { facilitiesActive: number; ambulancesAvailable: number };
type FacilityRow = { wardBeds: number; inpatients: number; erWaiting: number };

export default function ServicesPage() {
  const { lang, t } = useLang();
  const ar = lang === "ar";
  const [kpis, setKpis] = useState<Kpis | null>(null);
  const [facilities, setFacilities] = useState<FacilityRow[]>([]);

  const load = useCallback(async () => {
    try {
      const [s, f] = await Promise.all([
        fetch("/api/public/summary").then((r) => (r.ok ? r.json() : null)),
        fetch("/api/public/facilities").then((r) => (r.ok ? r.json() : null)),
      ]);
      if (s?.kpis) setKpis(s.kpis);
      if (f?.facilities) setFacilities(f.facilities);
    } catch { /* static content still renders */ }
  }, []);

  useEffect(() => {
    load();
    const id = window.setInterval(load, 60000);
    return () => window.clearInterval(id);
  }, [load]);

  const totalErWaiting = facilities.reduce((a, f) => a + (f.erWaiting || 0), 0);
  const totalFreeBeds = facilities.reduce((a, f) => a + Math.max(0, (f.wardBeds || 0) - (f.inpatients || 0)), 0);

  return (
    <>
      <ServiceFinder />
      <SectionHead eyebrow={ar ? "ماذا تشغّل المنظومة" : "What the platform operates"} title={t("servicesTitle")} sub={t("servicesSub")} />
      <div className="svc-grid">
        {FEATURES.map((f, i) => (
          <div key={f.en} className="svc-card">
            <div className="svc-ico" aria-hidden>{f.icon}</div>
            <h3>{ar ? f.ar : f.en}</h3>
            <p>{ar ? f.arBody : f.enBody}</p>
            {i === 1 && kpis ? <span className="live-hint">🚑 {kpis.ambulancesAvailable} {t("availableNow")} · {totalErWaiting} {t("erWaitingNow")}</span> : null}
            {i === 0 && kpis ? <span className="live-hint">🛏 {totalFreeBeds} {t("bedsFreeWord")}</span> : null}
            {i === 2 && kpis ? <span className="live-hint">🏥 {kpis.facilitiesActive} {t("unitsWord")}</span> : null}
          </div>
        ))}
      </div>
    </>
  );
}
