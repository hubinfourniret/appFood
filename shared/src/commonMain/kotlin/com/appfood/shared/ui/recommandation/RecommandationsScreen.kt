package com.appfood.shared.ui.recommandation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.appfood.shared.ui.Strings
import com.appfood.shared.ui.common.ErrorMessage
import com.appfood.shared.ui.common.LoadingSkeleton

// Back arrow icon
private val BackArrowIcon: ImageVector by lazy {
    ImageVector.Builder("Back", 24.dp, 24.dp, 24f, 24f)
        .path(fill = SolidColor(Color.Black)) {
            moveTo(20f, 11f)
            horizontalLineTo(7.83f)
            lineTo(13.42f, 5.41f)
            lineTo(12f, 4f)
            lineTo(4f, 12f)
            lineTo(12f, 20f)
            lineTo(13.41f, 18.59f)
            lineTo(7.83f, 13f)
            horizontalLineTo(20f)
            close()
        }.build()
}

/**
 * Recommendations screen (RECO-01, RECO-02).
 * Connected composable that delegates to pure content composable.
 * Supports aliment and recipe recommendation tabs.
 */
@Composable
fun RecommandationsScreen(
    viewModel: RecommandationViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val recetteState by viewModel.recetteState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val recettePortions by viewModel.recettePortions.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadRecommandations()
    }

    RecommandationsContent(
        state = state,
        recetteState = recetteState,
        selectedTab = selectedTab,
        recettePortions = recettePortions,
        onTabSelected = viewModel::onTabSelected,
        onAteThis = viewModel::onAteThis,
        onRecettePortionsChanged = viewModel::onRecettePortionsChanged,
        onAddRecetteToJournal = viewModel::onAddRecetteToJournal,
        onRetry = viewModel::retry,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecommandationsContent(
    state: RecommandationState,
    recetteState: RecommandationRecetteState,
    selectedTab: RecommandationTab,
    recettePortions: Map<String, Int>,
    onTabSelected: (RecommandationTab) -> Unit,
    onAteThis: (RecommandationUiModel) -> Unit,
    onRecettePortionsChanged: (String, Int) -> Unit,
    onAddRecetteToJournal: (RecommandationRecetteUiModel) -> Unit,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.RECO_TITLE) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = BackArrowIcon,
                            contentDescription = Strings.JOURNAL_BACK,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Tab selector (RECO-01 aliments / RECO-02 recettes)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = selectedTab == RecommandationTab.ALIMENTS,
                    onClick = { onTabSelected(RecommandationTab.ALIMENTS) },
                    label = { Text(Strings.RECO_SUBTITLE) },
                )
                FilterChip(
                    selected = selectedTab == RecommandationTab.RECETTES,
                    onClick = { onTabSelected(RecommandationTab.RECETTES) },
                    label = { Text(Strings.RECO_RECETTES_TITLE) },
                )
            }

            when (selectedTab) {
                RecommandationTab.ALIMENTS -> {
                    AlimentRecommandationsTabContent(
                        state = state,
                        onAteThis = onAteThis,
                        onRetry = onRetry,
                    )
                }

                RecommandationTab.RECETTES -> {
                    RecetteRecommandationsTabContent(
                        state = recetteState,
                        recettePortions = recettePortions,
                        onPortionsChanged = onRecettePortionsChanged,
                        onAddToJournal = onAddRecetteToJournal,
                        onRetry = onRetry,
                    )
                }
            }
        }
    }
}

@Composable
private fun AlimentRecommandationsTabContent(
    state: RecommandationState,
    onAteThis: (RecommandationUiModel) -> Unit,
    onRetry: () -> Unit,
) {
    when (state) {
        is RecommandationState.Loading -> {
            LoadingSkeleton(lines = 6)
        }

        is RecommandationState.Error -> {
            ErrorMessage(
                message = state.message,
                onRetry = onRetry,
            )
        }

        is RecommandationState.NoDeficit -> {
            NoDeficitContent()
        }

        is RecommandationState.Success -> {
            RecommandationListContent(
                recommandations = state.recommandations,
                onAteThis = onAteThis,
            )
        }
    }
}

