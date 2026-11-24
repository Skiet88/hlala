import { Link } from 'react-router-dom';
import { useApi } from '../../api/ApiProvider';
import { useAction, useLoad } from '../../hooks/useLoad';
import { formatRand } from '../../domain/money';
import { LandlordNav } from '../../components/BottomNav';
import { Icon } from '../../components/Icon';
import { ErrorNote, Loading, Screen, useToast } from '../../components/ui';

export function LandlordHome() {
  const api = useApi();
  const home = useLoad(() => api.landlordHome(), []);
  const { error, run } = useAction();
  const [toast, showToast] = useToast();
  const h = home.data;

  async function remind(room: number, name: string) {
    const sent = await run(() => api.remindTenant(room).then(() => true));
    if (sent) showToast(`Reminder sent to ${name} by SMS`);
  }

  return (
    <Screen nav={<LandlordNav />}>
      {!h ? (
        <Loading error={home.error} onRetry={home.reload} />
      ) : (
        <>
          <div className="topbar topbar--split">
            <div className="stack" style={{ gap: 2 }}>
              <span className="eyebrow">Molo, {h.greetingName}</span>
              <h1 className="title title--lg">Your rooms</h1>
            </div>
            <Link to="/list/where" className="pill-btn pill-btn--dark">
              <Icon name="plus" size={16} color="var(--amber)" strokeWidth={2.6} />
              Add a room
            </Link>
          </div>

          <div className="panel-dark">
            <div className="row row--between">
              <span className="muted" style={{ fontSize: 13 }}>
                {h.monthLabel} rent collected
              </span>
              <Link to="/landlord/rent-book" style={{ color: 'var(--amber)', fontSize: 13, fontWeight: 700 }}>
                Open rent book
              </Link>
            </div>
            <div className="row" style={{ alignItems: 'baseline', gap: 8 }}>
              <span className="price" style={{ fontSize: 30 }}>
                {formatRand(h.collected)}
              </span>
              <span className="muted" style={{ fontSize: 14 }}>
                of {formatRand(h.expected)}
              </span>
            </div>
            <div
              className="bar"
              role="progressbar"
              aria-label={`${formatRand(h.collected)} of ${formatRand(h.expected)} collected`}
              aria-valuemin={0}
              aria-valuemax={h.expected}
              aria-valuenow={h.collected}
            >
              <div className="bar__fill" style={{ width: `${Math.min(100, (h.collected / h.expected) * 100)}%` }} />
            </div>
            <span className="muted" style={{ fontSize: 13 }}>
              {h.summary}
            </span>
          </div>

          <ErrorNote message={error} />
          <ul className="stack" style={{ listStyle: 'none', margin: 0, padding: 0 }}>
            {h.rooms.map((r) => (
              <li key={r.number} className="card row" style={{ padding: 12, gap: 12 }}>
                <span className="avatar avatar--sm" style={{ background: 'var(--mint)', color: 'var(--green-dark)' }} aria-label={`Room ${r.number}`}>
                  {r.number}
                </span>
                <div className="list-row__text">
                  <span className="list-row__title">{r.title}</span>
                  <span className="list-row__note">{r.note}</span>
                </div>
                {r.status === 'PAID' && <span className="tag">Paid</span>}
                {r.status === 'LATE' && (
                  <button type="button" className="btn btn--amber btn--sm" onClick={() => remind(r.number, r.title)}>
                    Remind
                  </button>
                )}
                {r.status === 'EMPTY' && (
                  <Link to="/landlord/requests" className="btn btn--primary btn--sm">
                    {r.requests} requests
                  </Link>
                )}
              </li>
            ))}
          </ul>

          <div className="grid-2" style={{ gap: 8 }}>
            <Link to="/lease" className="card stack" style={{ padding: 12, gap: 2, textDecoration: 'none', color: 'var(--ink)' }}>
              <span style={{ fontSize: 14, fontWeight: 700 }}>Leases</span>
              <span className="small">Simple, signed on the phone</span>
            </Link>
            <Link to="/home/repair" className="card stack" style={{ padding: 12, gap: 2, textDecoration: 'none', color: 'var(--ink)' }}>
              <span style={{ fontSize: 14, fontWeight: 700 }}>Repairs</span>
              <span className="small">{h.openRepairs}</span>
            </Link>
          </div>
        </>
      )}
      {toast}
    </Screen>
  );
}
