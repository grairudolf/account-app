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
    primaryContainer = BrandDarkNavy,
    onPrimaryContainer = Color.White,
    secondary = BrandWarmGold,
    onSecondary = BrandDarkNavy,
    secondaryContainer = LightBlueContainer,
    onSecondaryContainer = BrandDarkNavy,
    tertiary = BrandVibrantYellow,
    onTertiary = BrandDarkNavy,
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
    primaryContainer = BrandDarkNavy,
    onPrimaryContainer = Color.White,
    secondary = BrandWarmGold,
    onSecondary = BrandDarkNavy,
    secondaryContainer = BrandMutedGold,
    onSecondaryContainer = BrandLightText,
    tertiary = BrandBrightYellow,
    onTertiary = BrandDarkNavy,
    background = AppBackgroundDark,
    onBackground = Color.White,
    surface = SurfaceDark,
    onSurface = Color.White,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = BrandLightText,
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

