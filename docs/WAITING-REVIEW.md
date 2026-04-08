# US en attente de validation utilisateur

## [LEGAL-01] — Politique de confidentialite
**Date** : 2026-04-08
**Raison du blocage** : US critique — contenu legal placeholder, validation utilisateur requise
**Fichiers modifies** :
- `shared/src/commonMain/kotlin/com/appfood/shared/ui/legal/PrivacyPolicyScreen.kt`
- `shared/src/commonMain/kotlin/com/appfood/shared/ui/Strings.kt` (sections PRIVACY_*)
**Resume des changements** : Ecran scrollable avec 9 sections RGPD placeholder (responsable traitement, finalites, base legale, duree conservation, droits utilisateur, sous-traitants, donnees de sante, cookies, modifications). Composants reutilisables LegalSectionTitle/Body.
**Action requise** : Valider la structure. Le contenu final sera redige par un juriste.

## [LEGAL-02] — Conditions Generales d'Utilisation
**Date** : 2026-04-08
**Raison du blocage** : US critique — contenu legal placeholder, validation utilisateur requise
**Fichiers modifies** :
- `shared/src/commonMain/kotlin/com/appfood/shared/ui/legal/TermsOfServiceScreen.kt`
- `shared/src/commonMain/kotlin/com/appfood/shared/ui/Strings.kt` (sections TOS_*)
**Resume des changements** : Ecran scrollable avec 9 sections placeholder (objet du service, avertissement medical, inscription, responsabilites, propriete intellectuelle, donnees personnelles, modification/resiliation, droit applicable). Version datee.
**Action requise** : Valider la structure. Le contenu final sera redige par un juriste.

## [LEGAL-04] — Chiffrement des donnees sensibles
**Date** : 2026-04-08
**Raison du blocage** : US critique — chiffrement coeur, validation utilisateur requise
**Fichiers modifies** :
- `backend/src/main/kotlin/com/appfood/backend/security/EncryptionService.kt` (AES-256-GCM)
- `backend/src/main/kotlin/com/appfood/backend/database/dao/UserProfileDao.kt` (chiffrement transparent)
- `backend/src/main/resources/db/migration/V6__encrypt_sensitive_profile_columns.sql`
- `backend/src/main/resources/application.conf` (section encryption)
**Resume des changements** : Chiffrement AES-256-GCM des champs sensibles (poids, taille, age) dans UserProfileDao. Cle via variable d'env ENCRYPTION_KEY. Mode clair avec warning en dev local. Migration Flyway convertit les colonnes INTEGER/DOUBLE en TEXT.
**Action requise** : Valider l'approche de chiffrement. Configurer ENCRYPTION_KEY sur Railway (`openssl rand -base64 32`).
