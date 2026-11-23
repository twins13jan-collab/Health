"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { setToken } from "@/lib/api";
import { useLang, type DictKey } from "@/lib/i18n";

const NAV: { href: string; key: DictKey; icon: string }[] = [
  { href: "/", key: "dashboard", icon: "◈" },
  { href: "/patients", key: "patients", icon: "☰" },
  { href: "/encounters", key: "encounters", icon: "✚" },
  { href: "/cycles", key: "cycles", icon: "↻" },
  { href: "/facilities", key: "facilities", icon: "⌂" },
  { href: "/labs", key: "labs", icon: "▸" },
  { href: "/radiology", key: "radiology", icon: "▸" },
  { href: "/pharmacy", key: "pharmacy", icon: "▸" },
  { href: "/blood-bank", key: "bloodBank", icon: "▸" },
  { href: "/referrals", key: "referrals", icon: "▸" },
  { href: "/capacity", key: "capacity", icon: "▸" },
  { href: "/billing", key: "billing", icon: "▸" },
  { href: "/inventory", key: "inventory", icon: "▸" },
  { href: "/appointments", key: "appointments", icon: "▸" },
  { href: "/programs", key: "programs", icon: "▸" },
  { href: "/dispatch", key: "dispatch", icon: "▸" },
  { href: "/quality", key: "quality", icon: "▸" },
  { href: "/workforce", key: "workforce", icon: "▸" },
  { href: "/governance", key: "governance", icon: "▸" },
  { href: "/surgery", key: "surgery", icon: "▸" },
  { href: "/icu", key: "icu", icon: "▸" },
  { href: "/maternity", key: "maternity", icon: "▸" },
  { href: "/neonatal", key: "neonatal", icon: "▸" },
  { href: "/rehab", key: "rehab", icon: "▸" },
  { href: "/mental-health", key: "mentalHealth", icon: "▸" },
  { href: "/palliative", key: "palliative", icon: "▸" },
  { href: "/home-care", key: "homeCare", icon: "▸" },
  { href: "/dental", key: "dental", icon: "▸" },
  { href: "/toxicology", key: "toxicology", icon: "▸" },
  { href: "/sensory", key: "sensory", icon: "▸" },
  { href: "/wounds", key: "wounds", icon: "▸" },
  { href: "/nutrition", key: "nutrition", icon: "▸" },
  { href: "/pathology", key: "pathology", icon: "▸" },
  { href: "/endoscopy", key: "endoscopy", icon: "▸" },
  { href: "/recalls", key: "recalls", icon: "▸" },
  { href: "/security-incidents", key: "securityIncidents", icon: "▸" },
  { href: "/national-reports", key: "nationalReports", icon: "▸" },
  { href: "/offline-sync", key: "offlineSync", icon: "▸" },
  { href: "/longitudinal-record", key: "longitudinal", icon: "▸" },
  { href: "/master-data", key: "masterData", icon: "▸" },
  { href: "/general-ledger", key: "ledger", icon: "▸" },
  { href: "/integrations", key: "integrations", icon: "▸" },
  { href: "/purchasing", key: "purchasing", icon: "▸" },
  { href: "/medical-assets", key: "medicalAssets", icon: "▸" },
  { href: "/complaints", key: "complaints", icon: "▸" },
  { href: "/clinical-ai", key: "clinicalAi", icon: "▸" },
  { href: "/documents", key: "documents", icon: "▸" },
  { href: "/oncology", key: "oncology", icon: "▸" },
  { href: "/cardiology", key: "cardiology", icon: "▸" },
  { href: "/stroke", key: "stroke", icon: "▸" },
  { href: "/infection-control", key: "infectionControl", icon: "▸" },
  { href: "/cssd", key: "cssd", icon: "▸" },
  { href: "/medical-waste", key: "medicalWaste", icon: "▸" },
  { href: "/facility-safety", key: "facilitySafety", icon: "▸" },
  { href: "/citizen-portal", key: "citizenPortal", icon: "▸" },
  { href: "/follow-up", key: "followUp", icon: "▸" },
  { href: "/eligibility", key: "eligibility", icon: "▸" },
  { href: "/unknown-patient", key: "unknownPatient", icon: "▸" },
  { href: "/organ-bank", key: "organBank", icon: "▸" },
  { href: "/clinical-trials", key: "clinicalTrials", icon: "▸" },
  { href: "/porterage", key: "porterage", icon: "▸" },
  { href: "/partnerships", key: "partnerships", icon: "▸" },
  { href: "/medical-content", key: "medicalContent", icon: "▸" },
  { href: "/geo-data", key: "geoData", icon: "▸" },
  { href: "/ux-governance", key: "uxGovernance", icon: "▸" },
  { href: "/service-desk", key: "serviceDesk", icon: "▸" },
  { href: "/complex-care", key: "complexCare", icon: "▸" },
  { href: "/school-health", key: "schoolHealth", icon: "▸" },
  { href: "/occupational-health", key: "occupationalHealth", icon: "▸" },
  { href: "/port-health", key: "portHealth", icon: "▸" },
  { href: "/medication-delivery", key: "medicationDelivery", icon: "▸" },
  { href: "/org-change", key: "orgChange", icon: "▸" },
  { href: "/hta", key: "hta", icon: "▸" },
  { href: "/anomaly-registry", key: "anomalyRegistry", icon: "▸" },
  { href: "/proms", key: "proms", icon: "▸" },
  { href: "/temp-workforce", key: "tempWorkforce", icon: "▸" },
  { href: "/construction", key: "construction", icon: "▸" },
  { href: "/digital-quality", key: "digitalQuality", icon: "▸" },
  { href: "/nursing", key: "nursing", icon: "▸" },
  { href: "/pain-sedation", key: "painSedation", icon: "▸" },
  { href: "/forensics", key: "forensics", icon: "▸" },
  { href: "/lab-safety", key: "labSafety", icon: "▸" },
  { href: "/clinical-comms", key: "clinicalComms", icon: "▸" },
  { href: "/device-vigilance", key: "deviceVigilance", icon: "▸" },
  { href: "/preventive", key: "preventive", icon: "▸" },
  { href: "/donation-campaigns", key: "donationCampaigns", icon: "▸" },
  { href: "/social-services", key: "socialServices", icon: "▸" },
  { href: "/patient-transport", key: "patientTransport", icon: "▸" },
  { href: "/hazmat", key: "hazmat", icon: "▸" },
  { href: "/transfer-safety", key: "transferSafety", icon: "▸" },
  { href: "/major-incidents", key: "majorIncidents", icon: "▸" },
  { href: "/disaster-ops", key: "disasterOps", icon: "▸" },
  { href: "/workforce-health", key: "workforceHealth", icon: "▸" },
  { href: "/env-health", key: "envHealth", icon: "▸" },
  { href: "/continuity", key: "continuity", icon: "▸" },
  { href: "/accreditation", key: "accreditation", icon: "▸" },
  { href: "/special-populations", key: "specialPopulations", icon: "▸" },
  { href: "/health-equity", key: "healthEquity", icon: "▸" },
  { href: "/ethics-committees", key: "ethicsCommittees", icon: "▸" },
  { href: "/legal-compliance", key: "legalCompliance", icon: "▸" },
  { href: "/institutional-risk", key: "institutionalRisk", icon: "▸" },
  { href: "/clinical-risk-matrix", key: "clinicalRiskMatrix", icon: "▸" },
  { href: "/decision-registry", key: "decisionRegistry", icon: "▸" },
  { href: "/clinical-costing", key: "clinicalCosting", icon: "▸" },
  { href: "/disease-registries", key: "diseaseRegistries", icon: "▸" },
  { href: "/protocol-quality", key: "protocolQuality", icon: "▸" },
  { href: "/data-quality", key: "dataQuality", icon: "▸" },
  { href: "/facilities-infra", key: "facilitiesInfra", icon: "▸" },
  { href: "/software-lifecycle", key: "softwareLifecycle", icon: "▸" },
  { href: "/clinical-forms", key: "clinicalForms", icon: "▸" },
  { href: "/api-management", key: "apiManagement", icon: "▸" },
  { href: "/mobile-devices", key: "mobileDevices", icon: "▸" },
  { href: "/nfr", key: "nfr", icon: "▸" },
  { href: "/traceability", key: "traceability", icon: "▸" },
  { href: "/facility-establishment", key: "facilityEstablishment", icon: "▸" },
  { href: "/medical-tourism", key: "medicalTourism", icon: "▸" },
  { href: "/service-catalog", key: "serviceCatalog", icon: "▸" },
  { href: "/observability", key: "observability", icon: "▸" },
  { href: "/org-learning", key: "orgLearning", icon: "▸" },
  { href: "/module-specs", key: "moduleSpecs", icon: "▸" },
  { href: "/governing-rules", key: "governingRules", icon: "▸" },
  { href: "/emergency-triage", key: "emergencyTriage", icon: "▸" },
  { href: "/patient-safety", key: "patientSafety", icon: "▸" },
  { href: "/radiology-deep", key: "radiologyDeep", icon: "▸" },
  { href: "/child-health", key: "childHealth", icon: "▸" },
  { href: "/amr-deep", key: "amrDeep", icon: "▸" },
  { href: "/clinical-quality", key: "clinicalQuality", icon: "▸" },
  { href: "/outpatient-deep", key: "outpatientDeep", icon: "▸" },
  { href: "/admissions-deep", key: "admissionsDeep", icon: "▸" },
  { href: "/chronic-deep", key: "chronicDeep", icon: "▸" },
  { href: "/referrals-deep", key: "referralsDeep", icon: "▸" },
  { href: "/telemedicine", key: "telemedicine", icon: "▸" },
  { href: "/connected-devices", key: "connectedDevices", icon: "▸" },
  { href: "/insurance-deep", key: "insuranceDeep", icon: "▸" },
  { href: "/hr-deep", key: "hrDeep", icon: "▸" },
  { href: "/critical-care", key: "criticalCare", icon: "▸" },
  { href: "/death-data", key: "deathData", icon: "▸" },
  { href: "/protocol-versions", key: "protocolVersions", icon: "▸" },
  { href: "/terminology-deep", key: "terminologyDeep", icon: "▸" },
  { href: "/iam-deep", key: "iamDeep", icon: "▸" },
  { href: "/audit-compliance", key: "auditCompliance", icon: "▸" },
  { href: "/research-governance", key: "researchGovernance", icon: "▸" },
  { href: "/supply-chain-deep", key: "supplyChainDeep", icon: "▸" },
  { href: "/multilingual", key: "multilingual", icon: "▸" },
  { href: "/transparency-deep", key: "transparencyDeep", icon: "▸" },
  { href: "/capability-access", key: "capabilityAccess", icon: "▸" },
  { href: "/cycle-verification", key: "cycleVerification", icon: "▸" },
  { href: "/ref-data-quality", key: "refDataQuality", icon: "▸" },
  { href: "/open-data", key: "openData", icon: "▸" },
  { href: "/vendor-monitoring", key: "vendorMonitoring", icon: "▸" },
  { href: "/facility-readiness", key: "facilityReadiness", icon: "▸" },
  { href: "/journeys", key: "journeys", icon: "▸" },
  { href: "/analytics", key: "analytics", icon: "▸" },
  { href: "/offline", key: "offline", icon: "▸" },

];

