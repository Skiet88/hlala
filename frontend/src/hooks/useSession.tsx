import { createContext, useCallback, useContext, useState, type ReactNode } from 'react';
import type { Role, Session } from '../api/types';

const KEY = 'hlala.session';

function readStored(): Session | null {
  try {
    const raw = localStorage.getItem(KEY);
    return raw ? (JSON.parse(raw) as Session) : null;
  } catch {
    return null;
  }
}

interface SessionState {
  session: Session | null;
  signIn: (s: Session) => void;
  signOut: () => void;
  /** Mock mode only: try the app as another role without signing in again. */
  switchRole: (role: Role) => void;
}

const SessionContext = createContext<SessionState | null>(null);

/**
 * Who is using the app. Until real sign in (Phase 2) this only remembers the
 * mock session in this browser; nothing here is a security boundary.
 */
export function SessionProvider({ children, initial }: { children: ReactNode; initial?: Session | null }) {
  const [session, setSession] = useState<Session | null>(() => (initial !== undefined ? initial : readStored()));

  const persist = useCallback((s: Session | null) => {
    setSession(s);
    try {
      if (s) localStorage.setItem(KEY, JSON.stringify(s));
      else localStorage.removeItem(KEY);
    } catch {
      // Private mode or storage blocked: the session just lasts for this visit.
    }
  }, []);

  const value: SessionState = {
    session,
    signIn: persist,
    signOut: () => persist(null),
    switchRole: (role) =>
      persist({ ...(session ?? { userId: 'u-demo', name: 'Demo', phone: '' }), roles: [role] }),
  };
  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>;
}

export function useSession(): SessionState {
  const s = useContext(SessionContext);
  if (!s) throw new Error('useSession must be used inside SessionProvider');
  return s;
}
