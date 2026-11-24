/**
 * Fair share of a shared prepaid meter, the same two rules the backend's
 * MeterSplitStrategy uses: equal per room, or by number of people.
 */
export type SplitMode = 'EQUAL_PER_ROOM' | 'PER_PERSON';

export interface YardMember {
  id: string;
  name: string;
  people: number;
  /** What this room spent on top ups this month. */
  paid: number;
}

export interface MemberShare {
  id: string;
  name: string;
  people: number;
  paid: number;
  share: number;
  /** Positive: the room is owed money. Negative: the room owes. */
  balance: number;
}

const roundCents = (x: number) => Math.round(x * 100) / 100;

export function splitMeter(members: YardMember[], mode: SplitMode): MemberShare[] {
  const total = members.reduce((sum, m) => sum + m.paid, 0);
  const heads = members.reduce((sum, m) => sum + m.people, 0);
  if (members.length === 0) return [];
  return members.map((m) => {
    const share =
      mode === 'EQUAL_PER_ROOM' ? total / members.length : heads === 0 ? 0 : (total * m.people) / heads;
    return { ...m, share: roundCents(share), balance: roundCents(m.paid - share) };
  });
}
