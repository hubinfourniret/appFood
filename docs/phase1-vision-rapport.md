# appFood — Phase 1 : Vision & Exploration

> Date : 2026-03-23
> Version : 1.0

---

## 1. Genese du projet

### Origine de l'idee

Le projet appFood est ne d'un besoin personnel : pouvoir suivre precisement ses apports en nutriments, connaitre ses quotas en fonction de son poids et de son activite physique, et surtout etre oriente vers des plats et aliments permettant d'atteindre ces quotas. Ce dernier point — la recommandation proactive — est identifie comme un manque dans les applications existantes (MyFitnessPal, Yazio, Cronometer).

### Probleme identifie

Les personnes ayant un regime alimentaire specifique (vegan, vegetarien) et pratiquant une activite physique ont des besoins nutritionnels particuliers : vitamine B12, fer, proteines vegetales, omega-3, zinc, calcium... Aujourd'hui, aucune application ne leur dit concretement **quoi manger pour combler leurs manques**. Les apps existantes se contentent de tracker passivement ce que l'utilisateur mange, sans le guider.

### Vision a 2 ans

Une application de reference pour la communaute vegan/vegetarienne sportive en France, qui simplifie radicalement le suivi nutritionnel en le rendant proactif et personnalise. L'utilisateur ne se demande plus "qu'est-ce que je dois manger ?" — l'app lui repond.

---

## 2. Cible & Utilisateurs

### Cible principale (niche de lancement)

**Vegans et vegetariens pratiquant une activite sportive en France.**

Pourquoi cette niche :
- Communaute engagee, active en ligne, habituee a utiliser des outils numeriques
- Besoins nutritionnels reels et specifiques (risques de carences documentes)
- Bouche-a-oreille fort dans les communautes vegan/sport
- Marche suffisamment grand pour valider le concept, suffisamment niche pour se differencier

### Utilisateurs secondaires (elargissement futur)

- Autres regimes alimentaires (sans gluten, allergies, halal, etc.)
- Grand public souhaitant mieux manger
- Sportifs non-vegetariens cherchant a optimiser leur nutrition
- Marche international

### Personas pressenties

1. **Le vegan sportif** — Pratique du sport regulierement, veut s'assurer qu'il n'a pas de carences, cherche des recettes adaptees a ses besoins
2. **Le vegetarien curieux** — Commence a s'interesser a sa nutrition, veut un outil simple pour savoir s'il mange equilibre
3. **L'administrateur** (equipe interne) — Gere la base de recettes, modere le contenu, analyse les usages

---

## 3. Positionnement & Differenciation

### Concurrence directe

| Application | Forces | Faiblesses |
|-------------|--------|------------|
| MyFitnessPal | Base d'aliments enorme, communaute | Tracking passif uniquement, pas de recommandation, donnees parfois incorrectes |
| Yazio | Belle UX, plans repas | Peu personnalise pour les regimes specifiques |
| Cronometer | Tres precis sur les micronutriments | Complexe, pas de recommandation proactive, UX austere |

### Concurrence indirecte

- Suivi sur Excel/papier
- Conseils de nutritionnistes
- Blogs et forums vegan
- Livres de recettes

### Differenciation appFood

