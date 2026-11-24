import type {
  AdminOverview,
  Application,
  AppNotification,
  ChatThread,
  CheckerJob,
  DepositHold,
  DisputeCase,
  LandlordHome,
  LeaseDoc,
  ListingDetail,
  ListingFilters,
  ListingSummary,
  MeterYard,
  MoveInDay,
  MoveOutCheck,
  MyHome,
  NewListingDraft,
  PaymentQuote,
  PayMethodId,
  RentBook,
  RenterProfile,
  RepairTicket,
  ReportReason,
  RequestsForRoom,
  ReviewDraft,
  Role,
  SavedRoom,
  SearchAlert,
  Session,
  Viewing,
  ViewingSlot,
  WantedPost,
} from './types';
import type { SplitMode } from '../domain/meterSplit';
import type { DeductionDecision } from '../domain/settlement';

/**
 * Everything the screens can ask of the server. Screens only see this
 * interface; which implementation runs is chosen once, in ApiProvider.
 *
 * MockApiClient: every method, on sample data, so the whole app can be
 * clicked through before real sign in exists (Phase 2).
 * HttpApiClient: the calls the backend can serve today.
 */
export interface ApiClient {
  // sign in (mocked until Phase 2: phone number and one time PIN)
  requestCode(phone: string): Promise<void>;
  verifyCode(phone: string, code: string, role: Role): Promise<Session>;

  // explore
  sections(): Promise<string[]>;
  searchListings(section: string | null, filters: ListingFilters, query?: string): Promise<ListingSummary[]>;
  getListing(id: string): Promise<ListingDetail>;
  viewingSlots(listingId: string): Promise<ViewingSlot[]>;
  bookViewing(listingId: string, slot: ViewingSlot): Promise<Viewing>;
  isSaved(listingId: string): Promise<boolean>;
  toggleSaved(listingId: string): Promise<boolean>;

  // chat
  chatThread(): Promise<ChatThread>;
  sendMessage(threadId: string, text: string): Promise<ChatThread>;

  // apply, pay, move in
  renterProfile(): Promise<RenterProfile>;
  apply(listingId: string, household: string, moveIn: string): Promise<Application>;
  paymentQuote(): Promise<PaymentQuote>;
  payIntoHold(method: PayMethodId): Promise<DepositHold>;
  moveInDay(): Promise<MoveInDay>;
  setMoveInItem(itemId: string, done: boolean): Promise<MoveInDay>;
  addConditionPhoto(label: string): Promise<MoveInDay>;
  confirmMovedIn(): Promise<DepositHold>;

  // living there
  myHome(): Promise<MyHome>;
  repair(id: string): Promise<RepairTicket>;
  confirmRepair(id: string, fixed: boolean): Promise<RepairTicket>;
  meterYard(): Promise<MeterYard>;
  setSplitMode(mode: SplitMode): Promise<MeterYard>;
  logTopUp(amount: number): Promise<MeterYard>;
  moveOutCheck(): Promise<MoveOutCheck>;
  decideDeduction(decision: DeductionDecision): Promise<MoveOutCheck>;
  reviewDraft(): Promise<ReviewDraft>;
  postReview(leaseId: string, stars: number, tags: string[], words: string): Promise<void>;

  // saved, alerts, notifications, wanted
  savedRooms(): Promise<SavedRoom[]>;
  alerts(): Promise<SearchAlert[]>;
  setAlert(id: string, on: boolean): Promise<SearchAlert[]>;
  saveFiltersAsAlert(section: string, filters: ListingFilters): Promise<SearchAlert[]>;
  notifications(): Promise<AppNotification[]>;
  markAllRead(): Promise<AppNotification[]>;
  wantedPost(): Promise<WantedPost>;
  hideOffer(offerId: string): Promise<WantedPost>;
  setWantedLive(live: boolean): Promise<WantedPost>;

  // landlord
  landlordHome(): Promise<LandlordHome>;
  remindTenant(roomNumber: number): Promise<void>;
  viewingRequests(): Promise<RequestsForRoom>;
  decideViewing(requestId: string, decision: 'PENDING' | 'ACCEPTED' | 'DECLINED'): Promise<RequestsForRoom>;
  rentBook(): Promise<RentBook>;
  recordCash(leaseId: string): Promise<RentBook>;
  lease(): Promise<LeaseDoc>;
  signLease(leaseId: string): Promise<LeaseDoc>;
  listingDraft(): Promise<NewListingDraft>;
  saveListingDraft(draft: NewListingDraft): Promise<NewListingDraft>;
  publishListing(): Promise<void>;

  // trust
  checkerJob(): Promise<CheckerJob>;
  setCheckerStep(stepId: string, done: boolean): Promise<CheckerJob>;
  submitCheck(passed: boolean): Promise<void>;
  report(reason: ReportReason, details: string): Promise<DisputeCase>;
  disputeCase(id: string): Promise<DisputeCase>;
  adminOverview(): Promise<AdminOverview>;
}

/** Raised when the real backend cannot serve a call yet. */
export class NotAvailableYetError extends Error {
  constructor(what: string) {
    super(`${what} needs sign in, which arrives in Phase 2. Run the app in mock mode to try it.`);
    this.name = 'NotAvailableYetError';
  }
}

/** Raised for a 409 from the backend: someone changed the same record at once. */
export class ConflictError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'ConflictError';
  }
}
