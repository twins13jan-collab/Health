import type { ReactNode } from "react";

/** KPI tile — white card, small label, big number, optional hint/delta. */
export default function KpiCard({
  label,
  value,
  hint,
  delta,
  tone = "default",
}: {
  label: string;
  value: ReactNode;
  hint?: string;
  delta?: ReactNode;
  tone?: "default" | "warn" | "danger" | "ok";
}) {
  return (
    <div className={`kpi${tone !== "default" ? ` ${tone}` : ""}`}>
      <div className="kpi-label">{label}</div>
      <div className="kpi-value">{value}</div>
      {delta ? <div className="delta">{delta}</div> : null}
      {hint ? <div className="kpi-hint">{hint}</div> : null}
    </div>
  );
}
