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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shared shimmer alpha animation for all skeleton variants.
 */
@Composable
private fun rememberShimmerAlpha(): Float {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    return alpha
}

/**
 * Basic skeleton with configurable number of lines (UX-03).
 */
@Composable
fun LoadingSkeleton(
    lines: Int = 3,
    lineHeight: Dp = 16.dp,
    modifier: Modifier = Modifier,
) {
    val alpha = rememberShimmerAlpha()

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

/**
 * Single skeleton line element.
 */
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

/**
 * Skeleton card mimicking a content card (UX-03).
 */
@Composable
fun LoadingSkeletonCard(
    modifier: Modifier = Modifier,
) {
    val alpha = rememberShimmerAlpha()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        // Title
        SkeletonLine(widthFraction = 0.6f, height = 20.dp, alpha = alpha)
        Spacer(modifier = Modifier.height(8.dp))

        // Body
        SkeletonLine(widthFraction = 1f, height = 14.dp, alpha = alpha)
        Spacer(modifier = Modifier.height(6.dp))
        SkeletonLine(widthFraction = 0.8f, height = 14.dp, alpha = alpha)
        Spacer(modifier = Modifier.height(12.dp))

        // Footer
        SkeletonLine(widthFraction = 0.4f, height = 12.dp, alpha = alpha)
    }
}

/**
 * Dashboard skeleton screen (UX-03).
 * Mimics the layout of the dashboard: calories card, nutriment sections, meals section.
 * Used instead of a full-screen spinner for a smoother perceived loading experience.
 */
@Composable
fun DashboardLoadingSkeleton(
    modifier: Modifier = Modifier,
) {
    val alpha = rememberShimmerAlpha()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Date placeholder
        SkeletonLine(widthFraction = 0.35f, height = 14.dp, alpha = alpha)

        // Calories summary card skeleton
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            ),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SkeletonLine(widthFraction = 0.3f, height = 14.dp, alpha = alpha)
                SkeletonLine(widthFraction = 0.6f, height = 28.dp, alpha = alpha)
                SkeletonLine(widthFraction = 1f, height = 8.dp, alpha = alpha)
            }
        }

        // Nutriment section skeleton (macros)
        SkeletonNutrimentSection(alpha = alpha, itemCount = 4)

        // Nutriment section skeleton (vitamins)
        SkeletonNutrimentSection(alpha = alpha, itemCount = 3)

        // Meals section skeleton
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SkeletonLine(widthFraction = 0.4f, height = 18.dp, alpha = alpha)
                repeat(4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .graphicsLayer { this.alpha = alpha }
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            SkeletonLine(widthFraction = 0.4f, height = 14.dp, alpha = alpha)
                        }
                        SkeletonLine(widthFraction = 0.2f, height = 14.dp, alpha = alpha)
                    }
                }
            }
        }
    }
}

/**
 * Skeleton for a nutriment section card.
 */
@Composable
private fun SkeletonNutrimentSection(
    alpha: Float,
    itemCount: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SkeletonLine(widthFraction = 0.45f, height = 18.dp, alpha = alpha)
            repeat(itemCount) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        SkeletonLine(widthFraction = 0.3f, height = 12.dp, alpha = alpha)
                        SkeletonLine(widthFraction = 0.15f, height = 12.dp, alpha = alpha)
                    }
                    SkeletonLine(widthFraction = 1f, height = 6.dp, alpha = alpha)
                }
            }
        }
    }
}

/**
 * Search loading indicator (UX-03).
 * Displays a compact skeleton inside the search results area.
 */
@Composable
fun SearchLoadingSkeleton(
    modifier: Modifier = Modifier,
) {
    val alpha = rememberShimmerAlpha()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(5) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    SkeletonLine(widthFraction = 0.7f, height = 16.dp, alpha = alpha)
                    SkeletonLine(widthFraction = 0.4f, height = 12.dp, alpha = alpha)
                }
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .graphicsLayer { this.alpha = alpha }
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                )
            }
        }
    }
}
