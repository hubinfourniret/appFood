# Bugs confirmés par tests Maestro (2026-04-16)

Tests exécutés sur emulator-5554 avec compte `hubin.fourniret1@gmail.com`.

## BLOQUANTS

### BUG #1 — Dashboard reste vide après ajout d'un plat (boucle infinie)
**Flows** : 02-bug-ajout-plat.yaml, 04-reco-j-ai-mange-ca.yaml
**Logcat** : `JournalViewModel.onValidateEntry error: NETWORK_ERROR Aliment non trouve: 8e14da7a-f2c8-460f-bf22-569af784093f`
**Cause** : Meilisearch désync avec PostgreSQL — l'index renvoie des UUIDs qui n'existent plus en DB
**Fix** : Variable `FORCE_REIMPORT_ALL=true` sur Railway + restart (retirer ensuite)
**Statut** : **EN ATTENTE UTILISATEUR** — le code fix est déployé, il faut juste déclencher le reimport.

### BUG #2 — Bouton "Ajouter a mon journal" sur détail recette ouvre écran générique
**Flow** : 03-bug-ajout-recette.yaml
**Observé** : Cliquer le bouton ouvre "Ajouter un aliment" avec onglets Aliment/Recette et liste des repas
**Attendu** : Dialog direct "Quand ? (Petit-dej/Midi/Soir/Collation)" + "Quel jour ?" avec pré-remplissage de la recette
**Impact UX** : L'utilisateur doit re-chercher la recette qu'il regarde déjà = redondant
**Fichier à corriger** : `shared/src/commonMain/kotlin/com/appfood/shared/ui/recette/RecetteDetailScreen.kt`

## FONCTIONNALITÉS MANQUANTES / STUBS

### BUG #3 — Recherche "Aliments exclus" retourne toujours vide
**Flow** : 05-profil-aliments-exclus.yaml
**Observé** : Taper "banane" dans Profil → Préférences → Aliments exclus n'affiche aucun résultat ni "Aucun résultat". La liste reste figée sur "Aucun aliment exclu" (texte vide).
**Cause probable** : `searchAliments()` retourne `emptyList()` dans le ViewModel (TODO)

### BUG #4 — Écran Paramètres complètement vide
**Flow** : 06-settings-placeholder.yaml
**Observé** : Tap sur icône ⚙ depuis Profil → écran avec uniquement le titre "Paramètres" au milieu, rien d'autre
**Fichier** : `shared/src/commonMain/kotlin/com/appfood/shared/ui/settings/SettingsScreen.kt` (PlaceholderScreen)

## INFO / CONFIRMÉS OK

- Login email/password fonctionne après activation Firebase réelle ✓
- Navigation Dashboard / Recettes / Profil / Ajouter ✓
- Liste des recettes affichée correctement avec kcal ✓
- Détail recette affiche valeurs nutritionnelles correctes (212 kcal pour Pudding de chia mangue) ✓
- Recherche aliments "banane" trouve "Banane plantain, crue" ✓
- Préférences alimentaires : sélection allergies (Gluten, Soja, etc.) fonctionne ✓
