import { useEffect, useState, type ReactNode } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Icon, type IconName } from './Icon';
import { formatRand } from '../domain/money';

// ------------------------------------------------------------ layout

export function Screen({
  children,
  footer,
  nav,
  tone = 'light',
  flush = false,
  label,
}: {
  children: ReactNode;
  footer?: ReactNode;
  nav?: ReactNode;
  tone?: 'light' | 'dark' | 'map';
  flush?: boolean;
  label?: string;
}) {
  const toneClass = tone === 'dark' ? ' screen--dark' : tone === 'map' ? ' screen--map' : '';
  return (
    <div className={`screen${toneClass}`}>
      <main className={`screen__body${flush ? ' screen__body--flush' : ''}`} aria-label={label}>
        {children}
      </main>
      {footer && <div className="screen__footer">{footer}</div>}
      {nav}
    </div>
  );
}

/** Round back button. Goes to `to` when given, else one step back in history. */
export function BackButton({ to, label = 'Back', icon = 'chevronLeft' }: { to?: string; label?: string; icon?: IconName }) {
  const navigate = useNavigate();
  if (to) {
    return (
      <Link to={to} className="icon-btn" aria-label={label}>
        <Icon name={icon} />
      </Link>
    );
  }
  return (
    <button type="button" className="icon-btn" aria-label={label} onClick={() => navigate(-1)}>
      <Icon name={icon} />
    </button>
  );
}

export function TopBar({
  back,
  backLabel,
  title,
  subtitle,
  right,
}: {
  back?: string | true;
  backLabel?: string;
  title: ReactNode;
  subtitle?: ReactNode;
  right?: ReactNode;
}) {
  return (
    <div className="topbar">
      {back && <BackButton to={back === true ? undefined : back} label={backLabel} />}
      <div className="topbar__titles">
        <h1 className="title">{title}</h1>
        {subtitle && <span className="subtitle">{subtitle}</span>}
      </div>
      {right}
    </div>
  );
}

export function StepProgress({ step, of = 4, close }: { step: number; of?: number; close: string }) {
  return (
    <>
      <div className="topbar topbar--split">
        <BackButton to={close} label="Close" icon="close" />
        <span className="eyebrow">
          Step {step} of {of}
        </span>
      </div>
      <div className="progress-steps" aria-hidden="true">
        {Array.from({ length: of }, (_, i) => (
          <span key={i} data-done={i < step} />
        ))}
      </div>
    </>
  );
}

// ------------------------------------------------------------- input

export function Chip({
  label,
  pressed,
  onClick,
  dark = false,
}: {
  label: string;
  pressed: boolean;
  onClick: () => void;
  dark?: boolean;
}) {
  return (
    <button type="button" className={`chip${dark ? ' chip--dark' : ''}`} aria-pressed={pressed} onClick={onClick}>
      {label}
    </button>
  );
}

export function Switch({ label, checked, onChange }: { label: string; checked: boolean; onChange: (v: boolean) => void }) {
  return (
    <button type="button" role="switch" className="switch" aria-checked={checked} aria-label={label} onClick={() => onChange(!checked)}>
      <span className="switch__track">
        <span className="switch__knob" />
      </span>
    </button>
  );
}

export function Tick({ label, checked, onChange }: { label: string; checked: boolean; onChange: (v: boolean) => void }) {
  return (
    <button type="button" role="checkbox" className="tick" aria-checked={checked} aria-label={label} onClick={() => onChange(!checked)}>
      <span className="tick__box">{checked && <Icon name="check" size={16} color="#fff" strokeWidth={3} />}</span>
    </button>
  );
}

export function SwitchRow({
  label,
  note,
  checked,
  onChange,
}: {
  label: string;
  note?: string;
  checked: boolean;
  onChange: (v: boolean) => void;
}) {
  return (
    <div className="list-row">
      <div className="list-row__text">
        <span className="list-row__title">{label}</span>
        {note && <span className="list-row__note">{note}</span>}
      </div>
      <Switch label={label} checked={checked} onChange={onChange} />
    </div>
  );
}

