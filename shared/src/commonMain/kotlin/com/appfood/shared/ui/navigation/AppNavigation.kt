package com.appfood.shared.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.appfood.shared.ui.Strings
import com.appfood.shared.ui.auth.AuthViewModel
import com.appfood.shared.ui.auth.ForgotPasswordScreen
import com.appfood.shared.ui.auth.LoginScreen
import com.appfood.shared.ui.auth.RegisterScreen
import com.appfood.shared.ui.onboarding.OnboardingScreen
import com.appfood.shared.ui.onboarding.OnboardingViewModel
import com.appfood.shared.ui.profil.EditProfilScreen
import com.appfood.shared.ui.profil.PreferencesAlimentairesScreen
import com.appfood.shared.ui.profil.ProfilScreen
import com.appfood.shared.ui.profil.ProfilViewModel

// Screens that show the bottom navigation bar
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
    val currentScreen = backStackEntry?.toScreen() ?: Screen.Login

    val showBottomNav = currentScreen::class in screensWithBottomNav

    // Shared ViewModels — created once and shared across screens
    // TODO: Replace with koinViewModel() when Koin DI is wired for ViewModels
    val authViewModel = remember { AuthViewModel() }
    val onboardingViewModel = remember { OnboardingViewModel() }
    val profilViewModel = remember { ProfilViewModel() }

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomNavBar(
                    currentScreen = currentScreen,
                    onNavigate = { screen ->
                        navController.navigate(screen) {
                            // Avoid stacking in back stack
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
            startDestination = Screen.Login,
            modifier = Modifier.padding(innerPadding),
        ) {
            // Auth flow
            composable<Screen.Login> {
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Screen.ForgotPassword) {
                            launchSingleTop = true
                        }
                    },
                    onLoginSuccess = { needsOnboarding ->
                        if (needsOnboarding) {
                            navController.navigate(Screen.Onboarding) {
                                popUpTo<Screen.Login> { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Dashboard) {
                                popUpTo<Screen.Login> { inclusive = true }
                            }
                        }
                    },
                )
            }

            composable<Screen.Auth> {
                // Redirect to Login (backward compatibility)
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Screen.ForgotPassword) {
                            launchSingleTop = true
                        }
                    },
                    onLoginSuccess = { needsOnboarding ->
                        if (needsOnboarding) {
                            navController.navigate(Screen.Onboarding) {
                                popUpTo<Screen.Auth> { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Dashboard) {
                                popUpTo<Screen.Auth> { inclusive = true }
                            }
                        }
                    },
                )
            }

            composable<Screen.Register> {
                RegisterScreen(
                    viewModel = authViewModel,
                    onNavigateToLogin = {
                        navController.popBackStack()
                    },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Onboarding) {
                            popUpTo<Screen.Login> { inclusive = true }
                        }
                    },
                )
            }

            composable<Screen.ForgotPassword> {
                ForgotPasswordScreen(
                    viewModel = authViewModel,
                    onNavigateToLogin = {
                        navController.popBackStack()
                    },
                )
            }

            // Onboarding flow
            composable<Screen.Onboarding> {
                OnboardingScreen(
                    viewModel = onboardingViewModel,
                    onOnboardingComplete = {
                        navController.navigate(Screen.Dashboard) {
                            popUpTo<Screen.Onboarding> { inclusive = true }
                        }
                    },
                )
            }

            // Main app screens
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

            // Profile screens
            composable<Screen.Profil> {
                ProfilScreen(
                    authViewModel = authViewModel,
                    onNavigateToEditProfil = {
                        navController.navigate(Screen.EditProfil) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToPreferences = {
                        navController.navigate(Screen.PreferencesAlimentaires) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings) {
                            launchSingleTop = true
                        }
                    },
                    onLogout = {
                        navController.navigate(Screen.Login) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onAccountDeleted = {
                        navController.navigate(Screen.Login) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }

            composable<Screen.EditProfil> {
                EditProfilScreen(
                    viewModel = profilViewModel,
                    onSaveSuccess = {
                        navController.popBackStack()
                    },
                )
            }

            composable<Screen.PreferencesAlimentaires> {
                PreferencesAlimentairesScreen(
                    viewModel = profilViewModel,
                    onSaveSuccess = {
                        navController.popBackStack()
                    },
                )
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
 * Determine the current Screen from the NavBackStackEntry.
 * Returns null if the destination is unknown.
 */
private fun androidx.navigation.NavBackStackEntry.toScreen(): Screen? {
    val route = destination.route ?: return null
    return when {
        route.contains("Login") -> Screen.Login
        route.contains("Register") -> Screen.Register
        route.contains("ForgotPassword") -> Screen.ForgotPassword
        route.contains("Onboarding") -> Screen.Onboarding
        route.contains("Dashboard") -> Screen.Dashboard
        route.contains("Journal") -> Screen.Journal
        route.contains("Recommandations") -> Screen.Recommandations
        route.contains("Recettes") -> Screen.Recettes
        route.contains("EditProfil") -> Screen.EditProfil
        route.contains("PreferencesAlimentaires") -> Screen.PreferencesAlimentaires
        route.contains("Profil") -> Screen.Profil
        route.contains("Settings") -> Screen.Settings
        route.contains("Auth") -> Screen.Auth
        else -> null
    }
}
