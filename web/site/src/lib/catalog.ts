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
export const countOS = (apps: App[], os: OS) => apps.filter((a) => hasOS(a, os)).length;
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
