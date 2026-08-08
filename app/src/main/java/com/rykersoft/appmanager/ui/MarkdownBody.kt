package com.rykersoft.appmanager.ui

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rykersoft.appmanager.ui.theme.BodyFontFamily
import com.rykersoft.appmanager.ui.theme.NeoBlack
import com.rykersoft.appmanager.ui.theme.NeoCyan
import com.rykersoft.appmanager.ui.theme.NeoMagenta
import com.rykersoft.appmanager.ui.theme.NeoMutedBg
import com.rykersoft.appmanager.ui.theme.NeoSubtext
import com.rykersoft.appmanager.ui.theme.NeoText
import com.rykersoft.appmanager.ui.theme.NeoYellow

private val BOLD_REGEX = Regex("""\*\*(.+?)\*\*""")
private val CODE_REGEX = Regex("""`(.+?)`""")
private val MARKDOWN_SYMBOLS_REGEX = Regex("""[#*_`>]""")
private val MULTI_SPACE_REGEX = Regex("""\s+""")
private val INLINE_MARKDOWN_REGEX = Regex("""(\*\*(.+?)\*\*|`(.+?)`|\[(.+?)\]\((.+?)\))""")
private val HEADING_REGEX = Regex("""^(#{1,3})\s+(.*)$""")
private val BULLET_REGEX = Regex("""^([-*\u2022])\s+""")
/** Magenta asterisk marker for PRO / unlock-gated features in hub docs. */
private val NeoProAsterisk = NeoMagenta
private val NUMBER_REGEX = Regex("""^\d+\.\s+""")

data class TocEntry(
    val title: String,
    val targetAnchor: String
)

fun normalizeAnchor(raw: String): String {
    return raw.lowercase()
        .removePrefix("#")
        .replace(Regex("[^a-z0-9]"), "")
}

fun extractTocEntries(markdown: String): List<TocEntry> {
    val entries = mutableListOf<TocEntry>()
    val lines = markdown.replace("\r\n", "\n").lines()
    for (line in lines) {
        val trimmed = line.trim()
        val headingMatch = HEADING_REGEX.find(trimmed)
        if (headingMatch != null) {
            val level = headingMatch.groupValues[1].length
            val text = headingMatch.groupValues[2].trim()
            if (level >= 1 && text.lowercase() != "table of contents") {
                val anchor = text.lowercase().replace(Regex("[^a-z0-9\\s-]"), "").replace(Regex("\\s+"), "-")
                entries.add(TocEntry(title = text, targetAnchor = anchor))
            }
        }
    }
    return entries
}

/**
 * First plain-text paragraph for list-card previews (strips simple markdown markers).
 */
fun markdownSummary(markdown: String): String {
    val lines = markdown.replace("\r\n", "\n").lines()
    val summary = StringBuilder()
    for (raw in lines) {
        val line = raw.trim()
        if (line.isEmpty()) {
            if (summary.isNotEmpty()) break
            continue
        }
        if (line.startsWith("#")) break
        val cleaned = line
            .removePrefix("- ")
            .removePrefix("* ")
            .replace(BOLD_REGEX, "$1")
            .replace(CODE_REGEX, "$1")
            .replace(Regex("""\[(.+?)\]\((.+?)\)"""), "$1")
            .trim()
        if (cleaned.isEmpty()) continue
        if (summary.isNotEmpty()) summary.append(' ')
        summary.append(cleaned)
    }
    return summary.toString().ifBlank {
        markdown.replace(MARKDOWN_SYMBOLS_REGEX, " ").replace(MULTI_SPACE_REGEX, " ").trim()
    }
}

// Inline styling rules (strict color roles):
//  - **bold**  -> weight only, inherits body color (yellow stays reserved for CTAs)
//  - `code`    -> monospace on a subtle inset background chip
//  - [link](x) -> electric cyan + underline (cyan = interactive)
private fun inlineMarkdown(
    text: String,
    onUrlClick: ((String) -> Unit)? = null
): AnnotatedString = buildAnnotatedString {
    var last = 0
    val linkStyle = SpanStyle(
        color = NeoCyan,
        fontWeight = FontWeight.SemiBold,
        textDecoration = TextDecoration.Underline
    )
    for (match in INLINE_MARKDOWN_REGEX.findAll(text)) {
        append(text.substring(last, match.range.first))
        val bold = match.groups[2]?.value
        val code = match.groups[3]?.value
        val linkText = match.groups[4]?.value
        val linkTarget = match.groups[5]?.value

        if (bold != null) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(bold)
            }
        } else if (code != null) {
            withStyle(
                SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    color = NeoText,
                    background = NeoMutedBg
                )
            ) {
                append(code)
            }
        } else if (linkText != null && linkTarget != null) {
            if (onUrlClick != null) {
                pushLink(
                    LinkAnnotation.Clickable(
                        tag = linkTarget,
                        styles = TextLinkStyles(style = linkStyle),
                        linkInteractionListener = { onUrlClick(linkTarget) }
                    )
                )
                append(linkText)
                pop()
            } else {
                withStyle(linkStyle) {
                    append(linkText)
                }
            }
        }
        last = match.range.last + 1
    }
    if (last < text.length) append(text.substring(last))
}

