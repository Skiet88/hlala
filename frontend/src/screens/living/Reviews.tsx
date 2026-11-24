import { useState } from 'react';
import { useApi } from '../../api/ApiProvider';
import { useAction, useLoad } from '../../hooks/useLoad';
import { Icon } from '../../components/Icon';
import { Chip, ErrorNote, Loading, Screen } from '../../components/ui';

const WORDS = ['Bad', 'Not good', 'Okay', 'Good place', 'Great place'];

export function Reviews() {
  const api = useApi();
  const draft = useLoad(() => api.reviewDraft(), []);
  const [stars, setStars] = useState(4);
  const [tags, setTags] = useState<string[]>(['Room as shown', 'Fixed things fast', 'Safe yard']);
  const [words, setWords] = useState('');
  const [posted, setPosted] = useState(false);
  const { busy, error, run } = useAction();
  const d = draft.data;

  async function post() {
    if (!d) return;
    const ok = await run(() => api.postReview(d.leaseId, stars, tags, words).then(() => true));
    if (ok) setPosted(true);
  }

  return (
    <Screen
      footer={
        d &&
        !posted && (
          <>
            <ErrorNote message={error} />
            <button type="button" className="btn btn--primary btn--block" onClick={post} disabled={busy}>
              Post review
            </button>
          </>
        )
      }
    >
      {!d ? (
        <Loading error={draft.error} onRetry={draft.reload} />
      ) : posted ? (
        <div className="state" role="status" style={{ color: 'var(--ink)' }}>
          <span className="avatar" style={{ width: 56, height: 56, borderRadius: 28 }}>
            <Icon name="check" size={26} color="#fff" strokeWidth={3} />
          </span>
          <h1 className="title">Review sealed</h1>
          <p className="lead">It goes live the moment {d.aboutName} posts theirs, so neither of you writes in reaction to the other.</p>
        </div>
      ) : (
        <>
          <div className="stack" style={{ gap: 4 }}>
            <span className="eyebrow">
              {d.placeTitle} · {d.months} months
            </span>
            <h1 className="title title--xl">How was living at {d.aboutName}'s place?</h1>
          </div>

          <div className="card card--pad" style={{ alignItems: 'center' }}>
            <div className="row" role="radiogroup" aria-label="Stars" style={{ gap: 4 }}>
              {[1, 2, 3, 4, 5].map((i) => (
                <button
                  key={i}
                  type="button"
                  role="radio"
                  aria-checked={i === stars}
                  aria-label={`${i} out of 5`}
                  className="icon-btn icon-btn--plain"
                  onClick={() => setStars(i)}
                  style={{ width: 48, height: 48 }}
                >
                  <Icon name="star" size={34} fill={i <= stars ? 'var(--amber)' : 'none'} color={i <= stars ? 'var(--amber)' : 'var(--line)'} strokeWidth={1.8} />
                </button>
              ))}
            </div>
            <span style={{ fontSize: 15, fontWeight: 700 }}>{WORDS[stars - 1]}</span>
          </div>

          <section className="stack" aria-labelledby="true-h">
            <h2 id="true-h" className="caps">
              What was true about this place
            </h2>
            <div className="chip-row">
              {d.tagOptions.map((t) => (
                <Chip key={t} label={t} pressed={tags.includes(t)} onClick={() => setTags(tags.includes(t) ? tags.filter((x) => x !== t) : [...tags, t])} />
              ))}
            </div>
          </section>

          <div className="field">
            <label htmlFor="words">Anything the next tenant should know</label>
            <input id="words" className="input" placeholder="Say it in your own words" value={words} onChange={(e) => setWords(e.target.value)} />
          </div>

          <p className="note note--mint">
            {d.aboutName} is rating you too. Both reviews stay sealed and go live together, so nobody can hit back. Only people who
            really rented here can review.
          </p>
        </>
      )}
    </Screen>
  );
}
