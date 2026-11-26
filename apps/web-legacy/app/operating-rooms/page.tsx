"use client";

import { useCallback, useEffect, useState } from "react";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";

interface Room { id: string; nameAr: string; specialty?: string; active: boolean }
interface Anaesthesia { id: string; asaClass?: number; anaesthesiaType?: string; anaesthetist?: string; preopAssessment: boolean }
interface CaseView { "case": { id: string; procedureName: string; scheduledAt: string; status: string; checklistStage: string; statusReason?: string }; patientName: string; anaesthesia?: Anaesthesia | null }
interface RoomBoard { room: Room; cases: CaseView[] }
interface Board { date: string; rooms: RoomBoard[]; unassigned: CaseView[]; casesToday: number; waitingCount: number }
interface WaitEntry { id: string; patientId: string; procedureName: string; priority: string; status: string; createdAt: string }
interface WaitRow { entry: WaitEntry; patientName: string; waitingDays: number }
interface Patient { id: string; fullNameAr: string; nationalId: string }

const today = () => new Date().toISOString().slice(0, 10);

/** Operating theatres & anaesthesia (Phase 4.5): the day board by
 *  room, the prioritised surgical waiting list, case flow with
 *  documented reasons, and the anaesthesia record that gates
 *  starting a case (pre-op assessment required except for a true
 *  emergency). The WHO checklist lives on the surgery page. */
