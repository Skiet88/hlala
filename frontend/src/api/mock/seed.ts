// Sample data for the mock API, taken from the prototype so every screen shows
// what was designed. Nothing here is real; names and amounts are examples.

import type {
  AdminOverview,
  AppNotification,
  ChatThread,
  CheckerJob,
  DisputeCase,
  LandlordHome,
  LeaseDoc,
  ListingDetail,
  MeterYard,
  MoveInDay,
  MoveOutCheck,
  MyHome,
  NewListingDraft,
  PaymentQuote,
  RentBook,
  RenterProfile,
  RepairTicket,
  RequestsForRoom,
  ReviewDraft,
  SavedRoom,
  SearchAlert,
  WantedPost,
} from '../types';

export const SECTIONS = ['Site C', 'Harare', 'Makhaza', 'Kuyasa', 'Site B', 'Langa'];

const NOMSA = { name: "Mam' Nomsa", initial: 'N', note: 'Lives on the property · ID checked' };

export const LISTINGS: ListingDetail[] = [
  {
    id: 'l-site-c-1',
    title: 'Backyard flat',
    area: 'Khayelitsha',
    section: 'Site C',
    kind: 'Backyard flat',
    rent: 2200,
    deposit: 2200,
    seenInPerson: true,
    features: ['Own toilet', 'Own prepaid meter', 'Water included', 'Burglar gate'],
    suits: ['Just me', 'Couple'],
    meta: 'Own toilet · 6 min to station',
    nearest: { minutes: 4, place: 'Site C taxi rank' },
    map: { x: 11, y: 42 },
    availableFrom: '1 December',
    whoFor: 'One person or a couple',
    checkedOn: '12 November',
    photoCount: 6,
    gettingAround: [
      { minutes: 4, place: 'Site C taxi rank' },
      { minutes: 6, place: 'Nolungile station' },
    ],
    landlord: NOMSA,
  },
  {
    id: 'l-site-c-2',
    title: 'Room in main house',
    area: 'Khayelitsha',
    section: 'Site C',
    kind: 'Room in a house',
    rent: 1800,
    deposit: 1800,
    seenInPerson: true,
    features: ['Water included', 'Geyser'],
    suits: ['Just me', 'Students'],
    meta: 'Shared bathroom · 4 min to rank',
    nearest: { minutes: 4, place: 'Site C taxi rank' },
    map: { x: 54, y: 25 },
    availableFrom: 'Now',
    whoFor: 'One person',
    checkedOn: '9 November',
    photoCount: 6,
    gettingAround: [
      { minutes: 4, place: 'Site C taxi rank' },
      { minutes: 9, place: 'Nolungile station' },
    ],
    landlord: { name: 'Bhut Sizwe', initial: 'S', note: 'Lives on the property · ID checked' },
  },
  {
    id: 'l-site-c-3',
    title: 'Backyard flat, upstairs',
    area: 'Khayelitsha',
    section: 'Site C',
    kind: 'Backyard flat',
    rent: 2600,
    deposit: 2600,
    seenInPerson: false,
    features: ['Own toilet', 'Own prepaid meter', 'Parking'],
    suits: ['Just me', 'Couple', 'Family'],
    meta: 'Own meter · 9 min to rank',
    nearest: { minutes: 9, place: 'Site C taxi rank' },
    map: { x: 74, y: 48 },
    availableFrom: '15 December',
    whoFor: 'Up to three people',
    checkedOn: null,
    photoCount: 4,
    gettingAround: [
      { minutes: 9, place: 'Site C taxi rank' },
      { minutes: 14, place: 'Nolungile station' },
    ],
    landlord: { name: 'Mr Dlamini', initial: 'D', note: 'Lives nearby · ID checked' },
  },
  {
    id: 'l-site-c-4',
    title: 'Backyard room',
    area: 'Khayelitsha',
    section: 'Site C',
    kind: 'Backyard room',
    rent: 1500,
    deposit: 1500,
    seenInPerson: true,
    features: ['Burglar gate'],
    suits: ['Just me', 'Students'],
    meta: 'Shared toilet · 3 min to rank',
    nearest: { minutes: 3, place: 'Nolungile station' },
    map: { x: 18, y: 66 },
    availableFrom: 'Now',
    whoFor: 'One person',
    checkedOn: '3 November',
    photoCount: 6,
    gettingAround: [
      { minutes: 3, place: 'Nolungile station' },
      { minutes: 5, place: 'Site C taxi rank' },
    ],
    landlord: { name: 'Mama Thandeka', initial: 'T', note: 'Lives on the property · ID checked' },
  },
  {
    id: 'l-site-c-5',
    title: 'Bachelor flat',
    area: 'Khayelitsha',
    section: 'Site C',
    kind: 'Whole house',
    rent: 3200,
    deposit: 3200,
    seenInPerson: true,
    features: ['Own toilet', 'Own prepaid meter', 'Furnished', 'Geyser', 'Wi-Fi'],
    suits: ['Just me', 'Couple'],
    meta: 'Furnished · 12 min to station',
    nearest: { minutes: 12, place: 'Nolungile station' },
    map: { x: 40, y: 82 },
    availableFrom: '1 January',
    whoFor: 'One person or a couple',
    checkedOn: '1 November',
    photoCount: 6,
    gettingAround: [
      { minutes: 12, place: 'Nolungile station' },
      { minutes: 10, place: 'Site C taxi rank' },
    ],
    landlord: { name: 'Mrs Mbeki', initial: 'M', note: 'Lives nearby · ID checked' },
  },
  {
    id: 'l-site-c-6',
    title: 'Backyard room',
    area: 'Khayelitsha',
    section: 'Site C',
    kind: 'Backyard room',
    rent: 2000,
    deposit: 2000,
    seenInPerson: false,
    features: ['Water included'],
    suits: ['Just me'],
    meta: 'Water included · 7 min to rank',
    nearest: { minutes: 7, place: 'Site C taxi rank' },
    map: { x: 52, y: 58 },
    availableFrom: 'Now',
    whoFor: 'One person',
    checkedOn: null,
    photoCount: 3,
    gettingAround: [{ minutes: 7, place: 'Site C taxi rank' }],
    landlord: { name: 'Mr Ntsebeza', initial: 'N', note: 'ID checked' },
  },
  {
    id: 'l-harare-1',
    title: 'Backyard room',
    area: 'Khayelitsha',
    section: 'Harare',
    kind: 'Backyard room',
    rent: 1900,
    deposit: 1900,
    seenInPerson: true,
    features: ['Own toilet', 'Burglar gate'],
    suits: ['Just me'],
    meta: 'Own toilet · 5 min to rank',
    nearest: { minutes: 5, place: 'Harare taxi rank' },
    map: { x: 30, y: 36 },
    availableFrom: 'Now',
    whoFor: 'One person',
    checkedOn: '14 November',
    photoCount: 6,
    gettingAround: [{ minutes: 5, place: 'Harare taxi rank' }],
    landlord: { name: 'Bhut Lunga', initial: 'L', note: 'Lives on the property · ID checked' },
  },
  {
    id: 'l-harare-2',
    title: 'Room in main house',
    area: 'Khayelitsha',
    section: 'Harare',
    kind: 'Room in a house',
    rent: 1600,
    deposit: 1600,
    seenInPerson: false,
    features: ['Water included'],
    suits: ['Just me', 'Students'],
    meta: 'Shared bathroom · 6 min to library',
    nearest: { minutes: 6, place: 'Harare library' },
    map: { x: 64, y: 54 },
    availableFrom: '1 December',
    whoFor: 'One person',
    checkedOn: null,
    photoCount: 4,
    gettingAround: [{ minutes: 6, place: 'Harare library' }],
    landlord: { name: 'Mama Zodwa', initial: 'Z', note: 'ID checked' },
  },
  {
    id: 'l-makhaza-1',
    title: 'Backyard flat',
    area: 'Khayelitsha',
    section: 'Makhaza',
    kind: 'Backyard flat',
    rent: 2400,
    deposit: 2400,
    seenInPerson: true,
    features: ['Own toilet', 'Own prepaid meter', 'Burglar gate'],
    suits: ['Couple', 'Family'],
    meta: 'Own meter · 8 min to rank',
    nearest: { minutes: 8, place: 'Makhaza rank' },
    map: { x: 46, y: 40 },
    availableFrom: '1 December',
    whoFor: 'A couple or a small family',
    checkedOn: '10 November',
    photoCount: 6,
    gettingAround: [{ minutes: 8, place: 'Makhaza rank' }],
    landlord: { name: 'Mr Mahlangu', initial: 'M', note: 'Lives on the property · ID checked' },
  },
  {
    id: 'l-kuyasa-1',
    title: 'Bachelor flat',
    area: 'Khayelitsha',
    section: 'Kuyasa',
    kind: 'Whole house',
    rent: 2800,
    deposit: 2800,
    seenInPerson: true,
    features: ['Own toilet', 'Own prepaid meter', 'Geyser', 'Parking'],
    suits: ['Just me', 'Couple'],
    meta: 'Geyser · 10 min to station',
    nearest: { minutes: 10, place: 'Kuyasa station' },
    map: { x: 58, y: 30 },
    availableFrom: '1 December',
    whoFor: 'One person or a couple',
    checkedOn: '8 November',
    photoCount: 6,
    gettingAround: [{ minutes: 10, place: 'Kuyasa station' }],
    landlord: { name: 'Mrs Jaxa', initial: 'J', note: 'Lives nearby · ID checked' },
  },
];

