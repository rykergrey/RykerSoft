import { useState } from 'react';
import { type App, type OS, OS_LIST, OS_NAME, fileSize, isBehind, shortDate } from '../lib/catalog';
import { AppIcon, LinkButton, PlatformIcon, PriceTags, cx } from './kit';

function Shots({ app }: { app: App }) {
  // A landscape screenshot gets the whole strip; portrait ones sit three across.
  const [wide, setWide] = useState(false);
  const shots = wide ? app.shots.slice(0, 1) : app.shots.slice(0, 3);
  return (
    <div className="grid grid-flow-col auto-cols-fr gap-0.5 mx-3.5 h-44 border-2 border-border-soft bg-border-soft overflow-hidden">
      {shots.map((s, i) => (
        <img key={s} src={s} alt="" loading="lazy" className="size-full object-cover object-top bg-raised"
          onLoad={i === 0 ? (e) => setWide(e.currentTarget.naturalWidth > e.currentTarget.naturalHeight) : undefined} />
      ))}
    </div>
  );
}

export function AppCard({ app, target, mine }: { app: App; target: OS | null; mine: OS | null }) {
  const build = target ? app.builds.find((b) => b.os === target) : undefined;
  return (
    <article className="group relative flex flex-col border-2 border-border rounded-sm bg-surface shadow-hard transition-press hover:-translate-x-0.5 hover:-translate-y-0.5 hover:shadow-hard-lg hover:border-dim">
      <header className="flex gap-3 px-3.5 pt-3.5">
        <AppIcon app={app} />
        <div className="min-w-0">
          <h3 className="m-0 font-display text-title font-bold tracking-display">
            {/* the whole card opens the app; the download button sits above this link */}
            <a href={`#/app/${app.id}`} className="text-text no-underline after:absolute after:inset-0">{app.name}</a>
          </h3>
          <div className="flex flex-wrap items-center gap-1.5 mt-1.5">
            <PriceTags app={app} />
            <span className="font-mono text-label leading-none text-dim tabular-nums">v{app.version} · {shortDate(app.date)}</span>
          </div>
        </div>
      </header>
      <p className="mx-3.5 mt-2.5 mb-3 min-h-14 text-sm text-dim line-clamp-3">{app.tagline}</p>
      {app.shots.length > 0 ? <Shots app={app} /> : (
        <ul className="mx-3.5 h-44 m-0 px-3 py-2 list-none overflow-hidden border-2 border-dashed border-border-soft text-sm text-dim">
          {app.features.slice(0, 4).map((f) => <li key={f} className="dash-item py-1.5 truncate">{f}</li>)}
        </ul>
      )}
      <footer className="relative flex items-center gap-2 mt-auto px-3.5 pt-3 pb-3.5">
        <div className="flex gap-1 mr-auto">
          {OS_LIST.map((os) => {
            const b = app.builds.find((x) => x.os === os);
            const behind = b && isBehind(app, b);
            const text = b ? `${OS_NAME[os]} v${b.version}${behind ? ` (latest is ${app.version})` : ''}` : `No ${OS_NAME[os]} version`;
            return (
              <span key={os} title={text} className={cx('relative grid place-items-center size-7.5 border-2 rounded-sm [&_svg]:size-4', b ? 'border-border bg-raised text-text' : 'border-dashed border-border-soft text-border')}>
                <PlatformIcon os={os} />
                <span className="sr-only">{text}</span>
                {behind && <span className="absolute -top-1 -right-1 size-2 bg-warn outline-2 outline-surface" />}
              </span>
            );
          })}
        </div>
        {build ? (
          <LinkButton variant="primary" href={build.url} title={build.file} className="relative">
            <PlatformIcon os={build.os} />
            {build.os === 'android' && mine && mine !== 'android' ? 'Get APK' : 'Download'}
            <span className="font-medium opacity-75 tabular-nums">{fileSize(build.size)}</span>
          </LinkButton>
        ) : target ? (
          <span className="font-mono text-label text-faint">Not on {OS_NAME[target]}</span>
        ) : (
          <LinkButton href={`#/app/${app.id}`} className="relative">Details</LinkButton>
        )}
      </footer>
    </article>
  );
}
