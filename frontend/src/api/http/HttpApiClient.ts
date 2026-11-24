import { ConflictError, NotAvailableYetError, type ApiClient } from '../ApiClient';
import type { Feature, ListingDetail, ListingFilters, ListingSummary, PlaceKind } from '../types';
import { matchesFilters } from '../../domain/listingFilter';

// Shapes the backend sends today (com.hlala.schemas.ListingSchemas).
type RoomType = 'BACKYARD_ROOM' | 'GRANNY_FLAT' | 'SHARED_ROOM' | 'FULL_UNIT';
type Amenity =
  | 'PRIVATE_ENTRANCE'
  | 'PRIVATE_TOILET'
  | 'SHARED_TOILET'
  | 'WATER_INSIDE'
  | 'ELECTRICITY_PREPAID'
  | 'ELECTRICITY_INCLUDED'
  | 'WIFI'
  | 'SECURE_YARD'
  | 'PARKING'
  | 'FURNISHED';

interface ListingSummaryDto {
  id: string;
  roomId: string;
  rentAmount: number;
  status: string;
  seenInPerson: boolean;
}

interface ListingDetailDto {
  id: string;
  rentAmount: number;
  depositAmount: number;
  status: string;
  verificationStatus: 'UNVERIFIED' | 'PENDING_CHECK' | 'VERIFIED';
  publishedAt: string | null;
  room: {
    id: string;
    area: string;
    landmarkNote: string | null;
    type: RoomType;
    amenities: Amenity[];
    occupancyMode: 'SOLE' | 'SHARED';
  };
}

const KIND: Record<RoomType, PlaceKind> = {
  BACKYARD_ROOM: 'Backyard room',
  GRANNY_FLAT: 'Backyard flat',
  SHARED_ROOM: 'Room in a house',
  FULL_UNIT: 'Whole house',
};

const FEATURE: Partial<Record<Amenity, Feature>> = {
  PRIVATE_TOILET: 'Own toilet',
  ELECTRICITY_PREPAID: 'Own prepaid meter',
  WATER_INSIDE: 'Water included',
  SECURE_YARD: 'Burglar gate',
  FURNISHED: 'Furnished',
  WIFI: 'Wi-Fi',
  PARKING: 'Parking',
};

export function toListingDetail(dto: ListingDetailDto): ListingDetail {
  const features = dto.room.amenities.map((a) => FEATURE[a]).filter((f): f is Feature => f != null);
  const landmark = dto.room.landmarkNote ?? dto.room.area;
  const seen = dto.verificationStatus === 'VERIFIED';
  return {
    id: dto.id,
    title: KIND[dto.room.type],
    area: dto.room.area,
    section: dto.room.area,
    kind: KIND[dto.room.type],
    rent: Number(dto.rentAmount),
    deposit: Number(dto.depositAmount),
    seenInPerson: seen,
    features,
    suits: dto.room.occupancyMode === 'SHARED' ? ['Just me', 'Students'] : ['Just me', 'Couple'],
    meta: [features[0], seen ? 'Seen in person' : 'Not checked yet'].filter(Boolean).join(' · '),
    // The backend has no walking times or map positions yet (see ROADMAP).
    nearest: { minutes: 0, place: landmark },
    map: { x: 50, y: 50 },
    availableFrom: 'Ask the landlord',
    whoFor: dto.room.occupancyMode === 'SHARED' ? 'Shared room' : 'Ask the landlord',
    checkedOn: null,
    photoCount: 0,
    gettingAround: [],
    landlord: { name: 'Landlord', initial: 'L', note: 'Details after you book a viewing' },
  };
}

/** The calls the backend serves without sign in today. */
class PublicCalls {
  constructor(
    private readonly baseUrl: string,
    private readonly timeoutMs = 10_000,
  ) {}

  private async get<T>(path: string): Promise<T> {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), this.timeoutMs);
    try {
      const response = await fetch(`${this.baseUrl}${path}`, {
        signal: controller.signal,
        headers: { Accept: 'application/json' },
        credentials: 'omit',
      });
      if (response.status === 409) throw new ConflictError('Someone changed this at the same moment. Please try again');
      if (!response.ok) {
        const body = (await response.json().catch(() => null)) as { message?: string } | null;
        throw new Error(body?.message ?? `The server said ${response.status}`);
      }
      return (await response.json()) as T;
    } catch (e) {
      if (e instanceof DOMException && e.name === 'AbortError') {
        throw new Error('The signal is weak and the server did not answer. Try again');
      }
      throw e;
    } finally {
      clearTimeout(timer);
    }
  }

  async sections(): Promise<string[]> {
    return ['Site C', 'Harare', 'Makhaza', 'Kuyasa', 'Site B', 'Langa'];
  }

  async searchListings(section: string | null, filters: ListingFilters, query = ''): Promise<ListingSummary[]> {
    const params = new URLSearchParams();
    if (section) params.set('area', section);
    if (filters.maxRent != null) params.set('maxRent', String(filters.maxRent));
    if (filters.seenOnly) params.set('verifiedOnly', 'true');
    const summaries = await this.get<ListingSummaryDto[]>(`/listings?${params}`);
    // The summary has no room details yet, so each one is loaded in full.
    const details = await Promise.all(summaries.map((s) => this.getListing(s.id)));
    const q = query.trim().toLowerCase();
    return details
      .filter((d) => matchesFilters(d, { ...filters, maxRent: null, seenOnly: false }))
      .filter((d) => q === '' || `${d.title} ${d.area}`.toLowerCase().includes(q));
  }

  async getListing(id: string): Promise<ListingDetail> {
    return toListingDetail(await this.get<ListingDetailDto>(`/listings/${encodeURIComponent(id)}`));
  }
}

/**
 * The real backend. Only public listing browsing works until sign in lands in
 * Phase 2; every other call fails with a clear NotAvailableYetError instead of
 * pretending.
 */
export function createHttpApiClient(baseUrl: string): ApiClient {
  const calls = new PublicCalls(baseUrl.replace(/\/$/, ''));
  return new Proxy(calls, {
    get(target, prop, receiver) {
      if (prop in target) {
        const value = Reflect.get(target, prop, receiver);
        return typeof value === 'function' ? value.bind(target) : value;
      }
      return () => Promise.reject(new NotAvailableYetError(String(prop)));
    },
  }) as unknown as ApiClient;
}
