import { useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useApi } from '../../api/ApiProvider';
import { useAction, useLoad } from '../../hooks/useLoad';
import { formatRand } from '../../domain/money';
import { ErrorNote, Loading, Photo, Screen, TopBar } from '../../components/ui';

const MONTH = 'November';

export function Viewing() {
  const { id = '' } = useParams();
  const api = useApi();
  const navigate = useNavigate();
  const listing = useLoad(() => api.getListing(id), [id]);
  const slots = useLoad(() => api.viewingSlots(id), [id]);
  const days = useMemo(() => [...new Set((slots.data ?? []).map((s) => s.day))], [slots.data]);
  const times = useMemo(() => [...new Set((slots.data ?? []).map((s) => s.time))], [slots.data]);
  const [day, setDay] = useState('Sat 21');
  const [time, setTime] = useState('11:00');
  const { busy, error, run } = useAction();

  async function confirm() {
    const booked = await run(() => api.bookViewing(id, { day, time }));
    if (booked) navigate('/chat');
  }

  const l = listing.data;
  return (
    <Screen
      footer={
        <>
          <ErrorNote message={error} />
          <div className="row row--between">
            <span style={{ fontSize: 14, fontWeight: 600 }}>
              {day} {MONTH} at {time}
              {l ? `, ${l.section}` : ''}
            </span>
          </div>
          <button type="button" className="btn btn--primary btn--block" onClick={confirm} disabled={busy || !l}>
            Confirm free viewing
          </button>
        </>
      }
    >
      <TopBar back={`/listings/${id}`} backLabel="Back to room details" title="Book a viewing" />

      {!l || !slots.data ? (
        <Loading error={listing.error ?? slots.error} onRetry={() => {
            listing.reload();
            slots.reload();
          }} />
      ) : (
        <>
          <div className="card row" style={{ padding: 10, gap: 12 }}>
            <Photo width={64} height={64} radius={10} />
            <div className="stack" style={{ gap: 2 }}>
              <span style={{ fontSize: 14, fontWeight: 700 }}>
                {l.title} in {l.section}
              </span>
              <span className="small">
                {formatRand(l.rent)} per month · {l.landlord.name}
              </span>
            </div>
          </div>

          <section className="stack" aria-labelledby="pick-day">
            <h2 id="pick-day" className="caps">
              Pick a day
            </h2>
            <div className="chip-grid">
              {days.map((d) => {
                const [dow, num] = d.split(' ');
                return (
                  <button
                    key={d}
                    type="button"
                    className="chip"
                    aria-pressed={d === day}
                    onClick={() => setDay(d)}
                    style={{ minHeight: 64, borderRadius: 16, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}
                  >
                    <span style={{ fontSize: 12 }}>{dow}</span>
                    <span style={{ fontFamily: 'var(--font-head)', fontSize: 20, fontWeight: 700 }}>{num}</span>
                  </button>
                );
              })}
            </div>
          </section>

          <section className="stack" aria-labelledby="pick-time">
            <h2 id="pick-time" className="caps">
              Pick a time
            </h2>
            <div className="chip-grid">
              {times.map((t) => (
                <button key={t} type="button" className="chip" aria-pressed={t === time} onClick={() => setTime(t)}>
                  {t}
                </button>
              ))}
            </div>
          </section>

          <section className="panel-dark" aria-labelledby="safe">
            <h2 id="safe" style={{ fontSize: 14, fontWeight: 700 }}>
              How your money stays safe
            </h2>
            {[
              ['See the room first.', 'Viewing is free and nobody may ask you for money before it.'],
              ['Deposit goes on hold.', 'If you want the room, your deposit is held by Hlala, not sent to the landlord.'],
              ['Keys in hand, then release.', 'The landlord is paid only when you confirm you have moved in.'],
            ].map(([head, body], i) => (
              <div key={head} className="row row--top" style={{ gap: 10 }}>
                <span className="count-dot" style={{ minWidth: 24, height: 24, borderRadius: 12 }}>
                  {i + 1}
                </span>
                <span style={{ fontSize: 13, lineHeight: '18px', color: 'var(--on-dark-2)' }}>
                  <strong style={{ color: '#fff' }}>{head}</strong> {body}
                </span>
              </div>
            ))}
          </section>
        </>
      )}
    </Screen>
  );
}
