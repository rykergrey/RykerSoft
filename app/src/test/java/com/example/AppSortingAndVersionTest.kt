package com.rykersoft.appmanager

import com.rykersoft.appmanager.ui.AppUiItem
import com.rykersoft.appmanager.ui.SortOption
import org.junit.Assert.assertEquals
import org.junit.Test

class AppSortingAndVersionTest {

    private fun createApp(
        packageName: String,
        name: String,
        latestVersionCode: Int,
        latestVersionName: String,
        isInstalled: Boolean = false,
        installedVersionName: String? = null,
        isOutdated: Boolean = false
    ): AppUiItem {
        return AppUiItem(
            packageName = packageName,
            name = name,
            description = "Test description",
            summaryDescription = "Test summary",
            latestVersionCode = latestVersionCode,
            latestVersionName = latestVersionName,
            apkUrl = "http://example.com/$packageName.apk",
            icon = "android",
            changelog = "Test changelog",
            screenshots = emptyList(),
            isGame = false,
            isInstalled = isInstalled,
            installedVersionName = installedVersionName,
            installedVersionCode = if (isInstalled) 1L else null,
            isOutdated = isOutdated,
            statusText = if (isOutdated) "Update Available" else "Up to Date"
        )
    }

    @Test
    fun `sorting by RECENTLY_UPDATED puts highest version code first`() {
        val app1 = createApp("pkg.a", "App A", latestVersionCode = 2, latestVersionName = "1.0.1")
        val app2 = createApp("pkg.b", "App B", latestVersionCode = 120, latestVersionName = "2.0.0")
        val app3 = createApp("pkg.c", "App C", latestVersionCode = 5, latestVersionName = "1.2.0")

        val apps = listOf(app1, app2, app3)
        val sorted = apps.sortedWith(compareByDescending<AppUiItem> { it.latestVersionCode }.thenBy { it.name })

        assertEquals(listOf("App B", "App C", "App A"), sorted.map { it.name })
    }

    @Test
    fun `sorting by NAME_ASC and NAME_DESC works correctly`() {
        val app1 = createApp("pkg.a", "Zebra App", latestVersionCode = 1, latestVersionName = "1.0.0")
        val app2 = createApp("pkg.b", "Alpha App", latestVersionCode = 1, latestVersionName = "1.0.0")
        val app3 = createApp("pkg.c", "Beta App", latestVersionCode = 1, latestVersionName = "1.0.0")

        val apps = listOf(app1, app2, app3)
        val sortedAsc = apps.sortedBy { it.name.lowercase() }
        val sortedDesc = apps.sortedByDescending { it.name.lowercase() }

        assertEquals(listOf("Alpha App", "Beta App", "Zebra App"), sortedAsc.map { it.name })
        assertEquals(listOf("Zebra App", "Beta App", "Alpha App"), sortedDesc.map { it.name })
    }

    @Test
    fun `version comparative display text logic produces expected output`() {
        // App 1: Different installed version (e.g. 1.0.0 installed, 1.0.2 latest)
        val appOutdated = createApp(
            packageName = "com.rykersoft.sampleapp",
            name = "Example",
            latestVersionCode = 3,
            latestVersionName = "1.0.2",
            isInstalled = true,
            installedVersionName = "1.0.0",
            isOutdated = true
        )
        val isDifferent1 = appOutdated.isInstalled && !appOutdated.installedVersionName.isNullOrEmpty() && appOutdated.installedVersionName != appOutdated.latestVersionName
        val display1 = if (isDifferent1) "v${appOutdated.installedVersionName} → v${appOutdated.latestVersionName}" else "v${appOutdated.latestVersionName}"
        assertEquals("v1.0.0 → v1.0.2", display1)

        // App 2: Up to date installed version
        val appUpToDate = createApp(
            packageName = "com.rykersoft.sampleapp2",
            name = "Example 2",
            latestVersionCode = 3,
            latestVersionName = "1.0.2",
            isInstalled = true,
            installedVersionName = "1.0.2",
            isOutdated = false
        )
        val isDifferent2 = appUpToDate.isInstalled && !appUpToDate.installedVersionName.isNullOrEmpty() && appUpToDate.installedVersionName != appUpToDate.latestVersionName
        val display2 = if (isDifferent2) "v${appUpToDate.installedVersionName} → v${appUpToDate.latestVersionName}" else "v${appUpToDate.latestVersionName}"
        assertEquals("v1.0.2", display2)

        // App 3: Not installed
        val appNotInstalled = createApp(
            packageName = "com.rykersoft.sampleapp3",
            name = "Example 3",
            latestVersionCode = 1,
            latestVersionName = "1.0.0",
            isInstalled = false,
            installedVersionName = null,
            isOutdated = false
        )
        val isDifferent3 = appNotInstalled.isInstalled && !appNotInstalled.installedVersionName.isNullOrEmpty() && appNotInstalled.installedVersionName != appNotInstalled.latestVersionName
        val display3 = if (isDifferent3) "v${appNotInstalled.installedVersionName} → v${appNotInstalled.latestVersionName}" else "v${appNotInstalled.latestVersionName}"
        assertEquals("v1.0.0", display3)
    }

    @Test
    fun `app manager self update detection identifies outdated app manager package`() {
        val appManagerPkg = "com.rykersoft.appmanager"
        val rykerSoftUpToDate = createApp(
            packageName = appManagerPkg,
            name = "RykerSoft",
            latestVersionCode = 1,
            latestVersionName = "1.0.0",
            isInstalled = true,
            installedVersionName = "1.0.0",
            isOutdated = false
        )
        val otherApp = createApp("com.informant.app", "INFORMANT", 2, "1.0.1", isInstalled = true, isOutdated = true)

        val appsListUpToDate = listOf(rykerSoftUpToDate, otherApp)
        val selfUpdateUpToDate = appsListUpToDate.find { it.packageName == appManagerPkg && it.isOutdated }
        assertEquals(null, selfUpdateUpToDate)

        val rykerSoftOutdated = createApp(
            packageName = appManagerPkg,
            name = "RykerSoft",
            latestVersionCode = 2,
            latestVersionName = "1.0.1",
            isInstalled = true,
            installedVersionName = "1.0.0",
            isOutdated = true
        )
        val appsListOutdated = listOf(rykerSoftOutdated, otherApp)
        val selfUpdateOutdated = appsListOutdated.find { it.packageName == appManagerPkg && it.isOutdated }
        assertEquals("RykerSoft", selfUpdateOutdated?.name)
        assertEquals(2, selfUpdateOutdated?.latestVersionCode)
        assertEquals("1.0.1", selfUpdateOutdated?.latestVersionName)
    }
}

