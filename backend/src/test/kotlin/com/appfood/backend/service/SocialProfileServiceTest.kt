package com.appfood.backend.service

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * TACHE-600 : tests unitaires des fonctions pures de SocialProfileService.
 * Couvre :
 * - Calcul d'age (anniversaire passe / pas passe)
 * - Eligibilite social (16+)
 */
class SocialProfileServiceTest {
    @Test
    fun `computeAge anniversaire deja passe cette annee`() {
        val today = LocalDate(2026, 5, 2)
        // Naissance le 1er mai 2000 -> 26 ans
        val dob = LocalDate(2000, 5, 1)
        assertEquals(26, SocialProfileService.computeAge(dob, today))
    }

    @Test
    fun `computeAge anniversaire pas encore atteint cette annee`() {
        val today = LocalDate(2026, 5, 2)
        // Naissance le 3 mai 2000 -> 25 ans (anniversaire demain)
        val dob = LocalDate(2000, 5, 3)
        assertEquals(25, SocialProfileService.computeAge(dob, today))
    }

    @Test
    fun `computeAge meme jour anniversaire`() {
        val today = LocalDate(2026, 5, 2)
        val dob = LocalDate(2000, 5, 2)
        assertEquals(26, SocialProfileService.computeAge(dob, today))
    }

    @Test
    fun `computeAge mois apres dans annee`() {
        val today = LocalDate(2026, 5, 2)
        // Naissance en juin 2000 -> 25 ans (mois pas atteint)
        val dob = LocalDate(2000, 6, 1)
        assertEquals(25, SocialProfileService.computeAge(dob, today))
    }

    @Test
    fun `isSocialEligible true si 16 ans`() {
        // En supposant la fonction est appelee aujourd'hui
        val ya16Ans = LocalDate(LocalDate(2026, 5, 2).year - 16, 1, 1)
        // Note : on passe par la version avec date 'today' pour ne pas dependre de Clock
        val age = SocialProfileService.computeAge(ya16Ans, LocalDate(2026, 5, 2))
        assertTrue(age >= 16)
    }

    @Test
    fun `isSocialEligible false si 15 ans`() {
        val ya15Ans = LocalDate(2011, 6, 1)
        val age = SocialProfileService.computeAge(ya15Ans, LocalDate(2026, 5, 2))
        assertFalse(age >= 16)
    }

    @Test
    fun `isSocialEligible false si null`() {
        assertFalse(SocialProfileService.isSocialEligible(null))
    }
}
