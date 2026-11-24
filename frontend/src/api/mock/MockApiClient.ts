import type { ApiClient } from '../ApiClient';
import type {
  ChatThread,
  DisputeCase,
  ListingFilters,
  NewListingDraft,
  PayMethodId,
  ReportReason,
  Role,
  Session,
  ViewingSlot,
} from '../types';
import type { SplitMode } from '../../domain/meterSplit';
import type { DeductionDecision } from '../../domain/settlement';
import { matchesFilters } from '../../domain/listingFilter';
import { formatRand } from '../../domain/money';
import * as seed from './seed';

const clone = <T>(value: T): T => structuredClone(value);

/**
 * Runs every screen on in memory sample data. Each instance starts from a
 * fresh copy of the seed, so tests never leak state into each other, and
 * actions (accept a viewing, record cash, sign) change what later reads see.
 */
export class MockApiClient implements ApiClient {
  private listings = clone(seed.LISTINGS);
  private chat = clone(seed.CHAT);
  private quote = clone(seed.QUOTE);
  private moveIn = clone(seed.MOVE_IN);
  private home = clone(seed.MY_HOME);
  private repairTicket = clone(seed.REPAIR);
  private meter = clone(seed.METER);
  private moveOut = clone(seed.MOVE_OUT);
  private saved = new Set(seed.SAVED.map((s) => s.listingId));
  private savedRoomsList = clone(seed.SAVED);
  private alertList = clone(seed.ALERTS);
  private notes = clone(seed.NOTIFICATIONS);
  private wanted = clone(seed.WANTED);
  private landlord = clone(seed.LANDLORD_HOME);
  private requests = clone(seed.REQUESTS);
  private book = clone(seed.RENT_BOOK);
  private leaseDoc = clone(seed.LEASE);
  private draft = clone(seed.LISTING_DRAFT);
  private job = clone(seed.CHECKER_JOB);
  private cases: DisputeCase[] = [clone(seed.DISPUTE)];
  private nextId = 1;

  /** @param latencyMs pretend network delay, so loading states get exercised. */
  constructor(private readonly latencyMs = 150) {}

  private reply<T>(value: T): Promise<T> {
    const copy = clone(value);
    if (this.latencyMs <= 0) return Promise.resolve(copy);
    return new Promise((resolve) => setTimeout(() => resolve(copy), this.latencyMs));
  }

  private id(prefix: string): string {
    return `${prefix}-${this.nextId++}`;
  }

  // ------------------------------------------------------------ sign in

  async requestCode(phone: string): Promise<void> {
    if (!/^0\d{9}$/.test(phone.replace(/\s/g, ''))) {
      throw new Error('Enter a 10 digit cell number, for example 071 234 5678');
    }
    return this.reply(undefined);
  }

  async verifyCode(phone: string, code: string, role: Role): Promise<Session> {
    if (!/^\d{6}$/.test(code)) throw new Error('The code is the 6 numbers in the SMS');
    const name = role === 'LANDLORD' ? "Mam' Nomsa" : role === 'CHECKER' ? 'Luyanda' : role === 'ADMIN' ? 'Thandi' : 'Sipho M.';
    return this.reply({ userId: `u-${role.toLowerCase()}`, name, phone, roles: [role] });
  }

  // ------------------------------------------------------------ explore

  sections() {
    return this.reply(seed.SECTIONS);
  }

  searchListings(section: string | null, filters: ListingFilters, query = '') {
    const q = query.trim().toLowerCase();
    const found = this.listings
      .filter((l) => matchesFilters(l, filters, section ?? undefined))
      .filter((l) => q === '' || `${l.title} ${l.section} ${l.nearest.place} ${l.meta}`.toLowerCase().includes(q))
      .sort((a, b) => a.nearest.minutes - b.nearest.minutes);
    return this.reply(found);
  }

  async getListing(id: string) {
    const listing = this.listings.find((l) => l.id === id);
    if (!listing) throw new Error('This room is no longer listed');
    return this.reply(listing);
  }

  viewingSlots() {
    return this.reply(seed.VIEWING_SLOTS.days.flatMap((day) => seed.VIEWING_SLOTS.times.map((time) => ({ day, time }))));
  }

