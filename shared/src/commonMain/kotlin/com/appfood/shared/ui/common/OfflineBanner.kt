package com.appfood.shared.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import com.appfood.shared.ui.Strings

// Cloud off icon
private val CloudOffIcon: ImageVector by lazy {
    ImageVector.Builder("CloudOff", 24.dp, 24.dp, 24f, 24f)
        .path(fill = SolidColor(Color.Black)) {
            moveTo(19.35f, 10.04f)
            curveTo(18.67f, 6.59f, 15.64f, 4f, 12f, 4f)
            curveTo(10.11f, 4f, 8.39f, 4.84f, 7.16f, 6.16f)
            lineTo(8.58f, 7.58f)
            curveTo(9.5f, 6.61f, 10.69f, 6f, 12f, 6f)
            curveTo(14.69f, 6f, 16.87f, 8.12f, 17.35f, 10.79f)
            lineTo(17.55f, 11.92f)
            lineTo(18.69f, 12.04f)
            curveTo(20.05f, 12.18f, 21.1f, 13.32f, 21.1f, 14.7f)
            curveTo(21.1f, 15.42f, 20.78f, 16.07f, 20.27f, 16.52f)
            lineTo(21.69f, 17.94f)
            curveTo(22.49f, 17.13f, 23f, 15.97f, 23f, 14.7f)
            curveTo(23f, 12.48f, 21.43f, 10.63f, 19.35f, 10.04f)
            close()
            moveTo(3f, 5.27f)
            lineTo(5.75f, 8.02f)
            curveTo(3.56f, 8.46f, 2f, 10.36f, 2f, 12.6f)
            curveTo(2f, 15.07f, 3.93f, 17.1f, 6.4f, 17.1f)
            horizontalLineTo(17.73f)
            lineTo(19.73f, 19.1f)
            lineTo(21f, 17.83f)
            lineTo(4.27f, 4f)
            close()
        }.build()
}

/**
 * Banner displayed when the app is in offline mode.
 * Informs the user that data will be synced later.
 */
@Composable
fun OfflineBanner(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = CloudOffIcon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onTertiaryContainer,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = Strings.OFFLINE_BANNER_MESSAGE,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }
}
