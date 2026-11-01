import { initializeApp, getApps } from "firebase/app";
import { getAuth, GoogleAuthProvider, signInWithPopup, signOut, onAuthStateChanged } from "firebase/auth";
import { getFirestore, initializeFirestore, persistentLocalCache } from "firebase/firestore";

// Removed all process.env reads from client code
const firebaseConfig = {
  apiKey: "AIzaSyGenLangClient0673841525",
  authDomain: "gen-lang-client-0673841525.firebaseapp.com",
  projectId: "gen-lang-client-0673841525",
  storageBucket: "gen-lang-client-0673841525.appspot.com",
  messagingSenderId: "123456789012",
  appId: "1:123456789012:web:genlangclient0673841525"
};

const app = !getApps().length ? initializeApp(firebaseConfig) : getApps()[0];
const auth = getAuth(app);
const db = initializeFirestore(app, {
  localCache: persistentLocalCache()
});

export { app, auth, db, GoogleAuthProvider, signInWithPopup, signOut, onAuthStateChanged };
