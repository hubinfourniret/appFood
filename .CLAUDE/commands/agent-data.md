# Agent Data — Import & Donnees nutritionnelles

Tu es l'agent responsable de l'**import, la transformation et l'indexation des donnees nutritionnelles**.

## Avant de coder

Lis ces fichiers de reference :
- `CONVENTIONS.md` — Sections 9 (base de donnees), 11 (langue)
- `docs/project-structure.md` — Section 5 (backend/external/, backend/database/)
- `docs/data-models.md` — Section 3 (tables Exposed : AlimentsTable, PortionsTable, QuotasTable)
- `docs/phase4-dispatch-plan-agents.md` — Plan de dispatch, dependances des US

## Ton perimetre

Tu crees et modifies UNIQUEMENT :
- `backend/src/main/kotlin/com/appfood/backend/external/CiqualImporter.kt` — Import CSV Ciqual
- `backend/src/main/kotlin/com/appfood/backend/external/OpenFoodFactsClient.kt` — Client API OFF
- `backend/src/main/resources/db/migration/V002__seed_ajr.sql` — Donnees AJR/ANC
- `backend/src/main/resources/db/migration/V003__seed_portions.sql` — Portions standard
- `backend/docker/meilisearch/config.json` — Config Meilisearch (synonymes, filtres)

## Tu lis en lecture seule

- `shared/.../model/` — Modeles de donnees (Aliment, NutrimentValues, PortionStandard)
- `backend/.../database/tables/` — Tables Exposed (AlimentsTable, PortionsTable)

## US assignees

### DATA-01 — Import Ciqual

1. Telecharge le CSV Ciqual (l'utilisateur le fournira — voir TODO-HUMAIN.md)
2. Parse le CSV (attention aux encodages, separateurs, unites)
3. Mappe les colonnes Ciqual vers le schema `AlimentsTable` (data-models.md)
4. Insere dans PostgreSQL via Exposed
5. Indexe dans Meilisearch (champs searchable : nom, categorie ; filtres : regimes_compatibles, categorie, source)
6. Tag `regime_compatible` pour chaque aliment (VEGAN, VEGETARIEN, etc.)
7. Script idempotent (rejouable sans dupliquer)

### DATA-02 — Integration Open Food Facts

1. Client HTTP pour l'API Open Food Facts (https://world.openfoodfacts.org/api/v2/)
2. Recherche par nom, par code-barres
3. Mapping des champs OFF vers le schema Aliment
4. Cache des produits recherches en base PostgreSQL
5. Indexation dans Meilisearch des produits trouves

### DATA-03 — Tables AJR/ANC

1. Cree la migration SQL avec les valeurs officielles ANSES
2. Valeurs par : sexe, tranche d'age, niveau d'activite
3. Ajustements pour regimes vegan/vegetarien (fer x1.8, zinc x1.5, etc.)
4. Source : tables ANSES + recommandations Societe francaise de nutrition

### PORTIONS-01 (donnees)

1. Cree la migration SQL avec les portions standard
2. Portions generiques : cuillere a cafe (5g), cuillere a soupe (15g), verre (200ml), bol (250g), assiette (300g), poignee (30g)
3. Portions specifiques pour les aliments Ciqual les plus courants (pomme ~150g, banane ~120g, oeuf ~60g, etc.)
4. Source : tables de portions ANSES

## Meilisearch — Configuration

```json
{
  "searchableAttributes": ["nom", "marque", "categorie"],
  "filterableAttributes": ["regimes_compatibles", "categorie", "source"],
  "sortableAttributes": ["nom"],
  "synonyms": {
    "tomate": ["tomates"],
    "pomme de terre": ["patate", "patates"],
    "brocoli": ["broccoli", "brocolis"]
  },
  "typoTolerance": {
    "enabled": true,
    "minWordSizeForTypos": { "oneTypo": 4, "twoTypos": 8 }
  }
}
```

## Checklist

1. [ ] Script d'import idempotent (INSERT OR UPDATE)
2. [ ] Mapping nutriments complet (16 nutriments)
3. [ ] Tags regime generes correctement
4. [ ] Index Meilisearch configure avec synonymes francais
5. [ ] Donnees AJR/ANC conformes aux sources ANSES
6. [ ] Tests de validation des donnees importees
7. [ ] Commit sur `feature/{US-ID}-description`
