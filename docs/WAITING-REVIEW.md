# US en attente de validation utilisateur

## [SYNC-02] — Sync auto au retour en ligne
**Date** : 2026-03-30
**Raison du blocage** : US critique — validation utilisateur requise
**Fichiers modifies** :
- `shared/src/commonMain/kotlin/com/appfood/shared/sync/SyncManager.kt` (auto-sync via ConnectivityMonitor, exponential backoff)
- `shared/src/commonMain/kotlin/com/appfood/shared/sync/SyncPreferences.kt` (interface persistence last sync timestamp)
- `shared/src/commonMain/kotlin/com/appfood/shared/sync/LocalSyncPreferences.kt` (impl in-memory MVP)
- `shared/src/commonMain/kotlin/com/appfood/shared/di/SharedModule.kt` (enregistrement Koin SyncPreferences + SyncManager)

**Resume des changements** :
- `SyncManager.startObserving()` observe la connectivite et declenche `syncAll()` automatiquement au retour en ligne
- `syncAll()` fait push puis pull avec retry exponential backoff (max 5 retries, max 32s delay)
- SyncState observable (Idle, Syncing, Success, Error) pour la UI
- `SyncPreferences` persiste le `lastSyncTimestamp` pour pull incrementiel
- Strategie conflits : server wins (supprime de la queue locale, donnees recuperees au pull)

**Action requise** : L'utilisateur doit review le code et valider ou demander des modifications
