package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import com.example.ui.theme.NeoCyan
import com.example.ui.theme.NeoMagenta
import com.example.ui.theme.NeoSubtext
import com.example.ui.theme.NeoText
import com.example.ui.theme.NeoYellow

private val BOLD_REGEX = Regex("""\*\*(.+?)\*\*""")
private val CODE_REGEX = Regex("""`(.+?)`""")
private val MARKDOWN_SYMBOLS_REGEX = Regex("""[#*_`>]""")
private val MULTI_SPACE_REGEX = Regex("""\s+""")
private val INLINE_MARKDOWN_REGEX = Regex("""(\*\*(.+?)\*\*|`(.+?)`)""")
private val HEADING_REGEX = Regex("""^(#{1,3})\s+(.*)$""")
private val BULLET_REGEX = Regex("""^[-*]\s+""")
private val NUMBER_REGEX = Regex("""^\d+\.\s+""")

data class TocEntry(
    val title: String,
    val targetAnchor: String
)

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
            .trim()
        if (cleaned.isEmpty()) continue
        if (summary.isNotEmpty()) summary.append(' ')
        summary.append(cleaned)
    }
    return summary.toString().ifBlank {
        markdown.replace(MARKDOWN_SYMBOLS_REGEX, " ").replace(MULTI_SPACE_REGEX, " ").trim()
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
    onHeaderPositioned: ((title: String, anchor: String, yPx: Float) -> Unit)? = null
) {
    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MdBlock.Heading -> {
                    val size = when (block.level) {
                        1 -> 16.sp
                        2 -> 13.sp
                        else -> 12.sp
                    }
                    val color = when (block.level) {
                        1 -> headingColor
                        2 -> accentColor
                        else -> NeoYellow
                    }
                    val anchor = remember(block.text) {
                        block.text.lowercase().replace(Regex("[^a-z0-9\\s-]"), "").replace(Regex("\\s+"), "-")
                    }
                    Text(
                        text = inlineMarkdown(block.text),
                        color = color,
                        fontSize = size,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = (size.value + 4).sp,
                        modifier = Modifier.onGloballyPositioned { coordinates ->
                            val y = coordinates.positionInParent().y
                            onHeaderPositioned?.invoke(block.text, anchor, y)
                        }
                    )
                }
                is MdBlock.Paragraph -> {
                    Text(
                        text = inlineMarkdown(block.text),
                        color = textColor,
                        fontSize = bodySize,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = lineHeight
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
                                Text(
                                    text = inlineMarkdown(item),
                                    color = textColor,
                                    fontSize = bodySize,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = lineHeight,
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
                                Text(
                                    text = inlineMarkdown(item),
                                    color = textColor,
                                    fontSize = bodySize,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = lineHeight,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                is MdBlock.Quote -> {
                    Text(
                        text = inlineMarkdown(block.text),
                        color = mutedColor,
                        fontSize = bodySize,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = lineHeight,
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

private fun inlineMarkdown(text: String) = buildAnnotatedString {
    var last = 0
    for (match in INLINE_MARKDOWN_REGEX.findAll(text)) {
        append(text.substring(last, match.range.first))
        val bold = match.groups[2]?.value
        val code = match.groups[3]?.value
        if (bold != null) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = NeoYellow)) {
                append(bold)
            }
        } else if (code != null) {
            withStyle(SpanStyle(fontFamily = FontFamily.Monospace, color = NeoCyan)) {
                append(code)
            }
        }
        last = match.range.last + 1
    }
    if (last < text.length) append(text.substring(last))
}

