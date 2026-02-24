# 📻 Lëtz Listen - Résumé Complet du Projet

## 🎯 Vue d'ensemble

**Lëtz Listen** est une application iOS de radio en streaming pour les stations luxembourgeoises.

- **Nom:** Lëtz Listen
- **Plateforme:** iOS (Swift + SwiftUI)
- **Type:** App radio streaming
- **Pays:** Luxembourg 🇱🇺
- **Stations:** 37 radios luxembourgeoises

---

## ✨ Fonctionnalités actuelles

### **1. Lecture Radio**
- ✅ Stream audio en direct
- ✅ 37 stations luxembourgeoises
- ✅ Lecture en arrière-plan
- ✅ Lock screen controls (play/pause depuis écran verrouillé)
- ✅ AirPlay support
- ✅ Métadonnées ICY (titre/artiste automatique)
- ✅ Album artwork via iTunes API

### **2. Gestion des Stations**
- ✅ Chargement dynamique depuis GitHub (JSON)
- ✅ Fallback local si pas d'internet
- ✅ Logos des stations (téléchargés depuis favicons)
- ✅ Stations désactivables (`isEnabled: false` dans JSON)
- ✅ Tri alphabétique (stations actives uniquement)
- ✅ Cache local des logos

### **3. Favoris**
- ✅ Like des chansons avec ❤️
- ✅ Sauvegarde persistante (UserDefaults)
- ✅ Affichage de la station où la chanson a été likée
- ✅ Date/heure du like
- ✅ Swipe pour supprimer
- ✅ Click sur favori → Recherche Google de la chanson

### **4. Partage**
- ✅ Bouton partage (↗️)
- ✅ Message: "Moien, I'm listening to [artist] - [title] now on [station]. [URL]"
- ✅ Compatible WhatsApp, iMessage, Signal, etc.

### **5. Interface Adaptative**
- ✅ **Portrait:** Layout vertical classique
- ✅ **Landscape:** Layout horizontal optimisé
- ✅ Détection automatique d'orientation
- ✅ Transitions fluides
- ✅ Support iPad

### **6. Mémoire & État**
- ✅ Dernière station écoutée sauvegardée
- ✅ Restauration au lancement
- ✅ État de lecture persistant

---

## 📁 Architecture du Projet

### **Fichiers Swift Principaux**

```
Lëtz Listen/
├── LetzListenApp.swift           # Point d'entrée app
├── ContentView.swift              # UI principale (avec landscape)
├── RadioPlayer.swift              # Lecteur audio AVPlayer
├── RadioStation.swift             # Modèle Station
├── RadioStationLoader.swift       # Chargement JSON (GitHub + local)
├── FaviconFetcher.swift          # Téléchargement logos
├── Favorite.swift                 # Modèle Favori
├── FavoritesManager.swift         # Gestion favoris
├── FavoritesView.swift            # UI liste favoris
├── StationSelectorView.swift      # Sélecteur de stations
└── BottomControlBar.swift         # Barre de contrôles
```

### **Assets**
```
Assets.xcassets/
├── AppIcon                        # Icône app (Gëlle Fra Luxembourg)
├── Colors/
└── (logos stations en cache)
```

### **JSON**
```
stations.json (local)              # Fallback 1 station
GitHub: stations.json              # Liste complète (37 stations)
```

---

## 🔧 Configuration Technique

### **Info.plist Clés**
```xml
<key>UIBackgroundModes</key>
<array>
    <string>audio</string>
</array>

<key>NSAppTransportSecurity</key>
<dict>
    <key>NSAllowsArbitraryLoads</key>
    <true/>
</dict>

<key>LSApplicationQueriesSchemes</key>
<array>
    <string>spotify</string>
    <string>youtube</string>
    <string>music</string>
</array>
```

### **Deployment**
- **Minimum iOS:** 16.0
- **Target Device:** iPhone, iPad
- **Orientations:** Portrait, Landscape Left, Landscape Right
- **Language:** Swift 5.9+
- **Framework:** SwiftUI

---

## 🌐 JSON Stations (GitHub)

### **URL Repository:**
```
https://github.com/arnoflorentin/letzlisten
https://raw.githubusercontent.com/arnoflorentin/letzlisten/main/stations.json
```

### **Format JSON:**
```json
{
  "version": "2.0",
  "last_updated": "2025-02-23",
  "stations": [
    {
      "id": "radio_1007",
      "name": "Radio 100,7",
      "streamURL": "https://stream.100komma7.lu/live/mp3-128/vtuner/",
      "logoImageName": "Radio1007Logo",
      "websiteURL": "https://www.100komma7.lu",
      "isEnabled": true
    }
  ]
}
```

### **Stations actives (37):**
- Radio 100,7
- Eldoradio
- Radio ARA
- RTL Radio Lëtzebuerg
- Radio Latina
- DNR
- Radio Gutt Laun
- Country Radio
- ...et 29 autres

### **Filtrage:**
- Seules les stations avec `isEnabled: true` sont affichées
- Tri alphabétique automatique

---

## 🎨 Design & UI

### **Couleurs:**
```swift
Background: Linear Gradient
  - Top: Color(red: 0.1, green: 0.1, blue: 0.2)
  - Bottom: Color(red: 0.05, green: 0.05, blue: 0.15)

Bouton Play: .blue
Bouton Stop: .red
Favoris: .red (cœur plein)
Texte: .white / .white.opacity(0.7-0.8)
```

### **Layouts:**

