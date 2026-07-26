package com.example.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Neo-Brutalist Card Container with thick black border and stark offset shadow.
 * Pass shadowOffset = 0.dp for a flat nested panel (reduces border/shadow fatigue
 * on inner containers while keeping the tactile look on top-level cards).
 */
@Composable
fun NeoCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = NeoSurface,
    borderColor: Color = NeoBorder,
    borderWidth: Dp = 2.dp,
    shadowOffset: Dp = 4.dp,
    shadowColor: Color = NeoBlack,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
    ) {
        // Shadow Box Layer (skipped entirely for flat nested panels)
        if (shadowOffset > 0.dp) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = shadowOffset, y = shadowOffset)
                    .background(shadowColor)
            )
        }

        // Top Surface Layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(borderWidth, borderColor)
                .background(backgroundColor)
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

enum class NeoButtonStyle {
    PRIMARY_MAGENTA,   // Brand magenta (identity moments, not routine actions)
    SECONDARY_YELLOW,  // PRIMARY CTA yellow (install / update / main actions)
    ACCENT_CYAN,       // Interactive secondary (links, toggles, alt actions)
    ACTION_GREEN,      // Success / launch / installed
    DANGER_RED,        // Destructive / error actions (remove, delete)
    NEUTRAL_WHITE,     // Quiet neutral surface control
    DARK_BLACK         // Carbon Black
}

/**
 * Tactile Neo-Brutalist Button with heavy outline and responsive press shift.
 *
 * Shadow is sized to the face (not the outer constraint box). When the caller
 * passes fillMaxWidth, both face and shadow expand together; wrap-content
 * buttons keep a matching shadow instead of a full-width black bar.
 */
