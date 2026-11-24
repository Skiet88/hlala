import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useApi } from '../../api/ApiProvider';
import type { NewListingDraft, PlaceKind } from '../../api/types';
import { useAction, useLoad } from '../../hooks/useLoad';
import { formatRand, parseRand } from '../../domain/money';
import { Icon } from '../../components/Icon';
import { Chip, ErrorNote, Loading, Screen, StepProgress, SwitchRow } from '../../components/ui';

// The four "list a room" steps share one draft kept by the API, so a landlord
// can leave half way and pick up where they stopped.

function useDraft() {
  const api = useApi();
  const loaded = useLoad(() => api.listingDraft(), []);
  const [draft, setDraft] = useState<NewListingDraft | null>(null);
  useEffect(() => {
    if (loaded.data) setDraft(loaded.data);
  }, [loaded.data]);
  const { busy, error, run } = useAction();
  const save = (next: NewListingDraft) => run(() => api.saveListingDraft(next));
  return { draft, setDraft, save, busy, error, loading: loaded, api };
}

const SECTIONS = ['Site C', 'Site B', 'Harare', 'Makhaza', 'Kuyasa', 'Langa'];
const KINDS: PlaceKind[] = ['Backyard room', 'Backyard flat', 'Room in a house', 'Whole house'];

export function ListPin() {
  const navigate = useNavigate();
  const { draft, setDraft, save, busy, error, loading } = useDraft();

  async function next() {
    if (draft && (await save(draft))) navigate('/list/rent');
  }

  return (
    <Screen
      footer={
        <>
          <ErrorNote message={error} />
          <button type="button" className="btn btn--primary btn--block" onClick={next} disabled={!draft || busy}>
            Continue to rent
          </button>
        </>
      }
    >
      <StepProgress step={1} close="/landlord" />
      <h1 className="title title--xl">Where is the room, and what kind is it?</h1>
      {!draft ? (
        <Loading error={loading.error} onRetry={loading.reload} />
      ) : (
        <>
          <section className="stack" aria-labelledby="sec">
            <h2 id="sec" className="caps">
              Section
            </h2>
            <div className="chip-row">
              {SECTIONS.map((s) => (
                <Chip key={s} label={s} pressed={draft.section === s} onClick={() => setDraft({ ...draft, section: s })} />
              ))}
            </div>
          </section>
          <section className="stack" aria-labelledby="kind">
            <h2 id="kind" className="caps">
              Type of place
            </h2>
            <div className="chip-row">
              {KINDS.map((k) => (
                <Chip key={k} label={k === 'Room in a house' ? 'Room in my house' : k} pressed={draft.kind === k} onClick={() => setDraft({ ...draft, kind: k })} />
              ))}
            </div>
          </section>
          <div className="field">
            <label htmlFor="mark">Nearest place everybody knows</label>
            <input
              id="mark"
              className="input"
              placeholder="Behind the Site C taxi rank, near the clinic"
              value={draft.landmark}
              onChange={(e) => setDraft({ ...draft, landmark: e.target.value })}
            />
          </div>
          <div className="map" style={{ minHeight: 150, flex: 'none', borderRadius: 16 }} aria-hidden="true">
            <div className="map__road" style={{ left: '-10%', top: '40%', width: '120%', height: 14, transform: 'rotate(-10deg)' }} />
            <div className="map__road" style={{ left: '55%', top: '-10%', width: 12, height: '130%', transform: 'rotate(-10deg)' }} />
            <span style={{ position: 'absolute', left: '50%', top: '50%', transform: 'translate(-50%, -100%)' }}>
              <Icon name="pin" size={34} color="var(--green)" fill="#fff" strokeWidth={2.4} />
            </span>
            <span className="tag tag--white" style={{ position: 'absolute', left: 12, bottom: 12 }}>
              Map placeholder. Drag the pin onto your yard.
            </span>
          </div>
          <span className="small">Tenants see the area and walking times only. Your exact yard shows after you accept a viewing.</span>
        </>
      )}
    </Screen>
  );
}

const EXTRAS: [string, string][] = [
  ['toilet', 'Own toilet and shower'],
  ['meter', 'Own prepaid meter'],
  ['water', 'Water included in rent'],
  ['gate', 'Burglar gate'],
  ['furnished', 'Furnished'],
];

