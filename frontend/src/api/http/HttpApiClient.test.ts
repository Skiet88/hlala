import { createHttpApiClient, toListingDetail } from './HttpApiClient';
import { NotAvailableYetError } from '../ApiClient';
import { EMPTY_FILTERS } from '../../domain/listingFilter';

const detailDto = {
  id: 'abc',
  rentAmount: 1500,
  depositAmount: 1500,
  status: 'LIVE',
  verificationStatus: 'VERIFIED' as const,
  publishedAt: '2026-11-20T10:00:00Z',
  room: {
    id: 'room-1',
    area: 'Khayelitsha Site C',
    landmarkNote: 'Behind the spaza near the taxi rank',
    type: 'BACKYARD_ROOM' as const,
    amenities: ['PRIVATE_TOILET' as const, 'ELECTRICITY_PREPAID' as const, 'PRIVATE_ENTRANCE' as const],
    occupancyMode: 'SOLE' as const,
  },
};

function respond(body: unknown, status = 200) {
  return Promise.resolve(new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } }));
}

describe('HttpApiClient', () => {
  afterEach(() => vi.unstubAllGlobals());

  it('maps the backend listing into what the screens show', () => {
    const l = toListingDetail(detailDto);
    expect(l.kind).toBe('Backyard room');
    expect(l.seenInPerson).toBe(true);
    expect(l.features).toEqual(['Own toilet', 'Own prepaid meter']);
    expect(l.rent).toBe(1500);
  });

  it('never shows an exact pin: the public listing has none to show', () => {
    const l = toListingDetail(detailDto);
    expect(JSON.stringify(l)).not.toMatch(/latitude|longitude/);
  });

  it('searches with the filters the backend understands', async () => {
    const fetchMock = vi.fn((url: string) =>
      url.includes('/listings?') ? respond([{ id: 'abc', roomId: 'room-1', rentAmount: 1500, status: 'LIVE', seenInPerson: true }]) : respond(detailDto),
    );
    vi.stubGlobal('fetch', fetchMock);
    const api = createHttpApiClient('https://example.test/');
    const found = await api.searchListings('Site C', { ...EMPTY_FILTERS, maxRent: 2000, seenOnly: true });
    expect(found).toHaveLength(1);
    expect(fetchMock.mock.calls[0][0]).toBe('https://example.test/listings?area=Site+C&maxRent=2000&verifiedOnly=true');
  });

  it('passes the server message on when a call fails', async () => {
    vi.stubGlobal('fetch', () => respond({ message: 'Listing not found' }, 404));
    await expect(createHttpApiClient('https://example.test').getListing('x')).rejects.toThrow('Listing not found');
  });

  it('says plainly that signed in calls wait for Phase 2', async () => {
    const api = createHttpApiClient('https://example.test');
    await expect(api.landlordHome()).rejects.toBeInstanceOf(NotAvailableYetError);
    await expect(api.payIntoHold('TILL')).rejects.toThrow(/Phase 2/);
  });
});
