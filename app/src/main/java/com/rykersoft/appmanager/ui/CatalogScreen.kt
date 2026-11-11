package com.rykersoft.appmanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import com.rykersoft.appmanager.ui.kit.AppIcon
import com.rykersoft.appmanager.ui.kit.ButtonKind
import com.rykersoft.appmanager.ui.kit.ChoiceMenu
import com.rykersoft.appmanager.ui.kit.HubButton
import com.rykersoft.appmanager.ui.kit.HubIconButton
import com.rykersoft.appmanager.ui.kit.HubLogo
import com.rykersoft.appmanager.ui.kit.SectionLabel
import com.rykersoft.appmanager.ui.kit.Segmented
import com.rykersoft.appmanager.ui.kit.Tag
import com.rykersoft.appmanager.ui.kit.Tone
import com.rykersoft.appmanager.ui.kit.icon
import com.rykersoft.appmanager.ui.theme.Hub

/** One titled run of apps in the catalog list. */
data class CatalogSection(val title: String, val apps: List<AppUiItem>)

private val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

/** "2026-11-10" becomes "Nov 10". Anything else is returned unchanged. */
fun shortDate(iso: String): String {
    val parts = iso.split("-")
    val month = parts.getOrNull(1)?.toIntOrNull()
    val day = parts.getOrNull(2)?.toIntOrNull()
    return if (parts.size == 3 && month != null && month in 1..12 && day != null) "${MONTHS[month - 1]} $day" else iso
}

fun matchesKind(app: AppUiItem, kind: FilterType): Boolean = when (kind) {
    FilterType.GAMES -> app.isGame
    FilterType.APPS -> !app.isGame
    else -> true
}

/**
 * Groups the catalog for the list. On this phone's own system the order is what needs you first:
 * updates, then what you have, then what you could install, then desktop-only apps. With Windows
 * or Linux selected there is one list of that system's downloads.
 */
fun catalogSections(apps: List<AppUiItem>, platform: PlatformFilter, kind: FilterType, query: String, sort: SortOption): List<CatalogSection> {
    val q = query.trim()
    val visible = sortApps(
        apps.filter { app ->
            app.isOn(platform) && matchesKind(app, kind) &&
                (q.isEmpty() || app.name.contains(q, true) || app.tagline.contains(q, true) || app.packageName.contains(q, true))
        },
        sort
    )
    if (platform == PlatformFilter.WINDOWS || platform == PlatformFilter.LINUX) {
        return listOf(CatalogSection("${platform.label} downloads", visible)).filter { it.apps.isNotEmpty() }
    }
    return listOf(
        CatalogSection("Updates", visible.filter { it.isOutdated }),
        CatalogSection("Installed", visible.filter { it.isInstalled && !it.isOutdated }),
        CatalogSection("Not installed", visible.filter { it.hasAndroid && !it.isInstalled }),
        CatalogSection("Desktop only", visible.filter { it.isDesktopOnly })
    ).filter { it.apps.isNotEmpty() }
}