export const VIEWING_SLOTS = {
  days: ['Sat 21', 'Sun 22', 'Mon 23', 'Tue 24'],
  times: ['09:00', '11:00', '14:00', '16:30'],
};

export const CHAT: ChatThread = {
  id: 't-1',
  listingId: 'l-site-c-1',
  listingTitle: 'Backyard flat in Site C',
  landlord: { name: "Mam' Nomsa", initial: 'N', note: 'ID checked' },
  viewing: { id: 'v-1', listingId: 'l-site-c-1', slot: { day: 'Sat 21', time: '11:00' }, status: 'BOOKED' },
  messages: [
    { id: 'm1', from: 'me', kind: 'text', text: 'Molo Mama, is the room still open? I work in Bellville and need to move by 1 December.' },
    { id: 'm2', from: 'them', kind: 'text', text: 'Molo mntanam. Yes it is open. Come see it on Saturday.' },
    { id: 'm3', from: 'them', kind: 'voice', seconds: 14 },
    { id: 'm4', from: 'system', kind: 'meeting', text: 'Meeting point shared: Site C taxi rank, 10:50' },
    { id: 'm5', from: 'me', kind: 'text', text: 'Enkosi Mama, I will be there.' },
  ],
  quickReplies: ['Is electricity included?', 'I am running late'],
};

