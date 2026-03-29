# US en attente de validation utilisateur

## [SYNC-01] — Saisie offline des repas
**Date** : 2026-03-29
**Raison du blocage** : US critique — validation utilisateur requise
**Fichiers crees/modifies** :
- `shared/src/commonMain/kotlin/com/appfood/shared/sync/SyncManager.kt` — Orchestrateur push/pull
- `shared/src/commonMain/kotlin/com/appfood/shared/sync/ConnectivityMonitor.kt` — expect class
- `shared/src/androidMain/kotlin/com/appfood/shared/sync/ConnectivityMonitor.android.kt` — actual Android (stub)
- `shared/src/iosMain/kotlin/com/appfood/shared/sync/ConnectivityMonitor.ios.kt` — actual iOS (stub)
- `shared/src/commonMain/kotlin/com/appfood/shared/data/remote/SyncApi.kt` — Client API /sync/push et /sync/pull
- `shared/src/commonMain/kotlin/com/appfood/shared/api/request/SyncRequests.kt` — SyncPushRequest DTO
- `shared/src/commonMain/kotlin/com/appfood/shared/api/response/SyncResponses.kt` — SyncPushResponse, SyncPullResponse DTOs
- `shared/src/commonMain/kotlin/com/appfood/shared/data/local/LocalSyncQueueDataSource.kt` — Acces sync_queue SQLDelight
- `backend/src/main/kotlin/com/appfood/backend/routes/dto/SyncDtos.kt` — DTOs backend sync (si existant)

**Resume des changements** :
Infrastructure complete de synchronisation offline-first :
1. **SyncManager** — Orchestre push/pull. Push regroupe les entrees par entity_type (journal, poids, hydratation), envoie via POST /sync/push, gere conflits (server wins) et erreurs (retry max 5). Pull recupere les mises a jour serveur depuis un timestamp.
2. **ConnectivityMonitor** — expect/actual pour observer l'etat reseau. Declenche syncAll() automatiquement au retour en ligne.
3. **sync_queue** — Table SQLDelight existante stocke les operations en attente (entity_type, entity_id, action, payload_json, retry_count, last_error).
4. **Enqueue** — Methode pour ajouter une entree a la queue. Si connecte, push immediat.
5. **OfflineBanner** — Composant UI existant affiche "Mode hors ligne" discretement.

**Architecture sync** :
- Strategie : offline-first avec sync_queue
- Conflits : SERVER_WINS (les donnees serveur ecrasent le local au pull)
- Retry : max 5 tentatives avec incrementRetry + last_error
- IDs : generes cote client (entityType_entityId_timestamp)

**Ce qui reste a wirer** (non bloquant pour validation) :
- Les repositories (Journal, Poids, Hydratation) doivent appeler `syncManager.enqueue()` apres chaque ecriture locale
- Le pullUpdates doit deleguer l'upsert aux repositories (TODO dans le code)
- ConnectivityMonitor actuals doivent etre wires avec les APIs plateformes (ConnectivityManager Android, NWPathMonitor iOS)

**Action requise** : L'utilisateur doit review l'architecture sync et valider ou demander des modifications avant de passer au Batch 2 (SYNC-02 depend de SYNC-01).
