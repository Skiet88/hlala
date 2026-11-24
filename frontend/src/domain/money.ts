/**
 * Rand amounts the way people write them ekasi: "R2 200", "R112.50".
 * A plain space groups thousands (not a comma, not a non breaking space),
 * cents show only when there are cents.
 */
export function formatRand(amount: number): string {
  const negative = amount < 0;
  const abs = Math.abs(amount);
  const cents = Math.round(abs * 100) % 100;
  const whole = Math.floor(Math.round(abs * 100) / 100);
  const grouped = String(whole).replace(/\B(?=(\d{3})+(?!\d))/g, ' ');
  const text = cents === 0 ? `R${grouped}` : `R${grouped}.${String(cents).padStart(2, '0')}`;
  return negative ? `-${text}` : text;
}

/** Reads what a person typed ("R2 200", "2200", "2 200.50") as rands, or null. */
export function parseRand(input: string): number | null {
  const cleaned = input.replace(/[Rr\s]/g, '').replace(',', '.');
  if (cleaned === '' || !/^\d+(\.\d{1,2})?$/.test(cleaned)) return null;
  return Number(cleaned);
}