export const PROFILE: RenterProfile = {
  name: 'Sipho M.',
  initial: 'S',
  badges: ['ID checked', 'Cell number confirmed'],
  rentRecord: 'Rent record: 14 months on time',
  rows: [
    { label: 'Work or study', value: 'Employed full time, Bellville', done: true },
    { label: 'Proof of income', value: 'Payslip uploaded, checked by Hlala', done: true },
    { label: 'Previous landlord', value: 'One reference, contact confirmed', done: true },
    { label: 'Someone who can vouch for you', value: 'Family member or employer', done: false },
  ],
};

export const QUOTE: PaymentQuote = {
  applicationId: 'a-1',
  landlordName: "Mam' Nomsa",
  deposit: 2200,
  firstMonth: 2200,
  firstMonthLabel: 'First month, December',
  feeLabel: '[YOUR FEE]',
  total: 4400,
  methods: [
    { id: 'TILL', label: 'Cash at a supermarket till', note: 'Show a code at the till, no bank account needed' },
    { id: 'EFT', label: 'Instant EFT', note: 'Pay from your banking app' },
    { id: 'CARD', label: 'Bank card', note: 'Debit or credit card' },
    { id: 'TWO_PARTS', label: 'Pay in two parts', note: 'Half now, half before move in day' },
  ],
};

