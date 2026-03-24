package com.appfood.shared.data.local

import com.appfood.shared.db.AppDatabase
import com.appfood.shared.db.Local_quota

class LocalQuotaDataSource(private val database: AppDatabase) {

    private val queries = database.quotaQueriesQueries

    fun findByUser(userId: String): List<Local_quota> {
        return queries.findByUser(userId).executeAsList()
    }

    fun insertOrReplace(quota: Local_quota) {
        queries.insertOrReplace(
            id = quota.id,
            user_id = quota.user_id,
            nutriment_type = quota.nutriment_type,
            valeur_cible = quota.valeur_cible,
            unite = quota.unite,
            est_personnalise = quota.est_personnalise,
        )
    }

    fun deleteByUser(userId: String) {
        queries.deleteByUser(userId)
    }
}
