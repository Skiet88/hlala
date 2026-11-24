import { settle } from './settlement';

describe('settle (move out deposit)', () => {
  it('pays the undisputed part now and holds the rest while pending', () => {
    expect(settle(2200, 350, 'PENDING')).toEqual({ held: 2200, deduction: 350, toTenantNow: 1850, toLandlordNow: 0, waiting: 350 });
  });

  it('pays the deduction to the landlord when the tenant accepts it', () => {
    const s = settle(2200, 350, 'ACCEPTED');
    expect(s.toLandlordNow).toBe(350);
    expect(s.waiting).toBe(0);
  });

  it('freezes a disputed deduction for the dispute desk', () => {
    const s = settle(2200, 350, 'DISPUTED');
    expect(s.toTenantNow).toBe(1850);
    expect(s.waiting).toBe(350);
  });

  it('always adds up to the amount on hold', () => {
    for (const d of ['PENDING', 'ACCEPTED', 'DISPUTED'] as const) {
      const s = settle(2200, 350, d);
      expect(s.toTenantNow + s.toLandlordNow + s.waiting).toBe(s.held);
    }
  });

  it('refuses a deduction bigger than the hold or below zero', () => {
    expect(() => settle(2200, 2500, 'PENDING')).toThrow();
    expect(() => settle(2200, -1, 'PENDING')).toThrow();
  });
});
