package com.example.pet_project_frontend.presentation.mypage.common

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat

@Composable
fun setStatusBarColor(color: Color, darkIcons: Boolean = true) {
    val activity = LocalContext.current as Activity
    val window = activity.window
    val insets = WindowCompat.getInsetsController(window, window.decorView)
    val oldColor = window.statusBarColor
    val oldLight = insets.isAppearanceLightStatusBars

    DisposableEffect(color, darkIcons) {
        window.statusBarColor = color.toArgb()
        insets.isAppearanceLightStatusBars = darkIcons
        onDispose {
            // 원상복구
            window.statusBarColor = oldColor
            insets.isAppearanceLightStatusBars = oldLight
        }
    }
}
