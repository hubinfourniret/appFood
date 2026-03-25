-- =============================================================
-- appFood — Seed : References nutritionnelles AJR/ANC (ANSES)
-- Migration V004 — Valeurs de reference par sexe, tranche d'age
-- Sources : ANSES RNP/AS, OMS, ADA, Societe francaise de nutrition
-- =============================================================

-- Table des references nutritionnelles
CREATE TABLE IF NOT EXISTS ajr_references (
    id VARCHAR(36) PRIMARY KEY,
    sexe VARCHAR(10) NOT NULL,              -- HOMME, FEMME
    tranche_age VARCHAR(20) NOT NULL,       -- ADO (14-17), ADULTE (18-64), SENIOR (65+)
    age_min INTEGER NOT NULL,
    age_max INTEGER NOT NULL,
    nutriment VARCHAR(20) NOT NULL,         -- NutrimentType enum name
    valeur_base DOUBLE PRECISION NOT NULL,  -- Valeur de reference standard (regime omnivore)
    unite VARCHAR(10) NOT NULL,
    source VARCHAR(50) NOT NULL DEFAULT 'ANSES',
    UNIQUE (sexe, tranche_age, nutriment)
);

-- Table des coefficients d'ajustement par regime alimentaire
CREATE TABLE IF NOT EXISTS ajr_regime_coefficients (
    id VARCHAR(36) PRIMARY KEY,
    regime VARCHAR(20) NOT NULL,            -- VEGAN, VEGETARIEN, FLEXITARIEN, OMNIVORE
    nutriment VARCHAR(20) NOT NULL,
    coefficient DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    justification TEXT,
    UNIQUE (regime, nutriment)
);

-- ============================================================
-- HOMME ADULTE (18-64 ans) — Valeurs de reference ANSES
-- ============================================================

INSERT INTO ajr_references (id, sexe, tranche_age, age_min, age_max, nutriment, valeur_base, unite, source) VALUES
-- Macronutriments (calcules dynamiquement via DET, ces valeurs sont des reperes pour 2500 kcal)
('ajr-ha-cal',  'HOMME', 'ADULTE', 18, 64, 'CALORIES',     2500,   'kcal', 'ANSES'),
('ajr-ha-prot', 'HOMME', 'ADULTE', 18, 64, 'PROTEINES',    93.75,  'g',    'ANSES'),
('ajr-ha-glu',  'HOMME', 'ADULTE', 18, 64, 'GLUCIDES',     312.5,  'g',    'ANSES'),
('ajr-ha-lip',  'HOMME', 'ADULTE', 18, 64, 'LIPIDES',      97.2,   'g',    'ANSES'),
('ajr-ha-fib',  'HOMME', 'ADULTE', 18, 64, 'FIBRES',       30,     'g',    'ANSES'),
('ajr-ha-sel',  'HOMME', 'ADULTE', 18, 64, 'SEL',          5,      'g',    'OMS'),
('ajr-ha-suc',  'HOMME', 'ADULTE', 18, 64, 'SUCRES',       62.5,   'g',    'ANSES'),
-- Micronutriments
('ajr-ha-fer',  'HOMME', 'ADULTE', 18, 64, 'FER',          11,     'mg',   'ANSES RNP'),
('ajr-ha-ca',   'HOMME', 'ADULTE', 18, 64, 'CALCIUM',      950,    'mg',   'ANSES AS'),
('ajr-ha-zn',   'HOMME', 'ADULTE', 18, 64, 'ZINC',         11,     'mg',   'ANSES RNP'),
('ajr-ha-mg',   'HOMME', 'ADULTE', 18, 64, 'MAGNESIUM',    380,    'mg',   'ANSES AS'),
('ajr-ha-b12',  'HOMME', 'ADULTE', 18, 64, 'VITAMINE_B12', 4,      'µg',   'ANSES AS'),
('ajr-ha-vd',   'HOMME', 'ADULTE', 18, 64, 'VITAMINE_D',   15,     'µg',   'ANSES AS'),
('ajr-ha-vc',   'HOMME', 'ADULTE', 18, 64, 'VITAMINE_C',   110,    'mg',   'ANSES RNP'),
('ajr-ha-o3',   'HOMME', 'ADULTE', 18, 64, 'OMEGA_3',      2.5,    'g',    'ANSES AS'),
('ajr-ha-o6',   'HOMME', 'ADULTE', 18, 64, 'OMEGA_6',      10,     'g',    'ANSES AS');

