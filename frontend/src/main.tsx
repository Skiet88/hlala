import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import '@fontsource-variable/inter';
import '@fontsource-variable/space-grotesk';
import './styles/app.css';
import { App } from './App';
import { ApiProvider } from './api/ApiProvider';
import { SessionProvider } from './hooks/useSession';
import { ExploreProvider } from './hooks/useExplore';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <ApiProvider>
        <SessionProvider>
          <ExploreProvider>
            <App />
          </ExploreProvider>
        </SessionProvider>
      </ApiProvider>
    </BrowserRouter>
  </StrictMode>,
);

// Offline shell for weak signal. Only in production builds, so development
// always shows fresh code.
if (import.meta.env.PROD && 'serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js').catch(() => {
      // Not fatal: the app works without it, just not offline.
    });
  });
}
