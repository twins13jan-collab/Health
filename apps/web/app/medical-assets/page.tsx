"use client";

import { useCallback, useEffect, useState } from "react";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";
import { Button, Card, Drawer, EmptyState, ErrorBanner, PageHead, Toasts } from "@/components/ui";
import KpiCard from "@/components/KpiCard";

type AssetRow = {
  id: string; asset_code: string; type: string | null; manufacturer: string | null; model: string | null;
  serial_number: string | null; location: string | null; owner: string | null; status: string;
  calibration_due: string | null; calibration_state: string; calibration_days: number | null;
  next_maintenance_due: string | null; maintenance_state: string;
  open_work_orders: number; open_incidents: number; patient_incident_open: boolean; usable: boolean; rank: number;
};
type WoRow = {
  id: string; asset_id: string; asset_code: string | null; type: string; technician: string | null;
  status: string; spare_part_shortage: boolean; spare_purchase_ref: string | null; age_hours: number | null;
};
type Board = { assets: AssetRow[]; work_orders: WoRow[]; summary: Record<string, number> };

const STATUS_AR: Record<string, string> = { in_service: "في الخدمة", out_of_service: "خارج الخدمة", under_maintenance: "قيد الصيانة", quarantined: "محجوز (معايرة مرفوضة)" };
const STATE_AR: Record<string, string> = { overdue: "متأخرة", due_soon: "تستحق قريبًا", current: "سارية", unscheduled: "غير مجدولة" };

