import React from "react";
import { useJournalEntries } from "@/hooks/useJournalEntries";

export default function JournalList() {
  const { entries, loading, error, user, removeEntry } = useJournalEntries();

  if (!user) {
    return (
      <div className="p-6 text-center text-xs text-stone-500">
        Connectez-vous pour voir votre journal en temps réel.
      </div>
    );
  }

  if (loading) {
    return (
      <div className="p-6 text-center text-xs text-stone-500 animate-pulse">
        Synchronisation des entrées en temps réel depuis Firestore...
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-4 bg-red-50 text-red-700 rounded-2xl text-xs text-center">
        Erreur de chargement : {error}
      </div>
    );
  }

  if (entries.length === 0) {
    return (
      <div className="p-8 text-center bg-stone-50 rounded-3xl border border-stone-200 text-xs text-stone-500">
        Aucune entrée pour le moment. Créez votre premier souvenir ci-dessus !
      </div>
    );
  }

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between px-1">
        <h4 className="text-xs font-bold text-stone-700 uppercase tracking-wider">
          Journal en Direct ({entries.length} entrées synchronisées)
        </h4>
        <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" title="Live sync actif"></span>
      </div>

      <div className="space-y-3">
        {entries.map((entry) => (
          <div 
            key={entry.id} 
            className="bg-white rounded-3xl p-4 border border-bronze-200/80 shadow-sm flex flex-col sm:flex-row gap-4 items-start transition-all hover:shadow-md"
          >
            {/* Photo / Image preview */}
            {entry.imageUrl ? (
              <img 
                src={entry.imageUrl} 
                alt={entry.title || "Photo journal"} 
                className="w-full sm:w-28 h-28 object-cover rounded-2xl border border-stone-200 shrink-0 bg-stone-100"
                onError={(e) => { e.target.style.display = 'none'; }}
              />
            ) : (
              <div className="w-full sm:w-28 h-28 rounded-2xl bg-bronze-50 border border-bronze-200 flex items-center justify-center text-bronze-400 shrink-0">
                <span className="text-2xl">📸</span>
              </div>
            )}

            {/* Content info */}
            <div className="flex-1 min-w-0 space-y-1 w-full">
              <div className="flex items-center justify-between gap-2">
                <h3 className="text-sm font-bold text-bronze-900 truncate">
                  {entry.title || "Sans titre"}
                </h3>
                <span className="px-2 py-0.5 bg-bronze-100 text-bronze-800 rounded-full text-[10px] font-semibold shrink-0">
                  Trimestre {entry.trimester || "1"}
                </span>
              </div>

              <p className="text-xs text-stone-600 leading-relaxed whitespace-pre-wrap">
                {entry.notes || "Aucune note saisie."}
              </p>

              <div className="flex items-center justify-between pt-2 border-t border-stone-100 mt-2">
                <span className="text-[10px] text-stone-400 font-medium">
                  {entry.createdAt ? new Date(entry.createdAt).toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit' }) : "À l'instant"}
                </span>

                <button 
                  onClick={() => removeEntry(entry.id)}
                  className="text-xs text-red-500 hover:text-red-700 font-medium transition cursor-pointer flex items-center gap-1"
                >
                  Supprimer
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
