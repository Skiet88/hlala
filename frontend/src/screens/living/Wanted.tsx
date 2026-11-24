import { Link } from 'react-router-dom';
import { useApi } from '../../api/ApiProvider';
import { useLoad } from '../../hooks/useLoad';
import { formatRand } from '../../domain/money';
import { Loading, Photo, Screen, TopBar, useToast } from '../../components/ui';

export function Wanted() {
  const api = useApi();
  const post = useLoad(() => api.wantedPost(), []);
  const [toast, showToast] = useToast();
  const w = post.data;

  return (
    <Screen
      footer={
        w && (
          <div className="screen__footer-row">
            <button type="button" className="btn btn--secondary" onClick={() => api.setWantedLive(!w.live).then(post.set)}>
              {w.live ? 'Take post down' : 'Put it back up'}
            </button>
            <button type="button" className="btn btn--dark btn--grow" onClick={() => showToast('Your post stays up 7 more days')} disabled={!w.live}>
              Keep it up 7 more days
            </button>
          </div>
        )
      }
    >
      <TopBar back={true} title="Room wanted" />
      {!w ? (
        <Loading error={post.error} onRetry={post.reload} />
      ) : (
        <>
          <div className="panel-dark">
            <div className="row row--between">
              <span className={`tag ${w.live ? 'tag--amber' : 'tag--outline-light'}`}>{w.live ? 'Your post is live' : 'Your post is down'}</span>
              <button type="button" className="link-btn" style={{ color: 'var(--amber)' }} onClick={() => showToast('Editing the post is not built yet')}>
                Edit
              </button>
            </div>
            <span style={{ fontSize: 16, fontWeight: 700 }}>{w.summary}</span>
            <div className="row row--wrap" style={{ gap: 6 }}>
              {w.tags.map((t) => (
                <span key={t} className="tag tag--outline-light">
                  {t}
                </span>
              ))}
            </div>
            <span className="muted" style={{ fontSize: 13 }}>
              Landlords see your profile ticks and this post. They never see your cell number.
            </span>
          </div>

          <h2 className="title" style={{ fontSize: 18 }}>
            {w.offers.length} {w.offers.length === 1 ? 'landlord' : 'landlords'} answered
          </h2>
          {w.offers.map((o) => (
            <article key={o.id} className="card card--pad" aria-label={o.title}>
              <div className="row" style={{ gap: 12 }}>
                <Photo width={64} height={64} radius={10} />
                <div className="list-row__text">
                  <span className="list-row__title">{o.title}</span>
                  <span className="list-row__note">{o.meta}</span>
                </div>
                <span className="price" style={{ fontSize: 16 }}>
                  {formatRand(o.rent)}
                </span>
              </div>
              <div className="screen__footer-row">
                <button type="button" className="btn btn--secondary btn--sm" onClick={() => api.hideOffer(o.id).then(post.set)}>
                  Not for me
                </button>
                <Link to={`/listings/${o.listingId}`} className="btn btn--primary btn--sm btn--grow">
                  View room
                </Link>
              </div>
            </article>
          ))}
        </>
      )}
      {toast}
    </Screen>
  );
}
