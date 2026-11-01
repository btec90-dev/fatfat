import React, { useState } from "react";
import { useJournalEntries } from "@/hooks/useJournalEntries";

export default function JournalForm() {
  const { addEntry, user } = useJournalEntries();
  const [title, setTitle] = useState("");
  const [notes, setNotes] = useState("");
  const [imageUrl, setImageUrl] = useState("");
  const [trimester, setTrimester] = useState("1");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title.trim() && !notes.trim()) return;

    setIsSubmitting(true);
    try {
      await addEntry({
        title,
        notes,
        imageUrl,
        trimester
      });

      // Reset form fields
      setTitle("");
      setNotes("");
      setImageUrl("");
      setTrimester("1");
    } catch (err) {
      console.error("Failed to add entry:", err);
      alert("Erreur lors de l'enregistrement de l'entrée.");
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!user) {
    return (
      <div className="p-4 bg-amber-50 border border-amber-200 rounded-2xl text-xs text-amber-900 text-center">
        Veuillez vous connecter pour ajouter des entrées à votre journal.
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit} className="bg-white rounded-3xl p-5 border border-bronze-200 shadow-sm space-y-4">
      <h3 className="text-sm font-bold text-bronze-900 flex items-center gap-2">
        <span>✍️ Nouvelle Entrée de Journal</span>
      </h3>

      <div className="space-y-3">
        <div>
          <label className="block text-xs font-semibold text-stone-700 mb-1">Titre</label>
          <input 
            type="text" 
            placeholder="Ex: Échographie du 1er trimestre" 
            value={title} 
            onChange={(e) => setTitle(e.target.value)}
            className="w-full bg-stone-50 border border-stone-200 rounded-xl p-2.5 text-xs focus:outline-none focus:border-bronze-500 font-medium"
          />
        </div>

        <div className="grid grid-cols-2 gap-2">
          <div>
            <label className="block text-xs font-semibold text-stone-700 mb-1">Trimestre</label>
            <select 
              value={trimester} 
              onChange={(e) => setTrimester(e.target.value)}
              className="w-full bg-stone-50 border border-stone-200 rounded-xl p-2.5 text-xs focus:outline-none focus:border-bronze-500 font-medium"
            >
              <option value="1">1er Trimestre</option>
              <option value="2">2e Trimestre</option>
              <option value="3">3e Trimestre</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-stone-700 mb-1">URL de la Photo</label>
            <input 
              type="url" 
              placeholder="https://... (photo ou écho)" 
              value={imageUrl} 
              onChange={(e) => setImageUrl(e.target.value)}
              className="w-full bg-stone-50 border border-stone-200 rounded-xl p-2.5 text-xs focus:outline-none focus:border-bronze-500 font-medium"
            />
          </div>
        </div>

        <div>
          <label className="block text-xs font-semibold text-stone-700 mb-1">Notes & Observations</label>
          <textarea 
            placeholder="Écrivez vos pensées, symptômes ou compte-rendus..." 
            value={notes} 
            onChange={(e) => setNotes(e.target.value)}
            rows="3"
            className="w-full bg-stone-50 border border-stone-200 rounded-xl p-2.5 text-xs focus:outline-none focus:border-bronze-500 font-medium"
          />
        </div>
      </div>

      <button 
        type="submit" 
        disabled={isSubmitting}
        className="w-full bg-bronze-600 hover:bg-bronze-700 text-white font-bold py-2.5 rounded-xl text-xs transition shadow-md flex items-center justify-center gap-2 cursor-pointer disabled:opacity-50"
      >
        {isSubmitting ? "Enregistrement..." : "Enregistrer dans Firestore (Temps Réel)"}
      </button>
    </form>
  );
}