-- ============================================================
-- FEMME ADULTE (18-64 ans)
-- ============================================================

INSERT INTO ajr_references (id, sexe, tranche_age, age_min, age_max, nutriment, valeur_base, unite, source) VALUES
('ajr-fa-cal',  'FEMME', 'ADULTE', 18, 64, 'CALORIES',     2000,   'kcal', 'ANSES'),
('ajr-fa-prot', 'FEMME', 'ADULTE', 18, 64, 'PROTEINES',    75,     'g',    'ANSES'),
('ajr-fa-glu',  'FEMME', 'ADULTE', 18, 64, 'GLUCIDES',     250,    'g',    'ANSES'),
('ajr-fa-lip',  'FEMME', 'ADULTE', 18, 64, 'LIPIDES',      77.8,   'g',    'ANSES'),
('ajr-fa-fib',  'FEMME', 'ADULTE', 18, 64, 'FIBRES',       30,     'g',    'ANSES'),
('ajr-fa-sel',  'FEMME', 'ADULTE', 18, 64, 'SEL',          5,      'g',    'OMS'),
('ajr-fa-suc',  'FEMME', 'ADULTE', 18, 64, 'SUCRES',       50,     'g',    'ANSES'),
('ajr-fa-fer',  'FEMME', 'ADULTE', 18, 64, 'FER',          16,     'mg',   'ANSES RNP'),
('ajr-fa-ca',   'FEMME', 'ADULTE', 18, 64, 'CALCIUM',      950,    'mg',   'ANSES AS'),
('ajr-fa-zn',   'FEMME', 'ADULTE', 18, 64, 'ZINC',         8,      'mg',   'ANSES RNP'),
('ajr-fa-mg',   'FEMME', 'ADULTE', 18, 64, 'MAGNESIUM',    300,    'mg',   'ANSES AS'),
('ajr-fa-b12',  'FEMME', 'ADULTE', 18, 64, 'VITAMINE_B12', 4,      'µg',   'ANSES AS'),
('ajr-fa-vd',   'FEMME', 'ADULTE', 18, 64, 'VITAMINE_D',   15,     'µg',   'ANSES AS'),
('ajr-fa-vc',   'FEMME', 'ADULTE', 18, 64, 'VITAMINE_C',   110,    'mg',   'ANSES RNP'),
('ajr-fa-o3',   'FEMME', 'ADULTE', 18, 64, 'OMEGA_3',      2,      'g',    'ANSES AS'),
('ajr-fa-o6',   'FEMME', 'ADULTE', 18, 64, 'OMEGA_6',      8,      'g',    'ANSES AS');

-- ============================================================
-- HOMME ADOLESCENT (14-17 ans)
-- ============================================================

INSERT INTO ajr_references (id, sexe, tranche_age, age_min, age_max, nutriment, valeur_base, unite, source) VALUES
('ajr-hado-cal',  'HOMME', 'ADO', 14, 17, 'CALORIES',     2500,   'kcal', 'ANSES'),
('ajr-hado-prot', 'HOMME', 'ADO', 14, 17, 'PROTEINES',    93.75,  'g',    'ANSES'),
('ajr-hado-glu',  'HOMME', 'ADO', 14, 17, 'GLUCIDES',     312.5,  'g',    'ANSES'),
('ajr-hado-lip',  'HOMME', 'ADO', 14, 17, 'LIPIDES',      97.2,   'g',    'ANSES'),
('ajr-hado-fib',  'HOMME', 'ADO', 14, 17, 'FIBRES',       25,     'g',    'ANSES'),
('ajr-hado-sel',  'HOMME', 'ADO', 14, 17, 'SEL',          5,      'g',    'OMS'),
('ajr-hado-suc',  'HOMME', 'ADO', 14, 17, 'SUCRES',       62.5,   'g',    'ANSES'),
('ajr-hado-fer',  'HOMME', 'ADO', 14, 17, 'FER',          13,     'mg',   'ANSES RNP'),
('ajr-hado-ca',   'HOMME', 'ADO', 14, 17, 'CALCIUM',      1000,   'mg',   'ANSES AS'),
('ajr-hado-zn',   'HOMME', 'ADO', 14, 17, 'ZINC',         13,     'mg',   'ANSES RNP'),
('ajr-hado-mg',   'HOMME', 'ADO', 14, 17, 'MAGNESIUM',    380,    'mg',   'ANSES AS'),
('ajr-hado-b12',  'HOMME', 'ADO', 14, 17, 'VITAMINE_B12', 4,      'µg',   'ANSES AS'),
('ajr-hado-vd',   'HOMME', 'ADO', 14, 17, 'VITAMINE_D',   15,     'µg',   'ANSES AS'),
('ajr-hado-vc',   'HOMME', 'ADO', 14, 17, 'VITAMINE_C',   110,    'mg',   'ANSES RNP'),
('ajr-hado-o3',   'HOMME', 'ADO', 14, 17, 'OMEGA_3',      2.5,    'g',    'ANSES AS'),
('ajr-hado-o6',   'HOMME', 'ADO', 14, 17, 'OMEGA_6',      10,     'g',    'ANSES AS');

