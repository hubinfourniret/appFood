package com.appfood.shared.ui.recette

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appfood.shared.model.Recette
import com.appfood.shared.ui.Strings
import com.appfood.shared.ui.common.EmptyState
import com.appfood.shared.ui.common.ErrorMessage
import com.appfood.shared.ui.common.LoadingSkeleton

private val BackArrowIconMy: ImageVector by lazy {
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

private val PlusIcon: ImageVector by lazy {
    ImageVector.Builder("Plus", 24.dp, 24.dp, 24f, 24f)
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

/**
 * TACHE-516 : ecran "Mes recettes" — liste les recettes personnelles + bouton creation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRecettesScreen(
    viewModel: RecettesViewModel,
    onNavigateBack: () -> Unit,
    onRecetteClick: (recetteId: String) -> Unit,
    onEditRecette: (recetteId: String) -> Unit,
    onCreateClick: () -> Unit,
) {
    val state by viewModel.myRecettesState.collectAsState()
    var deleteId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadMyRecettes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.MY_RECETTES_TITLE) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = BackArrowIconMy,
                            contentDescription = Strings.JOURNAL_BACK,
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateClick,
                icon = {
                    Icon(
                        imageVector = PlusIcon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                },
                text = { Text(Strings.MY_RECETTES_CREATE) },
            )
        },
    ) { innerPadding ->
        when (val current = state) {
            is MyRecettesState.Loading -> {
                LoadingSkeleton(lines = 6, modifier = Modifier.padding(innerPadding))
            }
            is MyRecettesState.Error -> {
                ErrorMessage(
                    message = current.message,
                    onRetry = viewModel::loadMyRecettes,
                    modifier = Modifier.padding(innerPadding),
                )
            }
            is MyRecettesState.Success -> {
                if (current.recettes.isEmpty()) {
                    EmptyState(
                        message = Strings.MY_RECETTES_EMPTY,
                        modifier = Modifier.padding(innerPadding),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(items = current.recettes, key = { it.id }) { recette ->
                            MyRecetteCard(
                                recette = recette,
                                onClick = { onRecetteClick(recette.id) },
                                onEdit = { onEditRecette(recette.id) },
                                onDelete = { deleteId = recette.id },
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }

    val pendingDeleteId = deleteId
    if (pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = { deleteId = null },
            title = { Text(Strings.RECETTE_DELETE_CONFIRM_TITLE) },
            text = { Text(Strings.RECETTE_DELETE_CONFIRM_MESSAGE) },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteMyRecette(pendingDeleteId)
                    deleteId = null
                }) { Text(Strings.RECETTE_DELETE_ACTION) }
            },
            dismissButton = {
                TextButton(onClick = { deleteId = null }) { Text(Strings.JOURNAL_DELETE_CANCEL) }
            },
        )
    }
}

@Composable
private fun MyRecetteCard(
    recette: Recette,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = recette.nom,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (recette.description.isNotBlank()) {
                Text(
                    text = recette.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = Strings.recetteTempsPrep(recette.tempsPreparationMin),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onEdit) { Text(Strings.RECETTE_EDIT_ACTION) }
                    IconButton(onClick = onDelete) {
                        Text(text = "🗑", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}
