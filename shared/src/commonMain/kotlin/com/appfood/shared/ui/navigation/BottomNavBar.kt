package com.appfood.shared.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.appfood.shared.ui.Strings

// Home icon (simple house shape)
private val HomeIcon: ImageVector by lazy {
    ImageVector.Builder("Home", 24.dp, 24.dp, 24f, 24f)
        .path(fill = SolidColor(Color.Black)) {
            moveTo(10f, 20f)
            verticalLineTo(14f)
            horizontalLineTo(14f)
            verticalLineTo(20f)
            horizontalLineTo(19f)
            verticalLineTo(12f)
            horizontalLineTo(22f)
            lineTo(12f, 3f)
            lineTo(2f, 12f)
            horizontalLineTo(5f)
            verticalLineTo(20f)
            close()
        }.build()
}

// Add/Plus icon
private val AddIcon: ImageVector by lazy {
    ImageVector.Builder("Add", 24.dp, 24.dp, 24f, 24f)
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

// Recettes icon (fork and knife simplified)
private val RecettesIcon: ImageVector by lazy {
    ImageVector.Builder("Recettes", 24.dp, 24.dp, 24f, 24f)
        .path(fill = SolidColor(Color.Black)) {
            // Fork
            moveTo(7f, 2f)
            horizontalLineTo(9f)
            verticalLineTo(10f)
            horizontalLineTo(8.25f)
            verticalLineTo(22f)
            horizontalLineTo(7.75f)
            verticalLineTo(10f)
            horizontalLineTo(7f)
            close()
            moveTo(5f, 2f)
            horizontalLineTo(6f)
            verticalLineTo(8f)
            horizontalLineTo(5f)
            close()
            moveTo(10f, 2f)
            horizontalLineTo(11f)
            verticalLineTo(8f)
            horizontalLineTo(10f)
            close()
            // Knife
            moveTo(16f, 2f)
            horizontalLineTo(18f)
            verticalLineTo(22f)
            horizontalLineTo(16f)
            close()
        }.build()
}

// Profil icon (person silhouette simplified)
private val ProfilIcon: ImageVector by lazy {
    ImageVector.Builder("Profil", 24.dp, 24.dp, 24f, 24f)
        .path(fill = SolidColor(Color.Black)) {
            // Head (square approximation of circle)
            moveTo(9f, 4f)
            horizontalLineTo(15f)
            verticalLineTo(10f)
            horizontalLineTo(9f)
            close()
            // Body
            moveTo(5f, 14f)
            horizontalLineTo(19f)
            verticalLineTo(20f)
            horizontalLineTo(5f)
            close()
        }.build()
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen,
)

private val bottomNavItemsLeft = listOf(
    BottomNavItem(label = Strings.TAB_DASHBOARD, icon = HomeIcon, screen = Screen.Dashboard),
)

private val bottomNavItemsRight = listOf(
    BottomNavItem(label = Strings.TAB_RECETTES, icon = RecettesIcon, screen = Screen.Recettes),
    BottomNavItem(label = Strings.TAB_PROFIL, icon = ProfilIcon, screen = Screen.Profil),
)

@Composable
fun BottomNavBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    onAddClick: () -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        // Left items (before central "+")
        bottomNavItemsLeft.forEach { item ->
            NavigationBarItem(
                selected = currentScreen == item.screen,
                onClick = { onNavigate(item.screen) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(text = item.label) },
            )
        }

        // Central "+" button — primary action, highlighted
        NavigationBarItem(
            selected = currentScreen is Screen.Journal,
            onClick = onAddClick,
            icon = {
                FloatingActionButton(
                    onClick = onAddClick,
                    modifier = Modifier.size(48.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                ) {
                    Icon(imageVector = AddIcon, contentDescription = Strings.TAB_ADD)
                }
            },
            label = { Text(text = Strings.TAB_ADD) },
        )

        // Right items (after central "+")
        bottomNavItemsRight.forEach { item ->
            NavigationBarItem(
                selected = currentScreen == item.screen,
                onClick = { onNavigate(item.screen) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(text = item.label) },
            )
        }
    }
}
