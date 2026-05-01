package com.appfood.backend.service

import com.appfood.backend.database.dao.AlimentDao
import com.appfood.backend.database.dao.AlimentRow
import com.appfood.backend.database.dao.JournalEntryDao
import com.appfood.backend.database.dao.JournalEntryRow
import com.appfood.backend.database.dao.RecetteDao
import com.appfood.backend.database.dao.RecetteRow
import com.appfood.backend.database.tables.MealType
import com.appfood.backend.plugins.NotFoundException
import com.appfood.backend.plugins.ValidationException
import com.appfood.backend.security.toEnumOrThrow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import org.slf4j.LoggerFactory
import java.util.UUID

class JournalService(
    private val journalEntryDao: JournalEntryDao,
    private val alimentDao: AlimentDao,
    private val recetteDao: RecetteDao,
) {
    private val logger = LoggerFactory.getLogger("JournalService")

    suspend fun getEntries(
        userId: String,
        date: LocalDate?,
        dateFrom: LocalDate?,
        dateTo: LocalDate?,
        mealTypeStr: String?,
    ): List<JournalEntryRow> {
        val mealType = mealTypeStr?.toEnumOrThrow<MealType>("mealType")

        val entries =
            when {
                dateFrom != null && dateTo != null -> {
                    if (dateFrom > dateTo) {
                        throw ValidationException("dateFrom doit etre avant dateTo")
                    }
                    journalEntryDao.findByUserAndDateRange(userId, dateFrom, dateTo)
                }
                date != null -> journalEntryDao.findByUserAndDate(userId, date)
                else -> journalEntryDao.findByUserAndDate(userId, LocalDate.Companion.parse(todayString()))
            }

        return if (mealType != null) {
            entries.filter { it.mealType == mealType }
        } else {
            entries
        }
    }

    suspend fun addEntry(
        userId: String,
        idParam: String?,
        dateStr: String,
        mealTypeStr: String,
        alimentId: String?,
        recetteId: String?,
        quantiteGrammes: Double?,
        nbPortions: Double?,
        ingredientOverrides: Map<String, Double>? = null,
    ): JournalEntryRow {
        // Validate alimentId XOR recetteId
        if (alimentId == null && recetteId == null) {
            throw ValidationException("alimentId ou recetteId est requis")
        }
        if (alimentId != null && recetteId != null) {
            throw ValidationException("alimentId et recetteId sont mutuellement exclusifs")
        }

        val mealType = mealTypeStr.toEnumOrThrow<MealType>("mealType")
        val date = parseDate(dateStr)
        val id = idParam ?: UUID.randomUUID().toString()
        val now = Clock.System.now()

        if (alimentId != null) {
            if (quantiteGrammes == null || quantiteGrammes <= 0.0) {
                throw ValidationException("quantiteGrammes doit etre > 0 pour un aliment")
            }
            val aliment =
                alimentDao.findById(alimentId)
                    ?: throw NotFoundException("Aliment non trouve: $alimentId")

            val nutrients = calculateNutrientsFromAliment(aliment, quantiteGrammes)

            val row =
                JournalEntryRow(
                    id = id,
                    userId = userId,
                    date = date,
                    mealType = mealType,
                    alimentId = alimentId,
                    recetteId = null,
                    nom = aliment.nom,
                    quantiteGrammes = quantiteGrammes,
                    nbPortions = null,
                    calories = nutrients.calories,
                    proteines = nutrients.proteines,
                    glucides = nutrients.glucides,
                    lipides = nutrients.lipides,
                    fibres = nutrients.fibres,
                    sel = nutrients.sel,
                    sucres = nutrients.sucres,
                    fer = nutrients.fer,
                    calcium = nutrients.calcium,
                    zinc = nutrients.zinc,
                    magnesium = nutrients.magnesium,
                    vitamineB12 = nutrients.vitamineB12,
                    vitamineD = nutrients.vitamineD,
                    vitamineC = nutrients.vitamineC,
                    omega3 = nutrients.omega3,
                    omega6 = nutrients.omega6,
                    createdAt = now,
                    updatedAt = now,
                )
            logger.info("AddEntry: aliment=$alimentId, quantity=$quantiteGrammes, userId=$userId")
            return journalEntryDao.insert(row)
        } else {
            // recetteId case
            if (nbPortions == null || nbPortions <= 0.0) {
                throw ValidationException("nbPortions doit etre > 0 pour une recette")
            }
            val recette =
                recetteDao.findById(recetteId!!)
                    ?: throw NotFoundException("Recette non trouvee: $recetteId")

            val nutrients =
                if (!ingredientOverrides.isNullOrEmpty()) {
                    calculateNutrientsFromRecetteWithOverrides(recette, nbPortions, ingredientOverrides)
                } else {
                    calculateNutrientsFromRecette(recette, nbPortions)
                }
            val quantiteGrammesCalc =
                if (recette.nbPortions > 0) {
                    nbPortions / recette.nbPortions * 100.0
                } else {
                    100.0
                }

            val row =
                JournalEntryRow(
                    id = id,
                    userId = userId,
                    date = date,
                    mealType = mealType,
                    alimentId = null,
                    recetteId = recetteId,
                    nom = recette.nom,
                    quantiteGrammes = quantiteGrammesCalc,
                    nbPortions = nbPortions,
                    calories = nutrients.calories,
                    proteines = nutrients.proteines,
                    glucides = nutrients.glucides,
                    lipides = nutrients.lipides,
                    fibres = nutrients.fibres,
                    sel = nutrients.sel,
                    sucres = nutrients.sucres,
                    fer = nutrients.fer,
                    calcium = nutrients.calcium,
                    zinc = nutrients.zinc,
                    magnesium = nutrients.magnesium,
                    vitamineB12 = nutrients.vitamineB12,
                    vitamineD = nutrients.vitamineD,
                    vitamineC = nutrients.vitamineC,
                    omega3 = nutrients.omega3,
                    omega6 = nutrients.omega6,
                    createdAt = now,
                    updatedAt = now,
                )
            logger.info("AddEntry: recette=$recetteId, portions=$nbPortions, userId=$userId")
            return journalEntryDao.insert(row)
        }
    }

    suspend fun updateEntry(
        userId: String,
        entryId: String,
        quantiteGrammes: Double?,
        nbPortions: Double?,
        mealTypeStr: String?,
        ingredientOverrides: Map<String, Double>? = null,
    ): JournalEntryRow {
        val existing =
            journalEntryDao.findById(entryId, userId)
                ?: throw NotFoundException("Entree de journal non trouvee")

        val newMealType = mealTypeStr?.toEnumOrThrow<MealType>("mealType") ?: existing.mealType

        // Recalculate nutrients if quantity changed
        val updatedRow =
            if (existing.alimentId != null) {
                val newQuantite = quantiteGrammes ?: existing.quantiteGrammes
                if (newQuantite <= 0.0) {
                    throw ValidationException("quantiteGrammes doit etre > 0")
                }
                if (newQuantite != existing.quantiteGrammes) {
                    val aliment =
                        alimentDao.findById(existing.alimentId)
                            ?: throw NotFoundException("Aliment non trouve: ${existing.alimentId}")
                    val nutrients = calculateNutrientsFromAliment(aliment, newQuantite)
                    existing.copy(
                        mealType = newMealType,
                        quantiteGrammes = newQuantite,
                        calories = nutrients.calories,
                        proteines = nutrients.proteines,
                        glucides = nutrients.glucides,
                        lipides = nutrients.lipides,
                        fibres = nutrients.fibres,
                        sel = nutrients.sel,
                        sucres = nutrients.sucres,
                        fer = nutrients.fer,
                        calcium = nutrients.calcium,
                        zinc = nutrients.zinc,
                        magnesium = nutrients.magnesium,
                        vitamineB12 = nutrients.vitamineB12,
                        vitamineD = nutrients.vitamineD,
                        vitamineC = nutrients.vitamineC,
                        omega3 = nutrients.omega3,
                        omega6 = nutrients.omega6,
                    )
                } else {
                    existing.copy(mealType = newMealType)
                }
            } else {
                // Recette entry
                val newPortions = nbPortions ?: existing.nbPortions ?: 1.0
                if (newPortions <= 0.0) {
                    throw ValidationException("nbPortions doit etre > 0")
                }
                val portionsChanged = newPortions != existing.nbPortions
                val hasOverrides = !ingredientOverrides.isNullOrEmpty()
                if (portionsChanged || hasOverrides) {
                    val recette =
                        recetteDao.findById(existing.recetteId!!)
                            ?: throw NotFoundException("Recette non trouvee: ${existing.recetteId}")
                    val nutrients = if (hasOverrides) {
                        calculateNutrientsFromRecetteWithOverrides(recette, newPortions, ingredientOverrides!!)
                    } else {
                        calculateNutrientsFromRecette(recette, newPortions)
                    }
                    val quantiteGrammesCalc =
                        if (recette.nbPortions > 0) {
                            newPortions / recette.nbPortions * 100.0
                        } else {
                            100.0
                        }
                    existing.copy(
                        mealType = newMealType,
                        quantiteGrammes = quantiteGrammesCalc,
                        nbPortions = newPortions,
                        calories = nutrients.calories,
                        proteines = nutrients.proteines,
                        glucides = nutrients.glucides,
                        lipides = nutrients.lipides,
                        fibres = nutrients.fibres,
                        sel = nutrients.sel,
                        sucres = nutrients.sucres,
                        fer = nutrients.fer,
                        calcium = nutrients.calcium,
                        zinc = nutrients.zinc,
                        magnesium = nutrients.magnesium,
                        vitamineB12 = nutrients.vitamineB12,
                        vitamineD = nutrients.vitamineD,
                        vitamineC = nutrients.vitamineC,
                        omega3 = nutrients.omega3,
                        omega6 = nutrients.omega6,
                    )
                } else {
                    existing.copy(mealType = newMealType)
                }
            }

        journalEntryDao.update(updatedRow)
        logger.info("UpdateEntry: id=$entryId, userId=$userId")
        return journalEntryDao.findById(entryId, userId)!!
    }

    suspend fun deleteEntry(
        userId: String,
        entryId: String,
    ) {
        val exists =
            journalEntryDao.findById(entryId, userId)
                ?: throw NotFoundException("Entree de journal non trouvee")
        journalEntryDao.delete(entryId, userId)
        logger.info("DeleteEntry: id=$entryId, userId=$userId")
    }

    suspend fun getDailySummary(
        userId: String,
        date: LocalDate,
    ): DailySummary {
        val entries = journalEntryDao.findByUserAndDate(userId, date)
        val total = sumNutrients(entries)
        val parRepas =
            entries.groupBy { it.mealType }
                .mapValues { (_, rows) -> sumNutrients(rows) }
        return DailySummary(
            date = date,
            totalNutriments = total,
            parRepas = parRepas,
            nbEntrees = entries.size,
        )
    }

    suspend fun getWeeklySummary(
        userId: String,
        weekOf: LocalDate,
    ): WeeklySummary {
        // Compute Monday-Sunday for the given week
        val dayOfWeek = weekOf.dayOfWeek.ordinal // Monday=0
        val monday = LocalDate.fromEpochDays(weekOf.toEpochDays() - dayOfWeek)
        val sunday = LocalDate.fromEpochDays(monday.toEpochDays() + 6)

        val entries = journalEntryDao.findByUserAndDateRange(userId, monday, sunday)
        val parJour = mutableMapOf<LocalDate, NutrientSums>()

        for (day in 0..6) {
            val d = LocalDate.fromEpochDays(monday.toEpochDays() + day)
            val dayEntries = entries.filter { it.date == d }
            if (dayEntries.isNotEmpty()) {
                parJour[d] = sumNutrients(dayEntries)
            }
        }

        val joursAvecSaisie = parJour.size
        val moyenne =
            if (joursAvecSaisie > 0) {
                averageNutrients(parJour.values.toList())
            } else {
                NutrientSums()
            }

        return WeeklySummary(
            dateFrom = monday,
            dateTo = sunday,
            moyenneJournaliere = moyenne,
            parJour = parJour,
            joursAvecSaisie = joursAvecSaisie,
        )
    }

    suspend fun getRecentAlimentIds(
        userId: String,
        limit: Int,
    ): List<String> {
        return journalEntryDao.findRecentAlimentIds(userId, limit)
    }

    // --- Nutrient calculation helpers ---

    private fun calculateNutrientsFromAliment(
        aliment: AlimentRow,
        quantiteGrammes: Double,
    ): NutrientSums {
        val factor = quantiteGrammes / 100.0
        return NutrientSums(
            calories = aliment.calories * factor,
            proteines = aliment.proteines * factor,
            glucides = aliment.glucides * factor,
            lipides = aliment.lipides * factor,
            fibres = aliment.fibres * factor,
            sel = aliment.sel * factor,
            sucres = aliment.sucres * factor,
            fer = aliment.fer * factor,
            calcium = aliment.calcium * factor,
            zinc = aliment.zinc * factor,
            magnesium = aliment.magnesium * factor,
            vitamineB12 = aliment.vitamineB12 * factor,
            vitamineD = aliment.vitamineD * factor,
            vitamineC = aliment.vitamineC * factor,
            omega3 = aliment.omega3 * factor,
            omega6 = aliment.omega6 * factor,
        )
    }

    /**
     * Calcule les nutriments d'une recette en remplaçant la quantité de certains
     * ingrédients par les valeurs d'override fournies par l'utilisateur.
     * key = ingredient.id, valeur = grammes réels pour cette saisie.
     */
    private suspend fun calculateNutrientsFromRecetteWithOverrides(
        recette: RecetteRow,
        nbPortions: Double,
        overrides: Map<String, Double>,
    ): NutrientSums {
        val factor = if (recette.nbPortions > 0) nbPortions / recette.nbPortions else 1.0
        val ingredients = recetteDao.findIngredientsByRecetteId(recette.id)
        var sums = NutrientSums()
        for (ing in ingredients) {
            val grams = overrides[ing.id] ?: (ing.quantiteGrammes * factor)
            if (grams <= 0.0) continue
            val aliment = alimentDao.findById(ing.alimentId) ?: continue
            val n = calculateNutrientsFromAliment(aliment, grams)
            sums = NutrientSums(
                calories = sums.calories + n.calories,
                proteines = sums.proteines + n.proteines,
                glucides = sums.glucides + n.glucides,
                lipides = sums.lipides + n.lipides,
                fibres = sums.fibres + n.fibres,
                sel = sums.sel + n.sel,
                sucres = sums.sucres + n.sucres,
                fer = sums.fer + n.fer,
                calcium = sums.calcium + n.calcium,
                zinc = sums.zinc + n.zinc,
                magnesium = sums.magnesium + n.magnesium,
                vitamineB12 = sums.vitamineB12 + n.vitamineB12,
                vitamineD = sums.vitamineD + n.vitamineD,
                vitamineC = sums.vitamineC + n.vitamineC,
                omega3 = sums.omega3 + n.omega3,
                omega6 = sums.omega6 + n.omega6,
            )
        }
        return sums
    }

    private fun calculateNutrientsFromRecette(
        recette: RecetteRow,
        nbPortions: Double,
    ): NutrientSums {
        val factor = if (recette.nbPortions > 0) nbPortions / recette.nbPortions else 1.0
        return NutrientSums(
            calories = recette.calories * factor,
            proteines = recette.proteines * factor,
            glucides = recette.glucides * factor,
            lipides = recette.lipides * factor,
            fibres = recette.fibres * factor,
            sel = recette.sel * factor,
            sucres = recette.sucres * factor,
            fer = recette.fer * factor,
            calcium = recette.calcium * factor,
            zinc = recette.zinc * factor,
            magnesium = recette.magnesium * factor,
            vitamineB12 = recette.vitamineB12 * factor,
            vitamineD = recette.vitamineD * factor,
            vitamineC = recette.vitamineC * factor,
            omega3 = recette.omega3 * factor,
            omega6 = recette.omega6 * factor,
        )
    }

    private fun sumNutrients(entries: List<JournalEntryRow>): NutrientSums {
        return entries.fold(NutrientSums()) { acc, e ->
            NutrientSums(
                calories = acc.calories + e.calories,
                proteines = acc.proteines + e.proteines,
                glucides = acc.glucides + e.glucides,
                lipides = acc.lipides + e.lipides,
                fibres = acc.fibres + e.fibres,
                sel = acc.sel + e.sel,
                sucres = acc.sucres + e.sucres,
                fer = acc.fer + e.fer,
                calcium = acc.calcium + e.calcium,
                zinc = acc.zinc + e.zinc,
                magnesium = acc.magnesium + e.magnesium,
                vitamineB12 = acc.vitamineB12 + e.vitamineB12,
                vitamineD = acc.vitamineD + e.vitamineD,
                vitamineC = acc.vitamineC + e.vitamineC,
                omega3 = acc.omega3 + e.omega3,
                omega6 = acc.omega6 + e.omega6,
            )
        }
    }

    private fun averageNutrients(sums: List<NutrientSums>): NutrientSums {
        if (sums.isEmpty()) return NutrientSums()
        val n = sums.size.toDouble()
        val total =
            sums.fold(NutrientSums()) { acc, s ->
                NutrientSums(
                    calories = acc.calories + s.calories,
                    proteines = acc.proteines + s.proteines,
                    glucides = acc.glucides + s.glucides,
                    lipides = acc.lipides + s.lipides,
                    fibres = acc.fibres + s.fibres,
                    sel = acc.sel + s.sel,
                    sucres = acc.sucres + s.sucres,
                    fer = acc.fer + s.fer,
                    calcium = acc.calcium + s.calcium,
                    zinc = acc.zinc + s.zinc,
                    magnesium = acc.magnesium + s.magnesium,
                    vitamineB12 = acc.vitamineB12 + s.vitamineB12,
                    vitamineD = acc.vitamineD + s.vitamineD,
                    vitamineC = acc.vitamineC + s.vitamineC,
                    omega3 = acc.omega3 + s.omega3,
                    omega6 = acc.omega6 + s.omega6,
                )
            }
        return NutrientSums(
            calories = total.calories / n,
            proteines = total.proteines / n,
            glucides = total.glucides / n,
            lipides = total.lipides / n,
            fibres = total.fibres / n,
            sel = total.sel / n,
            sucres = total.sucres / n,
            fer = total.fer / n,
            calcium = total.calcium / n,
            zinc = total.zinc / n,
            magnesium = total.magnesium / n,
            vitamineB12 = total.vitamineB12 / n,
            vitamineD = total.vitamineD / n,
            vitamineC = total.vitamineC / n,
            omega3 = total.omega3 / n,
            omega6 = total.omega6 / n,
        )
    }

    private fun parseDate(dateStr: String): LocalDate {
        return try {
            LocalDate.parse(dateStr)
        } catch (e: Exception) {
            throw ValidationException("Format de date invalide: '$dateStr'. Attendu: YYYY-MM-DD")
        }
    }

    private fun todayString(): String {
        return Clock.System.now().toString().substringBefore("T")
    }
}

/**
 * Intermediate data class for nutrient sums used in summaries.
 */
data class NutrientSums(
    val calories: Double = 0.0,
    val proteines: Double = 0.0,
    val glucides: Double = 0.0,
    val lipides: Double = 0.0,
    val fibres: Double = 0.0,
    val sel: Double = 0.0,
    val sucres: Double = 0.0,
    val fer: Double = 0.0,
    val calcium: Double = 0.0,
    val zinc: Double = 0.0,
    val magnesium: Double = 0.0,
    val vitamineB12: Double = 0.0,
    val vitamineD: Double = 0.0,
    val vitamineC: Double = 0.0,
    val omega3: Double = 0.0,
    val omega6: Double = 0.0,
)

data class DailySummary(
    val date: LocalDate,
    val totalNutriments: NutrientSums,
    val parRepas: Map<MealType, NutrientSums>,
    val nbEntrees: Int,
)

data class WeeklySummary(
    val dateFrom: LocalDate,
    val dateTo: LocalDate,
    val moyenneJournaliere: NutrientSums,
    val parJour: Map<LocalDate, NutrientSums>,
    val joursAvecSaisie: Int,
)
