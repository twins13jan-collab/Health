"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { api, getToken } from "@/lib/api";
import { useLang } from "@/lib/i18n";
import { facilityTypeLabel } from "@/lib/types";
import { SectionHead, Tabs } from "@/components/ui";
import FacilityCascade, { type FacilityRow } from "@/components/FacilityCascade";

const STAFF_ROLE_OPTIONS: { v: string; ar: string; en: string }[] = [
  { v: "facility_manager", ar: "مدير منشأة", en: "Facility manager" },
  { v: "physician", ar: "طبيب", en: "Physician" },
  { v: "nurse", ar: "تمريض", en: "Nurse" },
  { v: "receptionist", ar: "استقبال", en: "Receptionist" },
  { v: "pharmacist", ar: "صيدلي", en: "Pharmacist" },
  { v: "finance_manager", ar: "مسؤول مالي", en: "Finance manager" },
  { v: "store_keeper", ar: "أمين مخزن", en: "Store keeper" },
  { v: "biomedical_engineer", ar: "مهندس أجهزة طبية", en: "Biomedical engineer" },
  { v: "oncologist", ar: "طبيب أورام", en: "Oncologist" },
  { v: "ipc_officer", ar: "مسؤول مكافحة عدوى", en: "IPC officer" },
  { v: "safety_officer", ar: "مسؤول سلامة", en: "Safety officer" },
  { v: "social_worker", ar: "أخصائي اجتماعي", en: "Social worker" },
  { v: "health_educator", ar: "مثقف صحي", en: "Health educator" },
  { v: "school_counselor", ar: "أخصائي مدرسي", en: "School counselor" },
  { v: "forensic_physician", ar: "طبيب شرعي", en: "Forensic physician" },
];

/** Self-registration: citizens/patients get an account linked to
 *  their unified patient record and are signed in immediately;
 *  facilities register into a pending state that the central
 *  administration approves before activation. Two tabs inside the
 *  public inner layout. */
const FACILITY_TYPE_OPTIONS = ["hospital", "general_hospital", "university_hospital", "emergency_hospital", "specialized_center", "center", "radiology_center", "clinic", "health_center", "health_unit", "family_center", "lab", "laboratory", "pharmacy", "blood_bank", "dialysis_center", "oncology_center", "ambulance_station", "mobile_clinic"];

