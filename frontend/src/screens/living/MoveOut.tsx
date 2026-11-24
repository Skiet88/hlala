import { Link } from 'react-router-dom';
import { useApi } from '../../api/ApiProvider';
import { useLoad } from '../../hooks/useLoad';
import { settle } from '../../domain/settlement';
import { formatRand } from '../../domain/money';
import { Loading, Screen, TopBar } from '../../components/ui';

export function MoveOut() {
  const api = useApi();
  const check = useLoad(() => api.moveOutCheck(), []);
  const c = check.data;
  const s = c ? settle(c.held, c.deduction.amount, c.decision) : null;

  const footnote = c
    ? {
        PENDING: `The part nobody argues about is paid out today. Only the ${formatRand(c.deduction.amount)} waits for your answer.`,
        ACCEPTED: `${formatRand(c.deduction.amount)} goes to the landlord for the ${c.deduction.label.toLowerCase()}. You get the rest today.`,
        DISPUTED: `${formatRand(c.deduction.amount)} stays frozen until the dispute desk decides. You still get ${formatRand(s!.toTenantNow)} today.`,
      }[c.decision]
    : '';

  return (
    <Screen
      footer={
        s && (
          <Link to="/home/reviews" className="btn btn--primary btn--block">
            Hand back keys and get {formatRand(s.toTenantNow)}
          </Link>
        )
      }
    >
      <TopBar back="/home" backLabel="Back to my home" title="Move out check" subtitle={c && `Notice given in the app · last day ${c.lastDay}`} />
      {!c || !s ? (
        <Loading error={check.error} onRetry={check.reload} />
      ) : (
        <>
          <section className="stack" aria-label="Photos from move in and today">
            <div style={{ display: 'grid', gridTemplateColumns: '64px 1fr 1fr 72px', gap: 8, alignItems: 'center' }}>
              <span />
              <span className="caps">Move in</span>
              <span className="caps">Today</span>
              <span />
              {c.pairs.map((p) => (
                <PairRow key={p.label} label={p.label} damaged={p.damaged} />
              ))}
            </div>
          </section>

          <div className="card card--pad">
            <div className="row row--between">
              <span style={{ fontSize: 15, fontWeight: 700 }}>{c.deduction.label}</span>
              <span className="price" style={{ fontSize: 18 }}>
                {formatRand(c.deduction.amount)}
              </span>
            </div>
            <span className="small">{c.deduction.note}</span>
            {c.decision === 'PENDING' ? (
              <div className="screen__footer-row">
                <button type="button" className="btn btn--secondary btn--sm btn--grow" onClick={() => api.decideDeduction('DISPUTED').then(check.set)}>
                  I do not agree
                </button>
                <button type="button" className="btn btn--dark btn--sm btn--grow" onClick={() => api.decideDeduction('ACCEPTED').then(check.set)}>
                  Fair, take it off
                </button>
              </div>
            ) : (
              <div className="note note--stone row row--between" role="status">
                <span>{c.decision === 'ACCEPTED' ? `You agreed to the ${formatRand(c.deduction.amount)}` : 'Sent to the dispute desk'}</span>
                <button type="button" className="link-btn" onClick={() => api.decideDeduction('PENDING').then(check.set)}>
                  Change
                </button>
              </div>
            )}
          </div>

          <div className="panel-dark" aria-label="Your deposit">
            <div className="row row--between">
              <span className="muted">Deposit on hold</span>
              <strong>{formatRand(s.held)}</strong>
            </div>
            <div className="row row--between">
              <span className="muted">{c.decision === 'DISPUTED' ? 'Frozen, in dispute' : `${c.deduction.label}, asked by landlord`}</span>
              <strong>{formatRand(s.deduction)}</strong>
            </div>
            <div style={{ height: 1, background: 'var(--muted)' }} />
            <div className="row row--between">
              <span style={{ fontWeight: 700 }}>Back to you now</span>
              <span className="price" style={{ fontSize: 24, color: 'var(--amber)' }}>
                {formatRand(s.toTenantNow)}
              </span>
            </div>
            <span className="muted" style={{ fontSize: 13 }}>
              {footnote}
            </span>
          </div>
        </>
      )}
    </Screen>
  );
}

function PairRow({ label, damaged }: { label: string; damaged: boolean }) {
  return (
    <>
      <span style={{ fontSize: 13, fontWeight: 600 }}>{label}</span>
      <div className="photo" style={{ height: 56, borderRadius: 10 }} role="img" aria-label={`${label} on move in day (placeholder)`}>
        <span aria-hidden="true">1 Dec</span>
      </div>
      <div className="photo" style={{ height: 56, borderRadius: 10 }} role="img" aria-label={`${label} today (placeholder)`}>
        <span aria-hidden="true">Today</span>
      </div>
      <span className={`tag ${damaged ? 'tag--cream' : ''}`} style={{ justifyContent: 'center' }}>
        {damaged ? 'Damage' : 'Same'}
      </span>
    </>
  );
}
