# Règles de développement LetzListen

## Séparation iOS / Android

- Ne **jamais** modifier des fichiers dans `ios/` si la demande concerne Android
- Ne **jamais** modifier des fichiers dans `android/` si la demande concerne iOS
- Toujours **lister les fichiers qui seront modifiés** avant de les modifier
- Si la plateforme cible n'est pas claire dans la demande, **demander confirmation** avant d'agir

## Fichiers partagés sensibles

- `stations.json` existe en deux exemplaires indépendants :
  - `ios/Radio/stations.json`
  - `android/app/src/main/assets/stations.json`
- Ne modifier les deux à la fois **que si explicitement demandé**

## Branche de développement

- Toujours développer sur la branche `claude/make-repo-modifiable-UuSR6`
- Ne jamais pousser sur une autre branche sans permission explicite
