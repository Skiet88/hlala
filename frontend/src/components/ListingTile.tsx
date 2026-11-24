import { Link } from 'react-router-dom';
import type { ListingSummary } from '../api/types';
import { formatRand } from '../domain/money';
import { SeenBadge } from './ui';

export function ListingTile({ listing }: { listing: ListingSummary }) {
  return (
    <Link to={`/listings/${listing.id}`} className="tile">
      <div className="photo tile__photo" aria-hidden="true">
        <span>Photo</span>
        {listing.seenInPerson && (
          <span className="tile__badge">
            <SeenBadge />
          </span>
        )}
      </div>
      <div className="tile__body">
        <span className="price">{formatRand(listing.rent)}</span>
        <span className="tile__title">{listing.title}</span>
        <span className="tile__meta">{listing.meta}</span>
        {!listing.seenInPerson && <span className="visually-hidden">Not checked yet</span>}
      </div>
    </Link>
  );
}