@Composable
fun CatalogScreen(
    state: MainUiState,
    search: String,
    onSearchChange: (String) -> Unit,
    onOpen: (AppUiItem) -> Unit,
    onInstall: (AppUiItem) -> Unit,
    onLaunch: (AppUiItem) -> Unit,
    onKind: (FilterType) -> Unit,
    onPlatform: (PlatformFilter) -> Unit,
    onSort: (SortOption) -> Unit,
    onSync: () -> Unit,
    onSettings: () -> Unit,
    onCancelInstall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val kind = if (state.filterType == FilterType.GAMES || state.filterType == FilterType.APPS) state.filterType else FilterType.ALL
    val inScope = remember(state.apps, state.platform) { state.apps.filter { it.isOn(state.platform) } }
    val sections = remember(state.apps, state.platform, kind, search, state.sortOption) {
        catalogSections(state.apps, state.platform, kind, search, state.sortOption)
    }
    val updates = remember(state.apps) { state.apps.count { it.isOutdated } }

    Column(modifier = modifier.fillMaxSize().testTag("app_dashboard_scaffold")) {
        // State at the top: what the catalog holds and whether it is being checked right now.
        Column(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Hub.D.gutter),
            verticalArrangement = Arrangement.spacedBy(Hub.D.gap)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Hub.D.gap)) {
                HubLogo()
                Text(
                    text = when {
                        state.isSyncing -> "Checking for updates…"
                        state.apps.isEmpty() -> "No catalog yet"
                        updates > 0 -> "$updates update${if (updates == 1) "" else "s"} · ${state.apps.size} apps"
                        else -> "Up to date · ${state.apps.size} apps"
                    },
                    style = Hub.T.data,
                    color = if (updates > 0 && !state.isSyncing) Hub.C.action else Hub.C.dim,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(start = Hub.D.gap).testTag("catalog_status")
                )
                if (state.isSyncing) {
                    Box(Modifier.size(Hub.D.touch), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Hub.C.link, strokeWidth = Hub.D.border, modifier = Modifier.size(Hub.D.icon))
                    }
                } else {
                    HubIconButton(Icons.Default.Refresh, "Check for updates", onSync, testTag = "sync_action_button")
                }
            }
            if (state.installSessionActive) {
                InstallBanner(state.installStatusMessage ?: "Waiting for you to confirm the install…", onCancelInstall)
            }
            Segmented(
                options = listOf(
                    FilterType.ALL to "All ${inScope.size}",
                    FilterType.APPS to "Apps ${inScope.count { !it.isGame }}",
                    FilterType.GAMES to "Games ${inScope.count { it.isGame }}"
                ),
                selected = kind,
                onSelect = onKind,
                modifier = Modifier.fillMaxWidth()
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().testTag("apps_lazy_column"),
            contentPadding = PaddingValues(start = Hub.D.gutter, end = Hub.D.gutter, top = Hub.D.gap, bottom = Hub.D.gapLg)
        ) {
            if (sections.isEmpty()) {
                item(key = "empty") {
                    EmptyCatalog(
                        hasCatalog = state.apps.isNotEmpty(),
                        isSyncing = state.isSyncing,
                        onSync = onSync,
                        onClear = { onSearchChange(""); onKind(FilterType.ALL); onPlatform(PlatformFilter.ALL) }
                    )
                }
            }
            sections.forEach { section ->
                item(key = "head-${section.title}", contentType = "head") {
                    SectionLabel(section.title, count = section.apps.size, modifier = Modifier.padding(top = Hub.D.gap + Hub.D.gapSm, bottom = Hub.D.gap))
                }
                items(section.apps, key = { "${section.title}-${it.packageName}" }, contentType = { "app" }) { app ->
                    AppRow(
                        app = app,
                        platform = state.platform,
                        downloadingPackage = state.downloadingPackage,
                        downloadProgress = state.downloadProgress,
                        onOpen = { onOpen(app) },
                        onInstall = { onInstall(app) },
                        onLaunch = { onLaunch(app) },
                        modifier = Modifier.padding(bottom = Hub.D.gap)
                    )
                }
            }
        }

        // Verbs at the thumb: find, narrow, order, configure.
        Dock(
            search = search,
            onSearchChange = onSearchChange,
            platform = state.platform,
            apps = state.apps,
            onPlatform = onPlatform,
            sort = state.sortOption,
            onSort = onSort,
            onSettings = onSettings
        )
    }
}

@Composable
private fun InstallBanner(message: String, onCancel: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Hub.C.surface).border(Hub.D.border, Hub.C.warn).padding(start = Hub.D.gap + Hub.D.gapSm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Hub.D.gap)
    ) {
        Text(message, style = Hub.T.bodySm, color = Hub.C.text, modifier = Modifier.weight(1f).padding(vertical = Hub.D.gap))
        HubButton("Cancel", onCancel, testTag = "cancel_install_button")
    }
}

@Composable
private fun EmptyCatalog(hasCatalog: Boolean, isSyncing: Boolean, onSync: () -> Unit, onClear: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = Hub.D.gapLg).border(Hub.D.border, Hub.C.borderSoft).padding(Hub.D.gapLg),
        verticalArrangement = Arrangement.spacedBy(Hub.D.gap),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (hasCatalog) {
            Text("Nothing matches these filters.", style = Hub.T.body, color = Hub.C.dim)
            HubButton("Clear filters", onClear)
        } else if (isSyncing) {
            Text("Fetching the catalog…", style = Hub.T.body, color = Hub.C.dim)
        } else {
            Text("The catalog has not been downloaded yet. Check your connection, then try again.", style = Hub.T.body, color = Hub.C.dim)
            HubButton("Fetch catalog", onSync, kind = ButtonKind.Primary)
        }
    }
}

