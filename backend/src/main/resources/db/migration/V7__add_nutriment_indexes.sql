-- PERF-01: indexes on nutrient columns to speed up "ORDER BY <nutrient> DESC LIMIT 200"
-- queries used by RecommandationService for candidate pre-filtering.
CREATE INDEX IF NOT EXISTS idx_aliments_proteines ON aliments (proteines DESC);
CREATE INDEX IF NOT EXISTS idx_aliments_fer ON aliments (fer DESC);
CREATE INDEX IF NOT EXISTS idx_aliments_calcium ON aliments (calcium DESC);
CREATE INDEX IF NOT EXISTS idx_aliments_vitamine_b12 ON aliments (vitamine_b12 DESC);
CREATE INDEX IF NOT EXISTS idx_aliments_fibres ON aliments (fibres DESC);
CREATE INDEX IF NOT EXISTS idx_aliments_zinc ON aliments (zinc DESC);
CREATE INDEX IF NOT EXISTS idx_aliments_magnesium ON aliments (magnesium DESC);
CREATE INDEX IF NOT EXISTS idx_aliments_vitamine_d ON aliments (vitamine_d DESC);
CREATE INDEX IF NOT EXISTS idx_aliments_omega_3 ON aliments (omega_3 DESC);

-- Index on regimes_compatibles to speed filtering by diet (LIKE '%VEGAN%')
CREATE INDEX IF NOT EXISTS idx_aliments_regimes ON aliments (regimes_compatibles);

-- Recettes indexes (only published recipes are queried for recommendations)
CREATE INDEX IF NOT EXISTS idx_recettes_publie ON recettes (publie);
CREATE INDEX IF NOT EXISTS idx_recettes_proteines ON recettes (proteines DESC) WHERE publie = true;
CREATE INDEX IF NOT EXISTS idx_recettes_fer ON recettes (fer DESC) WHERE publie = true;
CREATE INDEX IF NOT EXISTS idx_recettes_fibres ON recettes (fibres DESC) WHERE publie = true;
CREATE INDEX IF NOT EXISTS idx_recettes_calcium ON recettes (calcium DESC) WHERE publie = true;
CREATE INDEX IF NOT EXISTS idx_recettes_vitamine_b12 ON recettes (vitamine_b12 DESC) WHERE publie = true;
