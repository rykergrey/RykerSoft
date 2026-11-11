package com.rykersoft.appmanager

import com.rykersoft.appmanager.ui.AppUiItem
import com.rykersoft.appmanager.ui.FilterType
import com.rykersoft.appmanager.ui.PlatformFilter
import com.rykersoft.appmanager.ui.SortOption
import com.rykersoft.appmanager.ui.catalogSections
import com.rykersoft.appmanager.ui.kit.changelogItems
import com.rykersoft.appmanager.ui.kit.monogram
import com.rykersoft.appmanager.ui.shortDate
import com.rykersoft.appmanager.ui.sortApps
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

internal fun testApp(
    name: String,
    packageName: String = "pkg.${name.lowercase().replace(" ", "")}",
    version: String = "1.0.0",
    releasedAt: String = "",
    installed: Boolean = false,
    installedVersion: String? = if (installed) version else null,
    outdated: Boolean = false,
    apkUrl: String = "https://example.invalid/download/x-v$version/app.apk",
    exeUrl: String = "",
    linuxUrl: String = "",
    isGame: Boolean = false,
    supportsPro: Boolean = false,
    proOn: Boolean = false
) = AppUiItem(
    packageName = packageName, name = name, description = "$name does one thing well.", summaryDescription = "",
    latestVersionCode = 2, latestVersionName = version, apkUrl = apkUrl, exeUrl = exeUrl, linuxUrl = linuxUrl,
    icon = "android", changelog = "A new screen | Faster start", screenshots = emptyList(), isGame = isGame,
    isInstalled = installed, installedVersionName = installedVersion, installedVersionCode = if (installed) 1L else null,
    isOutdated = outdated, statusText = "", updatesHistory = "## $version · 2026-11-01\n- A new screen\n- Faster start",
    specs = "", tagline = "$name in one line.", features = listOf("First feature", "Second feature"),
    proNote = if (supportsPro) "Pro adds extra tools." else "", releasedAt = releasedAt,
    supportsAiUnlock = supportsPro, aiUnlocked = proOn
)

class CatalogLogicTest {
    @Test
    fun `latest release sorts by release date, not by version number`() {
        val apps = listOf(
            testApp("Old Big Version", version = "9.9.9", releasedAt = "2026-01-05"),
            testApp("Fresh", version = "0.1.0", releasedAt = "2026-11-10"),
            testApp("Middle", version = "2.0.0", releasedAt = "2026-06-01")
        )
        assertEquals(listOf("Fresh", "Middle", "Old Big Version"), sortApps(apps, SortOption.RECENTLY_UPDATED).map { it.name })
        assertEquals(listOf("Fresh", "Middle", "Old Big Version"), sortApps(apps, SortOption.NAME_ASC).map { it.name })
    }

    @Test
    fun `needs update first puts outdated apps ahead of installed and not installed`() {
        val apps = listOf(
            testApp("Absent"),
            testApp("Current", installed = true),
            testApp("Stale", installed = true, installedVersion = "0.9.0", outdated = true)
        )
        assertEquals(listOf("Stale", "Current", "Absent"), sortApps(apps, SortOption.STATUS).map { it.name })
    }

    @Test
    fun `all systems groups by what needs you first`() {
        val apps = listOf(
            testApp("Absent"),
            testApp("Current", installed = true),
            testApp("Stale", installed = true, installedVersion = "0.9.0", outdated = true),
            testApp("Desk", apkUrl = "", exeUrl = "https://example.invalid/download/desk-v1.0.0/desk.exe")
        )
        val sections = catalogSections(apps, PlatformFilter.ALL, FilterType.ALL, "", SortOption.NAME_ASC)
        assertEquals(listOf("Updates", "Installed", "Not installed", "Desktop only"), sections.map { it.title })
        assertEquals(listOf("Stale"), sections[0].apps.map { it.name })
        assertEquals(listOf("Desk"), sections[3].apps.map { it.name })
    }

    @Test
    fun `a system filter narrows the catalog to that system`() {
        val apps = listOf(
            testApp("Phone Only"),
            testApp("Everywhere", exeUrl = "https://example.invalid/download/e-v1.0.0/e.exe", linuxUrl = "https://example.invalid/download/e-v0.9.0/e.AppImage"),
            testApp("Desk", apkUrl = "", exeUrl = "https://example.invalid/download/desk-v1.0.0/desk.exe")
        )
        fun names(platform: PlatformFilter) = catalogSections(apps, platform, FilterType.ALL, "", SortOption.NAME_ASC).flatMap { it.apps }.map { it.name }
        assertEquals(listOf("Phone Only", "Everywhere", "Desk").sorted(), names(PlatformFilter.ALL).sorted())
        assertEquals(listOf("Everywhere", "Phone Only"), names(PlatformFilter.ANDROID).sorted())
        assertEquals(listOf("Desk", "Everywhere"), names(PlatformFilter.WINDOWS))
        assertEquals(listOf("Everywhere"), names(PlatformFilter.LINUX))
    }

    @Test
    fun `search and kind filters combine`() {
        val apps = listOf(testApp("Word Game", isGame = true), testApp("Word Tool"), testApp("Other Game", isGame = true))
        val found = catalogSections(apps, PlatformFilter.ALL, FilterType.GAMES, "word", SortOption.NAME_ASC).flatMap { it.apps }.map { it.name }
        assertEquals(listOf("Word Game"), found)
    }

    @Test
    fun `a desktop file knows the version in its release tag`() {
        val app = testApp("Everywhere", version = "2.3.3", exeUrl = "https://github.com/o/r/releases/download/everywhere-v2.2.11/Everywhere.exe", linuxUrl = "https://example.invalid/plain/Everywhere.AppImage")
        assertEquals("2.2.11", app.desktopBuilds[0].version)
        assertEquals("EXE", app.desktopBuilds[0].format)
        assertNull(app.desktopBuilds[1].version)
        assertEquals("AppImage", app.desktopBuilds[1].format)
    }

    @Test
    fun `small formatters`() {
        assertEquals("Nov 10", shortDate("2026-11-10"))
        assertEquals("soon", shortDate("soon"))
        assertEquals(listOf("A new screen", "Faster start"), changelogItems("A new screen | Faster start"))
        assertEquals(listOf("One", "Two"), changelogItems("- One\n• Two"))
        assertEquals("ST", monogram("SuperThink.ing"))
        assertEquals("BT", monogram("BetterTracking"))
    }
}
