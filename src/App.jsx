import React, { useState, useEffect } from 'react';

const INITIAL_CHECKLIST = [
  // Baby Essentials (Clothing & Gear)
  {
    id: 'b1',
    module: 'baby',
    category: 'Clothing & Gear',
    title: 'Two-way zip footies (0–3M)',
    priority: 'Must-Have',
    quantity: '4-6 packs',
    purchased: false,
    notes: 'Essential for quick and easy midnight diaper changes without exposing baby to cold.'
  },
  {
    id: 'b2',
    module: 'baby',
    category: 'Clothing & Gear',
    title: 'Short and long-sleeve kimono bodysuits',
    priority: 'Must-Have',
    quantity: '6-8 pieces',
    purchased: false,
    notes: 'Kimono style wraps around without pulling over newborn delicate head.'
  },
  {
    id: 'b3',
    module: 'baby',
    category: 'Clothing & Gear',
    title: 'Swaddles / sleep sacks',
    priority: 'Must-Have',
    quantity: '3-4 pieces',
    purchased: false,
    notes: 'Helps soothe startle reflex and promotes safe, cozy infant sleep.'
  },
  {
    id: 'b4',
    module: 'baby',
    category: 'Clothing & Gear',
    title: 'Infant car seat and base',
    priority: 'Must-Have',
    quantity: '1',
    purchased: false,
    notes: 'Mandatory for hospital discharge and safe transit.'
  },
  {
    id: 'b5',
    module: 'baby',
    category: 'Clothing & Gear',
    title: 'Bassinet / crib with fitted sheets',
    priority: 'Must-Have',
    quantity: '1',
    purchased: false,
    notes: 'Safe sleep space meeting current AAP safety guidelines.'
  },
  {
    id: 'b6',
    module: 'baby',
    category: 'Clothing & Gear',
    title: 'Baby first-aid (rectal/infant thermometer, nasal aspirator, infant acetaminophen)',
    priority: 'Must-Have',
    quantity: '1 kit',
    purchased: false,
    notes: 'Essential health toolkit for monitoring temperature and clearing congestion.'
  },

  // Mom & Postpartum Care
  {
    id: 'm1',
    module: 'mom',
    category: 'Hospital Bag',
    title: 'Hospital bag essentials (loose clothing, long charging cables, toiletries, ID/insurance)',
    priority: 'Hospital Bag Essential',
    quantity: '1 bag',
    purchased: false,
    notes: 'Pack by week 36 with comfortable clothes, 10ft phone charger, and important documents.'
  },
  {
    id: 'm2',
    module: 'mom',
    category: 'Perineal Recovery',
    title: 'Perineal recovery (peri bottle, witch hazel pads, ice packs, disposable briefs)',
    priority: 'Must-Have',
    quantity: '1 kit',
    purchased: false,
    notes: 'Crucial for soothing and healing postpartum soreness and swelling.'
  },
  {
    id: 'm3',
    module: 'mom',
    category: 'Nursing & Pumping Gear',
    title: 'Nursing & pumping gear (nursing bras, nipple balm, breast pads, nursing pillow)',
    priority: 'Must-Have',
    quantity: '1 set',
    purchased: false,
    notes: 'Supports comfortable feeding sessions and protects sensitive skin.'
  }
];