export function CheckRow({
  label,
  note,
  checked,
  onChange,
}: {
  label: string;
  note?: string;
  checked: boolean;
  onChange: (v: boolean) => void;
}) {
  return (
    <div className="list-row">
      <Tick label={label} checked={checked} onChange={onChange} />
      <div className="list-row__text">
        <span className="list-row__title">{label}</span>
        {note && <span className="list-row__note">{note}</span>}
      </div>
    </div>
  );
}

/** A read only progress list: what happened, what is still waiting. */
export function Timeline({ steps }: { steps: { label: string; note: string; done: boolean }[] }) {
  return (
    <ol className="list-card" style={{ listStyle: 'none', margin: 0, padding: 0 }}>
      {steps.map((s) => (
        <li key={s.label} className="list-row">
          <span className={`step-dot${s.done ? ' step-dot--done' : ''}`}>
            {s.done && <Icon name="check" size={14} color="#fff" strokeWidth={3} />}
          </span>
          <div className="list-row__text">
            <span className="list-row__title">
              {s.label}
              <span className="visually-hidden">{s.done ? ', done' : ', waiting'}</span>
            </span>
            <span className="list-row__note">{s.note}</span>
          </div>
        </li>
      ))}
    </ol>
  );
}

// ----------------------------------------------------------- display

export function Photo({
  label = 'Photo',
  height,
  width,
  radius,
  className = '',
}: {
  label?: string;
  height?: number;
  width?: number;
  radius?: number;
  className?: string;
}) {
  return (
    <div
      className={`photo ${className}`}
      style={{ height, width, flex: width ? 'none' : undefined, borderRadius: radius }}
      role="img"
      aria-label={`${label} (placeholder)`}
    >
      <span aria-hidden="true">{label}</span>
    </div>
  );
}

export function SeenBadge() {
  return (
    <span className="seen-badge">
      <Icon name="check" size={12} strokeWidth={3} />
      Seen in person
    </span>
  );
}

export function Rand({ amount }: { amount: number }) {
  return <>{formatRand(amount)}</>;
}

export function PerMonth({ amount, size = 'md' }: { amount: number; size?: 'md' | 'xl' }) {
  return (
    <span className={`price${size === 'xl' ? ' price--xl' : ''}`}>
      {formatRand(amount)} <span className="price__unit">per month</span>
    </span>
  );
}

export function ErrorNote({ message }: { message: string | null }) {
  if (!message) return null;
  return (
    <p className="note note--error" role="alert">
      {message}
    </p>
  );
}

/** Loading and error states for a whole screen while its data arrives. */
export function Loading({ error, onRetry }: { error?: string | null; onRetry?: () => void }) {
  if (error) {
    return (
      <div className="state" role="alert">
        <Icon name="alert" size={28} />
        <p>{error}</p>
        {onRetry && (
          <button type="button" className="btn btn--secondary btn--sm" onClick={onRetry}>
            Try again
          </button>
        )}
      </div>
    );
  }
  return (
    <div className="state" aria-busy="true" aria-live="polite">
      <div className="skeleton" style={{ width: '100%', height: 120 }} />
      <div className="skeleton" style={{ width: '100%', height: 64 }} />
      <span className="visually-hidden">Loading</span>
    </div>
  );
}

/** A short message that disappears by itself, for confirmations. */
export function useToast(): [ReactNode, (message: string) => void] {
  const [message, setMessage] = useState<string | null>(null);
  useEffect(() => {
    if (!message) return;
    const t = setTimeout(() => setMessage(null), 2600);
    return () => clearTimeout(t);
  }, [message]);
  const node = message ? (
    <div className="toast" role="status">
      {message}
    </div>
  ) : null;
  return [node, setMessage];
}
