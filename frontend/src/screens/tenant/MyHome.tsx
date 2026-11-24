import { Link } from 'react-router-dom';
import { useApi } from '../../api/ApiProvider';
import { useLoad } from '../../hooks/useLoad';
import { formatRand } from '../../domain/money';
import { Icon } from '../../components/Icon';
import { TenantNav } from '../../components/BottomNav';
import { Loading, Screen, useToast } from '../../components/ui';

export function MyHome() {
  const api = useApi();
  const home = useLoad(() => api.myHome(), []);
  const [toast, showToast] = useToast();
  const h = home.data;

  const actions = h
    ? [
        { label: 'Repairs', note: h.openRepair ? `1 open, ${h.openRepair.title.toLowerCase()}` : 'Nothing open', to: '/home/repair' },
        { label: 'Yard electricity', note: 'Top ups and fair split', to: '/home/electricity' },
        { label: 'Lease and receipts', note: 'Every payment, saved', to: '/lease' },
        { label: 'Give notice', note: 'One month, then move out check', to: '/home/move-out' },
        { label: 'Notifications', note: h.unreadNotifications ? `${h.unreadNotifications} new` : 'All read', to: '/notifications' },
        { label: 'Report a problem', note: 'Money stays frozen until it is sorted', to: '/report' },
      ]
    : [];

  return (
    <Screen nav={<TenantNav />}>
      {!h ? (
        <Loading error={home.error} onRetry={home.reload} />
      ) : (
        <>
          <div className="stack" style={{ gap: 2 }}>
            <h1 className="title title--lg">My home</h1>
            <span className="subtitle">
              {h.title} · Landlord {h.landlordName}
            </span>
          </div>

          <div className="panel-dark">
            <div className="row row--between row--top">
              <div className="stack" style={{ gap: 2 }}>
                <span className="muted" style={{ fontSize: 13 }}>
                  {h.rentDue.monthLabel}
                </span>
                <span className="price" style={{ fontSize: 30 }}>
                  {formatRand(h.rentDue.amount)}
                </span>
              </div>
              <span className="tag tag--amber">{h.rentDue.dueLabel}</span>
            </div>
            <div className="screen__footer-row">
              <button type="button" className="btn btn--white btn--md btn--grow" onClick={() => showToast('Paying rent in the app arrives with payments')}>
                Pay rent
              </button>
              <button type="button" className="btn btn--ghost-light btn--md" onClick={() => showToast('Paying in parts arrives with payments')}>
                Pay in parts
              </button>
            </div>
          </div>

          <section className="card card--pad" aria-labelledby="record">
            <div className="row row--between">
              <h2 id="record" style={{ fontSize: 15, fontWeight: 700 }}>
                Your rent record
              </h2>
              <span className="tag">{h.onTimeLabel}</span>
            </div>
            <ol className="row" style={{ listStyle: 'none', margin: 0, padding: 0, justifyContent: 'space-between' }}>
              {h.months.map((m) => (
                <li key={m.label} className="stack" style={{ alignItems: 'center', gap: 4 }}>
                  <span
                    className="avatar avatar--sm"
                    style={{ width: 32, height: 32, background: m.paid ? 'var(--green)' : 'var(--amber)', color: m.paid ? '#fff' : 'var(--ink)' }}
                  >
                    {m.paid ? <Icon name="check" size={14} strokeWidth={3} /> : '!'}
                  </span>
                  <span className="small">
                    {m.label}
                    <span className="visually-hidden">{m.paid ? ' paid' : ' due'}</span>
                  </span>
                </li>
              ))}
            </ol>
            <span className="small">Paying through Hlala builds a record you can show the next landlord, a shop or a bank.</span>
          </section>

          <nav className="grid-2" aria-label="Your tenancy" style={{ gap: 8 }}>
            {actions.map((a) => (
              <Link key={a.label} to={a.to} className="card stack" style={{ padding: 12, gap: 2, textDecoration: 'none', color: 'var(--ink)', minHeight: 72 }}>
                <span style={{ fontSize: 14, fontWeight: 700 }}>{a.label}</span>
                <span className="small">{a.note}</span>
              </Link>
            ))}
          </nav>

          {h.openRepair && (
            <Link to="/home/repair" className="note note--cream row row--between" style={{ textDecoration: 'none' }}>
              <span className="stack" style={{ gap: 2 }}>
                <strong>{h.openRepair.when}</strong>
                <span>{h.openRepair.title}</span>
              </span>
              <span className="tag tag--dark">Open</span>
            </Link>
          )}
        </>
      )}
      {toast}
    </Screen>
  );
}
