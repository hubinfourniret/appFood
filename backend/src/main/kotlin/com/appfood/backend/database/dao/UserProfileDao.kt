package com.appfood.backend.database.dao

import com.appfood.backend.database.dbQuery
import com.appfood.backend.database.tables.NiveauActivite
import com.appfood.backend.database.tables.ObjectifPoids
import com.appfood.backend.database.tables.RegimeAlimentaire
import com.appfood.backend.database.tables.Sexe
import com.appfood.backend.database.tables.UserProfilesTable
import com.appfood.backend.security.EncryptionService
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

data class UserProfileRow(
    val userId: String,
    val sexe: Sexe,
    val age: Int,
    val poidsKg: Double,
    val tailleCm: Int,
    val regimeAlimentaire: RegimeAlimentaire,
    val niveauActivite: NiveauActivite,
    val onboardingComplete: Boolean,
    val objectifPoids: ObjectifPoids?,
    val updatedAt: kotlinx.datetime.Instant,
)

class UserProfileDao(
    private val encryptionService: EncryptionService,
) {

    suspend fun findByUserId(userId: String): UserProfileRow? = dbQuery {
        UserProfilesTable.selectAll()
            .where { UserProfilesTable.userId eq userId }
            .map { it.toRow() }
            .singleOrNull()
    }

    suspend fun insert(
        userId: String,
        sexe: Sexe,
        age: Int,
        poidsKg: Double,
        tailleCm: Int,
        regimeAlimentaire: RegimeAlimentaire,
        niveauActivite: NiveauActivite,
        onboardingComplete: Boolean = true,
    ): UserProfileRow = dbQuery {
        val now = Clock.System.now()
        UserProfilesTable.insert {
            it[UserProfilesTable.userId] = userId
            it[UserProfilesTable.sexe] = sexe
            it[UserProfilesTable.age] = encryptionService.encryptInt(age)
            it[UserProfilesTable.poidsKg] = encryptionService.encryptDouble(poidsKg)
            it[UserProfilesTable.tailleCm] = encryptionService.encryptInt(tailleCm)
            it[UserProfilesTable.regimeAlimentaire] = regimeAlimentaire
            it[UserProfilesTable.niveauActivite] = niveauActivite
            it[UserProfilesTable.onboardingComplete] = onboardingComplete
            it[UserProfilesTable.updatedAt] = now
        }
        UserProfileRow(userId, sexe, age, poidsKg, tailleCm, regimeAlimentaire, niveauActivite, onboardingComplete, null, now)
    }

    suspend fun update(
        userId: String,
        sexe: Sexe,
        age: Int,
        poidsKg: Double,
        tailleCm: Int,
        regimeAlimentaire: RegimeAlimentaire,
        niveauActivite: NiveauActivite,
        onboardingComplete: Boolean,
        objectifPoids: ObjectifPoids?,
    ): Boolean = dbQuery {
        UserProfilesTable.update({ UserProfilesTable.userId eq userId }) {
            it[UserProfilesTable.sexe] = sexe
            it[UserProfilesTable.age] = encryptionService.encryptInt(age)
            it[UserProfilesTable.poidsKg] = encryptionService.encryptDouble(poidsKg)
            it[UserProfilesTable.tailleCm] = encryptionService.encryptInt(tailleCm)
            it[UserProfilesTable.regimeAlimentaire] = regimeAlimentaire
            it[UserProfilesTable.niveauActivite] = niveauActivite
            it[UserProfilesTable.onboardingComplete] = onboardingComplete
            it[UserProfilesTable.objectifPoids] = objectifPoids
            it[UserProfilesTable.updatedAt] = Clock.System.now()
        } > 0
    }

    suspend fun delete(userId: String): Boolean = dbQuery {
        UserProfilesTable.deleteWhere { UserProfilesTable.userId eq userId } > 0
    }

    private fun ResultRow.toRow() = UserProfileRow(
        userId = this[UserProfilesTable.userId],
        sexe = this[UserProfilesTable.sexe],
        age = encryptionService.decryptInt(this[UserProfilesTable.age]),
        poidsKg = encryptionService.decryptDouble(this[UserProfilesTable.poidsKg]),
        tailleCm = encryptionService.decryptInt(this[UserProfilesTable.tailleCm]),
        regimeAlimentaire = this[UserProfilesTable.regimeAlimentaire],
        niveauActivite = this[UserProfilesTable.niveauActivite],
        onboardingComplete = this[UserProfilesTable.onboardingComplete],
        objectifPoids = this[UserProfilesTable.objectifPoids],
        updatedAt = this[UserProfilesTable.updatedAt],
    )
}
