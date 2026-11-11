import { Switch as BaseSwitch } from '@base-ui/react/switch';
import type { AnchorHTMLAttributes, ButtonHTMLAttributes, InputHTMLAttributes, ReactNode } from 'react';
import { type App, type OS, monogram } from '../lib/catalog';

const cx = (...c: (string | false | null | undefined)[]) => c.filter(Boolean).join(' ');

/* Buttons. Tappable means contained: every button has a border and a fill. */
const BTN = 'inline-flex items-center justify-center gap-2 min-h-9 pointer-coarse:min-h-11 px-3 border-2 rounded-sm font-mono text-label leading-none uppercase tracking-label whitespace-nowrap no-underline select-none transition-press [&_svg]:size-4 [&_svg]:shrink-0';
const VARIANT = {
  plain: 'border-border bg-raised text-text font-semibold hover:border-dim active:translate-x-px active:translate-y-px',
  primary: 'border-shadow bg-action text-action-ink font-bold shadow-hard-sm hover:-translate-x-px hover:-translate-y-px hover:shadow-hard active:translate-x-0.5 active:translate-y-0.5 active:shadow-none',
};
type Variant = keyof typeof VARIANT;
const btn = (variant: Variant, icon: boolean, className?: string) => cx(BTN, VARIANT[variant], icon && 'w-9 pointer-coarse:w-11 px-0', 'disabled:opacity-45 disabled:pointer-events-none', className);

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
/* A labelled text input. The label stays visible above the field. */
export function Field({ label, hint, className, ...p }: InputHTMLAttributes<HTMLInputElement> & { label: string; hint?: ReactNode }) {
  return (
    <label className={cx('flex flex-col gap-1.5 min-w-0', className)}>
      <span className="flex items-center gap-2 font-mono text-label font-medium text-dim">{label}{hint}</span>
      <input className="w-full h-9 pointer-coarse:h-11 px-2.5 border-2 border-border rounded-sm bg-bg font-mono text-sm text-text placeholder:text-faint focus-visible:border-link focus-visible:outline-none disabled:opacity-45" {...p} />
    </label>
  );
}

/* On/off. Green means Pro is switched on (DESIGN.md, ok). */
export function Switch({ checked, onChange, disabled, label }: { checked: boolean; onChange: (v: boolean) => void; disabled?: boolean; label: string }) {
  return (
    <BaseSwitch.Root checked={checked} onCheckedChange={(v) => onChange(v)} disabled={disabled} aria-label={label} title={label}
      className="relative inline-flex shrink-0 items-center w-10 h-5.5 p-0.5 border-2 border-border rounded-sm bg-bg transition-press data-[checked]:border-ok data-[checked]:bg-ok/20 data-[disabled]:opacity-45 data-[disabled]:cursor-progress pointer-coarse:my-2.5">
      <BaseSwitch.Thumb className="block size-3.5 rounded-sm bg-faint transition-press data-[checked]:translate-x-4.5 data-[checked]:bg-ok" />
    </BaseSwitch.Root>
  );
}

/* A settings row: a name and a line of explanation on the left, its control on the right. */
export function Row({ title, sub, children }: { title: ReactNode; sub?: ReactNode; children?: ReactNode }) {
  return (
    <div className="flex flex-wrap items-center gap-x-4 gap-y-2.5 px-3.5 py-3 border-t-2 border-border-soft first:border-t-0">
      <div className="flex-1 min-w-48">
        <div className="text-body font-medium">{title}</div>
        {sub && <div className="text-sm text-dim">{sub}</div>}
      </div>
      {children && <div className="flex flex-wrap items-center gap-2">{children}</div>}
    </div>
  );
}
export { cx };
