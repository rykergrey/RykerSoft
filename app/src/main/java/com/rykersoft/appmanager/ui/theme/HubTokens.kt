package com.rykersoft.appmanager.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rykersoft.appmanager.R

/**
 * The hub's world, "Hard-Edge Neon": tinted blue-black graph paper, 2 dp borders, hard offset
 * shadows as the one loud device, and three typefaces bundled with the app.
 *
 * Every colour, text style, shape and measure a screen uses is named here. Screens never hold a
 * raw value. DESIGN.md at the repo root says what each role means.
 */
object Hub {

    /** Colours. Each accent has one meaning. */
    object C {
        val bg = Color(0xFF0F1015)
        val grid = Color(0xFF15161D)
        val surface = Color(0xFF191A23)
        val raised = Color(0xFF222431)
        val border = Color(0xFF3C3F52)
        val borderSoft = Color(0xFF2A2C3A)
        val text = Color(0xFFF2F3F7)
        val dim = Color(0xFFA9ABBB)
        val faint = Color(0xFF7C7F93)
        val shadow = Color(0xFF000000)
        val scrim = Color(0xCC000000)

        /** The primary action and the active path: Update, the selected filter. */
        val action = Color(0xFFFFD600)
        val actionInk = Color(0xFF14130A)
        /** Identity only: the logo offset and the mark before a section name. */
        val brand = Color(0xFFFF2A85)
        /** Installed, up to date, free, Pro switched on. */
        val ok = Color(0xFF00E676)
        /** Links and focus. */
        val link = Color(0xFF00E5FF)
        /** The Games kind. */
        val game = Color(0xFFB08CFF)
        /** A build that lags behind the current version, or an install waiting on you. */
        val warn = Color(0xFFFFAA33)
        /** Destructive actions and errors. */
        val danger = Color(0xFFFF3366)
    }

    /** Type. Mono for data, sans for content, the display face for names. Nothing under 12 sp. */
    object T {
        @OptIn(ExperimentalTextApi::class)
        private fun plex(weight: Int) = Font(R.font.ibm_plex_sans, FontWeight(weight), variationSettings = FontVariation.Settings(FontVariation.weight(weight)))

        @OptIn(ExperimentalTextApi::class)
        private fun mono(weight: Int) = Font(R.font.jetbrains_mono, FontWeight(weight), variationSettings = FontVariation.Settings(FontVariation.weight(weight)))

        val Sans = FontFamily(plex(400), plex(500), plex(600))
        val Mono = FontFamily(mono(500), mono(600), mono(700))
        val Display = FontFamily(
            Font(R.font.chakra_petch_semibold, FontWeight.SemiBold),
            Font(R.font.chakra_petch_bold, FontWeight.Bold)
        )

        /** An app's name at the top of its page. */
        val display = TextStyle(fontFamily = Display, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 28.sp)
        /** An app's name in a row, a screen title. */
        val title = TextStyle(fontFamily = Display, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 21.sp, letterSpacing = 0.1.sp)
        val body = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp)
        val bodyStrong = body.copy(fontWeight = FontWeight.SemiBold)
        val bodySm = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp)
        /** Versions, dates, counts, package names. */
        val data = TextStyle(fontFamily = Mono, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp)
        /** Button text. Written in capitals by the kit. */
        val button = TextStyle(fontFamily = Mono, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 14.sp, letterSpacing = 0.5.sp)
        /** Section names and tags. Written in capitals by the kit. */
        val label = TextStyle(fontFamily = Mono, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 14.sp, letterSpacing = 0.9.sp)
        /** Markdown headings inside release notes and specs. */
        val heading = TextStyle(fontFamily = Mono, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 18.sp)
    }

    /** Shapes: three radii for the whole app. */
    object S {
        val square = RoundedCornerShape(0.dp)
        val control = RoundedCornerShape(2.dp)
        val tile = RoundedCornerShape(4.dp)
    }

    /** Measures. */
    object D {
        val gutter = 14.dp
        val gap = 8.dp
        val gapSm = 4.dp
        val gapLg = 16.dp
        val border = 2.dp
        val hair = 1.dp
        val shadow = 3.dp
        val shadowLg = 5.dp
        val touch = 48.dp
        val control = 40.dp
        val iconTile = 44.dp
        val iconTileLg = 64.dp
        val icon = 18.dp
        val iconSm = 14.dp
        val gridStep = 32.dp
        val rail = 3.dp
        val logoHeight = 20.dp
        val shotHeight = 300.dp
        val viewerHeight = 560.dp
        val progress = 4.dp
        val menuWidth = 232.dp
        val listMax = 280.dp
    }
}
