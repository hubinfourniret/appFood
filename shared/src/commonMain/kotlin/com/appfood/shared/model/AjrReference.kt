package com.appfood.shared.model

import kotlinx.serialization.Serializable

/**
 * Reference AJR/ANC (Apports Journaliers Recommandes / Apports Nutritionnels Conseilles).
 * Values are based on ANSES RNP (References Nutritionnelles pour la Population)
 * and AS (Apports Satisfaisants).
 *
 * These references are seeded in PostgreSQL by the DATA agent (V002__seed_ajr.sql).
 * The lookup logic by profile (sexe, age, regime) is in CalculerQuotasUseCase (Sprint 2).
 */
@Serializable
data class AjrReference(
    val sexe: Sexe,
    val ageMin: Int,
    val ageMax: Int,
    val nutriment: NutrimentType,
    val valeur: Double,
    val unite: String,
    val source: String,
)

/**
 * Coefficients d'ajustement par regime alimentaire.
 * Certains nutriments ont une biodisponibilite reduite dans les sources vegetales.
 * Ex: fer non-heminique (vegetal) = absorption 5-12% vs 15-35% pour le fer heminique.
 */
@Serializable
data class CoefficientRegime(
    val regimeAlimentaire: RegimeAlimentaire,
    val nutriment: NutrimentType,
    val coefficient: Double,
)
