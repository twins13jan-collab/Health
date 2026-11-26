"use client";

import { useCallback, useEffect, useState } from "react";
import { Button, Drawer, PageHead, Toasts } from "@/components/ui";
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

const REQ_TYPE_AR: Record<string, string> = { lab: "تحليل معملي", pharmacy: "صيدلية", radiology: "أشعة وتصوير طبي", dialysis: "غسيل كلوي", home_visit: "زيارة منزلية", other: "خدمة أخرى" };
const REQ_TYPE_EN: Record<string, string> = { lab: "Lab test", pharmacy: "Pharmacy", radiology: "Radiology & imaging", dialysis: "Dialysis", home_visit: "Home visit", other: "Other service" };
/** Finder deep links carry a catalog service key; map it to the
 *  portal request type it files as (clinics book sessions instead). */
const SERVICE_TO_PORTAL: Record<string, string> = { lab: "lab", pharmacy: "pharmacy", radiology: "radiology", dialysis: "dialysis", home_visit: "home_visit", blood: "other", vaccination: "other" };

/** My Health — the patient/citizen dashboard: profile, clinic
 *  booking against live sessions, my appointments with reasoned
 *  cancellation, and service requests (lab / pharmacy / home
 *  visit) with their facility responses. Exactly the /api/portal
 *  surface — nothing fictional. */
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
      setFacilities((facs.facilities ?? []).filter((f: FacilitySvc) => f.services.length > 0));
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); }
  }, []);
  useEffect(() => { load(); }, [load]);

  /* Arriving from the public finder: preselect the facility, map
   * the chosen bookable service onto the request form (or aim at
   * the clinic-booking card for session services), and scroll to
   * the relevant card. */
  useEffect(() => {
    const qs = new URLSearchParams(window.location.search);
    const fac = qs.get("facility");
    const svc = qs.get("service");
    if (fac) setFacilityId(fac);
    if (svc && SERVICE_TO_PORTAL[svc]) setReqForm((f) => ({ ...f, serviceType: SERVICE_TO_PORTAL[svc] }));
    if (fac || svc) {
      const target = svc === "clinics" ? "book-card" : "request-card";
      window.setTimeout(() => document.getElementById(target)?.scrollIntoView({ behavior: "smooth", block: "start" }), 500);
    }
  }, []);

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
    : s === "confirmed" ? <span className="badge info">{ar ? "مؤكدة" : "Confirmed"}</span>
    : s === "cancelled" ? <span className="badge danger">{ar ? "ملغاة" : "Cancelled"}</span>
    : <span className="badge warn">{ar ? "قيد المراجعة" : "Requested"}</span>;

  return (
    <>
      <PageHead
        eyebrow={ar ? "بوابة المريض" : "Patient portal"}
        title={`♥ ${ar ? "حسابي الصحي" : "My Health"}`}
        sub={ar ? "مواعيدك وحجوزاتك وطلبات خدماتك — كلها مرتبطة بسجلك الصحي الموحد." : "Your appointments, bookings and service requests — all linked to your unified health record."}
      />
      {error && <div className="alert-error">{error} <Button variant="soft" small onClick={() => setError(null)}>{ar ? "إغلاق" : "Dismiss"}</Button></div>}
      <Toasts items={toast ? [{ text: toast }] : []} />

      <div className="kpi-grid">
        <div className="kpi"><div className="kpi-label">{ar ? "الاسم" : "Name"}</div><div className="kpi-value kpi-name">{me?.patient?.fullName ?? me?.user?.fullName ?? "—"}</div></div>
        <div className="kpi"><div className="kpi-label">{ar ? "الرقم القومي" : "National ID"}</div><div className="kpi-value kpi-name" dir="ltr">{me?.patient?.nationalId ?? "—"}</div></div>
        <div className="kpi"><div className="kpi-label">{ar ? "مواعيد قائمة" : "Upcoming appointments"}</div><div className="kpi-value">{appointments.filter((a) => a.status === "booked").length}</div></div>
        <div className="kpi"><div className="kpi-label">{ar ? "طلبات بانتظار الرد" : "Requests awaiting reply"}</div><div className="kpi-value">{requests.filter((r) => r.status === "requested").length}</div></div>
      </div>

      <div className="card" id="book-card">
        <h2>{ar ? "احجز موعد عيادة" : "Book a clinic appointment"}</h2>
        <div className="toolbar">
          <div className="field grow-field">
            <label>{ar ? "المنشأة" : "Facility"}</label>
            <select value={facilityId} onChange={(e) => setFacilityId(e.target.value)}>
              <option value="">{ar ? "اختر المنشأة" : "Pick a facility"}</option>
              {facilities.map((f) => <option key={f.id} value={f.id}>{f.nameAr} — {f.governorate}</option>)}
            </select>
          </div>
          <div className="field">
            <label>{ar ? "التاريخ" : "Date"}</label>
            <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
          </div>
        </div>
        {facilityId && (
          sessions.length === 0
            ? <div className="empty">{ar ? "لا جلسات عيادات مفتوحة في هذا اليوم بهذه المنشأة." : "No open clinic sessions at this facility on this day."}</div>
            : (
              <div className="tbl-wrap">
                <table className="tbl">
                  <thead><tr><th>{ar ? "العيادة" : "Clinic"}</th><th>{ar ? "الطبيب" : "Doctor"}</th><th>{ar ? "الوقت" : "Time"}</th><th>{ar ? "المتبقي" : "Remaining"}</th><th></th></tr></thead>
                  <tbody>
                    {sessions.map((sv) => (
                      <tr key={sv.session.id}>
                        <td>{sv.clinicName}</td><td>{sv.session.doctorName}</td>
                        <td dir="ltr">{sv.session.startsAt} – {sv.session.endsAt}</td>
                        <td>{sv.remaining}</td>
                        <td><Button small disabled={sv.remaining <= 0} onClick={() => book(sv.session.id)}>{ar ? "احجز" : "Book"}</Button></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )
        )}
      </div>

      <div className="card" id="request-card">
        <h2>{ar ? "اطلب خدمة (معمل / صيدلية / أشعة / غسيل كلوي / زيارة منزلية)" : "Request a service (lab / pharmacy / radiology / dialysis / home visit)"}</h2>
        <form onSubmit={sendRequest}>
          <div className="form-grid">
            <div className="field"><label>{ar ? "نوع الخدمة" : "Service type"}</label>
              <select value={reqForm.serviceType} onChange={(e) => setReqForm({ ...reqForm, serviceType: e.target.value })}>
                <option value="lab">{ar ? "تحليل معملي" : "Lab test"}</option>
                <option value="pharmacy">{ar ? "صرف / إعادة صرف دواء" : "Pharmacy refill"}</option>
                <option value="radiology">{ar ? "أشعة وتصوير طبي" : "Radiology & imaging"}</option>
                <option value="dialysis">{ar ? "جلسة غسيل كلوي" : "Dialysis session"}</option>
                <option value="home_visit">{ar ? "زيارة منزلية" : "Home visit"}</option>
                <option value="other">{ar ? "خدمة أخرى (تطعيم / دم / غيره)" : "Other service (vaccination / blood / …)"}</option>
              </select></div>
            <div className="field"><label>{ar ? "التاريخ المفضل" : "Preferred date"}</label>
              <input type="date" value={reqForm.preferredDate} onChange={(e) => setReqForm({ ...reqForm, preferredDate: e.target.value })} /></div>
            <div className="field"><label>{ar ? "التفاصيل" : "Details"}</label>
              <input value={reqForm.details} onChange={(e) => setReqForm({ ...reqForm, details: e.target.value })} placeholder={ar ? "مثال: تحليل سكر صائم، أو اسم الدواء" : "e.g. fasting glucose, or the medicine name"} /></div>
          </div>
          <div className="form-actions">
            <button type="submit" className="btn">{ar ? "أرسل الطلب" : "Send request"}</button>
          </div>
        </form>
      </div>

      <div className="grid-2">
        <div className="card">
          <h2>{ar ? "مواعيدي" : "My appointments"}</h2>
          {appointments.length === 0 ? <div className="empty">{ar ? "لا مواعيد بعد." : "No appointments yet."}</div> : (
            <div className="tbl-wrap">
              <table className="tbl">
                <thead><tr><th>{ar ? "الموعد" : "When"}</th><th>{ar ? "العيادة" : "Clinic"}</th><th>{ar ? "الطبيب" : "Doctor"}</th><th>{ar ? "الحالة" : "Status"}</th><th></th></tr></thead>
                <tbody>
                  {appointments.map((a) => (
                    <tr key={a.id}>
                      <td dir="ltr">{new Date(a.startsAt).toLocaleString(ar ? "ar-EG" : "en-GB")}</td>
                      <td>{a.clinic ?? "—"}</td><td>{a.doctorName ?? "—"}</td>
                      <td>{a.status === "booked" ? <span className="badge ok">{ar ? "محجوز" : "Booked"}</span> : a.status === "cancelled" ? <span className="badge danger">{ar ? "ملغي" : "Cancelled"}</span> : <span className="badge">{a.status}</span>}</td>
                      <td>{a.status === "booked" && <Button small variant="soft" onClick={() => setCancelFor(a)}>{ar ? "إلغاء" : "Cancel"}</Button>}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        <div className="card">
          <h2>{ar ? "طلبات خدماتي" : "My service requests"}</h2>
          {requests.length === 0 ? <div className="empty">{ar ? "لا طلبات بعد." : "No requests yet."}</div> : (
            <div className="tbl-wrap">
              <table className="tbl">
                <thead><tr><th>{ar ? "النوع" : "Type"}</th><th>{ar ? "التفاصيل" : "Details"}</th><th>{ar ? "الحالة" : "Status"}</th><th>{ar ? "رد المنشأة" : "Facility reply"}</th></tr></thead>
                <tbody>
                  {requests.map((r) => (
                    <tr key={r.id}>
                      <td>{(ar ? REQ_TYPE_AR : REQ_TYPE_EN)[r.serviceType] ?? r.serviceType}</td><td>{r.details ?? "—"}</td><td>{reqBadge(r.status)}</td><td>{r.staffNote ?? "—"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {cancelFor && (
        <Drawer title={ar ? "إلغاء الموعد" : "Cancel appointment"} onClose={() => setCancelFor(null)}>
          <div className="field"><label>{ar ? "سبب الإلغاء (إلزامي)" : "Reason (mandatory)"}</label>
            <input value={reason} onChange={(e) => setReason(e.target.value)} /></div>
          <div className="form-actions">
            <Button variant="danger" disabled={!reason.trim()} onClick={cancel}>{ar ? "تأكيد الإلغاء" : "Confirm cancellation"}</Button>
          </div>
        </Drawer>
      )}
    </>
  );
}
