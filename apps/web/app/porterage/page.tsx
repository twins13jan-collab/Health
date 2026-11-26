"use client";

import { useCallback, useEffect, useState } from "react";
import KpiCard from "@/components/KpiCard";
import { Button, Drawer, PageHead, SectionHead, Toasts } from "@/components/ui";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";

interface Transport { id: string; patient_ref?: string; from_location?: string; to_location?: string; priority?: string; status?: string; escort_required?: boolean; oxygen_required?: boolean; escort_name?: string; assigned_porter?: string; wait_minutes?: number; target_minutes?: number; breached?: boolean }
interface Room { id: string; room_ref?: string; status?: string; cleaning_done?: boolean; bed_ready?: boolean; inspected_by?: string; wait_minutes?: number; overdue?: boolean }
interface Laundry { id: string; batch_ref?: string; batch_type?: string; sent_count?: number; returned_count?: number; loss_count?: number; state?: string }
interface ServiceReq { id: string; patient_ref?: string; request_type?: string; detail?: string; wait_minutes?: number }
interface Board { transports: Transport[]; rooms: Room[]; laundry: Laundry[]; service_requests: ServiceReq[]; summary: Record<string, number> }

/** Support-services operations board (module 109): the internal
 *  transport queue in attention order — response-target breaches
 *  first, then critical, then mid-journey — room turnovers with
 *  overdue flags, laundry batches with losses named, and patient
 *  hotel-service requests tracked to fulfilment. Replaces the old
 *  generic page, whose list endpoint never existed. */
