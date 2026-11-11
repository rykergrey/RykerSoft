package com.rykersoft.appmanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rykersoft.appmanager.ui.kit.AppIcon
import com.rykersoft.appmanager.ui.kit.ButtonKind
import com.rykersoft.appmanager.ui.kit.DashList
import com.rykersoft.appmanager.ui.kit.Group
import com.rykersoft.appmanager.ui.kit.GroupDivider
import com.rykersoft.appmanager.ui.kit.GroupRow
import com.rykersoft.appmanager.ui.kit.HubButton
import com.rykersoft.appmanager.ui.kit.HubDialog
import com.rykersoft.appmanager.ui.kit.HubIconButton
import com.rykersoft.appmanager.ui.kit.SectionLabel
import com.rykersoft.appmanager.ui.kit.Segmented
import com.rykersoft.appmanager.ui.kit.Tag
import com.rykersoft.appmanager.ui.kit.Tone
import com.rykersoft.appmanager.ui.kit.changelogItems
import com.rykersoft.appmanager.ui.kit.icon
import com.rykersoft.appmanager.ui.theme.Hub

enum class AppDetailTab(val label: String) {
    UPDATES("What's new"),
    DESCRIPTION("About"),
    SPECS("Specs")
}

/** An app with an update waiting opens on What's new; everything else opens on About. */
fun defaultAppDetailTab(app: AppUiItem): AppDetailTab = if (app.isOutdated) AppDetailTab.UPDATES else AppDetailTab.DESCRIPTION

