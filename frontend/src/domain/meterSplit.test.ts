import { splitMeter, type YardMember } from './meterSplit';

// The yard from the prototype: R350 bought, four households.
const yard: YardMember[] = [
  { id: 'r1', name: 'Room 1', people: 1, paid: 0 },
  { id: 'r2', name: 'Room 2', people: 2, paid: 150 },
  { id: 'r3', name: 'Room 3', people: 1, paid: 200 },
  { id: 'main', name: 'Main house', people: 3, paid: 0 },
];

describe('splitMeter', () => {
  it('splits equally per room', () => {
    const shares = splitMeter(yard, 'EQUAL_PER_ROOM');
    expect(shares.map((s) => s.share)).toEqual([87.5, 87.5, 87.5, 87.5]);
    expect(shares.find((s) => s.id === 'r3')!.balance).toBe(112.5); // "you are owed R112.50"
    expect(shares.find((s) => s.id === 'r1')!.balance).toBe(-87.5);
  });

  it('splits by number of people', () => {
    const shares = splitMeter(yard, 'PER_PERSON');
    expect(shares.find((s) => s.id === 'main')!.share).toBe(150);
    expect(shares.find((s) => s.id === 'r2')!.share).toBe(100);
  });

  it('balances always add up to zero: nobody is short changed', () => {
    for (const mode of ['EQUAL_PER_ROOM', 'PER_PERSON'] as const) {
      const sum = splitMeter(yard, mode).reduce((s, m) => s + m.balance, 0);
      expect(Math.abs(sum)).toBeLessThan(0.02);
    }
  });

  it('copes with an empty yard', () => {
    expect(splitMeter([], 'EQUAL_PER_ROOM')).toEqual([]);
  });
});