export default function PorteragePage() {
  const { lang, t } = useLang();
  const ar = lang === "ar";
  const [board, setBoard] = useState<Board | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [toasts, setToasts] = useState<{ text: string; err?: boolean }[]>([]);
  const [busy, setBusy] = useState(false);
  const [assignSel, setAssignSel] = useState<Transport | null>(null);
  const [porter, setPorter] = useState("");
  const [escort, setEscort] = useState("");
  const [receiveSel, setReceiveSel] = useState<Laundry | null>(null);
  const [returned, setReturned] = useState("");
  const [newOpen, setNewOpen] = useState(false);
  const [form, setForm] = useState({ patientRef: "", fromLocation: "", toLocation: "", priority: "routine", oxygenRequired: false });

  const toast = (text: string, err?: boolean) => { setToasts((x) => [...x, { text, err }]); setTimeout(() => setToasts((x) => x.slice(1)), 4500); };

  const load = useCallback(async () => {
    try { setBoard(await api.getJson<Board>("/api/porterage/board")); }
    catch (e) { setError(e instanceof Error ? e.message : String(e)); }
  }, []);
  useEffect(() => { load(); const iv = setInterval(load, 30000); return () => clearInterval(iv); }, [load]);

  async function act(path: string, body: unknown, okMsg: string) {
    setBusy(true);
    try { await api.postJson(path, body); toast(okMsg); setAssignSel(null); setReceiveSel(null); setNewOpen(false); await load(); }
    catch (e) { toast(e instanceof Error ? e.message : String(e), true); }
    finally { setBusy(false); }
  }

  const s = board?.summary ?? {};
  const prioBadge = (p?: string) => p === "critical" ? "badge danger" : p === "urgent" ? "badge warn" : "badge info";

  return (
    <div>
      <PageHead eyebrow={ar ? "الخدمات المساندة" : "Support services"} title={t("porterage")}
        sub={ar ? "لوحة تشغيل النقل الداخلي وتجهيز الغرف والغسيل وطلبات المرضى الفندقية: المخالفات لهدف زمن الاستجابة أولًا، ثم الحرج، ثم من هم في الطريق — والغرفة المتأخرة عن ساعتين معلّمة لأنها سرير محجوب عن الطوارئ والقبول."
               : "Operations board for internal transport, room turnover, laundry and patient hotel requests: response-target breaches first, then critical, then mid-journey — a room past two hours is flagged because it is a bed denied to the ER and admissions."}
        actions={<>
          <Button onClick={() => setNewOpen(true)}>{ar ? "طلب نقل جديد" : "New transport"}</Button>
          <Button onClick={load}>{ar ? "تحديث" : "Refresh"}</Button>
        </>} />
      {error ? <div className="alert-error">{error}</div> : null}
      <div className="kpi-grid">
        <KpiCard label={ar ? "نقل مفتوح" : "Open transports"} value={s.transports_open ?? "—"} />
        <KpiCard label={ar ? "تجاوز هدف الاستجابة" : "Response breaches"} value={s.transports_breached ?? "—"} />
        <KpiCard label={ar ? "غرف بانتظار التجهيز" : "Rooms pending"} value={s.rooms_pending ?? "—"} />
        <KpiCard label={ar ? "غرف متأخرة" : "Rooms overdue"} value={s.rooms_overdue ?? "—"} />
        <KpiCard label={ar ? "غسيل قيد التنفيذ" : "Laundry in process"} value={s.laundry_open ?? "—"} />
        <KpiCard label={ar ? "طلبات خدمة مفتوحة" : "Open service requests"} value={s.service_open ?? "—"} />
      </div>

      <SectionHead title={ar ? "طابور النقل الداخلي" : "Internal transport queue"} />
      {!board ? <div className="empty">{ar ? "جارٍ التحميل…" : "Loading…"}</div> : board.transports.length === 0 ? <div className="empty">{ar ? "لا طلبات نقل مفتوحة" : "No open transports"}</div> : (
        <div className="cards-grid">
          {board.transports.map((tr) => (
            <section key={tr.id} className="card">
              <div className="l-row">
                <strong>{tr.from_location} ← {tr.to_location}</strong>
                <span className={prioBadge(tr.priority)}>{tr.priority}</span>
                {tr.breached ? <span className="badge danger">{ar ? "تجاوز الهدف" : "breached"}</span> : null}
              </div>
              <div className="l-row">
                <span className="badge info">{tr.patient_ref}</span>
                <span className="badge info">{tr.status}</span>
                <span className="badge gold">{ar ? "انتظار" : "wait"} {tr.wait_minutes} / {tr.target_minutes} {ar ? "د" : "min"}</span>
                {tr.oxygen_required ? <span className="badge warn">{ar ? "أكسجين" : "oxygen"}</span> : null}
                {tr.assigned_porter ? <span className="badge info">{tr.assigned_porter}</span> : null}
                {tr.escort_name ? <span className="badge info">{ar ? "مرافق:" : "escort:"} {tr.escort_name}</span> : null}
              </div>
              <div className="l-actions">
                {tr.status === "requested" ? <Button disabled={busy} onClick={() => { setAssignSel(tr); setPorter(""); setEscort(""); }}>{ar ? "إسناد" : "Assign"}</Button> : null}
                {tr.status === "assigned" ? <Button disabled={busy} onClick={() => act(`/api/porterage/transports/${tr.id}/pickup`, {}, ar ? "تم الاستلام" : "Picked up")}>{ar ? "استلام المريض" : "Pick up"}</Button> : null}
                {tr.status === "picked_up" ? <Button disabled={busy} onClick={() => act(`/api/porterage/transports/${tr.id}/deliver`, { deliveredAt: new Date().toISOString() }, ar ? "تم التوصيل" : "Delivered")}>{ar ? "توصيل" : "Deliver"}</Button> : null}
              </div>
            </section>
          ))}
        </div>
      )}

      <SectionHead title={ar ? "تجهيز الغرف بعد الخروج" : "Room turnover after discharge"} />
      {board && board.rooms.length === 0 ? <div className="empty">{ar ? "لا غرف بانتظار التجهيز" : "No rooms awaiting turnover"}</div> : (
        <div className="cards-grid">
          {(board?.rooms ?? []).map((r) => (
            <section key={r.id} className="card">
              <div className="l-row">
                <strong>{r.room_ref}</strong>
                {r.overdue ? <span className="badge danger">{ar ? "متأخرة" : "overdue"}</span> : <span className="badge warn">{r.status}</span>}
                <span className="badge gold">{r.wait_minutes} {ar ? "دقيقة" : "min"}</span>
              </div>
              <div className="l-row">
                <span className={r.cleaning_done ? "badge ok" : "badge info"}>{ar ? "التنظيف" : "cleaning"} {r.cleaning_done ? "✓" : "…"}</span>
                <span className={r.bed_ready ? "badge ok" : "badge info"}>{ar ? "السرير" : "bed"} {r.bed_ready ? "✓" : "…"}</span>
                <span className={r.inspected_by ? "badge ok" : "badge info"}>{ar ? "التفتيش" : "inspection"} {r.inspected_by ?? "…"}</span>
              </div>
              <div className="l-actions">
                <Button disabled={busy} onClick={() => act(`/api/porterage/rooms/${r.id}/progress`, { cleaningDone: true, bedReady: r.bed_ready ?? false, inspectedBy: r.inspected_by ?? null }, ar ? "تم التنظيف" : "Cleaning done")}>{ar ? "تنظيف ✓" : "Cleaned"}</Button>
                <Button disabled={busy} onClick={() => act(`/api/porterage/rooms/${r.id}/progress`, { cleaningDone: r.cleaning_done ?? false, bedReady: true, inspectedBy: r.inspected_by ?? null }, ar ? "السرير جاهز" : "Bed ready")}>{ar ? "سرير ✓" : "Bed"}</Button>
                <Button disabled={busy} onClick={() => act(`/api/porterage/rooms/${r.id}/progress`, { cleaningDone: r.cleaning_done ?? false, bedReady: r.bed_ready ?? false, inspectedBy: "مفتش المناوبة" }, ar ? "تم التفتيش والتوقيع" : "Inspected & signed")}>{ar ? "تفتيش وتوقيع" : "Inspect"}</Button>
              </div>
            </section>
          ))}
        </div>
      )}

      <SectionHead title={ar ? "الغسيل" : "Laundry"} />
      {board && board.laundry.length === 0 ? <div className="empty">{ar ? "لا دفعات غسيل حالية" : "No current laundry batches"}</div> : (
        <div className="cards-grid">
          {(board?.laundry ?? []).map((b) => (
            <section key={b.id} className="card">
              <div className="l-row">
                <strong>{b.batch_ref}</strong>
                <span className="badge info">{b.batch_type}</span>
                {b.state === "received_with_loss" ? <span className="badge danger">{ar ? "فقد" : "loss"} {b.loss_count}</span> : b.state === "received_complete" ? <span className="badge ok">{ar ? "مكتملة" : "complete"}</span> : <span className="badge warn">{ar ? "قيد التنفيذ" : "in process"}</span>}
              </div>
              <div className="l-row"><span className="badge info">{ar ? "أُرسل" : "sent"} {b.sent_count}</span>{b.returned_count != null ? <span className="badge info">{ar ? "استُلم" : "returned"} {b.returned_count}</span> : null}</div>
              {b.state === "in_process" ? <div className="l-actions"><Button disabled={busy} onClick={() => { setReceiveSel(b); setReturned(""); }}>{ar ? "استلام الدفعة" : "Receive batch"}</Button></div> : null}
            </section>
          ))}
        </div>
      )}

      <SectionHead title={ar ? "طلبات خدمة المرضى" : "Patient service requests"} />
      {board && board.service_requests.length === 0 ? <div className="empty">{ar ? "لا طلبات مفتوحة" : "No open requests"}</div> : (
        <div className="cards-grid">
          {(board?.service_requests ?? []).map((sr) => (
            <section key={sr.id} className="card">
              <div className="l-row"><strong>{sr.request_type}</strong><span className="badge gold">{sr.wait_minutes} {ar ? "دقيقة" : "min"}</span></div>
              <div className="l-row"><span className="badge info">{sr.patient_ref}</span><span>{sr.detail}</span></div>
              <div className="l-actions"><Button disabled={busy} onClick={() => act(`/api/porterage/service-requests/${sr.id}/fulfill`, {}, ar ? "تم التنفيذ" : "Fulfilled")}>{ar ? "تنفيذ" : "Fulfil"}</Button></div>
            </section>
          ))}
        </div>
      )}

      {assignSel ? (
        <Drawer title={ar ? "إسناد النقل" : "Assign transport"} onClose={() => setAssignSel(null)}>
          <div className="field"><label>{ar ? "عامل النقل" : "Porter"}</label><input value={porter} onChange={(e) => setPorter(e.target.value)} /></div>
          <div className="field"><label>{ar ? "المرافق السريري (إلزامي للحرج والأكسجين)" : "Clinical escort (mandatory for critical / oxygen)"}</label><input value={escort} onChange={(e) => setEscort(e.target.value)} /></div>
          <div className="form-actions"><Button disabled={busy || !porter} onClick={() => act(`/api/porterage/transports/${assignSel.id}/assign`, { porter, escortName: escort || null }, ar ? "تم الإسناد" : "Assigned")}>{ar ? "إسناد" : "Assign"}</Button></div>
        </Drawer>
      ) : null}
      {receiveSel ? (
        <Drawer title={ar ? "استلام دفعة الغسيل" : "Receive laundry batch"} onClose={() => setReceiveSel(null)}>
          <div className="field"><label>{ar ? "العدد المستلم" : "Returned count"}</label><input type="number" min={0} value={returned} onChange={(e) => setReturned(e.target.value)} /></div>
          <div className="form-actions"><Button disabled={busy || returned === ""} onClick={() => act(`/api/porterage/laundry/${receiveSel.id}/receive`, { returnedCount: Number(returned) }, ar ? "تم الاستلام وحساب الفقد" : "Received; loss counted")}>{ar ? "استلام" : "Receive"}</Button></div>
        </Drawer>
      ) : null}
      {newOpen ? (
        <Drawer title={ar ? "طلب نقل داخلي جديد" : "New internal transport"} onClose={() => setNewOpen(false)}>
          <div className="form-grid">
            <div className="field"><label>{ar ? "مرجع المريض" : "Patient ref"}</label><input value={form.patientRef} onChange={(e) => setForm({ ...form, patientRef: e.target.value })} /></div>
            <div className="field"><label>{ar ? "من" : "From"}</label><input value={form.fromLocation} onChange={(e) => setForm({ ...form, fromLocation: e.target.value })} /></div>
            <div className="field"><label>{ar ? "إلى" : "To"}</label><input value={form.toLocation} onChange={(e) => setForm({ ...form, toLocation: e.target.value })} /></div>
            <div className="field"><label>{ar ? "الأولوية" : "Priority"}</label>
              <select value={form.priority} onChange={(e) => setForm({ ...form, priority: e.target.value })}>
                <option value="routine">routine</option><option value="urgent">urgent</option><option value="critical">critical</option>
              </select></div>
            <div className="field"><label>{ar ? "يحتاج أكسجين" : "Oxygen required"}</label>
              <select value={form.oxygenRequired ? "true" : "false"} onChange={(e) => setForm({ ...form, oxygenRequired: e.target.value === "true" })}>
                <option value="false">{ar ? "لا" : "No"}</option><option value="true">{ar ? "نعم" : "Yes"}</option>
              </select></div>
          </div>
          <div className="form-actions"><Button disabled={busy || !form.patientRef || !form.fromLocation || !form.toLocation}
            onClick={() => act("/api/porterage/transports", { patientRef: form.patientRef, fromLocation: form.fromLocation, toLocation: form.toLocation, priority: form.priority, oxygenRequired: form.oxygenRequired, requestedAt: new Date().toISOString() }, ar ? "تم إنشاء الطلب" : "Request created")}>{ar ? "إنشاء" : "Create"}</Button></div>
        </Drawer>
      ) : null}
      <Toasts items={toasts} />
    </div>
  );
}
