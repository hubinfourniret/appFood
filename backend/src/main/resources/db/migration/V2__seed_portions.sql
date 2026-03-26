-- =============================================================
-- appFood — Seed : Portions standard generiques
-- Migration V002 — Portions reutilisables par tous les aliments
-- =============================================================

-- Portions generiques (aliment_id = NULL, est_generique = TRUE)
-- Utilisees comme choix par defaut quand aucune portion specifique n'est definie

INSERT INTO portions (id, aliment_id, nom, quantite_grammes, est_generique, est_personnalise, user_id) VALUES
-- Contenants
('portion-verre',       NULL, 'Un verre (200 ml)',       200, TRUE, FALSE, NULL),
('portion-bol',         NULL, 'Un bol (300 ml)',          300, TRUE, FALSE, NULL),
('portion-tasse',       NULL, 'Une tasse (250 ml)',       250, TRUE, FALSE, NULL),
('portion-mug',         NULL, 'Un mug (350 ml)',          350, TRUE, FALSE, NULL),

-- Cuilleres
('portion-cas',         NULL, 'Une cuillere a soupe',      15, TRUE, FALSE, NULL),
('portion-cac',         NULL, 'Une cuillere a cafe',        5, TRUE, FALSE, NULL),

-- Assiettes
('portion-assiette',    NULL, 'Une assiette (250 g)',     250, TRUE, FALSE, NULL),
('portion-petite-assiette', NULL, 'Une petite assiette (150 g)', 150, TRUE, FALSE, NULL),

-- Poignees / morceaux
('portion-poignee',     NULL, 'Une poignee (30 g)',        30, TRUE, FALSE, NULL),
('portion-tranche',     NULL, 'Une tranche (30 g)',        30, TRUE, FALSE, NULL),
('portion-morceau',     NULL, 'Un morceau (50 g)',         50, TRUE, FALSE, NULL),

-- Fruits standard
('portion-fruit-petit', NULL, 'Un petit fruit (80 g)',     80, TRUE, FALSE, NULL),
('portion-fruit-moyen', NULL, 'Un fruit moyen (150 g)',   150, TRUE, FALSE, NULL),
('portion-fruit-gros',  NULL, 'Un gros fruit (200 g)',    200, TRUE, FALSE, NULL),

-- Boissons
('portion-bouteille',   NULL, 'Une bouteille (500 ml)',   500, TRUE, FALSE, NULL),
('portion-canette',     NULL, 'Une canette (330 ml)',     330, TRUE, FALSE, NULL),

-- Grammages courants
('portion-100g',        NULL, '100 grammes',              100, TRUE, FALSE, NULL),
('portion-50g',         NULL, '50 grammes',                50, TRUE, FALSE, NULL),
('portion-200g',        NULL, '200 grammes',              200, TRUE, FALSE, NULL);
