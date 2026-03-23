# appFood — Clarifications US critiques

> Ce document precise les US dont le niveau de detail est insuffisant pour les agents.
> Les agents SHARED, BACKEND et DATA doivent lire ce fichier AVANT d'implementer les US concernees.
> Ce document est complementaire au backlog (phase3-backlog-rapport.md), pas un remplacement.

---

## 1. QUOTAS-01 — Algorithme de calcul des quotas personnalises

### 1.1 Formule du metabolisme de base (MB)

**Formule retenue : Mifflin-St Jeor** (plus precise que Harris-Benedict selon l'ANSES et l'ADA).

```
Homme : MB = (10 × poids_kg) + (6.25 × taille_cm) - (5 × age) + 5
Femme : MB = (10 × poids_kg) + (6.25 × taille_cm) - (5 × age) - 161
```

### 1.2 Depense energetique totale (DET)

```
DET = MB × coefficient_activite
```

| NiveauActivite | Coefficient | Description |
|----------------|-------------|-------------|
| SEDENTAIRE | 1.2 | Peu ou pas d'exercice, travail de bureau |
| LEGER | 1.375 | Exercice leger 1-3 jours/semaine |
| MODERE | 1.55 | Exercice modere 3-5 jours/semaine |
| ACTIF | 1.725 | Exercice intense 6-7 jours/semaine |
| TRES_ACTIF | 1.9 | Exercice tres intense + travail physique |

### 1.3 Repartition des macronutriments

Pourcentages de la DET, identiques quel que soit le regime :

| Nutriment | % de la DET | Calcul |
|-----------|-------------|--------|
| PROTEINES | 15% | DET × 0.15 / 4 (4 kcal/g) |
| GLUCIDES | 50% | DET × 0.50 / 4 (4 kcal/g) |
| LIPIDES | 35% | DET × 0.35 / 9 (9 kcal/g) |
| FIBRES | — | 30g fixe (adulte), 25g si age < 18 |
| SUCRES | — | max 10% de la DET → DET × 0.10 / 4 |
| SEL | — | max 5g fixe (OMS) |

**Note** : La repartition proteines/glucides/lipides est celle recommandee par l'ANSES pour un adulte. Elle ne change PAS selon le regime (vegan ou non). Ce qui change, ce sont les micronutriments (voir 1.4).

### 1.4 Micronutriments — Valeurs de reference ANSES

Les valeurs ci-dessous sont les **References Nutritionnelles pour la Population (RNP)** ou **Apports Satisfaisants (AS)** de l'ANSES.

#### Homme adulte (19-64 ans)

| Nutriment | Valeur | Unite | Source ANSES |
|-----------|--------|-------|-------------|
| FER | 11 | mg | RNP |
| CALCIUM | 950 | mg | AS |
| ZINC | 11 | mg | RNP (2500 kcal) |
| MAGNESIUM | 380 | mg | AS |
| VITAMINE_B12 | 4 | µg | AS |
| VITAMINE_D | 15 | µg | AS |
| VITAMINE_C | 110 | mg | RNP |
| OMEGA_3 | 2.5 | g | AS (ALA+EPA+DHA, ~1% DET) |
| OMEGA_6 | 10 | g | AS (~4% DET) |

#### Femme adulte (19-64 ans)

| Nutriment | Valeur | Unite | Source ANSES |
|-----------|--------|-------|-------------|
| FER | 16 | mg | RNP (menstruations) |
| CALCIUM | 950 | mg | AS |
| ZINC | 8 | mg | RNP (2000 kcal) |
| MAGNESIUM | 300 | mg | AS |
| VITAMINE_B12 | 4 | µg | AS |
| VITAMINE_D | 15 | µg | AS |
| VITAMINE_C | 110 | mg | RNP |
| OMEGA_3 | 2 | g | AS (~1% DET) |
| OMEGA_6 | 8 | g | AS (~4% DET) |

#### Ajustements par tranche d'age

| Tranche | Ajustements vs adulte |
|---------|----------------------|
| 14-18 ans | CALCIUM = 1000mg, FER homme = 13mg, ZINC homme = 13mg |
| 65+ ans | VITAMINE_D = 20µg, CALCIUM = 1200mg, PROTEINES = 1g/kg (au lieu de % DET si plus eleve) |

**Regle d'implementation** : Si l'age est hors [14-100], utiliser les valeurs adulte par defaut. Le questionnaire onboarding valide age dans [1-120] mais les AJR < 14 ans ne sont pas dans le scope MVP — afficher un avertissement "Consultez un pediatre" et utiliser les valeurs 14-18.