export const MOVE_IN: MoveInDay = {
  dateLabel: 'Tuesday 1 December',
  hold: { id: 'H 20931', leaseId: 'lease-1', amount: 4400, state: 'HELD' },
  landlordName: "Mam' Nomsa",
  checklist: [
    { id: 'keys', label: 'Keys and gate remote in my hand', note: 'Count them and write the number in the lease', done: true },
    { id: 'match', label: 'Room matches the photos', note: 'Same room, same toilet, same door', done: true },
    { id: 'meter', label: 'Prepaid meter number recorded', note: 'Meter 0412 7788 301, units left 12.4', done: true },
    { id: 'lease', label: 'Lease signed by both of us', note: 'Read it before you sign', done: false },
    { id: 'rules', label: 'House rules agreed', note: 'Visitors, noise, gate times, washing line', done: false },
  ],
  photos: ['Door', 'Walls', 'Toilet'],
  photosNeeded: 6,
};

export const MY_HOME: MyHome = {
  title: 'Backyard flat in Site C',
  landlordName: "Mam' Nomsa",
  rentDue: { monthLabel: 'January rent', amount: 2200, dueLabel: 'Due Fri 1 January' },
  onTimeLabel: '15 months on time',
  months: ['Aug', 'Sep', 'Oct', 'Nov', 'Dec', 'Jan'].map((label, i) => ({ label, paid: i < 5 })),
  openRepair: { id: 'r-1', title: 'Leaking tap in the shower', when: 'Plumber booked: Thu 17 Dec, 10:00' },
  unreadNotifications: 3,
};

export const REPAIR: RepairTicket = {
  id: 'r-1',
  title: 'Leaking tap in the shower',
  description: 'Drips all night, water runs under the door.',
  category: 'Plumbing · landlord pays',
  steps: [
    { label: 'You reported it', note: 'Mon 14 December, with a photo and a voice note', done: true },
    { label: 'Landlord saw it', note: 'Mon 14 December, 18:20', done: true },
    { label: 'Plumber booked', note: 'Thu 17 December at 10:00, you agreed to the time', done: true },
    { label: 'You confirm it is fixed', note: 'Waiting for you', done: false },
  ],
  landlordNote: 'Mntanam, the plumber is done. Please check the tap tonight and tell me.',
  status: 'BOOKED',
  dayCount: 3,
};

export const METER: MeterYard = {
  meterNumber: '0412 7788 301',
  monthLabel: 'December',
  mode: 'EQUAL_PER_ROOM',
  members: [
    { id: 'room-1', name: 'Room 1 · Thabo', people: 1, paid: 0 },
    { id: 'room-2', name: 'Room 2 · Zanele', people: 2, paid: 150 },
    { id: 'room-3', name: 'Room 3 · You', people: 1, paid: 200 },
    { id: 'main', name: "Main house · Mam' Nomsa", people: 3, paid: 0 },
  ],
  topUps: [
    { id: 'tu-2', label: '12 Dec · you bought, slip photo saved', amount: 200 },
    { id: 'tu-1', label: '3 Dec · Zanele bought, slip photo saved', amount: 150 },
  ],
};

export const MOVE_OUT: MoveOutCheck = {
  lastDay: '31 March',
  pairs: [
    { label: 'Door', damaged: false },
    { label: 'Walls', damaged: false },
    { label: 'Window', damaged: true },
    { label: 'Toilet', damaged: false },
  ],
  held: 2200,
  deduction: {
    label: 'Cracked window pane',
    amount: 350,
    note: "Mam' Nomsa wants this taken off your deposit. Glazier quote attached.",
  },
  decision: 'PENDING',
};

export const REVIEW: ReviewDraft = {
  leaseId: 'lease-1',
  placeTitle: 'Backyard flat in Site C',
  months: 4,
  aboutName: "Mam' Nomsa",
  tagOptions: [
    'Room as shown',
    'Fair with the deposit',
    'Fixed things fast',
    'Safe yard',
    'Respected my privacy',
    'Electricity was fair',
    'Quiet at night',
  ],
};

