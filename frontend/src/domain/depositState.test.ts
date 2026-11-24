import { depositStateWords, isFinal } from './depositState';

describe('deposit states', () => {
  it('only the three end states are final, matching the backend transition table', () => {
    expect(isFinal('RELEASED')).toBe(true);
    expect(isFinal('REFUNDED')).toBe(true);
    expect(isFinal('PARTIALLY_SETTLED')).toBe(true);
    expect(isFinal('HELD')).toBe(false);
    expect(isFinal('DISPUTED')).toBe(false);
  });

  it('describes a hold in plain words', () => {
    expect(depositStateWords('HELD')).toBe('On hold until you have the keys');
  });
});
