# Actions requises par l'utilisateur

Les agents ne peuvent PAS faire ces actions. Elles necessitent une intervention humaine.

## Actions terminees

| Action | US | Statut |
|--------|-----|--------|
| Creer un projet Firebase + obtenir `google-services.json` et `GoogleService-Info.plist` | AUTH-01 a AUTH-05, NOTIF-01 | Done |
| Configurer Google Sign-In dans la console Firebase (SHA-1 Android) | AUTH-03 | Done |
| Creer un compte Railway + provisionner les services | INFRA-02 | Done |
| Telecharger la base Ciqual (CSV) depuis le site ANSES | DATA-01 | Done |
| Configurer ENCRYPTION_KEY sur Railway (`openssl rand -base64 32`) | LEGAL-04 | Done |

## Actions en attente

### Bloquantes pour publication

| Priorite | Action | Bloque quelles US | Statut |
|----------|--------|-------------------|--------|
| 🔴 Bloquant | Configurer Apple Sign-In (Apple Developer Account + Firebase) | AUTH-05 | Todo |
| 🔴 Bloquant | Creer les comptes Google Play Console et Apple Developer | Publication | Todo |
| 🔴 Bloquant | Desactiver FIREBASE_MOCK=true en prod et configurer le vrai Firebase Admin SDK | AUTH-01 a AUTH-05 | Todo |
| 🔴 Bloquant | Rediger le contenu final de la politique de confidentialite (consulter juriste) | LEGAL-01 | Todo |
| 🔴 Bloquant | Rediger le contenu final des CGU (consulter juriste) | LEGAL-02 | Todo |

### Importantes pour qualite

| Priorite | Action | Bloque quelles US | Statut |
|----------|--------|-------------------|--------|
| 🟡 Important | Creer un compte Sentry + obtenir le DSN | INFRA-03 | En cours |
| 🟡 Important | Creer un compte UptimeRobot | INFRA-03 | Todo |
| 🟡 Important | iOS : verifier dans Xcode que `GoogleService-Info.plist` est dans Build Phases (necessite un Mac) | AUTH iOS | Todo |
| 🟡 Important | Creer un utilisateur ADMIN en base (INSERT manuel) pour gerer les recettes | RECETTES-03 | Todo |
| 🟡 Important | Configurer SDK Firebase client pour notifications push (code natif Android/iOS) | NOTIF-01 | Todo |
| 🟡 Important | Integrer SQLCipher pour chiffrement base locale SQLDelight | LEGAL-04 local | Todo |
| 🟡 Important | Configurer Testcontainers Meilisearch dans CI pour tests integration | QUALITE-02 | Todo |

### Plus tard (V1.1 / post-MVP)

| Priorite | Action | Bloque quelles US | Statut |
|----------|--------|-------------------|--------|
| 🔵 Plus tard | Rediger le contenu de la FAQ (remplacer les placeholders) | SUPPORT-02 | Todo |
| 🔵 Plus tard | Creer 50-100 recettes vegan/vegetariennes | RECETTES-03 | Todo |
| 🔵 Plus tard | Decision produit : multi-device FCM (voir FcmTokensTable) | NOTIF-01 | Todo |
| 🔵 Plus tard | Tester le build iOS sur Mac (simulateur + device) | Publication iOS | Todo |
| 🔵 Plus tard | Configurer le domaine personnalise (api.appfood.fr) | INFRA-02 | Todo |
| 🔵 Plus tard | Lien vers politique/CGU depuis les stores | LEGAL-01 | Todo |
