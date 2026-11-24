import { useState } from 'react';
import { useApi } from '../../api/ApiProvider';
import { useAction, useLoad } from '../../hooks/useLoad';
import { Icon } from '../../components/Icon';
import { CheckRow, ErrorNote, Loading, Screen } from '../../components/ui';

export function Checker() {
  const api = useApi();
  const job = useLoad(() => api.checkerJob(), []);
  const { busy, error, run } = useAction();
  const [outcome, setOutcome] = useState<'passed' | 'failed' | null>(null);
  const j = job.data;
  const done = j?.steps.filter((s) => s.done).length ?? 0;
  const total = j?.steps.length ?? 0;

  async function submit(passed: boolean) {
    const ok = await run(() => api.submitCheck(passed).then(() => true));
    if (ok) setOutcome(passed ? 'passed' : 'failed');
  }

  return (
    <Screen
      footer={
        j &&
        !outcome && (
          <>
            <ErrorNote message={error} />
            <div className="screen__footer-row">
              <button type="button" className="btn btn--secondary" onClick={() => submit(false)} disabled={busy}>
                Fail check
              </button>
              <button type="button" className="btn btn--primary btn--grow" onClick={() => submit(true)} disabled={busy}>
                {done === total ? 'Submit check, award badge' : `Submit check, ${done} of ${total}`}
              </button>
            </div>
          </>
        )
      }
    >
      {!j ? (
        <Loading error={job.error} onRetry={job.reload} />
      ) : (
        <>
          <div className="topbar topbar--split">
            <div className="stack" style={{ gap: 2 }}>
              <span className="eyebrow">Hlala checker · {j.area}</span>
              <h1 className="title title--lg">Room check</h1>
            </div>
            <span className="tag tag--dark">{j.payLabel}</span>
          </div>

          <div className="panel-dark">
            <div className="row row--between">
              <span style={{ fontSize: 15, fontWeight: 700 }}>{j.title}</span>
              <span className="muted" style={{ fontSize: 13 }}>
                {j.distance}
              </span>
            </div>
            <div className="row" style={{ gap: 8 }}>
              <Icon name="pin" size={16} color={j.atPin ? 'var(--amber)' : 'var(--on-dark)'} />
              <span style={{ fontSize: 13, color: j.atPin ? 'var(--amber)' : 'var(--on-dark)', fontWeight: 600 }}>
                {j.atPin ? 'You are at the pin. Location matches the listing.' : 'Walk to the pin before you start.'}
              </span>
            </div>
          </div>

          {outcome ? (
            <div className="note note--mint" role="status">
              {outcome === 'passed'
                ? 'Check submitted. The room now shows the Seen in person badge.'
                : 'Check failed and sent to the back office. The landlord is asked to fix the listing.'}
            </div>
          ) : (
            <div className="list-card">
              {j.steps.map((s) => (
                <CheckRow key={s.id} label={s.label} note={s.note} checked={s.done} onChange={(v) => api.setCheckerStep(s.id, v).then(job.set)} />
              ))}
            </div>
          )}

          <p className="note note--cream">
            Photos can only be taken with the in app camera, at the pin. Gallery uploads are blocked so nobody can pass off stolen
            pictures.
          </p>

          {j.next && (
            <div className="card row" style={{ padding: 12 }}>
              <div className="list-row__text">
                <span className="list-row__title">{j.next.title}</span>
                <span className="list-row__note">{j.next.note}</span>
              </div>
              <span className="tag">{j.moreToday} more today</span>
            </div>
          )}
        </>
      )}
    </Screen>
  );
}
