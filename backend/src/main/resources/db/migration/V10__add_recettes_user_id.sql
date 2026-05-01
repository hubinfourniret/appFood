-- TACHE-516 : recettes personnelles. user_id null pour les recettes systeme/admin.
ALTER TABLE recettes
    ADD COLUMN IF NOT EXISTS user_id VARCHAR(36) NULL REFERENCES users(id) ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_recettes_user_id ON recettes(user_id) WHERE user_id IS NOT NULL;
