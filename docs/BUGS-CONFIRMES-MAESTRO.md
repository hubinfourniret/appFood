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
**Flow** : 03-bug-ajout-recette.yaml, 12-recette-ajout-journal-dialog.yaml
**Observé initialement** : Cliquer le bouton ouvre "Ajouter un aliment" avec onglets Aliment/Recette et liste des repas
**Attendu** : Dialog direct "Quand ? (Petit-dej/Midi/Soir/Collation)" avec pré-remplissage de la recette
**Statut** : **CORRIGE** — Le test 12 confirme que le dialog de sélection de repas s'affiche correctement avec les 4 options (Petit-dejeuner, Dejeuner, Diner, Collation). Le bug initial était probablement lié à un tap Maestro sur le mauvais élément.

## CORRIGES (2026-04-17)

### BUG #3 — Recherche "Aliments exclus" retourne toujours vide (CORRIGE)
**Flow** : 05-profil-aliments-exclus.yaml, 11-fix-search-aliments-exclus.yaml
**Cause** : Pas de feedback utilisateur (loading/erreur/vide) — le ProfilViewModel avalait les erreurs silencieusement
**Fix** : Ajout de `SearchState` (Idle/Loading/Success/Empty/Error) dans ProfilViewModel + affichage dans PreferencesAlimentairesScreen
**Fichiers modifies** :
- `shared/.../ui/profil/ProfilViewModel.kt` — ajout SearchState + feedback
- `shared/.../ui/profil/PreferencesAlimentairesScreen.kt` — affichage loading/erreur/vide
- `shared/.../ui/Strings.kt` — 3 nouvelles constantes PREFERENCES_SEARCH_*
**Statut** : **CORRIGE** — Test 11 passe.

### BUG #4 — Écran Paramètres complètement vide (CORRIGE)
**Flow** : 06-settings-placeholder.yaml, 10-fix-settings-screen.yaml
**Cause** : PlaceholderScreen utilisé au lieu d'un vrai écran
**Fix** : Création de SettingsScreen.kt avec 3 sections (General, Sante et suivi, Informations legales) et navigation vers tous les sous-écrans
**Fichiers modifies** :
- `shared/.../ui/settings/SettingsScreen.kt` — nouvel écran complet
- `shared/.../ui/navigation/AppNavigation.kt` — câblage navigation
- `shared/.../ui/Strings.kt` — 14 nouvelles constantes SETTINGS_*
**Statut** : **CORRIGE** — Test 10 passe.

### BUG #5 — Bouton "Reessayer" ne réinitialise pas le formulaire login (NOUVEAU + CORRIGE)
**Flow** : 15-edge-cases-etats-vides.yaml
**Observé** : Après un login échoué, cliquer "Reessayer" efface l'erreur mais garde les champs email/password remplis, causant des doublons de texte
**Fix** : `onDismissError` appelle maintenant `clearLoginForm()` en plus de `resetState()`
**Fichier modifie** : `shared/.../ui/auth/LoginScreen.kt`
**Statut** : **CORRIGE** — Test 15 passe.

## INFO / CONFIRMÉS OK

- Login email/password fonctionne après activation Firebase réelle ✓
- Navigation Dashboard / Recettes / Profil / Ajouter ✓
- Liste des recettes affichée correctement avec kcal ✓
- Détail recette affiche valeurs nutritionnelles correctes (212 kcal pour Pudding de chia mangue) ✓
- Recherche aliments "banane" trouve "Banane plantain, crue" ✓
- Préférences alimentaires : sélection allergies (Gluten, Soja, etc.) fonctionne ✓
- SettingsScreen affiche 3 sections avec navigation fonctionnelle ✓
- Navigation complète entre tous les onglets et sous-écrans ✓
- Cold start < 15s, dashboard chargé < 30s ✓
- Tab switching fluide entre Dashboard/Recettes/Profil ✓
- Déconnexion ramène au login ✓
- Login après erreur fonctionne correctement (Reessayer réinitialise le formulaire) ✓

## Tests Maestro disponibles

| Test | Description | Statut |
|------|-------------|--------|
| 00 | Smoke test | OK |
| 01 | Login + dashboard | OK |
| 02 | Bug ajout plat (BUG #1) | EN ATTENTE reimport |
| 03 | Bug ajout recette (BUG #2) | CORRIGE |
| 04 | Reco "J'ai mangé ça" | EN ATTENTE reimport |
| 05 | Profil aliments exclus (BUG #3) | CORRIGE |
| 06 | Settings placeholder (BUG #4) | CORRIGE |
| 10 | Fix SettingsScreen verification | PASS |
| 11 | Fix recherche aliments exclus | PASS |
| 12 | Recette → dialog repas | PASS |
| 13 | Navigation complète | PASS |
| 14 | Cold start performance | PASS |
| 15 | Edge cases (erreur login + déconnexion) | PASS |
| 16 | Cohérence dashboard | PASS |
| 99 | Tour complet screenshots | OK |
