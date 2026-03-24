package com.appfood.shared.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    onPrimary = Green99,
    primaryContainer = Green90,
    onPrimaryContainer = Green10,
    secondary = Brown40,
    onSecondary = Brown99,
    secondaryContainer = Brown90,
    onSecondaryContainer = Brown10,
    tertiary = Orange40,
    onTertiary = Orange99,
    tertiaryContainer = Orange90,
    onTertiaryContainer = Orange10,
    error = Error40,
    onError = Error99,
    errorContainer = Error90,
    onErrorContainer = Error10,
    background = Green99,
    onBackground = Neutral10,
    surface = Green99,
    onSurface = Neutral10,
    surfaceVariant = Neutral95,
    onSurfaceVariant = Neutral30,
    outline = Neutral50,
    outlineVariant = Neutral80,
)

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    onPrimary = Green20,
    primaryContainer = Green30,
    onPrimaryContainer = Green90,
    secondary = Brown80,
    onSecondary = Brown20,
    secondaryContainer = Brown30,
    onSecondaryContainer = Brown90,
    tertiary = Orange80,
    onTertiary = Orange20,
    tertiaryContainer = Orange30,
    onTertiaryContainer = Orange90,
    error = Error80,
    onError = Error20,
    errorContainer = Error30,
    onErrorContainer = Error90,
    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = Neutral30,
    onSurfaceVariant = Neutral80,
    outline = Neutral60,
    outlineVariant = Neutral40,
)

@Composable
fun AppFoodTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppFoodTypography,
        shapes = AppFoodShapes,
        content = content,
    )
}
