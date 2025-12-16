package com.d4viddf.silksongwaiting.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.d4viddf.silksongwaiting.R

// --- 1. Colors ---
// The user requested source color
val SeedColor = Color(0xFF041219)

// A dark scheme seeded from 0xFF041219 (Deep Void Blue)
// In a real app, you might use MaterialTheme builder, but here are manually tuned matches:
val SilkSongScheme = darkColorScheme(
    primary = Color(0xFF82D3E0),      // Light cyan (Silk/Soul energy)
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF9EEFFD),
    secondary = Color(0xFFFF5252),    // Hornet Red accent
    onSecondary = Color(0xFF410002),
    background = SeedColor,           // Your requested background
    surface = SeedColor,              // Matching surface
    onBackground = Color(0xFFE1E2E4),
    onSurface = Color(0xFFE1E2E4)
)

// --- 2. Typography (Roboto Flex) ---
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.xml.font_certs
)

val RobotoFlex = GoogleFont("Roboto Flex")

val AppFontFamily = FontFamily(
    Font(googleFont = RobotoFlex, fontProvider = provider)
)

// Expressive Typography: Bold, Large, Readable
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Black, // Extra Bold for the "Funny/Strong" look
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold, // Bold body for "Expressive" feel
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)