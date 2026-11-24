// What the screens work with. Shapes follow the backend DTOs where the backend
// already has them (ids, amounts, enums), and add the display fields the
// prototype needs that the backend does not serve yet (walking times, names).

import type { DepositHoldState } from '../domain/depositState';
import type { SplitMode } from '../domain/meterSplit';
import type { DeductionDecision } from '../domain/settlement';

export type Role = 'TENANT' | 'LANDLORD' | 'CHECKER' | 'ADMIN';

export interface Session {
  userId: string;
  name: string;
  phone: string;
  roles: Role[];
}

// ------------------------------------------------------------- listings

export type PlaceKind = 'Backyard room' | 'Backyard flat' | 'Room in a house' | 'Whole house';
export type Feature =
  | 'Own toilet'
  | 'Own prepaid meter'
  | 'Water included'
  | 'Burglar gate'
  | 'Furnished'
  | 'Wi-Fi'
  | 'Geyser'
  | 'Parking';
export type Household = 'Just me' | 'Couple' | 'Family' | 'Students';

export interface Walk {
  minutes: number;
  place: string;
}

export interface ListingSummary {
  id: string;
  title: string;
  area: string;
  section: string;
  kind: PlaceKind;
  rent: number;
  deposit: number;
  seenInPerson: boolean;
  features: Feature[];
  suits: Household[];
  /** Short line under the title, e.g. "Own toilet · 6 min to station". */
  meta: string;
  nearest: Walk;
  /** Position on the placeholder map, in percent of its width and height. */
  map: { x: number; y: number };
}

export interface ListingDetail extends ListingSummary {
  availableFrom: string;
  whoFor: string;
  checkedOn: string | null;
  photoCount: number;
  gettingAround: Walk[];
  landlord: { name: string; initial: string; note: string };
}

export interface ListingFilters {
  minRent: number | null;
  maxRent: number | null;
  kinds: PlaceKind[];
  mustHave: Feature[];
  household: Household[];
  seenOnly: boolean;
}

// ---------------------------------------------------- viewing and chat

export interface ViewingSlot {
  day: string; // "Sat 21"
  time: string; // "11:00"
}

export interface Viewing {
  id: string;
  listingId: string;
  slot: ViewingSlot;
  status: 'BOOKED' | 'COMPLETED' | 'NO_SHOW' | 'CANCELLED';
}

export type ChatMessage =
  | { id: string; from: 'me' | 'them'; kind: 'text'; text: string }
  | { id: string; from: 'me' | 'them'; kind: 'voice'; seconds: number }
  | { id: string; from: 'system'; kind: 'meeting'; text: string };

export interface ChatThread {
  id: string;
  listingId: string;
  listingTitle: string;
  landlord: { name: string; initial: string; note: string };
  viewing: Viewing | null;
  messages: ChatMessage[];
  quickReplies: string[];
}

// ------------------------------------------------ apply, pay, move in

export interface ProfileRow {
  label: string;
  value: string;
  done: boolean;
}

export interface RenterProfile {
  name: string;
  initial: string;
  badges: string[];
  rentRecord: string;
  rows: ProfileRow[];
}

export interface Application {
  id: string;
  listingId: string;
  status: 'PENDING' | 'ACCEPTED' | 'DECLINED' | 'WITHDRAWN';
  household: string;
  moveIn: string;
}

export type PayMethodId = 'TILL' | 'EFT' | 'CARD' | 'TWO_PARTS';

export interface PayMethod {
  id: PayMethodId;
  label: string;
  note: string;
}

export interface PaymentQuote {
  applicationId: string;
  landlordName: string;
  deposit: number;
  firstMonth: number;
  firstMonthLabel: string;
  /** Unresolved commercial placeholder: the owner has not set the fee. */
  feeLabel: string;
  total: number;
  methods: PayMethod[];
}

export interface DepositHold {
  id: string;
  leaseId: string;
  amount: number;
  state: DepositHoldState;
}

export interface ChecklistItem {
  id: string;
  label: string;
  note: string;
  done: boolean;
}

export interface MoveInDay {
  dateLabel: string;
  hold: DepositHold;
  landlordName: string;
  checklist: ChecklistItem[];
  photos: string[];
  photosNeeded: number;
}

// ------------------------------------------------------------- my home

export interface MonthMark {
  label: string;
  paid: boolean;
}

