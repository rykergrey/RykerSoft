package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

import androidx.compose.material.icons.filled.Settings

import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.example.R

@Composable
fun RykerSoftTitleHeader(
    onOpenSettings: () -> Unit,
    currentPreset: TitleFontPreset = TitleFontPreset.ARCADE_3D,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Column: 3D Layered Title Logo
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.testTag("app_title_header")
        ) {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                // 3D Layered Logo Box (Custom 90s sci-fi Terminator/Star Trek style logotype)
                Box(
                    modifier = Modifier.wrapContentSize()
                ) {
                    // 1. Black Shadow Offset Layer (3D depth effect)
                    Image(
                        painter = painterResource(id = R.drawable.rykersoft_logo),
                        contentDescription = null,
                        modifier = Modifier
                            .height(26.dp)
                            .offset(x = 2.dp, y = 2.dp),
                        colorFilter = ColorFilter.tint(NeoBlack)
                    )

                    // 2. Secondary Magenta Glow Shadow Offset Layer
                    Image(
                        painter = painterResource(id = R.drawable.rykersoft_logo),
                        contentDescription = null,
                        modifier = Modifier
                            .height(26.dp)
                            .offset(x = 1.dp, y = 1.dp),
                        colorFilter = ColorFilter.tint(NeoMagenta)
                    )

                    // 3. Crisp White Foreground Custom Logo
                    Image(
                        painter = painterResource(id = R.drawable.rykersoft_logo),
                        contentDescription = "RykerSoft Logo",
                        modifier = Modifier.height(26.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Sub-tagline chip badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = NeoMagenta,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = "APPLICATION MANAGER",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = NeoSubtext,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }

        // Store Settings Icon Button
        NeoButton(
            onClick = onOpenSettings,
            style = NeoButtonStyle.NEUTRAL_WHITE,
            shadowOffset = 2.dp,
            contentPadding = PaddingValues(6.dp),
            modifier = Modifier.testTag("settings_trigger_button")
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Store Settings",
                tint = NeoText,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun FontPickerModalDialog(
    currentPreset: TitleFontPreset,
    onDismiss: () -> Unit,
    onSelectPreset: (TitleFontPreset) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
        ) {
            // Shadow Layer
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 5.dp, y = 5.dp)
                    .background(NeoBlack)
            )

            // Content Window
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.5.dp, NeoBorder)
                    .background(NeoSurface)
                    .padding(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = NeoMagenta,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "TITLE TYPOGRAPHY STYLES",
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            color = NeoText
                        )
                    }

                    NeoButton(
                        onClick = onDismiss,
                        style = NeoButtonStyle.PRIMARY_MAGENTA,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("✕", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Select your favorite display font style for the RykerSoft app title:",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = NeoSubtext
                )

                Spacer(modifier = Modifier.height(14.dp))

                // List of Font Presets
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.heightIn(max = 380.dp)
                ) {
                    items(TitleFontPreset.entries) { preset ->
                        val isSelected = preset == currentPreset

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.5.dp,
                                    color = if (isSelected) NeoMagenta else NeoBorder
                                )
                                .background(if (isSelected) NeoMutedBg else NeoBg)
                                .clickable { onSelectPreset(preset) }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = preset.displayName.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isSelected) NeoMagenta else NeoSubtext
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    val previewFontSize = when (preset) {
                                        TitleFontPreset.PIXEL_8BIT -> 14.sp
                                        TitleFontPreset.SCI_FI_ORBIT -> 18.sp
                                        else -> 20.sp
                                    }

                                    // Render title preview in this specific font!
                                    Box {
                                        Text(
                                            text = "RYKERSOFT",
                                            style = TextStyle(
                                                fontFamily = preset.fontFamily,
                                                fontWeight = FontWeight.Black,
                                                fontSize = previewFontSize,
                                                color = NeoBlack
                                            ),
                                            modifier = Modifier.offset(x = 1.5.dp, y = 1.5.dp)
                                        )
                                        Text(
                                            text = "RYKERSOFT",
                                            style = TextStyle(
                                                fontFamily = preset.fontFamily,
                                                fontWeight = FontWeight.Black,
                                                fontSize = previewFontSize,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }

                                if (isSelected) {
                                    StickerBadge(
                                        text = "ACTIVE",
                                        bgColor = NeoGreen,
                                        textColor = Color.White,
                                        shadowOffset = 1.dp
                                    )
                                } else {
                                    NeoButton(
                                        onClick = { onSelectPreset(preset) },
                                        style = NeoButtonStyle.NEUTRAL_WHITE,
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("USE", fontSize = 9.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
