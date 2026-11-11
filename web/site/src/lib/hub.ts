/* The RykerSoft account: Firebase Auth and the Firestore records the Android hub uses
   (app/.../entitlements/EntitlementRepository.kt). Every permission is enforced by firestore.rules;
   the checks here only keep the page honest about what it can do. */
import { type FirebaseApp, initializeApp } from 'firebase/app';
import {
  type Auth, type User, GoogleAuthProvider, getAuth, linkWithPopup, onAuthStateChanged,
  sendPasswordResetEmail, signInWithEmailAndPassword, signInWithPopup, signOut as fbSignOut,
} from 'firebase/auth';
import {
  type Firestore, Timestamp, collection, doc, getDoc, getDocs, getFirestore, onSnapshot, serverTimestamp, setDoc,
} from 'firebase/firestore';
import { useEffect, useState } from 'react';

const ADMIN_EMAIL = 'heavensounds@gmail.com';

/** Legacy Pro packages, used when no capability record is published (AiUnlockPackages.kt). */
const LEGACY_PRO: [string, string][] = [
  ['com.rykersoft.superthinking', 'SuperThink.ing'],
  ['com.rykersoft.bettertracking', 'bettertracking'],
  ['com.rykersoft.informant', 'INFORMANT'],
  ['com.rykersoft.photocrafting', 'Photocraft.ing'],
  ['com.rykersoft.comiccrafting', 'ComicCraft.ing'],
];

/** Hyperscribe Desktop reads the Mobile app's provider keys. */
export const providerKeyPackage = (packageId: string) =>
  packageId === 'com.rykersoft.hyperscribedesktop' ? 'com.rykersoft.hyperscribemobile' : packageId;

// Firebase web config for project rykersoft-abe84. It is public by design (it ships in every web page and APK and
// is also in the repository's .env.example); access is controlled by firestore.rules and the Auth authorized domains.
const config = {
  apiKey: 'AIzaSyCfDdisPZnz1qwIy_oQCOvTyk23AQdTLJA',
  authDomain: 'rykersoft-abe84.firebaseapp.com',
  projectId: 'rykersoft-abe84',
  appId: '1:809926103320:web:60700e2e0c0fcf2e924e6d',
  messagingSenderId: '809926103320',
  storageBucket: 'rykersoft-abe84.firebasestorage.app',
};

let app: FirebaseApp | null = null;
function fb(): { auth: Auth; db: Firestore } {
  app ??= initializeApp(config, 'rykersoft-hub');
  return { auth: getAuth(app), db: getFirestore(app) };
}

export const isAdmin = (u: User | null) => !!u && u.emailVerified && u.email?.toLowerCase() === ADMIN_EMAIL;

export interface Account {
  ready: boolean;
  user: User | null;
  google: boolean;
  password: boolean;
  admin: boolean;
  entitlements: Record<string, boolean>;
}

/** The signed-in account and its own Pro grants, live. */
export function useAccount(): Account {
  const [state, setState] = useState<Account>({ ready: false, user: null, google: false, password: false, admin: false, entitlements: {} });
  useEffect(() => {
    const { auth, db } = fb();
    let stopEnt = () => {};
    const stopAuth = onAuthStateChanged(auth, (user) => {
      stopEnt();
      const providers = new Set(user?.providerData.map((p) => p.providerId));
      const base = { ready: true, user, google: providers.has('google.com'), password: providers.has('password'), admin: isAdmin(user) };
      setState({ ...base, entitlements: {} });
      if (!user) return;
      stopEnt = onSnapshot(doc(db, 'users', user.uid, 'entitlements', 'apps'),
        (snap) => setState({ ...base, entitlements: bools(snap.data()) }),
        () => setState({ ...base, entitlements: {} }));
    });
    return () => { stopEnt(); stopAuth(); };
  }, []);
  return state;
}

const bools = (data: Record<string, unknown> | undefined) =>
  Object.fromEntries(Object.entries(data ?? {}).filter(([, v]) => typeof v === 'boolean')) as Record<string, boolean>;

/* ---- Sign-in, matching the Android flows. */

function explain(e: unknown): Error {
  const code = (e as { code?: string })?.code ?? '';
  const known: Record<string, string> = {
    'auth/account-exists-with-different-credential': 'This email belongs to an older password account. Sign in with its password below, then link Google.',
    'auth/credential-already-in-use': 'That Google account is already linked to another RykerSoft account. Nothing was merged.',
    'auth/popup-blocked': 'The browser blocked the Google window. Allow pop-ups for this site and try again.',
    'auth/popup-closed-by-user': 'The Google window was closed before signing in finished.',
    'auth/cancelled-popup-request': 'The Google window was closed before signing in finished.',
    'auth/unauthorized-domain': 'Google sign-in is not enabled for this web address yet.',
    'auth/invalid-credential': 'That email and password do not match a RykerSoft account.',
    'auth/wrong-password': 'That email and password do not match a RykerSoft account.',
    'auth/user-not-found': 'That email and password do not match a RykerSoft account.',
    'auth/too-many-requests': 'Too many attempts. Wait a few minutes and try again.',
    'auth/network-request-failed': 'The network request failed. Check the connection and try again.',
    'permission-denied': 'The RykerSoft server refused this change.',
  };
  return new Error(known[code] ?? (e instanceof Error ? e.message : String(e)));
}
async function wrap<T>(f: () => Promise<T>): Promise<T> {
  try { return await f(); } catch (e) { throw explain(e); }
}

