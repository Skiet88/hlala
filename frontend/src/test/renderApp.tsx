import { render } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { App } from '../App';
import { ApiProvider } from '../api/ApiProvider';
import { MockApiClient } from '../api/mock/MockApiClient';
import { SessionProvider } from '../hooks/useSession';
import { ExploreProvider } from '../hooks/useExplore';

/** Renders the whole app at a route, on a fresh mock with no delay. */
export function renderApp(route: string) {
  const api = new MockApiClient(0);
  const utils = render(
    <MemoryRouter initialEntries={[route]}>
      <ApiProvider client={api}>
        <SessionProvider initial={null}>
          <ExploreProvider>
            <App />
          </ExploreProvider>
        </SessionProvider>
      </ApiProvider>
    </MemoryRouter>,
  );
  return { api, ...utils };
}
