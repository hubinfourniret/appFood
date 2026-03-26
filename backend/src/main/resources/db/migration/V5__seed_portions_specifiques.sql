-- =============================================================
-- appFood — Seed : Portions specifiques pour aliments courants
-- Migration V005 — Portions liees a des categories d'aliments
-- Complement de V002 (portions generiques)
-- =============================================================

-- Note: ces portions sont generiques (aliment_id = NULL, est_generique = TRUE)
-- car elles correspondent a des categories d'aliments, pas a des aliments
-- specifiques de la base Ciqual. Elles s'affichent comme suggestions pour
-- les aliments des categories correspondantes.

-- Fruits individuels (poids moyen unitaire)
INSERT INTO portions (id, aliment_id, nom, quantite_grammes, est_generique, est_personnalise, user_id) VALUES
('portion-pomme',          NULL, 'Une pomme (~150 g)',          150, TRUE, FALSE, NULL),
('portion-banane',         NULL, 'Une banane (~120 g)',         120, TRUE, FALSE, NULL),
('portion-orange',         NULL, 'Une orange (~200 g)',         200, TRUE, FALSE, NULL),
('portion-poire',          NULL, 'Une poire (~170 g)',          170, TRUE, FALSE, NULL),
('portion-peche',          NULL, 'Une peche (~150 g)',          150, TRUE, FALSE, NULL),
('portion-kiwi',           NULL, 'Un kiwi (~80 g)',              80, TRUE, FALSE, NULL),
('portion-mandarine',      NULL, 'Une mandarine (~70 g)',        70, TRUE, FALSE, NULL),
('portion-abricot',        NULL, 'Un abricot (~45 g)',           45, TRUE, FALSE, NULL),
('portion-prune',          NULL, 'Une prune (~60 g)',            60, TRUE, FALSE, NULL),
('portion-mangue',         NULL, 'Une mangue (~200 g)',         200, TRUE, FALSE, NULL),
('portion-avocat',         NULL, 'Un avocat (~150 g)',          150, TRUE, FALSE, NULL),

-- Legumes unitaires
('portion-tomate',         NULL, 'Une tomate (~120 g)',         120, TRUE, FALSE, NULL),
('portion-carotte',        NULL, 'Une carotte (~80 g)',          80, TRUE, FALSE, NULL),
('portion-courgette',      NULL, 'Une courgette (~200 g)',      200, TRUE, FALSE, NULL),
('portion-poivron',        NULL, 'Un poivron (~170 g)',         170, TRUE, FALSE, NULL),
('portion-oignon',         NULL, 'Un oignon (~100 g)',          100, TRUE, FALSE, NULL),
('portion-pomme-de-terre', NULL, 'Une pomme de terre (~150 g)', 150, TRUE, FALSE, NULL),
('portion-patate-douce',   NULL, 'Une patate douce (~200 g)',   200, TRUE, FALSE, NULL),

-- Proteines animales
('portion-oeuf',           NULL, 'Un oeuf (~60 g)',              60, TRUE, FALSE, NULL),
('portion-yaourt',         NULL, 'Un yaourt (~125 g)',          125, TRUE, FALSE, NULL),
('portion-fromage',        NULL, 'Une portion de fromage (30 g)', 30, TRUE, FALSE, NULL),

-- Pain et cereales
('portion-tranche-pain',   NULL, 'Une tranche de pain (~30 g)',  30, TRUE, FALSE, NULL),
('portion-pain-complet',   NULL, 'Une tranche de pain complet (~40 g)', 40, TRUE, FALSE, NULL),
('portion-baguette-morceau', NULL, 'Un morceau de baguette (~60 g)', 60, TRUE, FALSE, NULL),
('portion-riz-cuit',       NULL, 'Une portion de riz cuit (~200 g)', 200, TRUE, FALSE, NULL),
('portion-pates-cuites',   NULL, 'Une portion de pates cuites (~200 g)', 200, TRUE, FALSE, NULL),

-- Legumineuses
('portion-lentilles-cuites', NULL, 'Une portion de lentilles cuites (~200 g)', 200, TRUE, FALSE, NULL),
('portion-pois-chiches',   NULL, 'Une portion de pois chiches (~200 g)', 200, TRUE, FALSE, NULL),
('portion-haricots',       NULL, 'Une portion de haricots cuits (~200 g)', 200, TRUE, FALSE, NULL),
('portion-tofu',           NULL, 'Un bloc de tofu (~100 g)',    100, TRUE, FALSE, NULL),

-- Oleagineux et graines
('portion-amandes',        NULL, 'Une poignee d''amandes (~20 g)', 20, TRUE, FALSE, NULL),
('portion-noix',           NULL, 'Une poignee de noix (~20 g)',    20, TRUE, FALSE, NULL),
('portion-graines-tournesol', NULL, 'Une cuillere a soupe de graines (~10 g)', 10, TRUE, FALSE, NULL),
('portion-graines-lin',    NULL, 'Une cuillere a soupe de graines de lin (~10 g)', 10, TRUE, FALSE, NULL),
('portion-beurre-cacahuete', NULL, 'Une cuillere a soupe de beurre de cacahuete (~15 g)', 15, TRUE, FALSE, NULL),

-- Huiles
('portion-huile-cas',      NULL, 'Une cuillere a soupe d''huile (~10 g)', 10, TRUE, FALSE, NULL),
('portion-huile-cac',      NULL, 'Une cuillere a cafe d''huile (~5 g)',    5, TRUE, FALSE, NULL),

-- Boissons vegetales
('portion-lait-vegetal',   NULL, 'Un verre de lait vegetal (~200 ml)', 200, TRUE, FALSE, NULL),

-- Produits laitiers
('portion-lait-verre',     NULL, 'Un verre de lait (~200 ml)',  200, TRUE, FALSE, NULL),
('portion-creme-cas',      NULL, 'Une cuillere a soupe de creme (~15 g)', 15, TRUE, FALSE, NULL);
