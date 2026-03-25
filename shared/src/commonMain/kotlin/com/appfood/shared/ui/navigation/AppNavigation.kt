package com.appfood.shared.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.appfood.shared.ui.Strings

// Ecrans qui affichent la barre de navigation
private val screensWithBottomNav = setOf(
    Screen.Dashboard::class,
    Screen.Journal::class,
    Screen.Recettes::class,
    Screen.Profil::class,
)

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = backStackEntry?.toScreen() ?: Screen.Dashboard

    val showBottomNav = currentScreen::class in screensWithBottomNav

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomNavBar(
                    currentScreen = currentScreen,
                    onNavigate = { screen ->
                        navController.navigate(screen) {
                            // Eviter l'empilement dans le back stack
                            popUpTo<Screen.Dashboard> {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onAddClick = {
                        navController.navigate(Screen.Journal) {
                            launchSingleTop = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<Screen.Auth> {
                PlaceholderScreen(title = Strings.SCREEN_AUTH)
            }
            composable<Screen.Register> {
                PlaceholderScreen(title = Strings.SCREEN_REGISTER)
            }
            composable<Screen.Onboarding> {
                PlaceholderScreen(title = Strings.SCREEN_ONBOARDING)
            }
            composable<Screen.Dashboard> {
                PlaceholderScreen(title = Strings.SCREEN_DASHBOARD)
            }
            composable<Screen.Journal> {
                PlaceholderScreen(title = Strings.SCREEN_JOURNAL)
            }
            composable<Screen.Recommandations> {
                PlaceholderScreen(title = Strings.SCREEN_RECOMMANDATIONS)
            }
            composable<Screen.Recettes> {
                PlaceholderScreen(title = Strings.SCREEN_RECETTES)
            }
            composable<Screen.Profil> {
                PlaceholderScreen(title = Strings.SCREEN_PROFIL)
            }
            composable<Screen.Settings> {
                PlaceholderScreen(title = Strings.SCREEN_SETTINGS)
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}

/**
 * Determine le Screen courant a partir du NavBackStackEntry.
 * Retourne null si la destination est inconnue.
 */
private fun androidx.navigation.NavBackStackEntry.toScreen(): Screen? {
    val route = destination.route ?: return null
    return when {
        route.contains("Auth") -> Screen.Auth
        route.contains("Register") -> Screen.Register
        route.contains("Onboarding") -> Screen.Onboarding
        route.contains("Dashboard") -> Screen.Dashboard
        route.contains("Journal") -> Screen.Journal
        route.contains("Recommandations") -> Screen.Recommandations
        route.contains("Recettes") -> Screen.Recettes
        route.contains("Profil") -> Screen.Profil
        route.contains("Settings") -> Screen.Settings
        else -> null
    }
}