-- ============================================================
-- FEMME ADOLESCENTE (14-17 ans)
-- ============================================================

INSERT INTO ajr_references (id, sexe, tranche_age, age_min, age_max, nutriment, valeur_base, unite, source) VALUES
('ajr-fado-cal',  'FEMME', 'ADO', 14, 17, 'CALORIES',     2000,   'kcal', 'ANSES'),
('ajr-fado-prot', 'FEMME', 'ADO', 14, 17, 'PROTEINES',    75,     'g',    'ANSES'),
('ajr-fado-glu',  'FEMME', 'ADO', 14, 17, 'GLUCIDES',     250,    'g',    'ANSES'),
('ajr-fado-lip',  'FEMME', 'ADO', 14, 17, 'LIPIDES',      77.8,   'g',    'ANSES'),
('ajr-fado-fib',  'FEMME', 'ADO', 14, 17, 'FIBRES',       25,     'g',    'ANSES'),
('ajr-fado-sel',  'FEMME', 'ADO', 14, 17, 'SEL',          5,      'g',    'OMS'),
('ajr-fado-suc',  'FEMME', 'ADO', 14, 17, 'SUCRES',       50,     'g',    'ANSES'),
('ajr-fado-fer',  'FEMME', 'ADO', 14, 17, 'FER',          16,     'mg',   'ANSES RNP'),
('ajr-fado-ca',   'FEMME', 'ADO', 14, 17, 'CALCIUM',      1000,   'mg',   'ANSES AS'),
('ajr-fado-zn',   'FEMME', 'ADO', 14, 17, 'ZINC',         8,      'mg',   'ANSES RNP'),
('ajr-fado-mg',   'FEMME', 'ADO', 14, 17, 'MAGNESIUM',    300,    'mg',   'ANSES AS'),
('ajr-fado-b12',  'FEMME', 'ADO', 14, 17, 'VITAMINE_B12', 4,      'µg',   'ANSES AS'),
('ajr-fado-vd',   'FEMME', 'ADO', 14, 17, 'VITAMINE_D',   15,     'µg',   'ANSES AS'),
('ajr-fado-vc',   'FEMME', 'ADO', 14, 17, 'VITAMINE_C',   110,    'mg',   'ANSES RNP'),
('ajr-fado-o3',   'FEMME', 'ADO', 14, 17, 'OMEGA_3',      2,      'g',    'ANSES AS'),
('ajr-fado-o6',   'FEMME', 'ADO', 14, 17, 'OMEGA_6',      8,      'g',    'ANSES AS');

-- ============================================================
-- HOMME SENIOR (65+ ans)
-- ============================================================

