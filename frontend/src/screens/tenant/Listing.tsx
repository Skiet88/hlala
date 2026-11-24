import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useApi } from '../../api/ApiProvider';
import type { Feature } from '../../api/types';
import { useLoad } from '../../hooks/useLoad';
import { formatRand } from '../../domain/money';
import { Icon, type IconName } from '../../components/Icon';
import { BackButton, Loading, Screen } from '../../components/ui';

const FEATURE_ICON: Record<Feature, IconName> = {
  'Own toilet': 'toilet',
  'Own prepaid meter': 'bolt',
  'Water included': 'drop',
  'Burglar gate': 'lock',
  Furnished: 'home',
  'Wi-Fi': 'grid',
  Geyser: 'drop',
  Parking: 'taxi',
};

export function Listing() {
  const { id = '' } = useParams();
  const api = useApi();
  const listing = useLoad(() => api.getListing(id), [id]);
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    api.isSaved(id).then(setSaved).catch(() => setSaved(false));
  }, [api, id]);

  if (!listing.data) {
    return (
      <Screen>
        <BackButton to="/explore" label="Back to explore" />
        <Loading error={listing.error} onRetry={listing.reload} />
      </Screen>
    );
  }
  const l = listing.data;

  return (
    <Screen
      flush
      footer={
        <div className="screen__footer-row">
          <Link to="/chat" className="icon-btn icon-btn--sq" aria-label={`Chat with ${l.landlord.name}`}>
            <Icon name="chat" size={22} color="var(--green)" />
          </Link>
          <Link to={`/listings/${l.id}/viewing`} className="btn btn--primary btn--grow">
            Book a viewing
          </Link>
        </div>
      }
    >
      <div className="photo" style={{ height: 232, flex: 'none' }}>
        <span style={{ fontSize: 12 }}>Photo: inside the room{l.seenInPerson ? ', taken on site' : ''}</span>
        <span style={{ position: 'absolute', left: 16, top: 16 }}>
          <BackButton to="/explore" label="Back to explore" />
        </span>
        <button
          type="button"
          className="icon-btn icon-btn--plain"
          aria-label={saved ? 'Remove from saved' : 'Save this room'}
          aria-pressed={saved}
          style={{ position: 'absolute', right: 16, top: 16 }}
          onClick={() => api.toggleSaved(l.id).then(setSaved)}
        >
          <Icon name="heart" size={20} fill={saved ? 'var(--green)' : 'none'} color={saved ? 'var(--green)' : 'currentColor'} />
        </button>
        {l.photoCount > 0 && (
          <span className="tag tag--dark" style={{ position: 'absolute', right: 16, bottom: 14 }}>
            1 of {l.photoCount}
          </span>
        )}
      </div>

      <div className="screen__body">
        <div className="stack" style={{ gap: 4 }}>
          <div className="row row--between" style={{ alignItems: 'baseline' }}>
            <span className="price price--xl">
              {formatRand(l.rent)} <span className="price__unit">per month</span>
            </span>
            <span className="eyebrow">Deposit {formatRand(l.deposit)}</span>
          </div>
          <h1 style={{ fontSize: 18, lineHeight: '24px', fontWeight: 700 }}>
            {l.title} in {l.section}, {l.area}
          </h1>
          <span className="subtitle">
            Available from {l.availableFrom} · {l.whoFor}
          </span>
        </div>

        {l.seenInPerson ? (
          <div className="panel-dark row row--top" style={{ flexDirection: 'row', padding: 14 }}>
            <span className="avatar avatar--amber avatar--sm">
              <Icon name="check" size={18} strokeWidth={2.8} />
            </span>
            <div className="stack" style={{ gap: 2 }}>
              <span style={{ fontSize: 14, fontWeight: 700 }}>Seen in person{l.checkedOn ? ` on ${l.checkedOn}` : ''}</span>
              <span style={{ fontSize: 13, lineHeight: '18px', color: 'var(--on-dark-2)' }}>
                A Hlala checker from the area visited this room and took these photos on site.
              </span>
            </div>
          </div>
        ) : (
          <p className="note note--cream">
            <strong>Not checked yet.</strong> No Hlala checker has visited this room. Viewing is free, and nobody may ask you
            for money before it.
          </p>
        )}

        {l.features.length > 0 && (
          <ul className="grid-2" style={{ listStyle: 'none', margin: 0, padding: 0, gap: 8 }} aria-label="What comes with the room">
            {l.features.map((f) => (
              <li key={f} className="card row" style={{ minHeight: 44, padding: '0 12px', borderRadius: 12, fontSize: 13, fontWeight: 600 }}>
                <Icon name={FEATURE_ICON[f]} size={18} color="var(--green)" />
                {f}
              </li>
            ))}
          </ul>
        )}

        {l.gettingAround.length > 0 && (
          <section className="stack" aria-labelledby="around">
            <h2 id="around" className="caps">
              Getting around
            </h2>
            <div className="row" style={{ gap: 8 }}>
              {l.gettingAround.map((w) => (
                <div key={w.place} className="stack" style={{ flex: 1, gap: 2, padding: '10px 12px', background: 'var(--mint)', borderRadius: 12 }}>
                  <span style={{ fontSize: 15, fontWeight: 700, color: 'var(--green-dark)' }}>{w.minutes} min walk</span>
                  <span style={{ fontSize: 12, color: 'var(--mint-ink)' }}>{w.place}</span>
                </div>
              ))}
            </div>
          </section>
        )}

        <div className="row" style={{ gap: 12 }}>
          <span className="avatar" aria-hidden="true">
            {l.landlord.initial}
          </span>
          <div className="stack" style={{ gap: 2 }}>
            <span style={{ fontSize: 14, fontWeight: 700 }}>{l.landlord.name}, landlord</span>
            <span className="small">{l.landlord.note}</span>
          </div>
        </div>

        <Link to="/report" className="link-btn">
          Something looks wrong? Report this listing
        </Link>
      </div>
    </Screen>
  );
}
