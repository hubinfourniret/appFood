package com.appfood.backend.service

import com.appfood.backend.database.dao.HydratationDao
import com.appfood.backend.database.dao.PoidsHistoryDao
import com.appfood.backend.routes.dto.HydratationDaySummary
import com.appfood.backend.routes.dto.HydratationWeeklyResponse
import com.appfood.backend.routes.dto.NutrimentValuesResponse
import com.appfood.backend.routes.dto.WeeklySummaryResponse
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import org.slf4j.LoggerFactory

class DashboardService(
    private val quotaService: QuotaService,
    private val journalService: JournalService,
    private val hydratationDao: HydratationDao,
    private val recommandationService: RecommandationService,
    private val poidsHistoryDao: PoidsHistoryDao,
) {
    private val logger = LoggerFactory.getLogger("DashboardService")

    // In-memory recommendation cache: key = "$userId:$date"
    private val recoCache = mutableMapOf<String, CachedRecommandations>()

    /**
     * Aggregated dashboard data for a given date.
     */
    suspend fun getDashboard(
        userId: String,
        date: LocalDate,
    ): DashboardData {
        val tStart = System.currentTimeMillis()

        // 1. Quota statuses
        val t0 = System.currentTimeMillis()
        val quotasStatus =
            try {
                quotaService.getQuotaStatus(userId, date)
            } catch (e: Exception) {
                logger.warn("No quotas found for userId=$userId, returning empty list")
                emptyList()
            }
        logger.info("Dashboard/$userId quotas=${System.currentTimeMillis() - t0}ms")

        // 2. Journal entries for the day
        val t1 = System.currentTimeMillis()
        val journalEntries =
            journalService.getEntries(
                userId = userId,
                date = date,
                dateFrom = null,
                dateTo = null,
                mealTypeStr = null,
            )
        logger.info("Dashboard/$userId journal=${System.currentTimeMillis() - t1}ms")

        // 3. Hydratation (nullable)
        val t2 = System.currentTimeMillis()
        val hydratation = hydratationDao.findByUserAndDate(userId, date)
        logger.info("Dashboard/$userId hydratation=${System.currentTimeMillis() - t2}ms")

        // 4. Recommendations (cached with 30min TTL)
        val t3 = System.currentTimeMillis()
        val cached = getCachedRecommandations(userId, date)
        logger.info("Dashboard/$userId reco=${System.currentTimeMillis() - t3}ms")

        // 5. Current weight
        val t4 = System.currentTimeMillis()
        val poidsCourant = poidsHistoryDao.findLatest(userId)?.poidsKg
        logger.info("Dashboard/$userId poids=${System.currentTimeMillis() - t4}ms")

        logger.info("Dashboard/$userId total=${System.currentTimeMillis() - tStart}ms")

        return DashboardData(
            date = date,
            quotasStatus = quotasStatus,
            journalEntries = journalEntries,
            hydratationQuantiteMl = hydratation?.quantiteMl,
            hydratationObjectifMl = hydratation?.objectifMl,
            recommandationsAliments = cached.aliments,
            recommandationsRecettes = cached.recettes,
            poidsCourant = poidsCourant,
        )
    }

    /**
     * Aggregated weekly dashboard data.
     */
    suspend fun getWeeklyDashboard(
        userId: String,
        weekOf: LocalDate,
    ): WeeklyDashboardData {
        val dayOfWeek = weekOf.dayOfWeek.ordinal // Monday=0
        val monday = LocalDate.fromEpochDays(weekOf.toEpochDays() - dayOfWeek)
        val sunday = LocalDate.fromEpochDays(monday.toEpochDays() + 6)

        // 1. Weekly nutrition summary
        val weeklySummary = journalService.getWeeklySummary(userId, weekOf)

        // 2. Weekly hydratation
        val hydratationRows = hydratationDao.findByUserAndDateRange(userId, monday, sunday)
        val parJour = mutableMapOf<String, HydratationDaySummary>()
        var totalHydratation = 0
        var lastObjectif = 2000

        for (day in 0..6) {
            val d = LocalDate.fromEpochDays(monday.toEpochDays() + day)
            val row = hydratationRows.find { it.date == d }
            val quantite = row?.quantiteMl ?: 0
            val objectif = row?.objectifMl ?: lastObjectif
            if (row != null) lastObjectif = row.objectifMl
            totalHydratation += quantite
            parJour[d.toString()] =
                HydratationDaySummary(
                    quantiteMl = quantite,
                    objectifMl = objectif,
                    pourcentage = if (objectif > 0) quantite.toDouble() / objectif * 100.0 else 0.0,
                )
        }

        val hydratationHebdo =
            HydratationWeeklyResponse(
                dateFrom = monday.toString(),
                dateTo = sunday.toString(),
                moyenneJournaliereMl = totalHydratation / 7,
                objectifMl = lastObjectif,
                parJour = parJour,
            )

        // 3. Nutriments critiques: ceux dont la moyenne est < 70% du quota
        val quotas =
            try {
                quotaService.getQuotaStatus(userId, weekOf)
            } catch (e: Exception) {
                emptyList()
            }

        // Utiliser la moyenne journaliere pour identifier les nutriments critiques
        val moyenne = weeklySummary.moyenneJournaliere
        val nutrimentsCritiques = mutableListOf<String>()
        val ameliorations = mutableListOf<String>()
        val degradations = mutableListOf<String>()

        for (quota in quotas) {
            val moyenneVal = getNutrientValueFromSums(quota.nutriment.name, moyenne)
            val pourcentageMoyen = if (quota.valeurCible > 0) moyenneVal / quota.valeurCible * 100.0 else 0.0
            if (pourcentageMoyen < 70.0) {
                nutrimentsCritiques.add(quota.nutriment.name)
            }
        }

        // 4. Ameliorations/degradations: comparer premiere et deuxieme moitie de semaine
        val joursTries = weeklySummary.parJour.entries.sortedBy { it.key }
        if (joursTries.size >= 4) {
            val firstHalf = joursTries.take(joursTries.size / 2)
            val secondHalf = joursTries.drop(joursTries.size / 2)

            val avgFirst = averageNutrientSums(firstHalf.map { it.value })
            val avgSecond = averageNutrientSums(secondHalf.map { it.value })

            // Comparer calories, proteines, fibres, fer, vitamineB12
            val comparisons =
                listOf(
                    Triple("Calories", avgFirst.calories, avgSecond.calories),
                    Triple("Proteines", avgFirst.proteines, avgSecond.proteines),
                    Triple("Fibres", avgFirst.fibres, avgSecond.fibres),
                    Triple("Fer", avgFirst.fer, avgSecond.fer),
                    Triple("Vitamine B12", avgFirst.vitamineB12, avgSecond.vitamineB12),
                    Triple("Calcium", avgFirst.calcium, avgSecond.calcium),
                )

            for ((name, first, second) in comparisons) {
                if (first > 0.0) {
                    val change = (second - first) / first * 100.0
                    if (change > 15.0) {
                        ameliorations.add("$name en hausse (+${"%.0f".format(change)}%)")
                    } else if (change < -15.0) {
                        degradations.add("$name en baisse (${"%.0f".format(change)}%)")
                    }
                }
            }
        }

        // Convert WeeklySummary to WeeklySummaryResponse
        val nutritionHebdo =
            WeeklySummaryResponse(
                dateFrom = weeklySummary.dateFrom.toString(),
                dateTo = weeklySummary.dateTo.toString(),
                moyenneJournaliere = weeklySummary.moyenneJournaliere.toNutrimentValuesResponse(),
                parJour =
                    weeklySummary.parJour.map { (date, sums) ->
                        date.toString() to sums.toNutrimentValuesResponse()
                    }.toMap(),
                joursAvecSaisie = weeklySummary.joursAvecSaisie,
            )

        return WeeklyDashboardData(
            dateFrom = monday,
            dateTo = sunday,
            nutritionHebdo = nutritionHebdo,
            hydratationHebdo = hydratationHebdo,
            nutrimentsCritiques = nutrimentsCritiques,
            ameliorations = ameliorations,
            degradations = degradations,
        )
    }

    private fun getNutrientValueFromSums(
        nutrimentName: String,
        sums: NutrientSums,
    ): Double {
        return when (nutrimentName) {
            "CALORIES" -> sums.calories
            "PROTEINES" -> sums.proteines
            "GLUCIDES" -> sums.glucides
            "LIPIDES" -> sums.lipides
            "FIBRES" -> sums.fibres
            "SEL" -> sums.sel
            "SUCRES" -> sums.sucres
            "FER" -> sums.fer
            "CALCIUM" -> sums.calcium
            "ZINC" -> sums.zinc
            "MAGNESIUM" -> sums.magnesium
            "VITAMINE_B12" -> sums.vitamineB12
            "VITAMINE_D" -> sums.vitamineD
            "VITAMINE_C" -> sums.vitamineC
            "OMEGA_3" -> sums.omega3
            "OMEGA_6" -> sums.omega6
            else -> 0.0
        }
    }

    private fun averageNutrientSums(list: List<NutrientSums>): NutrientSums {
        if (list.isEmpty()) return NutrientSums()
        val n = list.size.toDouble()
        return NutrientSums(
            calories = list.sumOf { it.calories } / n,
            proteines = list.sumOf { it.proteines } / n,
            glucides = list.sumOf { it.glucides } / n,
            lipides = list.sumOf { it.lipides } / n,
            fibres = list.sumOf { it.fibres } / n,
            sel = list.sumOf { it.sel } / n,
            sucres = list.sumOf { it.sucres } / n,
            fer = list.sumOf { it.fer } / n,
            calcium = list.sumOf { it.calcium } / n,
            zinc = list.sumOf { it.zinc } / n,
            magnesium = list.sumOf { it.magnesium } / n,
            vitamineB12 = list.sumOf { it.vitamineB12 } / n,
            vitamineD = list.sumOf { it.vitamineD } / n,
            vitamineC = list.sumOf { it.vitamineC } / n,
            omega3 = list.sumOf { it.omega3 } / n,
            omega6 = list.sumOf { it.omega6 } / n,
        )
    }

    private fun NutrientSums.toNutrimentValuesResponse() =
        NutrimentValuesResponse(
            calories = calories,
            proteines = proteines,
            glucides = glucides,
            lipides = lipides,
            fibres = fibres,
            sel = sel,
            sucres = sucres,
            fer = fer,
            calcium = calcium,
            zinc = zinc,
            magnesium = magnesium,
            vitamineB12 = vitamineB12,
            vitamineD = vitamineD,
            vitamineC = vitamineC,
            omega3 = omega3,
            omega6 = omega6,
        )

    /**
     * Invalidate the recommendation cache for a user.
     * Called after journal entry add/update/delete or quota change.
     */
    fun invalidateCache(userId: String) {
        val keysToRemove = recoCache.keys.filter { it.startsWith("$userId:") }
        keysToRemove.forEach { recoCache.remove(it) }
        if (keysToRemove.isNotEmpty()) {
            logger.info("Cache invalidated for userId=$userId, keys=${keysToRemove.size}")
        }
    }

    private suspend fun getCachedRecommandations(
        userId: String,
        date: LocalDate,
    ): CachedRecommandations {
        val cacheKey = "$userId:$date"
        val now = Clock.System.now()
        val existing = recoCache[cacheKey]

        if (existing != null && (now - existing.timestamp).inWholeMinutes < TTL_MINUTES) {
            return existing
        }

        // Compute fresh recommendations with graceful degradation on timeout
        val alimentResult =
            try {
                withTimeoutOrNull(RECO_TIMEOUT_MS) {
                    recommandationService.getRecommandationsAliments(userId, date, limit = 5)
                } ?: run {
                    logger.warn("Recommandation aliments timeout after ${RECO_TIMEOUT_MS}ms, returning empty")
                    RecommandationAlimentResult(emptyList(), emptyList())
                }
            } catch (e: Exception) {
                logger.warn("Failed to compute aliment recommendations for userId=$userId: ${e.message}")
                RecommandationAlimentResult(emptyList(), emptyList())
            }

        val recetteResult =
            try {
                withTimeoutOrNull(RECO_TIMEOUT_MS) {
                    recommandationService.getRecommandationsRecettes(userId, date, limit = 3)
                } ?: run {
                    logger.warn("Recommandation recettes timeout after ${RECO_TIMEOUT_MS}ms, returning empty")
                    RecommandationRecetteResult(emptyList(), emptyList())
                }
            } catch (e: Exception) {
                logger.warn("Failed to compute recette recommendations for userId=$userId: ${e.message}")
                RecommandationRecetteResult(emptyList(), emptyList())
            }

        val cached =
            CachedRecommandations(
                aliments = alimentResult,
                recettes = recetteResult,
                timestamp = now,
            )
        recoCache[cacheKey] = cached

        // Evict stale entries to prevent memory leak
        evictStaleEntries()

        return cached
    }

    private fun evictStaleEntries() {
        val now = Clock.System.now()
        val staleKeys =
            recoCache.entries
                .filter { (now - it.value.timestamp).inWholeMinutes >= TTL_MINUTES * 2 }
                .map { it.key }
        staleKeys.forEach { recoCache.remove(it) }
    }

    companion object {
        private const val TTL_MINUTES = 30L
        private const val RECO_TIMEOUT_MS = 5_000L
    }
}

// --- Internal data classes ---

private data class CachedRecommandations(
    val aliments: RecommandationAlimentResult,
    val recettes: RecommandationRecetteResult,
    val timestamp: kotlinx.datetime.Instant,
)

data class DashboardData(
    val date: LocalDate,
    val quotasStatus: List<QuotaStatusResult>,
    val journalEntries: List<com.appfood.backend.database.dao.JournalEntryRow>,
    val hydratationQuantiteMl: Int?,
    val hydratationObjectifMl: Int?,
    val recommandationsAliments: RecommandationAlimentResult,
    val recommandationsRecettes: RecommandationRecetteResult,
    val poidsCourant: Double?,
)

data class WeeklyDashboardData(
    val dateFrom: LocalDate,
    val dateTo: LocalDate,
    val nutritionHebdo: WeeklySummaryResponse,
    val hydratationHebdo: HydratationWeeklyResponse,
    val nutrimentsCritiques: List<String>,
    val ameliorations: List<String>,
    val degradations: List<String>,
)
