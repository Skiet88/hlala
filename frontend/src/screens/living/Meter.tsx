import { useState, type FormEvent } from 'react';
import { useApi } from '../../api/ApiProvider';
import { useAction, useLoad } from '../../hooks/useLoad';
import { splitMeter } from '../../domain/meterSplit';
import { formatRand, parseRand } from '../../domain/money';
import { ErrorNote, Loading, Screen, TopBar, useToast } from '../../components/ui';

export function Meter() {
  const api = useApi();
  const yard = useLoad(() => api.meterYard(), []);
  const [logging, setLogging] = useState(false);
  const [amount, setAmount] = useState('');
  const { busy, error, run } = useAction();
  const [toast, showToast] = useToast();
  const y = yard.data;
  const total = y ? y.members.reduce((s, m) => s + m.paid, 0) : 0;
  const shares = y ? splitMeter(y.members, y.mode) : [];

  async function log(e: FormEvent) {
    e.preventDefault();
    const value = parseRand(amount);
    const next = await run(() => api.logTopUp(value ?? 0));
    if (next) {
      yard.set(next);
      setAmount('');
      setLogging(false);
    }
  }

  return (
    <Screen
      footer={
        <>
          <button type="button" className="btn btn--primary btn--block" onClick={() => showToast('Everyone gets their share by SMS')}>
            Send everyone their share
          </button>
          <span className="footer-note">Changing the split needs a yes from every room.</span>
        </>
      }
    >
      <TopBar back="/home" backLabel="Back to my home" title="Yard electricity" subtitle="For yards that share one prepaid meter" />
      {!y ? (
        <Loading error={yard.error} onRetry={yard.reload} />
      ) : (
        <>
          <div className="panel-dark">
            <div className="row row--between row--top">
              <div className="stack" style={{ gap: 2 }}>
                <span className="muted" style={{ fontSize: 13 }}>
                  Bought in {y.monthLabel}
                </span>
                <span className="price" style={{ fontSize: 30 }}>
                  {formatRand(total)}
                </span>
                <span className="muted" style={{ fontSize: 12 }}>
                  {y.topUps.length} top ups · meter {y.meterNumber}
                </span>
              </div>
              {!logging && (
                <button type="button" className="btn btn--white btn--sm" onClick={() => setLogging(true)}>
                  Log a top up
                </button>
              )}
            </div>
            {logging && (
              <form className="row" onSubmit={log}>
                <label htmlFor="topup" className="visually-hidden">
                  Amount on the slip
                </label>
                <input id="topup" className="input input--dark" inputMode="decimal" placeholder="R100" value={amount} onChange={(e) => setAmount(e.target.value)} style={{ flex: 1, minWidth: 0 }} autoFocus />
                <button type="submit" className="btn btn--amber btn--sm" disabled={busy}>
                  Save
                </button>
              </form>
            )}
            <ErrorNote message={error} />
          </div>

          <section className="stack" aria-labelledby="how-split">
            <h2 id="how-split" className="caps">
              How the yard agreed to split
            </h2>
            <div className="segmented" role="group" aria-labelledby="how-split">
              <button type="button" aria-pressed={y.mode === 'EQUAL_PER_ROOM'} onClick={() => api.setSplitMode('EQUAL_PER_ROOM').then(yard.set)}>
                Equal per room
              </button>
              <button type="button" aria-pressed={y.mode === 'PER_PERSON'} onClick={() => api.setSplitMode('PER_PERSON').then(yard.set)}>
                By number of people
              </button>
            </div>
          </section>

          <div className="list-card">
            {shares.map((s) => (
              <div key={s.id} className="list-row">
                <div className="list-row__text">
                  <span className="list-row__title">{s.name}</span>
                  <span className="list-row__note">
                    {s.people === 1 ? '1 person' : `${s.people} people`} · share {formatRand(s.share)} · paid {formatRand(s.paid)}
                  </span>
                </div>
                <span className={`tag ${s.balance < 0 ? 'tag--cream' : ''}`}>
                  {s.balance < 0 ? `Owes ${formatRand(-s.balance)}` : `Gets ${formatRand(s.balance)}`}
                </span>
              </div>
            ))}
          </div>

          <section className="stack" aria-labelledby="topups">
            <h2 id="topups" className="caps">
              Top ups this month
            </h2>
            {y.topUps.map((t) => (
              <div key={t.id} className="row row--between" style={{ fontSize: 13 }}>
                <span>{t.label}</span>
                <strong>{formatRand(t.amount)}</strong>
              </div>
            ))}
          </section>
        </>
      )}
      {toast}
    </Screen>
  );
}
