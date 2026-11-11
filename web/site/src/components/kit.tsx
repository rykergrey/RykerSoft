import type { AnchorHTMLAttributes, ButtonHTMLAttributes, ReactNode } from 'react';
import { type App, type OS, monogram } from '../lib/catalog';

const cx = (...c: (string | false | null | undefined)[]) => c.filter(Boolean).join(' ');

/* Buttons. Tappable means contained: every button has a border and a fill. */
const BTN = 'inline-flex items-center justify-center gap-2 min-h-9 pointer-coarse:min-h-11 px-3 border-2 rounded-sm font-mono text-label leading-none uppercase tracking-label whitespace-nowrap no-underline select-none transition-press [&_svg]:size-4 [&_svg]:shrink-0';
const VARIANT = {
  plain: 'border-border bg-raised text-text font-semibold hover:border-dim active:translate-x-px active:translate-y-px',
  primary: 'border-shadow bg-action text-action-ink font-bold shadow-hard-sm hover:-translate-x-px hover:-translate-y-px hover:shadow-hard active:translate-x-0.5 active:translate-y-0.5 active:shadow-none',
};
type Variant = keyof typeof VARIANT;
const btn = (variant: Variant, icon: boolean, className?: string) => cx(BTN, VARIANT[variant], icon && 'w-9 pointer-coarse:w-11 px-0', className);

export function Button({ variant = 'plain', icon = false, className, ...p }: ButtonHTMLAttributes<HTMLButtonElement> & { variant?: Variant; icon?: boolean }) {
  return <button type="button" className={btn(variant, icon, className)} {...p} />;
}
export function LinkButton({ variant = 'plain', icon = false, className, ...p }: AnchorHTMLAttributes<HTMLAnchorElement> & { variant?: Variant; icon?: boolean }) {
  return <a className={btn(variant, icon, className)} {...p} />;
}
export const buttonClass = btn;

/* Tags are data, not buttons: thin border, no fill. */
const TAG = {
  plain: 'text-dim border-border-soft',
  ok: 'text-ok border-ok/40',
  game: 'text-game border-game/40',
  warn: 'text-warn border-warn/45',
  mine: 'text-action-ink bg-action border-action',
};
export function Tag({ tone = 'plain', title, children }: { tone?: keyof typeof TAG; title?: string; children: ReactNode }) {
  return (
    <span title={title} className={cx('inline-flex items-center gap-1 h-5 px-1.5 border rounded-sm font-mono text-meta leading-none font-semibold uppercase tracking-tag whitespace-nowrap [&_svg]:size-3', TAG[tone])}>
      {children}
    </span>
  );
}

export function PriceTags({ app }: { app: App }) {
  return (
    <>
      {app.kind === 'game' && <Tag tone="game">Game</Tag>}
      {app.pro ? <Tag title={app.pro}>Free + Pro</Tag> : <Tag tone="ok">Free</Tag>}
    </>
  );
}

export function AppIcon({ app, size = 'md' }: { app: App; size?: 'md' | 'lg' }) {
  return (
    <span aria-hidden="true" className={cx('grid place-items-center shrink-0 overflow-hidden border-2 rounded-md bg-raised font-display font-bold text-text', app.kind === 'game' ? 'border-game/55' : 'border-border', size === 'lg' ? 'size-18 text-stat' : 'size-11 text-body')}>
      {app.icon ? <img src={app.icon} alt="" className="size-full object-cover" /> : monogram(app.name)}
    </span>
  );
}

/* The three system marks. Everything else on the site uses Lucide. */
export function PlatformIcon({ os, className }: { os: OS; className?: string }) {
  if (os === 'windows') return <svg viewBox="0 0 24 24" fill="currentColor" className={className} aria-hidden="true"><path d="M3 3h8.300v8.300H3zM12.700 3H21v8.300h-8.300zM3 12.700h8.300V21H3zM12.700 12.700H21V21h-8.300z" /></svg>;
  if (os === 'linux') return (
    <svg viewBox="0 0 24 24" fill="currentColor" className={className} aria-hidden="true">
      <path d="M12 2c-2.300 0-3.700 1.800-3.700 4.300 0 1.900-.6 2.900-1.700 4.600C5.400 12.800 4.300 15 4.300 17.300c0 .9.2 1.700.6 2.300L3.500 22h6.300l.4-1.200h3.600l.4 1.200h6.300l-1.400-2.400c.4-.6.6-1.400.6-2.300 0-2.300-1.100-4.500-2.300-6.400-1.100-1.700-1.700-2.700-1.700-4.600C15.700 3.800 14.300 2 12 2z" />
      <ellipse cx="12" cy="14.200" rx="3.300" ry="4.600" className="fill-shadow/40" />
    </svg>
  );
  return (
    <svg viewBox="0 0 24 24" fill="currentColor" className={className} aria-hidden="true">
      <path d="M4 18a8 8 0 0 1 16 0v1H4z" />
      <path d="M7 6.500 8.800 9.600M17 6.500l-1.800 3.100" stroke="currentColor" strokeWidth="1.700" strokeLinecap="round" />
      <circle cx="8.600" cy="14.800" r="1.050" className="fill-shadow/70" />
      <circle cx="15.400" cy="14.800" r="1.050" className="fill-shadow/70" />
    </svg>
  );
}

export function Panel({ title, children }: { title: string; children: ReactNode }) {
  return (
    <section className="border-2 border-border bg-surface">
      <h2 className="label-mono rail-mark m-0 px-3 py-2.5 border-b-2 border-border-soft">{title}</h2>
      {children}
    </section>
  );
}

export function Section({ title, children }: { title: ReactNode; children: ReactNode }) {
  return (
    <section className="mt-7">
      <h2 className="label-mono rail-mark m-0 mb-2.5">{title}</h2>
      {children}
    </section>
  );
}

export function DashList({ items, className }: { items: string[]; className?: string }) {
  return (
    <ul className={cx('m-0 p-0 list-none max-w-prose', className)}>
      {items.map((t) => <li key={t} className="dash-item py-1.5 border-t border-border-soft text-dim">{t}</li>)}
    </ul>
  );
}
export { cx };
