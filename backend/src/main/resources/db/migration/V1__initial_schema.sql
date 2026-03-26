-- =============================================================
-- appFood — Schema initial PostgreSQL
-- Migration V001 — Tables, index, contraintes
-- =============================================================

-- Utilisateurs
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    nom VARCHAR(100),
    prenom VARCHAR(100),
    role VARCHAR(10) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Profil utilisateur (1:1 avec users)
CREATE TABLE user_profiles (
    user_id VARCHAR(36) PRIMARY KEY REFERENCES users(id),
    sexe VARCHAR(10) NOT NULL,
    age INTEGER NOT NULL,
    poids_kg DOUBLE PRECISION NOT NULL,
    taille_cm INTEGER NOT NULL,
    regime_alimentaire VARCHAR(20) NOT NULL,
    niveau_activite VARCHAR(20) NOT NULL,
    onboarding_complete BOOLEAN NOT NULL DEFAULT FALSE,
    objectif_poids VARCHAR(20),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Preferences utilisateur (1:1 avec users)
CREATE TABLE user_preferences (
    user_id VARCHAR(36) PRIMARY KEY REFERENCES users(id),
    aliments_exclus TEXT NOT NULL DEFAULT '[]',
    allergies TEXT NOT NULL DEFAULT '[]',
    aliments_favoris TEXT NOT NULL DEFAULT '[]',
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Aliments (Ciqual, Open Food Facts, manuels)
CREATE TABLE aliments (
    id VARCHAR(36) PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    marque VARCHAR(255),
    source VARCHAR(20) NOT NULL,
    source_id VARCHAR(100),
    code_barres VARCHAR(50),
    categorie VARCHAR(100) NOT NULL,
    regimes_compatibles TEXT NOT NULL,
    calories DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    proteines DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    glucides DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    lipides DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    fibres DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    sel DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    sucres DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    fer DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    calcium DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    zinc DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    magnesium DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    vitamine_b12 DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    vitamine_d DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    vitamine_c DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    omega_3 DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    omega_6 DOUBLE PRECISION NOT NULL DEFAULT 0.0
);

CREATE INDEX idx_aliments_code_barres ON aliments(code_barres);

-- Portions standard
CREATE TABLE portions (
    id VARCHAR(36) PRIMARY KEY,
    aliment_id VARCHAR(36) REFERENCES aliments(id),
    nom VARCHAR(100) NOT NULL,
    quantite_grammes DOUBLE PRECISION NOT NULL,
    est_generique BOOLEAN NOT NULL DEFAULT FALSE,
    est_personnalise BOOLEAN NOT NULL DEFAULT FALSE,
    user_id VARCHAR(36) REFERENCES users(id)
);

-- Recettes
CREATE TABLE recettes (
    id VARCHAR(36) PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    temps_preparation_min INTEGER NOT NULL,
    temps_cuisson_min INTEGER NOT NULL,
    nb_portions INTEGER NOT NULL,
    regimes_compatibles TEXT NOT NULL,
    source VARCHAR(20) NOT NULL,
    type_repas TEXT NOT NULL,
    etapes TEXT NOT NULL,
    calories DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    proteines DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    glucides DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    lipides DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    fibres DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    sel DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    sucres DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    fer DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    calcium DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    zinc DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    magnesium DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    vitamine_b12 DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    vitamine_d DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    vitamine_c DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    omega_3 DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    omega_6 DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    image_url VARCHAR(500),
    publie BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Ingredients des recettes
CREATE TABLE ingredients (
    id VARCHAR(36) PRIMARY KEY,
    recette_id VARCHAR(36) NOT NULL REFERENCES recettes(id) ON DELETE CASCADE,
    aliment_id VARCHAR(36) NOT NULL REFERENCES aliments(id),
    aliment_nom VARCHAR(255) NOT NULL,
    quantite_grammes DOUBLE PRECISION NOT NULL
);

-- Journal alimentaire
CREATE TABLE journal_entries (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id),
    date DATE NOT NULL,
    meal_type VARCHAR(20) NOT NULL,
    aliment_id VARCHAR(36) REFERENCES aliments(id),
    recette_id VARCHAR(36) REFERENCES recettes(id),
    nom VARCHAR(255) NOT NULL,
    quantite_grammes DOUBLE PRECISION NOT NULL,
    nb_portions DOUBLE PRECISION,
    calories DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    proteines DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    glucides DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    lipides DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    fibres DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    sel DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    sucres DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    fer DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    calcium DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    zinc DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    magnesium DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    vitamine_b12 DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    vitamine_d DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    vitamine_c DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    omega_3 DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    omega_6 DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_journal_user_id ON journal_entries(user_id);
CREATE INDEX idx_journal_date ON journal_entries(date);

-- Quotas journaliers (cle composite userId + nutriment)
CREATE TABLE quotas (
    user_id VARCHAR(36) NOT NULL REFERENCES users(id),
    nutriment VARCHAR(20) NOT NULL,
    valeur_cible DOUBLE PRECISION NOT NULL,
    est_personnalise BOOLEAN NOT NULL DEFAULT FALSE,
    valeur_calculee DOUBLE PRECISION NOT NULL,
    unite VARCHAR(10) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (user_id, nutriment)
);

-- Historique poids
CREATE TABLE poids_history (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id),
    date DATE NOT NULL,
    poids_kg DOUBLE PRECISION NOT NULL,
    est_reference BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_poids_user_id ON poids_history(user_id);

-- Hydratation journaliere
CREATE TABLE hydratation (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id),
    date DATE NOT NULL,
    quantite_ml INTEGER NOT NULL,
    objectif_ml INTEGER NOT NULL,
    est_objectif_personnalise BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_hydratation_user_id ON hydratation(user_id);
CREATE UNIQUE INDEX idx_user_date_hydra ON hydratation(user_id, date);

-- Entrees d'hydratation (detail des prises)
CREATE TABLE hydratation_entries (
    id VARCHAR(36) PRIMARY KEY,
    hydratation_id VARCHAR(36) NOT NULL REFERENCES hydratation(id) ON DELETE CASCADE,
    heure TIMESTAMP WITH TIME ZONE NOT NULL,
    quantite_ml INTEGER NOT NULL
);

-- Tokens FCM pour les notifications push
CREATE TABLE fcm_tokens (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id),
    token TEXT NOT NULL,
    platform VARCHAR(10) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_fcm_user_id ON fcm_tokens(user_id);
CREATE UNIQUE INDEX idx_user_token ON fcm_tokens(user_id, token);

-- Notifications
CREATE TABLE notifications (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id),
    type VARCHAR(20) NOT NULL,
    titre VARCHAR(255) NOT NULL,
    contenu TEXT NOT NULL,
    date_envoi TIMESTAMP WITH TIME ZONE NOT NULL,
    lue BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);

-- Consentements RGPD
CREATE TABLE consents (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id),
    type VARCHAR(30) NOT NULL,
    accepte BOOLEAN NOT NULL,
    date_consentement TIMESTAMP WITH TIME ZONE NOT NULL,
    version_politique VARCHAR(10) NOT NULL
);

CREATE INDEX idx_consents_user_id ON consents(user_id);
CREATE UNIQUE INDEX idx_user_consent_type ON consents(user_id, type);

-- FAQ
CREATE TABLE faq (
    id VARCHAR(36) PRIMARY KEY,
    theme VARCHAR(100) NOT NULL,
    question TEXT NOT NULL,
    reponse TEXT NOT NULL,
    ordre INTEGER NOT NULL,
    actif BOOLEAN NOT NULL DEFAULT TRUE
);