export default function RegisterPage() {
  const { lang } = useLang();
  const ar = lang === "ar";
  const router = useRouter();
  const [tab, setTab] = useState<"patient" | "facility" | "staff">("patient");
  const [staff, setStaff] = useState({ fullName: "", email: "", password: "", role: "nurse" });
  const [staffFacility, setStaffFacility] = useState<FacilityRow | null>(null);
  const [patient, setPatient] = useState({ fullName: "", nationalId: "", email: "", password: "", phone: "", birthDate: "", gender: "male", governorate: "" });
  const [facility, setFacility] = useState({ facilityNameAr: "", type: "hospital", level: "secondary", governorate: "", directorate: "", city: "", ownership: "private", bedCapacity: "", adminFullName: "", adminEmail: "", adminPassword: "", licenseNo: "", licenseAuthority: "", licenseExpiry: "" });
  const [error, setError] = useState<string | null>(null);
  const [done, setDone] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => { if (getToken()) router.replace("/my"); }, [router]);

  const [govList, setGovList] = useState<{ code: string; nameAr: string; nameEn: string; directorateAr?: string }[]>([]);
  useEffect(() => {
    api.getJson<{ governorates: { code: string; nameAr: string; nameEn: string; directorateAr?: string }[] }>("/api/public/hierarchy")
      .then((r) => setGovList(r.governorates ?? []))
      .catch(() => setGovList([]));
  }, []);
  const pickGovernorate = (nameAr: string) => {
    const g = govList.find((x) => x.nameAr === nameAr);
    setFacility((f) => ({ ...f, governorate: nameAr, directorate: g?.directorateAr ?? f.directorate }));
  };

  async function submitPatient(e: React.FormEvent) {
    e.preventDefault(); setBusy(true); setError(null);
    try {
      await api.registerAccount({
        fullName: patient.fullName, nationalId: patient.nationalId, email: patient.email,
        password: patient.password,
        birthDate: patient.birthDate || undefined, gender: patient.gender,
        governorate: patient.governorate || undefined,
      });
      router.push("/my");
    } catch (err) { setError(err instanceof Error ? err.message : String(err)); } finally { setBusy(false); }
  }

  async function submitStaff(e: React.FormEvent) {
    e.preventDefault(); setBusy(true); setError(null);
    try {
      if (!staffFacility) { setError(ar ? "اختر المنشأة التابع لها الحساب من التسلسل" : "Choose the facility from the hierarchy picker"); setBusy(false); return; }
      const staffPw = staff.password;
      const r = await api.postJson<{ message?: string }>("/api/auth/register-staff", {
        fullName: staff.fullName, email: staff.email, password: staffPw, role: staff.role, facilityId: staffFacility.id,
      });
      setDone(String(r.message ?? (ar ? "تم إنشاء الحساب — بانتظار اعتماد مدير المنشأة" : "Account created — awaiting facility manager approval")));
    } catch (err) { setError(err instanceof Error ? err.message : String(err)); } finally { setBusy(false); }
  }

  async function submitFacility(e: React.FormEvent) {
    e.preventDefault(); setBusy(true); setError(null);
    try {
      const r = await api.registerFacility({
        ...facility,
        bedCapacity: facility.bedCapacity ? Number(facility.bedCapacity) : undefined,
        licenseNo: facility.licenseNo || undefined,
        licenseAuthority: facility.licenseAuthority || undefined,
        licenseExpiry: facility.licenseExpiry || undefined,
      });
      setDone(String(r.message ?? (ar ? "سُجلت المنشأة — بانتظار اعتماد الإدارة" : "Facility registered — awaiting admin approval")));
    } catch (err) { setError(err instanceof Error ? err.message : String(err)); } finally { setBusy(false); }
  }

  return (
    <>
      <SectionHead
        eyebrow={ar ? "حساب جديد" : "New account"}
        title={ar ? "إنشاء حساب" : "Create an account"}
        sub={ar ? "حساب مريض يربطك بسجلك الصحي الموحد فورًا، وتسجيل منشأة يُعتمد من الإدارة قبل التفعيل، وحساب طاقم يُسجَّل بالتسلسل الهرمي (الدور ← الجهة ← المحافظة ← المديرية ← المنشأة) ويُعتمد من مدير منشأته." : "A patient account links you to your unified health record at once; a facility registration is approved by administration before activation; a staff account is registered through the hierarchy (role → entity → governorate → directorate → facility) and approved by its facility manager."}
      />
      <div className="card">
        <Tabs
          value={tab}
          onChange={(v) => setTab(v as "patient" | "facility" | "staff")}
          tabs={[
            { value: "patient", label: ar ? "👤 مريض / مواطن" : "👤 Patient / citizen" },
            { value: "facility", label: ar ? "🏥 منشأة صحية" : "🏥 Health facility" },
            { value: "staff", label: ar ? "👩‍⚕️ طاقم / مسؤول" : "👩‍⚕️ Staff / officer" },
          ]}
        />
        {error && <div className="alert-error">{error}</div>}
        {done ? (
          <div>
            <div className="notice-ok">✅ {done}</div>
            <p className="muted">{ar ? "سيصل مدير المنشأة إشعار بالتفعيل بعد الاعتماد، ويمكنه بعدها تسجيل الدخول وإضافة طاقمه من إدارة الحسابات." : "The facility manager can sign in once approved, then add staff from account management."}</p>
            <Link className="btn mt" href="/login">{ar ? "إلى تسجيل الدخول" : "To sign-in"}</Link>
          </div>
        ) : tab === "patient" ? (
          <form onSubmit={submitPatient}>
            <div className="form-grid">
              <div className="field"><label>{ar ? "الاسم الكامل" : "Full name"}</label><input required value={patient.fullName} onChange={(e) => setPatient({ ...patient, fullName: e.target.value })} /></div>
              <div className="field"><label>{ar ? "الرقم القومي (14 رقمًا)" : "National ID (14 digits)"}</label><input required dir="ltr" maxLength={14} value={patient.nationalId} onChange={(e) => setPatient({ ...patient, nationalId: e.target.value.replace(/\D/g, "") })} /></div>
              <div className="field"><label>{ar ? "البريد الإلكتروني" : "Email"}</label><input required type="email" dir="ltr" value={patient.email} onChange={(e) => setPatient({ ...patient, email: e.target.value })} /></div>
              <div className="field"><label>{ar ? "كلمة المرور (10 أحرف على الأقل، حروف وأرقام)" : "Password (min 10 chars, letters + digits)"}</label><input required type="password" dir="ltr" value={patient.password} onChange={(e) => setPatient({ ...patient, password: e.target.value })} /></div>
              <div className="field"><label>{ar ? "رقم الهاتف" : "Phone"}</label><input dir="ltr" value={patient.phone} onChange={(e) => setPatient({ ...patient, phone: e.target.value })} /></div>
              <div className="field"><label>{ar ? "تاريخ الميلاد" : "Birth date"}</label><input type="date" value={patient.birthDate} onChange={(e) => setPatient({ ...patient, birthDate: e.target.value })} /></div>
              <div className="field"><label>{ar ? "المحافظة" : "Governorate"}</label>
                <select value={patient.governorate} onChange={(e) => setPatient({ ...patient, governorate: e.target.value })}>
                  <option value="">{ar ? "اختر المحافظة…" : "Choose the governorate…"}</option>
                  {govList.map((g) => <option key={g.code} value={g.nameAr}>{ar ? g.nameAr : g.nameEn}</option>)}
                </select></div>
              <div className="field"><label>{ar ? "النوع" : "Gender"}</label>
                <select value={patient.gender} onChange={(e) => setPatient({ ...patient, gender: e.target.value })}>
                  <option value="male">{ar ? "ذكر" : "Male"}</option>
                  <option value="female">{ar ? "أنثى" : "Female"}</option>
                </select></div>
            </div>
            <div className="form-actions">
              <button type="submit" className="btn" disabled={busy}>{ar ? "إنشاء الحساب والدخول" : "Create account & sign in"}</button>
            </div>
          </form>
        ) : tab === "staff" ? (
          <form onSubmit={submitStaff}>
            <p className="muted">{ar
              ? "حساب طاقم يُنشأ بالتسلسل الكامل: اختر دورك، ثم الجهة التابع لها، فالمحافظة، فمديرية الصحة، فنوع المنشأة، فاسم منشأتك. الحساب يُفعَّل بعد اعتماد مدير المنشأة."
              : "A staff account is created through the full hierarchy: your role, then the entity, governorate, health directorate, facility type and your facility's name. The facility manager approves it before activation."}</p>
            <div className="form-grid">
              <div className="field"><label>{ar ? "الاسم الكامل" : "Full name"}</label><input required value={staff.fullName} onChange={(e) => setStaff({ ...staff, fullName: e.target.value })} /></div>
              <div className="field"><label>{ar ? "الدور" : "Role"}</label>
                <select value={staff.role} onChange={(e) => setStaff({ ...staff, role: e.target.value })}>
                  {STAFF_ROLE_OPTIONS.map((r) => <option key={r.v} value={r.v}>{ar ? r.ar : r.en}</option>)}
                </select></div>
              <div className="field"><label>{ar ? "البريد الإلكتروني" : "Email"}</label><input required type="email" dir="ltr" value={staff.email} onChange={(e) => setStaff({ ...staff, email: e.target.value })} /></div>
              <div className="field"><label>{ar ? "كلمة المرور" : "Password"}</label><input required type="password" dir="ltr" value={staff.password} onChange={(e) => setStaff({ ...staff, password: e.target.value })} /></div>
            </div>
            <FacilityCascade onPick={setStaffFacility} />
            {staffFacility ? <p className="muted">{ar ? "المنشأة المختارة:" : "Selected facility:"} <strong>{staffFacility.nameAr}</strong></p> : null}
            <div className="form-actions">
              <button type="submit" className="btn" disabled={busy}>{ar ? "تسجيل الحساب للاعتماد" : "Register for approval"}</button>
            </div>
          </form>
        ) : (
          <form onSubmit={submitFacility}>
            <div className="form-grid">
              <div className="field"><label>{ar ? "اسم المنشأة" : "Facility name"}</label><input required value={facility.facilityNameAr} onChange={(e) => setFacility({ ...facility, facilityNameAr: e.target.value })} /></div>
              <div className="field"><label>{ar ? "النوع" : "Type"}</label>
                <select value={facility.type} onChange={(e) => setFacility({ ...facility, type: e.target.value })}>
                  {FACILITY_TYPE_OPTIONS.map((ty) => (
                    <option key={ty} value={ty}>{facilityTypeLabel(ty, lang)}</option>
                  ))}
                </select></div>
              <div className="field"><label>{ar ? "المحافظة" : "Governorate"}</label>
                <select required value={facility.governorate} onChange={(e) => pickGovernorate(e.target.value)}>
                  <option value="">{ar ? "اختر المحافظة…" : "Choose the governorate…"}</option>
                  {govList.map((g) => <option key={g.code} value={g.nameAr}>{ar ? g.nameAr : g.nameEn}</option>)}
                </select></div>
              <div className="field"><label>{ar ? "مديرية الصحة" : "Health directorate"}</label><input disabled value={facility.directorate} placeholder={ar ? "تُحدد تلقائيًا من المحافظة" : "Set automatically from the governorate"} /></div>
              <div className="field"><label>{ar ? "المدينة" : "City"}</label><input value={facility.city} onChange={(e) => setFacility({ ...facility, city: e.target.value })} /></div>
              <div className="field"><label>{ar ? "السعة السريرية" : "Bed capacity"}</label><input type="number" min={0} value={facility.bedCapacity} onChange={(e) => setFacility({ ...facility, bedCapacity: e.target.value })} /></div>
              <div className="field"><label>{ar ? "رقم ترخيص المنشأة" : "Facility licence no."}</label><input value={facility.licenseNo} onChange={(e) => setFacility({ ...facility, licenseNo: e.target.value })} /></div>
              <div className="field"><label>{ar ? "جهة الترخيص" : "Licensing authority"}</label><input value={facility.licenseAuthority} onChange={(e) => setFacility({ ...facility, licenseAuthority: e.target.value })} /></div>
              <div className="field"><label>{ar ? "انتهاء الترخيص" : "Licence expiry"}</label><input type="date" value={facility.licenseExpiry} onChange={(e) => setFacility({ ...facility, licenseExpiry: e.target.value })} /></div>
              <div className="field"><label>{ar ? "اسم مدير المنشأة" : "Facility manager name"}</label><input required value={facility.adminFullName} onChange={(e) => setFacility({ ...facility, adminFullName: e.target.value })} /></div>
              <div className="field"><label>{ar ? "بريد المدير" : "Manager email"}</label><input required type="email" dir="ltr" value={facility.adminEmail} onChange={(e) => setFacility({ ...facility, adminEmail: e.target.value })} /></div>
              <div className="field"><label>{ar ? "كلمة مرور المدير" : "Manager password"}</label><input required type="password" dir="ltr" value={facility.adminPassword} onChange={(e) => setFacility({ ...facility, adminPassword: e.target.value })} /></div>
            </div>
            <div className="form-actions">
              <button type="submit" className="btn" disabled={busy}>{ar ? "تسجيل المنشأة للاعتماد" : "Register for approval"}</button>
            </div>
          </form>
        )}
        <p className="muted mt">
          {ar ? "لديك حساب؟ " : "Already have an account? "}<Link href="/login">{ar ? "تسجيل الدخول" : "Sign in"}</Link>
        </p>
      </div>
    </>
  );
}