@Composable
fun ClickableMarkdownText(
    markdownText: String,
    modifier: Modifier = Modifier,
    color: Color = NeoText,
    fontSize: TextUnit = 12.5.sp,
    fontFamily: FontFamily = BodyFontFamily,
    lineHeight: TextUnit = 18.sp,
    onUrlClick: ((String) -> Unit)? = null
) {
    val latestOnUrlClick = rememberUpdatedState(onUrlClick)
    val linksEnabled = onUrlClick != null
    val annotatedString = remember(markdownText, linksEnabled) {
        if (linksEnabled) {
            inlineMarkdown(markdownText) { url -> latestOnUrlClick.value?.invoke(url) }
        } else {
            inlineMarkdown(markdownText)
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = fontFamily,
        lineHeight = lineHeight
    )
}

@Composable
private fun HeadingText(
    text: String,
    level: Int,
    headingColor: Color,
    accentColor: Color,
    highlightAnchor: String?,
    highlightNonce: Int,
    onHeaderPositioned: ((title: String, anchor: String, yPx: Float) -> Unit)?
) {
    // Restrained hierarchy: headings stay high-contrast text; the neon accent is
    // confined to a short underline bar so section colors never fight the CTAs.
    val size = when (level) {
        1 -> 16.sp
        2 -> 13.5.sp
        else -> 12.sp
    }
    val defaultColor = when (level) {
        1, 2 -> headingColor
        else -> NeoSubtext
    }
    val anchor = remember(text) {
        text.lowercase().replace(Regex("[^a-z0-9\\s-]"), "").replace(Regex("\\s+"), "-")
    }

    val isHighlighted = remember(highlightAnchor, text, anchor) {
        if (highlightAnchor.isNullOrBlank()) false
        else {
            val normHighlight = normalizeAnchor(highlightAnchor)
            val normAnchor = normalizeAnchor(anchor)
            val normText = normalizeAnchor(text)
            normHighlight.isNotEmpty() && (
                normHighlight == normAnchor ||
                    normHighlight == normText ||
                    // Prefer exact/near-exact; avoid loose contains on very short anchors
                    (normHighlight.length >= 4 && normText.contains(normHighlight)) ||
                    (normText.length >= 4 && normHighlight.contains(normText))
                )
        }
    }

    val animColor = remember { Animatable(Color.Transparent) }

    // highlightNonce re-triggers the flash when the same TOC link is tapped again
    LaunchedEffect(highlightAnchor, highlightNonce, isHighlighted) {
        if (isHighlighted) {
            animColor.snapTo(NeoYellow)
            animColor.animateTo(
                targetValue = Color.Transparent,
                animationSpec = tween(
                    durationMillis = 2500,
                    delayMillis = 800
                )
            )
        } else {
            animColor.snapTo(Color.Transparent)
        }
    }

    val currentBg = animColor.value
    val isYellowBg = currentBg.alpha > 0.2f
    val textColor = if (isYellowBg) NeoBlack else defaultColor

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                // Window Y so the detail dialog can scroll relative to the outer list,
                // not the inner markdown column (positionInParent was too small).
                val y = coordinates.positionInWindow().y
                onHeaderPositioned?.invoke(text, anchor, y)
            }
            .background(currentBg, shape = RoundedCornerShape(3.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(
            text = inlineMarkdown(text),
            color = textColor,
            fontSize = size,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            lineHeight = (size.value + 4).sp
        )
        // Short neon accent bar under major headings (color-restrained flair)
        if (level <= 2) {
            Box(
                modifier = Modifier
                    .padding(top = 3.dp)
                    .width(if (level == 1) 34.dp else 22.dp)
                    .height(3.dp)
                    .background(if (isYellowBg) NeoBlack else accentColor)
            )
        }
    }
}

@Composable
fun MarkdownBody(
    markdown: String,
    modifier: Modifier = Modifier,
    textColor: Color = NeoText,
    mutedColor: Color = NeoSubtext,
    headingColor: Color = NeoText,
    accentColor: Color = NeoMagenta,
    bodySize: TextUnit = 12.5.sp,
    lineHeight: TextUnit = 18.sp,
    highlightAnchor: String? = null,
    highlightNonce: Int = 0,
    onHeaderPositioned: ((title: String, anchor: String, yPx: Float) -> Unit)? = null,
    onUrlClick: ((String) -> Unit)? = null
) {
    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MdBlock.Heading -> {
                    HeadingText(
                        text = block.text,
                        level = block.level,
                        headingColor = headingColor,
                        accentColor = accentColor,
                        highlightAnchor = highlightAnchor,
                        highlightNonce = highlightNonce,
                        onHeaderPositioned = onHeaderPositioned
                    )
                }
                is MdBlock.Paragraph -> {
                    ClickableMarkdownText(
                        markdownText = block.text,
                        color = textColor,
                        fontSize = bodySize,
                        lineHeight = lineHeight,
                        onUrlClick = onUrlClick
                    )
                }
                is MdBlock.BulletList -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        block.items.forEach { item ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = if (item.isPro) "*" else "•",
                                    color = if (item.isPro) NeoProAsterisk else accentColor,
                                    fontSize = bodySize,
                                    fontWeight = if (item.isPro) FontWeight.Black else FontWeight.Normal,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .width(14.dp)
                                        .padding(top = 0.dp)
                                )
                                ClickableMarkdownText(
                                    markdownText = item.text,
                                    color = textColor,
                                    fontSize = bodySize,
                                    lineHeight = lineHeight,
                                    onUrlClick = onUrlClick,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                is MdBlock.NumberedList -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        block.items.forEachIndexed { index, item ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "${index + 1}.",
                                    color = accentColor,
                                    fontSize = bodySize,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.width(22.dp)
                                )
                                ClickableMarkdownText(
                                    markdownText = item,
                                    color = textColor,
                                    fontSize = bodySize,
                                    lineHeight = lineHeight,
                                    onUrlClick = onUrlClick,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                is MdBlock.Quote -> {
                    ClickableMarkdownText(
                        markdownText = block.text,
                        color = mutedColor,
                        fontSize = bodySize,
                        lineHeight = lineHeight,
                        onUrlClick = onUrlClick,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MarkdownSummaryText(
    markdown: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 2,
    color: Color = NeoText,
    fontSize: TextUnit = 12.sp,
    lineHeight: TextUnit = 17.sp
) {
    val summaryText = remember(markdown) { markdownSummary(markdown) }
    Text(
        text = summaryText,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = BodyFontFamily,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        lineHeight = lineHeight
    )
}

private data class BulletItem(val text: String, val isPro: Boolean = false)

private sealed class MdBlock {
    data class Heading(val level: Int, val text: String) : MdBlock()
    data class Paragraph(val text: String) : MdBlock()
    data class BulletList(val items: List<BulletItem>) : MdBlock()
    data class NumberedList(val items: List<String>) : MdBlock()
    data class Quote(val text: String) : MdBlock()
}

/**
 * PRO marker rules for hub docs:
 * - `* Feature` → magenta `*` bullet (pro / unlock-gated)
 * - `- * Feature` → same (dash list with a leading asterisk in the item text)
 * - `- Feature` → normal `•` bullet
 */
private fun parseBulletItem(rawLine: String): BulletItem {
    val trimmed = rawLine.trim()
    val marker = BULLET_REGEX.find(trimmed)?.groupValues?.getOrNull(1)
    var body = trimmed.replace(BULLET_REGEX, "")
    val starredMarker = marker == "*"
    val inlinePro = body.startsWith("* ")
    if (inlinePro) body = body.removePrefix("* ").trimStart()
    return BulletItem(text = body, isPro = starredMarker || inlinePro)
}

private fun parseMarkdownBlocks(markdown: String): List<MdBlock> {
    val lines = markdown.replace("\r\n", "\n").lines()
    val blocks = mutableListOf<MdBlock>()
    var i = 0
    while (i < lines.size) {
        val raw = lines[i]
        val line = raw.trimEnd()
        val trimmed = line.trim()
        if (trimmed.isEmpty()) {
            i++
            continue
        }

        val heading = HEADING_REGEX.find(trimmed)
        if (heading != null) {
            blocks.add(MdBlock.Heading(heading.groupValues[1].length, heading.groupValues[2].trim()))
            i++
            continue
        }

        if (trimmed.startsWith(">")) {
            val quoteLines = mutableListOf<String>()
            while (i < lines.size && lines[i].trim().startsWith(">")) {
                quoteLines.add(lines[i].trim().removePrefix(">").trim())
                i++
            }
            blocks.add(MdBlock.Quote(quoteLines.joinToString(" ")))
            continue
        }

        if (BULLET_REGEX.containsMatchIn(trimmed)) {
            val items = mutableListOf<BulletItem>()
            while (i < lines.size && BULLET_REGEX.containsMatchIn(lines[i].trim())) {
                items.add(parseBulletItem(lines[i]))
                i++
            }
            blocks.add(MdBlock.BulletList(items))
            continue
        }

        if (NUMBER_REGEX.containsMatchIn(trimmed)) {
            val items = mutableListOf<String>()
            while (i < lines.size && NUMBER_REGEX.containsMatchIn(lines[i].trim())) {
                items.add(lines[i].trim().replace(NUMBER_REGEX, ""))
                i++
            }
            blocks.add(MdBlock.NumberedList(items))
            continue
        }

        val para = mutableListOf<String>()
        while (i < lines.size) {
            val t = lines[i].trim()
            if (t.isEmpty()) break
            if (t.startsWith("#") || t.startsWith(">") ||
                BULLET_REGEX.containsMatchIn(t) ||
                NUMBER_REGEX.containsMatchIn(t)
            ) break
            para.add(t)
            i++
        }
        if (para.isNotEmpty()) {
            blocks.add(MdBlock.Paragraph(para.joinToString(" ")))
        }
    }
    return blocks
}
