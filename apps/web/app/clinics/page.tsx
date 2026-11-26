"use client";

import { useCallback, useEffect, useState } from "react";
import { Button, Drawer, PageHead, ProgressBar, Toasts } from "@/components/ui";
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
        <div className="picker">
          {patientResults.map((p) => (
            <button key={p.id} type="button" className={`picker-item${picked?.id === p.id ? " picked" : ""}`} onClick={() => setPicked(p)}>
              {p.fullNameAr} · {p.nationalId}
            </button>
          ))}
        </div>
      )}
      {picked && <p className="picked-note">✓ {picked.fullNameAr}</p>}
    </div>
  );

  return (
    <>
      <PageHead
        eyebrow={ar ? "العيادات" : "Outpatient"}
        title={ar ? "العيادات الخارجية والجدولة" : "Outpatient Clinics & Scheduling"}
        sub={ar ? "عيادات وجلسات أطباء بخانات زمنية وسعة محددة، حجز في أول خانة شاغرة، وقائمة انتظار تُرقّى تلقائيًا عند الإلغاء" : "Clinics and doctor sessions on a slot grid with capacity, booking into the next free slot, and a waiting list that auto-promotes on cancellation"}
        actions={
          <>
            <Button onClick={() => { setSessionForm((f) => ({ ...f, clinicId: clinics[0]?.id ?? "" })); setDrawer("session"); }}>{ar ? "+ جلسة جديدة" : "+ New session"}</Button>
            <Button variant="soft" onClick={() => setDrawer("clinic")}>{ar ? "+ عيادة جديدة" : "+ New clinic"}</Button>
          </>
        }
      />
      {error && <div className="alert-error">{error} <Button variant="soft" small onClick={() => { loadSessions(); setError(null); }}>{ar ? "إعادة المحاولة" : "Retry"}</Button></div>}
      <Toasts items={toast ? [{ text: toast }] : []} />

      <div className="toolbar">
        <div className="field">
          <label>{ar ? "تاريخ الجلسات" : "Session date"}</label>
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
        </div>
        <span className="count-note">{clinics.filter((c) => c.active).length} {ar ? "عيادة عاملة" : "active clinics"} · {sessions.length} {ar ? "جلسة في هذا اليوم" : "sessions this day"}</span>
      </div>

      <div className="cards-grid">
        {sessions.map(({ session: s, clinicNameAr, booked, waiting }) => (
          <div key={s.id} className="ward-card">
            <div className="wc-head">
              <strong>{clinicNameAr}</strong>
              <span className={`badge ${s.status === "open" ? "ok" : s.status === "cancelled" ? "danger" : ""}`}>{STATUS_AR[s.status] ?? s.status}</span>
            </div>
            <p className="muted">👨‍⚕️ {s.doctorName} · 🕘 <span dir="ltr">{s.startsAt}–{s.endsAt}</span> · {s.slotMinutes} {ar ? "دقيقة/خانة" : "min/slot"}</p>
            <div className="mt mb">
              <ProgressBar pct={s.capacity ? (booked / s.capacity) * 100 : 0} />
            </div>
            <p>{ar ? "محجوز" : "Booked"}: <strong>{booked}/{s.capacity}</strong>{waiting > 0 ? <> · {ar ? "في الانتظار" : "Waiting"}: <strong>{waiting}</strong></> : null}</p>
            <div className="row mt">
              <Button small onClick={() => loadBoard(s.id)}>{ar ? "لوحة الجلسة" : "Session board"}</Button>
              {s.status !== "open" && s.status !== "cancelled" && <Button small variant="soft" onClick={() => sessionStatus(s, "open")}>{ar ? "فتح" : "Open"}</Button>}
              {s.status === "open" && <Button small variant="soft" onClick={() => sessionStatus(s, "closed")}>{ar ? "إغلاق" : "Close"}</Button>}
            </div>
          </div>
        ))}
        {sessions.length === 0 && <div className="empty">{ar ? "لا جلسات في هذا التاريخ — أنشئ جلسة جديدة" : "No sessions on this date — create one"}</div>}
      </div>

      {board && (
        <div className="card mt">
          <h2>{ar ? "لوحة الجلسة" : "Session board"} — {board.clinicNameAr} · {board.session.doctorName} · <span dir="ltr">{board.session.startsAt}–{board.session.endsAt}</span></h2>
          <div className="row mb">
            <Button small onClick={() => { setPicked(null); setPatientQuery(""); setDrawer("book"); }}>{ar ? "حجز مريض" : "Book patient"}</Button>
            <Button small variant="soft" onClick={() => { setPicked(null); setPatientQuery(""); setDrawer("wait"); }}>{ar ? "إضافة لقائمة الانتظار" : "Add to waitlist"}</Button>
          </div>
          <div className="tbl-wrap">
            <table className="tbl">
              <thead><tr><th>#</th><th>{ar ? "الوقت" : "Time"}</th><th>{ar ? "المريض" : "Patient"}</th><th>{ar ? "الحالة" : "Status"}</th><th>{ar ? "إجراءات" : "Actions"}</th></tr></thead>
              <tbody>
                {board.queue.map((row, i) => (
                  <tr key={row.appointment.id} className={row.appointment.status === "cancelled" ? "dim" : undefined}>
                    <td>{i + 1}</td>
                    <td dir="ltr">{new Date(row.appointment.startsAt).toLocaleTimeString(ar ? "ar-EG" : "en-GB", { hour: "2-digit", minute: "2-digit" })}</td>
                    <td><strong>{row.patientName}</strong>{row.appointment.cancelReason ? <div className="muted small">{row.appointment.cancelReason}</div> : null}</td>
                    <td><span className={`badge ${row.appointment.status === "attended" ? "ok" : row.appointment.status === "cancelled" ? "danger" : row.appointment.status === "no_show" ? "warn" : ""}`}>{STATUS_AR[row.appointment.status] ?? row.appointment.status}</span></td>
                    <td>
                      <span className="row">
                        {row.appointment.status === "booked" || row.appointment.status === "confirmed" ? <>
                          <Button small onClick={() => apptStatus(row.appointment, "attended")}>{ar ? "حضر" : "Attended"}</Button>
                          <Button small variant="soft" onClick={() => apptStatus(row.appointment, "no_show")}>{ar ? "لم يحضر" : "No-show"}</Button>
                          <Button small variant="danger" onClick={() => { setCancelTarget(row); setDrawer("cancel"); }}>{ar ? "إلغاء" : "Cancel"}</Button>
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
            <h3>{ar ? "قائمة الانتظار (بترتيب الترقية: العاجل أولًا ثم الأقدم)" : "Waiting list (promotion order: urgent first, then arrival)"}</h3>
            <div className="tbl-wrap">
              <table className="tbl">
                <thead><tr><th>#</th><th>{ar ? "المريض" : "Patient"}</th><th>{ar ? "الأولوية" : "Priority"}</th><th>{ar ? "السبب" : "Reason"}</th><th></th></tr></thead>
                <tbody>
                  {board.waitlist.map((w, i) => (
                    <tr key={w.entry.id}>
                      <td>{i + 1}</td><td><strong>{w.patientName}</strong></td>
                      <td>{w.entry.priority === "urgent" ? <span className="badge danger">{ar ? "عاجل" : "Urgent"}</span> : <span className="badge">{ar ? "عادي" : "Normal"}</span>}</td>
                      <td>{w.entry.reason ?? "—"}</td>
                      <td><Button small variant="soft" onClick={async () => { await api.postJson(`/api/clinics/waitlist/${w.entry.id}/cancel`, {}); refreshBoard(); }}>{ar ? "إزالة" : "Remove"}</Button></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </>}
        </div>
      )}

      {drawer && (
        <Drawer
          title={drawer === "book" ? (ar ? "حجز مريض في الجلسة" : "Book a patient")
            : drawer === "wait" ? (ar ? "إضافة لقائمة الانتظار" : "Add to waiting list")
            : drawer === "cancel" ? (ar ? "إلغاء الحجز" : "Cancel booking")
            : drawer === "clinic" ? (ar ? "عيادة جديدة" : "New clinic")
            : (ar ? "جلسة جديدة" : "New session")}
          onClose={() => setDrawer(null)}
        >
          {drawer === "book" && <>
            {patientPicker}
            <div className="form-actions">
              <Button disabled={!picked} onClick={book}>{ar ? "تأكيد الحجز" : "Confirm booking"}</Button>
            </div>
          </>}
          {drawer === "wait" && <>
            {patientPicker}
            <div className="field"><label>{ar ? "الأولوية" : "Priority"}</label>
              <select value={waitForm.priority} onChange={(e) => setWaitForm({ ...waitForm, priority: e.target.value })}>
                <option value="normal">{ar ? "عادي" : "Normal"}</option>
                <option value="urgent">{ar ? "عاجل" : "Urgent"}</option>
              </select></div>
            <div className="field"><label>{ar ? "السبب" : "Reason"}</label>
              <input value={waitForm.reason} onChange={(e) => setWaitForm({ ...waitForm, reason: e.target.value })} /></div>
            <div className="form-actions">
              <Button disabled={!picked} onClick={joinWaitlist}>{ar ? "إضافة" : "Add"}</Button>
            </div>
          </>}
          {drawer === "cancel" && <>
            <p className="mb">{cancelTarget?.patientName} — {ar ? "سبب الإلغاء مطلوب، وسيُرقّى أول منتظر تلقائيًا إلى الخانة المحررة" : "A reason is required; the first waiting patient is promoted into the freed slot automatically"}</p>
            <div className="field"><label>{ar ? "سبب الإلغاء" : "Cancellation reason"}</label>
              <input value={cancelReason} onChange={(e) => setCancelReason(e.target.value)} /></div>
            <div className="form-actions">
              <Button variant="danger" disabled={!cancelReason.trim()} onClick={cancelBooking}>{ar ? "تأكيد الإلغاء" : "Confirm cancellation"}</Button>
            </div>
          </>}
          {drawer === "clinic" && <>
            <div className="field"><label>{ar ? "اسم العيادة" : "Clinic name"}</label>
              <input value={clinicForm.nameAr} onChange={(e) => setClinicForm({ ...clinicForm, nameAr: e.target.value })} /></div>
            <div className="field"><label>{ar ? "التخصص" : "Specialty"}</label>
              <input value={clinicForm.specialty} onChange={(e) => setClinicForm({ ...clinicForm, specialty: e.target.value })} /></div>
            <div className="field"><label>{ar ? "الغرفة" : "Room"}</label>
              <input value={clinicForm.room} onChange={(e) => setClinicForm({ ...clinicForm, room: e.target.value })} /></div>
            <div className="form-actions">
              <Button disabled={!clinicForm.nameAr.trim()} onClick={createClinic}>{ar ? "إنشاء" : "Create"}</Button>
            </div>
          </>}
          {drawer === "session" && <>
            <div className="field"><label>{ar ? "العيادة" : "Clinic"}</label>
              <select value={sessionForm.clinicId} onChange={(e) => setSessionForm({ ...sessionForm, clinicId: e.target.value })}>
                <option value="">—</option>
                {clinics.filter((c) => c.active).map((c) => <option key={c.id} value={c.id}>{c.nameAr}</option>)}
              </select></div>
            <div className="field"><label>{ar ? "الطبيب" : "Doctor"}</label>
              <input value={sessionForm.doctorName} onChange={(e) => setSessionForm({ ...sessionForm, doctorName: e.target.value })} /></div>
            <div className="field"><label>{ar ? "التاريخ" : "Date"}</label>
              <input type="date" value={sessionForm.sessionDate} onChange={(e) => setSessionForm({ ...sessionForm, sessionDate: e.target.value })} /></div>
            <div className="form-grid">
              <div className="field"><label>{ar ? "من" : "From"}</label>
                <input type="time" value={sessionForm.startsAt} onChange={(e) => setSessionForm({ ...sessionForm, startsAt: e.target.value })} /></div>
              <div className="field"><label>{ar ? "إلى" : "To"}</label>
                <input type="time" value={sessionForm.endsAt} onChange={(e) => setSessionForm({ ...sessionForm, endsAt: e.target.value })} /></div>
              <div className="field"><label>{ar ? "دقائق الخانة" : "Slot minutes"}</label>
                <input type="number" value={sessionForm.slotMinutes} onChange={(e) => setSessionForm({ ...sessionForm, slotMinutes: e.target.value })} /></div>
              <div className="field"><label>{ar ? "السعة" : "Capacity"}</label>
                <input type="number" value={sessionForm.capacity} onChange={(e) => setSessionForm({ ...sessionForm, capacity: e.target.value })} /></div>
            </div>
            <div className="form-actions">
              <Button disabled={!sessionForm.clinicId || !sessionForm.doctorName.trim()} onClick={createSession}>{ar ? "إنشاء الجلسة" : "Create session"}</Button>
            </div>
          </>}
        </Drawer>
      )}
    </>
  );
}
