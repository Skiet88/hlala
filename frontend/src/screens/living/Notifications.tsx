import { Link } from 'react-router-dom';
import { useApi } from '../../api/ApiProvider';
import { useLoad } from '../../hooks/useLoad';
import { Loading, Screen, TopBar } from '../../components/ui';

export function Notifications() {
  const api = useApi();
  const notes = useLoad(() => api.notifications(), []);

  return (
    <Screen>
      <TopBar
        back={true}
        title="Notifications"
        right={
          <button type="button" className="link-btn" onClick={() => api.markAllRead().then(notes.set)}>
            Mark all read
          </button>
        }
      />
      {!notes.data ? (
        <Loading error={notes.error} onRetry={notes.reload} />
      ) : (
        <ul className="list-card" style={{ listStyle: 'none', margin: 0, padding: 0 }}>
          {notes.data.map((n) => (
            <li key={n.id}>
              <Link to={n.to} className="list-row" style={{ alignItems: 'flex-start' }}>
                <span
                  aria-hidden="true"
                  style={{ flex: 'none', width: 10, height: 10, marginTop: 6, borderRadius: 5, background: n.unread ? 'var(--amber)' : 'transparent' }}
                />
                <span className="list-row__text">
                  <span className="list-row__title" style={{ fontWeight: n.unread ? 700 : 600 }}>
                    {n.unread && <span className="visually-hidden">New: </span>}
                    {n.title}
                  </span>
                  <span className="list-row__note">{n.body}</span>
                </span>
                <span className="small" style={{ flex: 'none' }}>
                  {n.when}
                </span>
              </Link>
            </li>
          ))}
        </ul>
      )}
      <span className="small">Money and viewing messages are also sent by SMS.</span>
    </Screen>
  );
}
