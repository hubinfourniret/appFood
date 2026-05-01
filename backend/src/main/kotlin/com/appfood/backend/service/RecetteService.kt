package com.appfood.backend.service

import com.appfood.backend.database.dao.AlimentDao
import com.appfood.backend.database.dao.AlimentRow
import com.appfood.backend.database.dao.IngredientRow
import com.appfood.backend.database.dao.RecetteDao
import com.appfood.backend.database.dao.RecetteRow
import com.appfood.backend.database.dao.UserDao
import com.appfood.backend.database.tables.MealType
import com.appfood.backend.database.tables.RegimeAlimentaire
import com.appfood.backend.database.tables.Role
import com.appfood.backend.database.tables.SourceRecette
import com.appfood.backend.plugins.ForbiddenException
import com.appfood.backend.plugins.NotFoundException
import com.appfood.backend.plugins.ValidationException
import com.appfood.backend.routes.dto.CreateRecetteRequest
import com.appfood.backend.routes.dto.UpdateRecetteRequest
import com.appfood.backend.security.toEnumOrThrow
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.util.UUID

class RecetteService(
    private val recetteDao: RecetteDao,
    private val alimentDao: AlimentDao,
    private val userDao: UserDao,
) {
    private val logger = LoggerFactory.getLogger("RecetteService")
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun listRecettes(
        regime: String?,
        typeRepas: String?,
        sort: String?,
        query: String?,
        page: Int,
        size: Int,
        currentUserId: String? = null,
    ): Pair<List<RecetteWithIngredients>, Int> {
        // Validate enums if provided
        regime?.toEnumOrThrow<RegimeAlimentaire>("regime")
        typeRepas?.toEnumOrThrow<MealType>("typeRepas")

        val pageIndex = (page - 1).coerceAtLeast(0)
        val offset = pageIndex.toLong() * size

        val t0 = System.currentTimeMillis()
        val (rows, total) = recetteDao.findPublishedPaginated(
            regime = regime,
            typeRepas = typeRepas,
            query = query,
            sort = sort,
            limit = size,
            offset = offset,
            currentUserId = currentUserId,
        )
        logger.info("listRecettes/query=${System.currentTimeMillis() - t0}ms total=$total returned=${rows.size}")

        if (rows.isEmpty()) {
            logger.info("listRecettes/total=${System.currentTimeMillis() - t0}ms")
            return Pair(emptyList(), total.toInt())
        }

        val t1 = System.currentTimeMillis()
        val ingredientsByRecetteId = recetteDao.findIngredientsByRecetteIds(rows.map { it.id })
        logger.info("listRecettes/ingredients=${System.currentTimeMillis() - t1}ms")

        val result = rows.map { recette ->
            RecetteWithIngredients(recette, ingredientsByRecetteId[recette.id] ?: emptyList())
        }

        val tTotal = System.currentTimeMillis() - t0
        logger.info("listRecettes/total=${tTotal}ms")

        return Pair(result, total.toInt())
    }

    suspend fun getRecetteDetail(id: String): RecetteWithIngredients {
        val recette =
            recetteDao.findById(id)
                ?: throw NotFoundException("Recette non trouvee: $id")
        val ingredients = recetteDao.findIngredientsByRecetteId(id)
        // Charger les aliments pour exposer les nutriments par 100g (TACHE-518 — preview live)
        val alimentsById =
            alimentDao.findByIds(ingredients.map { it.alimentId }.distinct())
                .associateBy { it.id }
        return RecetteWithIngredients(recette, ingredients, alimentsById)
    }

    suspend fun createRecette(
        userId: String,
        request: CreateRecetteRequest,
    ): RecetteWithIngredients {
        // TACHE-516 : tout utilisateur peut creer une recette personnelle.
        // Seul un admin peut publier (publie=true visible par tous).
        val isAdmin = isAdmin(userId)
        validateCreateRequest(request)
        val effectivePublie = if (isAdmin) request.publie else false
        val ownerUserId = if (isAdmin && request.publie) null else userId

        val now = Clock.System.now()
        val recetteId = UUID.randomUUID().toString()

        // Resolve ingredients and calculate total nutrients
        val ingredientRows = mutableListOf<IngredientRow>()
        var totalNutrients = NutrientSums()

        for (ingredient in request.ingredients) {
            val aliment =
                alimentDao.findById(ingredient.alimentId)
                    ?: throw NotFoundException("Aliment non trouve: ${ingredient.alimentId}")

            val factor = ingredient.quantiteGrammes / 100.0
            totalNutrients = addAlimentNutrients(totalNutrients, aliment, factor)

            ingredientRows.add(
                IngredientRow(
                    id = UUID.randomUUID().toString(),
                    recetteId = recetteId,
                    alimentId = aliment.id,
                    alimentNom = aliment.nom,
                    quantiteGrammes = ingredient.quantiteGrammes,
                ),
            )
        }

        val regimesJson = request.regimesCompatibles.joinToString(",")
        val typeRepasJson = request.typeRepas.joinToString(",")
        val etapesJson = request.etapes.joinToString("|||")

        val recetteRow =
            RecetteRow(
                id = recetteId,
                nom = request.nom,
                description = request.description,
                tempsPreparationMin = request.tempsPreparationMin,
                tempsCuissonMin = request.tempsCuissonMin,
                nbPortions = request.nbPortions,
                regimesCompatibles = regimesJson,
                source = SourceRecette.MANUELLE,
                typeRepas = typeRepasJson,
                etapes = etapesJson,
                calories = totalNutrients.calories,
                proteines = totalNutrients.proteines,
                glucides = totalNutrients.glucides,
                lipides = totalNutrients.lipides,
                fibres = totalNutrients.fibres,
                sel = totalNutrients.sel,
                sucres = totalNutrients.sucres,
                fer = totalNutrients.fer,
                calcium = totalNutrients.calcium,
                zinc = totalNutrients.zinc,
                magnesium = totalNutrients.magnesium,
                vitamineB12 = totalNutrients.vitamineB12,
                vitamineD = totalNutrients.vitamineD,
                vitamineC = totalNutrients.vitamineC,
                omega3 = totalNutrients.omega3,
                omega6 = totalNutrients.omega6,
                imageUrl = request.imageUrl,
                publie = effectivePublie,
                userId = ownerUserId,
                createdAt = now,
                updatedAt = now,
            )

        recetteDao.insert(recetteRow)
        ingredientRows.forEach { recetteDao.insertIngredient(it) }

        logger.info("CreateRecette: id=$recetteId, nom=${request.nom}, userId=$userId")
        return RecetteWithIngredients(recetteRow, ingredientRows)
    }

    suspend fun updateRecette(
        userId: String,
        recetteId: String,
        request: UpdateRecetteRequest,
    ): RecetteWithIngredients {
        val existing =
            recetteDao.findById(recetteId)
                ?: throw NotFoundException("Recette non trouvee: $recetteId")

        // TACHE-516 : owner ou admin peut modifier
        checkCanEditRecette(userId, existing)

        // Validate enums if provided
        request.regimesCompatibles?.forEach { it.toEnumOrThrow<RegimeAlimentaire>("regimesCompatibles") }
        request.typeRepas?.forEach { it.toEnumOrThrow<MealType>("typeRepas") }
        request.nbPortions?.let {
            if (it < 1) throw ValidationException("nbPortions doit etre >= 1")
        }
        request.tempsPreparationMin?.let {
            if (it < 0) throw ValidationException("tempsPreparationMin doit etre >= 0")
        }
        request.tempsCuissonMin?.let {
            if (it < 0) throw ValidationException("tempsCuissonMin doit etre >= 0")
        }

        // If ingredients are updated, recalculate nutrients
        var updatedNutrients: NutrientSums? = null
        var newIngredientRows: List<IngredientRow>? = null

        if (request.ingredients != null) {
            request.ingredients.forEach { ing ->
                if (ing.quantiteGrammes <= 0.0) {
                    throw ValidationException("quantiteGrammes doit etre > 0 pour chaque ingredient")
                }
            }
            var totalNutrients = NutrientSums()
            val rows = mutableListOf<IngredientRow>()
            for (ingredient in request.ingredients) {
                val aliment =
                    alimentDao.findById(ingredient.alimentId)
                        ?: throw NotFoundException("Aliment non trouve: ${ingredient.alimentId}")
                val factor = ingredient.quantiteGrammes / 100.0
                totalNutrients = addAlimentNutrients(totalNutrients, aliment, factor)
                rows.add(
                    IngredientRow(
                        id = UUID.randomUUID().toString(),
                        recetteId = recetteId,
                        alimentId = aliment.id,
                        alimentNom = aliment.nom,
                        quantiteGrammes = ingredient.quantiteGrammes,
                    ),
                )
            }
            updatedNutrients = totalNutrients
            newIngredientRows = rows
        }

        val updatedRow =
            existing.copy(
                nom = request.nom ?: existing.nom,
                description = request.description ?: existing.description,
                tempsPreparationMin = request.tempsPreparationMin ?: existing.tempsPreparationMin,
                tempsCuissonMin = request.tempsCuissonMin ?: existing.tempsCuissonMin,
                nbPortions = request.nbPortions ?: existing.nbPortions,
                regimesCompatibles = request.regimesCompatibles?.joinToString(",") ?: existing.regimesCompatibles,
                typeRepas = request.typeRepas?.joinToString(",") ?: existing.typeRepas,
                etapes = request.etapes?.joinToString("|||") ?: existing.etapes,
                calories = updatedNutrients?.calories ?: existing.calories,
                proteines = updatedNutrients?.proteines ?: existing.proteines,
                glucides = updatedNutrients?.glucides ?: existing.glucides,
                lipides = updatedNutrients?.lipides ?: existing.lipides,
                fibres = updatedNutrients?.fibres ?: existing.fibres,
                sel = updatedNutrients?.sel ?: existing.sel,
                sucres = updatedNutrients?.sucres ?: existing.sucres,
                fer = updatedNutrients?.fer ?: existing.fer,
                calcium = updatedNutrients?.calcium ?: existing.calcium,
                zinc = updatedNutrients?.zinc ?: existing.zinc,
                magnesium = updatedNutrients?.magnesium ?: existing.magnesium,
                vitamineB12 = updatedNutrients?.vitamineB12 ?: existing.vitamineB12,
                vitamineD = updatedNutrients?.vitamineD ?: existing.vitamineD,
                vitamineC = updatedNutrients?.vitamineC ?: existing.vitamineC,
                omega3 = updatedNutrients?.omega3 ?: existing.omega3,
                omega6 = updatedNutrients?.omega6 ?: existing.omega6,
                imageUrl = request.imageUrl ?: existing.imageUrl,
                publie = request.publie ?: existing.publie,
            )

        recetteDao.update(updatedRow)

        // Replace ingredients if provided
        if (newIngredientRows != null) {
            recetteDao.deleteIngredientsByRecetteId(recetteId)
            newIngredientRows.forEach { recetteDao.insertIngredient(it) }
        }

        logger.info("UpdateRecette: id=$recetteId, userId=$userId")
        val finalIngredients = recetteDao.findIngredientsByRecetteId(recetteId)
        return RecetteWithIngredients(
            recetteDao.findById(recetteId)!!,
            finalIngredients,
        )
    }

    suspend fun deleteRecette(
        userId: String,
        recetteId: String,
    ) {
        val existing =
            recetteDao.findById(recetteId)
                ?: throw NotFoundException("Recette non trouvee: $recetteId")

        checkCanEditRecette(userId, existing)

        recetteDao.deleteIngredientsByRecetteId(recetteId)
        recetteDao.delete(recetteId)
        logger.info("DeleteRecette: id=$recetteId, userId=$userId")
    }

    /**
     * TACHE-516 : liste les recettes personnelles d'un utilisateur.
     */
    suspend fun listMyRecettes(userId: String): List<RecetteWithIngredients> {
        val rows = recetteDao.findByUserId(userId)
        if (rows.isEmpty()) return emptyList()
        val ingredients = recetteDao.findIngredientsByRecetteIds(rows.map { it.id })
        return rows.map { row ->
            RecetteWithIngredients(row, ingredients[row.id].orEmpty())
        }
    }

    private suspend fun checkAdmin(userId: String) {
        val user =
            userDao.findById(userId)
                ?: throw NotFoundException("Utilisateur non trouve")
        if (user.role != Role.ADMIN) {
            throw ForbiddenException("Acces reserve aux administrateurs")
        }
    }

    private suspend fun isAdmin(userId: String): Boolean {
        val user = userDao.findById(userId) ?: return false
        return user.role == Role.ADMIN
    }

    /**
     * Verifie que l'utilisateur peut modifier/supprimer la recette : owner OU admin.
     */
    private suspend fun checkCanEditRecette(userId: String, recette: RecetteRow) {
        if (recette.userId == userId) return
        if (isAdmin(userId)) return
        throw ForbiddenException("Acces refuse a cette recette")
    }

    private fun validateCreateRequest(request: CreateRecetteRequest) {
        if (request.nom.isBlank()) throw ValidationException("Le nom est requis")
        if (request.description.isBlank()) throw ValidationException("La description est requise")
        if (request.nbPortions < 1) throw ValidationException("nbPortions doit etre >= 1")
        if (request.tempsPreparationMin < 0) throw ValidationException("tempsPreparationMin doit etre >= 0")
        if (request.tempsCuissonMin < 0) throw ValidationException("tempsCuissonMin doit etre >= 0")
        if (request.ingredients.isEmpty()) throw ValidationException("Au moins un ingredient est requis")
        if (request.etapes.isEmpty()) throw ValidationException("Au moins une etape est requise")
        request.regimesCompatibles.forEach { it.toEnumOrThrow<RegimeAlimentaire>("regimesCompatibles") }
        request.typeRepas.forEach { it.toEnumOrThrow<MealType>("typeRepas") }
        request.ingredients.forEach { ing ->
            if (ing.quantiteGrammes <= 0.0) {
                throw ValidationException("quantiteGrammes doit etre > 0 pour chaque ingredient")
            }
        }
    }

    private fun addAlimentNutrients(
        sums: NutrientSums,
        aliment: AlimentRow,
        factor: Double,
    ): NutrientSums {
        return NutrientSums(
            calories = sums.calories + aliment.calories * factor,
            proteines = sums.proteines + aliment.proteines * factor,
            glucides = sums.glucides + aliment.glucides * factor,
            lipides = sums.lipides + aliment.lipides * factor,
            fibres = sums.fibres + aliment.fibres * factor,
            sel = sums.sel + aliment.sel * factor,
            sucres = sums.sucres + aliment.sucres * factor,
            fer = sums.fer + aliment.fer * factor,
            calcium = sums.calcium + aliment.calcium * factor,
            zinc = sums.zinc + aliment.zinc * factor,
            magnesium = sums.magnesium + aliment.magnesium * factor,
            vitamineB12 = sums.vitamineB12 + aliment.vitamineB12 * factor,
            vitamineD = sums.vitamineD + aliment.vitamineD * factor,
            vitamineC = sums.vitamineC + aliment.vitamineC * factor,
            omega3 = sums.omega3 + aliment.omega3 * factor,
            omega6 = sums.omega6 + aliment.omega6 * factor,
        )
    }
}

data class RecetteWithIngredients(
    val recette: RecetteRow,
    val ingredients: List<IngredientRow>,
    /** Nutriments par 100g de chaque aliment ingrédient (key = alimentId). Vide si non charge. */
    val alimentsById: Map<String, AlimentRow> = emptyMap(),
)