export const SAVED: SavedRoom[] = [
  { listingId: 'l-site-c-1', title: 'Backyard flat, Site C', rent: 2200, meta: 'Viewing Sat 21 Nov', state: 'Viewing booked' },
  { listingId: 'l-harare-2', title: 'Room in house, Harare', rent: 1600, meta: 'Saved 2 days ago', state: 'Still open' },
];

export const ALERTS: SearchAlert[] = [
  { id: 'a1', label: 'Site C · under R2 500 · own toilet', note: '2 new rooms today', on: true },
  { id: 'a2', label: 'Langa · backyard flat · any price', note: 'Nothing new this week', on: true },
  { id: 'a3', label: 'Tell me when a saved room drops its price', note: 'Off', on: false },
];

export const NOTIFICATIONS: AppNotification[] = [
  { id: 'n1', title: 'Viewing accepted', body: "Mam' Nomsa confirmed Sat 21 Nov at 11:00.", when: '2 min', to: '/chat', unread: true },
  { id: 'n2', title: '2 new rooms match your alert', body: 'Site C, under R2 500, own toilet.', when: '1 h', to: '/explore', unread: true },
  { id: 'n3', title: 'A landlord answered your room wanted post', body: 'Backyard room in Harare, R1 900.', when: '3 h', to: '/wanted', unread: true },
  { id: 'n4', title: 'Deposit received into hold', body: 'R4 400. Receipt H 20931 saved under Lease and receipts.', when: 'Mon', to: '/home', unread: false },
  { id: 'n5', title: 'Rent due in 3 days', body: 'R2 200 for January. Pay in the app or at a till.', when: 'Mon', to: '/home', unread: false },
  { id: 'n6', title: 'Plumber booked for your repair', body: 'Leaking tap, Thursday at 10:00.', when: 'Sun', to: '/home/repair', unread: false },
  { id: 'n7', title: 'Your electricity share is ready', body: 'December yard split: you are owed R112.50.', when: 'Sat', to: '/home/electricity', unread: false },
];

export const WANTED: WantedPost = {
  summary: 'Backyard room or flat, up to R2 000, by 1 December',
  tags: ['Site C or Harare', 'Just me', 'Own toilet', 'Near a taxi rank'],
  live: true,
  offers: [
    { id: 'o-a', listingId: 'l-harare-1', title: 'Backyard room, Harare', meta: 'Seen in person · own toilet · 5 min to rank', rent: 1900 },
    { id: 'o-b', listingId: 'l-site-c-1', title: 'Backyard flat, Site C', meta: 'Seen in person · own meter · 4 min to rank', rent: 2200 },
    { id: 'o-c', listingId: 'l-site-c-2', title: 'Room in main house, Site C', meta: 'Seen in person · shared bathroom', rent: 1800 },
  ],
};

export const LANDLORD_HOME: LandlordHome = {
  greetingName: "Mam' Nomsa",
  monthLabel: 'November',
  collected: 4200,
  expected: 6400,
  summary: '3 rooms let · 1 empty · 1 tenant late',
  rooms: [
    { number: 1, title: 'Thabo N.', note: 'R2 200 · paid 1 November in app', status: 'PAID', requests: 0 },
    { number: 2, title: 'Zanele M.', note: 'R2 000 · paid 3 November, cash recorded', status: 'PAID', requests: 0 },
    { number: 3, title: 'Lwazi D.', note: 'R2 200 · 15 days late', status: 'LATE', requests: 0 },
    { number: 4, title: 'Empty since 31 October', note: 'Listed at R2 200 · seen in person', status: 'EMPTY', requests: 3 },
  ],
  openRepairs: '1 open, leaking tap',
};

