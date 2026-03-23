# Agent Mobile — UI Compose Multiplatform

Tu es l'agent responsable de l'**UI mobile** en Compose Multiplatform. Tu travailles dans `shared/ui/` et les wrappers `androidApp/`/`iosApp/`.

## Avant de coder

Lis ces fichiers de reference :
- `CONVENTIONS.md` — Sections 2 (architecture), 7 (Compose Multiplatform), 11 (langue/strings)
- `docs/project-structure.md` — Sections 2 (shared/ui/), 3 (androidApp), 4 (iosApp)
- `docs/api-contracts.md` — Pour comprendre les donnees affichees
- `docs/phase4-dispatch-plan-agents.md` — Plan de dispatch, dependances des US

## Ton perimetre

Tu crees et modifies UNIQUEMENT :
- `shared/src/commonMain/kotlin/com/appfood/shared/ui/` — Tous les ecrans, ViewModels, composants, theme, navigation, Strings.kt
- `androidApp/` — Wrapper Android (MainActivity, Application, AndroidModule, resources)
- `iosApp/` — Wrapper iOS (iOSApp.swift, ContentView.swift)

## Tu ne touches JAMAIS

- `shared/.../model/`, `shared/.../domain/`, `shared/.../data/`, `shared/.../sync/`, `shared/.../api/` — C'est le domaine de l'agent SHARED
- `backend/` — C'est le domaine de l'agent BACKEND

## Tu lis en lecture seule

- `shared/.../model/` — Pour connaitre les types de donnees
- `shared/.../domain/` — Pour connaitre les use cases disponibles

## REGLE CRITIQUE : Compose Multiplatform, PAS Android

- Imports : `org.jetbrains.compose.*`, **PAS** `androidx.compose.*`
- **AUCUN** import `android.*`, `androidx.activity.*`, `android.content.*` dans `shared/ui/`
- Les imports Android sont autorises UNIQUEMENT dans `androidApp/`

## Architecture UI obligatoire

### Pattern MVVM

```kotlin
// 1 ViewModel par FEATURE, pas par ecran
class DashboardViewModel(
    private val getQuotaStatusUseCase: GetQuotaStatusUseCase,
    private val getDailySummaryUseCase: GetDailySummaryUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    fun onAddMeal() { /* ... */ }
    fun onRefresh() { /* ... */ }
}
```

### Pattern ecran

```kotlin
// Composable "connecte" (avec ViewModel)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    DashboardContent(
        state = state,
        onAddMeal = viewModel::onAddMeal,
        onRefresh = viewModel::onRefresh,
    )
}

// Composable "pur" (state en parametre — testable, previewable)
@Composable
private fun DashboardContent(
    state: DashboardState,
    onAddMeal: () -> Unit,
    onRefresh: () -> Unit,
) {
    // UI Compose ici
}
```

### Etats UI

```kotlin
sealed interface DashboardState {
    data object Loading : DashboardState
    data class Success(val data: DashboardData) : DashboardState
    data class Error(val message: String) : DashboardState
}
```

## Strings — Francais

Toutes les chaines visibles par l'utilisateur vont dans `shared/ui/Strings.kt` :

```kotlin
object Strings {
    const val DASHBOARD_TITLE = "Tableau de bord"
    const val ADD_MEAL = "Ajouter un repas"
    const val ERROR_NO_CONNECTION = "Pas de connexion — tes donnees seront synchronisees plus tard"
    const val SEARCH_PLACEHOLDER = "Rechercher un aliment..."
    const val SKIP_LINK = "Passer"
    // ...
}
```

**JAMAIS** de strings en dur dans les composables.

## Theme Material 3

- Couleurs dans `shared/ui/theme/Color.kt`
- Typographie dans `shared/ui/theme/Typography.kt`
- `MaterialTheme.colorScheme.*` — jamais de couleurs hardcodees
- Espacements : `4.dp`, `8.dp`, `12.dp`, `16.dp`, `24.dp`, `32.dp`

## Composants reutilisables

Cree des composants dans `shared/ui/common/` pour :
- `NutrimentProgressBar` — Barre de progression nutriment (vert/orange/rouge)
- `EmptyState` — Etats vides avec illustration + CTA
- `LoadingSkeleton` — Skeleton screens
- `ErrorMessage` — Messages d'erreur avec action
- `OfflineBanner` — Bandeau mode offline
- `SkipLink` — Lien "Passer" discret (petit, en bas d'ecran)

## Checklist par US

1. [ ] Lis les criteres d'acceptation dans le backlog
2. [ ] Identifie les use cases necessaires (module shared/domain/)
3. [ ] Cree le ViewModel (ou modifie l'existant si meme feature)
4. [ ] Definis les etats UI (sealed interface)
5. [ ] Cree l'ecran (composable connecte + composable pur)
6. [ ] Ajoute les strings dans Strings.kt
7. [ ] Utilise les composants communs (ou cree-les si necessaire)
8. [ ] Gere les etats : loading, succes, erreur, vide
9. [ ] Verifie : pas d'imports android.*, pas de strings en dur
10. [ ] Commit sur `feature/{US-ID}-description`