/**
 * One app's page. What it is and where it stands read from the top; the action sits in a bar at
 * the thumb. Starting a download never changes the tab or scroll position.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppDetailScreen(
    app: AppUiItem,
    downloadingPackage: String?,
    downloadProgress: Int,
    accountEmail: String?,
    onBack: () -> Unit,
    onInstall: () -> Unit,
    onLaunch: () -> Unit,
    onCopyLink: (label: String, url: String) -> Unit,
    onShareLink: (label: String, url: String) -> Unit,
    onOpenLink: (url: String) -> Unit,
    modifier: Modifier = Modifier,
    initialTab: AppDetailTab = defaultAppDetailTab(app)
) {
    var tab by rememberSaveable(app.packageName) { mutableStateOf(initialTab) }
    var viewerIndex by rememberSaveable(app.packageName) { mutableIntStateOf(-1) }
    var showPro by rememberSaveable(app.packageName) { mutableStateOf(false) }
    val downloading = downloadingPackage == app.packageName
    val busyElsewhere = downloadingPackage != null && !downloading
    val tabs = AppDetailTab.entries.filter { it != AppDetailTab.SPECS || app.specs.isNotBlank() }

    Column(modifier = modifier.fillMaxSize().testTag("app_detail_${app.packageName}")) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Hub.D.gutter - Hub.D.gapSm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HubIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Back to the catalog", onBack, testTag = "detail_back_button")
            Text(if (app.isGame) "GAMES" else "APPS", style = Hub.T.label, color = Hub.C.faint, modifier = Modifier.padding(start = Hub.D.gap))
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Hub.D.gutter).padding(bottom = Hub.D.gapLg),
            verticalArrangement = Arrangement.spacedBy(Hub.D.gap + Hub.D.gapSm)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(Hub.D.gap + Hub.D.gapSm), verticalAlignment = Alignment.CenterVertically) {
                AppIcon(app.name, app.iconUrl, app.isGame, large = true)
                Column(verticalArrangement = Arrangement.spacedBy(Hub.D.gapSm)) {
                    Text(app.name, style = Hub.T.display, color = Hub.C.text, modifier = Modifier.semantics { heading() })
                    Text(
                        text = "v${app.latestVersionName}" + if (app.releasedAt.isNotBlank()) " · ${app.releasedAt}" else "",
                        style = Hub.T.data,
                        color = Hub.C.dim
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(Hub.D.gapSm), verticalArrangement = Arrangement.spacedBy(Hub.D.gapSm)) {
                        if (app.isGame) Tag("Game", Tone.Game)
                        when {
                            app.aiUnlocked -> Tag("Pro on", Tone.Ok)
                            app.supportsAiUnlock || app.proNote.isNotBlank() -> Tag("Free + Pro")
                            else -> Tag("Free", Tone.Ok)
                        }
                        PlatformFilter.entries.filter { it != PlatformFilter.ALL && app.isOn(it) }.forEach { Tag(it.label) }
                    }
                }
            }
            if (app.tagline.isNotBlank()) Text(app.tagline, style = Hub.T.body, color = Hub.C.dim)

            if (app.screenshots.isNotEmpty()) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(Hub.D.gap)) {
                    app.screenshots.forEachIndexed { index, url ->
                        AdaptiveScreenshotThumbnail(
                            url = url,
                            contentDescription = "${app.name} screenshot ${index + 1} of ${app.screenshots.size}",
                            fixedHeight = Hub.D.shotHeight,
                            fallbackAspectRatio = 9f / 19f,
                            onClick = { viewerIndex = index }
                        )
                    }
                }
            }

            Segmented(options = tabs.map { it to it.label }, selected = tab, onSelect = { tab = it }, modifier = Modifier.fillMaxWidth())

            when (tab) {
                AppDetailTab.UPDATES -> {
                    val history = app.updatesHistory.ifBlank { null }
                    val latest = changelogItems(app.changelog)
                    when {
                        history != null -> MarkdownBody(markdown = history, headingColor = Hub.C.text, accentColor = Hub.C.faint)
                        latest.isNotEmpty() -> { SectionLabel("Version ${app.latestVersionName}"); DashList(latest) }
                        else -> Text("No release notes have been published for this version.", style = Hub.T.body, color = Hub.C.dim)
                    }
                }
                AppDetailTab.DESCRIPTION -> {
                    if (app.description.isNotBlank()) MarkdownBody(markdown = app.description, accentColor = Hub.C.faint)
                    else Text("No description has been published yet.", style = Hub.T.body, color = Hub.C.dim)
                    if (app.features.isNotEmpty()) DashList(app.features)
                }
                AppDetailTab.SPECS -> MarkdownBody(markdown = app.specs, accentColor = Hub.C.faint)
            }

            if (app.supportsAiUnlock || app.proNote.isNotBlank()) {
                SectionLabel("Free and Pro")
                Group {
                    GroupRow(
                        title = if (app.aiUnlocked) "Pro is on for your account" else "Free, with optional Pro features",
                        subtitle = app.proNote.ifBlank { "Features that need an API key use Pro access. Everything else is free." },
                        onClick = if (app.supportsAiUnlock) ({ showPro = true }) else null,
                        testTag = "pro_access_row"
                    ) { if (app.aiUnlocked) Tag("On", Tone.Ok) }
                }
            }

            if (app.desktopBuilds.isNotEmpty()) {
                SectionLabel(if (app.hasAndroid) "Also on desktop" else "Desktop downloads")
                Group {
                    app.desktopBuilds.forEachIndexed { index, build ->
                        if (index > 0) GroupDivider()
                        val label = "${app.name} for ${build.platform.label} (${build.format})"
                        val behind = build.version != null && build.version != app.latestVersionName
                        GroupRow(
                            title = "${build.platform.label} · ${build.format}",
                            subtitle = (build.version?.let { "v$it" } ?: "v${app.latestVersionName}") + if (behind) " · behind v${app.latestVersionName}" else "",
                            subtitleColor = if (behind) Hub.C.warn else Hub.C.dim,
                            icon = build.platform.icon()
                        ) {
                            HubIconButton(Icons.Default.ContentCopy, "Copy the $label link", { onCopyLink(label, build.url) })
                            HubIconButton(Icons.Default.Share, "Send the $label link to another device", { onShareLink(label, build.url) })
                            HubIconButton(Icons.AutoMirrored.Filled.OpenInNew, "Open the $label link in the browser", { onOpenLink(build.url) })
                        }
                    }
                }
            }

            SectionLabel("Details")
            Group {
                GroupRow(title = "Package", subtitle = app.packageName)
                if (app.isInstalled) {
                    GroupDivider()
                    GroupRow(title = "On this phone", subtitle = "v${app.installedVersionName ?: "unknown"}")
                }
            }
        }

        // Verbs at the thumb.
        Column(modifier = Modifier.fillMaxWidth().background(Hub.C.bg)) {
            if (downloading) {
                LinearProgressIndicator(
                    progress = { downloadProgress.coerceIn(0, 100) / 100f },
                    color = Hub.C.action, trackColor = Hub.C.raised, drawStopIndicator = {},
                    modifier = Modifier.fillMaxWidth().height(Hub.D.progress)
                )
            } else {
                Box(Modifier.fillMaxWidth().height(Hub.D.border).background(Hub.C.borderSoft))
            }
            Row(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(start = Hub.D.gutter, end = Hub.D.gutter - Hub.D.gapSm, top = Hub.D.gapSm, bottom = Hub.D.gapSm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Hub.D.gapSm)
            ) {
                Column(modifier = Modifier.weight(1f).testTag("detail_version_${app.packageName}").semantics(mergeDescendants = true) { }) {
                    Text(
                        text = when {
                            downloading -> "Downloading"
                            app.isDesktopOnly -> "Desktop app"
                            app.isOutdated -> "Update available"
                            app.isInstalled -> "Up to date"
                            else -> "Not installed"
                        }.uppercase(),
                        style = Hub.T.label,
                        color = if (app.isOutdated || downloading) Hub.C.action else if (app.isInstalled) Hub.C.ok else Hub.C.dim
                    )
                    Text(
                        text = when {
                            downloading -> "$downloadProgress%"
                            app.isDesktopOnly -> "Send a link to your computer"
                            app.isOutdated -> "${app.installedVersionName ?: "?"} → ${app.latestVersionName}"
                            else -> "v${app.latestVersionName}"
                        },
                        style = Hub.T.data, color = Hub.C.text, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
                if (app.hasAndroid) {
                    HubIconButton(Icons.Default.Share, "Share the ${app.name} APK link", { onShareLink("${app.name} for Android (APK)", app.apkUrl) }, testTag = "share_detail_apk_${app.packageName}")
                    when {
                        downloading -> Unit
                        app.isOutdated -> HubButton("Update", onInstall, kind = ButtonKind.Primary, icon = Icons.Default.Download, enabled = !busyElsewhere, testTag = "detail_action_${app.packageName}")
                        !app.isInstalled -> HubButton("Install", onInstall, kind = ButtonKind.Primary, icon = Icons.Default.Download, enabled = !busyElsewhere, testTag = "detail_action_${app.packageName}")
                        else -> HubButton("Open", onLaunch, kind = ButtonKind.Primary, testTag = "detail_action_${app.packageName}")
                    }
                }
            }
        }
    }

    if (viewerIndex >= 0 && app.screenshots.isNotEmpty()) {
        Dialog(onDismissRequest = { viewerIndex = -1 }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Column(
                modifier = Modifier.fillMaxSize().background(Hub.C.scrim).statusBarsPadding().navigationBarsPadding().padding(Hub.D.gap),
                verticalArrangement = Arrangement.spacedBy(Hub.D.gap)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("${viewerIndex + 1} / ${app.screenshots.size}", style = Hub.T.data, color = Hub.C.dim, modifier = Modifier.weight(1f).padding(start = Hub.D.gap))
                    HubIconButton(Icons.Default.Close, "Close screenshots", { viewerIndex = -1 })
                }
                SwipeableZoomableScreenshotPreview(
                    screenshots = app.screenshots,
                    currentIndex = viewerIndex,
                    onIndexChange = { viewerIndex = it },
                    maxHeight = Hub.D.viewerHeight,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (showPro) ProAccessDialog(appName = app.name, accountEmail = accountEmail, unlocked = app.aiUnlocked, onDismiss = { showPro = false })
}

@Composable
fun ProAccessDialog(appName: String, accountEmail: String?, unlocked: Boolean = false, onDismiss: () -> Unit) {
    HubDialog(
        title = "Pro access for $appName",
        onDismiss = onDismiss,
        modifier = Modifier.testTag("pro_access_info_dialog"),
        actions = { HubButton("Close", onDismiss) }
    ) {
        Text(
            if (unlocked) "Pro is switched on for this account. Sign in with the same Google account inside $appName to use it."
            else "Pro is switched on per account by the RykerSoft administrator. Ask for $appName to be enabled, then sign in with the same Google account inside the app.",
            style = Hub.T.body, color = Hub.C.dim
        )
        Column(modifier = Modifier.fillMaxWidth().border(Hub.D.hair, Hub.C.borderSoft).padding(Hub.D.gap + Hub.D.gapSm)) {
            Text("SIGNED-IN ACCOUNT", style = Hub.T.label, color = Hub.C.faint)
            Text(accountEmail ?: "Not signed in. Sign in from Settings.", style = Hub.T.data, color = Hub.C.text)
        }
        Text("Every feature that does not need an API key is free and keeps working without Pro.", style = Hub.T.bodySm, color = Hub.C.dim)
    }
}
