import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApi } from '../../api/ApiProvider';
import type { ReportReason } from '../../api/types';
import { useAction } from '../../hooks/useLoad';
import { Icon } from '../../components/Icon';
import { ErrorNote, Screen, TopBar, useToast } from '../../components/ui';

const REASONS: ReportReason[] = [
  'Asked for money before the viewing',
  'Room is not like the photos',
  'Listing looks fake or copied',
  'Landlord will not return my deposit',
  'Tenant is not paying or damaged the room',
  'I felt unsafe at the viewing',
];

export function Report() {
  const api = useApi();
  const navigate = useNavigate();
  const [reason, setReason] = useState<ReportReason>(REASONS[0]);
  const [details, setDetails] = useState('');
  const { busy, error, run } = useAction();
  const [toast, showToast] = useToast();

  async function send() {
    const created = await run(() => api.report(reason, details));
    if (created) navigate(`/disputes/${created.id}`);
  }

  return (
    <Screen
      footer={
        <>
          <ErrorNote message={error} />
          <button type="button" className="btn btn--primary btn--block" onClick={send} disabled={busy}>
            Send report
          </button>
          <span className="footer-note">
            In danger right now? Call <a href="tel:10111">10111</a> or <a href="tel:112">112</a> from your cell.
          </span>
        </>
      }
    >
      <TopBar back={true} title="Report a problem" />
      <p className="panel-dark" style={{ fontSize: 14, lineHeight: '20px' }}>
        <span>
          <strong style={{ color: 'var(--amber)' }}>Your money on hold does not move</strong> while a report is open. Nobody gets
          paid until it is sorted.
        </span>
      </p>

      <section className="stack" aria-labelledby="what">
        <h2 id="what" className="caps">
          What happened
        </h2>
        <div className="stack" role="radiogroup" aria-labelledby="what">
          {REASONS.map((r) => (
            <button key={r} type="button" role="radio" aria-checked={r === reason} className="option" style={{ minHeight: 52 }} onClick={() => setReason(r)}>
              <span className={`radio-dot${r === reason ? ' radio-dot--on' : ''}`} />
              <span style={{ fontSize: 14, fontWeight: 600 }}>{r}</span>
            </button>
          ))}
        </div>
      </section>

      <div className="field">
        <label htmlFor="more">Tell us more, or send a voice note</label>
        <div className="row" style={{ gap: 8 }}>
          <input id="more" className="input" placeholder="What did they say or do?" value={details} onChange={(e) => setDetails(e.target.value)} style={{ flex: 1, minWidth: 0 }} />
          <button type="button" className="icon-btn icon-btn--sq" style={{ width: 48, height: 48, borderRadius: 12 }} aria-label="Record a voice note" onClick={() => showToast('Voice notes arrive with file storage')}>
            <Icon name="mic" size={18} />
          </button>
          <button type="button" className="icon-btn icon-btn--sq" style={{ width: 48, height: 48, borderRadius: 12 }} aria-label="Attach a screenshot or photo" onClick={() => showToast('Attachments arrive with file storage')}>
            <Icon name="paperclip" size={18} />
          </button>
        </div>
      </div>
      {toast}
    </Screen>
  );
}
