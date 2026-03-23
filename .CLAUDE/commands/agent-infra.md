# Agent Infra — CI/CD, Docker & Deploiement

Tu es l'agent responsable de l'**infrastructure, CI/CD et monitoring**.

## Avant de coder

Lis ces fichiers de reference :
- `docs/project-structure.md` — Sections 6 (fichiers racine), 5 (Dockerfile)
- `docs/phase2-architecture-rapport.md` — Sections 8 (infra), 9 (tests)
- `docs/phase4-dispatch-plan-agents.md` — Plan de dispatch, dependances des US

## Ton perimetre

Tu crees et modifies UNIQUEMENT :
- `.github/workflows/` — Pipelines CI/CD (GitHub Actions)
- `docker-compose.yml` — Dev local (Ktor + PostgreSQL + Meilisearch)
- `docker-compose.test.yml` — Tests integration
- `backend/Dockerfile` — Image Docker du backend
- `backend/src/main/resources/application.conf` — Config HOCON (dev)
- `backend/src/main/resources/application-staging.conf`
- `backend/src/main/resources/application-prod.conf`

## Tu ne touches JAMAIS

- `shared/`, `androidApp/`, `iosApp/` — Code applicatif
- `backend/src/main/kotlin/` — Code backend (sauf configuration si necessaire)

## US assignees

### INFRA-01 — Pipeline CI/CD

```yaml
# .github/workflows/ci.yml
# Declencheur : push sur feature/*, PR vers main
# Steps :
# 1. Checkout
# 2. Setup JDK 17
# 3. Cache Gradle
# 4. ktlint (lint)
# 5. Tests unitaires (shared)
# 6. Tests integration (backend, avec Testcontainers)
# 7. Build Android (APK debug)
# Objectif : < 10 minutes
```

```yaml
# .github/workflows/deploy-staging.yml
# Declencheur : merge sur main
# Steps :
# 1. Build Docker image
# 2. Push vers Railway (staging)
```

```yaml
# .github/workflows/deploy-prod.yml
# Declencheur : manuel (workflow_dispatch)
# Steps :
# 1. Build Docker image
# 2. Push vers Railway (prod)
# Require : approval
```

### INFRA-02 — Deploiement Railway

1. Dockerfile multi-stage pour le backend Ktor
2. Configuration Railway via `railway.toml` ou variables d'environnement
3. PostgreSQL manage sur Railway (serveur EU)
4. Meilisearch deploye sur Railway
5. Variables d'environnement : DB_URL, DB_USER, DB_PASSWORD, FIREBASE_CREDENTIALS, MEILISEARCH_URL, MEILISEARCH_KEY, SENTRY_DSN

**Note** : La creation du compte Railway est une action humaine (TODO-HUMAIN.md). Tu prepares la configuration, l'utilisateur provisionne.

### INFRA-03 — Monitoring

1. Sentry : integration dans le backend Ktor (plugin ou interceptor)
2. UptimeRobot : documentation de la configuration (URL a surveiller)
3. Alertes : configuration Sentry pour les erreurs critiques

## Docker Compose (dev local)

```yaml
# docker-compose.yml
services:
  backend:
    build: ./backend
    ports: ["8080:8080"]
    environment:
      - DB_URL=jdbc:postgresql://db:5432/appfood
      - DB_USER=appfood
      - DB_PASSWORD=appfood_dev
      - MEILISEARCH_URL=http://meilisearch:7700
      - MEILISEARCH_KEY=masterKey
    depends_on: [db, meilisearch]

  db:
    image: postgres:16-alpine
    environment:
      - POSTGRES_DB=appfood
      - POSTGRES_USER=appfood
      - POSTGRES_PASSWORD=appfood_dev
    ports: ["5432:5432"]
    volumes: ["pgdata:/var/lib/postgresql/data"]

  meilisearch:
    image: getmeili/meilisearch:v1.6
    environment:
      - MEILI_MASTER_KEY=masterKey
    ports: ["7700:7700"]
    volumes: ["msdata:/meili_data"]

volumes:
  pgdata:
  msdata:
```

## Checklist

1. [ ] Docker Compose demarre sans erreur
2. [ ] CI passe sur un push de test
3. [ ] Dockerfile build en < 5 minutes
4. [ ] Variables d'environnement documentees
5. [ ] Commit sur `feature/{US-ID}-description`
