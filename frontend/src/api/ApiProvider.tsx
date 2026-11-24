import { createContext, useContext, useMemo, type ReactNode } from 'react';
import type { ApiClient } from './ApiClient';
import { MockApiClient } from './mock/MockApiClient';
import { createHttpApiClient } from './http/HttpApiClient';

const ApiContext = createContext<ApiClient | null>(null);

/** Picks the implementation once: VITE_API_MODE=http uses the backend, anything else the mock. */
export function defaultApiClient(): ApiClient {
  if (import.meta.env.VITE_API_MODE === 'http') {
    return createHttpApiClient(import.meta.env.VITE_API_BASE_URL ?? 'https://hlala.onrender.com');
  }
  return new MockApiClient();
}

export function ApiProvider({ client, children }: { client?: ApiClient; children: ReactNode }) {
  const value = useMemo(() => client ?? defaultApiClient(), [client]);
  return <ApiContext.Provider value={value}>{children}</ApiContext.Provider>;
}

export function useApi(): ApiClient {
  const api = useContext(ApiContext);
  if (!api) throw new Error('useApi must be used inside ApiProvider');
  return api;
}
