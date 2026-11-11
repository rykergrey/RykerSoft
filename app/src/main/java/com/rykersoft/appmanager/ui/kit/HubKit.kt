package com.rykersoft.appmanager.ui.kit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import coil.compose.SubcomposeAsyncImage
import com.rykersoft.appmanager.R
import com.rykersoft.appmanager.ui.PlatformFilter
import com.rykersoft.appmanager.ui.theme.Hub

// The hub's kit: the small vocabulary every screen is built from.

/** The world's one loud device: a hard, unblurred shadow down and to the right. */
fun Modifier.hardShadow(offset: Dp = Hub.D.shadow, color: Color = Hub.C.shadow): Modifier = drawBehind {
    val d = offset.toPx()
    drawRect(color, topLeft = Offset(d, d), size = size)
}

/** Graph paper: the canvas behind every screen. */
fun Modifier.graphPaper(): Modifier = background(Hub.C.bg).drawBehind {
    val step = Hub.D.gridStep.toPx()
    val w = Hub.D.hair.toPx()
    var x = 0f
    while (x < size.width) { drawLine(Hub.C.grid, Offset(x, 0f), Offset(x, size.height), w); x += step }
    var y = 0f
    while (y < size.height) { drawLine(Hub.C.grid, Offset(0f, y), Offset(size.width, y), w); y += step }
}

enum class ButtonKind { Primary, Plain, Danger }

/** A contained button. Primary is the yellow action; there is at most one per row or bar. */
@Composable
fun HubButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    kind: ButtonKind = ButtonKind.Plain,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    testTag: String? = null
) {
    val fill = when (kind) { ButtonKind.Primary -> Hub.C.action; else -> Hub.C.raised }
    val ink = when (kind) { ButtonKind.Primary -> Hub.C.actionInk; ButtonKind.Danger -> Hub.C.danger; ButtonKind.Plain -> Hub.C.text }
    val edge = when (kind) { ButtonKind.Primary -> Hub.C.shadow; ButtonKind.Danger -> Hub.C.danger; ButtonKind.Plain -> Hub.C.border }
    Box(
        modifier = modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .heightIn(min = Hub.D.touch)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .then(if (kind == ButtonKind.Primary && enabled) Modifier.hardShadow() else Modifier)
                .clip(Hub.S.control)
                .background(if (enabled) fill else Hub.C.surface)
                .border(Hub.D.border, if (enabled) edge else Hub.C.borderSoft, Hub.S.control)
                .heightIn(min = Hub.D.control)
                .padding(horizontal = Hub.D.gap + Hub.D.gapSm, vertical = Hub.D.gapSm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Hub.D.gap, Alignment.CenterHorizontally)
        ) {
            val tint = if (enabled) ink else Hub.C.faint
            if (icon != null) Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(Hub.D.icon))
            Text(text.uppercase(), style = Hub.T.button, color = tint, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** A square icon button with a full 48 dp target and a required name. */
@Composable
fun HubIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    enabled: Boolean = true,
    testTag: String? = null
) {
    Box(
        modifier = modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .size(Hub.D.touch)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(Hub.D.control)
                .clip(Hub.S.control)
                .background(Hub.C.raised)
                .border(Hub.D.border, if (active) Hub.C.action else Hub.C.border, Hub.S.control),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (!enabled) Hub.C.faint else if (active) Hub.C.action else Hub.C.text, modifier = Modifier.size(Hub.D.icon))
        }
    }
}

enum class Tone { Plain, Ok, Game, Warn, Danger }

/** A tag is data, not a button: a thin outline and no fill. */
@Composable
fun Tag(text: String, tone: Tone = Tone.Plain, modifier: Modifier = Modifier) {
    val color = when (tone) { Tone.Ok -> Hub.C.ok; Tone.Game -> Hub.C.game; Tone.Warn -> Hub.C.warn; Tone.Danger -> Hub.C.danger; Tone.Plain -> Hub.C.dim }
    Text(
        text = text.uppercase(),
        style = Hub.T.label,
        color = color,
        maxLines = 1,
        modifier = modifier
            .border(Hub.D.hair, if (tone == Tone.Plain) Hub.C.borderSoft else color.copy(alpha = 0.45f), Hub.S.control)
            .padding(horizontal = Hub.D.gapSm + Hub.D.hair, vertical = Hub.D.hair + Hub.D.hair)
    )
}

/** Two letters for an app the catalog has no icon for. */
fun monogram(name: String): String =
    name.replace(Regex("[^A-Za-z ]"), "").split(Regex("\\s+|(?<=[a-z])(?=[A-Z])")).filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1).uppercase() }