### 1.5 Coefficients d'ajustement par regime alimentaire

Les regimes vegetalien et vegetarien necessitent des ajustements car certains nutriments sont moins biodisponibles dans les sources vegetales.

| Nutriment | VEGAN | VEGETARIEN | FLEXITARIEN | OMNIVORE | Justification |
|-----------|-------|------------|-------------|----------|---------------|
| FER | ×1.8 | ×1.5 | ×1.0 | ×1.0 | Fer non-heminique (vegetal) = absorption 5-12% vs 15-35% pour le fer heminique (animal). Coefficient OMS/ADA. |
| ZINC | ×1.5 | ×1.3 | ×1.0 | ×1.0 | Phytates des legumineuses et cereales completes reduisent l'absorption. Coefficient ADA. |
| CALCIUM | ×1.0 | ×1.0 | ×1.0 | ×1.0 | Pas d'ajustement — la biodisponibilite du calcium vegetal (brocoli, chou) est comparable aux laitages. |
| OMEGA_3 | ×1.5 | ×1.2 | ×1.0 | ×1.0 | Conversion ALA → EPA/DHA < 10%. Les vegans doivent compenser par un apport ALA plus eleve. |
| PROTEINES | ×1.1 | ×1.0 | ×1.0 | ×1.0 | Digestibilite legerement inferieure des proteines vegetales (PDCAAS). Ajustement modere. |
| VITAMINE_B12 | ×1.0 | ×1.0 | ×1.0 | ×1.0 | Pas d'ajustement sur le quota — la B12 est absente des sources vegetales, mais le quota reste le meme. Le role de l'app est de signaler le manque et recommander des aliments fortifies. |
| Autres | ×1.0 | ×1.0 | ×1.0 | ×1.0 | — |

### 1.6 Formule complete — Pseudo-code

```kotlin
fun calculerQuotas(profile: UserProfile): List<QuotaJournalier> {
    // 1. Metabolisme de base (Mifflin-St Jeor)
    val mb = when (profile.sexe) {
        HOMME -> (10.0 * profile.poidsKg) + (6.25 * profile.tailleCm) - (5.0 * profile.age) + 5.0
        FEMME -> (10.0 * profile.poidsKg) + (6.25 * profile.tailleCm) - (5.0 * profile.age) - 161.0
    }

    // 2. Depense energetique totale
    val coeffActivite = when (profile.niveauActivite) {
        SEDENTAIRE -> 1.2
        LEGER -> 1.375
        MODERE -> 1.55
        ACTIF -> 1.725
        TRES_ACTIF -> 1.9
    }
    val det = mb * coeffActivite

    // 3. Macronutriments (derives de la DET)
    val calories = det
    val proteines = det * 0.15 / 4.0
    val glucides = det * 0.50 / 4.0
    val lipides = det * 0.35 / 9.0
    val fibres = if (profile.age < 18) 25.0 else 30.0
    val sucres = det * 0.10 / 4.0
    val sel = 5.0

    // 4. Micronutriments (valeurs de reference ANSES)
    val microBase = getMicroNutrimentsBase(profile.sexe, profile.age) // voir tables 1.4

    // 5. Ajustement par regime
    val coeffRegime = getCoefficientsRegime(profile.regimeAlimentaire) // voir table 1.5
    val fer = microBase.fer * coeffRegime.fer
    val zinc = microBase.zinc * coeffRegime.zinc
    val omega3 = microBase.omega3 * coeffRegime.omega3
    val proteinesAjustees = proteines * coeffRegime.proteines
    // ... idem pour chaque nutriment avec coefficient

    // 6. Ajustement 65+ pour proteines
    if (profile.age >= 65) {
        proteinesAjustees = max(proteinesAjustees, profile.poidsKg * 1.0)
    }

    // 7. Arrondir : calories entier, macros 1 decimale, micros entier sauf B12/D (1 decimale)
    // 8. Construire la liste de QuotaJournalier avec estPersonnalise = false
}
```

### 1.7 Unites par nutriment (pour le champ `unite` de QuotaJournalier)

| NutrimentType | Unite |
|---------------|-------|
| CALORIES | kcal |
| PROTEINES | g |
| GLUCIDES | g |
| LIPIDES | g |
| FIBRES | g |
| SEL | g |
| SUCRES | g |
| FER | mg |
| CALCIUM | mg |
| ZINC | mg |
| MAGNESIUM | mg |
| VITAMINE_B12 | µg |
| VITAMINE_D | µg |
| VITAMINE_C | mg |
| OMEGA_3 | g |
| OMEGA_6 | g |

