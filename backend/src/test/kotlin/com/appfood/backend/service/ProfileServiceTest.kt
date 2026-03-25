package com.appfood.backend.service

import com.appfood.backend.plugins.ValidationException
import com.appfood.backend.security.toEnumOrThrow
import com.appfood.backend.database.tables.Sexe
import com.appfood.backend.database.tables.RegimeAlimentaire
import com.appfood.backend.database.tables.NiveauActivite
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ProfileServiceTest {

    @Test
    fun `should parse valid enum values`() {
        // Given / When / Then
        assertEquals(Sexe.HOMME, "HOMME".toEnumOrThrow<Sexe>("sexe"))
        assertEquals(Sexe.FEMME, "FEMME".toEnumOrThrow<Sexe>("sexe"))
        assertEquals(RegimeAlimentaire.VEGAN, "VEGAN".toEnumOrThrow<RegimeAlimentaire>("regimeAlimentaire"))
        assertEquals(NiveauActivite.ACTIF, "ACTIF".toEnumOrThrow<NiveauActivite>("niveauActivite"))
    }

    @Test
    fun `should throw ValidationException for invalid enum value`() {
        // Given / When / Then
        val exception = assertFailsWith<ValidationException> {
            "INVALID".toEnumOrThrow<Sexe>("sexe")
        }
        assert(exception.message.contains("INVALID"))
        assert(exception.message.contains("sexe"))
    }

    @Test
    fun `should serialize and deserialize preference lists correctly`() {
        // Given
        val list = listOf("aliment-1", "aliment-2", "aliment-3")

        // When
        val json = ProfileService.serializeList(list)
        val result = ProfileService.deserializeList(json)

        // Then
        assertEquals(list, result)
    }

    @Test
    fun `should handle empty preference lists`() {
        // Given
        val list = emptyList<String>()

        // When
        val json = ProfileService.serializeList(list)
        val result = ProfileService.deserializeList(json)

        // Then
        assertEquals(list, result)
    }

    @Test
    fun `should handle malformed JSON in deserializeList`() {
        // Given
        val malformed = "not-valid-json"

        // When
        val result = ProfileService.deserializeList(malformed)

        // Then
        assertEquals(emptyList(), result)
    }
}
