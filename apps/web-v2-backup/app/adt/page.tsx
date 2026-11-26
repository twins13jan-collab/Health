"use client";

import { useCallback, useEffect, useState } from "react";
import StatusBadge from "@/components/StatusBadge";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";
import type { Patient } from "@/lib/types";

interface WardRow {
  id: string; nameAr: string; nameEn?: string; wardType: string;
  totalBeds: number; occupied: number; free: number; occupancyPct: number | null; active: boolean;
  beds?: { bedLabel: string; admissionId: string; patientName?: string; admittedAt?: string }[];
}
interface AdmissionRow {
  id: string; patientId: string; patientName?: string; nationalId?: string;
  wardId: string; wardName?: string; bedLabel: string; attendingName?: string;
  admissionReason?: string; source: string; status: string;
  admittedAt?: string; losDays: number; dischargeDisposition?: string;
}

const WARD_TYPES = ["general", "icu", "nicu", "er", "observation", "maternity", "or_recovery", "isolation"];
const SOURCES = ["er", "clinic", "referral", "direct", "transfer_in"];
const DISPOSITIONS = ["home", "transfer_out", "deceased", "ama", "rehab"];

/** ADT operations: live bed board, facility census, admit / transfer /
 *  discharge — the hospital's daily bed management, system of record. */
