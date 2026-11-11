package com.rykersoft.appmanager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// Material 3 stays the interaction language (ripple, focus, text fields, menus, switches).
// The world comes from Hub: a static dark scheme, the bundled type and shallow shapes.
private val HubScheme = darkColorScheme(
    primary = Hub.C.action,
    onPrimary = Hub.C.actionInk,
    primaryContainer = Hub.C.raised,
    onPrimaryContainer = Hub.C.text,
    secondary = Hub.C.link,
    onSecondary = Hub.C.actionInk,
    secondaryContainer = Hub.C.raised,
    onSecondaryContainer = Hub.C.text,
    tertiary = Hub.C.brand,
    onTertiary = Hub.C.text,
    error = Hub.C.danger,
    onError = Hub.C.text,
    background = Hub.C.bg,
    onBackground = Hub.C.text,
    surface = Hub.C.surface,
    onSurface = Hub.C.text,
    surfaceVariant = Hub.C.raised,
    onSurfaceVariant = Hub.C.dim,
    surfaceContainer = Hub.C.surface,
    surfaceContainerHigh = Hub.C.raised,
    surfaceContainerHighest = Hub.C.raised,
    outline = Hub.C.border,
    outlineVariant = Hub.C.borderSoft,
    scrim = Hub.C.scrim
)

private val HubShapes = Shapes(
    extraSmall = Hub.S.control,
    small = Hub.S.control,
    medium = Hub.S.tile,
    large = Hub.S.tile,
    extraLarge = Hub.S.tile
)

private val HubTypography = Typography().run {
    copy(
        headlineSmall = Hub.T.display,
        titleLarge = Hub.T.display,
        titleMedium = Hub.T.title,
        titleSmall = Hub.T.title,
        bodyLarge = Hub.T.body,
        bodyMedium = Hub.T.body,
        bodySmall = Hub.T.bodySm,
        labelLarge = Hub.T.button,
        labelMedium = Hub.T.label,
        labelSmall = Hub.T.data
    )
}

@Composable
fun MyApplicationTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = HubScheme, typography = HubTypography, shapes = HubShapes, content = content)
}