### 1.8 Cas limites

- **Profil incomplet (onboarding skippe)** : Utiliser les valeurs par defaut → Homme 30 ans, 75kg, 175cm, OMNIVORE, MODERE (ou Femme 30 ans, 60kg, 165cm selon le sexe si fourni). Si aucune info, defaut HOMME.
- **Poids extreme (< 30kg ou > 300kg)** : Le calcul s'applique normalement (la validation onboarding limite a [20-500]).
- **Age < 14** : Utiliser les valeurs 14-18 + afficher avertissement.

---

## 2. RECO-01 — Algorithme de recommandation d'aliments

### 2.1 Seuil de declenchement

Un nutriment est considere en **deficit** si :

```
pourcentageAtteint = (consomme / quota) * 100

- Si pourcentageAtteint < 70% → deficit FORT (priorite haute)
- Si pourcentageAtteint >= 70% et < 90% → deficit MODERE (priorite basse)
- Si pourcentageAtteint >= 90% → PAS de deficit (pas de recommandation)
```

**Heure de reference** : Le seuil est dynamique dans la journee. A 20h, il est normal d'etre a 70%. Pour eviter des faux positifs le matin :
- Pas de recommandation avant 11h (l'utilisateur n'a peut-etre pas encore saisi son petit-dejeuner)
- Ou plus simple au MVP : ignorer l'heure et toujours recommander sur la base du deficit brut. L'utilisateur comprend que les recommandations sont "pour la journee restante".

**Choix MVP : deficit brut, pas de ponderation horaire.** L'intelligence horaire est un candidat V1.1.

### 2.2 Score d'un aliment