export interface MyHome {
  title: string;
  landlordName: string;
  rentDue: { monthLabel: string; amount: number; dueLabel: string };
  onTimeLabel: string;
  months: MonthMark[];
  openRepair: { id: string; title: string; when: string } | null;
  unreadNotifications: number;
}

export interface RepairTicket {
  id: string;
  title: string;
  description: string;
  category: string;
  steps: { label: string; note: string; done: boolean }[];
  landlordNote: string;
  status: 'REPORTED' | 'SEEN' | 'BOOKED' | 'CONFIRMED_FIXED';
  dayCount: number;
}

export interface MeterYard {
  meterNumber: string;
  monthLabel: string;
  mode: SplitMode;
  members: { id: string; name: string; people: number; paid: number }[];
  topUps: { id: string; label: string; amount: number }[];
}

export interface MoveOutCheck {
  lastDay: string;
  pairs: { label: string; damaged: boolean }[];
  held: number;
  deduction: { label: string; amount: number; note: string };
  decision: DeductionDecision;
}

export interface ReviewDraft {
  leaseId: string;
  placeTitle: string;
  months: number;
  aboutName: string;
  tagOptions: string[];
}

// --------------------------------------------------------------- saved

export interface SavedRoom {
  listingId: string;
  title: string;
  rent: number;
  meta: string;
  state: string;
}

export interface SearchAlert {
  id: string;
  label: string;
  note: string;
  on: boolean;
}

export interface AppNotification {
  id: string;
  title: string;
  body: string;
  when: string;
  to: string;
  unread: boolean;
}

export interface WantedPost {
  summary: string;
  tags: string[];
  live: boolean;
  offers: { id: string; listingId: string; title: string; meta: string; rent: number }[];
}

// ------------------------------------------------------------ landlord

export interface LandlordRoom {
  number: number;
  title: string;
  note: string;
  status: 'PAID' | 'LATE' | 'EMPTY';
  requests: number;
}

export interface LandlordHome {
  greetingName: string;
  monthLabel: string;
  collected: number;
  expected: number;
  summary: string;
  rooms: LandlordRoom[];
  openRepairs: string;
}

export interface ViewingRequest {
  id: string;
  initial: string;
  name: string;
  who: string;
  slot: string;
  tags: string[];
  status: 'PENDING' | 'ACCEPTED' | 'DECLINED';
}

export interface RequestsForRoom {
  roomLabel: string;
  roomNote: string;
  viewings: ViewingRequest[];
  applications: number;
}

export interface RentRow {
  leaseId: string;
  name: string;
  note: string;
  amount: number;
  status: 'PAID' | 'LATE';
  how: string;
}

export interface RentBook {
  monthLabel: string;
  rows: RentRow[];
}

export interface LeaseTerm {
  head: string;
  body: string;
}

export interface LeaseDoc {
  id: string;
  title: string;
  intro: string;
  terms: LeaseTerm[];
  landlordSigned: { by: string; on: string } | null;
  tenantSigned: { by: string; on: string } | null;
  language: 'EN' | 'XH';
}

export interface NewListingDraft {
  section: string;
  kind: PlaceKind;
  landmark: string;
  rent: number | null;
  deposit: number | null;
  extras: Record<string, boolean>;
  photos: Record<string, boolean>;
  checkerSlot: string;
  showWanted: boolean;
  smsRequests: boolean;
}

// --------------------------------------------------------------- trust

export interface CheckerJob {
  id: string;
  area: string;
  title: string;
  distance: string;
  atPin: boolean;
  payLabel: string;
  steps: ChecklistItem[];
  next: { title: string; note: string } | null;
  moreToday: number;
}

export type ReportReason =
  | 'Asked for money before the viewing'
  | 'Room is not like the photos'
  | 'Listing looks fake or copied'
  | 'Landlord will not return my deposit'
  | 'Tenant is not paying or damaged the room'
  | 'I felt unsafe at the viewing';

export interface DisputeCase {
  id: string;
  number: string;
  reason: string;
  steps: { label: string; note: string; done: boolean }[];
  proof: { label: string; note: string; from: string }[];
}

export interface AdminOverview {
  areaLabel: string;
  dateLabel: string;
  staffName: string;
  stats: { label: string; value: string; note: string }[];
  panels: {
    id: string;
    title: string;
    rows: { id: string; head: string; body: string; act: string; alt: string }[];
  }[];
  trail: { id: string; time: string; who: string; what: string; amount: number; ref: string }[];
}
