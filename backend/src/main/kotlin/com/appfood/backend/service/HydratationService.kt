package com.appfood.backend.service

import com.appfood.backend.database.dao.HydratationDao
import com.appfood.backend.database.dao.HydratationEntryRow
import com.appfood.backend.database.dao.HydratationRow
import com.appfood.backend.database.dao.UserProfileDao
import com.appfood.backend.database.tables.NiveauActivite
import com.appfood.backend.plugins.NotFoundException
import com.appfood.backend.plugins.ValidationException
import com.appfood.backend.routes.dto.AddHydratationRequest
import com.appfood.backend.routes.dto.HydratationDaySummary
import com.appfood.backend.routes.dto.HydratationEntryResponse
import com.appfood.backend.routes.dto.HydratationResponse
import com.appfood.backend.routes.dto.HydratationWeeklyResponse
import com.appfood.backend.routes.dto.UpdateHydratationObjectifRequest
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import org.slf4j.LoggerFactory
import java.util.UUID

class HydratationService(
    private val hydratationDao: HydratationDao,
    private val userProfileDao: UserProfileDao,
) {
    private val logger = LoggerFactory.getLogger("HydratationService")

    /**
     * Retourne le cumul d'hydratation + entrees pour une date donnee.
     */
    suspend fun getDaily(userId: String, date: LocalDate): HydratationResponse {
        val row = hydratationDao.findByUserAndDate(userId, date)
            ?: return emptyDayResponse(userId, date)

        val entries = hydratationDao.findEntriesByHydratationId(row.id)
        return row.toResponse(entries)
    }

    /**
     * Retourne le resume hebdomadaire d'hydratation.
     */
    suspend fun getWeekly(userId: String, weekOf: LocalDate): HydratationWeeklyResponse {
        val dayOfWeek = weekOf.dayOfWeek.ordinal // Monday=0
        val monday = LocalDate.fromEpochDays(weekOf.toEpochDays() - dayOfWeek)
        val sunday = LocalDate.fromEpochDays(monday.toEpochDays() + 6)

        val rows = hydratationDao.findByUserAndDateRange(userId, monday, sunday)
        val defaultObjectif = computeDefaultObjectif(userId)

        val parJour = mutableMapOf<String, HydratationDaySummary>()
        var totalMl = 0

        for (day in 0..6) {
            val d = LocalDate.fromEpochDays(monday.toEpochDays() + day)
            val row = rows.find { it.date == d }
            val quantite = row?.quantiteMl ?: 0
            val objectif = row?.objectifMl ?: defaultObjectif
            totalMl += quantite
            parJour[d.toString()] = HydratationDaySummary(
                quantiteMl = quantite,
                objectifMl = objectif,
                pourcentage = if (objectif > 0) quantite.toDouble() / objectif * 100.0 else 0.0,
            )
        }

        val joursAvecDonnees = rows.size.coerceAtLeast(1)
        val objectifActuel = rows.lastOrNull()?.objectifMl ?: defaultObjectif

        return HydratationWeeklyResponse(
            dateFrom = monday.toString(),
            dateTo = sunday.toString(),
            moyenneJournaliereMl = totalMl / 7,
            objectifMl = objectifActuel,
            parJour = parJour,
        )
    }

    /**
     * Ajoute une entree d'hydratation. Auto-cree la journee si besoin.
     */
    suspend fun addEntry(userId: String, request: AddHydratationRequest): HydratationResponse {
        val date = parseDate(request.date)
        if (request.quantiteMl <= 0) {
            throw ValidationException("quantiteMl doit etre > 0")
        }

        val now = Clock.System.now()
        var row = hydratationDao.findByUserAndDate(userId, date)

        if (row == null) {
            // Auto-creation de la journee
            val defaultObjectif = computeDefaultObjectif(userId)
            row = hydratationDao.insert(
                HydratationRow(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    date = date,
                    quantiteMl = 0,
                    objectifMl = defaultObjectif,
                    estObjectifPersonnalise = false,
                    updatedAt = now,
                ),
            )
        }

        // Ajouter l'entree
        val entryId = request.id ?: UUID.randomUUID().toString()
        hydratationDao.insertEntry(
            HydratationEntryRow(
                id = entryId,
                hydratationId = row.id,
                heure = now,
                quantiteMl = request.quantiteMl,
            ),
        )

        // Incrementer le cumul
        val newTotal = row.quantiteMl + request.quantiteMl
        hydratationDao.updateQuantite(row.id, userId, newTotal)

        logger.info("AddHydratation: userId=$userId, date=$date, +${request.quantiteMl}ml, total=${newTotal}ml")

        // Retourner le jour mis a jour
        return getDaily(userId, date)
    }

    /**
     * Met a jour l'objectif d'hydratation personnalise.
     */
    suspend fun updateObjectif(userId: String, request: UpdateHydratationObjectifRequest): HydratationResponse {
        if (request.objectifMl <= 0) {
            throw ValidationException("objectifMl doit etre > 0")
        }

        val today = todayDate()
        var row = hydratationDao.findByUserAndDate(userId, today)

        if (row == null) {
            row = hydratationDao.insert(
                HydratationRow(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    date = today,
                    quantiteMl = 0,
                    objectifMl = request.objectifMl,
                    estObjectifPersonnalise = true,
                    updatedAt = Clock.System.now(),
                ),
            )
        } else {
            hydratationDao.updateObjectif(row.id, userId, request.objectifMl, true)
        }

        logger.info("UpdateObjectif: userId=$userId, objectifMl=${request.objectifMl}")
        return getDaily(userId, today)
    }

    /**
     * Reinitialise l'objectif a la valeur automatique basee sur le poids et l'activite.
     */
    suspend fun resetObjectif(userId: String): HydratationResponse {
        val today = todayDate()
        val defaultObjectif = computeDefaultObjectif(userId)

        val row = hydratationDao.findByUserAndDate(userId, today)
        if (row != null) {
            hydratationDao.updateObjectif(row.id, userId, defaultObjectif, false)
        } else {
            hydratationDao.insert(
                HydratationRow(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    date = today,
                    quantiteMl = 0,
                    objectifMl = defaultObjectif,
                    estObjectifPersonnalise = false,
                    updatedAt = Clock.System.now(),
                ),
            )
        }

        logger.info("ResetObjectif: userId=$userId, objectifMl=$defaultObjectif")
        return getDaily(userId, today)
    }

    /**
     * Calcule l'objectif par defaut: poids * coefficient ml/kg selon le niveau d'activite.
     */
    internal suspend fun computeDefaultObjectif(userId: String): Int {
        val profile = userProfileDao.findByUserId(userId)
        if (profile == null) {
            return DEFAULT_OBJECTIF_ML
        }

        val mlPerKg = when (profile.niveauActivite) {
            NiveauActivite.SEDENTAIRE -> 30
            NiveauActivite.LEGER -> 31
            NiveauActivite.MODERE -> 32
            NiveauActivite.ACTIF -> 34
            NiveauActivite.TRES_ACTIF -> 35
        }

        return (profile.poidsKg * mlPerKg).toInt()
    }

    private suspend fun emptyDayResponse(userId: String, date: LocalDate): HydratationResponse {
        val defaultObjectif = computeDefaultObjectif(userId)
        return HydratationResponse(
            id = "",
            date = date.toString(),
            quantiteMl = 0,
            objectifMl = defaultObjectif,
            estObjectifPersonnalise = false,
            pourcentage = 0.0,
            entrees = emptyList(),
        )
    }

    private fun HydratationRow.toResponse(entries: List<HydratationEntryRow>): HydratationResponse {
        val pourcentage = if (objectifMl > 0) quantiteMl.toDouble() / objectifMl * 100.0 else 0.0
        return HydratationResponse(
            id = id,
            date = date.toString(),
            quantiteMl = quantiteMl,
            objectifMl = objectifMl,
            estObjectifPersonnalise = estObjectifPersonnalise,
            pourcentage = pourcentage,
            entrees = entries.map {
                HydratationEntryResponse(
                    id = it.id,
                    heure = it.heure.toString(),
                    quantiteMl = it.quantiteMl,
                )
            },
        )
    }

    private fun parseDate(dateStr: String): LocalDate {
        return try {
            LocalDate.parse(dateStr)
        } catch (e: Exception) {
            throw ValidationException("Format de date invalide: '$dateStr'. Attendu: YYYY-MM-DD")
        }
    }

    private fun todayDate(): LocalDate {
        val todayStr = Clock.System.now().toString().substringBefore("T")
        return LocalDate.parse(todayStr)
    }

    companion object {
        private const val DEFAULT_OBJECTIF_ML = 2000
    }
}
