package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.R

val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val BungeeFont = GoogleFont("Bungee")
val PressStartFont = GoogleFont("Press Start 2P")
val ChakraPetchFont = GoogleFont("Chakra Petch")
val RighteousFont = GoogleFont("Righteous")
val OrbitronFont = GoogleFont("Orbitron")

val BungeeFontFamily = FontFamily(
    Font(googleFont = BungeeFont, fontProvider = googleFontProvider)
)

val PressStartFontFamily = FontFamily(
    Font(googleFont = PressStartFont, fontProvider = googleFontProvider)
)

val ChakraPetchFontFamily = FontFamily(
    Font(googleFont = ChakraPetchFont, fontProvider = googleFontProvider)
)

val RighteousFontFamily = FontFamily(
    Font(googleFont = RighteousFont, fontProvider = googleFontProvider)
)

val OrbitronFontFamily = FontFamily(
    Font(googleFont = OrbitronFont, fontProvider = googleFontProvider)
)

enum class TitleFontPreset(val displayName: String, val fontFamily: FontFamily) {
    ARCADE_3D("Arcade Bungee", BungeeFontFamily),
    PIXEL_8BIT("Pixel 8-Bit", PressStartFontFamily),
    CYBER_PUNK("Cyber Petch", ChakraPetchFontFamily),
    RETRO_DISPLAY("Retro Display", RighteousFontFamily),
    SCI_FI_ORBIT("Sci-Fi Orbit", OrbitronFontFamily),
    CLASSIC_MONO("Classic Mono", FontFamily.Monospace)
}

// Set of Material typography styles to start with
val Typography =
  Typography(
    bodyLarge =
      TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.5.sp,
      ),
    titleMedium =
      TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.5.sp,
      ),
    labelSmall =
      TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
      )
  )

