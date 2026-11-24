import { useApi } from '../../api/ApiProvider';
import { useAction, useLoad } from '../../hooks/useLoad';
import { formatRand } from '../../domain/money';
import { LandlordNav } from '../../components/BottomNav';
import { Icon } from '../../components/Icon';
import { ErrorNote, Loading, Screen, TopBar, useToast } from '../../components/ui';

export function RentBook() {
  const api = useApi();
  const book = useLoad(() => api.rentBook(), []);
  const { busy, error, run } = useAction();
  const [toast, showToast] = useToast();

  async function cash(leaseId: string) {
    const next = await run(() => api.recordCash(leaseId));
    if (next) {
      book.set(next);
      showToast('Cash recorded. The tenant gets a receipt by SMS');
    }
  }

  const b = book.data;
  const collected = b?.rows.filter((r) => r.status === 'PAID').reduce((s, r) => s + r.amount, 0) ?? 0;
  const owed = b?.rows.filter((r) => r.status === 'LATE').reduce((s, r) => s + r.amount, 0) ?? 0;

  return (
    <Screen
      nav={<LandlordNav />}
      footer={
        <button type="button" className="btn btn--secondary btn--block" onClick={() => showToast('Statements arrive with payments')}>
          Download statement for the year
        </button>
      }
    >
      <TopBar
        back="/landlord"
        backLabel="Back to your rooms"
        title="Rent book"
        right={
          <div className="row" style={{ gap: 0 }}>
            <button type="button" className="icon-btn icon-btn--plain" aria-label="Previous month" disabled>
              <Icon name="chevronLeft" size={18} />
            </button>
            <span style={{ fontSize: 14, fontWeight: 700 }}>{b?.monthLabel}</span>
            <button type="button" className="icon-btn icon-btn--plain" aria-label="Next month" disabled>
              <Icon name="chevronRight" size={18} />
            </button>
          </div>
        }
      />
      {!b ? (
        <Loading error={book.error} onRetry={book.reload} />
      ) : (
        <>
          <div className="grid-2">
            <div className="panel-dark" style={{ gap: 4 }}>
              <span className="muted" style={{ fontSize: 13 }}>
                Collected
              </span>
              <span className="price" style={{ fontSize: 26 }}>
                {formatRand(collected)}
              </span>
            </div>
            <div className="card card--pad" style={{ gap: 4 }}>
              <span className="subtitle">Still owed</span>
              <span className="price" style={{ fontSize: 26 }}>
                {formatRand(owed)}
              </span>
            </div>
          </div>
          <ErrorNote message={error} />
          {b.rows.map((r) => (
            <article key={r.leaseId} className="card card--pad" aria-label={r.name}>
              <div className="row row--between row--top">
                <div className="stack" style={{ gap: 2 }}>
                  <span style={{ fontSize: 14, fontWeight: 700 }}>{r.name}</span>
                  <span className="small" style={r.status === 'LATE' ? { color: '#8a5a00', fontWeight: 600 } : undefined}>
                    {r.note}
                  </span>
                </div>
                <span className="price" style={{ fontSize: 17 }}>
                  {formatRand(r.amount)}
                </span>
              </div>
              {r.status === 'PAID' ? (
                <div className="row row--between">
                  <span className="tag">{r.how}</span>
                  <button type="button" className="link-btn" onClick={() => showToast('Receipt sent again by SMS')}>
                    Send receipt again
                  </button>
                </div>
              ) : (
                <div className="screen__footer-row">
                  <button type="button" className="btn btn--secondary btn--sm btn--grow" onClick={() => cash(r.leaseId)} disabled={busy}>
                    Record cash paid
                  </button>
                  <button type="button" className="btn btn--amber btn--sm btn--grow" onClick={() => showToast('Reminder sent by SMS')}>
                    Send reminder
                  </button>
                </div>
              )}
            </article>
          ))}
          <p className="note note--mint">Cash still counts. Record it here and the tenant gets a receipt, so nobody argues later.</p>
        </>
      )}
      {toast}
    </Screen>
  );
}