Pour chaque aliment compatible (regime OK, pas exclu, pas d'allergie) :

```
score(aliment) = somme sur chaque nutriment en deficit de :
    min(apport_nutriment / manque_nutriment, 1.0) × poids_nutriment

Ou :
- apport_nutriment = nutrimentsPour100g[nutriment] × (quantiteSuggeree / 100)
- manque_nutriment = quota - consomme
- poids_nutriment = facteur de priorite du deficit (voir table ci-dessous)
```

#### Poids de priorite par deficit

| Condition | Poids |
|-----------|-------|
| Deficit FORT (< 70%) sur un nutriment critique pour le regime | 3.0 |
| Deficit FORT (< 70%) sur un autre nutriment | 2.0 |
| Deficit MODERE (70-90%) | 1.0 |

#### Nutriments critiques par regime

| Regime | Nutriments critiques |
|--------|---------------------|
| VEGAN | VITAMINE_B12, FER, ZINC, OMEGA_3, CALCIUM, PROTEINES |
| VEGETARIEN | VITAMINE_B12, FER, ZINC, OMEGA_3 |
| FLEXITARIEN | (aucun specifique) |
| OMNIVORE | (aucun specifique) |

### 2.3 Quantite suggeree

La quantite suggeree est calculee pour combler le **nutriment le plus deficitaire** parmi ceux que l'aliment couvre :

```
nutrimentPrincipal = nutriment en deficit pour lequel l'aliment a la plus forte densite
manque = quota - consomme
quantiteSuggereGrammes = min(
    (manque / nutrimentsPour100g[nutrimentPrincipal]) × 100,
    300  // cap a 300g — une portion realiste
)

// Arrondir a 10g pres
quantiteSuggereGrammes = round(quantiteSuggereGrammes / 10) × 10
```

**Cap a 300g** : On ne suggere jamais de manger plus de 300g d'un aliment. Si 300g ne suffit pas a combler le manque, on affiche quand meme la suggestion avec le % de couverture partiel.

### 2.4 Pourcentage de couverture

Pour chaque nutriment en deficit :

```
pourcentageCouverture[nutriment] = min(
    (nutrimentsPour100g[nutriment] × quantiteSuggereGrammes / 100) / manque × 100,
    100.0
)
```

### 2.5 Filtrage des aliments

Avant le scoring, filtrer :

1. **Regime** : `aliment.regimesCompatibles` doit contenir le regime de l'utilisateur (OU un regime "superieur" — un aliment VEGAN est aussi compatible VEGETARIEN, FLEXITARIEN, OMNIVORE)
2. **Exclusions** : `aliment.id` ne doit PAS etre dans `userPreferences.alimentsExclus`
3. **Allergies** : Si l'aliment appartient a une categorie allergene listee dans `userPreferences.allergies`, l'exclure. Le mapping categorie → allergene est defini ci-dessous :
   - `"gluten"` → exclure les aliments de categorie contenant "ble", "seigle", "orge", "avoine" (sauf si "sans gluten" dans le nom)
   - `"soja"` → exclure categorie contenant "soja"
   - `"arachides"` → exclure categorie contenant "arachide", "cacahuete"
   - `"fruits_a_coque"` → exclure categorie contenant "noix", "amande", "noisette", "cajou", "pistache", "pecan", "macadamia"
   - `"lait"` → exclure categorie contenant "lait", "fromage", "yaourt", "beurre", "creme"
   - `"oeufs"` → exclure categorie contenant "oeuf", "egg"

**Note MVP** : Le filtrage allergene par categorie est approximatif. En V1.1, utiliser les tags allergenes Open Food Facts pour un filtrage plus precis.

### 2.6 Tri et limite

1. Trier par score decroissant
2. **Diversite** : Ne pas retourner plus de 2 aliments de la meme categorie dans le top 10
3. Retourner les top `limit` (defaut 10)

### 2.7 Cas ou aucun aliment ne comble le manque

Si un nutriment est en deficit mais aucun aliment compatible ne le couvre significativement (ex: B12 en regime vegan strict et pas d'aliments fortifies en base) :

- Inclure le nutriment dans `manquesIdentifies` de la reponse
- Ajouter un message dans la recommandation : pointer vers les aliments fortifies ou complements (B12 est le cas classique)
- **Au MVP** : simplement retourner les meilleurs aliments meme si la couverture est faible. L'UI affichera le % de couverture et l'utilisateur comprendra.

### 2.8 RECO-02 — Score d'une recette

Meme logique que pour un aliment, mais :

```
// Nutriments pour une portion de recette
nutrimentParPortion = recette.nutrimentsTotaux / recette.nbPortions

score(recette) = somme sur chaque nutriment en deficit de :
    min(nutrimentParPortion[nutriment] / manque_nutriment, 1.0) × poids_nutriment
```

La `quantiteSuggereGrammes` n'est pas pertinente pour une recette — on recommande "1 portion" (ajustable par l'utilisateur).

Le `pourcentageCouvertureGlobal` est la moyenne des `pourcentageCouverture` sur les nutriments en deficit.

### 2.9 Cache des recommandations

Comme precise dans `api-contracts.md` :
- Le `DashboardService` cache les recommandations avec un TTL de 30 minutes
- Invalidation du cache si : nouvelle entree journal, modification/suppression d'entree, changement de quotas
- En memoire (HashMap + timestamp), pas de Redis au MVP

---

## 3. DATA-01 — Import de la base Ciqual

### 3.1 Fichier source

- **Fichier** : `Table Ciqual YYYY_*.csv` (telechargeable sur https://ciqual.anses.fr)
- **Encodage** : UTF-8 (depuis 2020, les fichiers recents sont en UTF-8. Si UTF-8 BOM, le gerer.)
- **Separateur** : point-virgule `;` (format francais)
- **Decimal** : virgule `,` (format francais) — convertir en point `.` pour le parsing

### 3.2 Mapping des colonnes Ciqual → NutrimentValues

Les noms de colonnes Ciqual sont longs et changent legerement entre versions. L'agent doit faire un mapping par **recherche de motif** (contains), pas par nom exact.

| Champ appFood | Pattern de colonne Ciqual (contient) | Unite Ciqual | Conversion |
|---------------|--------------------------------------|-------------|------------|
| `calories` | `"Energie, Règlement UE"` ET `"(kcal"` | kcal/100g | Aucune |
| `proteines` | `"Protéines"` ET `"(g"` | g/100g | Aucune |
| `glucides` | `"Glucides"` ET `"(g"` | g/100g | Aucune |
| `lipides` | `"Lipides"` ET `"(g"` | g/100g | Aucune |
| `fibres` | `"Fibres"` ET `"(g"` | g/100g | Aucune |
| `sel` | `"Sel chlorure de sodium"` ET `"(g"` | g/100g | Aucune |
| `sucres` | `"Sucres"` ET `"(g"` | g/100g | Aucune |
| `fer` | `"Fer"` ET `"(mg"` | mg/100g | Aucune |
| `calcium` | `"Calcium"` ET `"(mg"` | mg/100g | Aucune |
| `zinc` | `"Zinc"` ET `"(mg"` | mg/100g | Aucune |
| `magnesium` | `"Magnésium"` ET `"(mg"` | mg/100g | Aucune |
| `vitamineB12` | `"Vitamine B12"` ET `"(µg"` | µg/100g | Aucune |
| `vitamineD` | `"Vitamine D"` ET `"(µg"` | µg/100g | Aucune |
| `vitamineC` | `"Vitamine C"` ET `"(mg"` | mg/100g | Aucune |
| `omega3` | CALCULE (voir 3.3) | — | — |
| `omega6` | CALCULE (voir 3.3) | — | — |

**Colonnes Ciqual additionnelles a lire** :

| Champ appFood | Pattern de colonne Ciqual |
|---------------|--------------------------|
| `sourceId` | `"alim_code"` ou premiere colonne numerique |
| `nom` | `"alim_nom_fr"` ou colonne contenant les noms d'aliments en francais |
| `categorie` | `"alim_grp_nom_fr"` ou `"alim_ssgrp_nom_fr"` — utiliser le sous-groupe si disponible, sinon le groupe |

### 3.3 Calcul omega-3 et omega-6

La base Ciqual ne fournit pas directement les omega-3/omega-6 totaux. Elle fournit les acides gras individuels :

**Omega-3** : Chercher les colonnes contenant `"AG 18:3 c9,c12,c15 (n-3)"` (ALA), `"AG 20:5 5c,8c,11c,14c,17c (n-3)"` (EPA), `"AG 22:6 4c,7c,10c,13c,16c,19c (n-3)"` (DHA). Si ces colonnes existent, sommer. Si aucune colonne n'est trouvee → 0.

```
omega3 = ALA + EPA + DHA (en g/100g)
```

**Omega-6** : Chercher la colonne contenant `"AG 18:2 9c,12c (n-6)"` (acide linoleique). C'est le principal omega-6 alimentaire.

```
omega6 = LA (en g/100g)
```

**Si les colonnes d'acides gras specifiques ne sont pas trouvees** : mettre omega3 = 0.0 et omega6 = 0.0. L'aliment sera quand meme importe, mais ne sera pas recommande pour les deficits en omega.

### 3.4 Gestion des valeurs manquantes/speciales

La base Ciqual utilise des marqueurs speciaux au lieu de valeurs numeriques :

| Marqueur | Signification | Valeur a stocker |
|----------|---------------|-----------------|
| `-` | Donnee manquante | `0.0` |
| `traces` ou `< X` | Quantite negligeable | `0.0` |
| `N/A` | Non applicable | `0.0` |
| valeur vide | Pas de donnee | `0.0` |

**Regle** : Toute valeur non parseable en Double → `0.0`. Logger un warning avec le nom de l'aliment et le nom du nutriment pour le suivi qualite.

**Flag qualite** : Compter pour chaque aliment le nombre de nutriments a `0.0`. Si plus de 8 nutriments sur 16 sont a zero, tagger l'aliment en `qualiteDonnees = "FAIBLE"` dans une colonne de metadonnees (hors scope modele actuel mais utile pour le futur). Au MVP, ne pas bloquer l'import — simplement logger.

### 3.5 Detection du regime compatible (vegan/vegetarien)

La base Ciqual ne contient PAS d'info sur le regime. La detection est basee sur la **categorie** (groupe/sous-groupe d'aliment).

#### Regles de classification

```
SI la categorie contient un des termes suivants → NON VEGAN, NON VEGETARIEN :
  "viande", "boeuf", "veau", "porc", "agneau", "mouton", "cheval",
  "volaille", "poulet", "dinde", "canard", "lapin",
  "poisson", "saumon", "thon", "sardine", "cabillaud", "merlu",
  "crustace", "crevette", "homard", "crabe",
  "mollusque", "moule", "huitre", "calamar",
  "charcuterie", "jambon", "saucisse", "pate", "boudin",
  "abats", "foie", "rognon",
  "gibier", "chevreuil", "sanglier"
  → regimesCompatibles = [OMNIVORE, FLEXITARIEN]

SI la categorie contient un des termes suivants → NON VEGAN, mais VEGETARIEN :
  "lait", "fromage", "yaourt", "beurre", "creme",
  "oeuf", "egg",
  "miel"
  → regimesCompatibles = [VEGETARIEN, FLEXITARIEN, OMNIVORE]

SINON → probablement vegetal :
  → regimesCompatibles = [VEGAN, VEGETARIEN, FLEXITARIEN, OMNIVORE]
```

**Attention** : Cette heuristique est approximative. Certains aliments transformes contiennent des ingredients animaux non detectables par la categorie (ex: certaines margarines avec du lactoserum). Au MVP, c'est acceptable — Open Food Facts (DATA-02) apportera les tags vegan/vegetarien pour les produits industriels.

**Recherche insensible a la casse et aux accents** : Normaliser les categories et les termes de recherche (lowercase, supprimer les accents) avant la comparaison.

### 3.6 Script d'import — Comportement

- **Idempotent** : Le script peut etre relance sans creer de doublons. Utiliser `sourceId` (code Ciqual) comme cle de deduplication.
- **Strategie** : `INSERT ON CONFLICT (source, source_id) DO UPDATE` — si l'aliment existe deja, mettre a jour les valeurs nutritionnelles.
- **ID** : Generer un UUID pour chaque aliment a l'insertion. Si l'aliment existe deja (par source + sourceId), conserver l'UUID existant.
- **Marque** : Toujours `null` pour les aliments Ciqual (ce sont des aliments generiques, pas des marques).
- **Code-barres** : Toujours `null` pour les aliments Ciqual.

### 3.7 Indexation Meilisearch apres import

Apres l'import PostgreSQL, indexer dans Meilisearch :

```json
{
  "index": "aliments",
  "primaryKey": "id",
  "searchableAttributes": ["nom", "categorie", "marque"],
  "filterableAttributes": ["regimesCompatibles", "categorie", "source"],
  "sortableAttributes": ["nom"],
  "synonyms": {
    "tomate": ["tomates"],
    "pomme": ["pommes"],
    "carotte": ["carottes"],
    "haricot": ["haricots"],
    "lentille": ["lentilles"],
    "pois chiche": ["pois chiches"],
    "noix": ["noix de grenoble"],
    "riz": ["riz blanc", "riz complet", "riz basmati"]
  },
  "stopWords": ["de", "du", "des", "le", "la", "les", "au", "aux", "un", "une", "en", "et"]
}
```

---

## 4. SYNC-01/02 — Synchronisation offline/online

### 4.1 Entites synchronisees

| Entite | Direction | Strategie |
|--------|-----------|-----------|
| `JournalEntry` | Bidirectionnelle (offline-first) | Creer/modifier offline → push vers serveur |
| `HistoriquePoids` | Bidirectionnelle (offline-first) | Creer offline → push vers serveur |
| `HydratationJournaliere` | Bidirectionnelle (offline-first) | Creer/modifier offline → push vers serveur |
| `Aliment` | Serveur → local (cache) | Pull uniquement, pas de creation offline |
| `Recette` | Serveur → local (cache) | Pull uniquement, pas de creation offline |
| `QuotaJournalier` | Serveur → local (cache) | Pull uniquement, modifications via API |
| `UserProfile` | Serveur → local (cache) | Pull uniquement, modifications via API |
| `UserPreferences` | Serveur → local (cache) | Pull uniquement, modifications via API |
| `PortionStandard` | Serveur → local (cache) | Pull uniquement |

**Regle** : Seuls les 3 premiers (journal, poids, hydratation) supportent la creation offline et utilisent la `sync_queue`. Les autres sont des caches en lecture seule mis a jour via `GET /sync/pull`.

### 4.2 Strategie d'ID

Les entites offline-first utilisent un **UUID genere cote client** :

```kotlin
// Au moment de la creation locale
val id = UUID.randomUUID().toString() // kotlin.uuid ou expect/actual
```

Le serveur accepte cet ID (`val id: String?` dans les requests). Comportement :

- Si `id` est fourni → le serveur utilise cet ID (pour l'idempotence)
- Si `id` est null → le serveur genere un UUID (cas ou le client n'est pas offline)
- Si un `id` existe deja en base → le serveur traite comme un conflit (pas d'insertion)

**Idempotence** : Si le client envoie 2 fois la meme entree (retry apres echec reseau), le serveur detecte l'ID duplique et retourne un succes sans re-inserer.

### 4.3 Structure de la sync_queue (SQLDelight)

La table `sync_queue` est deja definie dans `data-models.md` :

```sql
CREATE TABLE sync_queue (
    id TEXT NOT NULL PRIMARY KEY,        -- UUID de l'entree dans la queue
    entity_type TEXT NOT NULL,           -- "journal" | "poids" | "hydratation"
    entity_id TEXT NOT NULL,             -- UUID de l'entite (meme que dans la table locale)
    action TEXT NOT NULL,                -- "CREATE" | "UPDATE" | "DELETE"
    payload_json TEXT NOT NULL,          -- L'entite serialisee en JSON (le request DTO)
    created_at INTEGER NOT NULL,         -- epoch millis
    retry_count INTEGER NOT NULL DEFAULT 0,
    last_error TEXT                      -- Message d'erreur de la derniere tentative
);
```

#### Actions

| action | Quand | Payload |
|--------|-------|---------|
| `CREATE` | L'utilisateur cree une entree offline | `AddJournalEntryRequest` / `AddPoidsRequest` / `AddHydratationRequest` serialise en JSON |
| `UPDATE` | L'utilisateur modifie une entree existante offline | `UpdateJournalEntryRequest` serialise en JSON |
| `DELETE` | L'utilisateur supprime une entree offline | `{ "id": "..." }` |

### 4.4 Workflow de synchronisation

#### Push (local → serveur)

```
1. ConnectivityMonitor detecte le retour de connexion
   (ou l'app passe au foreground et a une connexion)

2. SyncManager.pushPendingEntries() :
   a. Lire toutes les entrees de sync_queue triees par created_at ASC
   b. Regrouper par entity_type
   c. Construire un SyncPushRequest :
      {
        journalEntries: [...],     // toutes les entrees journal PENDING
        poidsEntries: [...],       // toutes les entrees poids PENDING
        hydratationEntries: [...]  // toutes les entrees hydratation PENDING
      }
   d. Appeler POST /sync/push

3. Traiter la reponse SyncPushResponse :
   a. Pour chaque entree accepted :
      - Mettre a jour sync_status = SYNCED dans la table locale
      - Supprimer l'entree de sync_queue
   b. Pour chaque conflit :
      - Appliquer la resolution serveur (SERVER_WINS = last-write-wins)
      - Mettre a jour l'entree locale avec la version serveur
      - Mettre sync_status = SYNCED
      - Supprimer l'entree de sync_queue
   c. Pour chaque erreur :
      - Incrementer retry_count dans sync_queue
      - Stocker le message d'erreur dans last_error
      - Si retry_count >= 5 : marquer comme sync_status = CONFLICT
        et ne plus retenter (l'utilisateur devra resoudre manuellement)

4. Enchainer avec un Pull (voir ci-dessous)
```

#### Pull (serveur → local)

```
1. Apres un push reussi (ou au lancement de l'app si connexion disponible)

2. SyncManager.pullUpdates() :
   a. Lire le lastSyncTimestamp stocke localement
      (SharedPreferences / NSUserDefaults via expect/actual)
   b. Appeler GET /sync/pull?since={lastSyncTimestamp}

3. Traiter la reponse SyncPullResponse :
   a. journalEntries : upsert dans local_journal_entry (sync_status = SYNCED)
   b. poidsEntries : upsert dans local_poids (sync_status = SYNCED)
   c. hydratationEntries : upsert dans local_hydratation (sync_status = SYNCED)
   d. quotas : upsert dans local_quota
   e. profile : upsert dans local_user (si non null)
   f. preferences : upsert dans local_preferences (si non null)

4. Stocker le nouveau timestamp : response.timestamp → lastSyncTimestamp
```

#### Resolution de conflits (last-write-wins)

```
Pour chaque entree en conflit :
  SI client.updatedAt > server.updatedAt → CLIENT_WINS
  SINON → SERVER_WINS (defaut)
```

**Au MVP, le serveur impose toujours SERVER_WINS** (simplifie l'implementation). Le `clientVersion` et `serverVersion` dans `SyncConflict` sont retournes pour information/debug, mais la resolution est toujours en faveur du serveur.

**Justification** : Pour un journal alimentaire, les conflits sont rares (un seul utilisateur par compte). Le cas typique est : l'utilisateur saisit offline, puis modifie la meme entree en ligne depuis un autre device. Le serveur a la version la plus recente.

### 4.5 ConnectivityMonitor — expect/actual

```kotlin
// shared/src/commonMain/kotlin/com/appfood/shared/sync/ConnectivityMonitor.kt
expect class ConnectivityMonitor {
    fun isConnected(): Boolean
    fun observeConnectivity(): Flow<Boolean>  // emet true/false a chaque changement
}

// Android : ConnectivityManager + NetworkCallback
// iOS : NWPathMonitor
```

### 4.6 Declencheurs de synchronisation

| Evenement | Action sync |
|-----------|-------------|
| App passe au foreground | Pull (si connecte et > 5 min depuis dernier pull) |
| Connexion retrouvee | Push (si sync_queue non vide) puis Pull |
| Nouvelle entree journal/poids/hydratation (si connecte) | Push immediat de l'entree |
| Nouvelle entree journal/poids/hydratation (si offline) | Ajouter a sync_queue, push au retour connexion |
| Pull periodique | Toutes les 15 minutes si l'app est au premier plan et connecte |
| Login | Pull complet (since = epoch 0) |

### 4.7 Indicateurs UI

| Etat | Affichage |
|------|-----------|
| Connecte, sync_queue vide | Rien (normal) |
| Connecte, sync en cours | Indicateur discret dans la toolbar (spinner petit) |
| Hors ligne | Bandeau `OfflineBanner` : "Mode hors ligne — tes donnees seront synchronisees" |
| Sync terminee | Rien (le bandeau disparait) |
| Conflit non resolu (retry_count >= 5) | Badge sur le profil, message dans les reglages |

---

## 5. LEGAL-04 — Chiffrement des donnees sensibles

### 5.1 Donnees concernees

Les donnees de sante (article 9 du RGPD) doivent etre chiffrees au repos :

| Table | Colonnes a chiffrer |
|-------|-------------------|
| `users` / `user_profiles` | `poids`, `taille`, `date_naissance`, `objectif_poids` |
| `poids_history` | `poids_kg` |
| `journal_entries` | aucune (les quantites seules ne sont pas sensibles) |
| `hydratation` | aucune |

**Regle** : On chiffre les donnees qui, prises isolement, revelent l'etat de sante d'une personne. Les quantites d'aliments consommes ne sont pas considérees comme des donnees de sante au sens du RGPD (elles le deviennent quand croisees avec le profil, mais le croisement est fait en memoire, pas stocke chiffre).

### 5.2 Algorithme cote serveur (PostgreSQL)

- **AES-256-GCM** pour le chiffrement au repos des colonnes sensibles
- Implementation dans `backend/.../security/Encryption.kt`
- Les colonnes chiffrees sont stockees en `TEXT` (base64 du ciphertext)
- Le chiffrement/dechiffrement se fait dans la couche **Service** (pas dans le DAO)

```kotlin
// Encryption.kt
object Encryption {
    private val key: SecretKey  // charge depuis variable d'environnement ENCRYPTION_KEY

    fun encrypt(plainText: String): String   // → base64(IV + ciphertext + tag)
    fun decrypt(cipherText: String): String  // → plainText
}
```

### 5.3 Gestion de la cle

- **Variable d'environnement** : `ENCRYPTION_KEY` (256 bits, base64-encoded)
- Generee une fois, stockee dans les secrets Railway (prod) et `.env` (dev)
- **Jamais dans le code source**, jamais dans les fichiers commites
- Ajout dans `docs/TODO-HUMAIN.md` : generer et configurer `ENCRYPTION_KEY`

### 5.4 Chiffrement local (SQLDelight / mobile)

- Utiliser **SQLCipher** pour chiffrer la base SQLDelight entiere
- Dependance : `net.zetetic:android-database-sqlcipher` (Android) / SQLCipher pour iOS
- La cle SQLCipher est derivee du token Firebase de l'utilisateur (ou generee au premier lancement et stockee dans le Keystore Android / Keychain iOS)

```kotlin
// DatabaseDriverFactory.kt (expect/actual)
// Android : SupportSQLiteOpenHelper.Factory avec SQLCipher
// iOS : NativeSqliteDriver avec chiffrement
```

### 5.5 Impact sur les requetes

Les colonnes chiffrees ne sont **plus filtrables ni triables en SQL**. Impact :
- `poids_history` : Trier par `created_at` (non chiffre), pas par `poids_kg` → OK
- `user_profiles` : Les recherches se font par `user_id` (non chiffre) → OK
- Le dechiffrement se fait apres le `SELECT`, dans le Service

### 5.6 Migration

- Ajouter une migration `V005__encrypt_sensitive_columns.sql` qui :
  1. Renomme les colonnes existantes (`poids` → `poids_legacy`)
  2. Ajoute les nouvelles colonnes `TEXT` (`poids_encrypted`)
  3. Un script Kotlin migre les donnees (chiffre les valeurs existantes)
  4. Supprime les colonnes legacy

---

*Ce document est un complement de phase3-backlog-rapport.md. Les agents doivent le lire avant d'implementer QUOTAS-01, RECO-01, DATA-01, SYNC-01/02 et LEGAL-04.*
