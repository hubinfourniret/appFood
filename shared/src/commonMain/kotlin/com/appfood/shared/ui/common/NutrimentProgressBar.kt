package com.appfood.shared.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

/**
 * Progress bar for nutriment tracking.
 * Color coded: green if >=80%, orange 50-80%, red <50%.
 *
 * @param label Name of the nutriment (e.g. "Proteines")
 * @param current Current consumed value
 * @param target Target value (quota)
 * @param unit Unit label (e.g. "g", "mg", "kcal")
 */
@Composable
fun NutrimentProgressBar(
    label: String,
    current: Double,
    target: Double,
    unit: String,
    modifier: Modifier = Modifier,
) {
    val percentage = if (target > 0) (current / target).coerceIn(0.0, 1.5) else 0.0
    val displayPercentage = (percentage * 100).toInt()

    val animatedProgress by animateFloatAsState(
        targetValue = percentage.toFloat().coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
    )

    val progressColor = when {
        percentage >= 0.8 -> NutrimentGreen
        percentage >= 0.5 -> NutrimentOrange
        else -> NutrimentRed
    }

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${formatValue(current)} / ${formatValue(target)} $unit ($displayPercentage%)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
        )
    }
}

private fun formatValue(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else if (value < 1.0) {
        val rounded = kotlin.math.round(value * 100.0) / 100.0
        rounded.toString()
    } else {
        val rounded = kotlin.math.round(value * 10.0) / 10.0
        rounded.toString()
    }
}

// Color constants for nutriment progress — not from theme since they have semantic meaning
private val NutrimentGreen = Color(0xFF4CAF50)
private val NutrimentOrange = Color(0xFFFF9800)
private val NutrimentRed = Color(0xFFF44336)
