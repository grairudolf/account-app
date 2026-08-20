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
    primary = BrandVibrantYellow,
    onPrimary = BrandDarkNavy,
    primaryContainer = Color(0xFF1E2C58),
    onPrimaryContainer = Color.White,
    secondary = BrandWarmGold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1B2B5C),
    onSecondaryContainer = Color.White,
    tertiary = StreakGold,
    onTertiary = BrandDarkNavy,
    tertiaryContainer = Color(0xFF3E320B),
    onTertiaryContainer = StreakGoldContainer,
    background = AppBackgroundDark,
    onBackground = Color.White,
    surface = SurfaceDark,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E2C58),
    onSurfaceVariant = Color(0xFFD0D7E5),
    outline = Color(0xFF3B4B7C),
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

