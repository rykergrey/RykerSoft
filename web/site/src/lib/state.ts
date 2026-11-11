import { useCallback, useEffect, useState } from 'react';
import type { OS } from './catalog';

export interface Filters { os: OS | 'all'; kind: 'all' | 'app' | 'game'; sort: 'date' | 'name'; q: string }
const KEY = 'rs-hub';
const DEFAULTS: Filters = { os: 'all', kind: 'all', sort: 'date', q: '' };

function initial(): Filters {
  let saved: Partial<Filters> = {};
  try { saved = JSON.parse(sessionStorage.getItem(KEY) || '{}'); } catch { /* private window */ }
  const os = new URLSearchParams(location.search).get('os');
  return { ...DEFAULTS, ...saved, ...(os === 'android' || os === 'windows' || os === 'linux' || os === 'all' ? { os } : {}) };
}

/** Filters live for the browser tab: opening an app and coming back keeps them (closing never destroys). */
export function useFilters() {
  const [filters, setFilters] = useState<Filters>(initial);
  const update = useCallback((patch: Partial<Filters>) => setFilters((f) => {
    const next = { ...f, ...patch };
    try { sessionStorage.setItem(KEY, JSON.stringify(next)); } catch { /* private window */ }
    return next;
  }), []);
  const reset = useCallback(() => update(DEFAULTS), [update]);
  return { filters, update, reset };
}

/** `#/app/<id>` opens an app. Hash routing needs no server rewrite rules. */
export function useRoute(): string | null {
  const read = () => (location.hash.match(/^#\/app\/(.+)$/) || [])[1] ?? null;
  const [id, setId] = useState<string | null>(read);
  useEffect(() => {
    const on = () => setId(read());
    addEventListener('hashchange', on);
    return () => removeEventListener('hashchange', on);
  }, []);
  return id ? decodeURIComponent(id) : null;
}
