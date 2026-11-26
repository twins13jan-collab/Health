const TONES: Record<string, string> = {
  active: "ok", completed: "ok", closed: "muted", reconciled: "ok",
  open: "info", in_progress: "info", triaged: "info", registered: "info",
  pending_review: "warn", on_hold: "warn", urgent: "warn", suspended: "warn",
  emergent: "danger", resuscitation: "danger", failed: "danger", escalated: "danger",
  cancelled: "muted", corrected: "warn", discharged: "ok", admitted: "info",
  in_consultation: "info", referred: "warn", draft: "muted",
  less_urgent: "ok", non_urgent: "muted", under_construction: "warn",
};

/** Soft-tinted status badge; unknown statuses fall back to a neutral tone. */
export default function StatusBadge({ status }: { status: string }) {
  const tone = TONES[status] ?? "muted";
  return <span className={`badge ${tone}`}>{status.replace(/_/g, " ")}</span>;
}
