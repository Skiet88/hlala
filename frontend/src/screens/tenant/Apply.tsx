import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useApi } from '../../api/ApiProvider';
import { useAction, useLoad } from '../../hooks/useLoad';
import { Icon } from '../../components/Icon';
import { ErrorNote, Loading, Screen, TopBar, useToast } from '../../components/ui';

export function Apply() {
  const { id = '' } = useParams();
  const api = useApi();
  const navigate = useNavigate();
  const profile = useLoad(() => api.renterProfile(), []);
  const listing = useLoad(() => api.getListing(id), [id]);
  const [who, setWho] = useState('1 adult');
  const [when, setWhen] = useState('1 December');
  const { busy, error, run } = useAction();
  const [toast, showToast] = useToast();

  async function send() {
    const app = await run(() => api.apply(id, who, when));
    if (app) navigate('/pay');
  }

  const p = profile.data;
  return (
    <Screen
      footer={
        <>
          <ErrorNote message={error} />
          <button type="button" className="btn btn--primary btn--block" onClick={send} disabled={busy || !p}>
            Send application
          </button>
        </>
      }
    >
      <TopBar back="/chat" backLabel="Back to chat" title="Apply for this room" />
      {!p ? (
        <Loading error={profile.error} onRetry={profile.reload} />
      ) : (
        <>
          <div className="panel-dark">
            <div className="row" style={{ gap: 12 }}>
              <span className="avatar avatar--amber" aria-hidden="true">
                {p.initial}
              </span>
              <div className="stack" style={{ gap: 2 }}>
                <span style={{ fontSize: 16, fontWeight: 700 }}>{p.name}</span>
                <span className="muted" style={{ fontSize: 13 }}>
                  Your renter profile, shown to the landlord
                </span>
              </div>
            </div>
            <div className="row row--wrap" style={{ gap: 6 }}>
              {p.badges.map((b) => (
                <span key={b} className="tag tag--amber">
                  {b}
                </span>
              ))}
              <span className="tag tag--outline-light">{p.rentRecord}</span>
            </div>
          </div>

          <div className="list-card">
            {p.rows.map((r) => (
              <div key={r.label} className="list-row">
                <div className="list-row__text">
                  <span className="list-row__note">{r.label}</span>
                  <span className="list-row__title">{r.value}</span>
                </div>
                {r.done ? (
                  <span className="avatar avatar--sm" style={{ background: 'var(--mint)', color: 'var(--green)' }} aria-label="Done">
                    <Icon name="check" size={16} strokeWidth={3} />
                  </span>
                ) : (
                  <button type="button" className="btn btn--secondary btn--sm" onClick={() => showToast('Adding documents arrives with file storage')}>
                    Add
                  </button>
                )}
              </div>
            ))}
          </div>

          <div className="grid-2">
            <div className="field">
              <label htmlFor="who">Who is moving in</label>
              <input id="who" className="input" value={who} onChange={(e) => setWho(e.target.value)} />
            </div>
            <div className="field">
              <label htmlFor="when">Move in date</label>
              <input id="when" className="input" value={when} onChange={(e) => setWhen(e.target.value)} />
            </div>
          </div>

          <p className="note note--mint">
            {listing.data?.landlord.name ?? 'The landlord'} sees the ticks only. Your ID number, payslip and bank details stay
            private.
          </p>
        </>
      )}
      {toast}
    </Screen>
  );
}