/** An app's icon from the catalog, or its monogram while loading or when there is none. */
@Composable
fun AppIcon(name: String, iconUrl: String, isGame: Boolean, modifier: Modifier = Modifier, large: Boolean = false) {
    val size = if (large) Hub.D.iconTileLg else Hub.D.iconTile
    val letters: @Composable () -> Unit = { Text(monogram(name), style = if (large) Hub.T.display else Hub.T.title, color = Hub.C.text) }
    Box(
        modifier = modifier
            .size(size)
            .clip(Hub.S.tile)
            .background(Hub.C.raised)
            .border(Hub.D.border, if (isGame) Hub.C.game.copy(alpha = 0.55f) else Hub.C.border, Hub.S.tile)
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center
    ) {
        if (iconUrl.isBlank()) letters() else SubcomposeAsyncImage(
            model = iconUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            loading = { Box(contentAlignment = Alignment.Center) { letters() } },
            error = { Box(contentAlignment = Alignment.Center) { letters() } },
            modifier = Modifier.size(size)
        )
    }
}

fun PlatformFilter.icon(): ImageVector = when (this) {
    PlatformFilter.ALL -> Icons.Default.Devices
    PlatformFilter.ANDROID -> Icons.Default.Android
    PlatformFilter.WINDOWS -> Icons.Default.Window
    PlatformFilter.LINUX -> Icons.Default.Terminal
}

/** The wordmark: white, with the brand colour offset behind it. */
@Composable
fun HubLogo(modifier: Modifier = Modifier) {
    Box(modifier = modifier.clearAndSetSemantics { contentDescription = "RykerSoft" }) {
        val painter = painterResource(R.drawable.rykersoft_logo)
        Image(painter, null, colorFilter = ColorFilter.tint(Hub.C.brand), modifier = Modifier.height(Hub.D.logoHeight).offset(Hub.D.border, Hub.D.border))
        Image(painter, null, colorFilter = ColorFilter.tint(Hub.C.text), modifier = Modifier.height(Hub.D.logoHeight))
    }
}

/** A section name: mono capitals after the brand mark, with an optional count. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, count: Int? = null) {
    Row(
        modifier = modifier.semantics(mergeDescendants = true) { heading() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Hub.D.gap)
    ) {
        Box(Modifier.width(Hub.D.rail).height(Hub.D.gap + Hub.D.gapSm).background(Hub.C.brand))
        Text(text.uppercase(), style = Hub.T.label, color = Hub.C.dim)
        if (count != null) Text(count.toString(), style = Hub.T.data, color = Hub.C.faint)
    }
}

/** A bordered group of rows. Rows are divided by hairlines. */
@Composable
fun Group(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Hub.C.surface)
            .border(Hub.D.border, Hub.C.border),
        content = content
    )
}

@Composable
fun GroupDivider() {
    Box(Modifier.fillMaxWidth().height(Hub.D.hair).background(Hub.C.borderSoft))
}

/** One row in a [Group]: a title, an optional second line, and an optional trailing control. */
@Composable
fun GroupRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    subtitleColor: Color = Hub.C.dim,
    icon: ImageVector? = null,
    danger: Boolean = false,
    onClick: (() -> Unit)? = null,
    testTag: String? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .fillMaxWidth()
            .heightIn(min = Hub.D.touch)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = Hub.D.gap + Hub.D.gapSm, vertical = Hub.D.gap),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Hub.D.gap)
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = if (danger) Hub.C.danger else Hub.C.dim, modifier = Modifier.size(Hub.D.icon))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = Hub.T.bodyStrong, color = if (danger) Hub.C.danger else Hub.C.text)
            if (!subtitle.isNullOrBlank()) Text(subtitle, style = Hub.T.bodySm, color = subtitleColor)
        }
        if (trailing != null) trailing()
    }
}

/** A row with a switch. The whole row toggles, and the change applies at once. */
@Composable
fun SwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, enabled: Boolean = true) {
    GroupRow(
        title = title,
        subtitle = subtitle,
        onClick = if (enabled) ({ onCheckedChange(!checked) }) else null,
        modifier = Modifier.semantics { stateDescription = if (checked) "On" else "Off" }
    ) {
        HubSwitch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
fun HubSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, enabled: Boolean = true) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Hub.C.actionInk,
            checkedTrackColor = Hub.C.action,
            checkedBorderColor = Hub.C.shadow,
            uncheckedThumbColor = Hub.C.dim,
            uncheckedTrackColor = Hub.C.raised,
            uncheckedBorderColor = Hub.C.border
        )
    )
}

