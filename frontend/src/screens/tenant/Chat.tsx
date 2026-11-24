import { useEffect, useRef, useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { useApi } from '../../api/ApiProvider';
import type { ChatMessage } from '../../api/types';
import { useAction, useLoad } from '../../hooks/useLoad';
import { Icon } from '../../components/Icon';
import { BackButton, ErrorNote, Loading, useToast } from '../../components/ui';

function Bubble({ m }: { m: ChatMessage }) {
  if (m.kind === 'meeting') return <div className="bubble bubble--system">{m.text}</div>;
  const side = m.from === 'me' ? 'bubble--me' : 'bubble--them';
  if (m.kind === 'voice') {
    return (
      <div className={`bubble ${side} voice`}>
        <span className="voice__wave" aria-hidden="true" />
        <span style={{ fontSize: 13, fontWeight: 600 }}>Voice note 0:{String(m.seconds).padStart(2, '0')}</span>
      </div>
    );
  }
  return <div className={`bubble ${side}`}>{m.text}</div>;
}

export function Chat() {
  const api = useApi();
  const thread = useLoad(() => api.chatThread(), []);
  const [text, setText] = useState('');
  const { busy, error, run } = useAction();
  const [toast, showToast] = useToast();
  const end = useRef<HTMLDivElement>(null);

  useEffect(() => {
    end.current?.scrollIntoView?.({ block: 'end' });
  }, [thread.data?.messages.length]);

  async function send(message: string) {
    if (!thread.data) return;
    const updated = await run(() => api.sendMessage(thread.data!.id, message));
    if (updated) {
      thread.set(updated);
      setText('');
    }
  }

  function onSubmit(e: FormEvent) {
    e.preventDefault();
    void send(text);
  }

  const t = thread.data;
  return (
    <div className="screen">
      <div className="topbar" style={{ padding: '12px 16px', background: 'var(--card)', borderBottom: '1px solid var(--line-soft)' }}>
        <BackButton label="Back" />
        {t && (
          <>
            <span className="avatar" aria-hidden="true">
              {t.landlord.initial}
            </span>
            <div className="topbar__titles">
              <h1 style={{ fontSize: 16, fontWeight: 700 }}>{t.landlord.name}</h1>
              <span className="small">
                {t.listingTitle} · {t.landlord.note}
              </span>
            </div>
            <button type="button" className="icon-btn" aria-label="Call through the app" onClick={() => showToast('Calls through the app are not built yet')}>
              <Icon name="phone" size={18} />
            </button>
          </>
        )}
      </div>

      <main className="screen__body" style={{ gap: 10 }} aria-label="Messages">
        {!t ? (
          <Loading error={thread.error} onRetry={thread.reload} />
        ) : (
          <>
            {t.viewing && (
              <div className="panel-dark row row--between" style={{ flexDirection: 'row', padding: '12px 14px' }}>
                <div className="stack" style={{ gap: 2 }}>
                  <span style={{ fontSize: 13, fontWeight: 700, color: 'var(--amber)' }}>Viewing booked</span>
                  <span style={{ fontSize: 14, fontWeight: 600 }}>
                    {t.viewing.slot.day} November at {t.viewing.slot.time}
                  </span>
                </div>
                <button type="button" className="btn btn--ghost-light btn--sm" onClick={() => showToast('The exact pin shows here once the landlord accepts')}>
                  Directions
                </button>
              </div>
            )}
            <p className="note note--cream">
              Keep money inside Hlala. If anyone asks you to pay before the viewing, <Link to="/report">report it here</Link>.
            </p>
            {t.messages.map((m) => (
              <Bubble key={m.id} m={m} />
            ))}
            <div className="chip-row" style={{ justifyContent: 'flex-end' }}>
              {t.quickReplies.map((q) => (
                <button key={q} type="button" className="chip" onClick={() => void send(q)} disabled={busy}>
                  {q}
                </button>
              ))}
              <Link to={`/apply/${t.listingId}`} className="chip" style={{ background: 'var(--amber)', borderColor: 'var(--amber)', fontWeight: 700, display: 'inline-flex', alignItems: 'center', textDecoration: 'none' }}>
                I want this room: apply
              </Link>
            </div>
            <ErrorNote message={error} />
            <div ref={end} />
          </>
        )}
      </main>

      <form className="screen__footer" onSubmit={onSubmit} style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
        <label htmlFor="msg" className="visually-hidden">
          Message
        </label>
        <input
          id="msg"
          className="input"
          placeholder="Write a message"
          value={text}
          onChange={(e) => setText(e.target.value)}
          style={{ flex: 1, minWidth: 0, background: 'var(--bg)', borderRadius: 24 }}
          autoComplete="off"
        />
        <button type="button" className="icon-btn" aria-label="Record a voice note" onClick={() => showToast('Voice notes arrive with file storage')}>
          <Icon name="mic" size={18} />
        </button>
        <button type="submit" className="icon-btn" aria-label="Send" disabled={busy || text.trim() === ''} style={{ background: 'var(--green)', borderColor: 'var(--green)', color: '#fff' }}>
          <Icon name="send" size={18} />
        </button>
      </form>
      {toast}
    </div>
  );
}