export function ListRoom() {
  const navigate = useNavigate();
  const { draft, setDraft, save, busy, error, loading } = useDraft();
  const [rentText, setRentText] = useState('');
  const [depositText, setDepositText] = useState('');
  const [problem, setProblem] = useState<string | null>(null);

  useEffect(() => {
    if (draft) {
      setRentText((t) => t || (draft.rent != null ? formatRand(draft.rent) : ''));
      setDepositText((t) => t || (draft.deposit != null ? formatRand(draft.deposit) : ''));
    }
  }, [draft]);

  async function next() {
    if (!draft) return;
    const rent = parseRand(rentText);
    const deposit = parseRand(depositText);
    if (rent == null || rent <= 0 || deposit == null || deposit <= 0) {
      setProblem('Enter the rent and the deposit in rand, for example R2 200');
      return;
    }
    setProblem(null);
    if (await save({ ...draft, rent, deposit })) navigate('/list/photos');
  }

  return (
    <Screen
      footer={
        <>
          <ErrorNote message={problem ?? error} />
          <div className="screen__footer-row">
            <Link to="/list/where" className="btn btn--secondary">
              Back
            </Link>
            <button type="button" className="btn btn--primary btn--grow" onClick={next} disabled={!draft || busy}>
              Continue to photos
            </button>
          </div>
        </>
      }
    >
      <StepProgress step={2} close="/landlord" />
      <div className="stack" style={{ gap: 4 }}>
        <h1 className="title title--xl">Rent, and what comes with the room</h1>
        <p className="lead">Clear answers here mean fewer wasted viewings.</p>
      </div>
      {!draft ? (
        <Loading error={loading.error} onRetry={loading.reload} />
      ) : (
        <>
          <div className="grid-2">
            <div className="field">
              <label htmlFor="rent">Rent per month</label>
              <input id="rent" className="input" inputMode="numeric" placeholder="R2 200" value={rentText} onChange={(e) => setRentText(e.target.value)} />
            </div>
            <div className="field">
              <label htmlFor="dep">Deposit</label>
              <input id="dep" className="input" inputMode="numeric" placeholder="R2 200" value={depositText} onChange={(e) => setDepositText(e.target.value)} />
            </div>
          </div>
          <div className="list-card">
            {EXTRAS.map(([key, label]) => (
              <SwitchRow key={key} label={label} checked={!!draft.extras[key]} onChange={(v) => setDraft({ ...draft, extras: { ...draft.extras, [key]: v } })} />
            ))}
          </div>
          <div className="note note--cream row row--top" style={{ gap: 12 }}>
            <span className="avatar avatar--amber avatar--sm" aria-hidden="true">
              <Icon name="camera" size={16} />
            </span>
            <span className="stack" style={{ gap: 2 }}>
              <strong>Next: photos from your phone</strong>
              <span>A Hlala checker from your area then visits to confirm the room. Checked rooms get the Seen in person badge and show first.</span>
            </span>
          </div>
        </>
      )}
    </Screen>
  );
}

const SHOTS = ['Door', 'Room', 'Window', 'Toilet', 'Yard', 'Gate'];

export function ListPhotos() {
  const navigate = useNavigate();
  const { draft, setDraft, save, busy, error, loading } = useDraft();
  const taken = draft ? SHOTS.filter((s) => draft.photos[s]).length : 0;

  async function next() {
    if (draft && (await save(draft))) navigate('/list/publish');
  }

  return (
    <Screen
      footer={
        <>
          <ErrorNote message={error} />
          <div className="screen__footer-row">
            <Link to="/list/rent" className="btn btn--secondary">
              Back
            </Link>
            <button type="button" className="btn btn--primary btn--grow" onClick={next} disabled={!draft || busy}>
              Continue to preview
            </button>
          </div>
        </>
      }
    >
      <StepProgress step={3} close="/landlord" />
      <div className="stack" style={{ gap: 4 }}>
        <h1 className="title title--xl">Six photos, taken now</h1>
        <p className="lead">Open the curtains, switch the light on, nobody in the picture. Tap a block to take that photo.</p>
      </div>
      {!draft ? (
        <Loading error={loading.error} onRetry={loading.reload} />
      ) : (
        <>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, minmax(0, 1fr))', gap: 8 }}>
            {SHOTS.map((s) => {
              const on = !!draft.photos[s];
              return (
                <button
                  key={s}
                  type="button"
                  aria-pressed={on}
                  aria-label={on ? `${s} photo taken. Tap to retake` : `Take the ${s.toLowerCase()} photo`}
                  onClick={() => setDraft({ ...draft, photos: { ...draft.photos, [s]: !on } })}
                  className={on ? 'photo' : undefined}
                  style={{
                    height: 96,
                    borderRadius: 14,
                    border: on ? 0 : '2px dashed var(--line)',
                    background: on ? undefined : 'transparent',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: 6,
                    fontSize: 13,
                    fontWeight: 600,
                  }}
                >
                  {on ? <Icon name="check" size={18} color="var(--green)" strokeWidth={3} /> : <Icon name="camera" size={20} color="var(--muted)" />}
                  {s}
                </button>
              );
            })}
          </div>
          <span className="eyebrow" aria-live="polite">
            {taken} of 6 photos taken
          </span>
          <div className="panel-dark row row--top" style={{ flexDirection: 'row', gap: 12 }}>
            <span className="avatar avatar--amber avatar--sm" aria-hidden="true">
              <Icon name="users" size={16} />
            </span>
            <span className="stack" style={{ gap: 4 }}>
              <strong style={{ fontSize: 14 }}>Not good with the camera?</strong>
              <span className="muted" style={{ fontSize: 13 }}>
                Skip this step. The Hlala checker takes all six photos when they visit.
              </span>
              <Link to="/list/publish" style={{ color: 'var(--amber)', fontWeight: 700, fontSize: 14 }}>
                Let the checker do it
              </Link>
            </span>
          </div>
          <p className="small">Sample mode: taking a photo just marks the block. Real in app capture arrives with file storage.</p>
        </>
      )}
    </Screen>
  );
}