  async bookViewing(listingId: string, slot: ViewingSlot) {
    const listing = this.listings.find((l) => l.id === listingId);
    if (!listing) throw new Error('This room is no longer listed');
    const viewing = { id: this.id('v'), listingId, slot, status: 'BOOKED' as const };
    this.chat = {
      ...this.chat,
      listingId,
      listingTitle: `${listing.title} in ${listing.section}`,
      landlord: { name: listing.landlord.name, initial: listing.landlord.initial, note: 'ID checked' },
      viewing,
    };
    return this.reply(viewing);
  }

  isSaved(listingId: string) {
    return this.reply(this.saved.has(listingId));
  }

  async toggleSaved(listingId: string) {
    const listing = this.listings.find((l) => l.id === listingId);
    if (this.saved.has(listingId)) {
      this.saved.delete(listingId);
      this.savedRoomsList = this.savedRoomsList.filter((s) => s.listingId !== listingId);
    } else if (listing) {
      this.saved.add(listingId);
      this.savedRoomsList.push({
        listingId,
        title: `${listing.title}, ${listing.section}`,
        rent: listing.rent,
        meta: 'Saved just now',
        state: 'Still open',
      });
    }
    return this.reply(this.saved.has(listingId));
  }

  // --------------------------------------------------------------- chat

  chatThread() {
    return this.reply(this.chat);
  }

  async sendMessage(threadId: string, text: string): Promise<ChatThread> {
    if (threadId !== this.chat.id) throw new Error('Chat not found');
    const trimmed = text.trim();
    if (trimmed === '') return this.reply(this.chat);
    this.chat = {
      ...this.chat,
      messages: [...this.chat.messages, { id: this.id('m'), from: 'me', kind: 'text', text: trimmed }],
    };
    return this.reply(this.chat);
  }

  // ------------------------------------------------ apply, pay, move in

  renterProfile() {
    return this.reply(seed.PROFILE);
  }

  async apply(listingId: string, household: string, moveIn: string) {
    return this.reply({ id: this.quote.applicationId, listingId, status: 'ACCEPTED' as const, household, moveIn });
  }

  paymentQuote() {
    return this.reply(this.quote);
  }

  async payIntoHold(method: PayMethodId) {
    if (!this.quote.methods.some((m) => m.id === method)) throw new Error('Pick how you want to pay');
    this.moveIn.hold = { ...this.moveIn.hold, state: 'HELD' };
    return this.reply(this.moveIn.hold);
  }

  moveInDay() {
    return this.reply(this.moveIn);
  }

  async setMoveInItem(itemId: string, done: boolean) {
    this.moveIn.checklist = this.moveIn.checklist.map((i) => (i.id === itemId ? { ...i, done } : i));
    return this.reply(this.moveIn);
  }

  async addConditionPhoto(label: string) {
    if (this.moveIn.photos.length < this.moveIn.photosNeeded) this.moveIn.photos.push(label);
    return this.reply(this.moveIn);
  }

  async confirmMovedIn() {
    if (this.moveIn.checklist.some((i) => !i.done)) {
      throw new Error('Tick every item on the list first. The money only moves once you are sure.');
    }
    this.moveIn.hold = { ...this.moveIn.hold, state: 'ACTIVE' };
    return this.reply(this.moveIn.hold);
  }

  // -------------------------------------------------------- living there

  myHome() {
    return this.reply(this.home);
  }

  async repair(id: string) {
    if (id !== this.repairTicket.id) throw new Error('Repair not found');
    return this.reply(this.repairTicket);
  }

  async confirmRepair(id: string, fixed: boolean) {
    if (id !== this.repairTicket.id) throw new Error('Repair not found');
    const steps = this.repairTicket.steps.map((s, i, all) =>
      i === all.length - 1
        ? { ...s, done: fixed, note: fixed ? 'Confirmed just now. Saved to the room history.' : 'Waiting for you' }
        : s,
    );
    this.repairTicket = { ...this.repairTicket, steps, status: fixed ? 'CONFIRMED_FIXED' : 'BOOKED' };
    this.home = {
      ...this.home,
      openRepair: fixed ? null : { id, title: this.repairTicket.title, when: 'Plumber booked: Thu 17 Dec, 10:00' },
    };
    return this.reply(this.repairTicket);
  }

