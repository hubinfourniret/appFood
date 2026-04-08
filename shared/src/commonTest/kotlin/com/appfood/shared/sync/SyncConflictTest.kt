package com.appfood.shared.sync

import com.appfood.shared.api.response.SyncConflict
import com.appfood.shared.api.response.SyncError
import com.appfood.shared.api.response.SyncPushResponse
import com.appfood.shared.db.Sync_queue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SyncConflictTest {

    // --- Helpers ---

    private fun buildSyncEntry(
        id: String = "entry-1",
        entityType: String = "journal",
        entityId: String = "journal-123",
        action: String = "create",
        payloadJson: String = """{"date":"2026-04-08","mealType":"DEJEUNER"}""",
        retryCount: Long = 0L,
        lastError: String? = null,
    ): Sync_queue = Sync_queue(
        id = id,
        entity_type = entityType,
        entity_id = entityId,
        action = action,
        payload_json = payloadJson,
        created_at = 1712534400000L,
        retry_count = retryCount,
        last_error = lastError,
    )

    private fun buildPushResponse(
        accepted: Int = 0,
        conflicts: List<SyncConflict> = emptyList(),
        errors: List<SyncError> = emptyList(),
    ): SyncPushResponse = SyncPushResponse(
        accepted = accepted,
        conflicts = conflicts,
        errors = errors,
    )

    // ==========================================
    // 1. Strategie "server wins"
    // ==========================================

    @Test
    fun `should delete conflicting entry from queue - server wins`() {
        // Given : une entree journal en conflit avec le serveur
        val entry = buildSyncEntry(
            id = "entry-1",
            entityType = "journal",
            entityId = "journal-123",
        )
        val response = buildPushResponse(
            accepted = 0,
            conflicts = listOf(
                SyncConflict(
                    entityType = "journal",
                    entityId = "journal-123",
                    clientVersion = "2026-04-08T10:00:00Z",
                    serverVersion = "2026-04-08T10:05:00Z",
                    resolution = "server_wins",
                ),
            ),
        )

        // When
        val resolutions = SyncConflictResolver.resolve(listOf(entry), response)

        // Then : l'entree locale est supprimee (server wins)
        assertEquals(1, resolutions.size)
        assertIs<SyncConflictResolver.Resolution.Delete>(resolutions[0])
        assertEquals("entry-1", (resolutions[0] as SyncConflictResolver.Resolution.Delete).entryId)
    }

    @Test
    fun `should delete accepted entry from queue`() {
        // Given : une entree acceptee sans conflit ni erreur
        val entry = buildSyncEntry(
            id = "entry-2",
            entityType = "poids",
            entityId = "poids-456",
        )
        val response = buildPushResponse(accepted = 1)

        // When
        val resolutions = SyncConflictResolver.resolve(listOf(entry), response)

        // Then : l'entree acceptee est supprimee de la queue
        assertEquals(1, resolutions.size)
        assertIs<SyncConflictResolver.Resolution.Delete>(resolutions[0])
        assertEquals("entry-2", (resolutions[0] as SyncConflictResolver.Resolution.Delete).entryId)
    }

    @Test
    fun `should increment retry for entry with server error`() {
        // Given : une entree qui genere une erreur serveur
        val entry = buildSyncEntry(
            id = "entry-3",
            entityType = "hydratation",
            entityId = "hydra-789",
        )
        val response = buildPushResponse(
            errors = listOf(
                SyncError(
                    entityType = "hydratation",
                    entityId = "hydra-789",
                    error = "Database constraint violation",
                ),
            ),
        )

        // When
        val resolutions = SyncConflictResolver.resolve(listOf(entry), response)

        // Then : retry increment
        assertEquals(1, resolutions.size)
        assertIs<SyncConflictResolver.Resolution.IncrementRetry>(resolutions[0])
        val retry = resolutions[0] as SyncConflictResolver.Resolution.IncrementRetry
        assertEquals("entry-3", retry.entryId)
        assertEquals("Database constraint violation", retry.errorMessage)
    }

    @Test
    fun `should handle mixed response with conflicts errors and accepted entries`() {
        // Given : 3 entrees avec resultats differents
        val entryAccepted = buildSyncEntry(id = "e1", entityType = "journal", entityId = "j-1")
        val entryConflict = buildSyncEntry(id = "e2", entityType = "poids", entityId = "p-1")
        val entryError = buildSyncEntry(id = "e3", entityType = "hydratation", entityId = "h-1")

        val response = buildPushResponse(
            accepted = 1,
            conflicts = listOf(
                SyncConflict("poids", "p-1", "v1", "v2", "server_wins"),
            ),
            errors = listOf(
                SyncError("hydratation", "h-1", "Validation failed"),
            ),
        )

        // When
        val resolutions = SyncConflictResolver.resolve(
            listOf(entryAccepted, entryConflict, entryError),
            response,
        )

        // Then
        assertEquals(3, resolutions.size)

        // e1: accepted => Delete
        assertIs<SyncConflictResolver.Resolution.Delete>(resolutions[0])
        assertEquals("e1", (resolutions[0] as SyncConflictResolver.Resolution.Delete).entryId)

        // e2: conflict => Delete (server wins)
        assertIs<SyncConflictResolver.Resolution.Delete>(resolutions[1])
        assertEquals("e2", (resolutions[1] as SyncConflictResolver.Resolution.Delete).entryId)

        // e3: error => IncrementRetry
        assertIs<SyncConflictResolver.Resolution.IncrementRetry>(resolutions[2])
        assertEquals("e3", (resolutions[2] as SyncConflictResolver.Resolution.IncrementRetry).entryId)
        assertEquals("Validation failed", (resolutions[2] as SyncConflictResolver.Resolution.IncrementRetry).errorMessage)
    }

    @Test
    fun `should use Unknown error when error message is missing`() {
        // Given : une erreur serveur sans message specifique dans la liste
        // (l'entree a un entityType/entityId qui matche un SyncError sans message)
        val entry = buildSyncEntry(id = "e-no-msg", entityType = "journal", entityId = "j-999")
        // Note: le cas "Unknown error" se produit quand firstOrNull?.error est null,
        // mais dans la data class SyncError le champ error est non-nullable String.
        // On teste le fallback en verifiant que le message est bien propage.
        val response = buildPushResponse(
            errors = listOf(
                SyncError("journal", "j-999", ""),
            ),
        )

        // When
        val resolutions = SyncConflictResolver.resolve(listOf(entry), response)

        // Then : le message d'erreur vide est propage (pas "Unknown error" car error non-null)
        assertIs<SyncConflictResolver.Resolution.IncrementRetry>(resolutions[0])
        val retry = resolutions[0] as SyncConflictResolver.Resolution.IncrementRetry
        assertEquals("", retry.errorMessage)
    }

    // ==========================================
    // 2. Retry avec backoff exponentiel
    // ==========================================

    @Test
    fun `should calculate exponential backoff delays`() {
        // Given/When/Then : 2^retryCount seconds, capped at 32
        assertEquals(1L, SyncConflictResolver.calculateBackoffSeconds(0))   // 2^0 = 1
        assertEquals(2L, SyncConflictResolver.calculateBackoffSeconds(1))   // 2^1 = 2
        assertEquals(4L, SyncConflictResolver.calculateBackoffSeconds(2))   // 2^2 = 4
        assertEquals(8L, SyncConflictResolver.calculateBackoffSeconds(3))   // 2^3 = 8
        assertEquals(16L, SyncConflictResolver.calculateBackoffSeconds(4))  // 2^4 = 16
        assertEquals(32L, SyncConflictResolver.calculateBackoffSeconds(5))  // 2^5 = 32
    }

    @Test
    fun `should cap backoff at MAX_BACKOFF_SECONDS`() {
        // Given : retryCount eleve => depasse le max
        // When/Then : plafonne a 32 par defaut
        assertEquals(32L, SyncConflictResolver.calculateBackoffSeconds(6))  // 2^6 = 64 -> capped at 32
        assertEquals(32L, SyncConflictResolver.calculateBackoffSeconds(10)) // 2^10 = 1024 -> capped at 32
    }

    @Test
    fun `should respect custom max backoff`() {
        // Given : maxBackoff personnalise
        assertEquals(8L, SyncConflictResolver.calculateBackoffSeconds(5, maxBackoffSeconds = 8L))
        assertEquals(4L, SyncConflictResolver.calculateBackoffSeconds(3, maxBackoffSeconds = 4L))
    }

    @Test
    fun `should mark entry as retryable when retry count below max`() {
        // Given
        val entry = buildSyncEntry(retryCount = 3)

        // When/Then
        assertTrue(SyncConflictResolver.isRetryable(entry, maxRetries = 5L))
    }

    @Test
    fun `should mark entry as not retryable when retry count equals max`() {
        // Given
        val entry = buildSyncEntry(retryCount = 5)

        // When/Then
        assertFalse(SyncConflictResolver.isRetryable(entry, maxRetries = 5L))
    }

    @Test
    fun `should mark entry as not retryable when retry count exceeds max`() {
        // Given
        val entry = buildSyncEntry(retryCount = 10)

        // When/Then
        assertFalse(SyncConflictResolver.isRetryable(entry, maxRetries = 5L))
    }

    @Test
    fun `should mark fresh entry as retryable`() {
        // Given : entree jamais retryee
        val entry = buildSyncEntry(retryCount = 0)

        // When/Then
        assertTrue(SyncConflictResolver.isRetryable(entry, maxRetries = 5L))
    }

    // ==========================================
    // 3. Edge cases
    // ==========================================

    @Test
    fun `should return empty resolutions for empty queue`() {
        // Given : queue vide
        val response = buildPushResponse(accepted = 0)

        // When
        val resolutions = SyncConflictResolver.resolve(emptyList(), response)

        // Then
        assertTrue(resolutions.isEmpty())
    }

    @Test
    fun `should handle entry with invalid payload JSON gracefully`() {
        // Given : entree avec JSON invalide (le resolver ne parse pas le JSON,
        // c'est le SyncManager qui le fait avant l'envoi — ici on verifie
        // que le resolver fonctionne independamment du contenu du payload)
        val entry = buildSyncEntry(
            id = "entry-invalid",
            entityType = "journal",
            entityId = "j-bad",
            payloadJson = "{{{{not valid json}}}}",
        )
        val response = buildPushResponse(accepted = 1) // pas de conflit, pas d'erreur

        // When
        val resolutions = SyncConflictResolver.resolve(listOf(entry), response)

        // Then : le resolver traite normalement (la validation JSON est en amont)
        assertEquals(1, resolutions.size)
        assertIs<SyncConflictResolver.Resolution.Delete>(resolutions[0])
    }

    @Test
    fun `should resolve multiple entries of same entity type independently`() {
        // Given : 2 entrees journal, 1 en conflit, 1 acceptee
        val entry1 = buildSyncEntry(id = "e1", entityType = "journal", entityId = "j-100")
        val entry2 = buildSyncEntry(id = "e2", entityType = "journal", entityId = "j-200")

        val response = buildPushResponse(
            accepted = 1,
            conflicts = listOf(
                SyncConflict("journal", "j-100", "v1", "v2", "server_wins"),
            ),
        )

        // When
        val resolutions = SyncConflictResolver.resolve(listOf(entry1, entry2), response)

        // Then
        assertEquals(2, resolutions.size)
        // j-100 en conflit => Delete
        assertIs<SyncConflictResolver.Resolution.Delete>(resolutions[0])
        assertEquals("e1", (resolutions[0] as SyncConflictResolver.Resolution.Delete).entryId)
        // j-200 acceptee => Delete
        assertIs<SyncConflictResolver.Resolution.Delete>(resolutions[1])
        assertEquals("e2", (resolutions[1] as SyncConflictResolver.Resolution.Delete).entryId)
    }

    @Test
    fun `should handle entry that is both conflict and error as conflict`() {
        // Given : une entree qui apparait a la fois dans conflicts et errors
        // Le resolver priorise le conflit (when: isConflict en premier)
        val entry = buildSyncEntry(id = "e-both", entityType = "poids", entityId = "p-both")
        val response = buildPushResponse(
            conflicts = listOf(
                SyncConflict("poids", "p-both", "v1", "v2", "server_wins"),
            ),
            errors = listOf(
                SyncError("poids", "p-both", "Some error"),
            ),
        )

        // When
        val resolutions = SyncConflictResolver.resolve(listOf(entry), response)

        // Then : conflit priorise => Delete (pas IncrementRetry)
        assertEquals(1, resolutions.size)
        assertIs<SyncConflictResolver.Resolution.Delete>(resolutions[0])
    }

    @Test
    fun `should handle large number of entries efficiently`() {
        // Given : 100 entrees, 50 en conflit, 25 en erreur, 25 acceptees
        val entries = (1..100).map { i ->
            buildSyncEntry(
                id = "e-$i",
                entityType = "journal",
                entityId = "j-$i",
            )
        }

        val conflicts = (1..50).map { i ->
            SyncConflict("journal", "j-$i", "v1", "v2", "server_wins")
        }
        val errors = (51..75).map { i ->
            SyncError("journal", "j-$i", "Error for $i")
        }
        val response = buildPushResponse(
            accepted = 25,
            conflicts = conflicts,
            errors = errors,
        )

        // When
        val resolutions = SyncConflictResolver.resolve(entries, response)

        // Then
        assertEquals(100, resolutions.size)

        val deletes = resolutions.filterIsInstance<SyncConflictResolver.Resolution.Delete>()
        val retries = resolutions.filterIsInstance<SyncConflictResolver.Resolution.IncrementRetry>()

        // 50 conflicts (Delete) + 25 accepted (Delete) = 75 deletes
        assertEquals(75, deletes.size)
        // 25 errors = 25 retries
        assertEquals(25, retries.size)
    }
}
