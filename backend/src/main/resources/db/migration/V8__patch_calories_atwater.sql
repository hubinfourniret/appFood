-- NOOP : migration retiree, l'utilisateur prefere les vraies valeurs Ciqual via reimport.
-- Conserver le fichier pour la coherence de l'historique Flyway (evite le "Detected applied
-- migration not resolved locally" si V8 a deja ete appliquee sur un environnement).
-- Le reimport Ciqual avec les patterns de colonne elargis (cf CiqualImporter.NUTRIMENT_PATTERNS)
-- doit etre declenche via FORCE_REIMPORT_ALL=true au prochain deploiement.
SELECT 1;
