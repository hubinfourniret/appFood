# Tests Maestro — appFood

Scripts de test UI automatisés pour l'app Android.

## Prerequis

- Emulateur Android demarre (API 34+)
- App installee : `./gradlew :androidApp:installDebug`
- Maestro installe : `maestro --version` doit repondre
- ADB dans le PATH OU accessible via `%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe`

## Credentials de test

Les flows utilisent ces variables d'environnement. Modifie-les avant de lancer :
- `TEST_EMAIL` : email d'un compte existant (ex: `hubin.fourniret@gmail.com`)
- `TEST_PASSWORD` : mot de passe de ce compte

Ou passe-les en ligne de commande :
```bash
maestro test -e TEST_EMAIL=xxx -e TEST_PASSWORD=xxx .maestro/01-login-dashboard.yaml
```

## Lancer tous les flows

```bash
maestro test .maestro/
```

## Lancer un flow specifique

```bash
maestro test .maestro/01-login-dashboard.yaml
```

## Voir les screenshots

Apres chaque run, Maestro cree un dossier avec les screenshots. Ils sont aussi accessibles via `.maestro/screenshots/`.

## Ordre recommande

1. `00-smoke-test.yaml` — l'app se lance
2. `01-login-dashboard.yaml` — login + arrivee dashboard
3. `02-bug-ajout-plat.yaml` — le bug remonte (dashboard vide apres ajout)
4. `03-bug-ajout-recette.yaml` — le bug recette (bouton qui ne fait rien)
5. `04-reco-j-ai-mange-ca.yaml` — bouton "J'ai mange ca"
6. `05-profil-aliments-exclus.yaml` — recherche qui retourne toujours vide
7. `06-settings-placeholder.yaml` — verifier que c'est bien un placeholder
8. `99-tour-complet-screenshots.yaml` — tour visuel de tous les ecrans