@Composable
private fun RecetteRecommandationsTabContent(
    state: RecommandationRecetteState,
    recettePortions: Map<String, Int>,
    onPortionsChanged: (String, Int) -> Unit,
    onAddToJournal: (RecommandationRecetteUiModel) -> Unit,
    onRetry: () -> Unit,
) {
    when (state) {
        is RecommandationRecetteState.Loading -> {
            LoadingSkeleton(lines = 6)
        }

        is RecommandationRecetteState.Error -> {
            ErrorMessage(
                message = state.message,
                onRetry = onRetry,
            )
        }

        is RecommandationRecetteState.NoDeficit -> {
            NoDeficitContent()
        }

        is RecommandationRecetteState.Success -> {
            RecetteRecommandationListContent(
                recettes = state.recettes,
                recettePortions = recettePortions,
                onPortionsChanged = onPortionsChanged,
                onAddToJournal = onAddToJournal,
            )
        }
    }
}

@Composable
private fun NoDeficitContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "\u2705",
            style = MaterialTheme.typography.displayMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = Strings.RECO_NO_DEFICIT,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun RecommandationListContent(
    recommandations: List<RecommandationUiModel>,
    onAteThis: (RecommandationUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = Strings.RECO_SUBTITLE,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(4.dp))

        recommandations.forEach { recommandation ->
            RecommandationCard(
                recommandation = recommandation,
                onAteThis = { onAteThis(recommandation) },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun RecetteRecommandationListContent(
    recettes: List<RecommandationRecetteUiModel>,
    recettePortions: Map<String, Int>,
    onPortionsChanged: (String, Int) -> Unit,
    onAddToJournal: (RecommandationRecetteUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        recettes.forEach { recette ->
            val portions = recettePortions[recette.recetteId] ?: 1
            RecetteRecommandationCard(
                recette = recette,
                portions = portions,
                onPortionsChanged = { newPortions -> onPortionsChanged(recette.recetteId, newPortions) },
                onAteThis = { onAddToJournal(recette) },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Plus icon for portion selector
private val RecoAddIcon: ImageVector by lazy {
    ImageVector.Builder("RecoAdd", 24.dp, 24.dp, 24f, 24f)
        .path(fill = SolidColor(Color.Black)) {
            moveTo(19f, 13f)
            horizontalLineTo(13f)
            verticalLineTo(19f)
            horizontalLineTo(11f)
            verticalLineTo(13f)
            horizontalLineTo(5f)
            verticalLineTo(11f)
            horizontalLineTo(11f)
            verticalLineTo(5f)
            horizontalLineTo(13f)
            verticalLineTo(11f)
            horizontalLineTo(19f)
            close()
        }.build()
}

// Minus icon for portion selector
private val RecoRemoveIcon: ImageVector by lazy {
    ImageVector.Builder("RecoRemove", 24.dp, 24.dp, 24f, 24f)
        .path(fill = SolidColor(Color.Black)) {
            moveTo(19f, 13f)
            horizontalLineTo(5f)
            verticalLineTo(11f)
            horizontalLineTo(19f)
            close()
        }.build()
}

@Composable
private fun RecetteRecommandationCard(
    recette: RecommandationRecetteUiModel,
    portions: Int,
    onPortionsChanged: (Int) -> Unit,
    onAteThis: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = recette.recetteNom,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = Strings.recoRecetteCoverage(recette.pourcentageCouvertureGlobal),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )

            if (recette.nutrimentsCibles.isNotEmpty()) {
                Text(
                    text = Strings.recoRecetteNutrients(recette.nutrimentsCibles.joinToString(", ")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = Strings.recetteTempsPrep(recette.tempsPreparationMin),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Selecteur de portions (RECO-03)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = Strings.RECO_PORTIONS_LABEL,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { if (portions > 1) onPortionsChanged(portions - 1) },
                    enabled = portions > 1,
                ) {
                    Icon(
                        imageVector = RecoRemoveIcon,
                        contentDescription = "-",
                    )
                }
                Text(
                    text = Strings.recoPortionCount(portions),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IconButton(
                    onClick = { onPortionsChanged(portions + 1) },
                ) {
                    Icon(
                        imageVector = RecoAddIcon,
                        contentDescription = "+",
                    )
                }
            }

            // Bouton "J'ai mange ca" (RECO-03)
            Button(
                onClick = onAteThis,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(Strings.RECO_ATE_THIS)
            }
        }
    }
}
