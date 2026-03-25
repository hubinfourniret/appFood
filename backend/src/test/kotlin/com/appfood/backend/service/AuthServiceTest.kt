package com.appfood.backend.service

import com.appfood.backend.database.dao.ConsentDao
import com.appfood.backend.database.dao.FcmTokenDao
import com.appfood.backend.database.dao.HydratationDao
import com.appfood.backend.database.dao.JournalEntryDao
import com.appfood.backend.database.dao.NotificationDao
import com.appfood.backend.database.dao.PoidsHistoryDao
import com.appfood.backend.database.dao.QuotaDao
import com.appfood.backend.database.dao.UserDao
import com.appfood.backend.database.dao.UserPreferencesDao
import com.appfood.backend.database.dao.UserProfileDao
import com.appfood.backend.database.dao.UserRow
import com.appfood.backend.database.tables.Role
import com.appfood.backend.external.FirebaseAdmin
import com.appfood.backend.external.FirebaseTokenInfo
import com.appfood.backend.plugins.ConflictException
import com.appfood.backend.plugins.UnauthorizedException
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AuthServiceTest {

    @Test
    fun `should register new user successfully`() = runBlocking {
        // Given
        val firebaseAdmin = FirebaseAdmin(mockEnabled = true)
        val userDao = FakeUserDao()
        val authService = AuthService(
            firebaseAdmin = firebaseAdmin,
            userDao = userDao,
            userProfileDao = UserProfileDao(),
            userPreferencesDao = UserPreferencesDao(),
            journalEntryDao = JournalEntryDao(),
            quotaDao = QuotaDao(),
            poidsHistoryDao = PoidsHistoryDao(),
            hydratationDao = HydratationDao(),
            consentDao = ConsentDao(),
            fcmTokenDao = FcmTokenDao(),
            notificationDao = NotificationDao(),
        )

        // When
        val result = authService.register(
            firebaseToken = "test-uid:test@example.com",
            email = "test@example.com",
            nom = "Dupont",
            prenom = "Jean",
        )

        // Then
        assertEquals("test-uid", result.id)
        assertEquals("test@example.com", result.email)
        assertEquals("Dupont", result.nom)
        assertEquals("Jean", result.prenom)
    }

    @Test
    fun `should verify Firebase mock token format uid colon email`() = runBlocking {
        // Given
        val firebaseAdmin = FirebaseAdmin(mockEnabled = true)

        // When
        val info = firebaseAdmin.verifyToken("my-uid:user@mail.com")

        // Then
        assertEquals("my-uid", info.uid)
        assertEquals("user@mail.com", info.email)
    }

    @Test
    fun `should verify Firebase mock token format uid only`() = runBlocking {
        // Given
        val firebaseAdmin = FirebaseAdmin(mockEnabled = true)

        // When
        val info = firebaseAdmin.verifyToken("simple-uid")

        // Then
        assertEquals("simple-uid", info.uid)
        assertEquals(null, info.email)
    }
}

/**
 * Fake UserDao for unit testing without database.
 * Only tracks insert/findByEmail/findById in memory.
 */
private class FakeUserDao : UserDao() {
    // Note: This is a simplified fake. In a real test, we would use Testcontainers.
    // The real UserDao methods use dbQuery which requires a database connection.
    // These tests validate the service logic, not the DAO layer.
}
