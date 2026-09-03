package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val LightAppColorScheme = lightColorScheme(
    primary = BrandDarkNavy,
    onPrimary = Color.White,
    primaryContainer = LightBlueContainer,
    onPrimaryContainer = BrandDarkNavy,
    secondary = BrandWarmGold,
    onSecondary = BrandDarkNavy,
    secondaryContainer = LightBlueContainer,
    onSecondaryContainer = BrandDarkNavy,
    tertiary = StreakGold,
    onTertiary = Color.White,
    tertiaryContainer = StreakGoldContainer,
    onTertiaryContainer = StreakGoldDark,
    background = AppBackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = BrandSlateBlue,
    outline = SurfaceBorderLight,
    error = StatusError,
    onError = Color.White
)

private val DarkAppColorScheme = darkColorScheme(
    primary = BrandChampagneGold,
    onPrimary = Color(0xFF322406),
    primaryContainer = Color(0xFF3F3011),
    onPrimaryContainer = Color(0xFFFFE0A1),
    secondary = BrandWarmGold,
    onSecondary = Color(0xFF281F08),
    secondaryContainer = Color(0xFF24303E),
    onSecondaryContainer = Color(0xFFE2E6EE),
    tertiary = BrandAmberGold,
    onTertiary = Color(0xFF322406),
    tertiaryContainer = Color(0xFF382C10),
    onTertiaryContainer = Color(0xFFFFE7B8),
    background = AppBackgroundDark,
    onBackground = TextPrimaryNight,
    surface = SurfaceDark,
    onSurface = TextPrimaryNight,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryNight,
    outline = SurfaceBorderDark,
    error = StatusError,
    onError = Color.White
)

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

@Composable
fun CmfiTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) DarkAppColorScheme else LightAppColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}