export const REQUESTS: RequestsForRoom = {
  roomLabel: 'Requests for room 4',
  roomNote: 'Backyard flat · R2 200 per month',
  viewings: [
    { id: 'sipho', initial: 'S', name: 'Sipho M.', who: 'Moving in alone · works in Bellville', slot: 'Sat 21 Nov, 11:00', tags: ['ID checked', '14 months rent on time', 'Proof of income'], status: 'PENDING' },
    { id: 'anele', initial: 'A', name: 'Anele K.', who: 'Student · moving in alone', slot: 'Sun 22 Nov, 14:00', tags: ['ID checked', 'New on Hlala', 'Parent will vouch'], status: 'PENDING' },
    { id: 'noku', initial: 'N', name: 'Nokuthula D.', who: 'Couple · both working', slot: 'Mon 23 Nov, 16:30', tags: ['ID checked', 'Previous landlord reference'], status: 'PENDING' },
  ],
  applications: 1,
};

export const RENT_BOOK: RentBook = {
  monthLabel: 'Nov 2026',
  rows: [
    { leaseId: 'lease-r1', name: 'Room 1 · Thabo N.', note: 'Paid 1 November', amount: 2200, status: 'PAID', how: 'Paid in app' },
    { leaseId: 'lease-r2', name: 'Room 2 · Zanele M.', note: 'Paid 3 November', amount: 2000, status: 'PAID', how: 'Cash, receipt sent' },
    { leaseId: 'lease-r3', name: 'Room 3 · Lwazi D.', note: 'Due 1 November, 15 days late', amount: 2200, status: 'LATE', how: '' },
  ],
};

export const LEASE: LeaseDoc = {
  id: 'lease-1',
  title: 'Lease, room 4',
  intro: 'One page, plain words. Between Nomsa G. (landlord) and Sipho M. (tenant), from 1 December 2026.',
  terms: [
    { head: 'Rent', body: 'R2 200 a month, paid by the 1st, in the app or in cash with a receipt.' },
    { head: 'Deposit', body: 'R2 200, held by Hlala. Paid back after the move out check, less any damage shown in the photos.' },
    { head: 'Electricity and water', body: 'Tenant buys own prepaid units. Water is included in the rent.' },
    { head: 'Notice', body: 'Either side gives one full month of notice, in the app, so there is a record.' },
    { head: 'House rules', body: 'Gate locked by 22:00. Visitors welcome, no extra people living in the room.' },
    { head: 'Repairs', body: 'Landlord fixes the structure, plumbing and electricity. Tenant fixes what the tenant breaks.' },
  ],
  landlordSigned: { by: 'Nomsa G.', on: '28 November' },
  tenantSigned: null,
  language: 'EN',
};

export const LISTING_DRAFT: NewListingDraft = {
  section: 'Site C',
  kind: 'Backyard flat',
  landmark: '',
  rent: null,
  deposit: null,
  extras: { toilet: true, meter: true, water: true, gate: false, furnished: false },
  photos: { Door: true, Room: true, Window: true, Toilet: false, Yard: false, Gate: false },
  checkerSlot: 'Wed 18',
  showWanted: true,
  smsRequests: true,
};

export const CHECKER_JOB: CheckerJob = {
  id: 'job-1',
  area: 'Site C',
  title: "Backyard flat, Mam' Nomsa",
  distance: '350 m away',
  atPin: true,
  payLabel: '[PAY PER CHECK]',
  steps: [
    { id: 'exists', label: 'The room exists and is empty', note: 'Stand inside it', done: true },
    { id: 'owner', label: 'Landlord ID matches the listing', note: 'Check the ID book or card', done: true },
    { id: 'photos', label: 'Six photos taken on site', note: 'Door, room, window, toilet, yard, gate', done: true },
    { id: 'toilet', label: 'Toilet and water are as listed', note: 'Own or shared, flush it, open the tap', done: false },
    { id: 'meter', label: 'Prepaid meter number recorded', note: 'Own meter or shared with main house', done: false },
    { id: 'safe', label: 'Lock, burglar gate and yard gate work', note: 'Try each one', done: false },
  ],
  next: { title: 'Next: room in main house, Harare', note: '1.2 km · landlord is home after 14:00' },
  moreToday: 2,
};

