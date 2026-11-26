"use client";

import { useCallback, useEffect, useState } from "react";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";
import { Button, Card, Drawer, EmptyState, ErrorBanner, PageHead, Toasts } from "@/components/ui";
import KpiCard from "@/components/KpiCard";

type StaffRow = { id: string; staff_ref: string | null; role: string | null; clinical: boolean; licence_valid: boolean; assignable: boolean; overdue_trainings: number; state: string; rank: number };
type TrainingRow = { id: string; staff_ref: string | null; course: string | null; mandatory: boolean; due_date: string | null; state: string; rank: number };
type GrievanceRow = { id: string; staff_ref: string | null; subject: string | null; reviewed: boolean; outcome: string | null; closable: boolean; rank: number };
type Board = { staff: StaffRow[]; trainings: TrainingRow[]; grievances: GrievanceRow[]; summary: Record<string, number> };

const STAFF_STATE_AR: Record<string, string> = { licence_blocked: "ترخيص منتهٍ — غير قابل للإسناد السريري", training_overdue: "تدريب إلزامي متأخر — عدم امتثال" };
const TRAIN_STATE_AR: Record<string, string> = { overdue: "متأخر", due_soon: "يستحق خلال 30 يومًا", open: "مفتوح" };

export default function WorkforceBoardPage() {
  const { lang } = useLang();
  const ar = lang === "ar";
  const [board, setBoard] = useState<Board | null>(null);
  const [err, setErr] = useState("");
  const [toasts, setToasts] = useState<{ text: string; err?: boolean }[]>([]);
  const [reviewG, setReviewG] = useState<GrievanceRow | null>(null);
  const [outcome, setOutcome] = useState("");
  const [newEmp, setNewEmp] = useState(false);
  const [empForm, setEmpForm] = useState({ staffRef: "", role: "", clinical: true, licenceValid: true });

  const toast = (text: string, isErr = false) => setToasts((t) => [...t, { text, err: isErr }]);
  const load = useCallback(async () => {
    try { setBoard(await api.getJson<Board>("/api/hr-deep/board")); setErr(""); }
    catch (e) { setErr(e instanceof Error ? e.message : String(e)); }
  }, []);
  useEffect(() => { load(); }, [load]);

  const close = async (g: GrievanceRow) => {
    try { await api.postJson(`/api/hr-deep/grievances/${g.id}/close`, {}); toast(ar ? "أُغلق التظلم بنتيجته المسجلة" : "Grievance closed with its recorded outcome"); load(); }
    catch (e) { toast(e instanceof Error ? e.message : String(e), true); }
  };
  const review = async () => {
    if (!reviewG) return;
    try {
      await api.postJson(`/api/hr-deep/grievances/${reviewG.id}/review`, { outcome });
      toast(ar ? "رُوجع التظلم وسُجلت نتيجته" : "Grievance reviewed with a recorded outcome");
      setReviewG(null); load();
    } catch (e) { toast(e instanceof Error ? e.message : String(e), true); }
  };
  const addEmp = async () => {
    try {
      await api.postJson("/api/hr-deep/employees", empForm);
      toast(ar ? "أُضيف سجل الموظف" : "Employee record added");
      setNewEmp(false); load();
    } catch (e) { toast(e instanceof Error ? e.message : String(e), true); }
  };

  const s = board?.summary ?? {};
  return (
    <div>
      <PageHead
        title={ar ? "القوى العاملة والموارد البشرية — الامتثال والجدولة" : "Workforce & HR — compliance and rostering"}
        sub={ar ? "الدور السريري بترخيص منتهٍ غير قابل للإسناد — الترخيص ليس تفصيلة ورقية. والتدريب الإلزامي المتأخر عدم امتثال موسوم، لا امتثال صامت. والتظلم لا يُغلق قبل مراجعة ونتيجة مسجلة." : "A clinical role with a lapsed licence is not assignable — a licence is not a paperwork detail. Overdue mandatory training is flagged non-compliance, never silent compliance. And a grievance closes only after review with a recorded outcome."}
        actions={<Button onClick={() => setNewEmp(true)}>{ar ? "+ سجل موظف" : "+ Employee record"}</Button>}
      />
      {err ? <ErrorBanner onRetry={load}>{err}</ErrorBanner> : null}
      <div className="kpi-grid">
        <KpiCard label={ar ? "سجلات موظفين" : "Staff records"} value={s.staff_records ?? "—"} />
        <KpiCard label={ar ? "محظور إسنادهم (ترخيص)" : "Licence-blocked"} value={s.licence_blocked ?? "—"} />
        <KpiCard label={ar ? "تدريبات إلزامية متأخرة" : "Overdue mandatory trainings"} value={s.trainings_overdue ?? "—"} />
        <KpiCard label={ar ? "تظلمات مفتوحة" : "Open grievances"} value={s.grievances_open ?? "—"} />
        <KpiCard label={ar ? "طاقم نشط / إجازات" : "Roster active / on leave"} value={`${s.roster_active ?? "—"} / ${s.roster_on_leave ?? "—"}`} />
        <KpiCard label={ar ? "ورديات اليوم" : "Shifts today"} value={s.shifts_today ?? "—"} />
      </div>
      <Card><h3 className="card-h">{ar ? "موظفون يحتاجون انتباهًا" : "Staff needing attention"}</h3>
        {!board ? <p className="muted">{ar ? "جارٍ التحميل…" : "Loading…"}</p>
          : board.staff.length === 0 ? <EmptyState>{ar ? "لا موظف محظورًا ولا متأخر التدريب — القوى العاملة ممتثلة." : "No blocked or overdue staff — the workforce is compliant."}</EmptyState>
          : board.staff.map((e) => (
            <div key={e.id} className="row-card">
              <div className="grow-field">
                <strong>{e.staff_ref ?? "—"}</strong> <span className="muted">{e.role ?? ""}</span>
                <div>
                  <span className={`badge${e.state === "licence_blocked" ? " danger" : " warn"}`}>{STAFF_STATE_AR[e.state] ?? e.state}</span>
                  {e.overdue_trainings > 0 ? <span className="badge warn">{e.overdue_trainings} {ar ? "تدريب متأخر" : "overdue trainings"}</span> : null}
                </div>
              </div>
            </div>
          ))}
      </Card>
      <Card><h3 className="card-h">{ar ? "تدريبات قادمة ومتأخرة" : "Trainings due & overdue"}</h3>
        {!board ? null : board.trainings.length === 0 ? <EmptyState>{ar ? "لا تدريبات مفتوحة." : "No open trainings."}</EmptyState>
          : board.trainings.map((t) => (
            <div key={t.id} className="row-card">
              <div className="grow-field">
                <strong>{t.course ?? "—"}</strong> <span className="muted">{t.staff_ref ?? ""} · {ar ? "الاستحقاق" : "Due"}: {t.due_date ?? "—"} {t.mandatory ? `· ${ar ? "إلزامي" : "mandatory"}` : ""}</span>
              </div>
              <span className={`badge${t.state === "overdue" ? " danger" : t.state === "due_soon" ? " warn" : ""}`}>{TRAIN_STATE_AR[t.state] ?? t.state}</span>
            </div>
          ))}
      </Card>
      <Card><h3 className="card-h">{ar ? "التظلمات" : "Grievances"}</h3>
        {!board ? null : board.grievances.length === 0 ? <EmptyState>{ar ? "لا تظلمات مفتوحة." : "No open grievances."}</EmptyState>
          : board.grievances.map((g) => (
            <div key={g.id} className="row-card">
              <div className="grow-field">
                <strong>{g.subject ?? "—"}</strong> <span className="muted">{g.staff_ref ?? ""}</span>
                <div>{g.reviewed
                  ? <span className="badge">{ar ? "رُوجع" : "Reviewed"}: {g.outcome ?? ""}</span>
                  : <span className="badge warn">{ar ? "بانتظار المراجعة — لا يُغلق بصمت" : "Unreviewed — never closes silently"}</span>}</div>
              </div>
              {!g.reviewed ? <Button variant="soft" onClick={() => { setReviewG(g); setOutcome(""); }}>{ar ? "مراجعة" : "Review"}</Button> : null}
              {g.closable ? <Button onClick={() => close(g)}>{ar ? "إغلاق" : "Close"}</Button> : null}
            </div>
          ))}
      </Card>

      {reviewG ? (
        <Drawer title={ar ? "مراجعة التظلم — نتيجة مسجلة" : "Review the grievance — recorded outcome"} onClose={() => setReviewG(null)}>
          <div className="field"><label>{ar ? "نتيجة المراجعة" : "Review outcome"}</label>
            <input value={outcome} onChange={(e) => setOutcome(e.target.value)} /></div>
          <Button onClick={review}>{ar ? "حفظ المراجعة" : "Save review"}</Button>
        </Drawer>
      ) : null}
      {newEmp ? (
        <Drawer title={ar ? "سجل موظف جديد" : "New employee record"} onClose={() => setNewEmp(false)}>
          <div className="field"><label>{ar ? "مرجع الموظف" : "Staff ref"}</label>
            <input value={empForm.staffRef} onChange={(e) => setEmpForm({ ...empForm, staffRef: e.target.value })} /></div>
          <div className="field"><label>{ar ? "الدور" : "Role"}</label>
            <input value={empForm.role} onChange={(e) => setEmpForm({ ...empForm, role: e.target.value })} /></div>
          <label className="check-row"><input type="checkbox" checked={empForm.clinical} onChange={(e) => setEmpForm({ ...empForm, clinical: e.target.checked })} /> {ar ? "دور سريري" : "Clinical role"}</label>
          <label className="check-row"><input type="checkbox" checked={empForm.licenceValid} onChange={(e) => setEmpForm({ ...empForm, licenceValid: e.target.checked })} /> {ar ? "الترخيص سارٍ" : "Licence valid"}</label>
          <Button onClick={addEmp}>{ar ? "إضافة" : "Add"}</Button>
        </Drawer>
      ) : null}
      <Toasts items={toasts} />
    </div>
  );
}
