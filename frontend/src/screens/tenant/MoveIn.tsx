import { Link, useNavigate } from 'react-router-dom';
import { useApi } from '../../api/ApiProvider';
import { useAction, useLoad } from '../../hooks/useLoad';
import { formatRand } from '../../domain/money';
import { depositStateWords } from '../../domain/depositState';
import { Icon } from '../../components/Icon';
import { CheckRow, ErrorNote, Loading, Photo, Screen } from '../../components/ui';

const PHOTO_ORDER = ['Door', 'Walls', 'Toilet', 'Window', 'Floor', 'Meter'];

export function MoveIn() {
  const api = useApi();
  const navigate = useNavigate();
  const day = useLoad(() => api.moveInDay(), []);
  const { busy, error, run } = useAction();

  async function release() {
    const hold = await run(() => api.confirmMovedIn());
    if (hold) navigate('/home');
  }

  const d = day.data;
  const allDone = d?.checklist.every((i) => i.done) ?? false;
  return (
    <Screen
      footer={
        <>
          <ErrorNote message={error} />
          <button type="button" className="btn btn--primary btn--block" onClick={release} disabled={busy || !d}>
            I have my keys. Release payment
          </button>
          {!allDone && d && <span className="footer-note">Tick every item first. The money only moves once you are sure.</span>}
          <Link to="/report" className="link-btn link-btn--center">
            Something is wrong with the room
          </Link>
        </>
      }
    >
      {!d ? (
        <Loading error={day.error} onRetry={day.reload} />
      ) : (
        <>
          <div className="stack" style={{ gap: 6 }}>
            <span className="eyebrow">{d.dateLabel}</span>
            <h1 className="title title--xl">Move in day. Check before you release.</h1>
            <p className="lead">
              {formatRand(d.hold.amount)} is still on hold. {d.landlordName} is paid only after you finish this list.
            </p>
            <span className="tag" style={{ alignSelf: 'flex-start' }}>
              Deposit: {depositStateWords(d.hold.state)}
            </span>
          </div>

          <div className="list-card">
            {d.checklist.map((item) => (
              <CheckRow
                key={item.id}
                label={item.label}
                note={item.note}
                checked={item.done}
                onChange={(v) => api.setMoveInItem(item.id, v).then(day.set)}
              />
            ))}
          </div>
          {!d.checklist.find((i) => i.id === 'lease')?.done && (
            <Link to="/lease" className="link-btn">
              Read and sign the lease
            </Link>
          )}

          <section className="stack" aria-labelledby="photos">
            <h2 id="photos" className="caps">
              Condition photos, {d.photos.length} of {d.photosNeeded}
            </h2>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, minmax(0, 1fr))', gap: 8 }}>
              {d.photos.map((p) => (
                <Photo key={p} label={p} height={72} radius={12} />
              ))}
              {d.photos.length < d.photosNeeded && (
                <button
                  type="button"
                  aria-label="Add a condition photo"
                  onClick={() => api.addConditionPhoto(PHOTO_ORDER[d.photos.length] ?? 'Extra').then(day.set)}
                  style={{ height: 72, borderRadius: 12, border: '2px dashed var(--line)', background: 'transparent', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                >
                  <Icon name="camera" size={22} color="var(--muted)" />
                </button>
              )}
            </div>
            <span className="small">
              Photos are date stamped and saved for both of you. They settle arguments about the deposit when you move out.
            </span>
          </section>
        </>
      )}
    </Screen>
  );
}
