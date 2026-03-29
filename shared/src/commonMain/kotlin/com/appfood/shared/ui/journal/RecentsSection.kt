package com.appfood.shared.ui.journal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.appfood.shared.model.MealType
import com.appfood.shared.ui.Strings

/**
 * Recents section displayed on the search screen (JOURNAL-04).
 * Shows the 10-20 most recent food entries for quick re-entry.
 * A tap re-adds the aliment with the same quantity (modifiable via PortionSelector).
 */
@Composable
fun RecentsSection(
    recents: List<RecentEntry>,
    onRecentTap: (RecentEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (recents.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = Strings.JOURNAL_RECENTS_TITLE,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        recents.forEach { entry ->
            RecentEntryItem(
                entry = entry,
                onClick = { onRecentTap(entry) },
            )
        }
    }
}

@Composable
private fun RecentEntryItem(
    entry: RecentEntry,
    onClick: () -> Unit,
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
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = entry.aliment.nom,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${entry.quantiteGrammes.toLong()}g - ${recentMealLabel(entry.mealType)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = Strings.journalCaloriesShort(
                    entry.aliment.nutrimentsPour100g.calories * entry.quantiteGrammes / 100.0,
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private fun recentMealLabel(mealType: MealType): String = when (mealType) {
    MealType.PETIT_DEJEUNER -> Strings.JOURNAL_MEAL_BREAKFAST
    MealType.DEJEUNER -> Strings.JOURNAL_MEAL_LUNCH
    MealType.DINER -> Strings.JOURNAL_MEAL_DINNER
    MealType.COLLATION -> Strings.JOURNAL_MEAL_SNACK
}
