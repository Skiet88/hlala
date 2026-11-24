import { activeFilterCount, EMPTY_FILTERS, matchesFilters } from './listingFilter';
import { LISTINGS } from '../api/mock/seed';

const flat = LISTINGS.find((l) => l.id === 'l-site-c-1')!; // R2 200, seen, own toilet
const upstairs = LISTINGS.find((l) => l.id === 'l-site-c-3')!; // R2 600, not checked

describe('matchesFilters', () => {
  it('matches everything with no filters', () => {
    expect(matchesFilters(flat, EMPTY_FILTERS)).toBe(true);
  });

  it('filters by section, rent, seen in person and must haves', () => {
    expect(matchesFilters(flat, EMPTY_FILTERS, 'Harare')).toBe(false);
    expect(matchesFilters(upstairs, { ...EMPTY_FILTERS, maxRent: 2500 })).toBe(false);
    expect(matchesFilters(flat, { ...EMPTY_FILTERS, minRent: 2300 })).toBe(false);
    expect(matchesFilters(upstairs, { ...EMPTY_FILTERS, seenOnly: true })).toBe(false);
    expect(matchesFilters(flat, { ...EMPTY_FILTERS, mustHave: ['Own toilet', 'Burglar gate'] })).toBe(true);
    expect(matchesFilters(flat, { ...EMPTY_FILTERS, mustHave: ['Furnished'] })).toBe(false);
  });

  it('matches a household if the room suits any chosen one', () => {
    expect(matchesFilters(flat, { ...EMPTY_FILTERS, household: ['Family', 'Couple'] })).toBe(true);
    expect(matchesFilters(flat, { ...EMPTY_FILTERS, household: ['Family'] })).toBe(false);
  });
});

describe('activeFilterCount', () => {
  it('counts each switched on filter once', () => {
    expect(activeFilterCount(EMPTY_FILTERS)).toBe(0);
    expect(activeFilterCount({ ...EMPTY_FILTERS, maxRent: 2500, mustHave: ['Own toilet', 'Wi-Fi'], seenOnly: true })).toBe(4);
  });
});
