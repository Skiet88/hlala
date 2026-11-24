import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useApi } from '../../api/ApiProvider';
import { useExplore } from '../../hooks/useExplore';
import { useLoad } from '../../hooks/useLoad';
import { activeFilterCount } from '../../domain/listingFilter';
import { Icon } from '../../components/Icon';
import { TenantNav } from '../../components/BottomNav';
import { ListingTile } from '../../components/ListingTile';
import { Chip, Loading, Screen } from '../../components/ui';

const QUICK = ['Site C', 'Harare', 'Makhaza', 'Kuyasa'];

export function Explore() {
  const api = useApi();
  const { section, setSection, filters, query, setQuery } = useExplore();
  const [sort, setSort] = useState<'closest' | 'cheapest'>('closest');
  const sections = useLoad(() => api.sections(), []);
  const results = useLoad(() => api.searchListings(section, filters, query), [section, filters, query]);
  const count = activeFilterCount(filters);

  const sorted = useMemo(() => {
    const list = [...(results.data ?? [])];
    return sort === 'cheapest' ? list.sort((a, b) => a.rent - b.rent) : list;
  }, [results.data, sort]);

  return (
    <Screen nav={<TenantNav />}>
      <div className="topbar topbar--split">
        <label className="pill-btn" style={{ position: 'relative', minWidth: 0, flex: '0 1 auto' }}>
          <Icon name="pin" size={16} color="var(--green)" />
          <span className="visually-hidden">Area</span>
          <select
            value={section}
            onChange={(e) => setSection(e.target.value)}
            style={{ border: 0, background: 'transparent', fontWeight: 600, fontSize: 14, appearance: 'none', paddingRight: 18, minWidth: 0 }}
          >
            {(sections.data ?? QUICK).map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
          <span style={{ position: 'absolute', right: 12, pointerEvents: 'none' }}>
            <Icon name="chevronDown" size={14} />
          </span>
        </label>
        <div className="row" style={{ gap: 8 }}>
          <Link to="/map" className="icon-btn" aria-label="Show rooms on a map">
            <Icon name="map" size={18} strokeWidth={2} />
          </Link>
          <Link to="/filters" className="pill-btn pill-btn--dark" aria-label={`Filters, ${count} active`}>
            <Icon name="sliders" size={16} />
            Filters
            {count > 0 && <span className="count-dot">{count}</span>}
          </Link>
        </div>
      </div>

      <div className="search-box">
        <Icon name="search" size={18} color="var(--muted)" />
        <label htmlFor="q" className="visually-hidden">
          Search by section or landmark
        </label>
        <input id="q" type="search" placeholder="Section, station or taxi rank" value={query} onChange={(e) => setQuery(e.target.value)} />
      </div>

      <div className="chip-grid" role="group" aria-label="Section">
        {QUICK.map((s) => (
          <Chip key={s} label={s} pressed={section === s} onClick={() => setSection(s)} />
        ))}
      </div>

      <div className="row row--between">
        <h1 className="title" style={{ fontSize: 18 }}>
          Rooms in {section}
          {results.data && <span className="subtitle"> · {results.data.length}</span>}
        </h1>
        <button type="button" className="link-btn" onClick={() => setSort(sort === 'closest' ? 'cheapest' : 'closest')}>
          Sort: {sort === 'closest' ? 'closest to rank' : 'cheapest first'}
        </button>
      </div>

      {results.loading && !results.data ? (
        <Loading error={results.error} onRetry={results.reload} />
      ) : results.error ? (
        <Loading error={results.error} onRetry={results.reload} />
      ) : sorted.length === 0 ? (
        <div className="state">
          <p>No rooms match in {section} right now.</p>
          <Link to="/filters" className="btn btn--secondary btn--sm">
            Change filters
          </Link>
          <Link to="/wanted" className="link-btn">
            Post a room wanted instead
          </Link>
        </div>
      ) : (
        <div className="tile-grid">
          {sorted.map((l) => (
            <ListingTile key={l.id} listing={l} />
          ))}
        </div>
      )}
    </Screen>
  );
}
