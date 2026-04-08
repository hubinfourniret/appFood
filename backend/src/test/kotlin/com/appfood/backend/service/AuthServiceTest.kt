package com.appfood.backend.service

import com.appfood.backend.external.FirebaseAdmin
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthServiceTest {
    @Test
    fun `should verify Firebase mock token format uid colon email`() =
        runBlocking {
            // Given
            val firebaseAdmin = FirebaseAdmin(mockEnabled = true)

            // When
            val info = firebaseAdmin.verifyToken("my-uid:user@mail.com")

            // Then
            assertEquals("my-uid", info.uid)
            assertEquals("user@mail.com", info.email)
        }

    @Test
    fun `should verify Firebase mock token format uid only`() =
        runBlocking {
            // Given
            val firebaseAdmin = FirebaseAdmin(mockEnabled = true)

            // When
            val info = firebaseAdmin.verifyToken("simple-uid")

            // Then
            assertEquals("simple-uid", info.uid)
            assertEquals(null, info.email)
        }
}
