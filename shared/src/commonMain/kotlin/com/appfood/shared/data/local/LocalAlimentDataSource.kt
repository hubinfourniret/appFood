package com.appfood.shared.data.local

import com.appfood.shared.db.AppDatabase
import com.appfood.shared.db.Local_aliment

class LocalAlimentDataSource(private val database: AppDatabase) {

    private val queries = database.alimentQueriesQueries

    fun findById(id: String): Local_aliment? {
        return queries.findById(id).executeAsOneOrNull()
    }

    fun search(query: String): List<Local_aliment> {
        return queries.search("%$query%").executeAsList()
    }

    fun findByCategorie(categorie: String): List<Local_aliment> {
        return queries.findByCategorie(categorie).executeAsList()
    }

    fun insertOrReplace(aliment: Local_aliment) {
        queries.insertOrReplace(
            id = aliment.id,
            nom = aliment.nom,
            marque = aliment.marque,
            categorie = aliment.categorie,
            source = aliment.source,
            source_id = aliment.source_id,
            calories = aliment.calories,
            proteines = aliment.proteines,
            glucides = aliment.glucides,
            lipides = aliment.lipides,
            fibres = aliment.fibres,
            sel = aliment.sel,
            sucres = aliment.sucres,
            fer = aliment.fer,
            calcium = aliment.calcium,
            zinc = aliment.zinc,
            magnesium = aliment.magnesium,
            vitamine_b12 = aliment.vitamine_b12,
            vitamine_d = aliment.vitamine_d,
            vitamine_c = aliment.vitamine_c,
            omega_3 = aliment.omega_3,
            omega_6 = aliment.omega_6,
            regimes_compatibles = aliment.regimes_compatibles,
        )
    }
}
