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
            handle = user.handle,
            bio = user.bio,
            date_naissance = user.date_naissance,
            social_visibility = user.social_visibility,
        )
    }

    /**
     * TACHE-600 : etat social du user en cache (pour decider de la redirection onboarding social).
     */
    data class CachedSocialState(
        val userId: String,
        val handle: String?,
        val bio: String?,
        val dateNaissance: String?,
        val socialVisibility: String,
    )

    fun findSocialState(): CachedSocialState? {
        val row = queries.findSocialState().executeAsOneOrNull() ?: return null
        return CachedSocialState(
            userId = row.id,
            handle = row.handle,
            bio = row.bio,
            dateNaissance = row.date_naissance,
            socialVisibility = row.social_visibility,
        )
    }

    fun deleteById(id: String) {
        queries.deleteById(id)
    }

    fun findAll(): List<Local_user> {
        return queries.findAll().executeAsList()
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

    fun deleteAllProfiles() {
        queries.deleteAllProfiles()
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

    fun deleteAllPreferences() {
        queries.deleteAllPreferences()
    }

    // ===== Auth Token =====

    fun getAuthToken(): String? {
        return queries.getAuthToken().executeAsOneOrNull()
    }

    fun getAuthUserId(): String? {
        return queries.getAuthTokenWithUserId().executeAsOneOrNull()?.user_id
    }

    fun saveAuthToken(userId: String, token: String) {
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        queries.insertOrReplaceAuthToken(
            user_id = userId,
            token = token,
            created_at = now,
        )
    }

    fun deleteAuthToken() {
        queries.deleteAuthToken()
    }
}
