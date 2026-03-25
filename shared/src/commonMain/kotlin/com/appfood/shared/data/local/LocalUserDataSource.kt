package com.appfood.shared.data.local

import com.appfood.shared.db.AppDatabase
import com.appfood.shared.db.Local_preferences
import com.appfood.shared.db.Local_user
import com.appfood.shared.db.Local_user_profile

class LocalUserDataSource(private val database: AppDatabase) {

    private val queries = database.userQueriesQueries

    // ===== User =====

    fun findById(id: String): Local_user? {
        return queries.findById(id).executeAsOneOrNull()
    }

    fun insertOrReplace(user: Local_user) {
        queries.insertOrReplace(
            id = user.id,
            email = user.email,
            nom = user.nom,
            prenom = user.prenom,
            role = user.role,
            created_at = user.created_at,
            updated_at = user.updated_at,
        )
    }

    fun deleteById(id: String) {
        queries.deleteById(id)
    }

    fun deleteAll() {
        queries.deleteAll()
    }

    // ===== Profile =====

    fun findProfileByUserId(userId: String): Local_user_profile? {
        return queries.findProfileByUserId(userId).executeAsOneOrNull()
    }

    fun insertOrReplaceProfile(profile: Local_user_profile) {
        queries.insertOrReplaceProfile(
            user_id = profile.user_id,
            sexe = profile.sexe,
            age = profile.age,
            poids_kg = profile.poids_kg,
            taille_cm = profile.taille_cm,
            regime_alimentaire = profile.regime_alimentaire,
            niveau_activite = profile.niveau_activite,
            onboarding_complete = profile.onboarding_complete,
            objectif_poids = profile.objectif_poids,
            updated_at = profile.updated_at,
        )
    }

    fun deleteProfileByUserId(userId: String) {
        queries.deleteProfileByUserId(userId)
    }

    // ===== Preferences =====

    fun findPreferencesByUserId(userId: String): Local_preferences? {
        return queries.findPreferencesByUserId(userId).executeAsOneOrNull()
    }

    fun insertOrReplacePreferences(preferences: Local_preferences) {
        queries.insertOrReplacePreferences(
            user_id = preferences.user_id,
            allergies = preferences.allergies,
            aliments_exclus = preferences.aliments_exclus,
            aliments_favoris = preferences.aliments_favoris,
            notifications_actives = preferences.notifications_actives,
            rappel_hydratation = preferences.rappel_hydratation,
            heure_rappel_matin = preferences.heure_rappel_matin,
            heure_rappel_soir = preferences.heure_rappel_soir,
            updated_at = preferences.updated_at,
        )
    }

    fun deletePreferencesByUserId(userId: String) {
        queries.deletePreferencesByUserId(userId)
    }
}
