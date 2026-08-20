package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme

// Explicit Brand Color Palette
val BrandDarkNavy = Color(0xFF14214C)       // Dark blue (#14214c)
val BrandSlateBlue = Color(0xFF5F6987)      // Navy slate (#5f6987)
val BrandMutedGold = Color(0xFF696240)      // Muted accent fill (#696240)
val BrandWarmGold = Color(0xFFBB9828)       // Warm mid-tone gold (#bb9828)
val BrandVibrantYellow = Color(0xFFFDBC0A)  // Vibrant yellow highlight (#fdbc0a)
val BrandBrightYellow = Color(0xFFFAE611)   // Bright yellow indicator (#fae611)
val BrandLightText = Color(0xFFCACED7)      // Light text for dark containers (#caced7)

// Canvas & Surfaces
val AppBackgroundLight = Color(0xFFF8F9FA)  // Clean off-white screen canvas
val SurfaceLight = Color(0xFFFFFFFF)        // Crisp white card surface
val SurfaceVariantLight = Color(0xFFF1F3F7) // Subtle card container
val SurfaceBorderLight = Color(0xFFE2E6EF)  // Crisp border

val AppBackgroundDark = Color(0xFF0D1636)   // Deep navy dark canvas
val SurfaceDark = Color(0xFF14214C)         // Dark navy card surface
val SurfaceDarkCard = Color(0xFF14214C)     // Dark navy card surface
val SurfaceVariantDark = Color(0xFF1B2B5C)  // Elevated dark container
val SurfaceBorderDark = Color(0xFF5F6987).copy(alpha = 0.4f)

// Navigation & Pill Tokens
val DarkNavBarBackground = Color(0xFF14214C)
val DarkPillBackground = Color(0xFF14214C)
val DarkPillText = Color(0xFFFFFFFF)

// Typography
val TextPrimary = Color(0xFF141519)
val TextSecondary = Color(0xFF5F6987)
val TextMuted = Color(0xFF8C98AC)
val TextPrimaryNight = Color(0xFFFFFFFF)
val TextSecondaryNight = Color(0xFFCACED7)
val DividerColor: Color
    @Composable
    get() = MaterialTheme.colorScheme.outline

// Status Colors
val StatusSuccess = Color(0xFF10B981)
val StatusWarning = Color(0xFFFDBC0A)
val StatusError = Color(0xFFEF4444)

// Primary & Accent Aliases
val PrimaryBlue = Color(0xFF14214C)
val PrimaryBlueDark = Color(0xFF0F172A)
val SecondaryBlue = Color(0xFF5F6987)
val LightBlueContainer = Color(0xFFEEF2FB)
val PurpleContainer = Color(0xFFF3F0FF)
val VeryLightBlue = Color(0xFFF8FAFC)

// Streak & Accents
val StreakGold = Color(0xFFFDBC0A)
val StreakGoldContainer = Color(0xFFFEF3C7)
val StreakGoldDark = Color(0xFFB45309)

val AccentMint = Color(0xFF10B981)
val AccentMintContainer = Color(0xFFECFDF5)
val AccentMintDark = Color(0xFF047857)

val AccentPurple = Color(0xFF8B5CF6)
val AccentPurpleContainer = Color(0xFFF5F3FF)

// Pastel / Card Accent Containers (Clean light surfaces with vibrant accents)
val PastelLavender = Color(0xFFDDD6FE)
val PastelLavenderContainer = Color(0xFFF3F0FF)
val PastelLavenderDark = Color(0xFF6D28D9)

val PastelPeach = Color(0xFFFED7AA)
val PastelPeachContainer = Color(0xFFFFF7ED)
val PastelPeachDark = Color(0xFFC2410C)

val PastelSky = Color(0xFFBAE6FD)
val PastelSkyContainer = Color(0xFFF0F9FF)
val PastelSkyDark = Color(0xFF0369A1)

val PastelMint = Color(0xFFA7F3D0)
val PastelMintContainer = Color(0xFFECFDF5)
val PastelMintDark = Color(0xFF047857)

val PastelPink = Color(0xFFFBCFE8)
val PastelPinkContainer = Color(0xFFFDF2F8)
val PastelPinkDark = Color(0xFFBE185D)

val PastelYellow = Color(0xFFFEF08A)
val PastelYellowContainer = Color(0xFFFEFCE8)
val PastelYellowDark = Color(0xFFB45309)



