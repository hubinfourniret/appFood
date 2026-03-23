# Phase 2A — Architecture Technique & Stack

Questions et points à couvrir pour les décisions d'architecture.

## 2A.1 — Choix de stack

- As-tu des préférences ou contraintes de langage/framework ? (si oui, on part de là)
- Si non, pose ces questions pour recommander :
  - Type d'application ? (web app, mobile, desktop, API, CLI, hybride...)
  - Besoin de temps réel ? (chat, notifications live, collaborative editing...)
  - Volume de données estimé ? (quelques Mo, Go, To ?)
  - Nombre d'utilisateurs simultanés visés ? (dizaines, milliers, millions ?)
  - Compétences de l'équipe disponible ?
  - Budget infra mensuel acceptable ?

Quand tu proposes une stack, structure-la ainsi :
- **Frontend** : framework, librairie UI, state management
- **Backend** : langage, framework, ORM
- **Base de données** : type (SQL/NoSQL/hybride), choix spécifique
- **Cache** : si nécessaire
- **Queue/Messages** : si nécessaire
- **Search** : si nécessaire

Pour chaque choix, explique brièvement le pourquoi (pas juste "c'est populaire" — en quoi c'est adapté à CE projet).

## 2A.2 — Architecture applicative

- Monolithe ou microservices ? (pour un MVP, recommande presque toujours un monolithe modulaire — les microservices sont un piège de complexité prématurée)
- API-first ? (si multi-clients : web + mobile + API publique)
- Pattern principal : MVC, hexagonal, CQRS, event-driven ?
- Gestion de l'authentification : session, JWT, OAuth2, SSO ?
- Multi-tenant ou single-tenant ?
- Internationalisation (i18n) nécessaire dès le départ ?

## 2A.3 — Modèle de données

- Quelles sont les entités principales du système ? (Utilisateur, Produit, Commande...)
- Quelles sont les relations clés entre elles ?
- Y a-t-il des données sensibles ? (données perso, paiement, santé...)
- Besoin d'historisation ? (audit trail, versioning)
- Besoin de recherche full-text ?
- Volume de données par entité (ordre de grandeur) ?

## 2A.4 — Intégrations externes

- APIs tierces nécessaires ? (paiement, email, SMS, maps, IA...)
- Besoin d'import/export de données ? (CSV, Excel, API...)
- Intégrations avec des outils existants ? (Slack, Teams, CRM, ERP...)
- Webhooks entrants ou sortants ?

## 2A.5 — Points d'attention techniques

Assure-toi d'aborder ces sujets "ennuyeux mais critiques" :
- **Rate limiting** : protection contre l'abus
- **Idempotence** : surtout pour les paiements et actions critiques
- **Migrations de données** : stratégie pour les évolutions de schéma
- **Gestion des erreurs** : circuit breaker, retry, fallback
- **Logging structuré** : pas juste des console.log
- **Tests** : stratégie de test (unitaires, intégration, E2E) — à définir tôt
- **Documentation API** : OpenAPI/Swagger si API exposée

---

## Livrable attendu

Un résumé d'architecture clair :
1. Stack technique retenue (avec justifications)
2. Pattern architectural choisi
3. Entités principales et leurs relations (schéma textuel)
4. Intégrations prévues
5. Points d'attention et décisions structurantes
