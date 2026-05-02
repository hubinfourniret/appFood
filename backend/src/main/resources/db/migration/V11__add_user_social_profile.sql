-- =============================================================
-- TACHE-600 : Profil social
-- Ajout des colonnes handle, bio, date_naissance, social_visibility
-- handle / date_naissance restent nullables : la navigation force
-- l'onboarding social si manquants (cf. AppNavigation).
-- =============================================================

ALTER TABLE users ADD COLUMN handle VARCHAR(30);
ALTER TABLE users ADD COLUMN bio VARCHAR(280);
ALTER TABLE users ADD COLUMN date_naissance DATE;
ALTER TABLE users ADD COLUMN social_visibility VARCHAR(10) NOT NULL DEFAULT 'PRIVATE';

-- Unicite case-insensitive sur handle
CREATE UNIQUE INDEX idx_users_handle_lower ON users (LOWER(handle));
