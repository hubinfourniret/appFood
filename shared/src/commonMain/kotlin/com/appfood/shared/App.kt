package com.appfood.shared

import androidx.compose.runtime.Composable
import com.appfood.shared.ui.navigation.AppNavigation
import com.appfood.shared.ui.theme.AppFoodTheme

@Composable
fun App() {
    AppFoodTheme {
        AppNavigation()
    }
}
