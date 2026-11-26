"use client";

/** Khattab v3 component kit — every visual primitive of the new
 *  design system lives here (plus DataTable / KpiCard / StatusBadge
 *  in their own files for import compatibility). Class-based,
 *  styled by app/globals.css. */
import type { ButtonHTMLAttributes, ReactNode } from "react";

type BtnVariant = "primary" | "soft" | "ghost" | "danger" | "gold";
const BTN_CLASS: Record<BtnVariant, string> = {
  primary: "btn",
  soft: "btn btn-soft",
  ghost: "btn btn-ghost",
  danger: "btn btn-danger",
  gold: "btn btn-gold",
};

export function Button({
  variant = "primary",
  small,
  block,
  className,
  ...rest
}: ButtonHTMLAttributes<HTMLButtonElement> & { variant?: BtnVariant; small?: boolean; block?: boolean }) {
  const cls = `${BTN_CLASS[variant]}${small ? " btn-sm" : ""}${block ? " btn-block" : ""}${className ? ` ${className}` : ""}`;
  return <button type="button" className={cls} {...rest} />;
}

export function Card({ children, className }: { children: ReactNode; className?: string }) {
  return <section className={`card${className ? ` ${className}` : ""}`}>{children}</section>;
}

export function PageHead({
  eyebrow,
  title,
  sub,
  actions,
}: {
  eyebrow?: string;
  title: ReactNode;
  sub?: ReactNode;
  actions?: ReactNode;
}) {
  return (
    <div className="page-head">
      <div className="page-head-row">
        <div>
          {eyebrow ? <span className="eyebrow">{eyebrow}</span> : null}
          <h1>{title}</h1>
          {sub ? <p className="sub">{sub}</p> : null}
        </div>
        {actions ? <div className="row">{actions}</div> : null}
      </div>
    </div>
  );
}

export function SectionHead({ eyebrow, title, sub }: { eyebrow?: string; title: ReactNode; sub?: ReactNode }) {
  return (
    <div className="section-head">
      {eyebrow ? <span className="eyebrow">{eyebrow}</span> : null}
      <h2>{title}</h2>
      {sub ? <p className="sub muted">{sub}</p> : null}
    </div>
  );
}

export function Drawer({
  title,
  onClose,
  children,
}: {
  title: ReactNode;
  onClose: () => void;
  children: ReactNode;
}) {
  return (
    <div className="scrim" onClick={onClose}>
      <div className="drawer" onClick={(e) => e.stopPropagation()} role="dialog" aria-modal="true">
        <div className="drawer-head">
          <h2>{title}</h2>
          <button type="button" className="drawer-close" onClick={onClose} aria-label="✕">✕</button>
        </div>
        {children}
      </div>
    </div>
  );
}

export function Toasts({ items }: { items: { text: string; err?: boolean }[] }) {
  if (!items.length) return null;
  return (
    <div className="toasts">
      {items.map((t, i) => (
        <div key={i} className={`toast${t.err ? " err" : ""}`}>{t.text}</div>
      ))}
    </div>
  );
}

export function Tabs({
  tabs,
  value,
  onChange,
}: {
  tabs: { value: string; label: ReactNode }[];
  value: string;
  onChange: (v: string) => void;
}) {
  return (
    <div className="tabs" role="tablist">
      {tabs.map((t) => (
        <button key={t.value} type="button" role="tab" aria-selected={value === t.value}
          className={value === t.value ? "on" : ""} onClick={() => onChange(t.value)}>
          {t.label}
        </button>
      ))}
    </div>
  );
}

export function SegChips({
  options,
  value,
  onChange,
}: {
  options: { value: string; label: ReactNode }[];
  value: string;
  onChange: (v: string) => void;
}) {
  return (
    <div className="seg">
      {options.map((o) => (
        <button key={o.value} type="button" className={value === o.value ? "on" : ""} onClick={() => onChange(o.value)}>
          {o.label}
        </button>
      ))}
    </div>
  );
}

export function Skeleton({ rows = 4, kpis = 0 }: { rows?: number; kpis?: number }) {
  return (
    <>
      {kpis > 0 ? (
        <div className="kpi-grid">
          {Array.from({ length: kpis }).map((_, i) => <div key={i} className="skel skel-kpi" />)}
        </div>
      ) : null}
      <div>
        {Array.from({ length: rows }).map((_, i) => <div key={i} className="skel skel-row" />)}
      </div>
    </>
  );
}

export function EmptyState({ children, action }: { children: ReactNode; action?: ReactNode }) {
  return (
    <div className="empty">
      <div>{children}</div>
      {action ? <div className="mt">{action}</div> : null}
    </div>
  );
}

export function ProgressBar({ pct }: { pct: number }) {
  const p = Math.max(0, Math.min(100, pct));
  const tone = p > 85 ? "hot" : p > 70 ? "warm" : "";
  return (
    <span className={`bar ${tone}`}>
      <i style={{ width: `${p}%` }} />
    </span>
  );
}

export function ErrorBanner({ children, onRetry, retryLabel }: { children: ReactNode; onRetry?: () => void; retryLabel?: string }) {
  return (
    <div className="alert-error" role="alert">
      <span className="grow">{children}</span>
      {onRetry ? <Button variant="soft" small onClick={onRetry}>{retryLabel ?? "Retry"}</Button> : null}
    </div>
  );
}
