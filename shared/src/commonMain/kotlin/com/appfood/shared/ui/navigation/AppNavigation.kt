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
import androidx.navigation.toRoute
import com.appfood.shared.ui.Strings
import com.appfood.shared.ui.auth.AuthViewModel
import com.appfood.shared.ui.auth.ForgotPasswordScreen
import com.appfood.shared.ui.auth.LoginScreen
import com.appfood.shared.ui.auth.RegisterScreen
import com.appfood.shared.ui.dashboard.DashboardScreen
import com.appfood.shared.ui.dashboard.DashboardViewModel
import com.appfood.shared.ui.dashboard.WeeklyDashboardScreen
import com.appfood.shared.ui.dashboard.WeeklyDashboardViewModel
import com.appfood.shared.data.repository.HydratationRepository
import com.appfood.shared.domain.hydratation.AjouterEauUseCase
import com.appfood.shared.domain.hydratation.GetHydratationJourUseCase
import com.appfood.shared.domain.hydratation.UpdateObjectifHydratationUseCase
import com.appfood.shared.ui.hydratation.HydratationScreen
import com.appfood.shared.ui.hydratation.HydratationViewModel
import com.appfood.shared.ui.legal.ConsentScreen
import com.appfood.shared.ui.legal.ConsentSettingsScreen
import com.appfood.shared.ui.legal.ConsentViewModel
import com.appfood.shared.ui.legal.DisclaimerScreen
import com.appfood.shared.ui.legal.DisclaimerViewModel
import com.appfood.shared.ui.legal.PrivacyPolicyScreen
import com.appfood.shared.ui.legal.TermsOfServiceScreen
import com.appfood.shared.ui.journal.AddEntryScreen
import com.appfood.shared.ui.journal.JournalViewModel
import com.appfood.shared.ui.journal.PortionSelectorScreen
import com.appfood.shared.ui.journal.SearchAlimentScreen
import com.appfood.shared.ui.onboarding.OnboardingScreen
import com.appfood.shared.ui.onboarding.OnboardingViewModel
import com.appfood.shared.domain.poids.DetecterChangementPoidsUseCase
import com.appfood.shared.domain.poids.EnregistrerPoidsUseCase
import com.appfood.shared.domain.poids.GetHistoriquePoidsUseCase
import com.appfood.shared.domain.poids.RecalculerQuotasApresPoidsUseCase
import com.appfood.shared.ui.poids.PoidsScreen
import com.appfood.shared.ui.poids.PoidsViewModel
import com.appfood.shared.ui.profil.EditProfilScreen
import com.appfood.shared.ui.profil.PreferencesAlimentairesScreen
import com.appfood.shared.ui.profil.ProfilScreen
import com.appfood.shared.ui.profil.ProfilViewModel
import com.appfood.shared.ui.quota.QuotaManagementScreen
import com.appfood.shared.ui.quota.QuotaViewModel
import com.appfood.shared.ui.recette.CreateRecetteScreen
import com.appfood.shared.ui.recette.RecetteDetailScreen
import com.appfood.shared.ui.recette.RecettesListScreen
import com.appfood.shared.ui.recette.RecettesViewModel
import com.appfood.shared.ui.recommandation.RecommandationsScreen
import com.appfood.shared.ui.recommandation.RecommandationViewModel
import com.appfood.shared.ui.settings.AboutScreen
import com.appfood.shared.data.local.LocalUserDataSource
import com.appfood.shared.data.repository.AlimentRepository
import com.appfood.shared.data.repository.DashboardRepository
import com.appfood.shared.data.repository.JournalRepository
import com.appfood.shared.data.repository.QuotaRepository
import com.appfood.shared.data.repository.RecetteRepository
import com.appfood.shared.data.repository.RecommandationRepository
import com.appfood.shared.data.repository.UserRepository
import com.appfood.shared.data.remote.ConsentApi
import com.appfood.shared.data.remote.SupportApi
import com.appfood.shared.ui.support.FaqScreen
import com.appfood.shared.ui.support.FaqViewModel
import org.koin.compose.koinInject

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
    // UserRepository injected via Koin, passed to ViewModels that need it
    val userRepository = koinInject<UserRepository>()
    val authViewModel = remember(userRepository) { AuthViewModel(userRepository) }
    val onboardingViewModel = remember(userRepository) { OnboardingViewModel(userRepository = userRepository) }
    val profilViewModel = remember(userRepository) { ProfilViewModel(userRepository = userRepository) }
    val journalRepository = koinInject<JournalRepository>()
    val alimentRepository = koinInject<AlimentRepository>()
    val recetteRepository = koinInject<RecetteRepository>()
    val journalViewModel = remember(journalRepository, alimentRepository, recetteRepository) {
        JournalViewModel(
            journalRepository = journalRepository,
            alimentRepository = alimentRepository,
            recetteRepository = recetteRepository,
        ).also { it.init() }
    }
    val dashboardRepository = koinInject<DashboardRepository>()
    val dashboardViewModel = remember(dashboardRepository) { DashboardViewModel(dashboardRepository = dashboardRepository) }
    val quotaRepository = koinInject<QuotaRepository>()
    val localUserDataSource = koinInject<LocalUserDataSource>()
    val quotaViewModel = remember(quotaRepository, localUserDataSource) { QuotaViewModel(quotaRepository, localUserDataSource) }
    val recommandationRepository = koinInject<RecommandationRepository>()
    val recommandationViewModel = remember(recommandationRepository) {
        RecommandationViewModel(recommandationRepository).also { it.loadRecommandations() }
    }
    val getHydratationJourUseCase = koinInject<GetHydratationJourUseCase>()
    val ajouterEauUseCase = koinInject<AjouterEauUseCase>()
    val updateObjectifHydratationUseCase = koinInject<UpdateObjectifHydratationUseCase>()
    val hydratationRepository = koinInject<HydratationRepository>()
    val hydratationViewModel = remember(
        getHydratationJourUseCase, ajouterEauUseCase, updateObjectifHydratationUseCase, hydratationRepository,
    ) {
        HydratationViewModel(
            getHydratationJourUseCase = getHydratationJourUseCase,
            ajouterEauUseCase = ajouterEauUseCase,
            updateObjectifUseCase = updateObjectifHydratationUseCase,
            hydratationRepository = hydratationRepository,
        )
    }
    val enregistrerPoidsUseCase = koinInject<EnregistrerPoidsUseCase>()
    val getHistoriquePoidsUseCase = koinInject<GetHistoriquePoidsUseCase>()
    val detecterChangementPoidsUseCase = koinInject<DetecterChangementPoidsUseCase>()
    val recalculerQuotasApresPoidsUseCase = koinInject<RecalculerQuotasApresPoidsUseCase>()
    val poidsViewModel = remember(
        enregistrerPoidsUseCase, getHistoriquePoidsUseCase,
        detecterChangementPoidsUseCase, recalculerQuotasApresPoidsUseCase, userRepository,
    ) {
        PoidsViewModel(
            enregistrerPoidsUseCase = enregistrerPoidsUseCase,
            getHistoriquePoidsUseCase = getHistoriquePoidsUseCase,
            detecterChangementPoidsUseCase = detecterChangementPoidsUseCase,
            recalculerQuotasApresPoidsUseCase = recalculerQuotasApresPoidsUseCase,
            userRepository = userRepository,
        )
    }
    val weeklyDashboardViewModel = remember(dashboardRepository) { WeeklyDashboardViewModel(dashboardRepository = dashboardRepository) }
    val disclaimerViewModel = remember { DisclaimerViewModel() }
    val consentApi = koinInject<ConsentApi>()
    val consentViewModel = remember(consentApi) { ConsentViewModel(consentApi) }
    val recettesViewModel = remember(recetteRepository) {
        RecettesViewModel(recetteRepository).also { it.init() }
    }
    val supportApi = koinInject<SupportApi>()
    val faqViewModel = remember(supportApi) { FaqViewModel(supportApi).also { it.init() } }

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
                        navController.navigate(Screen.AddEntry) {
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
                            // Consent screen first (LEGAL-03) — before any data collection
                            navController.navigate(Screen.Consent) {
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
                            // Consent screen first (LEGAL-03) — before any data collection
                            navController.navigate(Screen.Consent) {
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
                        // Consent screen first (LEGAL-03) — before any data collection
                        navController.navigate(Screen.Consent) {
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

            // Consent screen (LEGAL-03) — shown at first launch before any data collection
            composable<Screen.Consent> {
                ConsentScreen(
                    viewModel = consentViewModel,
                    onConsentConfirmed = {
                        navController.navigate(Screen.Onboarding) {
                            popUpTo<Screen.Consent> { inclusive = true }
                        }
                    },
                )
            }

            // Onboarding flow
            composable<Screen.Onboarding> {
                OnboardingScreen(
                    viewModel = onboardingViewModel,
                    onOnboardingComplete = {
                        navController.navigate(Screen.Disclaimer) {
                            popUpTo<Screen.Onboarding> { inclusive = true }
                        }
                    },
                )
            }

            // Legal disclaimer (UX-05) — shown after onboarding, before dashboard
            composable<Screen.Disclaimer> {
                DisclaimerScreen(
                    viewModel = disclaimerViewModel,
                    onDisclaimerAccepted = {
                        navController.navigate(Screen.Dashboard) {
                            popUpTo<Screen.Disclaimer> { inclusive = true }
                        }
                    },
                )
            }

            // Main app screens
            composable<Screen.Dashboard> {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    hydratationViewModel = hydratationViewModel,
                    onNavigateToAddEntry = {
                        navController.navigate(Screen.AddEntry) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToQuotaManagement = {
                        navController.navigate(Screen.QuotaManagement) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToRecommandations = {
                        navController.navigate(Screen.Recommandations) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToHydratation = {
                        navController.navigate(Screen.Hydratation) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToWeeklyDashboard = {
                        navController.navigate(Screen.WeeklyDashboard) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable<Screen.Journal> {
                PlaceholderScreen(title = Strings.SCREEN_JOURNAL)
            }

            // Journal flow — Add entry
            composable<Screen.AddEntry> {
                AddEntryScreen(
                    viewModel = journalViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToSearch = {
                        navController.navigate(Screen.SearchAliment) {
                            launchSingleTop = true
                        }
                    },
                    onEntrySaved = {
                        navController.popBackStack(Screen.Dashboard, inclusive = false)
                    },
                )
            }

            composable<Screen.SearchAliment> {
                SearchAlimentScreen(
                    viewModel = journalViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onAlimentSelected = {
                        navController.navigate(Screen.PortionSelector) {
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable<Screen.PortionSelector> {
                PortionSelectorScreen(
                    viewModel = journalViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onEntryValidated = {
                        navController.popBackStack(Screen.Dashboard, inclusive = false)
                    },
                )
            }
            composable<Screen.Recommandations> {
                RecommandationsScreen(
                    viewModel = recommandationViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                )
            }

            composable<Screen.QuotaManagement> {
                QuotaManagementScreen(
                    viewModel = quotaViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                )
            }
            composable<Screen.Recettes> {
                RecettesListScreen(
                    viewModel = recettesViewModel,
                    onRecetteClick = { recetteId ->
                        navController.navigate(Screen.RecetteDetail(recetteId)) {
                            launchSingleTop = true
                        }
                    },
                )
            }

            // Recette detail (RECETTES-02)
            composable<Screen.RecetteDetail> { backStackEntry ->
                val screen = backStackEntry.toRoute<Screen.RecetteDetail>()
                RecetteDetailScreen(
                    recetteId = screen.recetteId,
                    viewModel = recettesViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToJournalAdd = {
                        navController.navigate(Screen.AddEntry) {
                            launchSingleTop = true
                        }
                    },
                )
            }

            // Create recette — admin (RECETTES-03)
            composable<Screen.CreateRecette> {
                CreateRecetteScreen(
                    viewModel = recettesViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                )
            }

            // Hydratation (HYDRA-01)
            composable<Screen.Hydratation> {
                HydratationScreen(
                    viewModel = hydratationViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                )
            }

            // Poids (POIDS-01)
            composable<Screen.Poids> {
                PoidsScreen(
                    viewModel = poidsViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                )
            }

            // Weekly dashboard (DASHBOARD-02)
            composable<Screen.WeeklyDashboard> {
                WeeklyDashboardScreen(
                    viewModel = weeklyDashboardViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                )
            }

            // Profile screens
            composable<Screen.Profil> {
                ProfilScreen(
                    authViewModel = authViewModel,
                    profilViewModel = profilViewModel,
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
                            popUpTo<Screen.Dashboard> { inclusive = true }
                        }
                    },
                    onAccountDeleted = {
                        navController.navigate(Screen.Login) {
                            popUpTo<Screen.Dashboard> { inclusive = true }
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

            // Consent settings (LEGAL-03) — modify consent choices from Settings
            composable<Screen.ConsentSettings> {
                ConsentSettingsScreen(
                    viewModel = consentViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                )
            }

            // About (SUPPORT-01)
            composable<Screen.About> {
                AboutScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToCgu = {
                        navController.navigate(Screen.TermsOfService) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToPrivacyPolicy = {
                        navController.navigate(Screen.PrivacyPolicy) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToFaq = {
                        navController.navigate(Screen.Faq) {
                            launchSingleTop = true
                        }
                    },
                )
            }

            // FAQ (SUPPORT-02)
            composable<Screen.Faq> {
                FaqScreen(
                    viewModel = faqViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                )
            }

            // Privacy Policy (LEGAL-01)
            composable<Screen.PrivacyPolicy> {
                PrivacyPolicyScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                )
            }

            // Terms of Service (LEGAL-02)
            composable<Screen.TermsOfService> {
                TermsOfServiceScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                )
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
        route.contains("AddEntry") -> Screen.AddEntry
        route.contains("SearchAliment") -> Screen.SearchAliment
        route.contains("PortionSelector") -> Screen.PortionSelector
        route.contains("Journal") -> Screen.Journal
        route.contains("QuotaManagement") -> Screen.QuotaManagement
        route.contains("Recommandations") -> Screen.Recommandations
        route.contains("Recettes") -> Screen.Recettes
        route.contains("WeeklyDashboard") -> Screen.WeeklyDashboard
        route.contains("Hydratation") -> Screen.Hydratation
        route.contains("Poids") -> Screen.Poids
        route.contains("EditProfil") -> Screen.EditProfil
        route.contains("PreferencesAlimentaires") -> Screen.PreferencesAlimentaires
        route.contains("Profil") -> Screen.Profil
        route.contains("Settings") -> Screen.Settings
        route.contains("Faq") -> Screen.Faq
        route.contains("About") -> Screen.About
        route.contains("Disclaimer") -> Screen.Disclaimer
        route.contains("PrivacyPolicy") -> Screen.PrivacyPolicy
        route.contains("TermsOfService") -> Screen.TermsOfService
        route.contains("ConsentSettings") -> Screen.ConsentSettings
        route.contains("Consent") -> Screen.Consent
        route.contains("Auth") -> Screen.Auth
        else -> null
    }
}
