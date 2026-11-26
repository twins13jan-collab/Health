"use client";

import { useEffect, useState, type FormEvent } from "react";
import DataTable from "@/components/DataTable";
import { PageHead } from "@/components/ui";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";
import type { Gender, Patient } from "@/lib/types";

export default function PatientsPage() {
  const { lang, t } = useLang();
  const ar = lang === "ar";
  const [rows, setRows] = useState<Patient[]>([]);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [q, setQ] = useState("");
  const [msg, setMsg] = useState<{ ok: boolean; text: string } | null>(null);
  const [form, setForm] = useState({ nationalId: "", fullNameAr: "", dateOfBirth: "", gender: "male" as Gender, governorate: "", phone: "", bloodType: "" });

  const load = async (query = "") => {
    try {
      const r = await api.patients(query);
      setRows(r.data);
      setLoadError(null);
    } catch (e) {
      setLoadError(e instanceof Error ? e.message : String(e));
    }
  };
  useEffect(() => { void load(); }, []);

  const onRegister = async (e: FormEvent) => {
    e.preventDefault();
    setMsg(null);
    if (!/^\d{14}$/.test(form.nationalId)) {
      setMsg({ ok: false, text: ar ? "الرقم القومي يجب أن يكون 14 رقمًا" : "National ID must be 14 digits" });
      return;
    }
    try {
      await api.registerPatient({ ...form, chronicConditions: [] });
      setMsg({ ok: true, text: ar ? "تم تسجيل المريض في السجل الموحد" : "Patient registered in the MPI" });
      setForm({ nationalId: "", fullNameAr: "", dateOfBirth: "", gender: "male", governorate: "", phone: "", bloodType: "" });
      void load();
    } catch (e) {
      setMsg({ ok: false, text: e instanceof Error ? e.message : String(e) });
    }
  };

  return (
    <>
      <PageHead
        eyebrow={ar ? "السجل الموحد MPI" : "Master Patient Index"}
        title={t("patients")}
        sub={ar ? "هوية موثوقة بالرقم القومي — منع الازدواج، ودمج/فصل موثّق فقط" : "Trusted identity by national ID — no duplicates, documented merge/split only"}
      />
      {loadError ? <div className="alert-error">{loadError}</div> : null}

      <section className="card">
        <h2>{t("search")}</h2>
        <form
          className="toolbar"
          onSubmit={(e) => { e.preventDefault(); void load(q); }}
        >
          <div className="field grow-field">
            <label htmlFor="pq">{ar ? "الاسم أو الرقم القومي" : "Name or national ID"}</label>
            <input id="pq" value={q} onChange={(e) => setQ(e.target.value)} />
          </div>
          <button className="btn" type="submit">{t("search")}</button>
        </form>
        <DataTable<Patient>
          rows={rows}
          columns={[
            { key: "name", header: ar ? "الاسم" : "Name", render: (r) => r.fullNameAr },
            { key: "nid", header: ar ? "الرقم القومي" : "National ID", render: (r) => r.nationalId },
            { key: "dob", header: ar ? "تاريخ الميلاد" : "Date of birth", render: (r) => r.dateOfBirth },
            { key: "gov", header: ar ? "المحافظة" : "Governorate", render: (r) => r.governorate ?? "—" },
            { key: "blood", header: ar ? "فصيلة الدم" : "Blood type", render: (r) => r.bloodType ?? "—" },
            { key: "chronic", header: ar ? "أمراض مزمنة" : "Chronic conditions", render: (r) => (r.chronicConditions ?? []).join("، ") || "—" },
          ]}
        />
      </section>

      <section className="card">
        <h2>{ar ? "تسجيل مريض جديد" : "Register a new patient"}</h2>
        <form onSubmit={onRegister}>
          <div className="form-grid">
            <div className="field">
              <label htmlFor="nid">{ar ? "الرقم القومي *" : "National ID *"}</label>
              <input id="nid" required value={form.nationalId} onChange={(e) => setForm({ ...form, nationalId: e.target.value })} />
            </div>
            <div className="field">
              <label htmlFor="fname">{ar ? "الاسم الكامل (عربي) *" : "Full name (Arabic) *"}</label>
              <input id="fname" required value={form.fullNameAr} onChange={(e) => setForm({ ...form, fullNameAr: e.target.value })} />
            </div>
            <div className="field">
              <label htmlFor="dob">{ar ? "تاريخ الميلاد *" : "Date of birth *"}</label>
              <input id="dob" type="date" required value={form.dateOfBirth} onChange={(e) => setForm({ ...form, dateOfBirth: e.target.value })} />
            </div>
            <div className="field">
              <label htmlFor="gender">{ar ? "النوع" : "Gender"}</label>
              <select id="gender" value={form.gender} onChange={(e) => setForm({ ...form, gender: e.target.value as Gender })}>
                <option value="male">{ar ? "ذكر" : "Male"}</option>
                <option value="female">{ar ? "أنثى" : "Female"}</option>
              </select>
            </div>
            <div className="field">
              <label htmlFor="gov">{ar ? "المحافظة" : "Governorate"}</label>
              <input id="gov" value={form.governorate} onChange={(e) => setForm({ ...form, governorate: e.target.value })} />
            </div>
            <div className="field">
              <label htmlFor="phone">{ar ? "الهاتف" : "Phone"}</label>
              <input id="phone" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
            </div>
            <div className="field">
              <label htmlFor="blood">{ar ? "فصيلة الدم" : "Blood type"}</label>
              <input id="blood" value={form.bloodType} onChange={(e) => setForm({ ...form, bloodType: e.target.value })} />
            </div>
          </div>
          <div className="form-actions">
            <button className="btn" type="submit">{t("register")}</button>
          </div>
          {msg ? <div className={`form-msg ${msg.ok ? "ok" : "err"}`}>{msg.text}</div> : null}
        </form>
      </section>
    </>
  );
}
