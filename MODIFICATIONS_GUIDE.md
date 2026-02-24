# 🔄 Guide des modifications - Lëtz Listen

## ✅ Modifications effectuées

---

## 1️⃣ Renommer le repo GitHub

### Sur GitHub:

1. Va sur: `https://github.com/arnoflorentin/radio-letzebuerg`
2. Click **Settings**
3. Section **Repository name**
4. Change: `radio-letzebuerg` → `letzlisten`
5. Click **Rename**

⚠️ **Attention:** Tous les liens vont changer!

**Avant:**
```
https://github.com/arnoflorentin/radio-letzebuerg
```

**Après:**
```
https://github.com/arnoflorentin/letzlisten
```

### ✅ Le code est déjà mis à jour:

Dans **RadioStationLoader.swift**:
```swift
private let remoteURL = "https://raw.githubusercontent.com/arnoflorentin/letzlisten/main/stations.json"
```

---

## 2️⃣ JSON interne avec une seule station

### Fichier: `stations.json` (dans le bundle Xcode)

**Remplace par:**
```json
{
  "version": "1.0",
  "last_updated": "2025-02-23",
  "stations": [
    {
      "id": "letzlisten",
      "name": "Lëtz Listen",
      "streamURL": "https://stream.letzlisten.lu/radio.mp3",
      "logoImageName": "LetzListenLogo",
      "websiteURL": "https://letzlisten.lu",
      "isEnabled": true
    }
  ]
}
```

### 🎯 Utilité:

- **Fallback** si GitHub est inaccessible
- **Première station** au lancement
- **Une seule** station par défaut

⚠️ **Note:** Change `streamURL` et `websiteURL` selon tes vrais liens!

---

## 3️⃣ Filtrer les stations actives depuis GitHub

### Code modifié dans **RadioStationLoader.swift**:

```swift
// Filter only enabled stations
let enabledStations = config.stations.filter { $0.enabled }

if enabledStations.isEmpty {
    print("⚠️ No enabled stations in GitHub JSON")
    return false
}

// Update stations with only enabled ones
self.stations = enabledStations.sorted { $0.name < $1.name }
```

### 📊 Exemple GitHub JSON:

**Sur GitHub** (`stations.json`):
```json
{
  "version": "2.0",
  "stations": [
    {
      "id": "radio_1007",
      "name": "Radio 100,7",
      "isEnabled": true      ← Sera chargée
    },
    {
      "id": "country_radio",
      "name": "Country Radio",
      "isEnabled": false     ← Sera ignorée
    },
    {
      "id": "eldoradio",
      "name": "Eldoradio",
      "isEnabled": true      ← Sera chargée
    }
  ]
}
```

**Résultat dans l'app:**
```
✅ Radio 100,7
✅ Eldoradio
(Country Radio n'apparaît pas)
```

---

## 🎯 Workflow complet

### 1. **Local (bundle):**
```
stations.json → 1 station (Lëtz Listen)
                ↓
         Fallback uniquement
```

### 2. **GitHub (remote):**
```
stations.json → Toutes les stations
                ↓
         Filter: isEnabled = true
                ↓
         App charge seulement les actives
```

### 3. **Dans l'app:**
```
Lancement → Charge GitHub
            ↓
    Filtre les actives
            ↓
    Affiche uniquement les radios enabled
```

---

## 📝 Checklist

### GitHub:
- [ ] Renommer le repo en `letzlisten`
- [ ] Créer/mettre à jour `stations.json` avec toutes les radios
- [ ] Marquer `isEnabled: false` pour les radios à cacher

### Xcode:
- [ ] Remplacer `stations.json` interne par la version à 1 station
- [ ] Mettre à jour `RadioStationLoader.swift`
- [ ] Build & test

### Test:
- [ ] Vérifier que seules les radios `isEnabled: true` s'affichent
- [ ] Tester sans internet → doit charger "Lëtz Listen"
- [ ] Tester avec internet → doit charger toutes les radios actives

---

## 💡 Avantages

✅ **Contrôle total:** Active/désactive des radios sans rebuild
✅ **Fallback simple:** Une seule station par défaut
✅ **Interface propre:** Seulement les radios disponibles
✅ **Gestion facile:** Edit JSON sur GitHub, push, done!

---

## 🚀 Déploiement

1. Renomme le repo GitHub
2. Update `stations.json` sur GitHub
3. Update `stations.json` dans Xcode
4. Build & release!

**Tout est prêt!** ✨
