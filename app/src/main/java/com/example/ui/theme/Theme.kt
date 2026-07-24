package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Enforce sharp 90-degree corners across Neo-Brutalist elements
val SharpShapes = Shapes(
  extraSmall = RoundedCornerShape(2.dp),
  small = RoundedCornerShape(4.dp),
  medium = RoundedCornerShape(6.dp),
  large = RoundedCornerShape(8.dp),
  extraLarge = RoundedCornerShape(12.dp)
)

private val DarkNeoColorScheme = darkColorScheme(
  primary = NeoMagenta,
  onPrimary = Color.White,
  primaryContainer = NeoYellow,
  onPrimaryContainer = Color.Black,
  secondary = NeoCyan,
  onSecondary = Color.Black,
  secondaryContainer = NeoMutedBg,
  tertiary = NeoPurple,
  onTertiary = Color.White,
  tertiaryContainer = NeoGreen,
  background = NeoBg,
  onBackground = NeoText,
  surface = NeoSurface,
  onSurface = NeoText,
  surfaceVariant = NeoMutedBg,
  onSurfaceVariant = NeoSubtext,
  outline = NeoBorder
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkNeoColorScheme,
    typography = Typography,
    shapes = SharpShapes,
    content = content
  )
}