@Composable
fun NeoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: NeoButtonStyle = NeoButtonStyle.PRIMARY_MAGENTA,
    enabled: Boolean = true,
    shadowOffset: Dp = 3.5.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val currentOffset by animateDpAsState(
        targetValue = if (isPressed) shadowOffset else 0.dp,
        animationSpec = tween(durationMillis = 40),
        label = "neoButtonOffset"
    )

    val (bgColor, textColor) = when (style) {
        NeoButtonStyle.PRIMARY_MAGENTA -> NeoMagenta to Color.White
        NeoButtonStyle.SECONDARY_YELLOW -> NeoYellow to Color.Black
        NeoButtonStyle.ACCENT_CYAN -> NeoCyan to Color.Black
        NeoButtonStyle.ACTION_GREEN -> NeoGreen to Color.Black
        NeoButtonStyle.DANGER_RED -> NeoRed to Color.White
        NeoButtonStyle.NEUTRAL_WHITE -> NeoMutedBg to NeoText
        NeoButtonStyle.DARK_BLACK -> NeoBlack to Color.White
    }

    // Crisp black edge on bright fills; structural gray edge on dark neutrals.
    val edgeColor = when (style) {
        NeoButtonStyle.NEUTRAL_WHITE, NeoButtonStyle.DARK_BLACK -> NeoBorder
        else -> NeoBlack
    }

    // fillMaxWidth() gives tight width constraints (min == max). Detect that so the
    // face expands with the shadow instead of leaving a full-width black bar behind
    // a wrap-content label (e.g. CANCEL INSTALL on the install waiting banner).
    BoxWithConstraints(modifier = modifier) {
        val expandWidth = constraints.hasFixedWidth
        val widthModifier = if (expandWidth) Modifier.fillMaxWidth() else Modifier

        Box(modifier = widthModifier) {
            // Shadow Layer (sized to the face; shrinks into place when pressed)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = shadowOffset, y = shadowOffset)
                    .background(if (enabled) NeoBlack else NeoBlack.copy(alpha = 0.3f))
            )

            // Interactive Button Layer
            Box(
                modifier = Modifier
                    .then(widthModifier)
                    .offset(x = currentOffset, y = currentOffset)
                    .border(2.dp, edgeColor)
                    .background(if (enabled) bgColor else bgColor.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = enabled,
                        onClick = onClick
                    )
                    .padding(contentPadding),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides if (enabled) textColor else textColor.copy(alpha = 0.6f)
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

/**
 * Tilted Sticker Badge Tag with bold outline and vibrant background
 */
@Composable
fun StickerBadge(
    text: String,
    modifier: Modifier = Modifier,
    bgColor: Color = NeoMagenta,
    textColor: Color = Color.White,
    rotation: Float = 0f,
    shadowOffset: Dp = 2.dp
) {
    Box(
        modifier = modifier
            .rotate(rotation)
    ) {
        // Shadow layer
        if (shadowOffset > 0.dp) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = shadowOffset, y = shadowOffset)
                    .background(NeoBlack)
            )
        }

        Box(
            modifier = Modifier
                .border(1.5.dp, NeoBlack)
                .background(bgColor)
                .padding(horizontal = 7.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.uppercase(),
                color = textColor,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Platform/Category Tag Chip in box with border
 */
@Composable
fun TagChip(
    text: String,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    bgColor: Color = NeoMutedBg,
    textColor: Color = NeoText
) {
    Box(
        modifier = modifier
            .border(1.dp, NeoBorderSoft)
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            icon?.invoke()
            Text(
                text = text.uppercase(),
                color = textColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

/**
 * Memphis Graph Paper Background Grid Canvas Component
 */
@Composable
fun MemphisBackgroundGrid(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val step = 20.dp.toPx()
        val gridColor = NeoGridLines

        var x = 0f
        while (x < size.width) {
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
            x += step
        }

        var y = 0f
        while (y < size.height) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += step
        }
    }
}

/**
 * Memphis Decorative Zig-Zag Banner Strip
 */
@Composable
fun MemphisZigZagBanner(
    modifier: Modifier = Modifier,
    height: Dp = 8.dp
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val w = size.width
        val h = size.height
        val teeth = (w / 14f).toInt().coerceAtLeast(1)
        val toothWidth = w / teeth

        val path = Path().apply {
            moveTo(0f, h)
            for (i in 0 until teeth) {
                val x1 = i * toothWidth + (toothWidth / 2f)
                val x2 = (i + 1) * toothWidth
                lineTo(x1, 0f)
                lineTo(x2, h)
            }
            close()
        }

        drawPath(path, color = NeoMagenta)
    }
}

/**
 * Memphis Explosive Starburst Badge (Image 2 style)
 */
@Composable
fun MemphisStarburst(
    modifier: Modifier = Modifier,
    points: Int = 10,
    color: Color = NeoYellow,
    borderColor: Color = NeoBlack,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val outerRadius = size.width / 2f
            val innerRadius = outerRadius * 0.72f

            val path = Path()
            val totalPoints = points * 2
            val angleStep = (2 * PI / totalPoints).toFloat()

            for (i in 0 until totalPoints) {
                val r = if (i % 2 == 0) outerRadius else innerRadius
                val angle = i * angleStep - (PI / 2).toFloat()
                val x = cx + r * cos(angle)
                val y = cy + r * sin(angle)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()

            // Draw solid drop shadow offset
            drawPath(
                path = path,
                color = borderColor,
                style = androidx.compose.ui.graphics.drawscope.Fill
            )

            drawPath(
                path = path,
                color = color
            )

            drawPath(
                path = path,
                color = borderColor,
                style = Stroke(width = 2.5.dp.toPx())
            )
        }

        content()
    }
}

/**
 * Memphis Wave / Squiggle Drawing
 */
@Composable
fun MemphisSquiggle(
    modifier: Modifier = Modifier,
    color: Color = NeoCyan,
    strokeWidth: Dp = 3.dp
) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(0f, size.height / 2f)
            val step = size.width / 4f
            val h = size.height / 2f
            cubicTo(step * 0.5f, 0f, step * 0.5f, size.height, step, h)
            cubicTo(step * 1.5f, 0f, step * 1.5f, size.height, step * 2f, h)
            cubicTo(step * 2.5f, 0f, step * 2.5f, size.height, step * 3f, h)
            cubicTo(step * 3.5f, 0f, step * 3.5f, size.height, size.width, h)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth.toPx())
        )
    }
}

