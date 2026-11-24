import type { ListingSummary, ListingFilters, Feature, PlaceKind } from '../api/types';

export const EMPTY_FILTERS: ListingFilters = {
  minRent: null,
  maxRent: null,
  kinds: [],
  mustHave: [],
  household: [],
  seenOnly: false,
};

/** How many filters are switched on, for the badge on the Filters button. */
export function activeFilterCount(f: ListingFilters): number {
  return (
    (f.minRent != null ? 1 : 0) +
    (f.maxRent != null ? 1 : 0) +
    f.kinds.length +
    f.mustHave.length +
    f.household.length +
    (f.seenOnly ? 1 : 0)
  );
}

export function matchesFilters(listing: ListingSummary, f: ListingFilters, section?: string): boolean {
  if (section && listing.section !== section) return false;
  if (f.minRent != null && listing.rent < f.minRent) return false;
  if (f.maxRent != null && listing.rent > f.maxRent) return false;
  if (f.kinds.length > 0 && !f.kinds.includes(listing.kind as PlaceKind)) return false;
  if (f.mustHave.some((need: Feature) => !listing.features.includes(need))) return false;
  if (f.household.length > 0 && !f.household.some((h) => listing.suits.includes(h))) return false;
  if (f.seenOnly && !listing.seenInPerson) return false;
  return true;
}