/** One app: who it is, where it stands, and the one thing to do about it. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppRow(
    app: AppUiItem,
    platform: PlatformFilter,
    downloadingPackage: String?,
    downloadProgress: Int,
    onOpen: () -> Unit,
    onInstall: () -> Unit,
    onLaunch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val desktopView = platform == PlatformFilter.WINDOWS || platform == PlatformFilter.LINUX
    val desktopBuild = if (desktopView) app.desktopBuilds.firstOrNull { it.platform == platform } else null
    val downloading = downloadingPackage == app.packageName
    val busyElsewhere = downloadingPackage != null && !downloading
    val behind = desktopBuild?.version?.let { it != app.latestVersionName } == true

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Hub.C.surface)
            .border(Hub.D.border, if (app.isOutdated && !desktopView) Hub.C.action else Hub.C.border)
            .clickable(role = Role.Button, onClickLabel = "Open ${app.name}", onClick = onOpen)
            .testTag("app_item_${app.packageName}")
    ) {
        Row(
            modifier = Modifier.padding(start = Hub.D.gap + Hub.D.gapSm, top = Hub.D.gap, bottom = Hub.D.gap, end = Hub.D.gapSm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Hub.D.gap + Hub.D.gapSm)
        ) {
            AppIcon(app.name, app.iconUrl, app.isGame)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Hub.D.hair + Hub.D.hair)) {
                Text(app.name, style = Hub.T.title, color = Hub.C.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = when {
                        desktopBuild != null -> "v${desktopBuild.version ?: app.latestVersionName} · ${desktopBuild.format}"
                        app.isOutdated -> "${app.installedVersionName ?: "?"} → ${app.latestVersionName}"
                        app.releasedAt.isNotBlank() -> "v${app.latestVersionName} · ${shortDate(app.releasedAt)}"
                        else -> "v${app.latestVersionName}"
                    },
                    style = Hub.T.data,
                    color = if (app.isOutdated && !desktopView) Hub.C.action else Hub.C.dim,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (app.tagline.isNotBlank()) {
                    Text(app.tagline, style = Hub.T.bodySm, color = Hub.C.dim, maxLines = if (app.isInstalled) 1 else 2, overflow = TextOverflow.Ellipsis)
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Hub.D.gapSm), verticalArrangement = Arrangement.spacedBy(Hub.D.gapSm), modifier = Modifier.padding(top = Hub.D.hair + Hub.D.hair)) {
                    if (app.isGame) Tag("Game", Tone.Game)
                    when {
                        app.aiUnlocked -> Tag("Pro on", Tone.Ok)
                        app.supportsAiUnlock || app.proNote.isNotBlank() -> Tag("Free + Pro")
                        else -> Tag("Free", Tone.Ok)
                    }
                    if (behind) Tag("Behind ${app.latestVersionName}", Tone.Warn)
                }
            }
            when {
                downloading -> Text("$downloadProgress%", style = Hub.T.button, color = Hub.C.action, modifier = Modifier.padding(horizontal = Hub.D.gap + Hub.D.gapSm))
                desktopView || app.isDesktopOnly -> HubButton("Links", onOpen, testTag = "action_${app.packageName}")
                app.isOutdated -> HubButton("Update", onInstall, kind = ButtonKind.Primary, enabled = !busyElsewhere, testTag = "action_${app.packageName}")
                !app.isInstalled -> HubButton("Install", onInstall, enabled = !busyElsewhere, testTag = "action_${app.packageName}")
                else -> HubButton("Open", onLaunch, testTag = "action_${app.packageName}")
            }
        }
        if (downloading) {
            // A real readout: the downloader's own percentage.
            LinearProgressIndicator(
                progress = { downloadProgress.coerceIn(0, 100) / 100f },
                color = Hub.C.action,
                trackColor = Hub.C.raised,
                drawStopIndicator = {},
                modifier = Modifier.fillMaxWidth().height(Hub.D.progress)
            )
        }
    }
}

@Composable
private fun Dock(
    search: String,
    onSearchChange: (String) -> Unit,
    platform: PlatformFilter,
    apps: List<AppUiItem>,
    onPlatform: (PlatformFilter) -> Unit,
    sort: SortOption,
    onSort: (SortOption) -> Unit,
    onSettings: () -> Unit
) {
    var systemOpen by remember { mutableStateOf(false) }
    var sortOpen by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxWidth().background(Hub.C.bg)) {
        Box(Modifier.fillMaxWidth().height(Hub.D.border).background(Hub.C.borderSoft))
        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(horizontal = Hub.D.gutter - Hub.D.gapSm, vertical = Hub.D.gapSm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f).heightIn(min = Hub.D.touch).padding(horizontal = Hub.D.gapSm), contentAlignment = Alignment.Center) {
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = Hub.D.control).clip(Hub.S.control).background(Hub.C.surface).border(Hub.D.border, Hub.C.border, Hub.S.control).padding(start = Hub.D.gap),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Hub.D.gap)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Hub.C.faint, modifier = Modifier.size(Hub.D.icon))
                    BasicTextField(
                        value = search,
                        onValueChange = onSearchChange,
                        singleLine = true,
                        textStyle = Hub.T.data.copy(color = Hub.C.text),
                        cursorBrush = SolidColor(Hub.C.link),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search, autoCorrectEnabled = false),
                        keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                        modifier = Modifier.weight(1f).testTag("search_text_field").semantics { contentDescription = "Search apps" },
                        decorationBox = { field ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (search.isEmpty()) Text("Search", style = Hub.T.data, color = Hub.C.faint)
                                field()
                            }
                        }
                    )
                    if (search.isNotEmpty()) {
                        Box(
                            modifier = Modifier.size(Hub.D.control).clickable(role = Role.Button, onClickLabel = "Clear search") { onSearchChange("") },
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.Close, contentDescription = "Clear search", tint = Hub.C.dim, modifier = Modifier.size(Hub.D.icon)) }
                    }
                }
            }

            // The system filter. All systems is the default.
            Box {
                Box(
                    modifier = Modifier
                        .heightIn(min = Hub.D.touch)
                        .clickable(role = Role.DropdownList, onClickLabel = "Choose a system") { systemOpen = true }
                        .padding(horizontal = Hub.D.gapSm)
                        .testTag("platform_dropdown_chip")
                        .semantics { contentDescription = "System: ${platform.label}" },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.heightIn(min = Hub.D.control).clip(Hub.S.control).background(Hub.C.raised)
                            .border(Hub.D.border, if (platform != PlatformFilter.ALL) Hub.C.action else Hub.C.border, Hub.S.control)
                            .padding(horizontal = Hub.D.gap),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Hub.D.gapSm + Hub.D.hair)
                    ) {
                        val tint = if (platform != PlatformFilter.ALL) Hub.C.action else Hub.C.text
                        Icon(platform.icon(), contentDescription = null, tint = tint, modifier = Modifier.size(Hub.D.icon))
                        Text(if (platform == PlatformFilter.ALL) "ALL" else platform.label.uppercase(), style = Hub.T.button, color = tint, maxLines = 1)
                        Icon(Icons.Default.ExpandMore, contentDescription = null, tint = Hub.C.dim, modifier = Modifier.size(Hub.D.iconSm))
                    }
                }
                ChoiceMenu(
                    expanded = systemOpen,
                    onDismiss = { systemOpen = false },
                    options = PlatformFilter.entries,
                    selected = platform,
                    label = { if (it == PlatformFilter.ALL) it.label else "${it.label} only" },
                    onSelect = onPlatform,
                    icon = { it.icon() },
                    count = { option -> apps.count { it.isOn(option) } }
                )
            }
            Box {
                HubIconButton(Icons.AutoMirrored.Filled.Sort, "Sort: ${sort.label}", { sortOpen = true }, active = sort != SortOption.RECENTLY_UPDATED, testTag = "sort_action_button")
                ChoiceMenu(sortOpen, { sortOpen = false }, SortOption.entries, sort, { it.label }, onSort)
            }
            HubIconButton(Icons.Default.Settings, "Settings", onSettings, testTag = "settings_button")
        }
    }
}
