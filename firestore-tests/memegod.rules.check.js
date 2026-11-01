import fs from 'node:fs';
import test,{before,after,beforeEach} from 'node:test';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {doc,setDoc,getDoc,collection,getDocs} from 'firebase/firestore';
let env;const meme='com.rykersoft.memegod',other='com.rykersoft.photocrafting';
before(async()=>{env=await initializeTestEnvironment({projectId:'demo-memegod-pro',firestore:{host:'127.0.0.1',port:8085,rules:fs.readFileSync('../firebase/memegod.firestore.rules','utf8')}});});
after(async()=>env?.cleanup());
beforeEach(async()=>{await env.clearFirestore();await env.withSecurityRulesDisabled(async c=>{for(const pkg of [meme,other]){await setDoc(doc(c.firestore(),'appCapabilities',pkg),{packageName:pkg,proEnabled:true});await setDoc(doc(c.firestore(),'providerKeys',pkg),{openai:'test-fixture'});}});});
const user=(provider='google.com')=>env.authenticatedContext('member',{email:'member@example.test',email_verified:true,firebase:{sign_in_provider:provider}}).firestore();
async function grant(fields){await env.withSecurityRulesDisabled(c=>setDoc(doc(c.firestore(),'users','member','entitlements','apps'),fields));}
test('signed-out and free accounts cannot read Meme God provider keys',async()=>{await assertFails(getDoc(doc(env.unauthenticatedContext().firestore(),'providerKeys',meme)));await assertFails(getDoc(doc(user(),'providerKeys',meme)));});
test('Pro on another app unlocks Meme God; false and string grants do not',async()=>{await grant({[other]:true});await assertSucceeds(getDoc(doc(user(),'providerKeys',meme)));await grant({[other]:false});await assertFails(getDoc(doc(user(),'providerKeys',meme)));await grant({[other]:'true'});await assertFails(getDoc(doc(user(),'providerKeys',meme)));});
test('Meme God grant remains valid, but does not unlock another app',async()=>{await grant({[meme]:true});await assertSucceeds(getDoc(doc(user(),'providerKeys',meme)));await assertFails(getDoc(doc(user(),'providerKeys',other)));});
test('password sessions, self-grants and provider enumeration remain denied',async()=>{await grant({[other]:true});await assertFails(getDoc(doc(user('password'),'providerKeys',meme)));await assertFails(setDoc(doc(user(),'users','member','entitlements','apps'),{[meme]:true}));await assertFails(getDocs(collection(user(),'providerKeys')));});
test('capability disablement and revocation remove access',async()=>{await grant({[other]:true});await assertSucceeds(getDoc(doc(user(),'providerKeys',meme)));await grant({[other]:false});await assertFails(getDoc(doc(user(),'providerKeys',meme)));await grant({[other]:true});await env.withSecurityRulesDisabled(c=>setDoc(doc(c.firestore(),'appCapabilities',meme),{proEnabled:false}));await assertFails(getDoc(doc(user(),'providerKeys',meme)));});
test('only a verified Google administrator may modify grants',async()=>{for(const provider of ['google.com','password']){const db=env.authenticatedContext('admin',{email:'heavensounds@gmail.com',email_verified:true,firebase:{sign_in_provider:provider}}).firestore();await (provider==='google.com'?assertSucceeds:assertFails)(setDoc(doc(db,'users','member','entitlements','apps'),{[meme]:true}));}});
