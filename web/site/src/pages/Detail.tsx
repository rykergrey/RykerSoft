import { ChevronLeft, Copy, Download } from 'lucide-react';
import { QRCodeSVG } from 'qrcode.react';
import { useEffect } from 'react';
import { toast } from 'sonner';
import { type App, type Build, type OS, OS_LIST, OS_NAME, fileSize, hasOS, isBehind } from '../lib/catalog';
import { AppIcon, Button, DashList, LinkButton, PlatformIcon, PriceTags, Section, Tag, cx } from '../components/kit';

async function copy(text: string, what: string) {
  try {
    await navigator.clipboard.writeText(text);
    toast(`${what} copied`);
  } catch {
    toast('Copying was blocked by the browser');
  }
}

function BuildRow({ app, build, mine, primary, showQr }: { app: App; build: Build; mine: OS | null; primary: boolean; showQr: boolean }) {
  const yours = build.os === mine;
  return (
    <div className={cx('px-3.5 pt-3 pb-3.5 border-t-2 border-border-soft first:border-t-0 border-l-4', yours ? 'border-l-action bg-raised/45' : 'border-l-transparent')}>
      <div className="flex items-center gap-2 [&>svg]:size-4.5">
        <PlatformIcon os={build.os} />
        <b className="font-display text-body font-bold tracking-display">{OS_NAME[build.os]}</b>
        <span className="font-mono text-label text-dim">{build.format}</span>
        {yours && <span className="ml-auto"><Tag tone="mine">Your system</Tag></span>}
      </div>
      <div className="flex flex-wrap gap-x-3.5 mt-2 mb-2.5 font-mono text-label text-dim tabular-nums">
        <span>v{build.version}</span>
        {build.size != null && <span>{fileSize(build.size)}</span>}
        {build.date && <span>{build.date}</span>}
        {isBehind(app, build) && <span className="text-warn">behind v{app.version}</span>}
        {build.sha256 ? (
          <span className="inline-flex items-center gap-1.5">
            <span className="text-ok">sha256</span> {build.sha256.slice(0, 10)}…{build.sha256.slice(-6)}
          </span>
        ) : <span>no checksum published</span>}
      </div>
      <div className="flex gap-2">
        <LinkButton variant={primary ? 'primary' : 'plain'} href={build.url} className="flex-1"><Download />Download {build.format}</LinkButton>
        {build.sha256 && <Button icon aria-label="Copy SHA-256 checksum" title="Copy SHA-256 checksum" onClick={() => copy(build.sha256, 'Checksum')}><span className="font-mono text-meta font-bold">#</span></Button>}
        <Button icon aria-label="Copy download link" title="Copy download link" onClick={() => copy(build.url, 'Link')}><Copy /></Button>
      </div>
      {showQr && (
        <div className="grid grid-cols-[96px_1fr] gap-3 items-center mt-3 pt-3 border-t border-dashed border-border text-sm text-dim">
          <div className="size-24 p-1.5 bg-paper"><QRCodeSVG value={build.url} level="M" marginSize={0} className="size-full" /></div>
          <p className="m-0">Scan with your phone to download the APK there. The code is the direct link to <span className="font-mono text-label break-all">{build.file}</span>.</p>
        </div>
      )}
    </div>
  );
}

export function Detail({ app, mine }: { app: App; mine: OS | null }) {
  useEffect(() => { scrollTo(0, 0); }, [app.id]);
  const builds = [...app.builds].sort((a, b) => Number(b.os === mine) - Number(a.os === mine));
  const apk = app.builds.find((b) => b.os === 'android');
  const [latest, ...earlier] = app.releases;
  const systems = OS_LIST.filter((os) => hasOS(app, os));

  return (
    <>
      <div className="flex items-center gap-2.5 pt-4.5">
        <LinkButton href="#/"><ChevronLeft />Catalog</LinkButton>
        <span className="label-mono">{app.kind === 'game' ? 'Games' : 'Apps'} / {app.name}</span>
      </div>
      <section className="flex items-start gap-3 md:gap-4.5 pt-4.5 pb-5.5">
        <AppIcon app={app} size="lg" />
        <div className="min-w-0">
          <h1 className="m-0 font-display text-display font-bold">{app.name}</h1>
          <div className="flex flex-wrap items-center gap-1.5 mt-2 mb-2.5">
            <PriceTags app={app} />
            {systems.map((os) => <Tag key={os}><PlatformIcon os={os} />{OS_NAME[os]}</Tag>)}
          </div>
          <p className="m-0 max-w-prose text-dim">{app.tagline}</p>
        </div>
      </section>

      <div className="grid gap-8 items-start pb-14 xl:grid-cols-[minmax(0,1fr)_400px]">
        <div className="min-w-0">
          {app.shots.length > 0 && (
            <div tabIndex={0} aria-label="Screenshots" className="flex gap-2.5 pb-3 overflow-x-auto snap-x">
              {app.shots.map((s, i) => (
                <a key={s} href={s} target="_blank" rel="noreferrer" className="shrink-0 snap-start">
                  <img src={s} alt={`${app.name} screenshot ${i + 1}`} loading="lazy" className="h-85 md:h-110 w-auto max-w-none border-2 border-border bg-surface" />
                </a>
              ))}
            </div>
          )}
          <div className="grid gap-x-10 xl:grid-cols-2">
            <div className="min-w-0">
          {latest && (
            <Section title={<>What's new in {latest.version}{latest.date && ` · ${latest.date}`}</>}>
              <DashList items={latest.notes} />
            </Section>
          )}
          {earlier.length > 0 && (
            <Section title="Earlier versions">
              <div className="max-w-prose">
                {earlier.map((r) => (
                  <div key={r.version} className="grid grid-cols-[88px_1fr] gap-x-3 py-2 border-t border-border-soft">
                    <div className="font-mono text-label tabular-nums"><div className="text-text">{r.version}</div><div className="text-faint">{r.date}</div></div>
                    <ul className="m-0 p-0 list-none text-dim text-sm">{r.notes.map((n) => <li key={n} className="py-0.5">{n}</li>)}</ul>
                  </div>
                ))}
              </div>
            </Section>
          )}
            </div>
            <div className="min-w-0">
          <Section title="About">
            <p className="m-0 mb-2.5 max-w-prose">{app.description}</p>
            <DashList items={app.features} />
          </Section>
          {app.pro && (
            <Section title="Free and Pro">
              <p className="m-0 max-w-prose text-dim">{app.pro} Pro covers only what costs money to run. It is switched on per account: sign in with Google inside the app and ask Ryker to enable it for you.</p>
            </Section>
          )}
          <Section title="Details">
            <dl className="grid grid-cols-[110px_1fr] md:grid-cols-[150px_1fr] m-0 max-w-prose border-2 border-border-soft font-mono text-label [&>dt]:label-mono [&>dt]:px-3 [&>dt]:py-2 [&>dd]:m-0 [&>dd]:px-3 [&>dd]:py-2 [&>dd]:break-all [&>*]:border-t [&>*]:border-border-soft [&>*:nth-child(-n+2)]:border-t-0">
              <dt>Latest</dt><dd>v{app.version} · {app.date}</dd>
              <dt>Systems</dt><dd>{systems.map((os) => OS_NAME[os]).join(', ')}</dd>
              <dt>Price</dt><dd>{app.pro ? 'Free, with optional Pro features' : 'Free'}</dd>
              <dt>Package</dt><dd>{app.packages.join(', ')}</dd>
            </dl>
          </Section>
            </div>
          </div>
        </div>

        <aside className="border-2 border-border bg-surface shadow-hard xl:sticky xl:top-20 max-xl:row-start-1">
          <h2 className="flex items-center justify-between m-0 px-3.5 py-2.5 border-b-2 border-border-soft">
            <span className="label-mono rail-mark">Get {app.name}</span>
            <span className="font-mono text-label text-dim tabular-nums">{builds.length} file{builds.length > 1 && 's'}</span>
          </h2>
          {builds.map((b, i) => <BuildRow key={b.url} app={app} build={b} mine={mine} primary={i === 0} showQr={b === apk && mine !== 'android'} />)}
          <p className="m-0 px-3.5 py-2.5 border-t-2 border-border-soft text-sm text-dim">
            {apk && mine !== 'android' && 'Android will ask you to allow installs from your browser the first time. '}
            {app.id !== 'rykersoft' && apk && <>The <a href="#/app/rykersoft">RykerSoft Hub</a> app installs and updates this for you.</>}
          </p>
        </aside>
      </div>
    </>
  );
}
