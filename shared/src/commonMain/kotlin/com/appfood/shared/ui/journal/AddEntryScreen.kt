package com.appfood.shared.ui.journal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import com.appfood.shared.model.MealType
import com.appfood.shared.ui.Strings

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
 * Main entry screen for adding a food entry (JOURNAL-01).
 * Connected composable — delegates to pure content.
 */
@Composable
fun AddEntryScreen(
    viewModel: JournalViewModel,
    initialMealType: MealType? = null,
    onNavigateBack: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onEntrySaved: () -> Unit,
) {
    val addEntryState by viewModel.addEntryState.collectAsState()
    val selectedMealType by viewModel.selectedMealType.collectAsState()

    // Apply initial meal type if provided
    LaunchedEffect(initialMealType) {
        if (initialMealType != null && selectedMealType == null) {
            viewModel.onMealTypeSelected(initialMealType)
        }
    }

    // Navigate on save success
    LaunchedEffect(addEntryState) {
        if (addEntryState is AddEntryState.Saved) {
            viewModel.resetAddEntryFlow()
            onEntrySaved()
        }
    }

    // Navigate to search when meal type is selected
    LaunchedEffect(addEntryState) {
        if (addEntryState is AddEntryState.SearchFood) {
            onNavigateToSearch()
        }
    }

    AddEntryContent(
        selectedMealType = selectedMealType,
        onMealTypeSelected = viewModel::onMealTypeSelected,
        onNavigateBack = {
            viewModel.resetAddEntryFlow()
            onNavigateBack()
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEntryContent(
    selectedMealType: MealType?,
    onMealTypeSelected: (MealType) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.JOURNAL_ADD_ENTRY_TITLE) },
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
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = Strings.JOURNAL_SELECT_MEAL_TYPE,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            MealType.entries.forEach { mealType ->
                MealTypeCard(
                    mealType = mealType,
                    isSelected = selectedMealType == mealType,
                    onClick = { onMealTypeSelected(mealType) },
                )
            }
        }
    }
}

@Composable
private fun MealTypeCard(
    mealType: MealType,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = mealTypeLabel(mealType),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = mealTypeDescription(mealType),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    },
                )
            }

            Text(
                text = mealTypeEmoji(mealType),
                style = MaterialTheme.typography.headlineMedium,
            )
        }
    }
}

private fun mealTypeLabel(mealType: MealType): String = when (mealType) {
    MealType.PETIT_DEJEUNER -> Strings.JOURNAL_MEAL_BREAKFAST
    MealType.DEJEUNER -> Strings.JOURNAL_MEAL_LUNCH
    MealType.DINER -> Strings.JOURNAL_MEAL_DINNER
    MealType.COLLATION -> Strings.JOURNAL_MEAL_SNACK
}

private fun mealTypeDescription(mealType: MealType): String = when (mealType) {
    MealType.PETIT_DEJEUNER -> Strings.JOURNAL_MEAL_BREAKFAST_DESC
    MealType.DEJEUNER -> Strings.JOURNAL_MEAL_LUNCH_DESC
    MealType.DINER -> Strings.JOURNAL_MEAL_DINNER_DESC
    MealType.COLLATION -> Strings.JOURNAL_MEAL_SNACK_DESC
}

private fun mealTypeEmoji(mealType: MealType): String = when (mealType) {
    MealType.PETIT_DEJEUNER -> "\uD83C\uDF73"
    MealType.DEJEUNER -> "\uD83C\uDF5D"
    MealType.DINER -> "\uD83C\uDF5B"
    MealType.COLLATION -> "\uD83C\uDF4E"
}
