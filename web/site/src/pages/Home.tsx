import { useMemo } from 'react';
import { type App, type OS, OS_LIST, OS_NAME, countOS, hasOS, shortDate } from '../lib/catalog';
import type { Filters } from '../lib/state';
import { AppCard } from '../components/AppCard';
import { Button, Panel, PlatformIcon, cx } from '../components/kit';
import { SortMenu } from '../components/menus';

const KINDS: [Filters['kind'], string][] = [['all', 'All'], ['app', 'Apps'], ['game', 'Games']];

export function Home({ apps, filters, update, reset, mine }: { apps: App[]; filters: Filters; update: (p: Partial<Filters>) => void; reset: () => void; mine: OS | null }) {
  const list = useMemo(() => {
    const q = filters.q.trim().toLowerCase();
    return apps
      .filter((a) => (filters.os === 'all' || hasOS(a, filters.os)) && (filters.kind === 'all' || a.kind === filters.kind)
        && (!q || [a.name, a.tagline, a.description, ...a.features, ...a.packages].join(' ').toLowerCase().includes(q)))
      .sort((a, b) => (filters.sort === 'name' ? 0 : b.date.localeCompare(a.date)) || a.name.localeCompare(b.name));
  }, [apps, filters]);
  const log = useMemo(() => [...apps].sort((a, b) => b.date.localeCompare(a.date)).slice(0, 9), [apps]);
  const kindCount = (k: Filters['kind']) => (k === 'all' ? apps.length : apps.filter((a) => a.kind === k).length);
  const target = filters.os !== 'all' ? filters.os : mine;

  return (
    <>
      <section className="grid gap-x-8 gap-y-5 items-end pt-6 pb-5 md:pt-8 md:pb-6 lg:grid-cols-[minmax(0,1fr)_auto]">
        <div>
          <h1 className="m-0 mb-2.5 font-display text-display font-bold tracking-tight text-balance">
            Apps I build for myself. <span className="bg-linear-to-b from-transparent from-62% via-brand/80 via-62% to-brand/80">Free</span> for you to use.
          </h1>
          <p className="m-0 max-w-prose text-dim">
            RykerSoft is one hobbyist's workshop: capture tools, music toys, trackers and party games for Android, Windows and Linux.
            Every feature that does not need an API key is free. The ones that do are Pro, switched on per person.
          </p>
        </div>
        <dl aria-label="Catalog size" className="grid grid-cols-4 m-0 border-2 border-border bg-surface shadow-hard">
          <div className="px-3 md:px-4 py-2.5">
            <dd className="m-0 font-mono text-stat font-bold tabular-nums">{apps.length}</dd>
            <dt className="label-mono mt-1.5">Apps</dt>
          </div>
          {OS_LIST.map((os) => (
            <div key={os} className="px-3 md:px-4 py-2.5 border-l-2 border-border-soft">
              <dd className="m-0 font-mono text-stat font-bold tabular-nums">{countOS(apps, os)}</dd>
              <dt className="label-mono mt-1.5 flex items-center gap-1 [&_svg]:size-3"><PlatformIcon os={os} />{OS_NAME[os]}</dt>
            </div>
          ))}
        </dl>
      </section>

      <div className="flex flex-wrap items-center gap-x-3 gap-y-2.5 py-3 border-t-2 border-border-soft">
        <div role="group" aria-label="Kind" className="inline-flex border-2 border-border rounded-sm bg-raised">
          {KINDS.map(([k, name]) => (
            <button key={k} type="button" aria-pressed={filters.kind === k} onClick={() => update({ kind: k })}
              className={cx('inline-flex items-center gap-1.5 min-h-8 pointer-coarse:min-h-10 px-2.5 border-0 not-first:border-l-2 not-first:border-border font-mono text-label font-semibold uppercase tracking-label transition-press', filters.kind === k ? 'bg-action text-action-ink' : 'bg-transparent text-dim')}>
              {name}<span className="font-medium opacity-70 tabular-nums">{kindCount(k)}</span>
            </button>
          ))}
        </div>
        <SortMenu value={filters.sort} onChange={(sort) => update({ sort })} />
        <span className="md:ml-auto font-mono text-label text-faint tabular-nums" aria-live="polite">
          {list.length} of {apps.length}{filters.os !== 'all' && ` · ${OS_NAME[filters.os]} only`}
        </span>
      </div>

      <div className="grid gap-7 items-start pb-14 xl:grid-cols-[minmax(0,1fr)_300px]">
        <div className="grid gap-x-4.5 gap-y-5 grid-cols-[repeat(auto-fill,minmax(min(330px,100%),1fr))]">
          {list.map((a) => <AppCard key={a.id} app={a} target={target} mine={mine} />)}
          {list.length === 0 && (
            <div className="col-span-full px-5 py-12 border-2 border-dashed border-border text-center text-dim">
              <p className="m-0 mb-3.5">Nothing matches{filters.q && ` “${filters.q}”`}{filters.os !== 'all' && ` on ${OS_NAME[filters.os]}`}.</p>
              <Button onClick={reset}>Clear filters</Button>
            </div>
          )}
        </div>
        <aside className="grid gap-5 xl:sticky xl:top-20 max-xl:grid-cols-[repeat(auto-fit,minmax(min(280px,100%),1fr))]">
          <Panel title="Release log">
            <ul className="m-0 py-1 px-0 list-none">
              {log.map((a) => (
                <li key={a.id}>
                  <a href={`#/app/${a.id}`} className="grid grid-cols-[50px_1fr_auto] gap-2 items-baseline px-3 py-1.5 pointer-coarse:py-3 text-sm text-text no-underline hover:bg-raised">
                    <span className="font-mono text-label text-faint tabular-nums">{shortDate(a.date)}</span>
                    <span className="font-medium truncate">{a.name}</span>
                    <span className="font-mono text-label text-faint tabular-nums">{a.version}</span>
                  </a>
                </li>
              ))}
            </ul>
          </Panel>
          <Panel title="Free and Pro">
            <div className="p-3 text-sm text-dim">
              <p className="m-0 mb-2"><strong className="font-semibold text-text">Free</strong> means every feature that does not need an API key. That is most of every app.</p>
              <p className="m-0"><strong className="font-semibold text-text">Pro</strong> covers the features that do need one, such as AI tools. Sign in with Google inside the app and Ryker can switch Pro on for your account, one app at a time. Many apps also accept your own key.</p>
            </div>
          </Panel>
          <Panel title="On your phone">
            <p className="m-0 p-3 text-sm text-dim">The <a href="#/app/rykersoft">RykerSoft Hub</a> app for Android installs these and keeps them updated.</p>
          </Panel>
        </aside>
      </div>
    </>
  );
}
