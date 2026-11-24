import { useApi } from '../../api/ApiProvider';
import { useAction, useLoad } from '../../hooks/useLoad';
import { Icon } from '../../components/Icon';
import { ErrorNote, Loading, Screen, TopBar, useToast } from '../../components/ui';

/** The one page lease. English only for now; the isiXhosa version needs a fluent translator. */
export function Lease() {
  const api = useApi();
  const lease = useLoad(() => api.lease(), []);
  const { busy, error, run } = useAction();
  const [toast, showToast] = useToast();
  const l = lease.data;

  async function sign() {
    if (!l) return;
    const next = await run(() => api.signLease(l.id));
    if (next) lease.set(next);
  }

  const bothSigned = !!(l?.landlordSigned && l?.tenantSigned);
  return (
    <Screen
      footer={
        l && (
          <>
            <ErrorNote message={error} />
            <div className="screen__footer-row">
              <button type="button" className="btn btn--secondary" aria-label="Listen to the lease read aloud" onClick={() => showToast('Read aloud arrives with the isiXhosa version')}>
                <Icon name="play" size={16} />
                Listen
              </button>
              <button type="button" className="btn btn--primary btn--grow" onClick={sign} disabled={busy || bothSigned}>
                {bothSigned ? 'Lease signed by both' : 'Sign as tenant'}
              </button>
            </div>
          </>
        )
      }
    >
      <TopBar back={true} title={l?.title ?? 'Lease'} right={<span className="tag tag--white" style={{ border: '1px solid var(--line)' }}>English</span>} />
      {!l ? (
        <Loading error={lease.error} onRetry={lease.reload} />
      ) : (
        <>
          <p className="lead">{l.intro}</p>
          <dl className="list-card" style={{ margin: 0 }}>
            {l.terms.map((t) => (
              <div key={t.head} className="list-row" style={{ flexDirection: 'column', alignItems: 'flex-start', gap: 2 }}>
                <dt className="caps">{t.head}</dt>
                <dd style={{ margin: 0, fontSize: 14, lineHeight: '20px' }}>{t.body}</dd>
              </div>
            ))}
          </dl>
          <div className="grid-2">
            <Signature role="Landlord" signed={l.landlordSigned} />
            <Signature role="Tenant" signed={l.tenantSigned} />
          </div>
          <p className="small">isiXhosa version coming: it will be written by a fluent speaker, not machine translated.</p>
        </>
      )}
      {toast}
    </Screen>
  );
}

function Signature({ role, signed }: { role: string; signed: { by: string; on: string } | null }) {
  return (
    <div className="stack" style={{ gap: 2, padding: 12, borderRadius: 14, background: signed ? 'var(--mint)' : 'var(--card)', border: signed ? 0 : '1px dashed var(--line)' }}>
      <span className="small">{role}</span>
      <span style={{ fontSize: 14, fontWeight: 700, color: signed ? 'var(--green-dark)' : 'var(--ink)' }}>{signed ? 'Signed' : 'Not signed yet'}</span>
      <span className="small">{signed ? `${signed.by}, ${signed.on}` : 'Sign with a code sent by SMS'}</span>
    </div>
  );
}
