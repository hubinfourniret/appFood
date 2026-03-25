package com.appfood.shared.model

import kotlinx.serialization.Serializable

@Serializable
enum class RegimeAlimentaire {
    VEGAN, VEGETARIEN, FLEXITARIEN, OMNIVORE
}

@Serializable
enum class NiveauActivite {
    SEDENTAIRE,       // Coefficient 1.2
    LEGER,            // Coefficient 1.375
    MODERE,           // Coefficient 1.55
    ACTIF,            // Coefficient 1.725
    TRES_ACTIF        // Coefficient 1.9
}

@Serializable
enum class Sexe {
    HOMME, FEMME
}

@Serializable
enum class MealType {
    PETIT_DEJEUNER, DEJEUNER, DINER, COLLATION
}

@Serializable
enum class NutrimentType {
    CALORIES, PROTEINES, GLUCIDES, LIPIDES, FIBRES, SEL, SUCRES,
    FER, CALCIUM, ZINC, MAGNESIUM,
    VITAMINE_B12, VITAMINE_D, VITAMINE_C,
    OMEGA_3, OMEGA_6
}

@Serializable
enum class SourceAliment {
    CIQUAL, OPEN_FOOD_FACTS, MANUEL
}

@Serializable
enum class SourceRecette {
    MANUELLE, IMPORT, COMMUNAUTAIRE
}

@Serializable
enum class ConsentType {
    ANALYTICS, PUBLICITE, AMELIORATION_SERVICE
}

@Serializable
enum class NotificationType {
    RAPPEL, RECOMMANDATION, BILAN, HYDRATATION
}

@Serializable
enum class ObjectifPoids {
    PRISE_DE_MASSE,    // +300-500 kcal/jour
    MAINTIEN,          // quotas standards
    PERTE_DE_POIDS     // -300-500 kcal/jour
}

@Serializable
enum class SyncStatus {
    SYNCED, PENDING, CONFLICT
}

@Serializable
enum class Role {
    USER, ADMIN
}
