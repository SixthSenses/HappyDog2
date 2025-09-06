package com.example.pet_project_frontend.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF006D3C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF8AF7B3),
    onPrimaryContainer = Color(0xFF00210F),
    secondary = Color(0xFF4D6356),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD0E8D8),
    onSecondaryContainer = Color(0xFF0A1F16),
    tertiary = Color(0xFF3B6470),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFBEEAF7),
    onTertiaryContainer = Color(0xFF001F26),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFBFDF8),
    onBackground = Color(0xFF191C1A),
    surface = Color(0xFFFBFDF8),
    onSurface = Color(0xFF191C1A)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6DD79A),
    onPrimary = Color(0xFF00391F),
    primaryContainer = Color(0xFF00522E),
    onPrimaryContainer = Color(0xFF8AF7B3),
    secondary = Color(0xFFB5CCBC),
    onSecondary = Color(0xFF213528),
    secondaryContainer = Color(0xFF374B3D),
    onSecondaryContainer = Color(0xFFD0E8D8),
    tertiary = Color(0xFFA2CDDA),
    onTertiary = Color(0xFF043542),
    tertiaryContainer = Color(0xFF214C58),
    onTertiaryContainer = Color(0xFFBEEAF7),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF111411),
    onBackground = Color(0xFFE2E3DE),
    surface = Color(0xFF111411),
    onSurface = Color(0xFFE2E3DE)
)

private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )
)

private val AppShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
