"use client";

import { useCallback, useEffect, useState } from "react";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";

interface MeInfo {
  user: { fullName?: string; email?: string; role?: string };
  patient?: { id: string; fullName: string; nationalId: string; phone?: string };
  totalAppointments?: number;
}
interface FacilitySvc { id: string; nameAr: string; type: string; governorate: string; services: string[] }
interface SessionView { session: { id: string; doctorName: string; startsAt: string; endsAt: string }; clinicName: string; booked: number; remaining: number }
interface Appointment { id: string; clinic?: string; doctorName?: string; startsAt: string; status: string }
interface ServiceRequest { id: string; serviceType: string; details?: string; status: string; staffNote?: string; createdAt: string }

const today = () => new Date().toISOString().slice(0, 10);

/** My Health — the patient/citizen dashboard: profile, clinic
 *  booking against live sessions, my appointments with reasoned
 *  cancellation, and service requests (lab / pharmacy / home
 *  visit) with their facility responses. */
export default function MyHealthPage() {
  const { lang } = useLang();
  const ar = lang === "ar";
  const [me, setMe] = useState<MeInfo | null>(null);
  const [facilities, setFacilities] = useState<FacilitySvc[]>([]);
  const [facilityId, setFacilityId] = useState("");
  const [date, setDate] = useState(today());
  const [sessions, setSessions] = useState<SessionView[]>([]);
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [requests, setRequests] = useState<ServiceRequest[]>([]);
  const [reqForm, setReqForm] = useState({ serviceType: "lab", details: "", preferredDate: "" });
  const [cancelFor, setCancelFor] = useState<Appointment | null>(null);
  const [reason, setReason] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [toast, setToast] = useState<string | null>(null);

  const showToast = (m: string) => { setToast(m); setTimeout(() => setToast(null), 3500); };

  const load = useCallback(async () => {
    try {
      const [meR, appts, reqs, facs] = await Promise.all([
        api.getJson<MeInfo>("/api/portal/me"),
        api.getJson<{ appointments: Appointment[] }>("/api/portal/my-appointments").catch(() => ({ appointments: [] })),
        api.getJson<{ requests: ServiceRequest[] }>("/api/portal/my-requests").catch(() => ({ requests: [] })),
        fetch("/api/public/services").then((r) => (r.ok ? r.json() : { facilities: [] })),
      ]);
      setMe(meR); setAppointments(appts.appointments); setRequests(reqs.requests);
      setFacilities((facs.facilities ?? []).filter((f: FacilitySvc) => f.services.includes("clinics") || f.services.includes("lab") || f.services.includes("pharmacy")));
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); }
  }, []);
  useEffect(() => { load(); }, [load]);

  const loadSessions = useCallback(async () => {
    if (!facilityId) { setSessions([]); return; }
    try {
      const r = await api.getJson<{ sessions: SessionView[] }>(`/api/portal/facilities/${facilityId}/sessions?date=${date}`);
      setSessions(r.sessions);
    } catch { setSessions([]); }
  }, [facilityId, date]);
  useEffect(() => { loadSessions(); }, [loadSessions]);

  async function book(sessionId: string) {
    try {
      await api.postJson("/api/portal/book", { sessionId });
      showToast(ar ? "حُجز موعدك بنجاح" : "Your appointment is booked");
      await Promise.all([load(), loadSessions()]);
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); }
  }

  async function cancel() {
    if (!cancelFor) return;
    try {
      await api.postJson(`/api/portal/appointments/${cancelFor.id}/cancel`, { reason });
      showToast(ar ? "أُلغي الموعد" : "Appointment cancelled");
      setCancelFor(null); setReason("");
      await Promise.all([load(), loadSessions()]);
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); }
  }

  async function sendRequest(e: React.FormEvent) {
    e.preventDefault();
    if (!facilityId) { setError(ar ? "اختر المنشأة أولًا" : "Pick a facility first"); return; }
    try {
      await api.postJson("/api/portal/service-requests", {
        facilityId, serviceType: reqForm.serviceType,
        details: reqForm.details || undefined,
        preferredDate: reqForm.preferredDate || undefined,
      });
      showToast(ar ? "أُرسل طلب الخدمة للمنشأة" : "Service request sent");
      setReqForm({ serviceType: "lab", details: "", preferredDate: "" });
      await load();
    } catch (err) { setError(err instanceof Error ? err.message : String(err)); }
  }

  const reqBadge = (s: string) =>
    s === "done" ? <span className="badge ok">{ar ? "تمت" : "Done"}</span>
    : s === "confirmed" ? <span className="badge">{ar ? "مؤكدة" : "Confirmed"}</span>
    : s === "cancelled" ? <span className="badge danger">{ar ? "ملغاة" : "Cancelled"}</span>
    : <span className="badge warn">{ar ? "قيد المراجعة" : "Requested"}</span>;

  return (
    <div>
      <div className="page-head">
        <h1>♥ {ar ? "حسابي الصحي" : "My Health"}</h1>
        <p>{ar ? "مواعيدك وحجوزاتك وطلبات خدماتك — كلها مرتبطة بسجلك الصحي الموحد." : "Your appointments, bookings and service requests — all linked to your unified health record."}</p>
      </div>
      {error && <div className="error-banner">{error} <button type="button" className="btn btn-small btn-secondary" onClick={() => setError(null)}>{ar ? "إغلاق" : "Dismiss"}</button></div>}
      {toast && <div className="toast">{toast}</div>}

      <div className="kpi-grid">
        <div className="kpi-card"><span className="kpi-value">{me?.patient?.fullName ?? me?.user?.fullName ?? "—"}</span><span className="kpi-label">{ar ? "الاسم" : "Name"}</span></div>
        <div className="kpi-card"><span className="kpi-value" dir="ltr">{me?.patient?.nationalId ?? "—"}</span><span className="kpi-label">{ar ? "الرقم القومي" : "National ID"}</span></div>
        <div className="kpi-card"><span className="kpi-value">{appointments.filter((a) => a.status === "booked").length}</span><span className="kpi-label">{ar ? "مواعيد قائمة" : "Upcoming appointments"}</span></div>
        <div className="kpi-card"><span className="kpi-value">{requests.filter((r) => r.status === "requested").length}</span><span className="kpi-label">{ar ? "طلبات بانتظار الرد" : "Requests awaiting reply"}</span></div>
      </div>

      <div className="card">
        <h2>{ar ? "احجز موعد عيادة أو اطلب خدمة" : "Book a clinic appointment or request a service"}</h2>
        <div className="l-row">
          <select value={facilityId} onChange={(e) => setFacilityId(e.target.value)} style={{ minWidth: 260 }}>
            <option value="">{ar ? "اختر المنشأة" : "Pick a facility"}</option>
            {facilities.map((f) => <option key={f.id} value={f.id}>{f.nameAr} — {f.governorate}</option>)}
          </select>
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} style={{ width: 170 }} />
        </div>
        {facilityId && (
          sessions.length === 0
            ? <p className="muted">{ar ? "لا جلسات عيادات مفتوحة في هذا اليوم بهذه المنشأة." : "No open clinic sessions at this facility on this day."}</p>
            : (
              <table className="table">
                <thead><tr><th>{ar ? "العيادة" : "Clinic"}</th><th>{ar ? "الطبيب" : "Doctor"}</th><th>{ar ? "الوقت" : "Time"}</th><th>{ar ? "المتبقي" : "Remaining"}</th><th></th></tr></thead>
                <tbody>
                  {sessions.map((sv) => (
                    <tr key={sv.session.id}>
                      <td>{sv.clinicName}</td><td>{sv.session.doctorName}</td>
                      <td dir="ltr">{sv.session.startsAt} – {sv.session.endsAt}</td>
                      <td>{sv.remaining}</td>
                      <td><button type="button" className="btn btn-small" disabled={sv.remaining <= 0} onClick={() => book(sv.session.id)}>{ar ? "احجز" : "Book"}</button></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )
        )}
        <form onSubmit={sendRequest} className="l-divider">
          <div style={{ display: "flex", gap: 8, flexWrap: "wrap", alignItems: "end" }}>
            <div className="field" style={{ minWidth: 180 }}><label>{ar ? "نوع الخدمة" : "Service type"}</label>
              <select value={reqForm.serviceType} onChange={(e) => setReqForm({ ...reqForm, serviceType: e.target.value })}>
                <option value="lab">{ar ? "تحليل معملي" : "Lab test"}</option>
                <option value="pharmacy">{ar ? "صرف / إعادة صرف دواء" : "Pharmacy refill"}</option>
                <option value="home_visit">{ar ? "زيارة منزلية" : "Home visit"}</option>
                <option value="other">{ar ? "خدمة أخرى" : "Other service"}</option>
              </select></div>
            <div className="field" style={{ flex: 1, minWidth: 220 }}><label>{ar ? "التفاصيل" : "Details"}</label>
              <input value={reqForm.details} onChange={(e) => setReqForm({ ...reqForm, details: e.target.value })} placeholder={ar ? "مثال: تحليل سكر صائم، أو اسم الدواء" : "e.g. fasting glucose, or the medicine name"} /></div>
            <div className="field"><label>{ar ? "التاريخ المفضل" : "Preferred date"}</label>
              <input type="date" value={reqForm.preferredDate} onChange={(e) => setReqForm({ ...reqForm, preferredDate: e.target.value })} /></div>
            <button type="submit" className="btn">{ar ? "أرسل الطلب" : "Send request"}</button>
          </div>
        </form>
      </div>

      <div className="card">
        <h2>{ar ? "مواعيدي" : "My appointments"}</h2>
        {appointments.length === 0 ? <p className="muted">{ar ? "لا مواعيد بعد." : "No appointments yet."}</p> : (
          <table className="table">
            <thead><tr><th>{ar ? "الموعد" : "When"}</th><th>{ar ? "العيادة" : "Clinic"}</th><th>{ar ? "الطبيب" : "Doctor"}</th><th>{ar ? "الحالة" : "Status"}</th><th></th></tr></thead>
            <tbody>
              {appointments.map((a) => (
                <tr key={a.id}>
                  <td dir="ltr">{new Date(a.startsAt).toLocaleString(ar ? "ar-EG" : "en-GB")}</td>
                  <td>{a.clinic ?? "—"}</td><td>{a.doctorName ?? "—"}</td>
                  <td>{a.status === "booked" ? <span className="badge ok">{ar ? "محجوز" : "Booked"}</span> : a.status === "cancelled" ? <span className="badge danger">{ar ? "ملغي" : "Cancelled"}</span> : <span className="badge">{a.status}</span>}</td>
                  <td>{a.status === "booked" && <button type="button" className="btn btn-small btn-secondary" onClick={() => setCancelFor(a)}>{ar ? "إلغاء" : "Cancel"}</button>}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <div className="card">
        <h2>{ar ? "طلبات خدماتي" : "My service requests"}</h2>
        {requests.length === 0 ? <p className="muted">{ar ? "لا طلبات بعد." : "No requests yet."}</p> : (
          <table className="table">
            <thead><tr><th>{ar ? "النوع" : "Type"}</th><th>{ar ? "التفاصيل" : "Details"}</th><th>{ar ? "الحالة" : "Status"}</th><th>{ar ? "رد المنشأة" : "Facility reply"}</th></tr></thead>
            <tbody>
              {requests.map((r) => (
                <tr key={r.id}>
                  <td>{r.serviceType}</td><td>{r.details ?? "—"}</td><td>{reqBadge(r.status)}</td><td>{r.staffNote ?? "—"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {cancelFor && (
        <div className="drawer-overlay" onClick={() => setCancelFor(null)}>
          <div className="drawer" onClick={(e) => e.stopPropagation()}>
            <h2>{ar ? "إلغاء الموعد" : "Cancel appointment"}</h2>
            <div className="field"><label>{ar ? "سبب الإلغاء (إلزامي)" : "Reason (mandatory)"}</label>
              <input value={reason} onChange={(e) => setReason(e.target.value)} /></div>
            <button type="button" className="btn" disabled={!reason.trim()} onClick={cancel}>{ar ? "تأكيد الإلغاء" : "Confirm cancellation"}</button>
          </div>
        </div>
      )}
    </div>
  );
}
