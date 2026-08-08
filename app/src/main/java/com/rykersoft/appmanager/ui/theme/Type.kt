package com.rykersoft.appmanager.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.rykersoft.appmanager.R

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
val InterFont = GoogleFont("Inter")

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

/**
 * Dual-font system:
 *  - Display / headers / badges / stats -> Monospace (retro personality)
 *  - Body / documentation prose         -> Inter (legible sans-serif)
 */
val BodyFontFamily = FontFamily(
    Font(googleFont = InterFont, fontProvider = googleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = InterFont, fontProvider = googleFontProvider, weight = FontWeight.Medium),
    Font(googleFont = InterFont, fontProvider = googleFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = InterFont, fontProvider = googleFontProvider, weight = FontWeight.Bold)
)

enum class TitleFontPreset(val displayName: String, val fontFamily: FontFamily) {
    ARCADE_3D("Arcade Bungee", BungeeFontFamily),
    PIXEL_8BIT("Pixel 8-Bit", PressStartFontFamily),
    CYBER_PUNK("Cyber Petch", ChakraPetchFontFamily),
    RETRO_DISPLAY("Retro Display", RighteousFontFamily),
    SCI_FI_ORBIT("Sci-Fi Orbit", OrbitronFontFamily),
    CLASSIC_MONO("Classic Mono", FontFamily.Monospace)
}

// Dual-font Material typography: sans-serif for reading, monospace for identity
val Typography =
  Typography(
    bodyLarge =
      TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 23.sp,
        letterSpacing = 0.2.sp,
      ),
    bodyMedium =
      TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp,
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

