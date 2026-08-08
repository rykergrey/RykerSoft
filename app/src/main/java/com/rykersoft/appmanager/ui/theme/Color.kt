package com.rykersoft.appmanager.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// RykerSoft Design Tokens — Dark Cyber Neo-Brutalist Palette
//
// Strict color-role system: every neon accent has exactly one semantic job so
// users can navigate by color instinctively.
//
//   YELLOW  -> Primary CTA & focus (install/update buttons, active tabs)
//   GREEN   -> Success & installed state (badges, launch, success toasts)
//   RED     -> Error, alerts & destructive actions (remove, error toasts)
//   CYAN    -> Interactive links & secondary interactive accents
//   MAGENTA -> Brand identity only (logo, zig-zag strip, "new" stickers)
//   PURPLE  -> Games category accent
// ============================================================================

// --- Neutral canvas & surfaces -----------------------------------------------
val NeoBg = Color(0xFF121318)             // Dark charcoal app canvas
val NeoSurface = Color(0xFF1C1D26)        // Card / dialog surface
val NeoMutedBg = Color(0xFF242633)        // Inset panel surface (one step up)
val NeoGridLines = Color(0xFF1A1B22)      // Whisper-subtle graph-paper grid
val NeoBlack = Color(0xFF000000)          // Hard 2D offset shadows
val NeoBorder = Color(0xFF3C3F52)         // Structural border stroke
val NeoBorderSoft = Color(0xFF2C2E3D)     // Softer border for nested elements

// --- Text ---------------------------------------------------------------------
val NeoText = Color(0xFFF2F3F7)           // High-contrast primary text
val NeoSubtext = Color(0xFFA6A8B8)        // Muted secondary / body-muted text

// --- Semantic accents ----------------------------------------------------------
val NeoYellow = Color(0xFFFFD600)         // PRIMARY CTA / focus / active tab
val NeoGreen = Color(0xFF00E676)          // SUCCESS / installed / launch
val NeoRed = Color(0xFFFF3366)            // ERROR / alert / destructive
val NeoCyan = Color(0xFF00E5FF)           // LINKS / interactive secondary
val NeoMagenta = Color(0xFFFF2A85)        // BRAND accent (identity, not UI state)
val NeoPurple = Color(0xFF9D67FF)         // Games category accent
val NeoOrange = Color(0xFFFFAA33)         // Warning / in-progress accent

// --- Semantic aliases (prefer these in new code) -------------------------------
val NeoPrimary = NeoYellow
val NeoSuccess = NeoGreen
val NeoDanger = NeoRed
val NeoLink = NeoCyan
val NeoBrand = NeoMagenta

// --- Dim companion tints for chips/containers (accent at low luminance) --------
val NeoGreenDim = Color(0xFF0E3B26)       // Success container background
val NeoRedDim = Color(0xFF3D1220)         // Error container background
val NeoCyanDim = Color(0xFF0B3540)        // Link/interactive container background
val NeoPurpleDim = Color(0xFF2B1B47)      // Games / hero container background
val NeoYellowDim = Color(0xFF3B330A)      // Primary-CTA container background
