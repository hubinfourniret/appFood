package com.appfood.shared.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.appfood.shared.ui.Strings

// Restaurant/plate icon for dashboard empty state
private val RestaurantIcon: ImageVector by lazy {
    ImageVector.Builder("Restaurant", 24.dp, 24.dp, 24f, 24f)
        .path(fill = SolidColor(Color.Black)) {
            moveTo(11f, 9f)
            horizontalLineTo(9f)
            verticalLineTo(2f)
            horizontalLineTo(7f)
            verticalLineTo(9f)
            horizontalLineTo(5f)
            verticalLineTo(2f)
            horizontalLineTo(3f)
            verticalLineTo(9f)
            curveTo(3f, 11.12f, 4.66f, 12.84f, 6.75f, 12.97f)
            verticalLineTo(22f)
            horizontalLineTo(8.25f)
            verticalLineTo(12.97f)
            curveTo(10.34f, 12.84f, 12f, 11.12f, 12f, 9f)
            verticalLineTo(2f)
            horizontalLineTo(11f)
            close()
            moveTo(16f, 6f)
            verticalLineTo(14f)
            horizontalLineTo(18.5f)
            verticalLineTo(22f)
            horizontalLineTo(20f)
            verticalLineTo(2f)
            curveTo(17.79f, 2f, 16f, 3.79f, 16f, 6f)
            close()
        }.build()
}

// Star icon for favorites empty state
private val StarOutlineIcon: ImageVector by lazy {
    ImageVector.Builder("StarOutline", 24.dp, 24.dp, 24f, 24f)
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

// Notepad/journal icon for journal empty state
private val NoteAddIcon: ImageVector by lazy {
    ImageVector.Builder("NoteAdd", 24.dp, 24.dp, 24f, 24f)
        .path(fill = SolidColor(Color.Black)) {
            moveTo(14f, 2f)
            horizontalLineTo(6f)
            curveTo(4.9f, 2f, 4f, 2.9f, 4f, 4f)
            verticalLineTo(20f)
            curveTo(4f, 21.1f, 4.9f, 22f, 6f, 22f)
            horizontalLineTo(18f)
            curveTo(19.1f, 22f, 20f, 21.1f, 20f, 20f)
            verticalLineTo(8f)
            close()
            moveTo(16f, 16f)
            horizontalLineTo(13f)
            verticalLineTo(19f)
            horizontalLineTo(11f)
            verticalLineTo(16f)
            horizontalLineTo(8f)
            verticalLineTo(14f)
            horizontalLineTo(11f)
            verticalLineTo(11f)
            horizontalLineTo(13f)
            verticalLineTo(14f)
            horizontalLineTo(16f)
            close()
            moveTo(13f, 9f)
            verticalLineTo(3.5f)
            lineTo(18.5f, 9f)
            close()
        }.build()
}

/**
 * Generic empty state composable (UX-02).
 * Displays an icon, title, message, and optional call-to-action button.
 */
@Composable
fun EmptyState(
    message: String = Strings.EMPTY_STATE_DEFAULT,
    title: String? = null,
    icon: ImageVector? = null,
    buttonText: String? = null,
    onButtonClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (buttonText != null && onButtonClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onButtonClick) {
                Text(text = buttonText)
            }
        }
    }
}

/**
 * Empty state for the dashboard (UX-02).
 * Welcome message guiding the user to add their first meal.
 */
@Composable
fun EmptyDashboardState(
    onAddFirstMeal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EmptyState(
        title = Strings.EMPTY_DASHBOARD_TITLE,
        message = Strings.EMPTY_DASHBOARD_MESSAGE,
        icon = RestaurantIcon,
        buttonText = Strings.EMPTY_DASHBOARD_CTA,
        onButtonClick = onAddFirstMeal,
        modifier = modifier,
    )
}

/**
 * Empty state for the journal (UX-02).
 * Encourages the user to add their first food entry.
 */
@Composable
fun EmptyJournalState(
    onAddEntry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EmptyState(
        title = Strings.EMPTY_JOURNAL_TITLE,
        message = Strings.EMPTY_JOURNAL_MESSAGE,
        icon = NoteAddIcon,
        buttonText = Strings.EMPTY_JOURNAL_CTA,
        onButtonClick = onAddEntry,
        modifier = modifier,
    )
}

/**
 * Empty state for the favorites section (UX-02).
 * Explains what favorites are for.
 */
@Composable
fun EmptyFavoritesState(
    modifier: Modifier = Modifier,
) {
    EmptyState(
        title = Strings.EMPTY_FAVORITES_TITLE,
        message = Strings.EMPTY_FAVORITES_MESSAGE,
        icon = StarOutlineIcon,
        modifier = modifier,
    )
}
