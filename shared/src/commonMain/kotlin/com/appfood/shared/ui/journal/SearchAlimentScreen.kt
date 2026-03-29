package com.appfood.shared.ui.journal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.appfood.shared.model.Aliment
import com.appfood.shared.ui.Strings
import com.appfood.shared.ui.common.EmptyState
import com.appfood.shared.ui.common.ErrorMessage

// Search icon
private val SearchIcon: ImageVector by lazy {
    ImageVector.Builder("Search", 24.dp, 24.dp, 24f, 24f)
        .path(fill = SolidColor(Color.Black)) {
            moveTo(15.5f, 14f)
            horizontalLineTo(14.71f)
            lineTo(14.43f, 13.73f)
            curveTo(15.41f, 12.59f, 16f, 11.11f, 16f, 9.5f)
            curveTo(16f, 5.91f, 13.09f, 3f, 9.5f, 3f)
            curveTo(5.91f, 3f, 3f, 5.91f, 3f, 9.5f)
            curveTo(3f, 13.09f, 5.91f, 16f, 9.5f, 16f)
            curveTo(11.11f, 16f, 12.59f, 15.41f, 13.73f, 14.43f)
            lineTo(14f, 14.71f)
            verticalLineTo(15.5f)
            lineTo(19f, 20.49f)
            lineTo(20.49f, 19f)
            close()
            moveTo(9.5f, 14f)
            curveTo(7.01f, 14f, 5f, 11.99f, 5f, 9.5f)
            curveTo(5f, 7.01f, 7.01f, 5f, 9.5f, 5f)
            curveTo(11.99f, 5f, 14f, 7.01f, 14f, 9.5f)
            curveTo(14f, 11.99f, 11.99f, 14f, 9.5f, 14f)
            close()
        }.build()
}

// Back arrow icon
private val BackIcon: ImageVector by lazy {
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

// Star icon (favorite)
private val StarFilledIcon: ImageVector by lazy {
    ImageVector.Builder("StarFilled", 24.dp, 24.dp, 24f, 24f)
        .path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 17.27f)
            lineTo(18.18f, 21f)
            lineTo(16.54f, 13.97f)
            lineTo(22f, 9.24f)
            lineTo(14.81f, 8.63f)
            lineTo(12f, 2f)
            lineTo(9.19f, 8.63f)
            lineTo(2f, 9.24f)
            lineTo(7.46f, 13.97f)
            lineTo(5.82f, 21f)
            close()
        }.build()
}

private val StarOutlinedIcon: ImageVector by lazy {
    ImageVector.Builder("StarOutlined", 24.dp, 24.dp, 24f, 24f)
        .path(fill = SolidColor(Color.Black)) {
            moveTo(22f, 9.24f)
            lineTo(14.81f, 8.62f)
            lineTo(12f, 2f)
            lineTo(9.19f, 8.63f)
            lineTo(2f, 9.24f)
            lineTo(7.46f, 13.97f)
            lineTo(5.82f, 21f)
            lineTo(12f, 17.27f)
            lineTo(18.18f, 21f)
            lineTo(16.54f, 13.97f)
            close()
            moveTo(12f, 15.4f)
            lineTo(8.24f, 17.67f)
            lineTo(9.24f, 13.39f)
            lineTo(5.92f, 10.51f)
            lineTo(10.3f, 10.13f)
            lineTo(12f, 6.1f)
            lineTo(13.71f, 10.14f)
            lineTo(18.09f, 10.52f)
            lineTo(14.77f, 13.4f)
            lineTo(15.77f, 17.68f)
            close()
        }.build()
}

/**
 * Search food screen (JOURNAL-01, JOURNAL-03, JOURNAL-04).
 * Shows search bar, favorites section, recents section, and search results.
 */
@Composable
fun SearchAlimentScreen(
    viewModel: JournalViewModel,
    onNavigateBack: () -> Unit,
    onAlimentSelected: () -> Unit,
) {
    val searchState by viewModel.searchState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val recents by viewModel.recents.collectAsState()

    SearchAlimentContent(
        searchQuery = searchQuery,
        searchState = searchState,
        favorites = favorites,
        recents = recents,
        isFavorite = viewModel::isFavorite,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onAlimentSelected = { aliment ->
            viewModel.onAlimentSelected(aliment)
            onAlimentSelected()
        },
        onToggleFavorite = viewModel::onToggleFavorite,
        onRecentTap = { entry ->
            viewModel.onRecentEntryTap(entry)
            onAlimentSelected()
        },
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchAlimentContent(
    searchQuery: String,
    searchState: SearchState,
    favorites: List<Aliment>,
    recents: List<RecentEntry>,
    isFavorite: (String) -> Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onAlimentSelected: (Aliment) -> Unit,
    onToggleFavorite: (Aliment) -> Unit,
    onRecentTap: (RecentEntry) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.JOURNAL_SEARCH_TITLE) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = BackIcon,
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
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(Strings.JOURNAL_SEARCH_PLACEHOLDER) },
                leadingIcon = {
                    Icon(
                        imageVector = SearchIcon,
                        contentDescription = null,
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                // Favorites section (JOURNAL-03)
                if (favorites.isNotEmpty() && searchQuery.isEmpty()) {
                    item {
                        FavoritesSection(
                            favorites = favorites,
                            onAlimentSelected = onAlimentSelected,
                            onToggleFavorite = onToggleFavorite,
                        )
                    }
                }

                // Recents section (JOURNAL-04)
                if (recents.isNotEmpty() && searchQuery.isEmpty()) {
                    item {
                        RecentsSection(
                            recents = recents,
                            onRecentTap = onRecentTap,
                        )
                    }
                }

                // Search results
                when (searchState) {
                    is SearchState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    is SearchState.Results -> {
                        items(
                            items = searchState.aliments,
                            key = { it.id },
                        ) { aliment ->
                            AlimentResultItem(
                                aliment = aliment,
                                isFavorite = isFavorite(aliment.id),
                                onClick = { onAlimentSelected(aliment) },
                                onToggleFavorite = { onToggleFavorite(aliment) },
                            )
                        }
                    }

                    is SearchState.Empty -> {
                        item {
                            EmptyState(
                                message = Strings.JOURNAL_SEARCH_NO_RESULTS,
                            )
                        }
                    }

                    is SearchState.Error -> {
                        item {
                            ErrorMessage(
                                message = searchState.message,
                            )
                        }
                    }

                    is SearchState.Idle -> {
                        if (favorites.isEmpty() && recents.isEmpty()) {
                            item {
                                EmptyState(
                                    message = Strings.JOURNAL_SEARCH_HINT,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Single search result item for an aliment.
 */
@Composable
fun AlimentResultItem(
    aliment: Aliment,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = aliment.nom,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (aliment.marque != null) {
                    Text(
                        text = aliment.marque,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Strings.journalCaloriesPer100g(aliment.nutrimentsPour100g.calories),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) StarFilledIcon else StarOutlinedIcon,
                    contentDescription = if (isFavorite) {
                        Strings.JOURNAL_REMOVE_FAVORITE
                    } else {
                        Strings.JOURNAL_ADD_FAVORITE
                    },
                    tint = if (isFavorite) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}
