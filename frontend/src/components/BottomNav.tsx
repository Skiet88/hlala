import { NavLink } from 'react-router-dom';
import { Icon, type IconName } from './Icon';

function Item({ to, icon, label, end }: { to: string; icon: IconName; label: string; end?: boolean }) {
  return (
    <NavLink to={to} end={end} className="bottom-nav__item">
      <Icon name={icon} size={22} />
      {label}
    </NavLink>
  );
}

/** Tenant navigation, as on Explore: the middle button lists a room. */
export function TenantNav() {
  return (
    <nav className="bottom-nav" aria-label="Main">
      <Item to="/explore" icon="search" label="Explore" />
      <Item to="/saved" icon="heart" label="Saved" />
      <NavLink to="/list/where" className="bottom-nav__fab" aria-label="List a room">
        <Icon name="plus" size={24} strokeWidth={2.6} />
      </NavLink>
      <Item to="/chat" icon="chat" label="Chats" />
      <Item to="/home" icon="home" label="My home" end />
    </nav>
  );
}

export function LandlordNav() {
  return (
    <nav className="bottom-nav" aria-label="Landlord">
      <Item to="/landlord" icon="home" label="Rooms" end />
      <Item to="/landlord/requests" icon="users" label="Requests" />
      <Item to="/landlord/rent-book" icon="book" label="Rent book" />
      <Item to="/chat" icon="chat" label="Chats" />
    </nav>
  );
}
