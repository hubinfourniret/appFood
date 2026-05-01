package com.appfood.shared.ui.journal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.appfood.shared.ui.Strings
import com.appfood.shared.ui.common.EmptyState
import com.appfood.shared.ui.common.SearchNoResultsMessage

private val BackArrowIconRecette: ImageVector by lazy {
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
 * TACHE-510 v3 : ecran de recherche des recettes pour ajout au journal,
 * symetrique a SearchAlimentScreen. Le mealType selectionne dans AddEntry
 * est transmis a RecetteDetail via la route pour skip le dialog de selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchRecetteScreen(
    viewModel: JournalViewModel,
    onNavigateBack: () -> Unit,
    onRecetteClick: (recetteId: String) -> Unit,
) {
    val recetteSearchQuery by viewModel.recetteSearchQuery.collectAsState()
    val recetteSearchResults by viewModel.recetteSearchResults.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.JOURNAL_SEARCH_RECETTE_PLACEHOLDER) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = BackArrowIconRecette,
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
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = recetteSearchQuery,
                onValueChange = viewModel::onRecetteSearchQueryChanged,
                placeholder = { Text(Strings.JOURNAL_SEARCH_RECETTE_PLACEHOLDER) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            when {
                recetteSearchResults.isEmpty() && recetteSearchQuery.isNotBlank() -> {
                    SearchNoResultsMessage()
                }
                recetteSearchResults.isEmpty() && recetteSearchQuery.isBlank() -> {
                    EmptyState(message = Strings.JOURNAL_SEARCH_HINT)
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(
                            items = recetteSearchResults,
                            key = { it.id },
                        ) { recette ->
                            RecetteResultCard(
                                recette = recette,
                                onClick = { onRecetteClick(recette.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecetteResultCard(
    recette: RecetteSearchResult,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = recette.nom,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = Strings.recetteTempsPrep(recette.tempsPreparationMin),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
