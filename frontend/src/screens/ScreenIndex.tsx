import { Link, useNavigate } from 'react-router-dom';
import type { Role } from '../api/types';
import { useSession } from '../hooks/useSession';
import { Screen } from '../components/ui';

// Every screen from the prototype, grouped as on its canvas. Handy for review
// while sign in is mocked; it can be hidden once real accounts exist.
const GROUPS: { title: string; role: Role; screens: [string, string][] }[] = [
  {
    title: 'Tenant journey: find, view, pay safely, move in',
    role: 'TENANT',
    screens: [
      ['1. Welcome', '/'],
      ['2. Explore rooms', '/explore'],
      ['3. Filters', '/filters'],
      ['4. Map', '/map'],
      ['5. Room details', '/listings/l-site-c-1'],
      ['6. Book a viewing', '/listings/l-site-c-1/viewing'],
      ['7. Chat with landlord', '/chat'],
      ['8. Renter profile and apply', '/apply/l-site-c-1'],
      ['9. Deposit into hold', '/pay'],
      ['10. Move in day', '/move-in'],
      ['11. My home', '/home'],
    ],
  },
  {
    title: 'Landlord side: list, choose tenants, collect rent',
    role: 'LANDLORD',
    screens: [
      ['L1. List: where is the room', '/list/where'],
      ['L2. List: rent and extras', '/list/rent'],
      ['L3. List: photos', '/list/photos'],
      ['L4. List: preview and publish', '/list/publish'],
      ['L5. Landlord home', '/landlord'],
      ['L6. Viewing requests', '/landlord/requests'],
      ['L7. Rent book', '/landlord/rent-book'],
      ['L8. Simple lease', '/lease'],
    ],
  },
  {
    title: 'Trust, safety and the back office',
    role: 'CHECKER',
    screens: [
      ['T1. Checker job', '/checker'],
      ['T2. Report a problem', '/report'],
      ['T3. Dispute case', '/disputes/case-0142'],
      ['T4. Back office (web)', '/admin'],
    ],
  },
  {
    title: 'Living there and moving on',
    role: 'TENANT',
    screens: [
      ['M1. Saved and alerts', '/saved'],
      ['M2. Notifications', '/notifications'],
      ['M3. Room wanted', '/wanted'],
      ['M4. Yard electricity split', '/home/electricity'],
      ['M5. Repair ticket', '/home/repair'],
      ['M6. Move out check', '/home/move-out'],
      ['M7. Reviews', '/home/reviews'],
    ],
  },
];

export function ScreenIndex() {
  const { switchRole } = useSession();
  const navigate = useNavigate();
  return (
    <Screen>
      <div className="stack" style={{ gap: 4 }}>
        <h1 className="title title--lg">All screens</h1>
        <p className="lead">Sample mode. Every screen runs on built in sample data; nothing is saved to a server.</p>
      </div>
      {GROUPS.map((g) => (
        <section key={g.title} className="index-group" aria-label={g.title}>
          <h2 className="caps">{g.title}</h2>
          <div className="list-card">
            {g.screens.map(([label, to]) => (
              <Link
                key={to}
                to={to}
                className="list-row"
                onClick={(e) => {
                  e.preventDefault();
                  switchRole(to === '/admin' ? 'ADMIN' : g.role);
                  navigate(to);
                }}
              >
                <span className="list-row__title">{label}</span>
              </Link>
            ))}
          </div>
        </section>
      ))}
    </Screen>
  );
}