INSERT INTO ajr_references (id, sexe, tranche_age, age_min, age_max, nutriment, valeur_base, unite, source) VALUES
('ajr-hs-cal',  'HOMME', 'SENIOR', 65, 120, 'CALORIES',     2200,   'kcal', 'ANSES'),
('ajr-hs-prot', 'HOMME', 'SENIOR', 65, 120, 'PROTEINES',    82.5,   'g',    'ANSES'),
('ajr-hs-glu',  'HOMME', 'SENIOR', 65, 120, 'GLUCIDES',     275,    'g',    'ANSES'),
('ajr-hs-lip',  'HOMME', 'SENIOR', 65, 120, 'LIPIDES',      85.6,   'g',    'ANSES'),
('ajr-hs-fib',  'HOMME', 'SENIOR', 65, 120, 'FIBRES',       30,     'g',    'ANSES'),
('ajr-hs-sel',  'HOMME', 'SENIOR', 65, 120, 'SEL',          5,      'g',    'OMS'),
('ajr-hs-suc',  'HOMME', 'SENIOR', 65, 120, 'SUCRES',       55,     'g',    'ANSES'),
('ajr-hs-fer',  'HOMME', 'SENIOR', 65, 120, 'FER',          11,     'mg',   'ANSES RNP'),
('ajr-hs-ca',   'HOMME', 'SENIOR', 65, 120, 'CALCIUM',      1200,   'mg',   'ANSES AS'),
('ajr-hs-zn',   'HOMME', 'SENIOR', 65, 120, 'ZINC',         11,     'mg',   'ANSES RNP'),
('ajr-hs-mg',   'HOMME', 'SENIOR', 65, 120, 'MAGNESIUM',    380,    'mg',   'ANSES AS'),
('ajr-hs-b12',  'HOMME', 'SENIOR', 65, 120, 'VITAMINE_B12', 4,      'µg',   'ANSES AS'),
('ajr-hs-vd',   'HOMME', 'SENIOR', 65, 120, 'VITAMINE_D',   20,     'µg',   'ANSES AS'),
('ajr-hs-vc',   'HOMME', 'SENIOR', 65, 120, 'VITAMINE_C',   110,    'mg',   'ANSES RNP'),
('ajr-hs-o3',   'HOMME', 'SENIOR', 65, 120, 'OMEGA_3',      2.5,    'g',    'ANSES AS'),
('ajr-hs-o6',   'HOMME', 'SENIOR', 65, 120, 'OMEGA_6',      10,     'g',    'ANSES AS');

-- ============================================================
-- FEMME SENIOR (65+ ans)
-- ============================================================

INSERT INTO ajr_references (id, sexe, tranche_age, age_min, age_max, nutriment, valeur_base, unite, source) VALUES
('ajr-fs-cal',  'FEMME', 'SENIOR', 65, 120, 'CALORIES',     1800,   'kcal', 'ANSES'),
('ajr-fs-prot', 'FEMME', 'SENIOR', 65, 120, 'PROTEINES',    67.5,   'g',    'ANSES'),
('ajr-fs-glu',  'FEMME', 'SENIOR', 65, 120, 'GLUCIDES',     225,    'g',    'ANSES'),
('ajr-fs-lip',  'FEMME', 'SENIOR', 65, 120, 'LIPIDES',      70,     'g',    'ANSES'),
('ajr-fs-fib',  'FEMME', 'SENIOR', 65, 120, 'FIBRES',       30,     'g',    'ANSES'),
('ajr-fs-sel',  'FEMME', 'SENIOR', 65, 120, 'SEL',          5,      'g',    'OMS'),
('ajr-fs-suc',  'FEMME', 'SENIOR', 65, 120, 'SUCRES',       45,     'g',    'ANSES'),
('ajr-fs-fer',  'FEMME', 'SENIOR', 65, 120, 'FER',          11,     'mg',   'ANSES RNP'),
('ajr-fs-ca',   'FEMME', 'SENIOR', 65, 120, 'CALCIUM',      1200,   'mg',   'ANSES AS'),
('ajr-fs-zn',   'FEMME', 'SENIOR', 65, 120, 'ZINC',         8,      'mg',   'ANSES RNP'),
('ajr-fs-mg',   'FEMME', 'SENIOR', 65, 120, 'MAGNESIUM',    300,    'mg',   'ANSES AS'),
('ajr-fs-b12',  'FEMME', 'SENIOR', 65, 120, 'VITAMINE_B12', 4,      'µg',   'ANSES AS'),
('ajr-fs-vd',   'FEMME', 'SENIOR', 65, 120, 'VITAMINE_D',   20,     'µg',   'ANSES AS'),
('ajr-fs-vc',   'FEMME', 'SENIOR', 65, 120, 'VITAMINE_C',   110,    'mg',   'ANSES RNP'),
('ajr-fs-o3',   'FEMME', 'SENIOR', 65, 120, 'OMEGA_3',      2,      'g',    'ANSES AS'),
('ajr-fs-o6',   'FEMME', 'SENIOR', 65, 120, 'OMEGA_6',      8,      'g',    'ANSES AS');

