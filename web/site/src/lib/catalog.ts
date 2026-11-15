export type OS = 'android' | 'windows' | 'linux';
export const OS_LIST: OS[] = ['android', 'windows', 'linux'];
export const OS_NAME: Record<OS, string> = { android: 'Android', windows: 'Windows', linux: 'Linux' };

export interface Build {
  os: OS;
  format: string;
  url: string;
  file: string;
  version: string;
  size: number | null;
  date: string;
  sha256: string;
}
export interface Release { version: string; date: string; notes: string[] }
export interface App {
  id: string;
  name: string;
  kind: 'app' | 'game';
  version: string;
  date: string;
  tagline: string;
  description: string;
  features: string[];
  pro: string;
  icon: string;
  shots: string[];
  packages: string[];
  /** Address of a version that runs in the browser, or ''. */
  web?: string;
  /** The web edition's own version and build date, from its web-edition.json. '' when it publishes none. */
  webVersion?: string;
  webDate?: string;
  builds: Build[];
  releases: Release[];
}
export interface Catalog { generatedAt: string; apps: App[] }

export async function loadCatalog(): Promise<Catalog> {
  const r = await fetch('./catalog.json', { cache: 'no-cache' });
  if (!r.ok) throw new Error(`The catalog could not be loaded (HTTP ${r.status}).`);
  return r.json();
}

export const hasOS = (a: App, os: OS) => a.builds.some((b) => b.os === os);
/** Where the app runs in this browser, or ''. The hub's own web address is this site, which is not an app to open. */
export function webEdition(a: App): string {
  try {
    return a.web && new URL(a.web).origin !== location.origin ? a.web : '';
  } catch {
    return '';
  }
}
export const countWeb = (apps: App[]) => apps.filter(webEdition).length;
export const countOS = (apps: App[], os: OS) => apps.filter((a) => hasOS(a, os)).length;
/** True when dotted version `a` is lower than `b` ("0.4.8" is not older than "0.4.3"). */
export function isOlderVersion(a: string, b: string): boolean {
  const parts = (v: string) => v.split(/[.\-+]/).map((n) => Number.parseInt(n, 10) || 0);
  const x = parts(a), y = parts(b);
  for (let i = 0; i < Math.max(x.length, y.length); i++) if ((x[i] ?? 0) !== (y[i] ?? 0)) return (x[i] ?? 0) < (y[i] ?? 0);
  return false;
}
/** A build is behind when its own version is not the app's current version. */
export const isBehind = (a: App, b: Build) => !!b.version && b.version !== a.version;

export function fileSize(n: number | null): string {
  if (n == null) return '';
  if (n < 1048576) return `${Math.max(1, Math.round(n / 1024))} KB`;
  const mb = n / 1048576;
  return `${mb >= 100 ? Math.round(mb) : mb.toFixed(1).replace(/\.0$/, '')} MB`;
}

export function shortDate(d: string): string {
  if (!d) return '';
  return new Date(`${d}T12:00:00`).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

/** Two letters for an app with no icon. */
export function monogram(name: string): string {
  return name.replace(/[^A-Za-z ]/g, '').split(/\s+|(?=[A-Z][a-z])/).filter(Boolean).slice(0, 2).map((w) => w[0].toUpperCase()).join('');
}

/** The viewer's own system. `?as=windows` overrides it, for checking how the site looks elsewhere. */
export function detectOS(): OS | null {
  const forced = new URLSearchParams(location.search).get('as');
  if (forced === 'android' || forced === 'windows' || forced === 'linux') return forced;
  const ua = navigator.userAgent;
  if (/Android/i.test(ua)) return 'android';
  if (/Windows/i.test(ua)) return 'windows';
  if (/Linux|X11/i.test(ua)) return 'linux';
  return null;
}
