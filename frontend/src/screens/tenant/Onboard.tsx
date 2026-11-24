import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useApi } from '../../api/ApiProvider';
import type { Role } from '../../api/types';
import { useSession } from '../../hooks/useSession';
import { useAction } from '../../hooks/useLoad';
import { Icon } from '../../components/Icon';
import { ErrorNote, Screen } from '../../components/ui';

const HOME: Record<Role, string> = { TENANT: '/explore', LANDLORD: '/landlord', CHECKER: '/checker', ADMIN: '/admin' };

export function Onboard() {
  const api = useApi();
  const { signIn } = useSession();
  const navigate = useNavigate();
  const [role, setRole] = useState<Role>('TENANT');
  const [phone, setPhone] = useState('');
  const [code, setCode] = useState('');
  const [codeSent, setCodeSent] = useState(false);
  const { busy, error, run } = useAction();

  async function sendCode(e: FormEvent) {
    e.preventDefault();
    const sent = await run(() => api.requestCode(phone).then(() => true));
    if (sent) setCodeSent(true);
  }

  async function confirm(e: FormEvent) {
    e.preventDefault();
    const session = await run(() => api.verifyCode(phone, code, role));
    if (session) {
      signIn(session);
      navigate(HOME[role]);
    }
  }

  return (
    <Screen tone="dark">
      <div className="stack" style={{ gap: 10, paddingTop: 16 }}>
        <span style={{ fontFamily: 'var(--font-head)', fontSize: 44, lineHeight: '44px', fontWeight: 700, letterSpacing: -1.5, color: 'var(--amber)' }}>
          Hlala
        </span>
        <h1 className="title title--xl" style={{ fontSize: 28, lineHeight: '33px' }}>
          A safe room ekasi, close to your taxi.
        </h1>
        <p style={{ fontSize: 15, lineHeight: '21px', color: 'var(--on-dark)' }}>
          Every badge room is checked in person. Your deposit is held until you have the keys.
        </p>
      </div>

      <div className="stack" role="group" aria-label="What brings you here">
        <button
          type="button"
          className="option"
          aria-pressed={role === 'TENANT'}
          onClick={() => setRole('TENANT')}
          style={{ background: 'var(--green)', border: role === 'TENANT' ? '2px solid var(--amber)' : '2px solid transparent', color: '#fff', borderRadius: 18, padding: 16, gap: 14 }}
        >
          <span className="avatar" style={{ width: 48, height: 48, borderRadius: 24, background: '#fff' }}>
            <Icon name="search" size={22} color="var(--green)" strokeWidth={2.4} />
          </span>
          <span className="stack" style={{ gap: 2 }}>
            <span style={{ fontFamily: 'var(--font-head)', fontSize: 18, fontWeight: 700 }}>I am looking for a room</span>
            <span style={{ fontSize: 13, color: '#e1f0e9' }}>Search, view and pay safely</span>
          </span>
        </button>
        <button
          type="button"
          className="option"
          aria-pressed={role === 'LANDLORD'}
          onClick={() => setRole('LANDLORD')}
          style={{ background: 'var(--ink-2)', border: role === 'LANDLORD' ? '2px solid var(--amber)' : '2px solid var(--muted)', color: '#fff', borderRadius: 18, padding: 16, gap: 14 }}
        >
          <span className="avatar avatar--amber" style={{ width: 48, height: 48, borderRadius: 24 }}>
            <Icon name="home" size={22} strokeWidth={2.4} />
          </span>
          <span className="stack" style={{ gap: 2 }}>
            <span style={{ fontFamily: 'var(--font-head)', fontSize: 18, fontWeight: 700 }}>I have a room to let</span>
            <span style={{ fontSize: 13, color: 'var(--on-dark)' }}>List it, pick a tenant, track rent</span>
          </span>
        </button>
      </div>

      {!codeSent ? (
        <form className="stack" onSubmit={sendCode} style={{ gap: 6 }}>
          <label htmlFor="cell" className="caps" style={{ color: 'var(--on-dark)' }}>
            Sign in with your cell number
          </label>
          <div className="row">
            <input
              id="cell"
              className="input input--dark"
              type="tel"
              inputMode="tel"
              autoComplete="tel"
              placeholder="071 234 5678"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              style={{ flex: 1, minWidth: 0 }}
            />
            <button type="submit" className="btn btn--white" disabled={busy || phone.trim() === ''}>
              Send code
            </button>
          </div>
        </form>
      ) : (
        <form className="stack" onSubmit={confirm} style={{ gap: 6 }}>
          <label htmlFor="code" className="caps" style={{ color: 'var(--on-dark)' }}>
            Enter the 6 numbers we sent to {phone}
          </label>
          <div className="row">
            <input
              id="code"
              className="input input--dark"
              inputMode="numeric"
              autoComplete="one-time-code"
              maxLength={6}
              placeholder="123456"
              value={code}
              onChange={(e) => setCode(e.target.value.replace(/\D/g, ''))}
              style={{ flex: 1, minWidth: 0, letterSpacing: 4 }}
            />
            <button type="submit" className="btn btn--amber" disabled={busy || code.length !== 6}>
              Confirm
            </button>
          </div>
          <button type="button" className="link-btn" style={{ color: 'var(--amber)' }} onClick={() => setCodeSent(false)}>
            Use a different number
          </button>
        </form>
      )}
      <ErrorNote message={error} />

      <Link to="/explore" className="link-btn" style={{ color: 'var(--amber)' }}>
        Just look at rooms first
      </Link>

      <p style={{ marginTop: 'auto', fontSize: 13, lineHeight: '18px', color: 'var(--on-dark)' }}>
        No password to remember. Built to use very little data, and the search still opens when the signal is weak.
        {import.meta.env.VITE_API_MODE !== 'http' && ' Sample mode: any 6 numbers work as the code.'}
      </p>
    </Screen>
  );
}
