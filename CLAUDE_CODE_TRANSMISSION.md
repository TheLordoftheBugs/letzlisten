# 📦 Fichiers à Transmettre à Claude Code

## 🎯 Fichiers Swift Essentiels

### **Core App:**
1. ✅ `LetzListenApp.swift` - Point d'entrée
2. ✅ `ContentView_LANDSCAPE_READY.swift` - UI principale (renommer en ContentView.swift)

### **Audio & Stations:**
3. ✅ `RadioPlayer.swift` - Lecteur audio
4. ✅ `RadioStation.swift` - Modèle station
5. ✅ `RadioStationLoader.swift` - Chargement JSON
6. ✅ `FaviconFetcher.swift` - Logos stations

### **Favoris:**
7. ✅ `Favorite.swift` - Modèle favori
8. ✅ `FavoritesManager.swift` - Gestion favoris
9. ✅ `FavoritesView.swift` - UI favoris

### **UI Components:**
10. ✅ `StationSelectorView.swift` - Sélecteur stations

### **JSON:**
11. ✅ `stations_internal.json` - Fallback local (renommer en stations.json)
12. ✅ `stations_github_example.json` - Exemple GitHub

### **Documentation:**
13. ✅ `PROJECT_SUMMARY.md` - Résumé complet
14. ✅ `LANDSCAPE_GUIDE.md` - Guide landscape
15. ✅ `MODIFICATIONS_GUIDE.md` - Guide modifications

---

## 📋 Instructions pour Claude Code

### **1. Contexte:**
```
Voici une app iOS de radio streaming pour le Luxembourg.
Elle est fonctionnelle et complète.
J'ai besoin d'aide pour [ta demande spécifique].
```

### **2. Donne-lui PROJECT_SUMMARY.md en premier:**
```
Lis d'abord PROJECT_SUMMARY.md pour comprendre l'architecture.
```

### **3. Ensuite les fichiers Swift:**
```
Voici les fichiers principaux du projet:
[Upload les .swift files]
```

### **4. Demande spécifique:**
```
Exemples de demandes:
- "Ajoute un sleep timer"
- "Crée un widget iOS"
- "Ajoute des statistiques d'écoute"
- "Optimise le cache des logos"
- "Debug ce problème: [décris]"
```

---

## 🗂️ Structure Recommandée

```
📁 LetzListen_For_ClaudeCode/
├── 📄 PROJECT_SUMMARY.md          ← Lis d'abord!
├── 📁 Swift/
│   ├── LetzListenApp.swift
│   ├── ContentView.swift          (renommé depuis ContentView_LANDSCAPE_READY.swift)
│   ├── RadioPlayer.swift
│   ├── RadioStation.swift
│   ├── RadioStationLoader.swift
│   ├── FaviconFetcher.swift
│   ├── Favorite.swift
│   ├── FavoritesManager.swift
│   ├── FavoritesView.swift
│   └── StationSelectorView.swift
├── 📁 JSON/
│   ├── stations.json              (renommé depuis stations_internal.json)
│   └── stations_github_example.json
└── 📁 Guides/
    ├── LANDSCAPE_GUIDE.md
    └── MODIFICATIONS_GUIDE.md
```

---

## 💡 Conseil

**Commence ta conversation avec Claude Code comme ça:**

```
Salut! J'ai une app iOS de radio streaming "Lëtz Listen" pour le Luxembourg.

Lis d'abord PROJECT_SUMMARY.md pour comprendre l'architecture.

L'app est fonctionnelle avec:
- 37 stations luxembourgeoises
- Favoris
- Support landscape
- Lock screen controls
- Chargement JSON depuis GitHub

J'aimerais que tu m'aides à [ta demande].
```

---

## ✅ Checklist Transmission

- [ ] PROJECT_SUMMARY.md (contexte)
- [ ] Tous les fichiers .swift
- [ ] stations.json (local)
- [ ] Info.plist configuration expliquée
- [ ] Ta demande spécifique claire

---

**Bonne chance avec Claude Code!** 🚀
