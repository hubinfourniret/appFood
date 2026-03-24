package com.appfood.shared.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LoadingSkeleton(
    lines: Int = 3,
    lineHeight: Dp = 16.dp,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(lines) { index ->
            val widthFraction = when {
                index == 0 -> 0.7f
                index == lines - 1 -> 0.5f
                else -> 0.9f
            }
            SkeletonLine(
                widthFraction = widthFraction,
                height = lineHeight,
                alpha = alpha,
            )
        }
    }
}

@Composable
fun SkeletonLine(
    widthFraction: Float,
    height: Dp,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(MaterialTheme.shapes.small)
            .graphicsLayer { this.alpha = alpha }
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
    )
}

@Composable
fun LoadingSkeletonCard(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.7f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        )

        // Titre
        SkeletonLine(widthFraction = 0.6f, height = 20.dp, alpha = alpha)
        Spacer(modifier = Modifier.height(8.dp))

        // Corps
        SkeletonLine(widthFraction = 1f, height = 14.dp, alpha = alpha)
        Spacer(modifier = Modifier.height(6.dp))
        SkeletonLine(widthFraction = 0.8f, height = 14.dp, alpha = alpha)
        Spacer(modifier = Modifier.height(12.dp))

        // Pied
        SkeletonLine(widthFraction = 0.4f, height = 12.dp, alpha = alpha)
    }
}
