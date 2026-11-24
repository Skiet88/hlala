import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApi } from '../../api/ApiProvider';
import type { Feature, Household, ListingFilters, PlaceKind } from '../../api/types';
import { useExplore } from '../../hooks/useExplore';
import { useAction } from '../../hooks/useLoad';
import { EMPTY_FILTERS } from '../../domain/listingFilter';
import { formatRand, parseRand } from '../../domain/money';
import { Chip, ErrorNote, BackButton, Screen, Switch } from '../../components/ui';

const KINDS: PlaceKind[] = ['Backyard room', 'Backyard flat', 'Room in a house', 'Whole house'];
const MUST: Feature[] = ['Own toilet', 'Own prepaid meter', 'Water included', 'Burglar gate', 'Furnished', 'Wi-Fi', 'Geyser', 'Parking'];
const WHO: Household[] = ['Just me', 'Couple', 'Family', 'Students'];

function toggle<T>(list: T[], item: T): T[] {
  return list.includes(item) ? list.filter((x) => x !== item) : [...list, item];
}

export function Filters() {
  const api = useApi();
  const navigate = useNavigate();
  const { section, filters, setFilters } = useExplore();
  const [draft, setDraft] = useState<ListingFilters>(filters);
  const [minText, setMinText] = useState(filters.minRent != null ? formatRand(filters.minRent) : '');
  const [maxText, setMaxText] = useState(filters.maxRent != null ? formatRand(filters.maxRent) : '');
  const { busy, error, run } = useAction();

  const withRent = (): ListingFilters => ({ ...draft, minRent: parseRand(minText), maxRent: parseRand(maxText) });

  function apply() {
    setFilters(withRent());
    navigate('/explore');
  }

  async function saveAlert() {
    const done = await run(() => api.saveFiltersAsAlert(section, withRent()));
    if (done) {
      setFilters(withRent());
      navigate('/saved');
    }
  }

  return (
    <Screen
      footer={
        <>
          <ErrorNote message={error} />
          <div className="screen__footer-row">
            <button type="button" className="btn btn--secondary" onClick={saveAlert} disabled={busy}>
              Save as alert
            </button>
            <button type="button" className="btn btn--primary btn--grow" onClick={apply}>
              Show rooms
            </button>
          </div>
        </>
      }
    >
      <div className="topbar topbar--split">
        <BackButton to="/explore" label="Close filters" icon="close" />
        <h1 className="title">Filters</h1>
        <button
          type="button"
          className="link-btn"
          onClick={() => {
            setDraft(EMPTY_FILTERS);
            setMinText('');
            setMaxText('');
          }}
        >
          Clear all
        </button>
      </div>

      <div className="grid-2">
        <div className="field">
          <label htmlFor="min">Rent from</label>
          <input id="min" className="input" inputMode="numeric" placeholder="R1 500" value={minText} onChange={(e) => setMinText(e.target.value)} />
        </div>
        <div className="field">
          <label htmlFor="max">Rent up to</label>
          <input id="max" className="input" inputMode="numeric" placeholder="R2 500" value={maxText} onChange={(e) => setMaxText(e.target.value)} />
        </div>
      </div>

      <section className="stack" aria-labelledby="f-kind">
        <h2 id="f-kind" className="caps">
          Type of place
        </h2>
        <div className="chip-row">
          {KINDS.map((k) => (
            <Chip key={k} label={k} pressed={draft.kinds.includes(k)} onClick={() => setDraft({ ...draft, kinds: toggle(draft.kinds, k) })} />
          ))}
        </div>
      </section>

      <section className="stack" aria-labelledby="f-must">
        <h2 id="f-must" className="caps">
          Must have
        </h2>
        <div className="chip-row">
          {MUST.map((k) => (
            <Chip key={k} label={k} pressed={draft.mustHave.includes(k)} onClick={() => setDraft({ ...draft, mustHave: toggle(draft.mustHave, k) })} />
          ))}
        </div>
      </section>

      <section className="stack" aria-labelledby="f-who">
        <h2 id="f-who" className="caps">
          Who is moving in
        </h2>
        <div className="chip-row">
          {WHO.map((k) => (
            <Chip key={k} label={k} pressed={draft.household.includes(k)} onClick={() => setDraft({ ...draft, household: toggle(draft.household, k) })} />
          ))}
        </div>
      </section>

      <div className="card row row--between" style={{ minHeight: 56, padding: '0 14px', borderRadius: 14 }}>
        <span style={{ fontSize: 14, fontWeight: 600 }}>Only rooms seen in person</span>
        <Switch label="Only rooms seen in person" checked={draft.seenOnly} onChange={(v) => setDraft({ ...draft, seenOnly: v })} />
      </div>
    </Screen>
  );
}