**Portrait (vertical):**
```
┌──────────────┐
│   ❤️    ↗️   │
│              │
│    [Logo]    │
│   180×180    │
│              │
│ Station Name │
│   Title      │
│   Artist     │
│      ❤️      │
│              │
├──────────────┤
│   Controls   │
│ [🔊][▶][📻] │
└──────────────┘
```

**Landscape (horizontal):**
```
┌────────────────────────────┐
│  ❤️                    ↗️  │
│                            │
│  [Logo]  │  Station Name   │
│  160×160 │  Title          │
│          │  Artist         │
│          │  ❤️             │
│          │                 │
│          │ [🔊][▶][📻]    │
└────────────────────────────┘
```

---

## 🔄 Flux de Données

### **Au lancement:**
```
1. RadioStationLoader.init()
   ↓
2. Charge stations.json (local)
   ↓
3. Tente de charger depuis GitHub
   ↓
4. Filtre stations enabled
   ↓
5. Tri alphabétique
   ↓
6. RadioPlayer.init()
   ↓
7. Restore dernière station (UserDefaults)
   ↓
8. ContentView s'affiche
```

### **Changement de station:**
```
1. User tap sur station
   ↓
2. RadioPlayer.switchStation()
   ↓
3. Stop current stream
   ↓
4. Load new station URL
   ↓
5. AVPlayer.play()
   ↓
6. Save station ID (UserDefaults)
   ↓
7. Update UI
```

### **Métadonnées:**
```
Stream ICY metadata
   ↓
RadioPlayer observe "timedMetadata"
   ↓
Parse artiste/titre
   ↓
Fetch iTunes artwork
   ↓
Update MPNowPlayingInfoCenter
   ↓
Lock screen + UI update
```

---

## 📦 Dépendances

### **Frameworks iOS:**
- `SwiftUI` - UI
- `AVFoundation` - Audio streaming
- `MediaPlayer` - Lock screen controls
- `Combine` - Reactive programming
- `UserDefaults` - Persistance simple

### **Packages externes:**
- ❌ Aucun! Tout natif iOS

---

## 🐛 Points d'Attention

### **Warnings connus (non critiques):**
- `BSActionErrorDomain code 6` - Simulateur iOS uniquement
- `LaunchServices database errors` - Simulateur uniquement

### **Limitations:**
- ❌ Pas de CarPlay (retiré du projet)
- ❌ Pas d'alarmes (retiré du projet)
- ⚠️ Certains streams nécessitent HTTP (NSAllowsArbitraryLoads)

### **Optimisations à faire:**
- Favicon cache: actuellement clearé à chaque lancement
- Pourrait bénéficier d'un cache plus intelligent

---

## 🎯 Features Implémentées Récemment

### **Dernières modifications:**
1. ✅ Support landscape complet
2. ✅ Favoris cliquables (recherche Google)
3. ✅ Dernière station mémorisée
4. ✅ Filtrage stations désactivées
5. ✅ Badge favoris retiré
6. ✅ Favicon cache clear au lancement

### **Features retirées:**
- ❌ CarPlay (complexité vs bénéfice)
- ❌ Alarmes/réveil (limitations iOS)
- ❌ Animations avancées (trop chargé)

---

## 🔑 Points Clés pour Claude Code

### **1. Structure actuelle est stable**
- App fonctionne parfaitement
- Toutes features principales implémentées
- Code propre et commenté

### **2. Possibilités d'amélioration:**
- Sleep timer (arrêt automatique)
- Recherche dans favoris
- Export favoris
- Statistiques d'écoute
- Widget iOS

### **3. Architecture est extensible:**
- RadioPlayer peut être étendu
- JSON peut avoir plus de champs
- UI est modulaire (Portrait/Landscape séparés)

### **4. Tests nécessaires:**
- Simulateur iOS ✅
- Device réel recommandé
- Test toutes orientations
- Test lock screen controls

---

## 📝 Commandes Utiles

### **Build & Run:**
```bash
⌘R                    # Run
⌘B                    # Build
⌘⇧K                   # Clean
⌘.                    # Stop
```

### **Debugging:**
```bash
⌘K                    # Clear console
⌘\                    # Breakpoint
⌘⌥P                   # Resume
```

---

## 🚀 Prochaines Étapes Possibles

### **Priorité Haute:**
1. Sleep Timer (⏱️ arrêt après X minutes)
2. Widget iOS (📱 contrôles rapides)
3. Recherche favoris (🔍)

### **Priorité Moyenne:**
4. Statistiques (📊 temps d'écoute)
5. Thèmes de couleur (🎨)
6. Export favoris (💾 CSV/JSON)

### **Priorité Basse:**
7. CarPlay (si demandé)
8. Android version (Flutter?)
9. Apple Watch app

---

## 📧 Contact & Repo

- **Developer:** Arno Florentin
- **GitHub:** https://github.com/arnoflorentin/letzlisten
- **App Name:** Lëtz Listen
- **Bundle ID:** (à définir dans Xcode)
- **Version:** 1.0 (en développement)

---

## ✅ Checklist Avant Transmission

- [x] Code compile sans erreurs
- [x] Toutes features documentées
- [x] JSON repository configuré
- [x] Landscape support fonctionnel
- [x] Favoris fonctionnels
- [x] Partage fonctionnel
- [x] Lock screen controls OK
- [x] Background playback OK

---

**Le projet est prêt pour Claude Code!** 🚀

Utilise ce résumé pour expliquer le contexte à Claude Code et lui permettre de comprendre rapidement l'architecture et les fonctionnalités de l'app.
