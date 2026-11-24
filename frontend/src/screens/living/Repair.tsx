import { Link } from 'react-router-dom';
import { useApi } from '../../api/ApiProvider';
import { useAction, useLoad } from '../../hooks/useLoad';
import { ErrorNote, Loading, Photo, Screen, Timeline, TopBar } from '../../components/ui';

export function Repair() {
  const api = useApi();
  const ticket = useLoad(() => api.repair('r-1'), []);
  const { busy, error, run } = useAction();
  const t = ticket.data;
  const closed = t?.status === 'CONFIRMED_FIXED';

  async function confirm(fixed: boolean) {
    const next = await run(() => api.confirmRepair('r-1', fixed));
    if (next) ticket.set(next);
  }

  return (
    <Screen
      footer={
        t && (
          <>
            <ErrorNote message={error} />
            <div className="screen__footer-row">
              <button type="button" className="btn btn--secondary" onClick={() => confirm(false)} disabled={busy}>
                Still broken
              </button>
              <button type="button" className="btn btn--primary btn--grow" onClick={() => confirm(true)} disabled={busy || closed}>
                Yes, it is fixed
              </button>
            </div>
          </>
        )
      }
    >
      <TopBar back="/home" backLabel="Back to my home" title="Repair" right={t && <span className="tag tag--dark">{closed ? 'Closed' : `Open, day ${t.dayCount}`}</span>} />
      {!t ? (
        <Loading error={ticket.error} onRetry={ticket.reload} />
      ) : (
        <>
          <div className="card row" style={{ padding: 12, gap: 12 }}>
            <Photo label="Your photo" width={84} height={84} radius={12} />
            <div className="stack" style={{ gap: 4 }}>
              <span style={{ fontSize: 15, fontWeight: 700 }}>{t.title}</span>
              <span className="small">{t.description}</span>
              <span className="tag tag--cream" style={{ alignSelf: 'flex-start' }}>
                {t.category}
              </span>
            </div>
          </div>
          <Timeline steps={t.steps} />
          <div className="bubble bubble--them" style={{ maxWidth: '100%' }}>
            {t.landlordNote}
          </div>
          <Link to="/report" className="link-btn">
            Landlord not fixing it? Ask Hlala to step in
          </Link>
        </>
      )}
    </Screen>
  );
}
