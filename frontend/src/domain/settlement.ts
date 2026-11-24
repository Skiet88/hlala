/**
 * Move out: what happens to the deposit when the landlord asks for a deduction.
 * The undisputed part is paid out straight away; a disputed deduction stays
 * frozen for the dispute desk. Amounts must add up to the hold, always.
 */
export type DeductionDecision = 'PENDING' | 'ACCEPTED' | 'DISPUTED';

export interface Settlement {
  held: number;
  deduction: number;
  /** Paid back to the tenant now. */
  toTenantNow: number;
  /** Paid to the landlord now. */
  toLandlordNow: number;
  /** Waits for a decision (the tenant's answer or the dispute desk). */
  waiting: number;
}

export function settle(held: number, deduction: number, decision: DeductionDecision): Settlement {
  if (deduction < 0 || deduction > held) {
    throw new Error('A deduction must be between zero and the amount on hold');
  }
  const undisputed = held - deduction;
  switch (decision) {
    case 'ACCEPTED':
      return { held, deduction, toTenantNow: undisputed, toLandlordNow: deduction, waiting: 0 };
    case 'DISPUTED':
    case 'PENDING':
      return { held, deduction, toTenantNow: undisputed, toLandlordNow: 0, waiting: deduction };
  }
}