  meterYard() {
    return this.reply(this.meter);
  }

  async setSplitMode(mode: SplitMode) {
    this.meter = { ...this.meter, mode };
    return this.reply(this.meter);
  }

  async logTopUp(amount: number) {
    if (!(amount > 0)) throw new Error('Enter the amount on the slip');
    this.meter = {
      ...this.meter,
      members: this.meter.members.map((m) => (m.id === 'room-3' ? { ...m, paid: m.paid + amount } : m)),
      topUps: [{ id: this.id('tu'), label: 'Today · you bought', amount }, ...this.meter.topUps],
    };
    return this.reply(this.meter);
  }

  moveOutCheck() {
    return this.reply(this.moveOut);
  }

  async decideDeduction(decision: DeductionDecision) {
    this.moveOut = { ...this.moveOut, decision };
    return this.reply(this.moveOut);
  }

  reviewDraft() {
    return this.reply(seed.REVIEW);
  }

  async postReview(_leaseId: string, stars: number) {
    if (stars < 1 || stars > 5) throw new Error('Pick between 1 and 5 stars');
    return this.reply(undefined);
  }

  // ----------------------------------------- saved, alerts, notifications

  savedRooms() {
    return this.reply(this.savedRoomsList);
  }

  alerts() {
    return this.reply(this.alertList);
  }

  async setAlert(id: string, on: boolean) {
    this.alertList = this.alertList.map((a) => (a.id === id ? { ...a, on } : a));
    return this.reply(this.alertList);
  }

  async saveFiltersAsAlert(section: string, filters: ListingFilters) {
    const parts = [section];
    if (filters.maxRent != null) parts.push(`under ${formatRand(filters.maxRent)}`);
    parts.push(...filters.mustHave.map((f) => f.toLowerCase()));
    this.alertList = [{ id: this.id('a'), label: parts.join(' · '), note: 'New alert', on: true }, ...this.alertList];
    return this.reply(this.alertList);
  }

  notifications() {
    return this.reply(this.notes);
  }

  async markAllRead() {
    this.notes = this.notes.map((n) => ({ ...n, unread: false }));
    this.home = { ...this.home, unreadNotifications: 0 };
    return this.reply(this.notes);
  }

  wantedPost() {
    return this.reply(this.wanted);
  }

  async hideOffer(offerId: string) {
    this.wanted = { ...this.wanted, offers: this.wanted.offers.filter((o) => o.id !== offerId) };
    return this.reply(this.wanted);
  }

  async setWantedLive(live: boolean) {
    this.wanted = { ...this.wanted, live };
    return this.reply(this.wanted);
  }

  // ------------------------------------------------------------ landlord

  landlordHome() {
    return this.reply(this.landlord);
  }

  async remindTenant(roomNumber: number) {
    if (!this.landlord.rooms.some((r) => r.number === roomNumber && r.status === 'LATE')) {
      throw new Error('Only late rooms get a reminder');
    }
    return this.reply(undefined);
  }

  viewingRequests() {
    return this.reply(this.requests);
  }

  async decideViewing(requestId: string, decision: 'PENDING' | 'ACCEPTED' | 'DECLINED') {
    this.requests = {
      ...this.requests,
      viewings: this.requests.viewings.map((v) => (v.id === requestId ? { ...v, status: decision } : v)),
    };
    return this.reply(this.requests);
  }

  rentBook() {
    return this.reply(this.book);
  }

  async recordCash(leaseId: string) {
    const row = this.book.rows.find((r) => r.leaseId === leaseId);
    if (!row || row.status !== 'LATE') throw new Error('Nothing owed on this room');
    this.book = {
      ...this.book,
      rows: this.book.rows.map((r) =>
        r.leaseId === leaseId ? { ...r, status: 'PAID', note: 'Paid today', how: 'Cash, receipt sent' } : r,
      ),
    };
    this.landlord = {
      ...this.landlord,
      collected: this.landlord.collected + row.amount,
      rooms: this.landlord.rooms.map((r) =>
        r.status === 'LATE' ? { ...r, status: 'PAID', note: `${formatRand(row.amount)} · cash recorded today` } : r,
      ),
      summary: '3 rooms let · 1 empty · nobody late',
    };
    return this.reply(this.book);
  }

