import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useApi } from '../../api/ApiProvider';
import { useExplore } from '../../hooks/useExplore';
import { useLoad } from '../../hooks/useLoad';
import { formatRand } from '../../domain/money';
import { Icon } from '../../components/Icon';
import { BackButton, Loading, Photo, useToast } from '../../components/ui';

/**
 * The prototype's map, drawn in CSS: no map library and no tiles to download,
 * which keeps it light on data. A real map (and walking routes) is a later
 * decision; pins come from each listing's position.
 */
export function MapView() {
  const api = useApi();
  const { section, filters } = useExplore();
  const results = useLoad(() => api.searchListings(section, filters), [section, filters]);
  const [picked, setPicked] = useState<string | null>(null);
  const [toast, showToast] = useToast();
  const list = results.data ?? [];
  const selected = list.find((l) => l.id === picked) ?? list[0];

  return (
    <div className="screen screen--map">
      <div className="map" role="region" aria-label={`Map of rooms in ${section}`}>
        <div className="map__road" aria-hidden="true" style={{ left: '-10%', top: '25%', width: '120%', height: 18, transform: 'rotate(-14deg)' }} />
        <div className="map__road" aria-hidden="true" style={{ left: '-10%', top: '52%', width: '120%', height: 12, transform: 'rotate(-14deg)' }} />
        <div className="map__road" aria-hidden="true" style={{ left: '28%', top: '-5%', width: 14, height: '110%', transform: 'rotate(-14deg)' }} />
        <div className="map__road" aria-hidden="true" style={{ left: '72%', top: '-5%', width: 10, height: '110%', transform: 'rotate(-14deg)' }} />
        <span className="map__caption">Map placeholder: {section}</span>

        <div className="map__top">
          <BackButton to="/explore" label="Back to the tile view" />
          <div className="pill-btn" style={{ flex: 1 }}>
            {section}
            {filters.maxRent != null && ` · under ${formatRand(filters.maxRent)}`}
          </div>
          <Link to="/explore" className="pill-btn pill-btn--dark">
            Tiles
          </Link>
        </div>

        <span className="map__landmark" style={{ left: '48%', top: '33%' }}>
          <Icon name="taxi" size={14} color="var(--amber)" strokeWidth={2.4} />
          Taxi rank
        </span>
        <span className="map__landmark" style={{ left: '76%', top: '66%' }}>
          <Icon name="train" size={14} color="var(--amber)" strokeWidth={2.4} />
          Station
        </span>

        {list.map((l) => (
          <button
            key={l.id}
            type="button"
            className="map__pin"
            aria-pressed={selected?.id === l.id}
            aria-label={`${formatRand(l.rent)}, ${l.title}`}
            style={{ left: `${l.map.x}%`, top: `${l.map.y}%` }}
            onClick={() => setPicked(l.id)}
          >
            {formatRand(l.rent)}
          </button>
        ))}
      </div>

      <div className="sheet">
        {results.loading && !results.data ? (
          <Loading error={results.error} onRetry={results.reload} />
        ) : !selected ? (
          <p className="lead">No rooms on the map with these filters.</p>
        ) : (
          <>
            <div className="row" style={{ gap: 12 }}>
              <Photo height={84} width={84} radius={12} />
              <div className="stack" style={{ gap: 3, flex: 1, minWidth: 0 }}>
                <span className="price" style={{ fontSize: 20 }}>
                  {formatRand(selected.rent)} <span className="price__unit">per month</span>
                </span>
                <span style={{ fontSize: 14, fontWeight: 600 }}>{selected.title}</span>
                <span className="small">
                  {selected.seenInPerson ? 'Seen in person' : 'Not checked yet'} · {selected.nearest.minutes} min walk to {selected.nearest.place}
                </span>
              </div>
            </div>
            <div className="screen__footer-row">
              <button type="button" className="btn btn--secondary btn--md" onClick={() => showToast('Walking routes come with the real map')}>
                Walking route
              </button>
              <Link to={`/listings/${selected.id}`} className="btn btn--primary btn--md btn--grow">
                View room
              </Link>
            </div>
          </>
        )}
      </div>
      {toast}
    </div>
  );
}
