package com.appfood.shared.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

// Warning icon
private val WarningIcon: ImageVector by lazy {
    ImageVector.Builder("Warning", 24.dp, 24.dp, 24f, 24f)
        .path(fill = SolidColor(Color.Black)) {
            moveTo(1f, 21f)
            horizontalLineTo(23f)
            lineTo(12f, 2f)
            close()
            moveTo(13f, 18f)
            horizontalLineTo(11f)
            verticalLineTo(16f)
            horizontalLineTo(13f)
            close()
            moveTo(13f, 14f)
            horizontalLineTo(11f)
            verticalLineTo(10f)
            horizontalLineTo(13f)
            close()
        }.build()
}

// Cloud off icon (network error)
private val CloudOffIcon: ImageVector by lazy {
    ImageVector.Builder("CloudOff", 24.dp, 24.dp, 24f, 24f)
        .path(fill = SolidColor(Color.Black)) {
            moveTo(19.35f, 10.04f)
            curveTo(18.67f, 6.59f, 15.64f, 4f, 12f, 4f)
            curveTo(9.11f, 4f, 6.6f, 5.64f, 5.35f, 8.04f)
            curveTo(2.34f, 8.36f, 0f, 10.91f, 0f, 14f)
            curveTo(0f, 17.31f, 2.69f, 20f, 6f, 20f)
            horizontalLineTo(19f)
            curveTo(21.76f, 20f, 24f, 17.76f, 24f, 15f)
            curveTo(24f, 12.36f, 21.95f, 10.22f, 19.35f, 10.04f)
            close()
            moveTo(19f, 18f)
            horizontalLineTo(6f)
            curveTo(3.79f, 18f, 2f, 16.21f, 2f, 14f)
            curveTo(2f, 11.79f, 3.79f, 10f, 6f, 10f)
            horizontalLineTo(6.71f)
            curveTo(7.37f, 7.69f, 9.48f, 6f, 12f, 6f)
            curveTo(14.76f, 6f, 17.09f, 7.99f, 17.56f, 10.65f)
            lineTo(17.79f, 11.93f)
            lineTo(19.1f, 12.04f)
            curveTo(20.68f, 12.18f, 22f, 13.45f, 22f, 15f)
            curveTo(22f, 16.65f, 20.65f, 18f, 19f, 18f)
            close()
        }.build()
}

// Search off icon
private val SearchOffIcon: ImageVector by lazy {
    ImageVector.Builder("SearchOff", 24.dp, 24.dp, 24f, 24f)
        .path(fill = SolidColor(Color.Black)) {
            moveTo(15.5f, 14f)
            horizontalLineTo(14.71f)
            lineTo(14.43f, 13.73f)
            curveTo(15.41f, 12.59f, 16f, 11.11f, 16f, 9.5f)
            curveTo(16f, 5.91f, 13.09f, 3f, 9.5f, 3f)
            curveTo(5.91f, 3f, 3f, 5.91f, 3f, 9.5f)
            curveTo(3f, 13.09f, 5.91f, 16f, 9.5f, 16f)
            curveTo(11.11f, 16f, 12.59f, 15.41f, 13.73f, 14.43f)
            lineTo(14f, 14.71f)
            verticalLineTo(15.5f)
            lineTo(19f, 20.49f)
            lineTo(20.49f, 19f)
            close()
            moveTo(9.5f, 14f)
            curveTo(7.01f, 14f, 5f, 11.99f, 5f, 9.5f)
            curveTo(5f, 7.01f, 7.01f, 5f, 9.5f, 5f)
            curveTo(11.99f, 5f, 14f, 7.01f, 14f, 9.5f)
            curveTo(14f, 11.99f, 11.99f, 14f, 9.5f, 14f)
            close()
        }.build()
}

/**
 * Generic error message composable (UX-04).
 * Displays an error message with optional icon and retry action.
 */
@Composable
fun ErrorMessage(
    message: String = Strings.ERROR_DEFAULT,
    title: String? = null,
    icon: ImageVector? = null,
    onRetry: (() -> Unit)? = null,
    retryText: String = Strings.RETRY,
    secondaryActionText: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
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
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error,
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

        if (onRetry != null || onSecondaryAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (secondaryActionText != null && onSecondaryAction != null) {
                    OutlinedButton(onClick = onSecondaryAction) {
                        Text(text = secondaryActionText)
                    }
                }
                if (onRetry != null) {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                    ) {
                        Text(text = retryText)
                    }
                }
            }
        }
    }
}

/**
 * Network error composable (UX-04).
 * Shown when the device is offline or a network request fails.
 */
@Composable
fun NetworkErrorMessage(
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    ErrorMessage(
        message = Strings.ERROR_NETWORK,
        icon = CloudOffIcon,
        onRetry = onRetry,
        modifier = modifier,
    )
}

/**
 * Server error composable (UX-04).
 * Shown when the server returns an unexpected error.
 */
@Composable
fun ServerErrorMessage(
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    ErrorMessage(
        message = Strings.ERROR_SERVER,
        icon = WarningIcon,
        onRetry = onRetry,
        modifier = modifier,
    )
}

/**
 * Search no-results composable (UX-04).
 * Shown when a search returns zero results.
 */
@Composable
fun SearchNoResultsMessage(
    onModifySearch: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    ErrorMessage(
        message = Strings.ERROR_SEARCH_NO_RESULTS,
        icon = SearchOffIcon,
        secondaryActionText = if (onModifySearch != null) Strings.ERROR_MODIFY_SEARCH else null,
        onSecondaryAction = onModifySearch,
        modifier = modifier,
    )
}

/**
 * Search failed composable (UX-04).
 * Shown when a search request fails.
 */
@Composable
fun SearchErrorMessage(
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    ErrorMessage(
        message = Strings.ERROR_SEARCH_FAILED,
        icon = CloudOffIcon,
        onRetry = onRetry,
        modifier = modifier,
    )
}
