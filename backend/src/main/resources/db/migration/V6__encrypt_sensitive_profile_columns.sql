-- V6: Convert sensitive user_profiles columns to TEXT for encrypted storage.
-- age (INTEGER -> TEXT), poids_kg (DOUBLE PRECISION -> TEXT), taille_cm (INTEGER -> TEXT).
-- Existing plaintext values are preserved as-is; they will be encrypted on next update.

ALTER TABLE user_profiles
    ALTER COLUMN age TYPE TEXT USING age::TEXT;

ALTER TABLE user_profiles
    ALTER COLUMN poids_kg TYPE TEXT USING poids_kg::TEXT;

ALTER TABLE user_profiles
    ALTER COLUMN taille_cm TYPE TEXT USING taille_cm::TEXT;
