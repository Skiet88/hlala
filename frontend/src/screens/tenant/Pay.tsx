import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApi } from '../../api/ApiProvider';
import type { PayMethodId } from '../../api/types';
import { useAction, useLoad } from '../../hooks/useLoad';
import { formatRand } from '../../domain/money';
import { ErrorNote, Loading, Screen, TopBar } from '../../components/ui';

export function Pay() {
  const api = useApi();
  const navigate = useNavigate();
  const quote = useLoad(() => api.paymentQuote(), []);
  const [method, setMethod] = useState<PayMethodId>('TILL');
  const { busy, error, run } = useAction();

  async function pay() {
    const hold = await run(() => api.payIntoHold(method));
    if (hold) navigate('/move-in');
  }

  const q = quote.data;
  return (
    <Screen
      footer={
        q && (
          <>
            <ErrorNote message={error} />
            <button type="button" className="btn btn--primary btn--block" onClick={pay} disabled={busy}>
              Pay {formatRand(q.total)} into hold
            </button>
            <span className="footer-note">You get a receipt by SMS and in the app.</span>
          </>
        )
      }
    >
      <TopBar back={true} title="Secure the room" />
      {!q ? (
        <Loading error={quote.error} onRetry={quote.reload} />
      ) : (
        <>
          <p className="note note--mint">
            <strong>{q.landlordName} accepted your application.</strong> Pay within 48 hours to keep the room.
          </p>

          <div className="panel-dark" aria-label="What you pay">
            {[
              ['Deposit', formatRand(q.deposit)],
              [q.firstMonthLabel, formatRand(q.firstMonth)],
              ['Hlala fee', q.feeLabel],
            ].map(([label, value]) => (
              <div key={label} className="row row--between">
                <span className="muted" style={{ fontSize: 14 }}>
                  {label}
                </span>
                <span style={{ fontSize: 15, fontWeight: 700 }}>{value}</span>
              </div>
            ))}
            <div style={{ height: 1, background: 'var(--muted)' }} />
            <div className="row row--between">
              <span style={{ fontSize: 15, fontWeight: 700 }}>Goes into hold</span>
              <span className="price" style={{ fontSize: 24, color: 'var(--amber)' }}>
                {formatRand(q.total)}
              </span>
            </div>
            <span className="muted" style={{ fontSize: 13, lineHeight: '18px' }}>
              Held until you confirm you have the keys. If the room is not as shown, you get all of it back.
            </span>
          </div>

          <section className="stack" aria-labelledby="how-pay">
            <h2 id="how-pay" className="caps">
              How do you want to pay
            </h2>
            <div className="stack" role="radiogroup" aria-labelledby="how-pay">
              {q.methods.map((m) => (
                <button key={m.id} type="button" role="radio" aria-checked={m.id === method} className="option" onClick={() => setMethod(m.id)}>
                  <span className={`radio-dot${m.id === method ? ' radio-dot--on' : ''}`} />
                  <span className="stack" style={{ gap: 2 }}>
                    <span style={{ fontSize: 15, fontWeight: 700 }}>{m.label}</span>
                    <span className="small">{m.note}</span>
                  </span>
                </button>
              ))}
            </div>
          </section>
        </>
      )}
    </Screen>
  );
}
