# Epic Communauté — Plateforme sociale appFood

> Date de création : 2026-05-02
> Statut : Reference active (V1 en cours)
> Document parent : `docs/workflow-claude-code.md`

---

## 1. Vision

appFood reste **avant tout un tracker nutritionnel** pour vegan/végé sportifs.
On ajoute un **onglet "Social"** en bottom nav qui permet de partager des recettes et de découvrir comment d'autres utilisateurs atteignent leurs quotas.

**Principe directeur** : le social tourne autour des **recettes** (et plus tard de posts liés à une recette). Pas de partage du poids, pas de partage brut des quotas, pas de défis. Le but est l'entraide via le partage de recettes éprouvées, pas la compétition.

## 2. Choix structurants validés (2026-05-02)

| # | Choix | Décision |
|---|-------|----------|
| 1 | Modèle relationnel | Follow asymétrique (Strava-like) |
| 2 | Identité | Handle unique `@xxx` + nom affiché optionnel |
| 3 | Visibilité | 3 niveaux : `PRIVATE` / `FRIENDS` / `PUBLIC`, **privé par défaut** |
| 4 | Granularité visibilité | Profil entier (recettes perso + journal + quotas + badges). Poids JAMAIS partagé. |
| 5 | Interactions | Like emoji uniquement (pas de commentaire) |
| 6 | Pas de DM | Le but est de partager des recettes, pas un réseau social |
| 7 | Modération | Posteriori sur signalement (recette pas un sujet sensible) |
| 8 | Modération photos | Pré-publication auto via AWS Rekognition |
| 9 | Stockage médias | Cloudflare R2 (signed upload, accès direct client → R2) |
| 10 | Âge minimum | **16 ans strict** pour la partie communauté (RGPD-safe, pas de consentement parental à gérer) |
| 11 | Forking | "Ajouter à mes recettes" = copie liée avec attribution `@auteur` |
| 12 | Co-auteurs | Champ structuré sur recette (liste d'users) |
| 13 | Remix | Lien parent `source_recette_id` quand on modifie une recette importée |
| 14 | Posts | Pas de post libre — uniquement liés à une recette (V2) |

## 3. Modèle de données

### V1 — migrations SQL

```sql
-- V11 : Profil social
ALTER TABLE users ADD COLUMN handle VARCHAR(30) UNIQUE;
ALTER TABLE users ADD COLUMN bio VARCHAR(280);
ALTER TABLE users ADD COLUMN date_naissance DATE;
ALTER TABLE users ADD COLUMN social_visibility VARCHAR(10) NOT NULL DEFAULT 'PRIVATE';
-- valeurs : PRIVATE | FRIENDS | PUBLIC
CREATE UNIQUE INDEX idx_users_handle ON users(LOWER(handle));

-- V12 : Follows asymétriques
CREATE TABLE social_follows (
    follower_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    following_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (follower_id, following_id),
    CHECK (follower_id <> following_id)
);
CREATE INDEX idx_follows_following ON social_follows(following_id);

-- V13 : Publication des recettes perso
ALTER TABLE recettes ADD COLUMN visibility VARCHAR(10) NOT NULL DEFAULT 'PRIVATE';
ALTER TABLE recettes ADD COLUMN published_at TIMESTAMPTZ;
-- visibility ignorée pour recettes système (user_id NULL)

-- V14 : Forking (recette importée depuis un autre user)
ALTER TABLE recettes ADD COLUMN source_recette_id UUID REFERENCES recettes(id) ON DELETE SET NULL;
CREATE INDEX idx_recettes_source ON recettes(source_recette_id);

-- V15 : Likes emoji
CREATE TABLE recette_likes (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    recette_id UUID NOT NULL REFERENCES recettes(id) ON DELETE CASCADE,
    emoji VARCHAR(10) NOT NULL DEFAULT '❤️',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, recette_id)
);
CREATE INDEX idx_likes_recette ON recette_likes(recette_id);

-- V16 : Signalements
CREATE TABLE user_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id UUID REFERENCES users(id) ON DELETE SET NULL,
    target_type VARCHAR(20) NOT NULL, -- 'RECETTE' | 'USER'
    target_id UUID NOT NULL,
    reason VARCHAR(50) NOT NULL, -- 'SPAM' | 'INAPPROPRIATE' | 'COPYRIGHT' | 'OTHER'
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    reviewed_at TIMESTAMPTZ,
    reviewer_id UUID REFERENCES users(id)
);
CREATE INDEX idx_reports_status ON user_reports(status);
```

### V2 — migrations additionnelles

```sql
-- V17 : Médias attachés aux recettes (photos + vidéo)
CREATE TABLE recette_medias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recette_id UUID NOT NULL REFERENCES recettes(id) ON DELETE CASCADE,
    type VARCHAR(10) NOT NULL, -- 'PHOTO' | 'VIDEO'
    r2_key TEXT NOT NULL,
    position INT NOT NULL DEFAULT 0,
    moderation_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    moderation_score JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_recette_medias_recette ON recette_medias(recette_id, position);

-- V18 : Co-auteurs
CREATE TABLE recette_co_authors (
    recette_id UUID NOT NULL REFERENCES recettes(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (recette_id, user_id)
);

-- V19 : Auto-subscribe aux recettes d'un user
ALTER TABLE social_follows ADD COLUMN auto_save_recettes BOOLEAN NOT NULL DEFAULT FALSE;

-- V20 : Posts type "ma semaine" (lié à une recette obligatoire)
CREATE TABLE social_posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    recette_id UUID NOT NULL REFERENCES recettes(id) ON DELETE CASCADE,
    visibility VARCHAR(10) NOT NULL,
    description TEXT,
    show_quotas BOOLEAN NOT NULL DEFAULT FALSE,
    show_streak BOOLEAN NOT NULL DEFAULT FALSE,
    period_type VARCHAR(10), -- NULL | 'DAY' | 'WEEK' (pour les posts récap)
    period_start DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_posts_user_created ON social_posts(user_id, created_at DESC);

-- V21 : Blocage utilisateur
CREATE TABLE user_blocks (
    blocker_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    blocked_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (blocker_id, blocked_id)
);

-- V22 : Push tokens (FCM)
CREATE TABLE push_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    fcm_token TEXT NOT NULL UNIQUE,
    platform VARCHAR(10) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_used_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

## 4. Endpoints API V1

Préfixe : `/api/v1/social/...` sauf indication.

| Méthode | Path | Auth | Description |
|---------|------|------|-------------|
| GET | `/api/v1/users/handle-available?handle=xxx` | Public | Vérifie disponibilité d'un handle |
| PUT | `/api/v1/users/me/social` | User | Update handle/bio/visibility/date_naissance |
| GET | `/social/users/search?q=xxx&limit=20` | User 16+ | Recherche par handle ou nom |
| GET | `/social/users/{handle}` | User 16+ | Profil public (selon visibilité) |
| POST | `/social/follows/{userId}` | User 16+ | Suivre un user |
| DELETE | `/social/follows/{userId}` | User 16+ | Ne plus suivre |
| GET | `/social/follows/me/following` | User 16+ | Liste de mes abonnements |
| GET | `/social/follows/me/followers` | User 16+ | Liste de mes abonnés |
| PUT | `/api/v1/recettes/{id}/publish` | Owner | `{visibility: PRIVATE\|FRIENDS\|PUBLIC}` |
| GET | `/social/feed?cursor=xxx&limit=20` | User 16+ | Feed des recettes publiées par les suivis |
| POST | `/social/recettes/{id}/like` | User 16+ | `{emoji: "❤️"}` |
| DELETE | `/social/recettes/{id}/like` | User 16+ | Retirer son like |
| POST | `/social/recettes/{id}/save` | User 16+ | Forker une recette dans mes recettes |
| POST | `/social/reports` | User 16+ | `{targetType, targetId, reason, description}` |
| GET | `/social/admin/reports?status=PENDING` | Admin | Liste signalements |
| PUT | `/social/admin/reports/{id}` | Admin | `{action: DISMISS\|REMOVE_CONTENT}` |

**Règles d'autorisation transverses** :
- Tout endpoint `/social/...` exige `date_naissance` renseignée et âge >= 16 (sinon 403 `AGE_RESTRICTED`)
- La visibilité d'une recette filtre les retours :
  - `PRIVATE` → seul le owner
  - `FRIENDS` → owner + followers (le owner doit suivre le viewer en plus pour qualifier d'ami mutuel ; on choisit suivi mutuel comme définition de "ami")
  - `PUBLIC` → tout user 16+
- Un user bloqué (V2) ne voit aucun contenu du bloqueur

## 5. Écrans mobiles V1

| Écran | Route | Description |
|-------|-------|-------------|
| `SocialOnboarding` | `Screen.SocialOnboarding` | Demandé une seule fois : handle + date naissance + bio + choix visibilité |
| `SocialTab` | `Screen.Social` | Onglet bottom nav : feed + accès recherche + accès profil |
| `SocialSearch` | `Screen.SocialSearch` | Recherche utilisateurs |
| `UserProfile` | `Screen.UserProfile(handle)` | Profil public d'un user (recettes publiées, follow/unfollow) |
| `SocialSettings` | `Screen.SocialSettings` | Modifier handle/bio/visibilité depuis Settings |
| `PublishRecetteDialog` | dialog | Choix visibilité au moment de publier |

Bouton "Social" dans la bottom nav apparaît **uniquement** si l'utilisateur a >= 16 ans (vérifié à partir de `date_naissance`).

## 6. Découpage en tâches

### V1 — MVP (8 tâches)

| ID | Titre | Priorité | Dépend de | Critique |
|----|-------|----------|-----------|----------|
| TACHE-600 | Profil social (handle + date naissance + visibilité + age gate 16+) | Haute | - | ⚠️ RGPD |
| TACHE-601 | Onglet Social + recherche utilisateurs + fiche profil public | Haute | TACHE-600 | - |
| TACHE-602 | Follow asymétrique | Haute | TACHE-601 | - |
| TACHE-603 | Publier une recette perso (PRIVATE/FRIENDS/PUBLIC) | Haute | TACHE-600 | - |
| TACHE-604 | Feed des recettes publiées (utilisateurs suivis) | Haute | TACHE-602, TACHE-603 | - |
| TACHE-605 | Like emoji | Moyenne | TACHE-604 | - |
| TACHE-606 | Forker une recette d'un autre user (avec attribution `source_recette_id`) | Haute | TACHE-604 | - |
| TACHE-607 | Signalement + dashboard admin minimal | Haute | TACHE-604 | ⚠️ Modération |

### V2 — Médias et partages riches (8 tâches)

| ID | Titre | Priorité |
|----|-------|----------|
| TACHE-610 | Photos sur recettes (R2 + upload signed URL + Rekognition pré-publi + galerie) | Haute |
| TACHE-611 | Vidéo sur recette (R2 + modération frame-sampling) | Moyenne |
| TACHE-612 | Posts "ma journée/semaine" lié à recette + carte auto-générée toggles | Haute |
| TACHE-613 | Streaks et badges partagés sur le profil | Moyenne |
| TACHE-614 | Auto-save : abonnement aux nouvelles recettes d'un user | Moyenne |
| TACHE-615 | Notifications push (like, follow) | Moyenne |
| TACHE-616 | Co-auteurs + remix + mentions @ | Haute |
| TACHE-617 | Bloquer un utilisateur | Haute |

### V3 — Découverte et viralité (3 tâches)

| ID | Titre | Priorité |
|----|-------|----------|
| TACHE-620 | Découverte par régime / tags / popularité | Moyenne |
| TACHE-621 | Export PNG "story Insta" partageable hors-app | Moyenne |
| TACHE-622 | Recommandation utilisateurs à suivre | Basse |

## 7. Risques et décisions à reprendre plus tard

| Sujet | Décision actuelle | À reprendre quand |
|-------|-------------------|-------------------|
| Modèle économique | Hors scope, on freeze | Après V1 livré et utilisé |
| Photos multi vs unique | Multi (V2) | TACHE-610 |
| Vidéo : taille max, durée max | À fixer dans TACHE-611 | TACHE-611 |
| Coût Rekognition | À budgéter dans TACHE-610 | TACHE-610 |
| Comptes existants | Ignorés (DB resetable en dev) | N/A |
| Fonction "amis" vs simple follow mutuel | Suivi mutuel = ami pour scope V1 | Si demande utilisateur |

## 8. Actions humaines requises (à ajouter à TODO-HUMAIN.md)

- Créer un bucket Cloudflare R2 + clés API (avant TACHE-610)
- Créer compte AWS + activer Rekognition + clés (avant TACHE-610)
- Mettre à jour la politique de confidentialité (LEGAL-01) avec les traitements sociaux (avant ouverture publique)
- Mettre à jour les CGU (LEGAL-02) avec règles de modération (avant ouverture publique)
- Désigner un compte admin pour le dashboard de modération (TACHE-607)
