package com.appfood.shared.model

import kotlinx.serialization.Serializable

@Serializable
enum class RegimeAlimentaire {
    VEGAN, VEGETARIEN, FLEXITARIEN, OMNIVORE
}

@Serializable
enum class NiveauActivite {
    SEDENTAIRE, LEGER, MODERE, ACTIF, TRES_ACTIF
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
enum class SyncStatus {
    SYNCED, PENDING, CONFLICT
}

@Serializable
enum class Role {
    USER, ADMIN
}
