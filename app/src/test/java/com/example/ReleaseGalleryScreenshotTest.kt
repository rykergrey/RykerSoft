package com.rykersoft.appmanager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.rykersoft.appmanager.ui.AppDetailDialog
import com.rykersoft.appmanager.ui.AppDetailTab
import com.rykersoft.appmanager.ui.AppItemCard
import com.rykersoft.appmanager.ui.AppUiItem
import com.rykersoft.appmanager.ui.RykerSoftTitleHeader
import com.rykersoft.appmanager.ui.SettingsDialog
import com.rykersoft.appmanager.ui.TagChip
import com.rykersoft.appmanager.ui.UnlockProDialog
import com.rykersoft.appmanager.ui.theme.MyApplicationTheme
import com.rykersoft.appmanager.ui.theme.NeoBg
import com.rykersoft.appmanager.ui.theme.NeoCyan
import com.rykersoft.appmanager.ui.theme.NeoMutedBg
import com.rykersoft.appmanager.ui.theme.NeoText
import com.rykersoft.appmanager.ui.theme.NeoYellow
import java.io.File
import org.junit.Before
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class ReleaseGalleryScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val galleryDir = File("../screenshots/rykersoft")

    @Before
    fun prepareGalleryDirectory() {
        check(galleryDir.exists() || galleryDir.mkdirs()) {
            "Could not create ${galleryDir.absolutePath}"
        }
    }

    @Test
    fun apkShareButtonInvokesCallback() {
        val app = sampleApp(
            packageName = "com.rykersoft.informant",
            name = "INFORMANT",
            versionName = "1.2.6",
            installed = true,
            outdated = false,
            status = "Installed",
            supportsPro = false
        )
        var shareClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                AppItemCard(
                    app = app,
                    onOpenDetail = {},
                    onLongClick = {},
                    onActionClick = {},
                    onLaunchClick = {},
                    downloadingPackage = null,
                    downloadProgress = 0,
                    onShareClick = { shareClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("share_apk_${app.packageName}").performClick()

        assertTrue(shareClicked)
    }

    @Test
    fun detailShareButtonInvokesCallback() {
        val app = sampleApp(
            packageName = "com.rykersoft.freeballing",
            name = "FreeBall.ing",
            versionName = "1.0.14",
            installed = true,
            outdated = false,
            status = "Installed",
            supportsPro = false
        )
        var shareClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                AppDetailDialog(
                    app = app,
                    onDismiss = {},
                    onActionClick = {},
                    onLaunchClick = {},
                    downloadingPackage = null,
                    downloadProgress = 0,
                    onShareClick = { shareClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("share_detail_apk_${app.packageName}").performClick()

        assertTrue(shareClicked)
    }

    @Test
    fun detailKeepsSelectedTabWhenDownloadStarts() {
        val app = sampleApp(
            packageName = "com.rykersoft.superthinking",
            name = "SuperThink.ing",
            versionName = "2.0.119",
            installed = true,
            outdated = true,
            status = "Update Available",
            supportsPro = false
        )
        val downloadingPackage = mutableStateOf<String?>(null)

        composeTestRule.setContent {
            MyApplicationTheme {
                AppDetailDialog(
                    app = app,
                    initialTab = AppDetailTab.UPDATES,
                    onDismiss = {},
                    onActionClick = {},
                    onLaunchClick = {},
                    downloadingPackage = downloadingPackage.value,
                    downloadProgress = 0
                )
            }
        }

        composeTestRule.onNodeWithText("USER GUIDE").performClick()
        composeTestRule.onNodeWithText("SUPERTHINK.ING USER GUIDE").assertExists()

        composeTestRule.runOnIdle { downloadingPackage.value = app.packageName }

        composeTestRule.onNodeWithText("SUPERTHINK.ING USER GUIDE").assertExists()
    }

    @Test
    fun cardShowsWindowsAvailabilityWithoutProgrammingLabels() {
        val app = sampleApp(
            packageName = "com.rykersoft.freeballing",
            name = "FreeBall.ing",
            versionName = "1.0.14",
            installed = true,
            outdated = false,
            status = "Installed",
            supportsPro = false
        ).copy(windowsAvailable = true)

        composeTestRule.setContent {
            MyApplicationTheme {
                AppItemCard(
                    app = app,
                    onOpenDetail = {},
                    onLongClick = {},
                    onActionClick = {},
                    onLaunchClick = {},
                    downloadingPackage = null,
                    downloadProgress = 0
                )
            }
        }

        composeTestRule.onNodeWithText("WINDOWS").assertExists()
        composeTestRule.onNodeWithText("C++ / Engine").assertDoesNotExist()
        composeTestRule.onNodeWithText("Kotlin / App").assertDoesNotExist()
    }

    @Test
    fun captureDashboardGallery() {
        val updateReady = sampleApp(
            packageName = "com.rykersoft.informant",
            name = "INFORMANT",
            versionName = "1.2.6",
            installed = true,
            outdated = true,
            status = "Update Available",
            supportsPro = true
        )
        val newRelease = sampleApp(
            packageName = "com.rykersoft.rush",
            name = "RUSH",
            versionName = "1.0.2",
            installed = false,
            outdated = false,
            status = "Not Installed",
            supportsPro = false
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                Box(
                    modifier = Modifier
                        .size(width = 412.dp, height = 892.dp)
                        .background(NeoBg)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        RykerSoftTitleHeader(onOpenSettings = {})
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TagChip(text = "ANDROID", bgColor = NeoMutedBg, textColor = NeoText)
                            TagChip(text = "ALL (8)", bgColor = NeoYellow, textColor = NeoBg)
                            TagChip(text = "UPDATES (1)", bgColor = NeoCyan, textColor = NeoBg)
                        }
                        Text(
                            text = "LATEST RYKERSOFT RELEASES",
                            color = NeoText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        AppItemCard(
                            app = updateReady,
                            onOpenDetail = {},
                            onLongClick = {},
                            onActionClick = {},
                            onLaunchClick = {},
                            downloadingPackage = null,
                            downloadProgress = 0
                        )
                        AppItemCard(
                            app = newRelease,
                            onOpenDetail = {},
                            onLongClick = {},
                            onActionClick = {},
                            onLaunchClick = {},
                            downloadingPackage = null,
                            downloadProgress = 0
                        )
                    }
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(
            filePath = "../screenshots/rykersoft/01-dashboard.png"
        )
    }

    @Test
    fun captureAppDetailGallery() {
        composeTestRule.setContent {
            MyApplicationTheme {
                AppDetailDialog(
                    app = sampleApp(
                        packageName = "com.rykersoft.appmanager",
                        name = "RykerSoft",
                        versionName = "1.2.9",
                        installed = true,
                        outdated = false,
                        status = "Installed",
                        supportsPro = false
                    ).copy(
                        screenshots = listOf(
                            "android.resource://com.rykersoft.appmanager/drawable/rykersoft_logo"
                        )
                    ),
                    initialTab = AppDetailTab.USER_GUIDE,
                    onDismiss = {},
                    onActionClick = {},
                    onLaunchClick = {},
                    downloadingPackage = null,
                    downloadProgress = 0
                )
            }
        }

        composeTestRule.onNode(isDialog()).captureRoboImage(
            filePath = "../screenshots/rykersoft/02-documentation.png"
        )
    }

    @Test
    fun captureSettingsGallery() {
        composeTestRule.setContent {
            MyApplicationTheme {
                SettingsDialog(
                    currentUrl = "https://raw.githubusercontent.com/rykergrey/RykerSoft/main/registry.json",
                    notificationsEnabled = true,
                    hubFirebaseConfigured = true,
                    hubGoogleConfigured = true,
                    onDismiss = {},
                    onSave = { _, _, _ -> },
                    onLoadSamples = {},
                    onAddAppClick = {},
                    onHubSignOut = {}
                )
            }
        }

        composeTestRule.onNode(hasTestTag("settings_dialog_content")).captureRoboImage(
            filePath = "../screenshots/rykersoft/03-settings.png"
        )
    }

    @Test
    fun remainingSecretFieldsHaveAccessibleVisibilityControls() {
        composeTestRule.setContent {
            MyApplicationTheme {
                SettingsDialog(
                    currentUrl = "https://raw.githubusercontent.com/rykergrey/RykerSoft/main/registry.json",
                    notificationsEnabled = true,
                    hubFirebaseConfigured = true,
                    hubGoogleConfigured = true,
                    onDismiss = {},
                    onSave = { _, _, _ -> },
                    onLoadSamples = {},
                    onAddAppClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("MIGRATE AN EXISTING PASSWORD ACCOUNT").performClick()
        composeTestRule.onNodeWithContentDescription("Show Legacy password").assertExists().performClick()
        composeTestRule.onNodeWithContentDescription("Hide Legacy password").assertExists()
        composeTestRule.onNodeWithContentDescription("Show GitHub token (PAT for private repos)").assertExists()
    }

    @Test
    fun unlockCodeStartsHiddenAndCanBeRevealed() {
        composeTestRule.setContent {
            MyApplicationTheme {
                UnlockProDialog(
                    appName = "INFORMANT",
                    busy = false,
                    onDismiss = {},
                    onUnlock = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Show Unlock code").assertExists().performClick()
        composeTestRule.onNodeWithContentDescription("Hide Unlock code").assertExists()
    }

    private fun sampleApp(
        packageName: String,
        name: String,
        versionName: String,
        installed: Boolean,
        outdated: Boolean,
        status: String,
        supportsPro: Boolean
    ) = AppUiItem(
        packageName = packageName,
        name = name,
        description = "$name is part of the RykerSoft app collection.",
        summaryDescription = "Install, update, and read complete documentation from the RykerSoft hub.",
        latestVersionCode = 16,
        latestVersionName = versionName,
        apkUrl = "https://github.com/rykergrey/RykerSoft/releases/download/v1.2.9/app-release.apk",
        icon = "android",
        changelog = "- Safer signed APK validation\n- Clear installation guidance\n- Updated documentation",
        screenshots = emptyList(),
        isGame = false,
        isInstalled = installed,
        installedVersionName = if (installed && outdated) "1.2.5" else if (installed) versionName else null,
        installedVersionCode = if (installed) 13L else null,
        isOutdated = outdated,
        statusText = status,
        userGuide = "# $name User Guide\n\n## Table of Contents\n\n- [1. Overview](#1-overview)\n\n## 1. Overview\n\nManage this app from RykerSoft.",
        updatesHistory = "## v$versionName\n\n- Safer installs and clearer status messages",
        specs = "## Platform\n\n- Android 7.0+\n- Signed release APK",
        supportsAiUnlock = supportsPro,
        aiUnlocked = false
    )
}