1. **Proactif** : L'app recommande des aliments et recettes pour atteindre les quotas, elle ne se contente pas de tracker
2. **Personnalisation poussee** : Profil alimentaire (regime, aliments exclus, preferences, allergies), calcul de quotas adapte au profil complet
3. **Multi-temporalite** : Suivi jour / semaine / mois selon les nutriments (certains nutriments se regardent a la semaine, d'autres au mois)
4. **Niche assumee** : Concu pour les vegan/vegetariens sportifs, pas un outil generique
5. **Simplification de la saisie** : Validation directe des plats recommandes comme raccourci de saisie

---

## 4. Modele economique

### Structure : Freemium + Publicite

**Version gratuite :**
- Saisie manuelle des aliments avec recherche intelligente
- Calcul automatique des quotas personnalises
- Dashboard jour/semaine
- Recommandations d'aliments basiques
- Livre de recettes
- Suivi du poids et de l'hydratation
- Publicite non-intrusive (bannieres, pas d'interstitiels)

**Version premium :**
- Suppression des publicites
- Reconnaissance photo des repas (IA) : prise en photo → identification des aliments + estimation des quantites → pre-remplissage de la saisie
- Dashboard mensuel + tendances long terme
- Recommandations de recettes avancees
- Statistiques detaillees
- Adaptation IA des recettes aux besoins (V2)
- Compatibilite robots de cuisine (V2)

**Philosophie freemium** : Le core de l'app reste gratuit et pleinement utilisable. Le premium apporte du confort (pas de pub, photo), de la profondeur (stats, IA) et des integrations. L'utilisateur gratuit ne doit jamais se sentir bloque.

**Gestion des abonnements** : Via Google Play Billing + StoreKit 2 (Apple), avec periode d'essai gratuite. Commission stores : 15-30%. Envisager RevenueCat pour simplifier la gestion cross-platform.

### Monetisation complementaire envisagee

- Partenariats avec des marques vegan/bio (non prioritaire, a explorer)

---

## 5. Perimetre MVP

### Fonctionnalites MVP (indispensables pour le lancement)

1. **Saisie des aliments consommes**
   - Recherche dans la base de donnees (Ciqual + Open Food Facts)
   - Portions standard (une pomme, une cuillere a soupe, un bol...) et saisie en grammes
   - Favoris et repas recents
   - Saisie rapide (copie d'un jour precedent, validation d'un plat recommande)

2. **Calcul automatique des quotas nutritionnels**
   - Basé sur : poids, taille, age, regime alimentaire, activite physique
   - Modification manuelle possible avec option de retour au calcul automatique

3. **Dashboard de suivi**
   - Vue journaliere, hebdomadaire, mensuelle
   - Visualisation claire des nutriments atteints / manquants
   - Focus sur les nutriments critiques pour le profil (B12, fer, proteines...)
   - Widget de suivi de l'hydratation

4. **Suivi du poids dans le temps**
   - Historique du poids avec graphique d'evolution
   - Recalcul automatique des quotas quand le poids change significativement

5. **Suivi de l'hydratation**
   - Saisie rapide par increments (verre, bouteille)
   - Objectif calcule en fonction du poids et de l'activite physique

6. **Recommandation d'aliments**
   - Suggestion d'aliments pour combler les manques identifies
   - Filtrage par preferences et exclusions de l'utilisateur

7. **Livre de recettes**
   - Base de recettes vegan/vegetariennes integree
   - Recettes liees aux nutriments qu'elles apportent
   - L'utilisateur peut valider un plat recommande et adapter les quantites

8. **Personnalisation du profil**
   - Regime alimentaire (vegan, vegetarien, etc.)
   - Activite physique (type, frequence, intensite)
   - Aliments exclus / non aimes
   - Allergies et intolerances
   - Onboarding progressif avec option de skip (lien discret, valeurs par defaut si skip)

9. **Conformite legale & Confidentialite**
   - Politique de confidentialite (donnees alimentaires = donnees de sante au sens RGPD art. 9)
   - CGU avec disclaimer medical
   - Gestion du consentement (analytics, publicite)
   - Chiffrement des donnees sensibles en base et localement

10. **Support utilisateur**
    - Page A propos avec email de contact (obligatoire App Store / Google Play)
    - FAQ integree

### Fonctionnalites V1.1 (post-lancement)

- Gestion freemium / premium (abonnements, paywall, publicite)
- Reconnaissance photo des repas (feature premium)
- Scan de code-barres
- Notifications intelligentes ("Il te manque du fer cette semaine")
- Rappel d'hydratation
- Objectif de poids (prise de masse, maintien, seche)
- Portions personnalisees
- Historique et tendances sur plusieurs mois
- Formulaire de feedback in-app
- Export des donnees

### Fonctionnalites V2 (moyen terme)

- Adaptation IA des recettes (remplacement d'ingredients pour optimiser les nutriments)
- Import de livres de recettes complets
- Soumission de recettes par la communaute (avec moderation)
- Gamification legere (semaines de suivi, progression positive)
- Compatibilite robots de cuisine (Companion, Cookeo, Thermomix...)

### Fonctionnalites envisagees (long terme)

- Internationalisation (langues, bases d'aliments locales)
- Integration avec des apps de sport (Strava, etc.)
- Suivi par un nutritionniste (partage des donnees)
- Marketplace de recettes premium

---

## 6. Risques identifies et mitigations

### Risque 1 : Cross-platform iOS/Android

**Criticite** : Elevee
**Description** : L'equipe maitrise Kotlin/Jetpack Compose mais pas le dev iOS natif.
**Mitigation** : Choix de Kotlin Multiplatform (KMP) + Compose Multiplatform, qui permet de rester dans l'ecosysteme Kotlin tout en ciblant iOS et Android. Technologie mature (soutenue par JetBrains et Google).
**Statut** : Resolu — decision prise.

### Risque 2 : Qualite des donnees nutritionnelles

**Criticite** : Elevee
**Description** : Les bases de donnees gratuites (Open Food Facts) ont des donnees inegales, surtout pour des produits de niche vegan.
**Mitigation** : Strategie a deux niveaux — Ciqual (ANSES) comme source prioritaire (~3000 aliments, donnees officielles et fiables), Open Food Facts en complement pour les produits industriels/marques. Flag de qualite des donnees pour l'utilisateur.
**Statut** : Resolu — strategie definie.

### Risque 3 : Scalabilite des recettes

**Criticite** : Moyenne
**Description** : L'ajout manuel de recettes ne scale pas a long terme. Il faudra des recettes couvrant tous les manques nutritionnels possibles.
**Mitigation** :
- Court terme (MVP) : 50-100 recettes ajoutees manuellement, bien taguees par nutriments
- Moyen terme : Import de livres de recettes + adaptation par IA pour ajouter/remplacer des ingredients selon les besoins nutritionnels
- Long terme : Recettes communautaires avec moderation
**Statut** : Strategie definie, a detailler en Phase 2.

### Risque 4 : Reglementaire — Conseil medical

**Criticite** : Moyenne
**Description** : Les recommandations nutritionnelles frolent le conseil medical.
**Mitigation** :
- Disclaimer clair a l'inscription : "Cette app ne remplace pas un avis medical / nutritionniste"
- Formulations en suggestions ("Cet aliment pourrait t'aider a..."), jamais en injonctions
- Quotas bases exclusivement sur les AJR/ANC officiels (ANSES)
- Aucun conseil lie a des pathologies
- Pas d'utilisation des termes "diagnostic" ou "prescription"
**Statut** : Resolu — regles definies.

### Risque 5 : Retention utilisateur

**Criticite** : Elevee
**Description** : Les apps de tracking ont un taux d'abandon eleve. La saisie quotidienne peut devenir une corvee.
**Mitigation** :
- Philosophie "outil qui simplifie la vie", pas "jeu qui culpabilise"
- Simplification maximale de la saisie : portions standard, favoris, repas recents, copie de jours, validation directe des plats recommandes, scan code-barres (V1.1)
- Les recommandations proactives sont le levier principal de retention
- Gamification legere et positive (suivi de semaines, progression), sans pression
- A terme : compatibilite robots de cuisine pour reduire encore la friction
**Statut** : Strategie definie, a concretiser en Phase 2 (UX).

### Risque 6 : Conformite RGPD — Donnees de sante

**Criticite** : Elevee
**Description** : Les donnees alimentaires couplees au poids/taille/age constituent des donnees de sante au sens du RGPD (article 9). Elles beneficient d'une protection renforcee et necessitent un consentement explicite.
**Mitigation** :
- Politique de confidentialite detaillee, conforme RGPD, accessible des l'inscription
- CGU couvrant la non-substitution a un avis medical
- Consentement explicite (case non pre-cochee) avant toute collecte
- Chiffrement des donnees sensibles en base et localement
- Gestion du consentement granulaire (analytics, publicite)
- Droit a l'effacement et a la portabilite implementes
- Envisager de consulter un juriste pour la redaction finale
**Statut** : Identifie — a traiter imperativement avant le lancement.

### Risque 7 : Validation App Store / Google Play

**Criticite** : Moyenne
**Description** : Apple et Google ont des exigences specifiques pour la validation (contact support, politique de confidentialite, Apple Sign-In si social login, etc.).
**Mitigation** :
- Page A propos avec email de contact (obligatoire)
- FAQ integree
- Politique de confidentialite et CGU accessibles
- Apple Sign-In au MVP (obligatoire si Google Sign-In est propose — exigence App Store)
**Statut** : Identifie — pris en compte dans le backlog.

---

## 7. Equipe & Ressources

### Equipe

| Membre | Profil | Competences |
|--------|--------|-------------|
| Membre 1 | Developpeur | Kotlin, Jetpack Compose, acces Mac pour dev iOS |
| Membre 2 | Developpeur | Kotlin, Jetpack Compose, memes competences |

### Ressources

- **Budget** : Debloquable si necessaire (hebergement, donnees, APIs)
- **Timeline** : Pas de deadline stricte, mais volonte d'avoir une vision claire pour se projeter
- **Infrastructure** : A definir en Phase 2

---

## 8. Decisions prises

| Decision | Choix | Justification |
|----------|-------|---------------|
| Plateforme | Mobile iOS + Android | App grand public, usage quotidien |
| Technologie | KMP + Compose Multiplatform | Capitalise sur les competences Kotlin de l'equipe |
| Niche de lancement | Vegan/vegetariens sportifs (France) | Communaute engagee, besoins specifiques reels |
| Donnees nutritionnelles | Ciqual (priorite) + Open Food Facts | Fiabilite officielle + couverture produits industriels |
| Recettes MVP | Ajout manuel (50-100) | Suffisant pour valider, bien taguer par nutriments |
| Monetisation | Freemium + pub | Modele eprouve, barriere d'entree basse |
| Philosophie UX | Outil simple, pas de gamification lourde | Retention par la valeur, pas par la culpabilite |

---

*Ce document sera complete par les rapports des phases suivantes : Architecture (Phase 2), Backlog (Phase 3), et Dispatch (Phase 4).*
