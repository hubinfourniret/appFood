package com.appfood.shared.ui.journal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appfood.shared.model.Aliment
import com.appfood.shared.ui.Strings

/**
 * Favorites section displayed at the top of the search screen (JOURNAL-03).
 * Shows user's favorite aliments for quick access.
 */
@Composable
fun FavoritesSection(
    favorites: List<Aliment>,
    onAlimentSelected: (Aliment) -> Unit,
    onToggleFavorite: (Aliment) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (favorites.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = Strings.JOURNAL_FAVORITES_TITLE,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        favorites.forEach { aliment ->
            AlimentResultItem(
                aliment = aliment,
                isFavorite = true,
                onClick = { onAlimentSelected(aliment) },
                onToggleFavorite = { onToggleFavorite(aliment) },
            )
        }
    }
}
