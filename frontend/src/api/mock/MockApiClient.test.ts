import { MockApiClient } from './MockApiClient';
import { EMPTY_FILTERS } from '../../domain/listingFilter';

const api = () => new MockApiClient(0);

describe('MockApiClient keeps the product rules', () => {
  it('will not release the deposit until every move in item is ticked', async () => {
    const a = api();
    await expect(a.confirmMovedIn()).rejects.toThrow(/Tick every item/);
    const day = await a.moveInDay();
    for (const item of day.checklist) await a.setMoveInItem(item.id, true);
    await expect(a.confirmMovedIn()).resolves.toMatchObject({ state: 'ACTIVE' });
  });

  it('signing the lease ticks the lease item on move in day', async () => {
    const a = api();
    await a.signLease('lease-1');
    const day = await a.moveInDay();
    expect(day.checklist.find((i) => i.id === 'lease')!.done).toBe(true);
  });

  it('a checker cannot award the badge with items unticked', async () => {
    const a = api();
    await expect(a.submitCheck(true)).rejects.toThrow(/Every item/);
    await expect(a.submitCheck(false)).resolves.toBeUndefined();
  });

  it('recording cash moves the room from late to paid and updates the landlord total', async () => {
    const a = api();
    const before = await a.landlordHome();
    const book = await a.recordCash('lease-r3');
    expect(book.rows.find((r) => r.leaseId === 'lease-r3')!.status).toBe('PAID');
    const after = await a.landlordHome();
    expect(after.collected).toBe(before.collected + 2200);
    await expect(a.recordCash('lease-r3')).rejects.toThrow(/Nothing owed/);
  });

  it('a listing needs rent and deposit before it can be published', async () => {
    const a = api();
    await expect(a.publishListing()).rejects.toThrow(/rent and the deposit/);
    const draft = await a.listingDraft();
    await a.saveListingDraft({ ...draft, rent: 1700, deposit: 1700, section: 'Harare' });
    await a.publishListing();
    const found = await a.searchListings('Harare', EMPTY_FILTERS);
    expect(found.some((l) => l.rent === 1700 && !l.seenInPerson)).toBe(true);
  });

  it('returns copies, so a screen cannot change the store by accident', async () => {
    const a = api();
    const first = await a.landlordHome();
    first.collected = 0;
    expect((await a.landlordHome()).collected).toBe(4200);
  });

  it('checks the cell number and the code format before sign in', async () => {
    const a = api();
    await expect(a.requestCode('12345')).rejects.toThrow(/10 digit/);
    await expect(a.requestCode('071 234 5678')).resolves.toBeUndefined();
    await expect(a.verifyCode('0712345678', '12', 'TENANT')).rejects.toThrow(/6 numbers/);
    await expect(a.verifyCode('0712345678', '123456', 'LANDLORD')).resolves.toMatchObject({ roles: ['LANDLORD'] });
  });

  it('two instances never share state', async () => {
    const a = api();
    const b = api();
    await a.recordCash('lease-r3');
    expect((await b.rentBook()).rows.find((r) => r.leaseId === 'lease-r3')!.status).toBe('LATE');
  });
});
