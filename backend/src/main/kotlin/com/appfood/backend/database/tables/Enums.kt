package com.appfood.backend.database.tables

/**
 * Enums utilises par les tables Exposed.
 * Dupliques depuis shared/model/Enums.kt car le backend ne depend pas du module shared.
 * Source de verite : docs/data-models.md section 1.
 */

enum class Role { USER, ADMIN }

enum class Sexe { HOMME, FEMME }

enum class RegimeAlimentaire { VEGAN, VEGETARIEN, FLEXITARIEN, OMNIVORE }

enum class NiveauActivite { SEDENTAIRE, LEGER, MODERE, ACTIF, TRES_ACTIF }

enum class MealType { PETIT_DEJEUNER, DEJEUNER, DINER, COLLATION }

enum class NutrimentType {
    CALORIES, PROTEINES, GLUCIDES, LIPIDES, FIBRES, SEL, SUCRES,
    FER, CALCIUM, ZINC, MAGNESIUM,
    VITAMINE_B12, VITAMINE_D, VITAMINE_C,
    OMEGA_3, OMEGA_6,
}

enum class SourceAliment { CIQUAL, OPEN_FOOD_FACTS, MANUEL }

enum class SourceRecette { MANUELLE, IMPORT, COMMUNAUTAIRE }

enum class ConsentType { ANALYTICS, PUBLICITE, AMELIORATION_SERVICE }

enum class NotificationType { RAPPEL, RECOMMANDATION, BILAN, HYDRATATION }

enum class ObjectifPoids { PRISE_DE_MASSE, MAINTIEN, PERTE_DE_POIDS }
