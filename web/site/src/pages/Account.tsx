import { ChevronLeft, LogOut, RefreshCw, Search } from 'lucide-react';
import { type FormEvent, useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { toast } from 'sonner';
import { Button, Field, LinkButton, Panel, Row, Section, Switch, Tag, cx } from '../components/kit';
import {
  type Account as AccountState, type HubUser, type ProApp, linkGoogle, loadProApps, providerKeyPackage,
  readEntitlements, sendReset, setPro, setProviderKeys, signInGoogle, signInPassword, signOut, useAccount, watchUsers,
} from '../lib/hub';

const message = (e: unknown) => (e instanceof Error ? e.message : String(e));

/** `#/account`: sign in, see your own Pro access, and (for the administrator) manage accounts and provider keys. */
export default function Account() {
  const account = useAccount();
  const [apps, setApps] = useState<ProApp[] | null>(null);
  const [appsError, setAppsError] = useState('');
  const uid = account.user?.uid;

  const reloadApps = useCallback(async () => {
    setAppsError('');
    try { setApps(await loadProApps(account.admin)); } catch (e) { setAppsError(message(e)); }
  }, [account.admin]);
  useEffect(() => { if (uid) reloadApps(); else setApps(null); }, [uid, reloadApps]);

  return (
    <div className="pb-14">
      <div className="flex items-center gap-2.5 pt-4.5">
        <LinkButton href="#/"><ChevronLeft />Catalog</LinkButton>
        <span className="label-mono">{account.admin ? 'Account / Administration' : 'Account'}</span>
      </div>
      <h1 className="m-0 pt-4.5 font-display text-display font-bold">{account.admin ? 'Administration' : 'Your account'}</h1>

      <div className="max-w-180"><AccountPanel account={account} /></div>

      {account.user && !account.admin && <YourPro account={account} apps={apps} error={appsError} />}
      {account.admin && (
        <>
          <ProMatrix apps={apps} appsError={appsError} onReloadApps={reloadApps} />
          <ProviderKeys apps={apps} onSaved={reloadApps} />
        </>
      )}
    </div>
  );
}

/* ---- Signing in */

function AccountPanel({ account }: { account: AccountState }) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [legacy, setLegacy] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const run = async (f: () => Promise<unknown>, done?: string) => {
    setBusy(true); setError('');
    try { await f(); if (done) toast(done); } catch (e) { setError(message(e)); } finally { setBusy(false); }
  };
  const submitLegacy = (e: FormEvent) => { e.preventDefault(); run(() => signInPassword(email, password).then(() => setPassword(''))); };
  const { user } = account;

  return (
    <Section title="RykerSoft account">
      <div className="border-2 border-border bg-surface">
        {!account.ready ? (
          <Row title="Checking your sign-in…" />
        ) : user ? (
          <>
            <Row title={<span className="font-mono">{user.email ?? 'Signed in'}</span>}
              sub={account.google && account.password ? 'Google is linked. The old password still works.'
                : account.google ? 'Signed in with Google. Use the same account inside each app for Pro.'
                  : 'Password account. Link Google to keep this account and its Pro access.'}>
              {account.admin && <Tag>Administrator</Tag>}
              <Tag tone="ok">Signed in</Tag>
            </Row>
            {account.adminNeedsGoogle && (
              <Row title="Sign in with Google to administer" sub="This session used the password. Pro access and provider keys can only be changed in a Google session.">
                <Button variant="primary" disabled={busy} onClick={() => run(signInGoogle)}>Use Google</Button>
              </Row>
            )}
            {!account.google && (
              <Row title="Link Google to this account" sub="Keeps your account, data and Pro access.">
                <Button variant="primary" disabled={busy} onClick={() => run(linkGoogle, 'Google account linked')}>Link Google</Button>
              </Row>
            )}
            <Row title="Sign out" sub="Signs out of this browser only.">
              <Button disabled={busy} onClick={() => run(signOut)}><LogOut />Sign out</Button>
            </Row>
          </>
        ) : (
          <>
            <Row title="Sign in with Google" sub="The same account carries Pro access into every RykerSoft app.">
              <Button variant="primary" disabled={busy} onClick={() => run(signInGoogle)}>Sign in</Button>
            </Row>
            <Row title="I have an older password account" sub="Sign in with the old password, then link Google to keep it.">
              <Button aria-expanded={legacy} onClick={() => setLegacy((v) => !v)}>{legacy ? 'Hide' : 'Show'}</Button>
            </Row>
            {legacy && (
              <form onSubmit={submitLegacy} className="grid gap-3 px-3.5 pb-3.5 sm:grid-cols-2">
                <Field label="Account email" type="email" autoComplete="username" required value={email} onChange={(e) => setEmail(e.target.value)} />
                <Field label="Password" type="password" autoComplete="current-password" required value={password} onChange={(e) => setPassword(e.target.value)} />
                <div className="flex flex-wrap gap-2 sm:col-span-2">
                  <Button type="submit" disabled={busy}>Sign in</Button>
                  <Button disabled={busy} onClick={() => run(() => sendReset(email), 'Reset link sent, if the account exists')}>Email a reset link</Button>
                </div>
              </form>
            )}
          </>
        )}
        {error && <p role="alert" className="m-0 px-3.5 py-2.5 border-t-2 border-border-soft text-sm text-danger">{error}</p>}
      </div>
    </Section>
  );
}

