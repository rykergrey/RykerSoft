import { Search } from 'lucide-react';
import { useEffect, useMemo, useRef, useState } from 'react';
import { Toaster } from 'sonner';
import { type Catalog, detectOS, loadCatalog } from './lib/catalog';
import { useFilters, useRoute } from './lib/state';
import { Button } from './components/kit';
import { SystemMenu } from './components/menus';
import { Detail } from './pages/Detail';
import { Home } from './pages/Home';

export function App() {
  const [catalog, setCatalog] = useState<Catalog | null>(null);
  const [error, setError] = useState('');
  const [attempt, setAttempt] = useState(0);
  const { filters, update, reset } = useFilters();
  const routeId = useRoute();
  const mine = useMemo(detectOS, []);
  const search = useRef<HTMLInputElement>(null);
  const homeScroll = useRef(0);

  useEffect(() => {
    setError('');
    loadCatalog().then(setCatalog, (e: Error) => setError(e.message));
  }, [attempt]);

  // "/" focuses search from anywhere; Escape leaves it.
  useEffect(() => {
    const on = (e: KeyboardEvent) => {
      const el = search.current;
      if (!el) return;
      const typing = document.activeElement instanceof HTMLInputElement || document.activeElement instanceof HTMLTextAreaElement;
      if (e.key === '/' && !typing) { e.preventDefault(); el.focus(); el.select(); }
      if (e.key === 'Escape' && document.activeElement === el) el.blur();
    };
    addEventListener('keydown', on);
    return () => removeEventListener('keydown', on);
  }, []);

  const app = catalog && routeId ? catalog.apps.find((a) => a.id === routeId) : undefined;

  // Coming back from an app returns to where the catalog was scrolled.
  useEffect(() => {
    if (app) return;
    const on = () => { homeScroll.current = scrollY; };
    scrollTo(0, homeScroll.current);
    addEventListener('scroll', on, { passive: true });
    return () => removeEventListener('scroll', on);
  }, [app]);

  useEffect(() => { document.title = app ? `${app.name} · RykerSoft` : 'RykerSoft'; }, [app]);

  return (
    <>
      <header className="sticky top-0 z-sticky border-b-2 border-border-soft bg-bg/94 backdrop-blur-sm">
        <div className="flex flex-wrap items-center gap-x-3.5 gap-y-2.5 max-w-page mx-auto px-4 md:px-6 py-2.5 md:min-h-15">
          <a href="#/" aria-label="RykerSoft home" className="block h-6 md:h-7.5 shrink-0">
            <img src="./logo.png" alt="" className="h-full w-auto invert drop-shadow-brand" />
          </a>
          <label className="relative order-3 md:order-none basis-full md:basis-auto md:flex-1 md:max-w-130 md:ml-2.5">
            <span className="sr-only">Search apps and games</span>
            <Search aria-hidden="true" className="absolute left-2.5 top-1/2 -translate-y-1/2 size-4 text-faint pointer-events-none" />
            <input ref={search} type="search" autoComplete="off" placeholder="Search apps and games" value={filters.q}
              onChange={(e) => { update({ q: e.target.value }); if (routeId) location.hash = '#/'; }}
              className="w-full h-9 pointer-coarse:h-11 pl-8.5 pr-9 border-2 border-border rounded-sm bg-surface font-mono text-sm font-medium text-text placeholder:text-faint focus-visible:border-link focus-visible:outline-none" />
            <kbd aria-hidden="true" className="max-md:hidden absolute right-2 top-1/2 -translate-y-1/2 grid place-items-center min-w-5 h-5 border border-border rounded-sm font-mono text-meta text-faint">/</kbd>
          </label>
          <div className="ml-auto md:ml-0">
            {catalog && <SystemMenu apps={catalog.apps} value={filters.os} mine={mine} onChange={(os) => { update({ os }); if (routeId) location.hash = '#/'; }} />}
          </div>
        </div>
      </header>

      <main className="max-w-page mx-auto px-4 md:px-6 min-h-150">
        {error ? (
          <div role="alert" className="mt-10 px-5 py-12 border-2 border-dashed border-border text-center text-dim">
            <p className="m-0 mb-3.5">{error}</p>
            <Button onClick={() => setAttempt((n) => n + 1)}>Try again</Button>
          </div>
        ) : !catalog ? (
          <p role="status" className="label-mono pt-10">Loading the catalog…</p>
        ) : app ? (
          <Detail app={app} mine={mine} />
        ) : (
          <Home apps={catalog.apps} filters={filters} update={update} reset={reset} mine={mine} />
        )}
      </main>

      <footer className="max-w-page mx-auto px-4 md:px-6">
        <div className="flex flex-wrap gap-x-5.5 gap-y-1.5 pt-4.5 pb-7.5 border-t-2 border-border-soft font-mono text-label text-faint">
          <span>Built by Ryker Grey, a hobbyist. No ads, no tracking.</span>
          <a href="https://github.com/rykergrey/RykerSoft-APKs/releases" className="text-dim">All release files</a>
          {catalog && <span>Catalog updated {catalog.generatedAt.slice(0, 10)}</span>}
        </div>
      </footer>
      <Toaster position="bottom-center" toastOptions={{ unstyled: true, className: 'px-3.5 py-2.5 border-2 border-border bg-surface shadow-hard-sm font-mono text-label font-medium text-text' }} />
    </>
  );
}