export default function OperatingRoomsPage() {
  const { lang } = useLang();
  const ar = lang === "ar";
  const [date, setDate] = useState(today());
  const [board, setBoard] = useState<Board | null>(null);
  const [waitlist, setWaitlist] = useState<WaitRow[]>([]);
  const [drawer, setDrawer] = useState<null | "room" | "waitlist" | "schedule" | "anaesthesia" | "postpone">(null);
  const [target, setTarget] = useState<WaitRow | CaseView | null>(null);
  const [roomForm, setRoomForm] = useState({ nameAr: "", specialty: "" });
  const [waitForm, setWaitForm] = useState({ procedureName: "", specialty: "", priority: "elective", notes: "" });
  const [schedForm, setSchedForm] = useState({ roomId: "", time: "09:00" });
  const [anaForm, setAnaForm] = useState({ asaClass: "", anaesthesiaType: "general", anaesthetist: "", preopAssessment: false, complications: "", notes: "" });
  const [reason, setReason] = useState("");
  const [patientQuery, setPatientQuery] = useState("");
  const [patientResults, setPatientResults] = useState<Patient[]>([]);
  const [picked, setPicked] = useState<Patient | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [toast, setToast] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const showToast = (m: string) => { setToast(m); setTimeout(() => setToast(null), 3500); };

  const load = useCallback(async () => {
    try {
      const [b, w] = await Promise.all([
        api.getJson<Board>(`/api/or/board?date=${date}`),
        api.getJson<{ waitlist: WaitRow[] }>("/api/or/waitlist"),
      ]);
      setBoard(b); setWaitlist(w.waitlist);
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); }
  }, [date]);
  useEffect(() => { load(); }, [load]);

  async function searchPatients(q: string) {
    setPatientQuery(q);
    if (q.trim().length < 2) { setPatientResults([]); return; }
    try { const r = await api.patients(q); setPatientResults(r.data.slice(0, 8)); } catch { setPatientResults([]); }
  }

  async function createRoom(e: React.FormEvent) {
    e.preventDefault(); setBusy(true);
    try {
      await api.postJson("/api/or/rooms", roomForm);
      showToast(ar ? "أُنشئت الغرفة" : "Room created");
      setDrawer(null); setRoomForm({ nameAr: "", specialty: "" }); await load();
    } catch (err) { setError(err instanceof Error ? err.message : String(err)); } finally { setBusy(false); }
  }

  async function addWaitlist(e: React.FormEvent) {
    e.preventDefault();
    if (!picked) { setError(ar ? "اختر المريض من السجل الموحد" : "Pick the patient from the MPI"); return; }
    setBusy(true);
    try {
      await api.postJson("/api/or/waitlist", { patientId: picked.id, ...waitForm });
      showToast(ar ? "أُضيف لقائمة الانتظار" : "Added to the waiting list");
      setDrawer(null); setPicked(null); setPatientQuery(""); await load();
    } catch (err) { setError(err instanceof Error ? err.message : String(err)); } finally { setBusy(false); }
  }

  async function schedule(e: React.FormEvent) {
    e.preventDefault();
    const row = target as WaitRow;
    setBusy(true);
    try {
      await api.postJson(`/api/or/waitlist/${row.entry.id}/schedule`, {
        roomId: schedForm.roomId,
        scheduledAt: `${date}T${schedForm.time}:00+02:00`,
      });
      showToast(ar ? "جُدولت الحالة" : "Case scheduled");
      setDrawer(null); await load();
    } catch (err) { setError(err instanceof Error ? err.message : String(err)); } finally { setBusy(false); }
  }

  async function caseAction(cv: CaseView, action: "start" | "complete" | "postpone", rsn?: string) {
    try {
      await api.postJson(`/api/or/cases/${cv.case.id}/${action}`, rsn ? { reason: rsn } : {});
      showToast(ar ? "حُدّثت الحالة" : "Case updated");
      setDrawer(null); setReason(""); await load();
    } catch (err) { setError(err instanceof Error ? err.message : String(err)); }
  }

  async function saveAnaesthesia(e: React.FormEvent) {
    e.preventDefault();
    const cv = target as CaseView;
    setBusy(true);
    try {
      await api.postJson(`/api/or/cases/${cv.case.id}/anaesthesia`, {
        asaClass: anaForm.asaClass ? Number(anaForm.asaClass) : undefined,
        anaesthesiaType: anaForm.anaesthesiaType,
        anaesthetist: anaForm.anaesthetist || undefined,
        preopAssessment: anaForm.preopAssessment,
        complications: anaForm.complications || undefined,
        notes: anaForm.notes || undefined,
      });
      showToast(ar ? "حُفظ سجل التخدير" : "Anaesthesia record saved");
      setDrawer(null); await load();
    } catch (err) { setError(err instanceof Error ? err.message : String(err)); } finally { setBusy(false); }
  }

  const prioBadge = (p: string) =>
    p === "emergency" ? <span className="badge danger">{ar ? "طارئة" : "Emergency"}</span>
    : p === "urgent" ? <span className="badge warn">{ar ? "عاجلة" : "Urgent"}</span>
    : <span className="badge muted">{ar ? "اختيارية" : "Elective"}</span>;
  const statusBadge = (s: string) =>
    s === "in_progress" ? <span className="badge warn">{ar ? "جارية" : "In progress"}</span>
    : s === "completed" ? <span className="badge ok">{ar ? "اكتملت" : "Completed"}</span>
    : s === "postponed" ? <span className="badge danger">{ar ? "مؤجلة" : "Postponed"}</span>
    : <span className="badge">{ar ? "مجدولة" : "Scheduled"}</span>;

  const caseRow = (cv: CaseView) => (
    <tr key={cv.case.id}>
      <td dir="ltr">{cv.case.scheduledAt ? new Date(cv.case.scheduledAt).toLocaleTimeString(ar ? "ar-EG" : "en-GB", { hour: "2-digit", minute: "2-digit" }) : "—"}</td>
      <td>{cv.patientName}</td>
      <td>{cv.case.procedureName}</td>
      <td>{statusBadge(cv.case.status)}</td>
      <td>{cv.anaesthesia ? <>ASA {cv.anaesthesia.asaClass ?? "—"} · {cv.anaesthesia.preopAssessment ? "✓" : "⚠"} {ar ? "تقييم" : "pre-op"}</> : <span className="muted">{ar ? "لا سجل تخدير" : "No anaesthesia record"}</span>}</td>
      <td>
        <span className="l-actions">
          <button type="button" className="btn btn-small btn-secondary" onClick={() => { setTarget(cv); setAnaForm({ asaClass: cv.anaesthesia?.asaClass ? String(cv.anaesthesia.asaClass) : "", anaesthesiaType: cv.anaesthesia?.anaesthesiaType ?? "general", anaesthetist: cv.anaesthesia?.anaesthetist ?? "", preopAssessment: cv.anaesthesia?.preopAssessment ?? false, complications: "", notes: "" }); setDrawer("anaesthesia"); }}>{ar ? "التخدير" : "Anaesthesia"}</button>
          {cv.case.status === "scheduled" && <button type="button" className="btn btn-small" onClick={() => caseAction(cv, "start")}>{ar ? "بدء" : "Start"}</button>}
          {cv.case.status === "scheduled" && <button type="button" className="btn btn-small btn-secondary" onClick={() => { setTarget(cv); setDrawer("postpone"); }}>{ar ? "تأجيل" : "Postpone"}</button>}
          {cv.case.status === "in_progress" && <button type="button" className="btn btn-small" onClick={() => caseAction(cv, "complete")}>{ar ? "إنهاء" : "Complete"}</button>}
        </span>
      </td>
    </tr>
  );

  return (
    <div>
      <div className="page-head">
        <h1>{ar ? "غرف العمليات والتخدير" : "Operating Rooms & Anaesthesia"}</h1>
        <p>{ar
          ? "لوحة اليوم لكل غرفة، وقائمة انتظار جراحية مرتبة بالأولوية السريرية ثم الأقدمية، وسجل تخدير لكل حالة — ولا تبدأ حالة اختيارية أو عاجلة قبل اكتمال تقييم ما قبل الجراحة."
          : "The day board per room, a surgical waiting list ordered by clinical priority then longest waiting, and an anaesthesia record per case — no elective or urgent case starts before its pre-operative assessment is complete."}</p>
      </div>
      {error && <div className="error-banner">{error} <button type="button" className="btn btn-small btn-secondary" onClick={() => setError(null)}>{ar ? "إغلاق" : "Dismiss"}</button></div>}
      {toast && <div className="toast">{toast}</div>}

      <div className="kpi-grid">
        <div className="kpi-card"><span className="kpi-value">{board?.casesToday ?? "—"}</span><span className="kpi-label">{ar ? "حالات اليوم" : "Cases today"}</span></div>
        <div className="kpi-card"><span className="kpi-value">{board?.waitingCount ?? "—"}</span><span className="kpi-label">{ar ? "في قائمة الانتظار" : "On the waiting list"}</span></div>
        <div className="kpi-card"><span className="kpi-value">{board?.rooms.filter((r) => r.room.active).length ?? "—"}</span><span className="kpi-label">{ar ? "غرف عاملة" : "Active rooms"}</span></div>
      </div>

      <div className="card">
        <div className="l-row">
          <h2 style={{ margin: 0 }}>{ar ? "لوحة اليوم" : "Day board"}</h2>
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} style={{ width: 170 }} />
          <span className="l-grow" />
          <button type="button" className="btn btn-small btn-secondary" onClick={() => setDrawer("room")}>{ar ? "غرفة جديدة" : "New room"}</button>
          <button type="button" className="btn btn-small" onClick={() => setDrawer("waitlist")}>{ar ? "إضافة لقائمة الانتظار" : "Add to waiting list"}</button>
        </div>
        {!board ? <p className="muted">{ar ? "جارٍ التحميل…" : "Loading…"}</p> : board.rooms.length === 0 ? (
          <p className="muted">{ar ? "لا غرف مسجلة بعد — أنشئ أول غرفة عمليات." : "No rooms registered yet — create the first operating room."}</p>
        ) : board.rooms.map((rb) => (
          <div key={rb.room.id} className="l-mt">
            <h3>{rb.room.nameAr}{rb.room.specialty ? ` · ${rb.room.specialty}` : ""} {!rb.room.active && <span className="badge danger">{ar ? "موقوفة" : "Inactive"}</span>}</h3>
            {rb.cases.length === 0 ? <p className="muted">{ar ? "لا حالات مجدولة في هذا اليوم." : "No cases scheduled this day."}</p> : (
              <table className="table">
                <thead><tr><th>{ar ? "الوقت" : "Time"}</th><th>{ar ? "المريض" : "Patient"}</th><th>{ar ? "الإجراء" : "Procedure"}</th><th>{ar ? "الحالة" : "Status"}</th><th>{ar ? "التخدير" : "Anaesthesia"}</th><th></th></tr></thead>
                <tbody>{rb.cases.map(caseRow)}</tbody>
              </table>
            )}
          </div>
        ))}
        {board && board.unassigned.length > 0 && (
          <div className="l-mt">
            <h3>{ar ? "حالات بلا غرفة" : "Unassigned cases"}</h3>
            <table className="table"><tbody>{board.unassigned.map(caseRow)}</tbody></table>
          </div>
        )}
      </div>

      <div className="card">
        <h2>{ar ? "قائمة الانتظار الجراحية" : "Surgical waiting list"}</h2>
        {waitlist.length === 0 ? <p className="muted">{ar ? "القائمة فارغة." : "The list is empty."}</p> : (
          <table className="table">
            <thead><tr><th>{ar ? "المريض" : "Patient"}</th><th>{ar ? "الإجراء" : "Procedure"}</th><th>{ar ? "الأولوية" : "Priority"}</th><th>{ar ? "أيام الانتظار" : "Days waiting"}</th><th></th></tr></thead>
            <tbody>
              {waitlist.map((w) => (
                <tr key={w.entry.id}>
                  <td>{w.patientName}</td>
                  <td>{w.entry.procedureName}</td>
                  <td>{prioBadge(w.entry.priority)}</td>
                  <td>{w.waitingDays}</td>
                  <td><button type="button" className="btn btn-small" onClick={() => { setTarget(w); setDrawer("schedule"); }}>{ar ? "جدولة" : "Schedule"}</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {drawer && (
        <div className="drawer-overlay" onClick={() => setDrawer(null)}>
          <div className="drawer" onClick={(e) => e.stopPropagation()}>
            {drawer === "room" && (
              <form onSubmit={createRoom}>
                <h2>{ar ? "غرفة عمليات جديدة" : "New operating room"}</h2>
                <div className="field"><label>{ar ? "الاسم" : "Name"}</label><input required value={roomForm.nameAr} onChange={(e) => setRoomForm({ ...roomForm, nameAr: e.target.value })} placeholder={ar ? "غرفة عمليات ١" : "OR 1"} /></div>
                <div className="field"><label>{ar ? "التخصص" : "Specialty"}</label><input value={roomForm.specialty} onChange={(e) => setRoomForm({ ...roomForm, specialty: e.target.value })} /></div>
                <button type="submit" className="btn" disabled={busy}>{ar ? "إنشاء" : "Create"}</button>
              </form>
            )}
            {drawer === "waitlist" && (
              <form onSubmit={addWaitlist}>
                <h2>{ar ? "إضافة لقائمة الانتظار" : "Add to waiting list"}</h2>
                <div className="field"><label>{ar ? "المريض (بحث في السجل الموحد)" : "Patient (MPI search)"}</label>
                  <input value={patientQuery} onChange={(e) => searchPatients(e.target.value)} placeholder={ar ? "الاسم أو الرقم القومي" : "Name or national ID"} />
                  <div className="picker-list">
                    {patientResults.map((pt) => (
                      <button type="button" key={pt.id} className={`picker-item ${picked?.id === pt.id ? "picked" : ""}`} onClick={() => setPicked(pt)}>
                        {pt.fullNameAr} · {pt.nationalId}
                      </button>
                    ))}
                  </div>
                </div>
                <div className="field"><label>{ar ? "الإجراء" : "Procedure"}</label><input required value={waitForm.procedureName} onChange={(e) => setWaitForm({ ...waitForm, procedureName: e.target.value })} /></div>
                <div className="field"><label>{ar ? "الأولوية" : "Priority"}</label>
                  <select value={waitForm.priority} onChange={(e) => setWaitForm({ ...waitForm, priority: e.target.value })}>
                    <option value="elective">{ar ? "اختيارية" : "Elective"}</option>
                    <option value="urgent">{ar ? "عاجلة" : "Urgent"}</option>
                    <option value="emergency">{ar ? "طارئة" : "Emergency"}</option>
                  </select></div>
                <div className="field"><label>{ar ? "ملاحظات" : "Notes"}</label><input value={waitForm.notes} onChange={(e) => setWaitForm({ ...waitForm, notes: e.target.value })} /></div>
                <button type="submit" className="btn" disabled={busy || !picked}>{ar ? "إضافة" : "Add"}</button>
              </form>
            )}
            {drawer === "schedule" && target && "entry" in target && (
              <form onSubmit={schedule}>
                <h2>{ar ? "جدولة" : "Schedule"}: {(target as WaitRow).entry.procedureName}</h2>
                <div className="field"><label>{ar ? "الغرفة" : "Room"}</label>
                  <select required value={schedForm.roomId} onChange={(e) => setSchedForm({ ...schedForm, roomId: e.target.value })}>
                    <option value="">{ar ? "اختر الغرفة" : "Pick a room"}</option>
                    {board?.rooms.filter((r) => r.room.active).map((r) => <option key={r.room.id} value={r.room.id}>{r.room.nameAr}</option>)}
                  </select></div>
                <div className="field"><label>{ar ? "الوقت" : "Time"}</label><input type="time" required value={schedForm.time} onChange={(e) => setSchedForm({ ...schedForm, time: e.target.value })} /></div>
                <p className="muted">{ar ? `التاريخ: ${date} — تُرفض الجدولة عند تعارض الغرفة ضمن 60 دقيقة من حالة أخرى.` : `Date: ${date} — scheduling is refused on a room conflict within 60 minutes of another case.`}</p>
                <button type="submit" className="btn" disabled={busy}>{ar ? "جدولة" : "Schedule"}</button>
              </form>
            )}
            {drawer === "anaesthesia" && target && "case" in target && (
              <form onSubmit={saveAnaesthesia}>
                <h2>{ar ? "سجل التخدير" : "Anaesthesia record"}: {(target as CaseView).case.procedureName}</h2>
                <div className="field"><label>ASA</label>
                  <select value={anaForm.asaClass} onChange={(e) => setAnaForm({ ...anaForm, asaClass: e.target.value })}>
                    <option value="">—</option><option>1</option><option>2</option><option>3</option><option>4</option><option>5</option>
                  </select></div>
                <div className="field"><label>{ar ? "نوع التخدير" : "Type"}</label>
                  <select value={anaForm.anaesthesiaType} onChange={(e) => setAnaForm({ ...anaForm, anaesthesiaType: e.target.value })}>
                    <option value="general">{ar ? "كلي" : "General"}</option>
                    <option value="spinal">{ar ? "نصفي (شوكي)" : "Spinal"}</option>
                    <option value="epidural">{ar ? "فوق الجافية" : "Epidural"}</option>
                    <option value="local">{ar ? "موضعي" : "Local"}</option>
                    <option value="sedation">{ar ? "تهدئة" : "Sedation"}</option>
                  </select></div>
                <div className="field"><label>{ar ? "طبيب التخدير" : "Anaesthetist"}</label><input value={anaForm.anaesthetist} onChange={(e) => setAnaForm({ ...anaForm, anaesthetist: e.target.value })} /></div>
                <div className="field"><label>{ar ? "اكتمل تقييم ما قبل الجراحة" : "Pre-operative assessment completed"}</label>
                  <input type="checkbox" checked={anaForm.preopAssessment} onChange={(e) => setAnaForm({ ...anaForm, preopAssessment: e.target.checked })} /></div>
                <div className="field"><label>{ar ? "مضاعفات" : "Complications"}</label><input value={anaForm.complications} onChange={(e) => setAnaForm({ ...anaForm, complications: e.target.value })} /></div>
                <div className="field"><label>{ar ? "ملاحظات" : "Notes"}</label><input value={anaForm.notes} onChange={(e) => setAnaForm({ ...anaForm, notes: e.target.value })} /></div>
                <button type="submit" className="btn" disabled={busy}>{ar ? "حفظ" : "Save"}</button>
              </form>
            )}
            {drawer === "postpone" && target && "case" in target && (
              <div>
                <h2>{ar ? "تأجيل الحالة" : "Postpone case"}</h2>
                <div className="field"><label>{ar ? "السبب (إلزامي وموثق)" : "Reason (mandatory, documented)"}</label>
                  <input value={reason} onChange={(e) => setReason(e.target.value)} /></div>
                <button type="button" className="btn" disabled={!reason.trim()} onClick={() => caseAction(target as CaseView, "postpone", reason)}>{ar ? "تأجيل" : "Postpone"}</button>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