/** A segmented choice. The selected segment is filled with the action colour. */
@Composable
fun <T> Segmented(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.clip(Hub.S.control).background(Hub.C.raised).border(Hub.D.border, Hub.C.border, Hub.S.control)) {
        options.forEachIndexed { index, (value, label) ->
            val on = value == selected
            if (index > 0) Box(Modifier.width(Hub.D.border).height(Hub.D.control).background(Hub.C.border))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = Hub.D.control)
                    .background(if (on) Hub.C.action else Color.Transparent)
                    .clickable(role = Role.Tab) { onSelect(value) }
                    .semantics { this.selected = on }
                    .padding(horizontal = Hub.D.gap),
                contentAlignment = Alignment.Center
            ) {
                Text(label.uppercase(), style = Hub.T.button, color = if (on) Hub.C.actionInk else Hub.C.dim, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/** A menu of exclusive choices that opens from a button. */
@Composable
fun <T> ChoiceMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    icon: ((T) -> ImageVector)? = null,
    count: ((T) -> Int)? = null
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        containerColor = Hub.C.surface,
        border = BorderStroke(Hub.D.border, Hub.C.border),
        shape = Hub.S.tile,
        modifier = Modifier.widthIn(min = Hub.D.menuWidth)
    ) {
        options.forEach { option ->
            val on = option == selected
            DropdownMenuItem(
                text = { Text(label(option), style = Hub.T.bodyStrong, color = Hub.C.text) },
                leadingIcon = icon?.let { pick -> { Icon(pick(option), contentDescription = null, tint = if (on) Hub.C.action else Hub.C.dim, modifier = Modifier.size(Hub.D.icon)) } },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Hub.D.gap)) {
                        if (count != null) Text(count(option).toString(), style = Hub.T.data, color = Hub.C.faint)
                        if (on) Icon(Icons.Default.Check, contentDescription = null, tint = Hub.C.action, modifier = Modifier.size(Hub.D.icon))
                    }
                },
                onClick = { onSelect(option); onDismiss() },
                modifier = Modifier
                    .background(if (on) Hub.C.raised else Color.Transparent)
                    .semantics { this.selected = on }
            )
        }
    }
}

/** A text field in the hub's colours. Pass [secret] for keys and passwords: it adds a show/hide control. */
@Composable
fun HubTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    secret: Boolean = false,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
    onDone: (() -> Unit)? = null,
    isError: Boolean = false,
    testTag: String? = null
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = Hub.T.bodySm) },
        singleLine = singleLine,
        isError = isError,
        textStyle = Hub.T.data.copy(color = Hub.C.text),
        shape = Hub.S.control,
        visualTransformation = if (secret && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (secret) KeyboardType.Password else keyboardType, imeAction = imeAction, autoCorrectEnabled = false),
        keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }, onSearch = { onDone?.invoke() }),
        trailingIcon = if (!secret) null else ({
            HubIconButton(
                icon = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = if (visible) "Hide $label" else "Show $label",
                onClick = { visible = !visible }
            )
        }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Hub.C.text,
            unfocusedTextColor = Hub.C.text,
            focusedBorderColor = Hub.C.link,
            unfocusedBorderColor = Hub.C.border,
            focusedLabelColor = Hub.C.link,
            unfocusedLabelColor = Hub.C.dim,
            cursorColor = Hub.C.link,
            focusedContainerColor = Hub.C.surface,
            unfocusedContainerColor = Hub.C.surface,
            errorBorderColor = Hub.C.danger,
            errorLabelColor = Hub.C.danger
        ),
        modifier = modifier.fillMaxWidth().then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    )
}

/** A dialog in the hub's frame. Actions sit at the bottom right; the safe choice comes first. */
@Composable
fun HubDialog(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Hub.C.surface,
            contentColor = Hub.C.text,
            shape = Hub.S.tile,
            border = BorderStroke(Hub.D.border, Hub.C.border),
            modifier = modifier.fillMaxWidth().hardShadow(Hub.D.shadowLg)
        ) {
            Column(modifier = Modifier.padding(Hub.D.gapLg), verticalArrangement = Arrangement.spacedBy(Hub.D.gap + Hub.D.gapSm)) {
                Text(title, style = Hub.T.title, color = Hub.C.text, modifier = Modifier.semantics { heading() })
                content()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Hub.D.gap, Alignment.End), content = actions)
            }
        }
    }
}

@Composable
fun Gap(size: Dp = Hub.D.gap) {
    Spacer(Modifier.size(size))
}

/** Splits a catalog changelog ("a | b" or one item per line) into its items. */
fun changelogItems(changelog: String): List<String> =
    changelog.split(Regex("\\s*\\|\\s*|\\n")).map { it.trim().trimStart('•', '-', '*').trim() }.filter { it.isNotEmpty() }

/** A list of short lines, each behind a dash. */
@Composable
fun DashList(items: List<String>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Hub.D.gapSm + Hub.D.hair)) {
        items.forEach { item ->
            Row(horizontalArrangement = Arrangement.spacedBy(Hub.D.gap)) {
                Box(Modifier.padding(top = Hub.D.gap + Hub.D.hair).width(Hub.D.gap).height(Hub.D.border).background(Hub.C.faint))
                Text(item, style = Hub.T.body, color = Hub.C.dim, modifier = Modifier.defaultMinSize(minHeight = Hub.D.gap))
            }
        }
    }
}
