package com.appfood.shared.ui.hydratation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appfood.shared.ui.Strings

/**
 * Compact hydration widget for the dashboard.
 * Shows progress bar + quick add buttons (glass/bottle).
 * Max height ~120dp.
 */
@Composable
fun HydratationWidget(
    quantiteMl: Int,
    objectifMl: Int,
    onAddGlass: () -> Unit,
    onAddBottle: () -> Unit,
    onNavigateToHydratation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val percentage = if (objectifMl > 0) {
        (quantiteMl.toFloat() / objectifMl).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 600),
    )

    val progressColor = when {
        percentage >= 0.8f -> HydraGreen
        percentage >= 0.5f -> HydraBlue
        else -> HydraLightBlue
    }

    Card(
        onClick = onNavigateToHydratation,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Title + progress text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = Strings.HYDRA_TITLE,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = Strings.hydraProgress(quantiteMl, objectifMl),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Progress bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
            )

            // Quick add buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onAddGlass,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = Strings.HYDRA_ADD_GLASS,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                Button(
                    onClick = onAddBottle,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = Strings.HYDRA_ADD_BOTTLE,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

// Hydration-specific colors
private val HydraGreen = Color(0xFF4CAF50)
private val HydraBlue = Color(0xFF2196F3)
private val HydraLightBlue = Color(0xFF64B5F6)
