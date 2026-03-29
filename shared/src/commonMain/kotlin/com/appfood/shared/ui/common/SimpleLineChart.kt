package com.appfood.shared.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Simple line chart composable using Canvas.
 * Draws data points connected by lines with optional labels.
 *
 * @param dataPoints List of (label, value) pairs
 * @param lineColor Color of the line
 * @param modifier Modifier for sizing
 */
@Composable
fun SimpleLineChart(
    dataPoints: List<Pair<String, Double>>,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
) {
    if (dataPoints.size < 2) return

    val textMeasurer = rememberTextMeasurer()
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val dotColor = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
    ) {
        val values = dataPoints.map { it.second }
        val minValue = values.min()
        val maxValue = values.max()
        val valueRange = (maxValue - minValue).let { if (it == 0.0) 1.0 else it }

        val paddingLeft = 48f
        val paddingRight = 16f
        val paddingTop = 16f
        val paddingBottom = 32f

        val chartWidth = size.width - paddingLeft - paddingRight
        val chartHeight = size.height - paddingTop - paddingBottom

        val stepX = chartWidth / (dataPoints.size - 1).coerceAtLeast(1)

        // Build path
        val path = Path()
        val points = dataPoints.mapIndexed { index, (_, value) ->
            val x = paddingLeft + index * stepX
            val y = paddingTop + chartHeight - ((value - minValue) / valueRange * chartHeight).toFloat()
            Offset(x, y)
        }

        points.forEachIndexed { index, point ->
            if (index == 0) {
                path.moveTo(point.x, point.y)
            } else {
                path.lineTo(point.x, point.y)
            }
        }

        // Draw line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = 3f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )

        // Draw dots
        points.forEach { point ->
            drawCircle(
                color = dotColor,
                radius = 5f,
                center = point,
            )
        }

        // Draw min/max labels on Y axis
        val labelStyle = TextStyle(
            fontSize = 10.sp,
            color = labelColor,
        )

        val maxLabel = formatChartValue(maxValue)
        val minLabel = formatChartValue(minValue)

        val maxResult = textMeasurer.measure(maxLabel, labelStyle)
        val minResult = textMeasurer.measure(minLabel, labelStyle)

        drawText(
            textLayoutResult = maxResult,
            topLeft = Offset(2f, paddingTop),
        )
        drawText(
            textLayoutResult = minResult,
            topLeft = Offset(2f, paddingTop + chartHeight - minResult.size.height),
        )

        // Draw bottom labels (show first, middle, last)
        val labelIndices = when {
            dataPoints.size <= 3 -> dataPoints.indices.toList()
            else -> listOf(0, dataPoints.size / 2, dataPoints.size - 1)
        }

        labelIndices.forEach { index ->
            val label = dataPoints[index].first
            val result = textMeasurer.measure(label, labelStyle)
            val x = paddingLeft + index * stepX - result.size.width / 2f
            drawText(
                textLayoutResult = result,
                topLeft = Offset(x.coerceIn(0f, size.width - result.size.width), size.height - paddingBottom + 4f),
            )
        }
    }
}

private fun formatChartValue(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        val rounded = kotlin.math.round(value * 10.0) / 10.0
        rounded.toString()
    }
}
