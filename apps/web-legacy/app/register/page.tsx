"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { api, getToken } from "@/lib/api";
import { useLang } from "@/lib/i18n";
import { PublicHeader, PublicFooter, PublicSideMenu } from "@/components/PublicChrome";

/** Self-registration: citizens/patients get an account linked to
 *  their unified patient record and are signed in immediately;
 *  facilities register into a pending state that the central
 *  administration approves before activation. */
export default function RegisterPage() {
  const { lang } = useLang();
  const ar = lang === "ar";
  const router = useRouter();
  const [tab, setTab] = useState<"patient" | "facility">("patient");
  const [patient, setPatient] = useState({ fullName: "", nationalId: "", email: "", password: "", phone: "", birthDate: "", gender: "male", governorate: "" });
  const [facility, setFacility] = useState({ facilityNameAr: "", type: "hospital", level: "secondary", governorate: "", directorate: "", city: "", ownership: "private", bedCapacity: "", adminFullName: "", adminEmail: "", adminPassword: "" });
  const [error, setError] = useState<string | null>(null);
  const [done, setDone] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => { if (getToken()) router.replace("/my"); }, [router]);

  async function submitPatient(e: React.FormEvent) {
    e.preventDefault(); setBusy(true); setError(null);
    try {
      await api.registerAccount({
        fullName: patient.fullName, nationalId: patient.nationalId, email: patient.email,
        password: patient.password, phone: patient.phone || undefined,
        birthDate: patient.birthDate || undefined, gender: patient.gender,
        governorate: patient.governorate || undefined,
      });
      router.push("/my");
    } catch (err) { setError(err instanceof Error ? err.message : String(err)); } finally { setBusy(false); }
  }

  async function submitFacility(e: React.FormEvent) {
    e.preventDefault(); setBusy(true); setError(null);
    try {
      const r = await api.registerFacility({
        ...facility,
        bedCapacity: facility.bedCapacity ? Number(facility.bedCapacity) : undefined,
      });
      setDone(String(r.message ?? (ar ? "سُجلت المنشأة — بانتظار اعتماد الإدارة" : "Facility registered — awaiting admin approval")));
    } catch (err) { setError(err instanceof Error ? err.message : String(err)); } finally { setBusy(false); }
  }

  return (
    <>
      <PublicHeader />
      <div className="page-grid">
        <PublicSideMenu />
        <div>
        <h1 className="section-title" style={{ textAlign: "center" }}>{ar ? "إنشاء حساب" : "Create an account"}</h1>
        <p className="section-sub" style={{ textAlign: "center" }}>
          {ar ? "حساب مريض يربطك بسجلك الصحي الموحد فورًا، وتسجيل منشأة يُعتمد من الإدارة قبل التفعيل." : "A patient account links you to your unified health record at once; a facility registration is approved by administration before activation."}
        </p>
        <div className="card">
          <div style={{ display: "flex", gap: 8, marginBottom: 16 }}>
            <button type="button" className={`btn ${tab === "patient" ? "" : "btn-secondary"}`} onClick={() => setTab("patient")}>{ar ? "👤 مريض / مواطن" : "👤 Patient / citizen"}</button>
            <button type="button" className={`btn ${tab === "facility" ? "" : "btn-secondary"}`} onClick={() => setTab("facility")}>{ar ? "🏥 منشأة صحية" : "🏥 Health facility"}</button>
          </div>
          {error && <div className="error-banner">{error}</div>}
          {done ? (
            <div>
              <p>✅ {done}</p>
              <p className="muted">{ar ? "سيصل مدير المنشأة إشعار بالتفعيل بعد الاعتماد، ويمكنه بعدها تسجيل الدخول وإضافة طاقمه من إدارة الحسابات." : "The facility manager can sign in once approved, then add staff from account management."}</p>
              <Link className="btn" href="/login">{ar ? "إلى تسجيل الدخول" : "To sign-in"}</Link>
            </div>
          ) : tab === "patient" ? (
            <form onSubmit={submitPatient}>
              <div className="field"><label>{ar ? "الاسم الكامل" : "Full name"}</label><input required value={patient.fullName} onChange={(e) => setPatient({ ...patient, fullName: e.target.value })} /></div>
              <div className="field"><label>{ar ? "الرقم القومي (14 رقمًا)" : "National ID (14 digits)"}</label><input required dir="ltr" maxLength={14} value={patient.nationalId} onChange={(e) => setPatient({ ...patient, nationalId: e.target.value.replace(/\D/g, "") })} /></div>
              <div className="field"><label>{ar ? "البريد الإلكتروني" : "Email"}</label><input required type="email" dir="ltr" value={patient.email} onChange={(e) => setPatient({ ...patient, email: e.target.value })} /></div>
              <div className="field"><label>{ar ? "كلمة المرور (10 أحرف على الأقل، حروف وأرقام)" : "Password (min 10 chars, letters + digits)"}</label><input required type="password" dir="ltr" value={patient.password} onChange={(e) => setPatient({ ...patient, password: e.target.value })} /></div>
              <div className="field"><label>{ar ? "رقم الهاتف" : "Phone"}</label><input dir="ltr" value={patient.phone} onChange={(e) => setPatient({ ...patient, phone: e.target.value })} /></div>
              <div className="field"><label>{ar ? "تاريخ الميلاد" : "Birth date"}</label><input type="date" value={patient.birthDate} onChange={(e) => setPatient({ ...patient, birthDate: e.target.value })} /></div>
              <div className="field"><label>{ar ? "المحافظة" : "Governorate"}</label><input value={patient.governorate} onChange={(e) => setPatient({ ...patient, governorate: e.target.value })} /></div>
              <button type="submit" className="btn" disabled={busy}>{ar ? "إنشاء الحساب والدخول" : "Create account & sign in"}</button>
            </form>
          ) : (
            <form onSubmit={submitFacility}>
              <div className="field"><label>{ar ? "اسم المنشأة" : "Facility name"}</label><input required value={facility.facilityNameAr} onChange={(e) => setFacility({ ...facility, facilityNameAr: e.target.value })} /></div>
              <div className="field"><label>{ar ? "النوع" : "Type"}</label>
                <select value={facility.type} onChange={(e) => setFacility({ ...facility, type: e.target.value })}>
                  <option value="hospital">{ar ? "مستشفى" : "Hospital"}</option>
                  <option value="health_unit">{ar ? "وحدة صحية" : "Health unit"}</option>
                  <option value="family_center">{ar ? "مركز صحة أسرة" : "Family centre"}</option>
                  <option value="lab">{ar ? "معمل تحاليل" : "Laboratory"}</option>
                  <option value="pharmacy">{ar ? "صيدلية" : "Pharmacy"}</option>
                  <option value="blood_bank">{ar ? "بنك دم" : "Blood bank"}</option>
                  <option value="dialysis_center">{ar ? "مركز غسيل كلوي" : "Dialysis centre"}</option>
                  <option value="oncology_center">{ar ? "مركز أورام" : "Oncology centre"}</option>
                </select></div>
              <div className="field"><label>{ar ? "المحافظة" : "Governorate"}</label><input required value={facility.governorate} onChange={(e) => setFacility({ ...facility, governorate: e.target.value })} /></div>
              <div className="field"><label>{ar ? "المدينة" : "City"}</label><input value={facility.city} onChange={(e) => setFacility({ ...facility, city: e.target.value })} /></div>
              <div className="field"><label>{ar ? "السعة السريرية" : "Bed capacity"}</label><input type="number" min={0} value={facility.bedCapacity} onChange={(e) => setFacility({ ...facility, bedCapacity: e.target.value })} /></div>
              <div className="field"><label>{ar ? "اسم مدير المنشأة" : "Facility manager name"}</label><input required value={facility.adminFullName} onChange={(e) => setFacility({ ...facility, adminFullName: e.target.value })} /></div>
              <div className="field"><label>{ar ? "بريد المدير" : "Manager email"}</label><input required type="email" dir="ltr" value={facility.adminEmail} onChange={(e) => setFacility({ ...facility, adminEmail: e.target.value })} /></div>
              <div className="field"><label>{ar ? "كلمة مرور المدير" : "Manager password"}</label><input required type="password" dir="ltr" value={facility.adminPassword} onChange={(e) => setFacility({ ...facility, adminPassword: e.target.value })} /></div>
              <button type="submit" className="btn" disabled={busy}>{ar ? "تسجيل المنشأة للاعتماد" : "Register for approval"}</button>
            </form>
          )}
          <p className="muted" style={{ marginTop: 14 }}>
            {ar ? "لديك حساب؟ " : "Already have an account? "}<Link href="/login">{ar ? "تسجيل الدخول" : "Sign in"}</Link>
          </p>
        </div>
        </div>
      </div>
      <PublicFooter />
    </>
  );
}
