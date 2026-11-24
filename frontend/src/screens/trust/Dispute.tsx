import { useParams } from 'react-router-dom';
import { useApi } from '../../api/ApiProvider';
import { useLoad } from '../../hooks/useLoad';
import { Icon } from '../../components/Icon';
import { Loading, Screen, Timeline, TopBar, useToast } from '../../components/ui';

export function Dispute() {
  const { id = 'case-0142' } = useParams();
  const api = useApi();
  const c = useLoad(() => api.disputeCase(id), [id]);
  const [toast, showToast] = useToast();
  const d = c.data;

  return (
    <Screen
      footer={
        <div className="screen__footer-row">
          <button type="button" className="btn btn--secondary" onClick={() => showToast('Adding proof arrives with file storage')}>
            Add more proof
          </button>
          <button type="button" className="btn btn--primary btn--grow" onClick={() => showToast('A case worker will call you back')}>
            Talk to a person
          </button>
        </div>
      }
    >
      <TopBar back={true} title={d ? `Case ${d.number}` : 'Case'} subtitle={d?.reason} />
      {!d ? (
        <Loading error={c.error} onRetry={c.reload} />
      ) : (
        <>
          <div className="panel-dark row row--top" style={{ flexDirection: 'row', gap: 12 }}>
            <span className="avatar avatar--amber avatar--sm" aria-hidden="true">
              <Icon name="lock" size={16} />
            </span>
            <div className="stack" style={{ gap: 2 }}>
              <span style={{ fontSize: 15, fontWeight: 700 }}>Nothing moves until this is decided</span>
              <span className="muted" style={{ fontSize: 13 }}>
                The listing is paused and no money can be paid out.
              </span>
            </div>
          </div>

          <Timeline steps={d.steps} />

          <section className="stack" aria-labelledby="proof">
            <h2 id="proof" className="caps">
              Proof on the case
            </h2>
            {d.proof.map((p) => (
              <div key={p.label} className="card row" style={{ padding: 12 }}>
                <div className="list-row__text">
                  <span className="list-row__title">{p.label}</span>
                  <span className="list-row__note">{p.note}</span>
                </div>
                <span className="tag">{p.from}</span>
              </div>
            ))}
          </section>
        </>
      )}
      {toast}
    </Screen>
  );
}
