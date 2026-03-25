package com.appfood.shared.data.local

import com.appfood.shared.db.AppDatabase
import com.appfood.shared.db.Local_portion

class LocalPortionDataSource(private val database: AppDatabase) {

    private val queries = database.portionQueriesQueries

    fun findByAlimentId(alimentId: String): List<Local_portion> {
        return queries.findByAlimentId(alimentId).executeAsList()
    }

    fun findGeneriques(): List<Local_portion> {
        return queries.findGeneriques().executeAsList()
    }

    fun insertOrReplace(portion: Local_portion) {
        queries.insertOrReplace(
            id = portion.id,
            aliment_id = portion.aliment_id,
            nom = portion.nom,
            quantite_grammes = portion.quantite_grammes,
            est_generique = portion.est_generique,
        )
    }
}