-- ============================================================
-- Coefficients d'ajustement par regime alimentaire
-- Sources : OMS, ADA (Academy of Nutrition and Dietetics)
-- ============================================================

INSERT INTO ajr_regime_coefficients (id, regime, nutriment, coefficient, justification) VALUES
-- VEGAN
('coeff-vegan-fer',   'VEGAN', 'FER',       1.8, 'Fer non-heminique vegetal : absorption 5-12% vs 15-35% heminique. Coefficient OMS/ADA.'),
('coeff-vegan-zn',    'VEGAN', 'ZINC',      1.5, 'Phytates des legumineuses et cereales completes reduisent absorption. Coefficient ADA.'),
('coeff-vegan-o3',    'VEGAN', 'OMEGA_3',   1.5, 'Conversion ALA vers EPA/DHA < 10%. Apport ALA plus eleve necessaire.'),
('coeff-vegan-prot',  'VEGAN', 'PROTEINES', 1.1, 'Digestibilite inferieure des proteines vegetales (score PDCAAS). Ajustement modere.'),
('coeff-vegan-b12',   'VEGAN', 'VITAMINE_B12', 1.0, 'Pas ajustement quota — B12 absente des vegetaux, signalement manque + recommandation fortifies.'),
('coeff-vegan-ca',    'VEGAN', 'CALCIUM',   1.0, 'Biodisponibilite du calcium vegetal (brocoli, chou) comparable aux laitages.'),

-- VEGETARIEN
('coeff-veg-fer',     'VEGETARIEN', 'FER',       1.5, 'Fer non-heminique predominant. Coefficient intermediaire OMS.'),
('coeff-veg-zn',      'VEGETARIEN', 'ZINC',      1.3, 'Phytates reduisent absorption, effet modere grace aux laitages/oeufs.'),
('coeff-veg-o3',      'VEGETARIEN', 'OMEGA_3',   1.2, 'Moins de sources directes EPA/DHA. Compensation partielle par oeufs enrichis.'),
('coeff-veg-prot',    'VEGETARIEN', 'PROTEINES', 1.0, 'Proteines laitieres et oeufs compensent. Pas ajustement necessaire.'),
('coeff-veg-b12',     'VEGETARIEN', 'VITAMINE_B12', 1.0, 'B12 presente dans laitages et oeufs. Pas ajustement.'),
('coeff-veg-ca',      'VEGETARIEN', 'CALCIUM',   1.0, 'Laitages disponibles. Pas ajustement.'),

-- FLEXITARIEN (tous coefficients a 1.0)
('coeff-flex-fer',    'FLEXITARIEN', 'FER',       1.0, 'Consommation reguliere de viande/poisson. Pas ajustement.'),
('coeff-flex-zn',     'FLEXITARIEN', 'ZINC',      1.0, 'Sources animales disponibles. Pas ajustement.'),
('coeff-flex-o3',     'FLEXITARIEN', 'OMEGA_3',   1.0, 'Poisson consomme regulierement. Pas ajustement.'),
('coeff-flex-prot',   'FLEXITARIEN', 'PROTEINES', 1.0, 'Mix animal/vegetal equilibre. Pas ajustement.'),

-- OMNIVORE (tous coefficients a 1.0)
('coeff-omni-fer',    'OMNIVORE', 'FER',       1.0, 'Regime standard. Pas ajustement.'),
('coeff-omni-zn',     'OMNIVORE', 'ZINC',      1.0, 'Regime standard. Pas ajustement.'),
('coeff-omni-o3',     'OMNIVORE', 'OMEGA_3',   1.0, 'Regime standard. Pas ajustement.'),
('coeff-omni-prot',   'OMNIVORE', 'PROTEINES', 1.0, 'Regime standard. Pas ajustement.');

-- Index for quick lookup by sexe + age range
CREATE INDEX idx_ajr_sexe_age ON ajr_references(sexe, age_min, age_max);
CREATE INDEX idx_ajr_regime ON ajr_regime_coefficients(regime);
