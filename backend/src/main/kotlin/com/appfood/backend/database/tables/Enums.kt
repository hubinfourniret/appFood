package com.appfood.backend.database.tables

/**
 * Typealias vers les enums definis dans le module shared (source de verite unique).
 * Le backend depend de :shared via le JVM target — plus besoin de duplication.
 *
 * Ces typealias permettent aux Tables Exposed et aux imports existants
 * (com.appfood.backend.database.tables.NutrimentType, etc.) de continuer a compiler
 * sans modifier les ~55 fichiers qui les referent.
 */

typealias Role = com.appfood.shared.model.Role
typealias Sexe = com.appfood.shared.model.Sexe
typealias RegimeAlimentaire = com.appfood.shared.model.RegimeAlimentaire
typealias NiveauActivite = com.appfood.shared.model.NiveauActivite
typealias MealType = com.appfood.shared.model.MealType
typealias NutrimentType = com.appfood.shared.model.NutrimentType
typealias SourceAliment = com.appfood.shared.model.SourceAliment
typealias SourceRecette = com.appfood.shared.model.SourceRecette
typealias ConsentType = com.appfood.shared.model.ConsentType
typealias NotificationType = com.appfood.shared.model.NotificationType
typealias ObjectifPoids = com.appfood.shared.model.ObjectifPoids
