import fs from "node:fs";
import path from "node:path";
import test, { after, before, beforeEach } from "node:test";
import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
} from "@firebase/rules-unit-testing";
import {
  collection,
  doc,
  getDoc,
  serverTimestamp,
  setDoc,
  writeBatch,
} from "firebase/firestore";

const PROJECT_ID = "rykersoft-abe84";
const USER_ID = "rules-test-user";
const OTHER_USER_ID = "other-rules-test-user";
const CODE_HASH = "a".repeat(64);
const INFORMANT = "com.rykersoft.informant";
const SUPERTHINKING = "com.rykersoft.superthinking";

let testEnvironment;

before(async () => {
  testEnvironment = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: fs.readFileSync(path.resolve("../firestore.rules"), "utf8"),
    },
  });
});

beforeEach(async () => {
  await testEnvironment.clearFirestore();
  await testEnvironment.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await setDoc(doc(db, "unlockCodes", CODE_HASH), { packages: [INFORMANT] });
    await setDoc(doc(db, "providerKeys", INFORMANT), { gemini: "test-only-key" });
    await setDoc(doc(db, "providerKeys", SUPERTHINKING), { gemini: "other-test-key" });
  });
});

after(async () => {
  await testEnvironment.cleanup();
});

test("unlock-code documents are never client-readable", async () => {
  const db = testEnvironment.authenticatedContext(USER_ID).firestore();
  await assertFails(getDoc(doc(db, "unlockCodes", CODE_HASH)));
});

test("arbitrary entitlement writes are denied", async () => {
  const db = testEnvironment.authenticatedContext(USER_ID).firestore();
  await assertFails(
    setDoc(doc(db, "users", USER_ID, "entitlements", "apps"), { [INFORMANT]: true }),
  );
});

test("an atomic valid code and package request grants only that entitlement", async () => {
  const db = testEnvironment.authenticatedContext(USER_ID).firestore();
  const requestRef = doc(collection(db, "users", USER_ID, "unlockRequests"));
  const entitlementRef = doc(db, "users", USER_ID, "entitlements", "apps");
  const batch = writeBatch(db);
  batch.set(requestRef, {
    codeHash: CODE_HASH,
    packageName: INFORMANT,
    createdAt: serverTimestamp(),
  });
  batch.set(entitlementRef, {
    [INFORMANT]: true,
    lastUnlockRequestId: requestRef.id,
    lastUnlockPackage: INFORMANT,
    updatedAt: serverTimestamp(),
  });
  await assertSucceeds(batch.commit());
  await assertSucceeds(getDoc(entitlementRef));
  await assertFails(getDoc(requestRef));
});

test("a code cannot grant a package absent from its package list", async () => {
  const db = testEnvironment.authenticatedContext(USER_ID).firestore();
  const requestRef = doc(collection(db, "users", USER_ID, "unlockRequests"));
  const entitlementRef = doc(db, "users", USER_ID, "entitlements", "apps");
  const batch = writeBatch(db);
  batch.set(requestRef, {
    codeHash: CODE_HASH,
    packageName: SUPERTHINKING,
    createdAt: serverTimestamp(),
  });
  batch.set(entitlementRef, {
    [SUPERTHINKING]: true,
    lastUnlockRequestId: requestRef.id,
    lastUnlockPackage: SUPERTHINKING,
    updatedAt: serverTimestamp(),
  });
  await assertFails(batch.commit());
});

test("entitlements and provider keys stay user- and package-scoped", async () => {
  const ownerDb = testEnvironment.authenticatedContext(USER_ID).firestore();
  const otherDb = testEnvironment.authenticatedContext(OTHER_USER_ID).firestore();
  await testEnvironment.withSecurityRulesDisabled(async (context) => {
    await setDoc(doc(context.firestore(), "users", USER_ID, "entitlements", "apps"), {
      [INFORMANT]: true,
    });
  });

  await assertSucceeds(getDoc(doc(ownerDb, "users", USER_ID, "entitlements", "apps")));
  await assertFails(getDoc(doc(otherDb, "users", USER_ID, "entitlements", "apps")));
  await assertSucceeds(getDoc(doc(ownerDb, "providerKeys", INFORMANT)));
  await assertFails(getDoc(doc(ownerDb, "providerKeys", SUPERTHINKING)));
});
