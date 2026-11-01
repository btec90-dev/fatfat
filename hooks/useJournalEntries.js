import { useState, useEffect } from "react";
import { db, auth } from "@/lib/firebase";
import { 
  collection, 
  doc, 
  addDoc, 
  updateDoc, 
  deleteDoc, 
  onSnapshot, 
  query, 
  orderBy, 
  serverTimestamp 
} from "firebase/firestore";
import { onAuthStateChanged } from "firebase/auth";

export function useJournalEntries() {
  const [entries, setEntries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [user, setUser] = useState(null);
  const [error, setError] = useState(null);

  // Authenticate user state
  useEffect(() => {
    const unsubscribeAuth = onAuthStateChanged(auth, (currentUser) => {
      setUser(currentUser);
      if (!currentUser) {
        setEntries([]);
        setLoading(false);
      }
    });
    return () => unsubscribeAuth();
  }, []);

  // Real-time persistent live listener on journal_entries collection sorted by createdAt descending
  useEffect(() => {
    if (!user) return;

    setLoading(true);
    const entriesColRef = collection(db, "users", user.uid, "journal_entries");
    const q = query(entriesColRef, orderBy("createdAt", "desc"));

    const unsubscribeSnapshot = onSnapshot(
      q,
      (snapshot) => {
        const fetchedEntries = [];
        snapshot.forEach((docSnap) => {
          fetchedEntries.push({
            id: docSnap.id,
            ...docSnap.data(),
            // Convert Firestore timestamp to ISO string / Date if present
            createdAt: docSnap.data().createdAt?.toDate ? docSnap.data().createdAt.toDate().toISOString() : new Date().toISOString()
          });
        });
        setEntries(fetchedEntries);
        setLoading(false);
      },
      (err) => {
        console.error("Firestore onSnapshot error:", err);
        setError(err.message);
        setLoading(false);
      }
    );

    // Clean execution of unsubscribe cleanup function on unmount
    return () => {
      unsubscribeSnapshot();
    };
  }, [user]);

  // Add new entry using addDoc and serverTimestamp
  const addEntry = async (entryData) => {
    if (!user) throw new Error("User must be logged in to add an entry.");
    const entriesColRef = collection(db, "users", user.uid, "journal_entries");
    
    const docRef = await addDoc(entriesColRef, {
      title: entryData.title || "Untitled Entry",
      notes: entryData.notes || "",
      imageUrl: entryData.imageUrl || "",
      trimester: entryData.trimester || "1",
      createdAt: serverTimestamp()
    });

    return docRef.id;
  };

  // Edit existing entry
  const editEntry = async (entryId, updatedData) => {
    if (!user) throw new Error("User must be logged in to edit an entry.");
    const docRef = doc(db, "users", user.uid, "journal_entries", entryId);
    
    await updateDoc(docRef, {
      ...updatedData,
      updatedAt: serverTimestamp()
    });
  };

  // Delete entry
  const removeEntry = async (entryId) => {
    if (!user) throw new Error("User must be logged in to delete an entry.");
    const docRef = doc(db, "users", user.uid, "journal_entries", entryId);
    await deleteDoc(docRef);
  };

  return {
    entries,
    loading,
    error,
    user,
    addEntry,
    editEntry,
    removeEntry
  };
}