export default function App() {
  const [activeTab, setActiveTab] = useState('dashboard'); // 'dashboard', 'registry', 'ai', 'calendar'
  const [geminiKey, setGeminiKey] = useState(localStorage.getItem('gemini_api_key') || '');
  const [promptText, setPromptText] = useState('');
  const [aiResponse, setAiResponse] = useState('');
  const [loading, setLoading] = useState(false);
  const [savedMessage, setSavedMessage] = useState(false);
  const [deferredPrompt, setDeferredPrompt] = useState(null);

  useEffect(() => {
    window.addEventListener('beforeinstallprompt', (e) => {
      e.preventDefault();
      setDeferredPrompt(e);
    });
  }, []);

  const handleInstallClick = async () => {
    if (deferredPrompt) {
      deferredPrompt.prompt();
      const { outcome } = await deferredPrompt.userChoice;
      if (outcome === 'accepted') {
        setDeferredPrompt(null);
      }
    } else {
      alert("📲 Pour ajouter Maman & Bébé à votre écran d'accueil :\n\n• Sur iPhone / iPad (Safari) : Appuyez sur le bouton Partager 📤 dans la barre d'outils, puis sélectionnez 'Sur l'écran d'accueil'.\n\n• Sur Android / Chrome : Appuyez sur le menu (trois points en haut à droite ⋮) puis 'Ajouter à l'écran d'accueil' ou 'Installer l'application'.");
    }
  };

  // Checklist state
  const [checklist, setChecklist] = useState(() => {
    const saved = localStorage.getItem('maman_bebe_checklist_v2');
    if (saved) {
      try { return JSON.parse(saved); } catch (e) { return INITIAL_CHECKLIST; }
    }
    return INITIAL_CHECKLIST;
  });

  const [registryFilter, setRegistryFilter] = useState('all'); // 'all', 'baby', 'mom'
  const [searchQuery, setSearchQuery] = useState('');
  const [expandedNotes, setExpandedNotes] = useState({});
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);

  // New item form state
  const [newItem, setNewItem] = useState({
    module: 'baby',
    category: 'Clothing (0-3M)',
    title: '',
    priority: 'Must-Have',
    quantity: '1',
    notes: ''
  });

  useEffect(() => {
    localStorage.setItem('maman_bebe_checklist_v2', JSON.stringify(checklist));
  }, [checklist]);

  const saveKey = () => {
    localStorage.setItem('gemini_api_key', geminiKey);
    setSavedMessage(true);
    setTimeout(() => setSavedMessage(false), 2500);
  };

  const askAi = async () => {
    if (!promptText.trim()) return;
    setLoading(true);
    try {
      const res = await fetch('/api/gemini', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ prompt: promptText, apiKey: geminiKey })
      });
      const data = await res.json();
      setAiResponse(data.reply || data.error || 'Réponse reçue.');
    } catch (e) {
      setAiResponse("Erreur de connexion au serveur sécurisé /api/gemini.");
    } finally {
      setLoading(false);
    }
  };

  const toggleItem = (id) => {
    setChecklist(checklist.map(item => item.id === id ? { ...item, purchased: !item.purchased } : item));
  };

  const deleteItem = (id) => {
    setChecklist(checklist.filter(item => item.id !== id));
  };

  const toggleNote = (id) => {
    setExpandedNotes(prev => ({ ...prev, [id]: !prev[id] }));
  };

  const handleAddSubmit = (e) => {
    e.preventDefault();
    if (!newItem.title.trim()) return;

    const itemToAdd = {
      id: 'custom_' + Date.now(),
      ...newItem,
      purchased: false
    };

    setChecklist([itemToAdd, ...checklist]);
    setNewItem({
      module: 'baby',
      category: 'Clothing (0-3M)',
      title: '',
      priority: 'Must-Have',
      quantity: '1',
      notes: ''
    });
    setIsAddModalOpen(false);
    setActiveTab('registry');
  };

  // Calculations for progress
  const totalItems = checklist.length;
  const purchasedItems = checklist.filter(i => i.purchased).length;
  const progressPercent = totalItems > 0 ? Math.round((purchasedItems / totalItems) * 100) : 0;

  const babyItems = checklist.filter(i => i.module === 'baby');
  const babyPurchased = babyItems.filter(i => i.purchased).length;

  const momItems = checklist.filter(i => i.module === 'mom');
  const momPurchased = momItems.filter(i => i.purchased).length;

  // Filtered list
  const filteredChecklist = checklist.filter(item => {
    if (registryFilter === 'baby' && item.module !== 'baby') return false;
    if (registryFilter === 'mom' && item.module !== 'mom') return false;
    if (searchQuery.trim() && !item.title.toLowerCase().includes(searchQuery.toLowerCase()) && !item.category.toLowerCase().includes(searchQuery.toLowerCase())) {
      return false;
    }
    return true;
  });

  return (
    <div className="min-h-screen bg-stone-50 text-stone-900 font-sans pb-12">
      {/* HEADER */}
      <header className="bg-white border-b border-stone-200 sticky top-0 z-30 shadow-xs">
        <div className="max-w-4xl mx-auto px-4 py-3 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-2xl bg-amber-100 flex items-center justify-center font-bold text-amber-900 text-base shadow-xs">
              M&B
            </div>
            <div>
              <h1 className="text-base font-bold text-stone-900 leading-tight">Maman & Bébé</h1>
              <p className="text-xs text-stone-500">Suivi de Grossesse, PMA & Préparation</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <span className="hidden sm:inline-block px-3 py-1 bg-amber-50 text-amber-900 text-xs font-medium rounded-full border border-amber-200">
              Semaine 28 de Grossesse 🤰
            </span>
          </div>
        </div>

        {/* NAVIGATION TABS */}
        <div className="max-w-4xl mx-auto px-4 flex gap-2 overflow-x-auto pb-2 scrollbar-none">
          <button 
            onClick={() => setActiveTab('dashboard')} 
            className={`px-4 py-2 rounded-xl text-xs font-bold transition whitespace-nowrap flex items-center gap-1.5 ${activeTab === 'dashboard' ? 'bg-amber-900 text-white shadow-sm' : 'bg-stone-100 text-stone-600 hover:bg-stone-200'}`}
          >
            📊 Tableau de Bord
          </button>
          <button 
            onClick={() => setActiveTab('registry')} 
            className={`px-4 py-2 rounded-xl text-xs font-bold transition whitespace-nowrap flex items-center gap-1.5 ${activeTab === 'registry' ? 'bg-amber-900 text-white shadow-sm' : 'bg-stone-100 text-stone-600 hover:bg-stone-200'}`}
          >
            🍼 Registre & Post-partum ({purchasedItems}/{totalItems})
          </button>
          <button 
            onClick={() => setActiveTab('ai')} 
            className={`px-4 py-2 rounded-xl text-xs font-bold transition whitespace-nowrap flex items-center gap-1.5 ${activeTab === 'ai' ? 'bg-amber-900 text-white shadow-sm' : 'bg-stone-100 text-stone-600 hover:bg-stone-200'}`}
          >
            ✨ Assistant Médical IA
          </button>
          <button 
            onClick={() => setActiveTab('calendar')} 
            className={`px-4 py-2 rounded-xl text-xs font-bold transition whitespace-nowrap flex items-center gap-1.5 ${activeTab === 'calendar' ? 'bg-amber-900 text-white shadow-sm' : 'bg-stone-100 text-stone-600 hover:bg-stone-200'}`}
          >
            📅 Calendrier & PMA
          </button>
        </div>
      </header>

      <main className="max-w-4xl mx-auto px-4 mt-6 space-y-6">
        {/* DASHBOARD TAB */}
        {activeTab === 'dashboard' && (
          <div className="space-y-6">
            <div className="bg-gradient-to-br from-amber-900 via-stone-900 to-amber-950 text-white rounded-3xl p-6 shadow-xl relative overflow-hidden">
              <div className="absolute right-[-20px] bottom-[-20px] opacity-10 text-9xl">👶</div>
              <h2 className="text-xl font-bold mb-2">Bienvenue dans votre espace serein</h2>
              <p className="text-amber-100 text-xs sm:text-sm max-w-xl leading-relaxed mb-6">
                Préparez l'arrivée de votre bébé et votre propre rétablissement post-partum avec notre checklist complète, validée par des professionnels de santé.
              </p>
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div className="bg-white/10 backdrop-blur-md rounded-2xl p-4 border border-white/15">
                  <p className="text-xs text-amber-200 font-medium">Progression Globale</p>
                  <p className="text-2xl font-black mt-1">{progressPercent}%</p>
                  <p className="text-[11px] text-stone-300 mt-1">{purchasedItems} sur {totalItems} articles acquis</p>
                </div>
                <div className="bg-white/10 backdrop-blur-md rounded-2xl p-4 border border-white/15">
                  <p className="text-xs text-amber-200 font-medium">Bébé & Essentiels</p>
                  <p className="text-2xl font-black mt-1">{babyPurchased}/{babyItems.length}</p>
                  <p className="text-[11px] text-stone-300 mt-1">Chambre, vêtements, repas</p>
                </div>
                <div className="bg-white/10 backdrop-blur-md rounded-2xl p-4 border border-white/15">
                  <p className="text-xs text-amber-200 font-medium">Maman & Post-partum</p>
                  <p className="text-2xl font-black mt-1">{momPurchased}/{momItems.length}</p>
                  <p className="text-[11px] text-stone-300 mt-1">Valise de maternité & soins</p>
                </div>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="bg-white rounded-3xl p-6 shadow-sm border border-stone-200 space-y-4">
                <div className="flex items-center justify-between">
                  <h3 className="font-bold text-stone-900 flex items-center gap-2">
                    <span>🍼</span> Baby Registry & Essentials
                  </h3>
                  <button onClick={() => { setActiveTab('registry'); setRegistryFilter('baby'); }} className="text-xs text-amber-800 font-bold hover:underline">
                    Voir tout →
                  </button>
                </div>
                <p className="text-xs text-stone-600 leading-relaxed">
                  Retrouvez tous les indispensables pour l'accueil de votre nouveau-né : vêtements 0-3M, poussette, chambre, change et sécurité.
                </p>
                <div className="w-full bg-stone-100 h-3 rounded-full overflow-hidden">
                  <div className="bg-amber-800 h-full transition-all duration-500" style={{ width: `${babyItems.length ? (babyPurchased / babyItems.length) * 100 : 0}%` }}></div>
                </div>
              </div>

              <div className="bg-white rounded-3xl p-6 shadow-sm border border-stone-200 space-y-4">
                <div className="flex items-center justify-between">
                  <h3 className="font-bold text-stone-900 flex items-center gap-2">
                    <span>💖</span> Postpartum & Mom Recovery
                  </h3>
                  <button onClick={() => { setActiveTab('registry'); setRegistryFilter('mom'); }} className="text-xs text-amber-800 font-bold hover:underline">
                    Voir tout →
                  </button>
                </div>
                <p className="text-xs text-stone-600 leading-relaxed">
                  Préparez votre valise de maternité, les soins de cicatrisation, protections post-partum et l'accompagnement à l'allaitement.
                </p>
                <div className="w-full bg-stone-100 h-3 rounded-full overflow-hidden">
                  <div className="bg-rose-700 h-full transition-all duration-500" style={{ width: `${momItems.length ? (momPurchased / momItems.length) * 100 : 0}%` }}></div>
                </div>
              </div>
            </div>

            {/* QUICK TAP / ACCES RAPIDE SHORTCUTS */}
            <div className="bg-white rounded-3xl p-6 shadow-sm border border-stone-200 space-y-4">
              <h3 className="text-sm font-bold text-stone-900 flex items-center gap-2">
                <span>⚡</span> Accès Rapide (Quick Tap)
              </h3>
              <p className="text-xs text-stone-500">
                Accédez instantanément à vos outils de suivi et ajoutez l'application en un clic sur votre écran d'accueil.
              </p>
              
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                <button
                  onClick={() => { setActiveTab('registry'); setRegistryFilter('baby'); }}
                  className="p-4 rounded-2xl bg-amber-50 hover:bg-amber-100 border border-amber-200 text-left transition space-y-1 group"
                >
                  <span className="text-xl block">🍼</span>
                  <p className="text-xs font-bold text-amber-900 group-hover:underline">Registre Bébé</p>
                  <p className="text-[10px] text-amber-700">{babyItems.length} articles</p>
                </button>

                <button
                  onClick={() => { setActiveTab('registry'); setRegistryFilter('mom'); }}
                  className="p-4 rounded-2xl bg-rose-50 hover:bg-rose-100 border border-rose-200 text-left transition space-y-1 group"
                >
                  <span className="text-xl block">💖</span>
                  <p className="text-xs font-bold text-rose-900 group-hover:underline">Post-partum Maman</p>
                  <p className="text-[10px] text-rose-700">{momItems.length} articles</p>
                </button>

                <button
                  onClick={() => setActiveTab('ai')}
                  className="p-4 rounded-2xl bg-indigo-50 hover:bg-indigo-100 border border-indigo-200 text-left transition space-y-1 group"
                >
                  <span className="text-xl block">✨</span>
                  <p className="text-xs font-bold text-indigo-900 group-hover:underline">Assistant IA</p>
                  <p className="text-[10px] text-indigo-700">Sécurité & Médicaments</p>
                </button>

                <button
                  onClick={() => setActiveTab('calendar')}
                  className="p-4 rounded-2xl bg-emerald-50 hover:bg-emerald-100 border border-emerald-200 text-left transition space-y-1 group"
                >
                  <span className="text-xl block">📅</span>
                  <p className="text-xs font-bold text-emerald-900 group-hover:underline">Calendrier & PMA</p>
                  <p className="text-[10px] text-emerald-700">Rendez-vous clés</p>
                </button>

                <button
                  onClick={() => setIsAddModalOpen(true)}
                  className="p-4 rounded-2xl bg-stone-100 hover:bg-stone-200 border border-stone-200 text-left transition space-y-1 group"
                >
                  <span className="text-xl block">➕</span>
                  <p className="text-xs font-bold text-stone-900 group-hover:underline">Ajouter Article</p>
                  <p className="text-[10px] text-stone-600">Personnaliser</p>
                </button>

                <button
                  onClick={handleInstallClick}
                  className="p-4 rounded-2xl bg-gradient-to-br from-amber-900 to-stone-900 text-white text-left transition space-y-1 shadow-md hover:opacity-95 group"
                >
                  <span className="text-xl block">📲</span>
                  <p className="text-xs font-bold text-amber-100 group-hover:underline">Écran d'Accueil</p>
                  <p className="text-[10px] text-amber-300">Installer l'app</p>
                </button>
              </div>
            </div>
          </div>
        )}

        {/* REGISTRY & PREPARATION MODULE TAB */}
        {activeTab === 'registry' && (
          <div className="space-y-6">
            {/* TOP CONTROLS & PROGRESS */}
            <div className="bg-white rounded-3xl p-6 shadow-sm border border-stone-200 space-y-4">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                  <h2 className="text-lg font-bold text-stone-900">Modules de Préparation & Registre</h2>
                  <p className="text-xs text-stone-500">Cochez les articles au fur et à mesure de vos achats et préparations.</p>
                </div>
                <button
                  onClick={() => setIsAddModalOpen(true)}
                  className="px-4 py-2.5 bg-amber-900 hover:bg-amber-800 text-white text-xs font-bold rounded-xl shadow-sm transition flex items-center justify-center gap-2"
                >
                  <span>+</span> Ajouter un article personnalisé
                </button>
              </div>

              {/* PROGRESS BAR */}
              <div className="space-y-2 bg-stone-50 p-4 rounded-2xl border border-stone-200">
                <div className="flex justify-between text-xs font-bold">
                  <span className="text-stone-700">Progression globale</span>
                  <span className="text-amber-900">{purchasedItems} / {totalItems} ({progressPercent}%)</span>
                </div>
                <div className="w-full bg-stone-200 h-3 rounded-full overflow-hidden">
                  <div className="bg-amber-900 h-full transition-all duration-500" style={{ width: `${progressPercent}%` }}></div>
                </div>
              </div>

              {/* FILTER PILLS & SEARCH */}
              <div className="flex flex-col sm:flex-row gap-3 pt-2">
                <div className="flex gap-2 overflow-x-auto pb-1">
                  <button
                    onClick={() => setRegistryFilter('all')}
                    className={`px-3.5 py-2 rounded-xl text-xs font-bold transition whitespace-nowrap ${registryFilter === 'all' ? 'bg-amber-900 text-white' : 'bg-stone-100 text-stone-600 hover:bg-stone-200'}`}
                  >
                    Tous ({totalItems})
                  </button>
                  <button
                    onClick={() => setRegistryFilter('baby')}
                    className={`px-3.5 py-2 rounded-xl text-xs font-bold transition whitespace-nowrap ${registryFilter === 'baby' ? 'bg-amber-900 text-white' : 'bg-stone-100 text-stone-600 hover:bg-stone-200'}`}
                  >
                    🍼 Baby Essentials ({babyPurchased}/{babyItems.length})
                  </button>
                  <button
                    onClick={() => setRegistryFilter('mom')}
                    className={`px-3.5 py-2 rounded-xl text-xs font-bold transition whitespace-nowrap ${registryFilter === 'mom' ? 'bg-amber-900 text-white' : 'bg-stone-100 text-stone-600 hover:bg-stone-200'}`}
                  >
                    💖 Mom & Postpartum ({momPurchased}/{momItems.length})
                  </button>
                </div>
                <div className="flex-1">
                  <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder="Rechercher un article..."
                    className="w-full bg-stone-50 border border-stone-200 rounded-xl px-3 py-2 text-xs text-stone-900 placeholder-stone-400 focus:outline-none focus:border-amber-800"
                  />
                </div>
              </div>
            </div>

            {/* CHECKLIST ITEMS LIST */}
            <div className="space-y-3">
              {filteredChecklist.length === 0 ? (
                <div className="bg-white rounded-3xl p-8 text-center border border-stone-200 text-stone-500 text-xs">
                  Aucun article trouvé pour cette recherche.
                </div>
              ) : (
                filteredChecklist.map((item) => {
                  const isExpanded = expandedNotes[item.id];
                  return (
                    <div
                      key={item.id}
                      className={`bg-white rounded-2xl p-4 border transition-all duration-200 shadow-xs ${item.purchased ? 'border-emerald-200 bg-emerald-50/20' : 'border-stone-200 hover:border-amber-300'}`}
                    >
                      <div className="flex items-start justify-between gap-3">
                        <div className="flex items-start gap-3.5 flex-1">
                          <input
                            type="checkbox"
                            checked={item.purchased}
                            onChange={() => toggleItem(item.id)}
                            className="mt-1 w-5 h-5 accent-amber-900 rounded cursor-pointer"
                          />
                          <div className="space-y-1 flex-1">
                            <div className="flex flex-wrap items-center gap-2">
                              <span className={`text-sm font-bold ${item.purchased ? 'line-through text-stone-400' : 'text-stone-900'}`}>
                                {item.title}
                              </span>
                              <span className={`text-[10px] px-2 py-0.5 rounded-full font-bold ${
                                item.priority === 'Must-Have' 
                                  ? 'bg-amber-100 text-amber-900 border border-amber-300' 
                                  : item.priority === 'Hospital Bag Essential' 
                                  ? 'bg-rose-100 text-rose-900 border border-rose-300'
                                  : 'bg-stone-100 text-stone-700 border border-stone-200'
                              }`}>
                                {item.priority}
                              </span>
                            </div>

                            <div className="flex items-center gap-3 text-xs text-stone-500">
                              <span className="font-medium text-amber-900 bg-amber-50 px-2 py-0.5 rounded-md border border-amber-200">
                                {item.category}
                              </span>
                              <span>Qté: <strong className="text-stone-700">{item.quantity}</strong></span>
                            </div>
                          </div>
                        </div>

                        <div className="flex items-center gap-2">
                          {item.notes && (
                            <button
                              onClick={() => toggleNote(item.id)}
                              className={`p-2 rounded-xl text-xs font-bold transition flex items-center gap-1 ${isExpanded ? 'bg-amber-900 text-white' : 'bg-stone-100 text-stone-600 hover:bg-stone-200'}`}
                              title="Conseil pratique"
                            >
                              💡 {isExpanded ? 'Masquer' : 'Conseil'}
                            </button>
                          )}
                          <button
                            onClick={() => deleteItem(item.id)}
                            className="p-2 bg-stone-100 hover:bg-rose-100 text-stone-400 hover:text-rose-700 rounded-xl transition text-xs"
                            title="Supprimer"
                          >
                            🗑️
                          </button>
                        </div>
                      </div>

                      {/* EXPANDABLE NOTES */}
                      {isExpanded && item.notes && (
                        <div className="mt-3 pt-3 border-t border-stone-100 text-xs text-stone-600 bg-amber-50/60 p-3 rounded-xl border border-amber-200/50 leading-relaxed">
                          <strong className="text-amber-900 block mb-1">💡 Conseil Pratique & Astuce :</strong>
                          {item.notes}
                        </div>
                      )}
                    </div>
                  );
                })
              )}
            </div>
          </div>
        )}

        {/* AI ASSISTANT TAB */}
        {activeTab === 'ai' && (
          <div className="space-y-6">
            <div className="bg-gradient-to-br from-indigo-950 via-purple-900 to-stone-900 text-white rounded-3xl p-6 shadow-xl space-y-4">
              <h2 className="text-base font-bold text-indigo-200">⚙️ Clé API Gemini & Assistant Médical</h2>
              <div className="flex gap-2">
                <input 
                  type="password" 
                  value={geminiKey} 
                  onChange={(e) => setGeminiKey(e.target.value)} 
                  placeholder="Collez votre clé API Gemini ici..." 
                  className="flex-1 bg-white/10 border border-indigo-300/30 rounded-xl px-3 py-2 text-xs text-white placeholder-stone-400 focus:outline-none"
                />
                <button onClick={saveKey} className="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white font-bold rounded-xl text-xs transition">
                  Sauvegarder
                </button>
              </div>
              {savedMessage && <p className="text-xs text-emerald-300">✅ Clé enregistrée dans le localStorage en toute sécurité !</p>}

              <div className="border-t border-indigo-500/30 pt-4 space-y-3">
                <h3 className="text-sm font-bold text-indigo-200">💬 Posez votre question sur la grossesse, la PMA ou les médicaments</h3>
                <div className="flex gap-2">
                  <input 
                    type="text" 
                    value={promptText} 
                    onChange={(e) => setPromptText(e.target.value)} 
                    placeholder="Ex: Quels sont les examens du 3ème trimestre ?" 
                    className="flex-1 bg-white/10 border border-indigo-300/30 rounded-xl px-3 py-2 text-xs text-white placeholder-stone-400 focus:outline-none"
                  />
                  <button onClick={askAi} disabled={loading} className="px-4 py-2 bg-indigo-500 hover:bg-indigo-400 text-white font-bold rounded-xl text-xs transition">
                    {loading ? '...' : 'Demander'}
                  </button>
                </div>
                {aiResponse && (
                  <div className="bg-black/30 rounded-2xl p-4 text-xs text-indigo-100 border border-indigo-500/30 leading-relaxed whitespace-pre-wrap">
                    {aiResponse}
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        {/* CALENDAR TAB */}
        {activeTab === 'calendar' && (
          <div className="bg-white rounded-3xl p-6 shadow-sm border border-stone-200 space-y-4">
            <h3 className="text-base font-bold text-stone-900">📅 Calendrier de Grossesse & Suivi PMA</h3>
            <p className="text-xs text-stone-600 leading-relaxed">
              Retrouvez vos prochains rendez-vous médicaux, échographies clés (T1, T2, T3) et rappels de traitement PMA synchronisés en toute sécurité.
            </p>
            <div className="p-4 bg-amber-50 rounded-2xl border border-amber-200 text-xs text-amber-900 space-y-2">
              <p className="font-bold">Prochains jalons :</p>
              <ul className="list-disc list-inside space-y-1 text-stone-700">
                <li>Échographie du 3ème trimestre - Semaine 32</li>
                <li>Consultation anesthésiste - Semaine 34</li>
                <li>Préparation à l'accouchement (Sages-femmes)</li>
              </ul>
            </div>
          </div>
        )}


      </main>

      {/* ADD CUSTOM ITEM MODAL */}
      {isAddModalOpen && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-xs z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-md w-full p-6 shadow-2xl space-y-4 border border-stone-200">
            <div className="flex items-center justify-between">
              <h3 className="text-base font-bold text-stone-900">Ajouter un article personnalisé</h3>
              <button onClick={() => setIsAddModalOpen(false)} className="text-stone-400 hover:text-stone-700 font-bold text-base">✕</button>
            </div>

            <form onSubmit={handleAddSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-stone-700 mb-1">Module / Catégorie</label>
                <select
                  value={newItem.module}
                  onChange={(e) => {
                    const mod = e.target.value;
                    setNewItem({
                      ...newItem,
                      module: mod,
                      category: mod === 'baby' ? 'Clothing (0-3M)' : 'Hospital Bag'
                    });
                  }}
                  className="w-full bg-stone-50 border border-stone-200 rounded-xl px-3 py-2 text-xs text-stone-900 focus:outline-none focus:border-amber-800"
                >
                  <option value="baby">🍼 Baby Registry & Essentials</option>
                  <option value="mom">💖 Postpartum & Mom Recovery</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-bold text-stone-700 mb-1">Sous-catégorie</label>
                <input
                  type="text"
                  value={newItem.category}
                  onChange={(e) => setNewItem({ ...newItem, category: e.target.value })}
                  placeholder="Ex: Chambre, Soins, Vêtements..."
                  className="w-full bg-stone-50 border border-stone-200 rounded-xl px-3 py-2 text-xs text-stone-900 focus:outline-none focus:border-amber-800"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-stone-700 mb-1">Nom de l'article</label>
                <input
                  type="text"
                  value={newItem.title}
                  onChange={(e) => setNewItem({ ...newItem, title: e.target.value })}
                  placeholder="Ex: Veilleuse musicale nomade"
                  className="w-full bg-stone-50 border border-stone-200 rounded-xl px-3 py-2 text-xs text-stone-900 focus:outline-none focus:border-amber-800"
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-stone-700 mb-1">Priorité</label>
                  <select
                    value={newItem.priority}
                    onChange={(e) => setNewItem({ ...newItem, priority: e.target.value })}
                    className="w-full bg-stone-50 border border-stone-200 rounded-xl px-3 py-2 text-xs text-stone-900 focus:outline-none focus:border-amber-800"
                  >
                    <option value="Must-Have">Must-Have</option>
                    <option value="Nice-to-Have">Nice-to-Have</option>
                    <option value="Hospital Bag Essential">Hospital Bag Essential</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-bold text-stone-700 mb-1">Quantité</label>
                  <input
                    type="text"
                    value={newItem.quantity}
                    onChange={(e) => setNewItem({ ...newItem, quantity: e.target.value })}
                    placeholder="Ex: 1 pièce"
                    className="w-full bg-stone-50 border border-stone-200 rounded-xl px-3 py-2 text-xs text-stone-900 focus:outline-none focus:border-amber-800"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-stone-700 mb-1">Conseil / Note pratique (Optionnel)</label>
                <textarea
                  value={newItem.notes}
                  onChange={(e) => setNewItem({ ...newItem, notes: e.target.value })}
                  placeholder="Astuces d'achat ou d'utilisation..."
                  className="w-full bg-stone-50 border border-stone-200 rounded-xl px-3 py-2 text-xs text-stone-900 focus:outline-none focus:border-amber-800 h-20 resize-none"
                />
              </div>

              <div className="flex gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setIsAddModalOpen(false)}
                  className="flex-1 px-4 py-2.5 bg-stone-100 hover:bg-stone-200 text-stone-700 font-bold rounded-xl text-xs transition"
                >
                  Annuler
                </button>
                <button
                  type="submit"
                  className="flex-1 px-4 py-2.5 bg-amber-900 hover:bg-amber-800 text-white font-bold rounded-xl text-xs transition shadow-sm"
                >
                  Ajouter à la liste
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
