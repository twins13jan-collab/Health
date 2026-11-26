"use client";

import { useCallback, useEffect, useState } from "react";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";
import type { Patient } from "@/lib/types";

interface Clinic { id: string; nameAr: string; nameEn?: string; specialty?: string; room?: string; active: boolean }
interface Session {
  id: string; clinicId: string; doctorName: string; sessionDate: string;
  startsAt: string; endsAt: string; slotMinutes: number; capacity: number; status: string;
}
interface SessionRow { session: Session; clinicNameAr: string; booked: number; waiting: number; slots: number }
interface Appt { id: string; patientId: string; startsAt: string; endsAt: string; status: string; cancelReason?: string }
interface QueueRow { appointment: Appt; patientName: string }
interface WaitRow { entry: { id: string; patientId: string; reason?: string; priority: string; createdAt: string }; patientName: string }
interface Board { session: Session; clinicNameAr: string; booked: number; queue: QueueRow[]; waitlist: WaitRow[] }

const STATUS_AR: Record<string, string> = {
  scheduled: "مجدولة", open: "مفتوحة", closed: "مغلقة", cancelled: "ملغاة",
  booked: "محجوز", confirmed: "مؤكد", attended: "حضر", no_show: "لم يحضر",
};

/** Outpatient clinics & scheduling: clinics, dated doctor sessions on
 *  a slot grid, booking into the next free slot, the live session
 *  queue (check-in / no-show), and the waiting list with automatic
 *  promotion when a booking is cancelled. */