export default function BiomedBoardPage() {
  const { lang } = useLang();
  const ar = lang === "ar";
  const [board, setBoard] = useState<Board | null>(null);
  const [err, setErr] = useState("");
  const [toasts, setToasts] = useState<{ text: string; err?: boolean }[]>([]);
  const [woAsset, setWoAsset] = useState<AssetRow | null>(null);
  const [calAsset, setCalAsset] = useState<AssetRow | null>(null);
  const [incAsset, setIncAsset] = useState<AssetRow | null>(null);
  const [completeWo, setCompleteWo] = useState<WoRow | null>(null);
  const [newAsset, setNewAsset] = useState(false);
  const [woForm, setWoForm] = useState({ type: "corrective", technician: "" });
  const [calForm, setCalForm] = useState({ referenceStandard: "", certificateRef: "", deviation: "", accepted: true, dueDate: "" });
  const [incForm, setIncForm] = useState({ description: "", patientAffected: false, safetyCaseRef: "" });
  const [doneForm, setDoneForm] = useState({ spareParts: "", shortage: false, purchaseRef: "", downtime: "", result: "" });
  const [assetForm, setAssetForm] = useState({ assetCode: "", type: "", manufacturer: "", model: "", serialNumber: "", location: "", owner: "", calibrationDue: "" });

  const toast = (text: string, isErr = false) => setToasts((t) => [...t, { text, err: isErr }]);
  const load = useCallback(async () => {
    try { setBoard(await api.getJson<Board>("/api/medical-assets/board")); setErr(""); }
    catch (e) { setErr(e instanceof Error ? e.message : String(e)); }
  }, []);
  useEffect(() => {
    load();
    const id = window.setInterval(load, 30000);
    return () => window.clearInterval(id);
  }, [load]);

  const openWo = async () => {
    if (!woAsset) return;
    try {
      await api.postJson(`/api/medical-assets/${woAsset.id}/work-orders`, woForm);
      toast(ar ? "فُتح أمر العمل وصار الجهاز قيد الصيانة" : "Work order opened; device under maintenance");
      setWoAsset(null); load();
    } catch (e) { toast(e instanceof Error ? e.message : String(e), true); }
  };
  const calibrate = async () => {
    if (!calAsset) return;
    try {
      const r = await api.postJson<{ asset_status: string }>(`/api/medical-assets/${calAsset.id}/calibrations`, {
        ...calForm, dueDate: calForm.dueDate || null,
      });
      toast(calForm.accepted ? (ar ? "اعتُمدت المعايرة وحُدّث الاستحقاق" : "Calibration accepted; due date updated")
        : (ar ? `رُفضت المعايرة — الجهاز الآن: ${r.asset_status}` : `Calibration rejected — device now: ${r.asset_status}`), !calForm.accepted);
      setCalAsset(null); load();
    } catch (e) { toast(e instanceof Error ? e.message : String(e), true); }
  };
  const reportIncident = async () => {
    if (!incAsset) return;
    try {
      await api.postJson("/api/device-incidents", { assetId: incAsset.id, ...incForm });
      toast(ar ? "سُجّل البلاغ وخرج الجهاز من الخدمة — الإفراج للهندسة الطبية فقط" : "Incident recorded; device out of service — release is biomedical only");
      setIncAsset(null); load();
    } catch (e) { toast(e instanceof Error ? e.message : String(e), true); }
  };
  const complete = async () => {
    if (!completeWo) return;
    try {
      const r = await api.postJson<{ purchase_request_created: boolean }>(`/api/medical-assets/work-orders/${completeWo.id}/complete`, {
        spareParts: doneForm.spareParts || null, sparePartShortage: doneForm.shortage,
        sparePurchaseRef: doneForm.purchaseRef || null,
        downtimeHours: doneForm.downtime ? Number(doneForm.downtime) : null, result: doneForm.result || null,
      });
      toast(r.purchase_request_created ? (ar ? "أُتمّ أمر العمل وأُنشئ طلب شراء لقطعة الغيار" : "Completed; spare-part purchase request created") : (ar ? "أُتمّ أمر العمل — بانتظار الإفراج" : "Completed — awaiting release"));
      setCompleteWo(null); load();
    } catch (e) { toast(e instanceof Error ? e.message : String(e), true); }
  };
  const release = async (w: WoRow) => {
    try {
      await api.postJson(`/api/medical-assets/work-orders/${w.id}/release`, {});
      toast(ar ? "أُفرج عن الجهاز وعاد للخدمة" : "Device released back to service");
      load();
    } catch (e) { toast(e instanceof Error ? e.message : String(e), true); }
  };
  const createAsset = async () => {
    try {
      await api.postJson("/api/medical-assets", { ...assetForm, calibrationDue: assetForm.calibrationDue || null });
      toast(ar ? "سُجّل الأصل ودخل الخدمة" : "Asset registered and commissioned");
      setNewAsset(false); load();
    } catch (e) { toast(e instanceof Error ? e.message : String(e), true); }
  };

  const s = board?.summary ?? {};
  return (
    <div>
      <PageHead
        title={ar ? "الهندسة الطبية — الأجهزة والصيانة والمعايرة" : "Biomedical engineering — devices, maintenance & calibration"}
        sub={ar ? "كل جهاز بهويته وحالته ومعايرته: المؤثر على مريض أولًا، ثم خارج الخدمة، ثم خارج المعايرة (غير صالح للاستخدام ولو كان يعمل). المبلّغ عن العطل لا يعيد الجهاز للخدمة أبدًا — الإفراج للهندسة الطبية والجودة." : "Every device with its identity, state and calibration: patient-affecting first, then out of service, then out of calibration (unfit even if it powers on). The fault reporter never returns a device to service — release belongs to biomedical engineering and quality."}
        actions={<Button onClick={() => setNewAsset(true)}>{ar ? "+ تسجيل جهاز" : "+ Register device"}</Button>}
      />
      {err ? <ErrorBanner onRetry={load}>{err}</ErrorBanner> : null}
      <div className="kpi-grid">
        <KpiCard label={ar ? "أجهزة تحتاج انتباهًا" : "Assets needing attention"} value={s.attention_assets ?? "—"} />
        <KpiCard label={ar ? "معايرة متأخرة" : "Calibration overdue"} value={s.calibration_overdue ?? "—"} />
        <KpiCard label={ar ? "خارج الخدمة" : "Out of service"} value={s.out_of_service ?? "—"} />
        <KpiCard label={ar ? "أوامر عمل مفتوحة" : "Open work orders"} value={s.work_orders_open ?? "—"} />
        <KpiCard label={ar ? "بانتظار الإفراج" : "Awaiting release"} value={s.awaiting_release ?? "—"} />
        <KpiCard label={ar ? "بلاغات مؤثرة على مرضى" : "Patient-affecting incidents"} value={s.incidents_patient_affected ?? "—"} />
      </div>

      <Card><h3 className="card-h">{ar ? "طابور انتباه الأجهزة" : "Device attention queue"}</h3>
        {!board ? <p className="muted">{ar ? "جارٍ التحميل…" : "Loading…"}</p>
          : board.assets.length === 0 ? <EmptyState>{ar ? "لا جهاز يحتاج انتباهًا — كل الأجهزة في الخدمة ومعايرتها وصيانتها سارية." : "No device needs attention — every device is in service with current calibration and maintenance."}</EmptyState>
          : board.assets.map((a) => (
            <div key={a.id} className="row-card">
              <div className="grow-field">
                <strong>{a.asset_code}</strong> <span className="muted">{a.type ?? ""} {a.manufacturer ?? ""} {a.model ?? ""}</span>
                <div className="muted">
                  {STATUS_AR[a.status] ?? a.status} · {a.location ?? "—"} · {ar ? "المالك" : "Owner"}: {a.owner ?? "—"}
                  {" · "}{ar ? "المعايرة" : "Calibration"}: {a.calibration_due ?? "—"} ({STATE_AR[a.calibration_state] ?? a.calibration_state}{a.calibration_days != null ? `، ${a.calibration_days} ${ar ? "يوم" : "d"}` : ""})
                  {" · "}{ar ? "الصيانة الوقائية" : "PM"}: {a.next_maintenance_due ?? "—"} ({STATE_AR[a.maintenance_state] ?? a.maintenance_state})
                </div>
                {a.patient_incident_open ? <span className="badge danger">{ar ? "بلاغ مؤثر على مريض مفتوح" : "Patient-affecting incident open"}</span> : null}
                {!a.usable && a.status === "in_service" ? <span className="badge warn">{ar ? "غير صالح للاستخدام — خارج المعايرة" : "UNFIT — out of calibration"}</span> : null}
                {a.open_work_orders > 0 ? <span className="badge">{a.open_work_orders} {ar ? "أمر عمل مفتوح" : "open WO"}</span> : null}
              </div>
              <Button variant="soft" onClick={() => { setWoAsset(a); setWoForm({ type: "corrective", technician: "" }); }}>{ar ? "أمر عمل" : "Work order"}</Button>
              <Button variant="soft" onClick={() => { setCalAsset(a); setCalForm({ referenceStandard: "", certificateRef: "", deviation: "", accepted: true, dueDate: "" }); }}>{ar ? "معايرة" : "Calibrate"}</Button>
              <Button variant="danger" onClick={() => { setIncAsset(a); setIncForm({ description: "", patientAffected: false, safetyCaseRef: "" }); }}>{ar ? "بلاغ عطل" : "Report fault"}</Button>
            </div>
          ))}
      </Card>

      <Card><h3 className="card-h">{ar ? "أوامر العمل" : "Work orders"}</h3>
        {!board || board.work_orders.length === 0 ? <EmptyState>{ar ? "لا أوامر عمل مفتوحة" : "No open work orders"}</EmptyState>
          : board.work_orders.map((w) => (
            <div key={w.id} className="row-card">
              <div className="grow-field">
                <strong>{w.asset_code ?? ""}</strong> <span className="muted">{w.type} · {w.technician ?? "—"} · {w.status === "open" ? (ar ? "مفتوح" : "Open") : (ar ? "مكتمل — بانتظار الإفراج" : "Completed — awaiting release")} · {w.age_hours ?? 0}{ar ? " س" : "h"}</span>
                {w.spare_part_shortage ? <div><span className="badge warn">{ar ? "نقص قطعة غيار" : "Spare-part shortage"}: {w.spare_purchase_ref ?? ""}</span></div> : null}
              </div>
              {w.status === "open" ? <Button variant="soft" onClick={() => { setCompleteWo(w); setDoneForm({ spareParts: "", shortage: false, purchaseRef: "", downtime: "", result: "" }); }}>{ar ? "إتمام" : "Complete"}</Button> : null}
              {w.status === "completed" ? <Button onClick={() => release(w)}>{ar ? "إفراج للخدمة" : "Release to service"}</Button> : null}
            </div>
          ))}
      </Card>

      {woAsset ? (
        <Drawer title={`${ar ? "أمر عمل" : "Work order"} — ${woAsset.asset_code}`} onClose={() => setWoAsset(null)}>
          <div className="field"><label>{ar ? "النوع" : "Type"}</label>
            <select value={woForm.type} onChange={(e) => setWoForm({ ...woForm, type: e.target.value })}>
              <option value="corrective">{ar ? "إصلاحي" : "Corrective"}</option>
              <option value="preventive">{ar ? "وقائي" : "Preventive"}</option>
              <option value="calibration">{ar ? "معايرة" : "Calibration"}</option>
            </select></div>
          <div className="field"><label>{ar ? "الفني" : "Technician"}</label>
            <input value={woForm.technician} onChange={(e) => setWoForm({ ...woForm, technician: e.target.value })} /></div>
          <Button onClick={openWo}>{ar ? "فتح أمر العمل" : "Open work order"}</Button>
        </Drawer>
      ) : null}
      {calAsset ? (
        <Drawer title={`${ar ? "معايرة" : "Calibration"} — ${calAsset.asset_code}`} onClose={() => setCalAsset(null)}>
          <div className="field"><label>{ar ? "المعيار المرجعي" : "Reference standard"}</label>
            <input value={calForm.referenceStandard} onChange={(e) => setCalForm({ ...calForm, referenceStandard: e.target.value })} /></div>
          <div className="field"><label>{ar ? "مرجع الشهادة" : "Certificate ref"}</label>
            <input value={calForm.certificateRef} onChange={(e) => setCalForm({ ...calForm, certificateRef: e.target.value })} /></div>
          <div className="field"><label>{ar ? "الانحراف" : "Deviation"}</label>
            <input value={calForm.deviation} onChange={(e) => setCalForm({ ...calForm, deviation: e.target.value })} /></div>
          <div className="field"><label>{ar ? "النتيجة" : "Outcome"}</label>
            <select value={calForm.accepted ? "1" : "0"} onChange={(e) => setCalForm({ ...calForm, accepted: e.target.value === "1" })}>
              <option value="1">{ar ? "مقبولة" : "Accepted"}</option>
              <option value="0">{ar ? "مرفوضة — يُحجز الجهاز" : "Rejected — device quarantined"}</option>
            </select></div>
          <div className="field"><label>{ar ? "الاستحقاق القادم" : "Next due"}</label>
            <input type="date" value={calForm.dueDate} onChange={(e) => setCalForm({ ...calForm, dueDate: e.target.value })} /></div>
          <Button onClick={calibrate}>{ar ? "تسجيل المعايرة" : "Record calibration"}</Button>
        </Drawer>
      ) : null}
      {incAsset ? (
        <Drawer title={`${ar ? "بلاغ عطل" : "Fault report"} — ${incAsset.asset_code}`} onClose={() => setIncAsset(null)}>
          <div className="field"><label>{ar ? "الوصف" : "Description"}</label>
            <input value={incForm.description} onChange={(e) => setIncForm({ ...incForm, description: e.target.value })} /></div>
          <label className="check-row"><input type="checkbox" checked={incForm.patientAffected} onChange={(e) => setIncForm({ ...incForm, patientAffected: e.target.checked })} /> {ar ? "أثّر على مريض" : "Patient affected"}</label>
          {incForm.patientAffected ? (
            <div className="field"><label>{ar ? "مرجع بلاغ السلامة (إلزامي)" : "Safety case ref (required)"}</label>
              <input value={incForm.safetyCaseRef} onChange={(e) => setIncForm({ ...incForm, safetyCaseRef: e.target.value })} /></div>
          ) : null}
          <Button variant="danger" onClick={reportIncident}>{ar ? "تسجيل البلاغ وإخراج الجهاز" : "Record & take out of service"}</Button>
        </Drawer>
      ) : null}
      {completeWo ? (
        <Drawer title={ar ? "إتمام أمر العمل" : "Complete work order"} onClose={() => setCompleteWo(null)}>
          <div className="field"><label>{ar ? "قطع الغيار المستخدمة" : "Spare parts used"}</label>
            <input value={doneForm.spareParts} onChange={(e) => setDoneForm({ ...doneForm, spareParts: e.target.value })} /></div>
          <label className="check-row"><input type="checkbox" checked={doneForm.shortage} onChange={(e) => setDoneForm({ ...doneForm, shortage: e.target.checked })} /> {ar ? "نقص قطعة غيار" : "Spare-part shortage"}</label>
          {doneForm.shortage ? (
            <div className="field"><label>{ar ? "مرجع طلب الشراء (إلزامي عند النقص)" : "Purchase request ref (required on shortage)"}</label>
              <input value={doneForm.purchaseRef} onChange={(e) => setDoneForm({ ...doneForm, purchaseRef: e.target.value })} /></div>
          ) : null}
          <div className="field"><label>{ar ? "ساعات التوقف" : "Downtime hours"}</label>
            <input type="number" min="0" step="0.5" value={doneForm.downtime} onChange={(e) => setDoneForm({ ...doneForm, downtime: e.target.value })} /></div>
          <div className="field"><label>{ar ? "النتيجة" : "Result"}</label>
            <input value={doneForm.result} onChange={(e) => setDoneForm({ ...doneForm, result: e.target.value })} /></div>
          <Button onClick={complete}>{ar ? "إتمام" : "Complete"}</Button>
        </Drawer>
      ) : null}
      {newAsset ? (
        <Drawer title={ar ? "تسجيل جهاز طبي" : "Register a medical device"} onClose={() => setNewAsset(false)}>
          <div className="field"><label>{ar ? "كود الأصل" : "Asset code"}</label>
            <input value={assetForm.assetCode} onChange={(e) => setAssetForm({ ...assetForm, assetCode: e.target.value })} /></div>
          <div className="field"><label>{ar ? "النوع" : "Type"}</label>
            <input value={assetForm.type} onChange={(e) => setAssetForm({ ...assetForm, type: e.target.value })} placeholder="ventilator / monitor / pump…" /></div>
          <div className="field"><label>{ar ? "الشركة / الموديل" : "Manufacturer / model"}</label>
            <input value={assetForm.manufacturer} onChange={(e) => setAssetForm({ ...assetForm, manufacturer: e.target.value })} />
            <input value={assetForm.model} onChange={(e) => setAssetForm({ ...assetForm, model: e.target.value })} /></div>
          <div className="field"><label>{ar ? "الرقم التسلسلي" : "Serial number"}</label>
            <input value={assetForm.serialNumber} onChange={(e) => setAssetForm({ ...assetForm, serialNumber: e.target.value })} /></div>
          <div className="field"><label>{ar ? "الموقع" : "Location"}</label>
            <input value={assetForm.location} onChange={(e) => setAssetForm({ ...assetForm, location: e.target.value })} /></div>
          <div className="field"><label>{ar ? "المالك / المسؤول" : "Owner"}</label>
            <input value={assetForm.owner} onChange={(e) => setAssetForm({ ...assetForm, owner: e.target.value })} /></div>
          <div className="field"><label>{ar ? "استحقاق المعايرة" : "Calibration due"}</label>
            <input type="date" value={assetForm.calibrationDue} onChange={(e) => setAssetForm({ ...assetForm, calibrationDue: e.target.value })} /></div>
          <Button onClick={createAsset}>{ar ? "تسجيل" : "Register"}</Button>
        </Drawer>
      ) : null}
      <Toasts items={toasts} />
    </div>
  );
}