export const DISPUTE: DisputeCase = {
  id: 'case-0142',
  number: '0142',
  reason: 'Asked for money before the viewing',
  steps: [
    { label: 'Report received', note: 'Mon 16 November, 10:12. You got an SMS with the case number.', done: true },
    { label: 'Listing paused, money frozen', note: 'Done automatically the moment you reported.', done: true },
    { label: 'Other side asked to answer', note: 'They have 48 hours. Silence counts against them.', done: false },
    { label: 'A Hlala case worker decides', note: 'A real person reads both sides and the proof.', done: false },
  ],
  proof: [
    { label: 'Chat history', note: 'Attached automatically, cannot be edited', from: 'System' },
    { label: '2 screenshots', note: 'WhatsApp message asking for R500', from: 'You' },
    { label: 'Voice note, 0:41', note: 'What happened, in your own words', from: 'You' },
  ],
};

export const ADMIN: AdminOverview = {
  areaLabel: 'Khayelitsha and Langa',
  dateLabel: 'Monday 16 November 2026',
  staffName: 'Thandi, case worker',
  stats: [
    { label: 'Rooms waiting for a check', value: '7', note: '3 have a checker booked' },
    { label: 'Fraud flags to review', value: '3', note: 'Raised automatically' },
    { label: 'Open dispute cases', value: '2', note: 'Oldest is 2 hours old' },
    { label: 'Money on hold', value: 'R86 400', note: '21 holds, 2 frozen' },
  ],
  panels: [
    {
      id: 'checks',
      title: 'Room checks',
      rows: [
        { id: 'c1', head: 'Backyard room, Makhaza', body: 'Listed today. No checker assigned yet.', act: 'Assign checker', alt: 'View listing' },
        { id: 'c2', head: 'Bachelor flat, Kuyasa', body: 'Checker Luyanda visits Wed 18 Nov, morning.', act: 'Open job', alt: 'Message landlord' },
        { id: 'c3', head: 'Room in house, Langa Zone 12', body: 'Check failed: toilet is shared, listing says own.', act: 'Ask landlord to fix', alt: 'Take listing down' },
      ],
    },
    {
      id: 'flags',
      title: 'Fraud flags',
      rows: [
        { id: 'f1', head: 'Same photo on two listings', body: 'Harare and Site B, different landlords. Second listing auto paused.', act: 'Compare photos', alt: 'Clear flag' },
        { id: 'f2', head: 'One cell number, four landlord accounts', body: 'All created in the last two days.', act: 'Review accounts', alt: 'Block number' },
        { id: 'f3', head: 'Rent far below the area', body: 'Backyard flat in Site C at R600. Area runs R1 500 to R2 600.', act: 'Hold until checked', alt: 'Clear flag' },
      ],
    },
    {
      id: 'cases',
      title: 'Dispute desk',
      rows: [
        { id: 'd1', head: 'Case 0142 · money asked before viewing', body: 'Site C. Tenant sent 2 screenshots. Landlord has 46 hours to answer.', act: 'Open case', alt: 'Call tenant' },
        { id: 'd2', head: 'Case 0139 · R350 window deduction', body: 'Move out check. Photos from both dates on file. R350 frozen.', act: 'Decide', alt: 'Ask for quote' },
      ],
    },
  ],
  trail: [
    { id: 't1', time: '16 Nov 10:12', who: 'System', what: 'Hold frozen, case 0142 opened', amount: 2000, ref: 'H 20944' },
    { id: 't2', time: '16 Nov 09:40', who: 'Tenant, Sipho M.', what: 'Deposit and first month paid at a till', amount: 4400, ref: 'H 20931' },
    { id: 't3', time: '16 Nov 09:02', who: 'Thandi and Bongani (two approvals)', what: 'Payout to landlord after keys confirmed', amount: 3600, ref: 'H 20877' },
    { id: 't4', time: '15 Nov 17:25', who: 'Tenant, Anele K.', what: 'Confirmed keys on move in day', amount: 3600, ref: 'H 20877' },
    { id: 't5', time: '15 Nov 14:10', who: 'System', what: 'Undisputed part of deposit refunded', amount: 1850, ref: 'H 20391' },
  ],
};
