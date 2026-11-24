import { formatRand, parseRand } from './money';

describe('formatRand', () => {
  it('groups thousands with a space, the way rent is written', () => {
    expect(formatRand(2200)).toBe('R2 200');
    expect(formatRand(86400)).toBe('R86 400');
    expect(formatRand(1250000)).toBe('R1 250 000');
  });

  it('shows cents only when there are cents', () => {
    expect(formatRand(350)).toBe('R350');
    expect(formatRand(112.5)).toBe('R112.50');
    expect(formatRand(87.505)).toBe('R87.51');
  });

  it('handles zero and negatives', () => {
    expect(formatRand(0)).toBe('R0');
    expect(formatRand(-350)).toBe('-R350');
  });
});

describe('parseRand', () => {
  it('reads what people type', () => {
    expect(parseRand('R2 200')).toBe(2200);
    expect(parseRand('2200')).toBe(2200);
    expect(parseRand(' r1 500 ')).toBe(1500);
    expect(parseRand('112,50')).toBe(112.5);
  });

  it('refuses anything that is not an amount', () => {
    expect(parseRand('')).toBeNull();
    expect(parseRand('two thousand')).toBeNull();
    expect(parseRand('-200')).toBeNull();
    expect(parseRand('12.345')).toBeNull();
  });
});
