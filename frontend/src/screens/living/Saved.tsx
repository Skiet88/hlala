import { Link } from 'react-router-dom';
import { useApi } from '../../api/ApiProvider';
import { useLoad } from '../../hooks/useLoad';
import { formatRand } from '../../domain/money';
import { Icon } from '../../components/Icon';
import { TenantNav } from '../../components/BottomNav';
import { Loading, Screen, SwitchRow } from '../../components/ui';

export function Saved() {
  const api = useApi();
  const rooms = useLoad(() => api.savedRooms(), []);
  const alerts = useLoad(() => api.alerts(), []);
  const notes = useLoad(() => api.notifications(), []);
  const unread = notes.data?.filter((n) => n.unread).length ?? 0;

  return (
    <Screen nav={<TenantNav />}>
      <div className="topbar topbar--split">
        <h1 className="title title--lg">Saved</h1>
        <Link to="/notifications" className="icon-btn" aria-label={`Notifications, ${unread} new`} style={{ position: 'relative' }}>
          <Icon name="bell" size={20} />
          {unread > 0 && (
            <span className="count-dot" style={{ position: 'absolute', top: -4, right: -4 }} aria-hidden="true">
              {unread}
            </span>
          )}
        </Link>
      </div>

      {!rooms.data ? (
        <Loading error={rooms.error} onRetry={rooms.reload} />
      ) : rooms.data.length === 0 ? (
        <p className="lead">Tap the heart on a room to keep it here.</p>
      ) : (
        <div className="tile-grid">
          {rooms.data.map((r) => (
            <Link key={r.listingId} to={`/listings/${r.listingId}`} className="tile">
              <div className="photo tile__photo" aria-hidden="true">
                <span>Photo</span>
                <span className="tag tag--dark tile__badge">{r.state}</span>
              </div>
              <div className="tile__body">
                <span className="price">{formatRand(r.rent)}</span>
                <span className="tile__title">{r.title}</span>
                <span className="tile__meta">{r.meta}</span>
              </div>
            </Link>
          ))}
        </div>
      )}

      <h2 className="title" style={{ fontSize: 18 }}>
        Your alerts
      </h2>
      {!alerts.data ? (
        <Loading error={alerts.error} onRetry={alerts.reload} />
      ) : (
        <div className="list-card">
          {alerts.data.map((a) => (
            <SwitchRow key={a.id} label={a.label} note={a.note} checked={a.on} onChange={(v) => api.setAlert(a.id, v).then(alerts.set)} />
          ))}
        </div>
      )}
      <span className="small">Alerts come by SMS too, so you hear about a room even with no data.</span>

      <Link to="/wanted" className="panel-dark row row--between" style={{ flexDirection: 'row', textDecoration: 'none' }}>
        <span className="stack" style={{ gap: 2 }}>
          <span style={{ fontSize: 15, fontWeight: 700, color: '#fff' }}>Nothing fits? Post a room wanted</span>
          <span className="muted" style={{ fontSize: 13 }}>
            Let landlords with empty rooms come to you
          </span>
        </span>
        <Icon name="chevronRight" size={20} color="var(--amber)" />
      </Link>
    </Screen>
  );
}
