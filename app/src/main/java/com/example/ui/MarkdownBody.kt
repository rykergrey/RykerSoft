package com.example.ui

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeoBlack
import com.example.ui.theme.NeoCyan
import com.example.ui.theme.NeoMagenta
import com.example.ui.theme.NeoSubtext
import com.example.ui.theme.NeoText
import com.example.ui.theme.NeoYellow

private val BOLD_REGEX = Regex("""\*\*(.+?)\*\*""")
private val CODE_REGEX = Regex("""`(.+?)`""")
private val MARKDOWN_SYMBOLS_REGEX = Regex("""[#*_`>]""")
private val MULTI_SPACE_REGEX = Regex("""\s+""")
private val INLINE_MARKDOWN_REGEX = Regex("""(\*\*(.+?)\*\*|`(.+?)`|\[(.+?)\]\((.+?)\))""")
private val HEADING_REGEX = Regex("""^(#{1,3})\s+(.*)$""")
private val BULLET_REGEX = Regex("""^[-*]\s+""")
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

private fun inlineMarkdown(text: String): AnnotatedString = buildAnnotatedString {
    var last = 0
    for (match in INLINE_MARKDOWN_REGEX.findAll(text)) {
        append(text.substring(last, match.range.first))
        val bold = match.groups[2]?.value
        val code = match.groups[3]?.value
        val linkText = match.groups[4]?.value
        val linkTarget = match.groups[5]?.value

        if (bold != null) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = NeoYellow)) {
                append(bold)
            }
        } else if (code != null) {
            withStyle(SpanStyle(fontFamily = FontFamily.Monospace, color = NeoCyan)) {
                append(code)
            }
        } else if (linkText != null && linkTarget != null) {
            pushStringAnnotation(tag = "URL", annotation = linkTarget)
            withStyle(
                SpanStyle(
                    color = NeoCyan,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    fontFamily = FontFamily.Monospace
                )
            ) {
                append(linkText)
            }
            pop()
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
    fontSize: TextUnit = 11.5.sp,
    fontFamily: FontFamily = FontFamily.Monospace,
    lineHeight: TextUnit = 16.sp,
    onUrlClick: ((String) -> Unit)? = null
) {
    val annotatedString = remember(markdownText) { inlineMarkdown(markdownText) }
    val hasLinks = remember(annotatedString) {
        annotatedString.getStringAnnotations(tag = "URL", start = 0, end = annotatedString.length).isNotEmpty()
    }

    if (hasLinks && onUrlClick != null) {
        ClickableText(
            text = annotatedString,
            modifier = modifier,
            style = androidx.compose.ui.text.TextStyle(
                color = color,
                fontSize = fontSize,
                fontFamily = fontFamily,
                lineHeight = lineHeight
            ),
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                    .firstOrNull()?.let { annotation ->
                        onUrlClick(annotation.item)
                    }
            }
        )
    } else {
        Text(
            text = annotatedString,
            modifier = modifier,
            color = color,
            fontSize = fontSize,
            fontFamily = fontFamily,
            lineHeight = lineHeight
        )
    }
}

@Composable
private fun HeadingText(
    text: String,
    level: Int,
    headingColor: Color,
    accentColor: Color,
    highlightAnchor: String?,
    onHeaderPositioned: ((title: String, anchor: String, yPx: Float) -> Unit)?
) {
    val size = when (level) {
        1 -> 16.sp
        2 -> 13.sp
        else -> 12.sp
    }
    val defaultColor = when (level) {
        1 -> headingColor
        2 -> accentColor
        else -> NeoYellow
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
                normText.contains(normHighlight) ||
                normHighlight.contains(normText)
            )
        }
    }

    val animColor = remember { Animatable(Color.Transparent) }

    LaunchedEffect(isHighlighted, highlightAnchor) {
        if (isHighlighted) {
            animColor.snapTo(NeoYellow)
            animColor.animateTo(
                targetValue = Color.Transparent,
                animationSpec = tween(
                    durationMillis = 2500,
                    delayMillis = 800
                )
            )
        }
    }

    val currentBg = animColor.value
    val isYellowBg = currentBg != Color.Transparent && currentBg.alpha > 0.2f
    val textColor = if (isYellowBg) NeoBlack else defaultColor

    Box(
        modifier = Modifier
            .onGloballyPositioned { coordinates ->
                val y = coordinates.positionInParent().y
                onHeaderPositioned?.invoke(text, anchor, y)
            }
            .background(currentBg, shape = RoundedCornerShape(3.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = inlineMarkdown(text),
            color = textColor,
            fontSize = size,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            lineHeight = (size.value + 4).sp
        )
    }
}

@Composable
fun MarkdownBody(
    markdown: String,
    modifier: Modifier = Modifier,
    textColor: Color = NeoText,
    mutedColor: Color = NeoSubtext,
    headingColor: Color = NeoMagenta,
    accentColor: Color = NeoCyan,
    bodySize: TextUnit = 11.5.sp,
    lineHeight: TextUnit = 16.sp,
    highlightAnchor: String? = null,
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
                                    text = "•",
                                    color = accentColor,
                                    fontSize = bodySize,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .width(14.dp)
                                        .padding(top = 0.dp)
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
    fontSize: TextUnit = 11.sp,
    lineHeight: TextUnit = 15.sp
) {
    val summaryText = remember(markdown) { markdownSummary(markdown) }
    Text(
        text = summaryText,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = FontFamily.Monospace,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        lineHeight = lineHeight
    )
}

private sealed class MdBlock {
    data class Heading(val level: Int, val text: String) : MdBlock()
    data class Paragraph(val text: String) : MdBlock()
    data class BulletList(val items: List<String>) : MdBlock()
    data class NumberedList(val items: List<String>) : MdBlock()
    data class Quote(val text: String) : MdBlock()
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
            val items = mutableListOf<String>()
            while (i < lines.size && BULLET_REGEX.containsMatchIn(lines[i].trim())) {
                items.add(lines[i].trim().replace(BULLET_REGEX, ""))
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
