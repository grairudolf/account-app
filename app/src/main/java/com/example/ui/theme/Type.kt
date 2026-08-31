package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// Plus Jakarta Sans for Headings & Titles (Bundled Font Family)
val HeadingFontFamily = FontFamily(
    Font(R.font.plusjakartasans_extralight, weight = FontWeight.ExtraLight),
    Font(R.font.plusjakartasans_extralightitalic, weight = FontWeight.ExtraLight, style = FontStyle.Italic),
    Font(R.font.plusjakartasans_light, weight = FontWeight.Light),
    Font(R.font.plusjakartasans_lightitalic, weight = FontWeight.Light, style = FontStyle.Italic),
    Font(R.font.plusjakartasans_regular, weight = FontWeight.Normal),
    Font(R.font.plusjakartasans_italic, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(R.font.plusjakartasans_medium, weight = FontWeight.Medium),
    Font(R.font.plusjakartasans_mediumitalic, weight = FontWeight.Medium, style = FontStyle.Italic),
    Font(R.font.plusjakartasans_semibold, weight = FontWeight.SemiBold),
    Font(R.font.plusjakartasans_semibolditalic, weight = FontWeight.SemiBold, style = FontStyle.Italic),
    Font(R.font.plusjakartasans_bold, weight = FontWeight.Bold),
    Font(R.font.plusjakartasans_bolditalic, weight = FontWeight.Bold, style = FontStyle.Italic),
    Font(R.font.plusjakartasans_extrabold, weight = FontWeight.ExtraBold),
    Font(R.font.plusjakartasans_extrabolditalic, weight = FontWeight.ExtraBold, style = FontStyle.Italic)
)

// Outfit for Body text & Labels (Bundled Font Family)
val BodyFontFamily = FontFamily(
    Font(R.font.outfit_thin, weight = FontWeight.Thin),
    Font(R.font.outfit_extralight, weight = FontWeight.ExtraLight),
    Font(R.font.outfit_light, weight = FontWeight.Light),
    Font(R.font.outfit_regular, weight = FontWeight.Normal),
    Font(R.font.outfit_medium, weight = FontWeight.Medium),
    Font(R.font.outfit_semibold, weight = FontWeight.SemiBold),
    Font(R.font.outfit_bold, weight = FontWeight.Bold),
    Font(R.font.outfit_extrabold, weight = FontWeight.ExtraBold),
    Font(R.font.outfit_black, weight = FontWeight.Black)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = HeadingFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = HeadingFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = HeadingFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = HeadingFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = HeadingFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = HeadingFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    titleLarge = TextStyle(
        fontFamily = HeadingFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    titleMedium = TextStyle(
        fontFamily = HeadingFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = HeadingFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    ),
    labelSmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)
