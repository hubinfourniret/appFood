package com.appfood.backend.security

import com.appfood.backend.plugins.ValidationException

/**
 * Utility to parse a String into an enum value, throwing ValidationException on failure.
 */
inline fun <reified T : Enum<T>> String.toEnumOrThrow(fieldName: String): T {
    return try {
        enumValueOf<T>(this)
    } catch (e: IllegalArgumentException) {
        throw ValidationException(
            "Valeur invalide pour '$fieldName': '$this'. Valeurs acceptees: ${enumValues<T>().joinToString()}",
        )
    }
}