  lease() {
    return this.reply(this.leaseDoc);
  }

  async signLease(leaseId: string) {
    if (leaseId !== this.leaseDoc.id) throw new Error('Lease not found');
    this.leaseDoc = { ...this.leaseDoc, tenantSigned: { by: 'Sipho M.', on: 'just now' } };
    this.moveIn.checklist = this.moveIn.checklist.map((i) => (i.id === 'lease' ? { ...i, done: true } : i));
    return this.reply(this.leaseDoc);
  }

  listingDraft() {
    return this.reply(this.draft);
  }

  async saveListingDraft(draft: NewListingDraft) {
    this.draft = clone(draft);
    return this.reply(this.draft);
  }

  async publishListing() {
    const d = this.draft;
    if (d.rent == null || d.deposit == null) throw new Error('Add the rent and the deposit before you publish');
    const features = [
      d.extras.toilet && 'Own toilet',
      d.extras.meter && 'Own prepaid meter',
      d.extras.water && 'Water included',
      d.extras.gate && 'Burglar gate',
      d.extras.furnished && 'Furnished',
    ].filter(Boolean) as ListingFilters['mustHave'];
    this.listings.push({
      id: this.id('l-new'),
      title: d.kind,
      area: 'Khayelitsha',
      section: d.section,
      kind: d.kind,
      rent: d.rent,
      deposit: d.deposit,
      seenInPerson: false,
      features,
      suits: ['Just me'],
      meta: [...features.slice(0, 1), 'Not checked yet'].join(' · '),
      nearest: { minutes: 5, place: d.landmark || `${d.section} taxi rank` },
      map: { x: 50, y: 50 },
      availableFrom: 'Now',
      whoFor: 'Ask the landlord',
      checkedOn: null,
      photoCount: Object.values(d.photos).filter(Boolean).length,
      gettingAround: [{ minutes: 5, place: d.landmark || `${d.section} taxi rank` }],
      landlord: { name: "Mam' Nomsa", initial: 'N', note: 'Lives on the property · ID checked' },
    });
    return this.reply(undefined);
  }

  // --------------------------------------------------------------- trust

  checkerJob() {
    return this.reply(this.job);
  }

  async setCheckerStep(stepId: string, done: boolean) {
    this.job = { ...this.job, steps: this.job.steps.map((s) => (s.id === stepId ? { ...s, done } : s)) };
    return this.reply(this.job);
  }

  async submitCheck(passed: boolean) {
    if (passed && this.job.steps.some((s) => !s.done)) {
      throw new Error('Every item must be ticked before the badge can be awarded');
    }
    if (!this.job.atPin) throw new Error('You must be at the pin to submit a check');
    return this.reply(undefined);
  }

  async report(reason: ReportReason, details: string) {
    const created: DisputeCase = {
      id: this.id('case'),
      number: String(143 + this.cases.length).padStart(4, '0'),
      reason,
      steps: [
        { label: 'Report received', note: 'Just now. You get an SMS with the case number.', done: true },
        { label: 'Listing paused, money frozen', note: 'Done automatically the moment you reported.', done: true },
        { label: 'Other side asked to answer', note: 'They have 48 hours. Silence counts against them.', done: false },
        { label: 'A Hlala case worker decides', note: 'A real person reads both sides and the proof.', done: false },
      ],
      proof: [
        { label: 'Chat history', note: 'Attached automatically, cannot be edited', from: 'System' },
        ...(details.trim() ? [{ label: 'Your words', note: details.trim(), from: 'You' }] : []),
      ],
    };
    this.cases.push(created);
    return this.reply(created);
  }

  async disputeCase(id: string) {
    const found = this.cases.find((c) => c.id === id);
    if (!found) throw new Error('Case not found');
    return this.reply(found);
  }

  adminOverview() {
    return this.reply(seed.ADMIN);
  }
}