const CHECKER_SLOTS: [string, string][] = [
  ['Wed 18', 'Morning'],
  ['Thu 19', 'Afternoon'],
  ['Sat 21', 'Morning'],
];

export function ListPublish() {
  const navigate = useNavigate();
  const { draft, setDraft, save, busy, error, loading, api } = useDraft();
  const publishing = useAction();

  async function publish() {
    if (!draft || !(await save(draft))) return;
    const done = await publishing.run(() => api.publishListing().then(() => true));
    if (done) navigate('/landlord');
  }

  const extrasLine = draft
    ? EXTRAS.filter(([k]) => draft.extras[k])
        .map(([, label]) => label.replace(' and shower', '').replace(' in rent', ''))
        .slice(0, 3)
        .join(' · ')
    : '';

  return (
    <Screen
      footer={
        <>
          <ErrorNote message={publishing.error ?? error} />
          <div className="screen__footer-row">
            <Link to="/list/photos" className="btn btn--secondary">
              Back
            </Link>
            <button type="button" className="btn btn--primary btn--grow" onClick={publish} disabled={!draft || busy || publishing.busy}>
              Publish my room
            </button>
          </div>
        </>
      }
    >
      <StepProgress step={4} close="/landlord" />
      <h1 className="title title--xl">This is what tenants will see</h1>
      {!draft ? (
        <Loading error={loading.error} onRetry={loading.reload} />
      ) : (
        <>
          <div className="card" style={{ overflow: 'hidden' }}>
            <div className="photo" style={{ height: 140 }}>
              <span>Your door photo</span>
              <span className="tag tag--white" style={{ position: 'absolute', left: 10, top: 10 }}>
                Not checked yet
              </span>
            </div>
            <div className="stack" style={{ padding: '12px 14px', gap: 3 }}>
              <span className="price" style={{ fontSize: 22 }}>
                {draft.rent != null ? formatRand(draft.rent) : 'R?'} <span className="price__unit">per month</span>
              </span>
              <span style={{ fontSize: 14, fontWeight: 600 }}>
                {draft.kind} in {draft.section}
              </span>
              <span className="small">{extrasLine || 'No extras ticked'}</span>
            </div>
          </div>

          <section className="stack" aria-labelledby="when-check">
            <h2 id="when-check" className="caps">
              When can the checker come
            </h2>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, minmax(0, 1fr))', gap: 8 }}>
              {CHECKER_SLOTS.map(([day, part]) => (
                <button
                  key={day}
                  type="button"
                  className="chip"
                  aria-pressed={draft.checkerSlot === day}
                  onClick={() => setDraft({ ...draft, checkerSlot: day })}
                  style={{ minHeight: 60, borderRadius: 14, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}
                >
                  <span style={{ fontSize: 14, fontWeight: 700 }}>{day}</span>
                  <span style={{ fontSize: 12 }}>{part}</span>
                </button>
              ))}
            </div>
          </section>

          <div className="list-card">
            <SwitchRow
              label="Show me people looking for a room like this"
              note="From the room wanted posts"
              checked={draft.showWanted}
              onChange={(v) => setDraft({ ...draft, showWanted: v })}
            />
            <SwitchRow label="Send me requests by SMS too" note="For when you have no data" checked={draft.smsRequests} onChange={(v) => setDraft({ ...draft, smsRequests: v })} />
          </div>
          <span className="small">
            Your room goes live now as "Not checked yet". It gets the Seen in person badge and moves to the top after the visit.
          </span>
        </>
      )}
    </Screen>
  );
}