function YourPro({ account, apps, error }: { account: AccountState; apps: ProApp[] | null; error: string }) {
  return (
    <div className="max-w-180">
      <Section title="Your Pro access">
        <div className="border-2 border-border bg-surface">
          {error ? <Row title="Pro access could not be read" sub={error} />
            : !apps ? <Row title="Reading your Pro access…" />
              : apps.map((a) => (
                <Row key={a.packageId} title={a.name} sub={<span className="font-mono text-label">{a.packageId}</span>}>
                  {account.entitlements[a.packageId] ? <Tag tone="ok">Pro on</Tag> : <Tag>Free</Tag>}
                </Row>
              ))}
        </div>
        <p className="m-0 mt-2.5 text-sm text-dim">Pro covers features that run on paid services, such as AI and cloud sync. Everything else is free.</p>
      </Section>
    </div>
  );
}

/* ---- Administration: who has Pro */

function ProMatrix({ apps, appsError, onReloadApps }: { apps: ProApp[] | null; appsError: string; onReloadApps: () => Promise<void> }) {
  const [users, setUsers] = useState<HubUser[] | null>(null);
  const [ents, setEnts] = useState<Record<string, Record<string, boolean>>>({});
  const [pending, setPending] = useState<Set<string>>(new Set());
  const [error, setError] = useState('');
  const [q, setQ] = useState('');
  const [onlyPro, setOnlyPro] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const seen = useRef(new Set<string>());

  const readAll = useCallback(async (list: HubUser[]) => {
    const pairs = await Promise.all(list.map(async (u) => [u.uid, await readEntitlements(u.uid).catch(() => null)] as const));
    setEnts((prev) => {
      const next = { ...prev };
      for (const [id, e] of pairs) if (e) next[id] = e;
      return next;
    });
  }, []);

  // The account list is live; grants for accounts we have not read yet are fetched as they appear.
  useEffect(() => watchUsers((list) => {
    setUsers(list); setError('');
    const fresh = list.filter((u) => !seen.current.has(u.uid));
    fresh.forEach((u) => seen.current.add(u.uid));
    readAll(fresh);
  }, (e) => setError(e.message)), [readAll]);

  const refresh = async () => {
    setRefreshing(true);
    try { await Promise.all([onReloadApps(), users ? readAll(users) : null]); } finally { setRefreshing(false); }
  };

  const toggle = async (u: HubUser, app: ProApp, on: boolean) => {
    const key = `${u.uid}|${app.packageId}`;
    setPending((s) => new Set(s).add(key));
    try {
      const stored = await setPro(u.uid, app.packageId, on);
      setEnts((prev) => ({ ...prev, [u.uid]: stored }));
      toast(`${app.name} Pro ${stored[app.packageId] ? 'on' : 'off'} for ${u.email || u.uid}`);
    } catch (e) {
      toast(`Not changed: ${message(e)}`);
    } finally {
      setPending((s) => { const n = new Set(s); n.delete(key); return n; });
    }
  };

  const proCount = (uid: string) => (apps ?? []).filter((a) => ents[uid]?.[a.packageId]).length;
  const rows = useMemo(() => {
    const needle = q.trim().toLowerCase();
    return (users ?? []).filter((u) => (!needle || u.email.toLowerCase().includes(needle) || u.uid.toLowerCase().includes(needle) || u.displayName.toLowerCase().includes(needle))
      && (!onlyPro || (apps ?? []).some((a) => ents[u.uid]?.[a.packageId])));
  }, [users, q, onlyPro, apps, ents]);
  const withPro = (users ?? []).filter((u) => proCount(u.uid) > 0).length;

  return (
    <Section title={<>Pro access <span className="text-dim tabular-nums">{users ? `· ${users.length} accounts · ${withPro} with Pro` : ''}</span></>}>
      <div className="flex flex-wrap items-center gap-2 mb-2.5">
        <label className="relative flex-1 min-w-60 max-w-120">
          <span className="sr-only">Find an account</span>
          <Search aria-hidden="true" className="absolute left-2.5 top-1/2 -translate-y-1/2 size-4 text-faint pointer-events-none" />
          <input type="search" value={q} onChange={(e) => setQ(e.target.value)} placeholder="Find by email, name or UID" autoComplete="off"
            className="w-full h-9 pointer-coarse:h-11 pl-8.5 pr-2.5 border-2 border-border rounded-sm bg-surface font-mono text-sm text-text placeholder:text-faint focus-visible:border-link focus-visible:outline-none" />
        </label>
        <Button aria-pressed={onlyPro} onClick={() => setOnlyPro((v) => !v)} className={cx(onlyPro && 'border-action text-action')}>Only with Pro</Button>
        <Button disabled={refreshing} onClick={refresh}><RefreshCw className={cx(refreshing && 'animate-spin')} />Refresh</Button>
      </div>

      {error || appsError ? (
        <div role="alert" className="px-3.5 py-3 border-2 border-danger/60 bg-surface text-sm text-danger">{error || appsError}</div>
      ) : !users || !apps ? (
        <p role="status" className="label-mono py-3">Reading accounts…</p>
      ) : (
        <div className="overflow-x-auto border-2 border-border bg-surface">
          <table className="w-full border-collapse">
            <thead>
              <tr className="border-b-2 border-border-soft">
                <th scope="col" className="sticky left-0 z-sticky bg-surface label-mono text-left px-3.5 py-2.5">Account</th>
                <th scope="col" className="label-mono px-2.5 py-2.5 text-right">Pro</th>
                {apps.map((a) => <th key={a.packageId} scope="col" title={a.packageId} className="label-mono px-2.5 py-2.5 text-center whitespace-nowrap">{a.name}</th>)}
              </tr>
            </thead>
            <tbody>
              {rows.length === 0 && (
                <tr><td colSpan={apps.length + 2} className="px-3.5 py-4 text-sm text-dim">{users.length ? 'No account matches.' : 'No RykerSoft accounts yet.'}</td></tr>
              )}
              {rows.map((u) => {
                const e = ents[u.uid];
                const n = proCount(u.uid);
                return (
                  <tr key={u.uid} className="group border-t border-border-soft">
                    <th scope="row" className="sticky left-0 z-sticky bg-surface group-hover:bg-raised text-left font-normal px-3.5 py-2">
                      <div className="max-w-36 sm:max-w-80 truncate text-sm font-medium" title={u.email}>{u.email || <span className="text-faint">No email</span>}</div>
                      <div className="max-w-36 sm:max-w-none truncate font-mono text-meta text-faint whitespace-nowrap">
                        <button type="button" title="Copy UID" className="text-faint hover:text-dim" onClick={() => navigator.clipboard.writeText(u.uid).then(() => toast('UID copied'), () => toast('Copying was blocked by the browser'))}>{u.uid}</button>
                        {u.created && <> · {u.created.toISOString().slice(0, 10)}</>}
                      </div>
                    </th>
                    <td className="group-hover:bg-raised px-2.5 py-2 text-right font-mono text-label tabular-nums">{e ? <span className={n ? 'text-ok' : 'text-faint'}>{n}</span> : <span className="text-faint">…</span>}</td>
                    {apps.map((a) => (
                      <td key={a.packageId} className="group-hover:bg-raised px-2.5 py-2 text-center">
                        {e ? (
                          <Switch checked={!!e[a.packageId]} disabled={pending.has(`${u.uid}|${a.packageId}`)}
                            label={`${a.name} Pro for ${u.email || u.uid}`} onChange={(v) => toggle(u, a, v)} />
                        ) : <span className="font-mono text-label text-faint" title="Reading this account's grants">…</span>}
                      </td>
                    ))}
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
      <p className="m-0 mt-2.5 text-sm text-dim">Each switch writes to the account straight away and shows what the server stored. The apps pick it up the next time they check.</p>
    </Section>
  );
}

/* ---- Administration: provider keys */

function ProviderKeys({ apps, onSaved }: { apps: ProApp[] | null; onSaved: () => Promise<void> }) {
  const withFields = (apps ?? []).filter((a) => a.fields.length);
  return (
    <Section title="Provider keys">
      <p className="m-0 mb-3 max-w-prose text-sm text-dim">Fields come from each app's capability record. Saved keys are never shown here; a blank field leaves the saved key as it is.</p>
      {!apps ? <p role="status" className="label-mono">Reading capability records…</p>
        : withFields.length === 0 ? <p className="m-0 text-dim">No Pro app declares provider keys.</p>
          : <div className="grid gap-4 items-start lg:grid-cols-2">{withFields.map((a) => <KeyForm key={a.packageId} app={a} onSaved={onSaved} />)}</div>}
    </Section>
  );
}

function KeyForm({ app, onSaved }: { app: ProApp; onSaved: () => Promise<void> }) {
  const [draft, setDraft] = useState<Record<string, string>>({});
  const [busy, setBusy] = useState(false);
  const filled = Object.values(draft).some((v) => v.trim());
  const shared = providerKeyPackage(app.packageId) !== app.packageId;

  const save = async (e: FormEvent) => {
    e.preventDefault();
    setBusy(true);
    try {
      const saved = await setProviderKeys(app, draft);
      setDraft({});
      await onSaved();
      toast(`${app.name}: saved ${saved.length} key${saved.length === 1 ? '' : 's'}`);
    } catch (err) {
      toast(`Not saved: ${message(err)}`);
    } finally { setBusy(false); }
  };

  return (
    <Panel title={app.name}>
      <form onSubmit={save} className="grid gap-3 p-3.5" autoComplete="off">
        <p className="m-0 font-mono text-label text-faint">{app.packageId}{shared && <> · shares keys with {providerKeyPackage(app.packageId)}</>}</p>
        {app.fields.map((f) => (
          <Field key={f.field} label={f.label} type="password" autoComplete="off" data-1p-ignore data-lpignore="true" spellCheck={false}
            placeholder={app.saved.has(f.field) ? 'Saved. Type to replace' : 'Not set'} value={draft[f.field] ?? ''}
            onChange={(e) => setDraft((d) => ({ ...d, [f.field]: e.target.value }))}
            hint={app.saved.has(f.field) ? <Tag tone="ok">Saved</Tag> : <Tag>Not set</Tag>} />
        ))}
        <div className="flex gap-2">
          <Button type="submit" variant={filled ? 'primary' : 'plain'} disabled={busy || !filled}>Save keys</Button>
          {filled && <Button disabled={busy} onClick={() => setDraft({})}>Clear</Button>}
        </div>
      </form>
    </Panel>
  );
}
