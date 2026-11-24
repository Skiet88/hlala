/** The deposit hold states from the backend, in words a tenant understands. */
export type DepositHoldState =
  | 'PENDING'
  | 'HELD'
  | 'ACTIVE'
  | 'DISPUTED'
  | 'RELEASED'
  | 'REFUNDED'
  | 'PARTIALLY_SETTLED';

const WORDS: Record<DepositHoldState, string> = {
  PENDING: 'Waiting for your payment',
  HELD: 'On hold until you have the keys',
  ACTIVE: 'Held while you live there',
  DISPUTED: 'Frozen while a case is open',
  RELEASED: 'Paid to the landlord',
  REFUNDED: 'Paid back to you',
  PARTIALLY_SETTLED: 'Split after the case was decided',
};

export function depositStateWords(state: DepositHoldState): string {
  return WORDS[state];
}

/** Money can still move from this state; the last three are final. */
export function isFinal(state: DepositHoldState): boolean {
  return state === 'RELEASED' || state === 'REFUNDED' || state === 'PARTIALLY_SETTLED';
}