export default function AdtPage() {
  const { lang } = useLang();
  const ar = lang === "ar";
  const [wards, setWards] = useState<WardRow[]>([]);
  const [census, setCensus] = useState<AdmissionRow[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [toast, setToast] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [drawer, setDrawer] = useState<null | { kind: "admit" } | { kind: "transfer" | "discharge"; row: AdmissionRow }>(null);
  const [patientQuery, setPatientQuery] = useState("");
  const [patientResults, setPatientResults] = useState<Patient[]>([]);
  const [admitForm, setAdmitForm] = useState({ patientId: "", patientName: "", wardId: "", bedLabel: "", attendingName: "", admissionReason: "", source: "clinic" });
  const [transferForm, setTransferForm] = useState({ toWardId: "", toBedLabel: "", reason: "" });
  const [disposition, setDisposition] = useState("home");
  const [wardForm, setWardForm] = useState({ nameAr: "", nameEn: "", wardType: "general", totalBeds: "20" });

  const load = useCallback(() => {
    Promise.all([
      api.moduleList<WardRow>("/api/adt/board", "wards"),
      api.moduleList<AdmissionRow>("/api/adt/admissions?status=admitted", "admissions"),
      api.moduleList<WardRow>("/api/adt/wards", "wards"),
    ])
      .then(([board, adm, w]) => {
        setWards(board.data);
        setCensus(adm.data);
        setWardOptions(w.data);
        setError(null);
      })
      .catch((e) => setError(e instanceof Error ? e.message : String(e)));
  }, []);
  const [wardOptions, setWardOptions] = useState<WardRow[]>([]);
  useEffect(() => { load(); }, [load]);
  useEffect(() => {
    if (!toast) return;
    const id = setTimeout(() => setToast(null), 4000);
    return () => clearTimeout(id);
  }, [toast]);

  const totalBeds = wards.reduce((s, w) => s + w.totalBeds, 0);
  const freeBeds = wards.reduce((s, w) => s + w.free, 0);

  async function searchPatients(q: string) {
    setPatientQuery(q);
    if (q.trim().length < 2) { setPatientResults([]); return; }
    try {
      const r = await api.patients(q.trim());
      setPatientResults(r.data.slice(0, 8));
    } catch { setPatientResults([]); }
  }

  async function submitAdmit(ev: React.FormEvent) {
    ev.preventDefault();
    setBusy(true); setError(null);
    try {
      await api.postJson("/api/adt/admissions", {
        patientId: admitForm.patientId, wardId: admitForm.wardId, bedLabel: admitForm.bedLabel,
        attendingName: admitForm.attendingName || null, admissionReason: admitForm.admissionReason || null,
        source: admitForm.source,
      });
      setDrawer(null);
      setToast(ar ? "تم قبول المريض وتنويمه" : "Patient admitted");
      load();
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); } finally { setBusy(false); }
  }

  async function submitTransfer(ev: React.FormEvent) {
    ev.preventDefault();
    if (!drawer || drawer.kind !== "transfer") return;
    setBusy(true); setError(null);
    try {
      await api.postJson(`/api/adt/admissions/${drawer.row.id}/transfer`, transferForm);
      setDrawer(null);
      setToast(ar ? "تم التحويل الداخلي" : "Transferred");
      load();
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); } finally { setBusy(false); }
  }

  async function submitDischarge(ev: React.FormEvent) {
    ev.preventDefault();
    if (!drawer || drawer.kind !== "discharge") return;
    setBusy(true); setError(null);
    try {
      await api.postJson(`/api/adt/admissions/${drawer.row.id}/discharge`, { disposition });
      setDrawer(null);
      setToast(ar ? "تم خروج المريض" : "Discharged");
      load();
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); } finally { setBusy(false); }
  }

  async function createWard(ev: React.FormEvent) {
    ev.preventDefault();
    setBusy(true); setError(null);
    try {
      await api.postJson("/api/adt/wards", {
        nameAr: wardForm.nameAr, nameEn: wardForm.nameEn || null,
        wardType: wardForm.wardType, totalBeds: Number(wardForm.totalBeds) || 0,
      });
      setWardForm({ nameAr: "", nameEn: "", wardType: "general", totalBeds: "20" });
      setToast(ar ? "تم إنشاء الجناح" : "Ward created");
      load();
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); } finally { setBusy(false); }
  }

  async function deactivateWard(w: WardRow) {
    setBusy(true); setError(null);
    try {
      await api.postJson(`/api/adt/wards/${w.id}/deactivate`, {});
      setToast(ar ? "تم إيقاف الجناح" : "Ward deactivated");
      load();
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); } finally { setBusy(false); }
  }

  return (
    <>
      <div className="page-head">
        <h1>{ar ? "القبول والإقامة والتحويلات (ADT)" : "Admissions, Discharge & Transfer"}</h1>
        <p>{ar ? "لوحة الأسرّة الحية وتعداد المنشأة — قبول وتنويم وتحويل داخلي وخروج، بسجل تدقيق كامل" : "The live bed board and facility census — admit, transfer and discharge with a full audit trail"}</p>
      </div>
      {error ? <div className="error-banner">{error}</div> : null}

      <div className="kpi-grid">
        <div className="kpi-card"><div className="kpi-label">{ar ? "مرضى مقيمون الآن" : "Current census"}</div><div className="kpi-value">{census.length}</div></div>
        <div className="kpi-card kpi-ok"><div className="kpi-label">{ar ? "أسرّة شاغرة" : "Free beds"}</div><div className="kpi-value">{freeBeds}</div><div className="kpi-hint">{ar ? `من إجمالي ${totalBeds}` : `of ${totalBeds} total`}</div></div>
        <div className="kpi-card"><div className="kpi-label">{ar ? "أجنحة نشطة" : "Active wards"}</div><div className="kpi-value">{wards.length}</div></div>
        <div className="kpi-card"><div className="kpi-label">{ar ? "إشغال المنشأة" : "Facility occupancy"}</div><div className="kpi-value">{totalBeds ? `${Math.round((1000 * (totalBeds - freeBeds)) / totalBeds) / 10}%` : "—"}</div></div>
      </div>

      <div className="l-mb">
        <button type="button" className="btn" onClick={() => setDrawer({ kind: "admit" })}>＋ {ar ? "قبول وتنويم مريض" : "Admit a patient"}</button>
      </div>

      <section className="card">
        <h2>{ar ? "لوحة الأسرّة" : "Bed board"}</h2>
        {wards.length === 0 ? (
          <div className="empty">{ar ? "لا توجد أجنحة بعد — أنشئ أول جناح من قسم إدارة الأجنحة بالأسفل" : "No wards yet — create the first ward below"}</div>
        ) : (
          <div className="cards-grid">
            {wards.map((w) => (
              <div key={w.id} className="ward-card">
                <div className="l-row-between">
                  <strong>{w.nameAr}</strong>
                  <span className="badge badge-muted">{w.wardType}</span>
                </div>
                <div className={`progress ${w.occupancyPct !== null && w.occupancyPct > 85 ? "hot" : w.occupancyPct !== null && w.occupancyPct > 70 ? "warm" : ""} my-10`}>
                  <div style={{ width: `${w.occupancyPct ?? 0}%` }} />
                </div>
                <div className="text-md l-mb">
                  {w.occupied} / {w.totalBeds} {ar ? "مشغول" : "occupied"} · {w.free} {ar ? "شاغر" : "free"}
                </div>
                <div className="l-row">
                  {(w.beds ?? []).map((b) => (
                    <span key={b.admissionId} className="badge badge-danger" title={b.patientName ?? ""}>
                      🛏 {b.bedLabel} · {b.patientName ?? "—"}
                    </span>
                  ))}
                  {w.free > 0 ? <span className="badge badge-ok">{ar ? `${w.free} سرير شاغر` : `${w.free} free`}</span> : null}
                </div>
              </div>
            ))}
          </div>
        )}
      </section>

      <section className="card">
        <h2>{ar ? "تعداد المرضى المقيمين" : "Inpatient census"}</h2>
        {census.length === 0 ? (
          <div className="empty">{ar ? "لا يوجد مرضى مقيمون حاليًا" : "No inpatients right now"}</div>
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead><tr>
                <th>{ar ? "المريض" : "Patient"}</th><th>{ar ? "الجناح / السرير" : "Ward / Bed"}</th>
                <th>{ar ? "الطبيب المعالج" : "Attending"}</th><th>{ar ? "المصدر" : "Source"}</th>
                <th>{ar ? "منذ" : "Admitted"}</th><th>{ar ? "أيام الإقامة" : "LOS (days)"}</th><th>{ar ? "إجراءات" : "Actions"}</th>
              </tr></thead>
              <tbody>
                {census.map((row) => (
                  <tr key={row.id}>
                    <td><strong>{row.patientName ?? row.patientId}</strong>{row.nationalId ? <div className="text-sm muted" dir="ltr">{row.nationalId}</div> : null}</td>
                    <td>{row.wardName ?? "—"} · {row.bedLabel}</td>
                    <td>{row.attendingName ?? "—"}</td>
                    <td><StatusBadge status={row.source} /></td>
                    <td>{row.admittedAt ? row.admittedAt.slice(0, 10) : "—"}</td>
                    <td>{row.losDays}</td>
                    <td className="nowrap">
                      <button type="button" className="btn btn-secondary btn-small" disabled={busy} onClick={() => { setTransferForm({ toWardId: "", toBedLabel: "", reason: "" }); setDrawer({ kind: "transfer", row }); }}>{ar ? "تحويل" : "Transfer"}</button>
                      <button type="button" className="btn btn-secondary btn-small" disabled={busy} onClick={() => { setDisposition("home"); setDrawer({ kind: "discharge", row }); }}>{ar ? "خروج" : "Discharge"}</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className="card">
        <h2>{ar ? "إدارة الأجنحة" : "Ward administration"}</h2>
        <form onSubmit={createWard} className="form-grid l-mb">
          <div className="field"><label>{ar ? "اسم الجناح (عربي)" : "Ward name (Arabic)"}</label><input required value={wardForm.nameAr} onChange={(e) => setWardForm({ ...wardForm, nameAr: e.target.value })} /></div>
          <div className="field"><label>{ar ? "الاسم (إنجليزي)" : "Name (English)"}</label><input dir="ltr" value={wardForm.nameEn} onChange={(e) => setWardForm({ ...wardForm, nameEn: e.target.value })} /></div>
          <div className="field"><label>{ar ? "النوع" : "Type"}</label>
            <select value={wardForm.wardType} onChange={(e) => setWardForm({ ...wardForm, wardType: e.target.value })}>
              {WARD_TYPES.map((tp) => <option key={tp} value={tp}>{tp}</option>)}
            </select></div>
          <div className="field"><label>{ar ? "عدد الأسرّة" : "Beds"}</label><input type="number" min="0" required value={wardForm.totalBeds} onChange={(e) => setWardForm({ ...wardForm, totalBeds: e.target.value })} /></div>
          <div className="field end"><button className="btn" type="submit" disabled={busy}>{ar ? "إنشاء جناح" : "Create ward"}</button></div>
        </form>
        {wardOptions.length > 0 ? (
          <div className="table-wrap">
            <table className="data-table">
              <thead><tr><th>{ar ? "الجناح" : "Ward"}</th><th>{ar ? "النوع" : "Type"}</th><th>{ar ? "الأسرّة" : "Beds"}</th><th>{ar ? "مشغول" : "Occupied"}</th><th>{ar ? "الحالة" : "State"}</th><th></th></tr></thead>
              <tbody>
                {wardOptions.map((w) => (
                  <tr key={w.id}>
                    <td>{w.nameAr}</td><td>{w.wardType}</td><td>{w.totalBeds}</td><td>{w.occupied}</td>
                    <td>{w.active ? <span className="badge badge-ok">{ar ? "نشط" : "Active"}</span> : <span className="badge badge-muted">{ar ? "موقوف" : "Inactive"}</span>}</td>
                    <td>{w.active ? <button type="button" className="btn btn-ghost btn-small" disabled={busy || w.occupied > 0} onClick={() => deactivateWard(w)}>{ar ? "إيقاف" : "Deactivate"}</button> : null}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : null}
      </section>

      {drawer?.kind === "admit" ? (
        <div className="drawer-overlay" onClick={() => setDrawer(null)}>
          <div className="drawer" onClick={(e) => e.stopPropagation()}>
            <h2>{ar ? "قبول وتنويم مريض" : "Admit a patient"}</h2>
            <form onSubmit={submitAdmit}>
              <div className="field l-mb">
                <label>{ar ? "بحث المريض (الاسم أو الرقم القومي)" : "Patient search (name or national ID)"}</label>
                <input value={patientQuery} onChange={(e) => searchPatients(e.target.value)} placeholder={ar ? "اكتب حرفين على الأقل…" : "Type at least 2 characters…"} />
                {patientResults.length > 0 ? (
                  <div className="mini-box">
                    {patientResults.map((pt) => (
                      <button key={pt.id} type="button" className="picker-item"
                        onClick={() => { setAdmitForm({ ...admitForm, patientId: pt.id, patientName: pt.fullNameAr }); setPatientResults([]); setPatientQuery(pt.fullNameAr); }}>
                        {pt.fullNameAr} <span className="muted" dir="ltr">{pt.nationalId}</span>
                      </button>
                    ))}
                  </div>
                ) : null}
                {admitForm.patientId ? <div className="picked-note">✓ {admitForm.patientName}</div> : null}
              </div>
              <div className="form-grid cols-1">
                <div className="field"><label>{ar ? "الجناح" : "Ward"}</label>
                  <select required value={admitForm.wardId} onChange={(e) => setAdmitForm({ ...admitForm, wardId: e.target.value })}>
                    <option value="">—</option>
                    {wardOptions.filter((w) => w.active).map((w) => <option key={w.id} value={w.id}>{w.nameAr} ({w.free} {ar ? "شاغر" : "free"})</option>)}
                  </select></div>
                <div className="field"><label>{ar ? "رقم/رمز السرير" : "Bed label"}</label><input required value={admitForm.bedLabel} onChange={(e) => setAdmitForm({ ...admitForm, bedLabel: e.target.value })} placeholder="12-A" /></div>
                <div className="field"><label>{ar ? "الطبيب المعالج" : "Attending physician"}</label><input value={admitForm.attendingName} onChange={(e) => setAdmitForm({ ...admitForm, attendingName: e.target.value })} /></div>
                <div className="field"><label>{ar ? "سبب التنويم" : "Admission reason"}</label><input value={admitForm.admissionReason} onChange={(e) => setAdmitForm({ ...admitForm, admissionReason: e.target.value })} /></div>
                <div className="field"><label>{ar ? "مصدر القبول" : "Source"}</label>
                  <select value={admitForm.source} onChange={(e) => setAdmitForm({ ...admitForm, source: e.target.value })}>
                    {SOURCES.map((s) => <option key={s} value={s}>{s}</option>)}
                  </select></div>
              </div>
              <div className="form-actions">
                <button type="submit" className="btn" disabled={busy || !admitForm.patientId}>{ar ? "تنويم" : "Admit"}</button>
                <button type="button" className="btn btn-ghost" onClick={() => setDrawer(null)}>{ar ? "إلغاء" : "Cancel"}</button>
              </div>
            </form>
          </div>
        </div>
      ) : null}

      {drawer?.kind === "transfer" ? (
        <div className="drawer-overlay" onClick={() => setDrawer(null)}>
          <div className="drawer" onClick={(e) => e.stopPropagation()}>
            <h2>{ar ? "تحويل داخلي" : "Internal transfer"} — {drawer.row.patientName}</h2>
            <p className="text-md">{ar ? "من" : "From"}: {drawer.row.wardName} · {drawer.row.bedLabel}</p>
            <form onSubmit={submitTransfer}>
              <div className="form-grid cols-1">
                <div className="field"><label>{ar ? "الجناح المستهدف" : "Target ward"}</label>
                  <select required value={transferForm.toWardId} onChange={(e) => setTransferForm({ ...transferForm, toWardId: e.target.value })}>
                    <option value="">—</option>
                    {wardOptions.filter((w) => w.active).map((w) => <option key={w.id} value={w.id}>{w.nameAr} ({w.free} {ar ? "شاغر" : "free"})</option>)}
                  </select></div>
                <div className="field"><label>{ar ? "السرير المستهدف" : "Target bed"}</label><input required value={transferForm.toBedLabel} onChange={(e) => setTransferForm({ ...transferForm, toBedLabel: e.target.value })} /></div>
                <div className="field"><label>{ar ? "سبب التحويل (إلزامي)" : "Reason (required)"}</label><input required value={transferForm.reason} onChange={(e) => setTransferForm({ ...transferForm, reason: e.target.value })} /></div>
              </div>
              <div className="form-actions">
                <button type="submit" className="btn" disabled={busy}>{ar ? "تنفيذ التحويل" : "Transfer"}</button>
                <button type="button" className="btn btn-ghost" onClick={() => setDrawer(null)}>{ar ? "إلغاء" : "Cancel"}</button>
              </div>
            </form>
          </div>
        </div>
      ) : null}

      {drawer?.kind === "discharge" ? (
        <div className="drawer-overlay" onClick={() => setDrawer(null)}>
          <div className="drawer" onClick={(e) => e.stopPropagation()}>
            <h2>{ar ? "خروج مريض" : "Discharge"} — {drawer.row.patientName}</h2>
            <form onSubmit={submitDischarge}>
              <div className="field">
                <label>{ar ? "قرار الخروج" : "Disposition"}</label>
                <select value={disposition} onChange={(e) => setDisposition(e.target.value)}>
                  {DISPOSITIONS.map((d) => <option key={d} value={d}>{d}</option>)}
                </select>
              </div>
              <div className="form-actions">
                <button type="submit" className="btn" disabled={busy}>{ar ? "تأكيد الخروج" : "Confirm discharge"}</button>
                <button type="button" className="btn btn-ghost" onClick={() => setDrawer(null)}>{ar ? "إلغاء" : "Cancel"}</button>
              </div>
            </form>
          </div>
        </div>
      ) : null}

      {toast ? <div className="toast-stack"><div className="toast">{toast}</div></div> : null}
    </>
  );
}
