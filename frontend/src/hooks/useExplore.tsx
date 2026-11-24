import { createContext, useContext, useState, type ReactNode } from 'react';
import type { ListingFilters } from '../api/types';
import { EMPTY_FILTERS } from '../domain/listingFilter';

interface ExploreState {
  section: string;
  setSection: (s: string) => void;
  filters: ListingFilters;
  setFilters: (f: ListingFilters) => void;
  query: string;
  setQuery: (q: string) => void;
}

const ExploreContext = createContext<ExploreState | null>(null);

/** Search state shared by Explore, Filters and the map, so they agree. */
export function ExploreProvider({ children }: { children: ReactNode }) {
  const [section, setSection] = useState('Site C');
  const [filters, setFilters] = useState<ListingFilters>(EMPTY_FILTERS);
  const [query, setQuery] = useState('');
  return (
    <ExploreContext.Provider value={{ section, setSection, filters, setFilters, query, setQuery }}>
      {children}
    </ExploreContext.Provider>
  );
}

export function useExplore(): ExploreState {
  const s = useContext(ExploreContext);
  if (!s) throw new Error('useExplore must be used inside ExploreProvider');
  return s;
}
