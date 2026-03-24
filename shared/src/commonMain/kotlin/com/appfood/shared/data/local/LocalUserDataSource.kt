package com.appfood.shared.data.local

import com.appfood.shared.db.AppDatabase
import com.appfood.shared.db.Local_user

class LocalUserDataSource(private val database: AppDatabase) {

    private val queries = database.userQueriesQueries

    fun findById(id: String): Local_user? {
        return queries.findById(id).executeAsOneOrNull()
    }

    fun insertOrReplace(user: Local_user) {
        queries.insertOrReplace(
            id = user.id,
            email = user.email,
            nom = user.nom,
            prenom = user.prenom,
            sexe = user.sexe,
            date_naissance = user.date_naissance,
            taille_cm = user.taille_cm,
            poids_kg = user.poids_kg,
            niveau_activite = user.niveau_activite,
            regime_alimentaire = user.regime_alimentaire,
            role = user.role,
        )
    }
}