export default function Sidebar() {
  const pathname = usePathname();
  const router = useRouter();
  const { lang, setLang, t } = useLang();

  if (pathname === "/login") return null;

  return (
    <aside className="sidebar">
      <div className="brand">
        <div className="brand-mark">🇪🇬</div>
        <div className="brand-name">{t("appName")}</div>
        <div className="brand-sub">Catalog v6 · {lang === "ar" ? "إعادة بناء من الصفر" : "Rebuilt from scratch"}</div>
      </div>
      <nav className="nav">
        {NAV.map((item) => (
          <Link
            key={item.href}
            href={item.href}
            className={`nav-item ${pathname === item.href ? "nav-item-active" : ""}`}
          >
            <span aria-hidden>{item.icon}</span> {t(item.key)}
          </Link>
        ))}
      </nav>
      <div className="sidebar-foot">
        <button
          type="button"
          className="btn btn-ghost btn-block"
          onClick={() => setLang(lang === "ar" ? "en" : "ar")}
        >
          {lang === "ar" ? "English" : "العربية"}
        </button>
        <button
          type="button"
          className="btn btn-ghost btn-block"
          onClick={() => {
            setToken(null);
            router.push("/login");
          }}
        >
          {t("logout")}
        </button>
      </div>
    </aside>
  );
}
