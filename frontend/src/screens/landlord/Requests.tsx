import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useApi } from '../../api/ApiProvider';
import type { ViewingRequest } from '../../api/types';
import { useAction, useLoad } from '../../hooks/useLoad';
import { LandlordNav } from '../../components/BottomNav';
import { ErrorNote, Loading, Screen, TopBar } from '../../components/ui';

export function Requests() {
  const api = useApi();
  const reqs = useLoad(() => api.viewingRequests(), []);
  const [tab, setTab] = useState<'viewings' | 'applications'>('viewings');
  const { error, run } = useAction();

  async function decide(id: string, decision: ViewingRequest['status']) {
    const next = await run(() => api.decideViewing(id, decision));
    if (next) reqs.set(next);
  }

  const r = reqs.data;
  return (
    <Screen nav={<LandlordNav />}>
      <TopBar back="/landlord" backLabel="Back to your rooms" title={r?.roomLabel ?? 'Requests'} subtitle={r?.roomNote} />
      {!r ? (
        <Loading error={reqs.error} onRetry={reqs.reload} />
      ) : (
        <>
          <div className="segmented" role="group" aria-label="Show">
            <button type="button" aria-pressed={tab === 'viewings'} onClick={() => setTab('viewings')}>
              Viewings, {r.viewings.length}
            </button>
            <button type="button" aria-pressed={tab === 'applications'} onClick={() => setTab('applications')}>
              Applications, {r.applications}
            </button>
          </div>
          <ErrorNote message={error} />

          {tab === 'applications' ? (
            <div className="card card--pad">
              <span style={{ fontSize: 14, fontWeight: 700 }}>Sipho M. applied after the viewing</span>
              <span className="small">ID checked · 14 months rent on time · proof of income</span>
              <p className="note note--mint">You see the ticks only. Documents stay with Hlala.</p>
              <Link to="/lease" className="btn btn--primary btn--md">
                Accept and send the lease
              </Link>
            </div>
          ) : (
            r.viewings.map((p) => (
              <article key={p.id} className="card card--pad" aria-label={p.name}>
                <div className="row" style={{ gap: 12 }}>
                  <span className="avatar" aria-hidden="true">
                    {p.initial}
                  </span>
                  <div className="list-row__text">
                    <span style={{ fontSize: 15, fontWeight: 700 }}>{p.name}</span>
                    <span className="small">{p.who}</span>
                  </div>
                  <span className="eyebrow" style={{ textAlign: 'right' }}>
                    {p.slot}
                  </span>
                </div>
                <div className="chip-row" style={{ gap: 6 }}>
                  {p.tags.map((t) => (
                    <span key={t} className="tag">
                      {t}
                    </span>
                  ))}
                </div>
                {p.status === 'PENDING' && (
                  <div className="screen__footer-row">
                    <button type="button" className="btn btn--secondary btn--sm" onClick={() => decide(p.id, 'DECLINED')}>
                      Decline
                    </button>
                    <Link to="/chat" className="btn btn--secondary btn--sm">
                      Chat
                    </Link>
                    <button type="button" className="btn btn--primary btn--sm btn--grow" onClick={() => decide(p.id, 'ACCEPTED')}>
                      Accept
                    </button>
                  </div>
                )}
                {p.status !== 'PENDING' && (
                  <div
                    className={`note ${p.status === 'ACCEPTED' ? '' : 'note--stone'} row row--between`}
                    style={p.status === 'ACCEPTED' ? { background: 'var(--ink)', color: '#fff' } : undefined}
                    role="status"
                  >
                    <span>{p.status === 'ACCEPTED' ? 'Viewing confirmed. Added to your calendar.' : 'Declined. They get a polite message.'}</span>
                    <button
                      type="button"
                      className="link-btn"
                      style={{ color: p.status === 'ACCEPTED' ? 'var(--amber)' : 'var(--green)' }}
                      onClick={() => decide(p.id, 'PENDING')}
                    >
                      Undo
                    </button>
                  </div>
                )}
              </article>
            ))
          )}
        </>
      )}
    </Screen>
  );
}