export default function ClinicsPage() {
  const { lang } = useLang();
  const ar = lang === "ar";
  const today = new Date().toISOString().slice(0, 10);
  const [date, setDate] = useState(today);
  const [clinics, setClinics] = useState<Clinic[]>([]);
  const [sessions, setSessions] = useState<SessionRow[]>([]);
  const [board, setBoard] = useState<Board | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [toast, setToast] = useState<string | null>(null);
  const [drawer, setDrawer] = useState<null | "book" | "wait" | "clinic" | "session" | "cancel">(null);
  const [cancelTarget, setCancelTarget] = useState<QueueRow | null>(null);
  const [cancelReason, setCancelReason] = useState("");
  const [patientQuery, setPatientQuery] = useState("");
  const [patientResults, setPatientResults] = useState<Patient[]>([]);
  const [picked, setPicked] = useState<Patient | null>(null);
  const [waitForm, setWaitForm] = useState({ priority: "normal", reason: "" });
  const [clinicForm, setClinicForm] = useState({ nameAr: "", specialty: "", room: "" });
  const [sessionForm, setSessionForm] = useState({ clinicId: "", doctorName: "", sessionDate: today, startsAt: "09:00", endsAt: "12:00", slotMinutes: "15", capacity: "12" });

  const loadClinics = useCallback(() => {
    api.getJson<{ clinics: Clinic[] }>("/api/clinics").then((r) => setClinics(r.clinics)).catch(() => {});
  }, []);
  const loadSessions = useCallback(() => {
    api.getJson<{ sessions: SessionRow[] }>(`/api/clinics/sessions?date=${date}`)
      .then((r) => { setSessions(r.sessions); setError(null); })
      .catch((e) => setError(e instanceof Error ? e.message : String(e)));
  }, [date]);
  const loadBoard = useCallback((id: string) => {
    api.getJson<Board>(`/api/clinics/sessions/${id}/board`).then(setBoard).catch((e) => setError(e instanceof Error ? e.message : String(e)));
  }, []);
  useEffect(() => { loadClinics(); }, [loadClinics]);
  useEffect(() => { loadSessions(); }, [loadSessions]);
  useEffect(() => { if (!toast) return; const id = setTimeout(() => setToast(null), 4500); return () => clearTimeout(id); }, [toast]);

  async function searchPatients(q: string) {
    setPatientQuery(q);
    if (q.trim().length < 2) { setPatientResults([]); return; }
    try { const r = await api.patients(q); setPatientResults(r.data.slice(0, 8)); } catch { setPatientResults([]); }
  }
  function refreshBoard() { if (board) loadBoard(board.session.id); loadSessions(); }

  async function book() {
    if (!board || !picked) return;
    try {
      const r = await api.postJson<{ slot: number }>(`/api/clinics/sessions/${board.session.id}/book`, { patientId: picked.id });
      setToast(ar ? `تم الحجز — خانة رقم ${r.slot}` : `Booked — slot #${r.slot}`);
      setDrawer(null); setPicked(null); setPatientQuery(""); refreshBoard();
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); }
  }
  async function joinWaitlist() {
    if (!board || !picked) return;
    try {
      await api.postJson(`/api/clinics/sessions/${board.session.id}/waitlist`, { patientId: picked.id, priority: waitForm.priority, reason: waitForm.reason || undefined });
      setToast(ar ? "أُضيف لقائمة الانتظار" : "Added to the waiting list");
      setDrawer(null); setPicked(null); refreshBoard();
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); }
  }
  async function apptStatus(a: Appt, status: string) {
    try { await api.postJson(`/api/appointments/${a.id}/status`, { status }); refreshBoard(); }
    catch (e) { setError(e instanceof Error ? e.message : String(e)); }
  }
  async function cancelBooking() {
    if (!cancelTarget) return;
    try {
      const r = await api.postJson<{ promoted: { patientName: string } | null }>(`/api/clinics/appointments/${cancelTarget.appointment.id}/cancel`, { reason: cancelReason });
      setToast(r.promoted
        ? (ar ? `أُلغي الحجز ورُقّي من الانتظار: ${r.promoted.patientName}` : `Cancelled; promoted from waitlist: ${r.promoted.patientName}`)
        : (ar ? "أُلغي الحجز" : "Booking cancelled"));
      setDrawer(null); setCancelTarget(null); setCancelReason(""); refreshBoard();
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); }
  }
  async function sessionStatus(s: Session, status: string) {
    try { await api.postJson(`/api/clinics/sessions/${s.id}/status`, { status }); loadSessions(); if (board?.session.id === s.id) loadBoard(s.id); }
    catch (e) { setError(e instanceof Error ? e.message : String(e)); }
  }
  async function createClinic() {
    try {
      await api.postJson("/api/clinics", clinicForm);
      setToast(ar ? "أُنشئت العيادة" : "Clinic created"); setDrawer(null); setClinicForm({ nameAr: "", specialty: "", room: "" }); loadClinics();
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); }
  }
  async function createSession() {
    try {
      await api.postJson("/api/clinics/sessions", {
        clinicId: sessionForm.clinicId, doctorName: sessionForm.doctorName, sessionDate: sessionForm.sessionDate,
        startsAt: sessionForm.startsAt, endsAt: sessionForm.endsAt,
        slotMinutes: Number(sessionForm.slotMinutes), capacity: Number(sessionForm.capacity),
      });
      setToast(ar ? "أُنشئت الجلسة" : "Session created"); setDrawer(null); loadSessions();
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); }
  }

  const patientPicker = (
    <div className="field">
      <label>{ar ? "المريض (بحث بالاسم أو الرقم القومي)" : "Patient (search by name or national ID)"}</label>
      <input value={patientQuery} onChange={(e) => searchPatients(e.target.value)} placeholder={ar ? "اكتب حرفين على الأقل" : "Type at least 2 characters"} />
      {patientResults.length > 0 && (
        <div className="picker-list">
          {patientResults.map((p) => (
            <button key={p.id} type="button" className={`picker-item ${picked?.id === p.id ? "picked" : ""}`} onClick={() => setPicked(p)}>
              {p.fullNameAr} · {p.nationalId}
            </button>
          ))}
        </div>
      )}
      {picked && <p className="muted">{ar ? "المختار" : "Selected"}: <strong>{picked.fullNameAr}</strong></p>}
    </div>
  );

  return (
    <div>
      <div className="page-head">
        <h1>{ar ? "العيادات الخارجية والجدولة" : "Outpatient Clinics & Scheduling"}</h1>
        <p>{ar ? "عيادات وجلسات أطباء بخانات زمنية وسعة محددة، حجز في أول خانة شاغرة، وقائمة انتظار تُرقّى تلقائيًا عند الإلغاء" : "Clinics and doctor sessions on a slot grid with capacity, booking into the next free slot, and a waiting list that auto-promotes on cancellation"}</p>
      </div>
      {error && <div className="error-banner">{error} <button type="button" className="btn btn-small" onClick={() => { loadSessions(); setError(null); }}>{ar ? "إعادة المحاولة" : "Retry"}</button></div>}
      {toast && <div className="toast">{toast}</div>}

      <div className="card l-row">
        <div className="field" style={{ margin: 0 }}>
          <label>{ar ? "تاريخ الجلسات" : "Session date"}</label>
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
        </div>
        <button type="button" className="btn" onClick={() => { setSessionForm((f) => ({ ...f, clinicId: clinics[0]?.id ?? "" })); setDrawer("session"); }}>{ar ? "+ جلسة جديدة" : "+ New session"}</button>
        <button type="button" className="btn btn-secondary" onClick={() => setDrawer("clinic")}>{ar ? "+ عيادة جديدة" : "+ New clinic"}</button>
        <span className="muted">{clinics.filter((c) => c.active).length} {ar ? "عيادة عاملة" : "active clinics"} · {sessions.length} {ar ? "جلسة في هذا اليوم" : "sessions this day"}</span>
      </div>

      <div className="charts-grid cards-grid">
        {sessions.map(({ session: s, clinicNameAr, booked, waiting }) => (
          <div key={s.id} className="card">
            <div className="l-row-between">
              <h3 style={{ margin: 0 }}>{clinicNameAr}</h3>
              <span className={`badge ${s.status === "open" ? "ok" : s.status === "cancelled" ? "danger" : "muted"}`}>{STATUS_AR[s.status] ?? s.status}</span>
            </div>
            <p className="muted" style={{ margin: "6px 0" }}>👨‍⚕️ {s.doctorName} · 🕘 <span dir="ltr">{s.startsAt}–{s.endsAt}</span> · {s.slotMinutes} {ar ? "دقيقة/خانة" : "min/slot"}</p>
            <div className="cap-track"><div className="cap-fill" style={{ width: `${s.capacity ? Math.min(100, (booked / s.capacity) * 100) : 0}%` }} /></div>
            <p style={{ margin: "8px 0" }}>{ar ? "محجوز" : "Booked"}: <strong>{booked}/{s.capacity}</strong>{waiting > 0 ? <> · {ar ? "في الانتظار" : "Waiting"}: <strong>{waiting}</strong></> : null}</p>
            <div className="l-row">
              <button type="button" className="btn btn-small" onClick={() => loadBoard(s.id)}>{ar ? "لوحة الجلسة" : "Session board"}</button>
              {s.status !== "open" && s.status !== "cancelled" && <button type="button" className="btn btn-small btn-secondary" onClick={() => sessionStatus(s, "open")}>{ar ? "فتح" : "Open"}</button>}
              {s.status === "open" && <button type="button" className="btn btn-small btn-secondary" onClick={() => sessionStatus(s, "closed")}>{ar ? "إغلاق" : "Close"}</button>}
            </div>
          </div>
        ))}
        {sessions.length === 0 && <div className="card muted">{ar ? "لا جلسات في هذا التاريخ — أنشئ جلسة جديدة" : "No sessions on this date — create one"}</div>}
      </div>

      {board && (
        <div className="card">
          <h3 style={{ marginTop: 0 }}>{ar ? "لوحة الجلسة" : "Session board"} — {board.clinicNameAr} · {board.session.doctorName} · <span dir="ltr">{board.session.startsAt}–{board.session.endsAt}</span></h3>
          <div style={{ display: "flex", gap: 8, marginBottom: 12, flexWrap: "wrap" }}>
            <button type="button" className="btn btn-small" onClick={() => { setPicked(null); setPatientQuery(""); setDrawer("book"); }}>{ar ? "حجز مريض" : "Book patient"}</button>
            <button type="button" className="btn btn-small btn-secondary" onClick={() => { setPicked(null); setPatientQuery(""); setDrawer("wait"); }}>{ar ? "إضافة لقائمة الانتظار" : "Add to waitlist"}</button>
          </div>
          <div className="table-wrap" style={{ boxShadow: "none", border: "none" }}>
            <table className="data">
              <thead><tr><th>#</th><th>{ar ? "الوقت" : "Time"}</th><th>{ar ? "المريض" : "Patient"}</th><th>{ar ? "الحالة" : "Status"}</th><th>{ar ? "إجراءات" : "Actions"}</th></tr></thead>
              <tbody>
                {board.queue.map((row, i) => (
                  <tr key={row.appointment.id} style={row.appointment.status === "cancelled" ? { opacity: 0.55 } : undefined}>
                    <td>{i + 1}</td>
                    <td dir="ltr">{new Date(row.appointment.startsAt).toLocaleTimeString(ar ? "ar-EG" : "en-GB", { hour: "2-digit", minute: "2-digit" })}</td>
                    <td><strong>{row.patientName}</strong>{row.appointment.cancelReason ? <div className="muted" style={{ fontSize: 12 }}>{row.appointment.cancelReason}</div> : null}</td>
                    <td><span className={`badge ${row.appointment.status === "attended" ? "ok" : row.appointment.status === "cancelled" ? "danger" : row.appointment.status === "no_show" ? "warn" : "muted"}`}>{STATUS_AR[row.appointment.status] ?? row.appointment.status}</span></td>
                    <td>
                      <span className="l-actions">
                        {row.appointment.status === "booked" || row.appointment.status === "confirmed" ? <>
                          <button type="button" className="btn btn-small" onClick={() => apptStatus(row.appointment, "attended")}>{ar ? "حضر" : "Attended"}</button>
                          <button type="button" className="btn btn-small btn-secondary" onClick={() => apptStatus(row.appointment, "no_show")}>{ar ? "لم يحضر" : "No-show"}</button>
                          <button type="button" className="btn btn-small btn-danger" onClick={() => { setCancelTarget(row); setDrawer("cancel"); }}>{ar ? "إلغاء" : "Cancel"}</button>
                        </> : null}
                      </span>
                    </td>
                  </tr>
                ))}
                {board.queue.length === 0 && <tr><td colSpan={5} className="muted">{ar ? "لا حجوزات بعد" : "No bookings yet"}</td></tr>}
              </tbody>
            </table>
          </div>
          {board.waitlist.length > 0 && <>
            <h4>{ar ? "قائمة الانتظار (بترتيب الترقية: العاجل أولًا ثم الأقدم)" : "Waiting list (promotion order: urgent first, then arrival)"}</h4>
            <div className="table-wrap" style={{ boxShadow: "none", border: "none" }}>
              <table className="data">
                <thead><tr><th>#</th><th>{ar ? "المريض" : "Patient"}</th><th>{ar ? "الأولوية" : "Priority"}</th><th>{ar ? "السبب" : "Reason"}</th><th></th></tr></thead>
                <tbody>
                  {board.waitlist.map((w, i) => (
                    <tr key={w.entry.id}>
                      <td>{i + 1}</td><td><strong>{w.patientName}</strong></td>
                      <td>{w.entry.priority === "urgent" ? <span className="badge danger">{ar ? "عاجل" : "Urgent"}</span> : <span className="badge muted">{ar ? "عادي" : "Normal"}</span>}</td>
                      <td>{w.entry.reason ?? "—"}</td>
                      <td><button type="button" className="btn btn-small btn-secondary" onClick={async () => { await api.postJson(`/api/clinics/waitlist/${w.entry.id}/cancel`, {}); refreshBoard(); }}>{ar ? "إزالة" : "Remove"}</button></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </>}
        </div>
      )}

      {drawer && (
        <div className="drawer-overlay" onClick={() => setDrawer(null)}>
          <div className="drawer" onClick={(e) => e.stopPropagation()}>
            {drawer === "book" && <>
              <h2>{ar ? "حجز مريض في الجلسة" : "Book a patient"}</h2>
              {patientPicker}
              <button type="button" className="btn" disabled={!picked} onClick={book}>{ar ? "تأكيد الحجز" : "Confirm booking"}</button>
            </>}
            {drawer === "wait" && <>
              <h2>{ar ? "إضافة لقائمة الانتظار" : "Add to waiting list"}</h2>
              {patientPicker}
              <div className="field"><label>{ar ? "الأولوية" : "Priority"}</label>
                <select value={waitForm.priority} onChange={(e) => setWaitForm({ ...waitForm, priority: e.target.value })}>
                  <option value="normal">{ar ? "عادي" : "Normal"}</option>
                  <option value="urgent">{ar ? "عاجل" : "Urgent"}</option>
                </select></div>
              <div className="field"><label>{ar ? "السبب" : "Reason"}</label>
                <input value={waitForm.reason} onChange={(e) => setWaitForm({ ...waitForm, reason: e.target.value })} /></div>
              <button type="button" className="btn" disabled={!picked} onClick={joinWaitlist}>{ar ? "إضافة" : "Add"}</button>
            </>}
            {drawer === "cancel" && <>
              <h2>{ar ? "إلغاء الحجز" : "Cancel booking"}</h2>
              <p>{cancelTarget?.patientName} — {ar ? "سبب الإلغاء مطلوب، وسيُرقّى أول منتظر تلقائيًا إلى الخانة المحررة" : "A reason is required; the first waiting patient is promoted into the freed slot automatically"}</p>
              <div className="field"><label>{ar ? "سبب الإلغاء" : "Cancellation reason"}</label>
                <input value={cancelReason} onChange={(e) => setCancelReason(e.target.value)} /></div>
              <button type="button" className="btn btn-danger" disabled={!cancelReason.trim()} onClick={cancelBooking}>{ar ? "تأكيد الإلغاء" : "Confirm cancellation"}</button>
            </>}
            {drawer === "clinic" && <>
              <h2>{ar ? "عيادة جديدة" : "New clinic"}</h2>
              <div className="field"><label>{ar ? "اسم العيادة" : "Clinic name"}</label>
                <input value={clinicForm.nameAr} onChange={(e) => setClinicForm({ ...clinicForm, nameAr: e.target.value })} /></div>
              <div className="field"><label>{ar ? "التخصص" : "Specialty"}</label>
                <input value={clinicForm.specialty} onChange={(e) => setClinicForm({ ...clinicForm, specialty: e.target.value })} /></div>
              <div className="field"><label>{ar ? "الغرفة" : "Room"}</label>
                <input value={clinicForm.room} onChange={(e) => setClinicForm({ ...clinicForm, room: e.target.value })} /></div>
              <button type="button" className="btn" disabled={!clinicForm.nameAr.trim()} onClick={createClinic}>{ar ? "إنشاء" : "Create"}</button>
            </>}
            {drawer === "session" && <>
              <h2>{ar ? "جلسة جديدة" : "New session"}</h2>
              <div className="field"><label>{ar ? "العيادة" : "Clinic"}</label>
                <select value={sessionForm.clinicId} onChange={(e) => setSessionForm({ ...sessionForm, clinicId: e.target.value })}>
                  <option value="">—</option>
                  {clinics.filter((c) => c.active).map((c) => <option key={c.id} value={c.id}>{c.nameAr}</option>)}
                </select></div>
              <div className="field"><label>{ar ? "الطبيب" : "Doctor"}</label>
                <input value={sessionForm.doctorName} onChange={(e) => setSessionForm({ ...sessionForm, doctorName: e.target.value })} /></div>
              <div className="field"><label>{ar ? "التاريخ" : "Date"}</label>
                <input type="date" value={sessionForm.sessionDate} onChange={(e) => setSessionForm({ ...sessionForm, sessionDate: e.target.value })} /></div>
              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 10 }}>
                <div className="field"><label>{ar ? "من" : "From"}</label>
                  <input type="time" value={sessionForm.startsAt} onChange={(e) => setSessionForm({ ...sessionForm, startsAt: e.target.value })} /></div>
                <div className="field"><label>{ar ? "إلى" : "To"}</label>
                  <input type="time" value={sessionForm.endsAt} onChange={(e) => setSessionForm({ ...sessionForm, endsAt: e.target.value })} /></div>
                <div className="field"><label>{ar ? "دقائق الخانة" : "Slot minutes"}</label>
                  <input type="number" value={sessionForm.slotMinutes} onChange={(e) => setSessionForm({ ...sessionForm, slotMinutes: e.target.value })} /></div>
                <div className="field"><label>{ar ? "السعة" : "Capacity"}</label>
                  <input type="number" value={sessionForm.capacity} onChange={(e) => setSessionForm({ ...sessionForm, capacity: e.target.value })} /></div>
              </div>
              <button type="button" className="btn" disabled={!sessionForm.clinicId || !sessionForm.doctorName.trim()} onClick={createSession}>{ar ? "إنشاء الجلسة" : "Create session"}</button>
            </>}
          </div>
        </div>
      )}
    </div>
  );
}
