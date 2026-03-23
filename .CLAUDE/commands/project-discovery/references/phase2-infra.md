# Phase 2C — Infrastructure, CI/CD & Déploiement

Questions et points à couvrir pour l'infrastructure et les opérations.

## 2C.1 — Hébergement

- Cloud provider préféré ? (AWS, GCP, Azure, Vercel, Railway, Fly.io, Hetzner...)
- Si pas de préférence, recommande en fonction de :
  - Complexité du projet (simple = PaaS type Vercel/Railway, complexe = cloud provider)
  - Budget (PaaS = plus cher par requête mais zéro ops, IaaS = moins cher mais plus de travail)
  - Contraintes de localisation des données (RGPD → serveurs EU)
  - Compétences de l'équipe en ops/DevOps
- Serverless ou serveurs dédiés/containers ?
- Besoin de CDN ? (contenu statique, images, vidéos)
- Stockage de fichiers : S3-compatible, local, CDN ?

## 2C.2 — Environnements

- Combien d'environnements ? (minimum recommandé : dev, staging, production)
- Stratégie de branches Git ? (trunk-based, GitFlow, GitHub Flow)
- Comment sont gérées les variables d'environnement et secrets ?
- Base de données par environnement ? (attention aux coûts)
- Données de test/seed : stratégie ?

## 2C.3 — CI/CD

- Outil de CI/CD ? (GitHub Actions, GitLab CI, CircleCI, Jenkins...)
- Pipeline minimum recommandé :
  1. Lint & format check
  2. Tests unitaires
  3. Tests d'intégration
  4. Build
  5. Deploy staging (auto sur merge)
  6. Deploy production (manuel ou auto selon maturité)
- Temps de build acceptable ? (cible < 5 min pour le feedback loop)
- Stratégie de déploiement ? (blue-green, rolling, canary)
- Rollback : comment revenir en arrière rapidement ?

## 2C.4 — Monitoring & Observabilité

- Logging : où et comment ? (stdout → agrégateur, ou service dédié type Datadog/Grafana)
- Monitoring applicatif : erreurs, performance, uptime
- Alerting : qui est notifié, comment, pour quels seuils ?
- APM (Application Performance Monitoring) : nécessaire dès le début ?
- Métriques business : tracking des KPIs techniques (latence, taux d'erreur, throughput)

## 2C.5 — Sécurité

- HTTPS everywhere (non négociable)
- Gestion des secrets : vault, env vars, secret manager cloud ?
- Politique de mots de passe / auth
- Headers de sécurité (CSP, CORS, HSTS...)
- Scan de dépendances (Dependabot, Snyk...)
- Politique de mise à jour des dépendances
- Backup : fréquence, rétention, test de restauration
- Plan de disaster recovery : RPO et RTO acceptables ?

## 2C.6 — Scalabilité

- Quel est le plan si le trafic explose ? (auto-scaling, load balancer...)
- Goulots d'étranglement prévisibles ? (DB, API externe, file processing...)
- Caching strategy : que cacher, où, combien de temps ?
- Rate limiting : par utilisateur, par IP, par API key ?
- Queue de tâches : nécessaire ? (emails, processing lourd, imports...)

## 2C.7 — Coûts

- Budget infra mensuel cible ?
- Estimation des coûts principaux :
  - Compute (serveurs/functions)
  - Base de données
  - Stockage
  - Bande passante / CDN
  - Services tiers (monitoring, email, etc.)
- À quel point de trafic les coûts deviennent significatifs ?
- Optimisations possibles dès le départ (réservations, tiers gratuits, etc.)

---

## Livrable attendu

Un plan d'infrastructure synthétique :
1. Choix d'hébergement et justification
2. Architecture des environnements
3. Pipeline CI/CD décrit
4. Stack de monitoring/alerting
5. Checklist sécurité
6. Estimation de coûts mensuels (fourchette)
