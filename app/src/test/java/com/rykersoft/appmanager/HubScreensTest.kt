package com.rykersoft.appmanager

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.rykersoft.appmanager.entitlements.AdminCredentialField
import com.rykersoft.appmanager.entitlements.AdminManagedApp
import com.rykersoft.appmanager.entitlements.AdminManagedUser
import com.rykersoft.appmanager.ui.AppDetailScreen
import com.rykersoft.appmanager.ui.AppDetailTab
import com.rykersoft.appmanager.ui.AppRow
import com.rykersoft.appmanager.ui.CatalogScreen
import com.rykersoft.appmanager.ui.MainUiState
import com.rykersoft.appmanager.ui.PlatformFilter
import com.rykersoft.appmanager.ui.ProAccessDialog
import com.rykersoft.appmanager.ui.SettingsScreen
import com.rykersoft.appmanager.ui.kit.graphPaper
import com.rykersoft.appmanager.ui.theme.MyApplicationTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Behaviour checks for the three screens, plus rendered captures of each. Captures go to
 * app/build/outputs/gallery; they show sample data and are not the published gallery.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class HubScreensTest {

    @get:Rule
    val rule = createComposeRule()

    private val gallery = File("build/outputs/gallery")

    @Before
    fun prepare() {
        check(gallery.exists() || gallery.mkdirs())
    }

    private val sampleApps = listOf(
        testApp("SuperThink.ing", packageName = "com.rykersoft.superthinking", version = "2.3.3", releasedAt = "2026-11-06", installed = true, installedVersion = "2.3.2", outdated = true, supportsPro = true,
            exeUrl = "https://github.com/o/r/releases/download/superthinking-v2.2.11/SuperThinking.exe", linuxUrl = "https://github.com/o/r/releases/download/superthinking-v2.2.11/SuperThinking.AppImage"),
        testApp("RykerSoft Hub", packageName = "com.rykersoft.appmanager", version = "1.5.0", releasedAt = "2026-11-10", installed = true),
        testApp("BetterTracking", packageName = "com.rykersoft.bettertracking", version = "1.15.0", releasedAt = "2026-11-10", installed = true, supportsPro = true, proOn = true),
        testApp("Earbound", packageName = "com.rykersoft.earbound", version = "0.7.0", releasedAt = "2026-11-07", isGame = true),
        testApp("WordPlay", packageName = "com.rykersoft.wordplaying", version = "1.3.58", releasedAt = "2026-10-26", isGame = true),
        testApp("Hyperscribe Desktop", packageName = "com.rykersoft.hyperscribedesktop", version = "2.4.4", releasedAt = "2026-10-07", apkUrl = "", supportsPro = true,
            exeUrl = "https://github.com/o/r/releases/download/hyperscribe-desktop-v2.4.4/Hyperscribe.exe")
    )

    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        MyApplicationTheme { Box(Modifier.fillMaxSize().graphPaper()) { content() } }
    }

    @Composable
    private fun Catalog(state: MainUiState, onInstall: (String) -> Unit = {}, onLaunch: (String) -> Unit = {}, onOpen: (String) -> Unit = {}, onPlatform: (PlatformFilter) -> Unit = {}) {
        CatalogScreen(
            state = state, search = "", onSearchChange = {}, onOpen = { onOpen(it.packageName) }, onInstall = { onInstall(it.packageName) },
            onLaunch = { onLaunch(it.packageName) }, onKind = {}, onPlatform = onPlatform, onSort = {}, onSync = {}, onSettings = {}, onCancelInstall = {}
        )
    }

    @Test
    fun rowActionsMatchTheAppsState() {
        val installs = mutableListOf<String>()
        val launches = mutableListOf<String>()
        rule.setContent { Frame { Catalog(MainUiState(apps = sampleApps), onInstall = { installs += it }, onLaunch = { launches += it }) } }

        rule.onNodeWithTag("catalog_status").assertTextContains("1 update · 6 apps")
        rule.onNodeWithTag("action_com.rykersoft.superthinking").assertTextContains("UPDATE").performClick()
        rule.onNodeWithTag("action_com.rykersoft.bettertracking").assertTextContains("OPEN").performClick()
        rule.onNodeWithTag("action_com.rykersoft.earbound").assertTextContains("INSTALL").performClick()

        assertEquals(listOf("com.rykersoft.superthinking", "com.rykersoft.earbound"), installs)
        assertEquals(listOf("com.rykersoft.bettertracking"), launches)
    }

    @Test
    fun systemMenuOffersAllFourChoicesAndDefaultsToAll() {
        var picked: PlatformFilter? = null
        rule.setContent { Frame { Catalog(MainUiState(apps = sampleApps), onPlatform = { picked = it }) } }

        rule.onNodeWithTag("platform_dropdown_chip").assertTextContains("ALL").performClick()
        rule.onNodeWithText("All systems").assertIsDisplayed()
        rule.onNodeWithText("Android only").assertIsDisplayed()
        rule.onNodeWithText("Windows only").assertIsDisplayed()
        rule.onNodeWithText("Linux only").performClick()

        assertEquals(PlatformFilter.LINUX, picked)
    }

    @Test
    fun aDownloadShowsItsRealProgressAndBlocksOtherInstalls() {
        rule.setContent {
            Frame {
                AppRow(sampleApps[3], PlatformFilter.ALL, downloadingPackage = "com.rykersoft.superthinking", downloadProgress = 42, onOpen = {}, onInstall = { error("blocked") }, onLaunch = {})
            }
        }
        rule.onNodeWithTag("action_com.rykersoft.earbound").performClick()
    }

    @Test
    fun detailShowsVersionsSharesTheApkLinkAndHasNoGuide() {
        var shared = ""
        rule.setContent {
            Frame {
                AppDetailScreen(
                    app = sampleApps[0], downloadingPackage = null, downloadProgress = 0, accountEmail = null, onBack = {}, onInstall = {}, onLaunch = {},
                    onCopyLink = { _, _ -> }, onShareLink = { _, url -> shared = url }, onOpenLink = {}
                )
            }
        }
        rule.onNodeWithTag("detail_version_com.rykersoft.superthinking").assertTextContains("2.3.2 → 2.3.3")
        rule.onNodeWithTag("share_detail_apk_com.rykersoft.superthinking").performClick()
        assertTrue(shared.endsWith("app.apk"))
        rule.onNodeWithText("USER GUIDE", ignoreCase = true).assertDoesNotExist()
        rule.onNodeWithText("GUIDE", ignoreCase = true).assertDoesNotExist()
        // The desktop builds are behind the Android version, and the page says so.
        assertEquals(2, rule.onAllNodesWithText("v2.2.11 · behind v2.3.3", substring = true, useUnmergedTree = true).fetchSemanticsNodes().size)
    }

    @Test
    fun settingsAppliesChangesAtOnceAndResetAsksFirst() {
        var notifications: Boolean? = null
        var resets = 0
        rule.setContent { Frame { Settings(MainUiState(apps = sampleApps, registryUrl = "https://example.invalid/registry.json"), onNotifications = { notifications = it }, onReset = { resets++ }) } }

        rule.onNodeWithText("SAVE SETTINGS", ignoreCase = true).assertDoesNotExist()
        rule.onNodeWithText("Check in the background").performClick()
        assertEquals(false, notifications)

        rule.onNodeWithTag("reset_catalog_button").performScrollTo().performClick()
        assertEquals(0, resets)
        rule.onNodeWithText("Reset the catalog?").assertIsDisplayed()
        rule.onNodeWithTag("confirm_reset_button").performClick()
        assertEquals(1, resets)
    }

    @Test
    fun adminKeyFieldsCanBeShownAndHidden() {
        rule.setContent { Frame { Settings(adminState()) } }
        // Collapsed until the app is opened.
        assertTrue(rule.onAllNodesWithContentDescription("Show Gemini API key (missing)").fetchSemanticsNodes().isEmpty())
        rule.onNodeWithTag("admin_app_com.rykersoft.superthinking").performScrollTo().performClick()
        assertFalse(rule.onAllNodesWithContentDescription("Show Gemini API key (missing)").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun adminAccountOpensToItsProSwitches() {
        val changes = mutableListOf<Triple<String, String, Boolean>>()
        rule.setContent { Frame { Settings(adminState(), onSetPro = { u, p, e -> changes += Triple(u, p, e) }) } }
        // Every account starts closed: the only SuperThink.ing row is the provider-keys one.
        assertEquals(1, rule.onAllNodesWithText("SuperThink.ing", useUnmergedTree = true).fetchSemanticsNodes().size)
        rule.onNodeWithTag("admin_user_uid-1").performScrollTo().performClick()
        rule.waitForIdle()
        rule.onAllNodesWithText("SuperThink.ing", useUnmergedTree = true)[0].performClick()
        assertEquals(listOf(Triple("uid-1", "com.rykersoft.superthinking", false)), changes)
        // Opening another account closes the first: one switch row (uid-2's) plus the keys row.
        rule.onNodeWithTag("admin_user_uid-2").performScrollTo().performClick()
        rule.waitForIdle()
        assertEquals(2, rule.onAllNodesWithText("SuperThink.ing", useUnmergedTree = true).fetchSemanticsNodes().size)
    }

    @Test
    fun proDialogExplainsPerAccountAccess() {
        rule.setContent { Frame { ProAccessDialog(appName = "SuperThink.ing", accountEmail = "person@example.com", onDismiss = {}) } }
        rule.onNodeWithTag("pro_access_info_dialog").assertExists()
        rule.onNodeWithText("switched on per account", substring = true).assertExists()
        rule.onNodeWithText("person@example.com").assertExists()
    }

    @Test
    fun captureGallery() {
        rule.setContent { Frame { Catalog(MainUiState(apps = sampleApps, lastSyncAt = 1L)) } }
        rule.onRoot().captureRoboImage(File(gallery, "01-catalog.png").path)
    }

    @Test
    fun captureCatalogDownloading() {
        rule.setContent { Frame { Catalog(MainUiState(apps = sampleApps, downloadingPackage = "com.rykersoft.superthinking", downloadProgress = 42, platform = PlatformFilter.WINDOWS)) } }
        rule.onRoot().captureRoboImage(File(gallery, "02-catalog-windows.png").path)
    }

    @Test
    @Config(qualifiers = "w411dp-h914dp-xxhdpi", fontScale = 1.3f, sdk = [36])
    fun captureCatalogLargeText() {
        rule.setContent { Frame { Catalog(MainUiState(apps = sampleApps)) } }
        rule.onRoot().captureRoboImage(File(gallery, "03-catalog-font-1.3.png").path)
    }

    @Test
    fun captureDetail() {
        rule.setContent {
            Frame {
                AppDetailScreen(
                    app = sampleApps[0], downloadingPackage = null, downloadProgress = 0, accountEmail = "person@example.com", onBack = {}, onInstall = {}, onLaunch = {},
                    onCopyLink = { _, _ -> }, onShareLink = { _, _ -> }, onOpenLink = {}, initialTab = AppDetailTab.UPDATES
                )
            }
        }
        rule.onRoot().captureRoboImage(File(gallery, "04-app-page.png").path)
    }

    @Test
    fun captureSettings() {
        rule.setContent { Frame { Settings(adminState()) } }
        rule.onRoot().captureRoboImage(File(gallery, "05-settings.png").path)
    }

    @Test
    @Config(qualifiers = "w411dp-h1800dp-xxhdpi", sdk = [36])
    fun captureSettingsAdminOpen() {
        rule.setContent { Frame { Settings(adminState()) } }
        rule.onNodeWithTag("admin_user_uid-1").performScrollTo().performClick()
        rule.onNodeWithTag("admin_app_com.rykersoft.superthinking").performScrollTo().performClick()
        rule.onNodeWithTag("admin_user_uid-1").performScrollTo()
        rule.onRoot().captureRoboImage(File(gallery, "06-settings-admin-open.png").path)
    }

    private fun adminState() = MainUiState(
        apps = sampleApps, registryUrl = "https://raw.githubusercontent.com/rykergrey/RykerSoft/main/registry.json",
        hubFirebaseConfigured = true, hubGoogleConfigured = true, hubSignedIn = true, hubAccountEmail = "person@example.com", hubHasGoogleProvider = true, hubAdmin = true,
        hubAdminUsers = listOf(
            AdminManagedUser("uid-1", "friend@example.com", entitlements = mapOf("com.rykersoft.superthinking" to true)),
            AdminManagedUser("uid-2", "tester@example.com"),
            AdminManagedUser("uid-3", "someone.with.a.long.address@example.com", entitlements = mapOf("com.rykersoft.superthinking" to true, "com.rykersoft.hyperscribe" to true))
        ),
        hubAdminApps = listOf(
            AdminManagedApp("com.rykersoft.superthinking", "SuperThink.ing", listOf(AdminCredentialField("geminiApiKey", "Gemini API key", "gemini"))),
            AdminManagedApp("com.rykersoft.hyperscribe", "Hyperscribe", listOf(AdminCredentialField("openaiApiKey", "OpenAI API key", "openai"), AdminCredentialField("deepgramApiKey", "Deepgram API key", "deepgram")), configuredFields = setOf("openaiApiKey")),
            AdminManagedApp("com.rykersoft.closecall", "Close Call")
        )
    )

    @Composable
    private fun Settings(state: MainUiState, onNotifications: (Boolean) -> Unit = {}, onReset: () -> Unit = {}, onSetPro: (String, String, Boolean) -> Unit = { _, _, _ -> }) {
        SettingsScreen(
            state = state, onBack = {}, onRegistryUrl = {}, onNotifications = onNotifications, onCheckNow = {}, onAddApp = { _, _, _, _, _, _ -> }, onResetCatalog = onReset,
            onGoogleSignIn = {}, onLinkGoogle = {}, onLegacySignIn = { _, _ -> }, onPasswordReset = {}, onSignOut = {}, onAdminRefresh = {}, onAdminSetPro = onSetPro, onAdminSetKeys = { _, _ -> }
        )
    }
}
