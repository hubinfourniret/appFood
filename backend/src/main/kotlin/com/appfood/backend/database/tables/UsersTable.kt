package com.appfood.backend.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object UsersTable : Table("users") {
    val id = varchar("id", 36)
    val email = varchar("email", 255).uniqueIndex()
    val nom = varchar("nom", 100).nullable()
    val prenom = varchar("prenom", 100).nullable()
    val role = enumerationByName<Role>("role", 10).default(Role.USER)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    override val primaryKey = PrimaryKey(id)
}

object UserProfilesTable : Table("user_profiles") {
    val userId = varchar("user_id", 36).references(UsersTable.id)
    val sexe = enumerationByName<Sexe>("sexe", 10)
    val age = integer("age")
    val poidsKg = double("poids_kg")
    val tailleCm = integer("taille_cm")
    val regimeAlimentaire = enumerationByName<RegimeAlimentaire>("regime_alimentaire", 20)
    val niveauActivite = enumerationByName<NiveauActivite>("niveau_activite", 20)
    val onboardingComplete = bool("onboarding_complete").default(false)
    val objectifPoids = enumerationByName<ObjectifPoids>("objectif_poids", 20).nullable()
    val updatedAt = timestamp("updated_at")
    override val primaryKey = PrimaryKey(userId)
}

object UserPreferencesTable : Table("user_preferences") {
    val userId = varchar("user_id", 36).references(UsersTable.id)
    val alimentsExclus = text("aliments_exclus")
    val allergies = text("allergies")
    val alimentsFavoris = text("aliments_favoris")
    val updatedAt = timestamp("updated_at")
    override val primaryKey = PrimaryKey(userId)
}
