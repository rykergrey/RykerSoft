import fs from "node:fs";
import path from "node:path";
import test, { after, before, beforeEach } from "node:test";
import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
} from "@firebase/rules-unit-testing";
import { deleteDoc, doc, getDoc, setDoc, updateDoc } from "firebase/firestore";

const PROJECT_ID = "rykersoft-abe84";
const USER_ID = "rules-test-user";
const OTHER_USER_ID = "other-rules-test-user";
const CODE_HASH = "a".repeat(64);
const INFORMANT = "com.rykersoft.informant";
const SUPERTHINKING = "com.rykersoft.superthinking";
const ADMIN_EMAIL = "heavensounds@gmail.com";

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
    await setDoc(doc(db, "appCapabilities", INFORMANT), {
      packageName: INFORMANT,
      displayName: "INFORMANT",
      proEnabled: true,
      providerModel: "trusted-family",
      credentialFields: [
        { field: "gemini", label: "Gemini API key", provider: "gemini", required: false },
        { field: "openai", label: "OpenAI API key", provider: "openai", required: false },
      ],
    });
  });
});

test("only the verified administrator can manage catalog, keys, and grants", async () => {
  const adminDb = testEnvironment.authenticatedContext("admin", {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();
  const spoofedDb = testEnvironment.authenticatedContext("spoofed", {
    email: ADMIN_EMAIL,
    email_verified: false,
  }).firestore();
  await testEnvironment.withSecurityRulesDisabled(async (context) => {
    await setDoc(doc(context.firestore(), "users", USER_ID), { email: "user@example.com" });
  });
  await assertSucceeds(setDoc(doc(adminDb, "users", USER_ID, "entitlements", "apps"), {
    [INFORMANT]: true,
  }, { merge: true }));
  await assertSucceeds(setDoc(doc(adminDb, "providerKeys", INFORMANT), {
    gemini: "replacement-key",
    openai: "replacement-openai-key",
  }, { merge: true }));
  await assertFails(setDoc(doc(spoofedDb, "providerKeys", INFORMANT), {
    gemini: "spoofed-key",
  }, { merge: true }));
});

after(async () => {
  await testEnvironment.cleanup();
});

test("unlock-code documents are never client-readable", async () => {
  const db = testEnvironment.authenticatedContext(USER_ID).firestore();
  await assertFails(getDoc(doc(db, "unlockCodes", CODE_HASH)));
});

test("owners cannot create their own entitlements", async () => {
  const db = testEnvironment.authenticatedContext(USER_ID).firestore();
  await assertFails(
    setDoc(doc(db, "users", USER_ID, "entitlements", "apps"), { [INFORMANT]: true }),
  );
});

test("clients cannot use the retired unlock-request path", async () => {
  const db = testEnvironment.authenticatedContext(USER_ID).firestore();
  await assertFails(setDoc(doc(db, "users", USER_ID, "unlockRequests", "attempt"), {
    codeHash: CODE_HASH,
    packageName: INFORMANT,
  }));
});

test("entitlements and provider keys stay user- and package-scoped", async () => {
  const ownerDb = testEnvironment.authenticatedContext(USER_ID).firestore();
  const otherDb = testEnvironment.authenticatedContext(OTHER_USER_ID).firestore();
  const anonymousDb = testEnvironment.unauthenticatedContext().firestore();
  await testEnvironment.withSecurityRulesDisabled(async (context) => {
    await setDoc(doc(context.firestore(), "users", USER_ID, "entitlements", "apps"), {
      [INFORMANT]: true,
    });
  });

  await assertSucceeds(getDoc(doc(ownerDb, "users", USER_ID, "entitlements", "apps")));
  await assertFails(getDoc(doc(otherDb, "users", USER_ID, "entitlements", "apps")));
  await assertFails(getDoc(doc(anonymousDb, "users", USER_ID, "entitlements", "apps")));
  await assertFails(setDoc(doc(ownerDb, "users", USER_ID, "entitlements", "apps"), {
    [SUPERTHINKING]: true,
  }, { merge: true }));
  await assertFails(updateDoc(doc(ownerDb, "users", USER_ID, "entitlements", "apps"), {
    [SUPERTHINKING]: true,
  }));
  await assertFails(deleteDoc(doc(ownerDb, "users", USER_ID, "entitlements", "apps")));
  await assertSucceeds(getDoc(doc(ownerDb, "providerKeys", INFORMANT)));
  await assertFails(getDoc(doc(ownerDb, "providerKeys", SUPERTHINKING)));
  await assertFails(getDoc(doc(anonymousDb, "providerKeys", INFORMANT)));
});
