import { screen } from '@testing-library/react';
import { renderApp } from './renderApp';

// Every route from the prototype renders its main heading on sample data.
const ROUTES: [string, RegExp][] = [
  ['/', /A safe room ekasi/],
  ['/explore', /Rooms in Site C/],
  ['/filters', /Filters/],
  ['/listings/l-site-c-1', /Backyard flat in Site C/],
  ['/listings/l-site-c-1/viewing', /Book a viewing/],
  ['/chat', /Mam' Nomsa/],
  ['/apply/l-site-c-1', /Apply for this room/],
  ['/pay', /Secure the room/],
  ['/move-in', /Move in day/],
  ['/home', /My home/],
  ['/list/where', /Where is the room/],
  ['/list/rent', /Rent, and what comes with the room/],
  ['/list/photos', /Six photos, taken now/],
  ['/list/publish', /This is what tenants will see/],
  ['/landlord', /Your rooms/],
  ['/landlord/requests', /Requests for room 4/],
  ['/landlord/rent-book', /Rent book/],
  ['/lease', /Lease, room 4/],
  ['/checker', /Room check/],
  ['/report', /Report a problem/],
  ['/disputes/case-0142', /Case 0142/],
  ['/admin', /Back office/],
  ['/saved', /Saved/],
  ['/notifications', /Notifications/],
  ['/wanted', /Room wanted/],
  ['/home/electricity', /Yard electricity/],
  ['/home/repair', /Repair/],
  ['/home/move-out', /Move out check/],
  ['/home/reviews', /How was living/],
  ['/screens', /All screens/],
  ['/no-such-page', /This page is not here/],
];

describe.each(ROUTES)('%s', (route, heading) => {
  it('renders its heading', async () => {
    renderApp(route);
    expect(await screen.findByRole('heading', { level: 1, name: heading })).toBeInTheDocument();
  });
});

it('the map renders a pin for each room', async () => {
  renderApp('/map');
  expect(await screen.findByRole('button', { name: 'R2 200, Backyard flat' })).toBeInTheDocument();
});