/** Create or refresh users/{uid}, within what the rules allow the owner to write. */
async function ensureProfile(user: User) {
  const { db } = fb();
  const ref = doc(db, 'users', user.uid);
  const existing = await getDoc(ref);
  const values: Record<string, unknown> = { email: user.email ?? '', displayName: user.displayName ?? '', updatedAt: Timestamp.now() };
  if (!existing.exists()) values.createdAt = Timestamp.now();
  await setDoc(ref, values, { merge: true });
}

export const signInGoogle = () => wrap(async () => {
  const { user } = await signInWithPopup(fb().auth, new GoogleAuthProvider());
  await ensureProfile(user);
});
export const signInPassword = (email: string, password: string) => wrap(async () => {
  const { user } = await signInWithEmailAndPassword(fb().auth, email.trim(), password);
  await ensureProfile(user);
});
export const linkGoogle = () => wrap(async () => {
  const user = fb().auth.currentUser;
  if (!user) throw new Error('Sign in to the password account before linking Google.');
  await linkWithPopup(user, new GoogleAuthProvider());
  await user.reload();
  await ensureProfile(user);
});
export const sendReset = (email: string) => wrap(async () => {
  if (!email.trim()) throw new Error('Enter the account email first.');
  await sendPasswordResetEmail(fb().auth, email.trim());
});
export const signOut = () => fbSignOut(fb().auth);

/* ---- Pro apps and provider keys. */

export interface CredentialField { field: string; label: string; required: boolean }
export interface ProApp { packageId: string; name: string; fields: CredentialField[]; saved: Set<string> }

/** Published Pro capabilities, for anyone signed in. */
export async function loadProApps(withKeys: boolean): Promise<ProApp[]> {
  const { db } = fb();
  const caps = (await getDocs(collection(db, 'appCapabilities'))).docs.filter((d) => d.get('proEnabled') === true);
  const apps = await Promise.all(caps.map(async (d) => {
    const fields: CredentialField[] = ((d.get('credentialFields') as unknown[]) ?? []).flatMap((raw) => {
      const e = raw as Record<string, unknown>;
      if (typeof e?.field !== 'string') return [];
      return [{ field: e.field, label: (typeof e.label === 'string' && e.label) || e.field, required: e.required !== false }];
    });
    let saved = new Set<string>();
    if (withKeys) {
      const keys = await getDoc(doc(db, 'providerKeys', providerKeyPackage(d.id)));
      saved = new Set(Object.entries(keys.data() ?? {}).filter(([, v]) => typeof v === 'string' && v.trim()).map(([k]) => k));
    }
    return { packageId: d.id, name: (d.get('displayName') as string) || legacyName(d.id), fields, saved };
  }));
  const list = apps.length ? apps : LEGACY_PRO.map(([packageId, name]) => ({ packageId, name, fields: [], saved: new Set<string>() }));
  return list.sort((a, b) => a.name.toLowerCase().localeCompare(b.name.toLowerCase()));
}
const legacyName = (id: string) => LEGACY_PRO.find(([p]) => p === id)?.[1] ?? id;

export interface HubUser { uid: string; email: string; displayName: string; created: Date | null }

/** Every RykerSoft account, live. Admin only. */
export function watchUsers(onUsers: (u: HubUser[]) => void, onError: (e: Error) => void) {
  const { db } = fb();
  return onSnapshot(collection(db, 'users'), (snap) => onUsers(snap.docs.map((d) => ({
    uid: d.id,
    email: ((d.get('email') as string) || '').trim(),
    displayName: (d.get('displayName') as string) || '',
    created: (d.get('createdAt') as Timestamp | undefined)?.toDate?.() ?? null,
  })).sort((a, b) => (a.email || '~').toLowerCase().localeCompare((b.email || '~').toLowerCase()) || a.uid.localeCompare(b.uid))),
  (e) => onError(explain(e)));
}

export async function readEntitlements(uid: string): Promise<Record<string, boolean>> {
  return bools((await getDoc(doc(fb().db, 'users', uid, 'entitlements', 'apps'))).data());
}

/** Turn one app's Pro access on or off for one account, then read back what was stored. */
export const setPro = (uid: string, packageId: string, enabled: boolean) => wrap(async () => {
  if (!isAdmin(fb().auth.currentUser)) throw new Error('Only the RykerSoft administrator can change Pro access.');
  await setDoc(doc(fb().db, 'users', uid, 'entitlements', 'apps'), { [packageId]: enabled }, { merge: true });
  return readEntitlements(uid);
});

/** Save the filled-in provider keys for an app. Blank fields keep the saved key. */
export const setProviderKeys = (app: ProApp, values: Record<string, string>) => wrap(async () => {
  if (!isAdmin(fb().auth.currentUser)) throw new Error('Only the RykerSoft administrator can change provider keys.');
  const allowed = new Set(app.fields.map((f) => f.field));
  const clean = Object.fromEntries(Object.entries(values).map(([k, v]) => [k, v.trim()]).filter(([k, v]) => v && allowed.has(k)));
  if (!Object.keys(clean).length) throw new Error('Enter at least one key.');
  await setDoc(doc(fb().db, 'providerKeys', providerKeyPackage(app.packageId)), { ...clean, updatedAt: serverTimestamp() }, { merge: true });
  return Object.keys(clean);
});
