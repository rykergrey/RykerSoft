package com.rykersoft.appmanager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.rykersoft.appmanager.BuildConfig
import com.rykersoft.appmanager.entitlements.AdminManagedApp
import com.rykersoft.appmanager.entitlements.AiUnlockPackages
import com.rykersoft.appmanager.ui.kit.ButtonKind
import com.rykersoft.appmanager.ui.kit.Group
import com.rykersoft.appmanager.ui.kit.GroupDivider
import com.rykersoft.appmanager.ui.kit.GroupRow
import com.rykersoft.appmanager.ui.kit.HubButton
import com.rykersoft.appmanager.ui.kit.HubDialog
import com.rykersoft.appmanager.ui.kit.HubIconButton
import com.rykersoft.appmanager.ui.kit.HubSwitch
import com.rykersoft.appmanager.ui.kit.HubTextField
import com.rykersoft.appmanager.ui.kit.SectionLabel
import com.rykersoft.appmanager.ui.kit.SwitchRow
import com.rykersoft.appmanager.ui.kit.Tag
import com.rykersoft.appmanager.ui.kit.Tone
import com.rykersoft.appmanager.ui.theme.Hub

/**
 * Settings is a page of row groups, not a form. Every change applies when you make it, so there
 * is no Save and no Cancel. Resetting the catalog comes last, apart, in red, and asks first.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    state: MainUiState,
    onBack: () -> Unit,
    onRegistryUrl: (String) -> Unit,
    onNotifications: (Boolean) -> Unit,
    onCheckNow: () -> Unit,
    onAddApp: (name: String, packageName: String, versionName: String, versionCode: Int, apkUrl: String, isGame: Boolean) -> Unit,
    onResetCatalog: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onLinkGoogle: () -> Unit,
    onLegacySignIn: (email: String, password: String) -> Unit,
    onPasswordReset: (email: String) -> Unit,
    onSignOut: () -> Unit,
    onAdminRefresh: () -> Unit,
    onAdminSetPro: (uid: String, packageId: String, enabled: Boolean) -> Unit,
    onAdminSetKeys: (packageId: String, values: Map<String, String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var showLegacy by rememberSaveable { mutableStateOf(false) }
    var legacyEmail by rememberSaveable { mutableStateOf("") }
    var legacyPassword by remember { mutableStateOf("") }
    var url by rememberSaveable(state.registryUrl) { mutableStateOf(state.registryUrl) }
    var showAdd by rememberSaveable { mutableStateOf(false) }
    var confirmReset by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Hub.D.gutter - Hub.D.gapSm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HubIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Back to the catalog", onBack, testTag = "settings_back_button")
            Text("Settings", style = Hub.T.title, color = Hub.C.text, modifier = Modifier.padding(start = Hub.D.gap).semantics { heading() })
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = Hub.D.gutter)
                .padding(bottom = Hub.D.gapLg)
                .testTag("settings_dialog_content"),
            verticalArrangement = Arrangement.spacedBy(Hub.D.gap)
        ) {
            // ---- Account
            SectionLabel("RykerSoft account", modifier = Modifier.padding(top = Hub.D.gap))
            Group {
                when {
                    !state.hubFirebaseConfigured -> GroupRow(
                        title = "Accounts are not available in this build",
                        subtitle = "This copy was built without the RykerSoft account configuration."
                    )
                    state.hubSignedIn -> {
                        GroupRow(
                            title = state.hubAccountEmail ?: "Signed in",
                            subtitle = when {
                                state.hubHasGoogleProvider && state.hubHasPasswordProvider -> "Google is linked. The old password still works."
                                state.hubHasGoogleProvider -> "Signed in with Google. Use the same account inside each app for Pro."
                                else -> "Password account. Link Google to keep this account and its Pro access."
                            }
                        ) { Tag("Signed in", Tone.Ok) }
                        if (!state.hubHasGoogleProvider) {
                            GroupDivider()
                            GroupRow(title = "Link Google to this account", subtitle = "Keeps your account, data and Pro access.") {
                                HubButton("Link", onLinkGoogle, kind = ButtonKind.Primary, enabled = !state.hubBusy && state.hubGoogleConfigured)
                            }
                        }
                        GroupDivider()
                        GroupRow(title = "Sign out") { HubButton("Sign out", onSignOut, enabled = !state.hubBusy) }
                    }
                    else -> {
                        GroupRow(
                            title = "Sign in with Google",
                            subtitle = if (state.hubGoogleConfigured) "One account carries Pro access into every RykerSoft app." else "Google sign-in is missing from this build."
                        ) {
                            HubButton("Sign in", onGoogleSignIn, kind = ButtonKind.Primary, enabled = !state.hubBusy && state.hubGoogleConfigured, testTag = "google_sign_in_button")
                        }
                        GroupDivider()
                        GroupRow(
                            title = "I have an older password account",
                            subtitle = "Sign in with the old password, then link Google to keep it.",
                            onClick = { showLegacy = !showLegacy }
                        ) { Tag(if (showLegacy) "Hide" else "Show") }
                        if (showLegacy) {
                            Column(modifier = Modifier.padding(horizontal = Hub.D.gap + Hub.D.gapSm).padding(bottom = Hub.D.gap + Hub.D.gapSm), verticalArrangement = Arrangement.spacedBy(Hub.D.gap)) {
                                HubTextField(legacyEmail, { legacyEmail = it }, "Account email", keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                                HubTextField(legacyPassword, { legacyPassword = it }, "Password", secret = true)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(Hub.D.gap)) {
                                    HubButton("Sign in", { onLegacySignIn(legacyEmail, legacyPassword) }, enabled = !state.hubBusy)
                                    HubButton("Email a reset link", { onPasswordReset(legacyEmail) }, enabled = !state.hubBusy)
                                }
                            }
                        }
                    }
                }
            }

            // ---- Updates
            SectionLabel("Updates", modifier = Modifier.padding(top = Hub.D.gap))
            Group {
                SwitchRow(
                    title = "Check in the background",
                    subtitle = "Notifies you when an app you have installed gets a new version.",
                    checked = state.notificationsEnabled,
                    onCheckedChange = onNotifications
                )
                GroupDivider()
                GroupRow(
                    title = "Check now",
                    subtitle = if (state.isSyncing) "Checking…" else if (state.lastSyncAt != null) "Checked this session." else "Not checked yet this session.",
                    icon = Icons.Default.Refresh
                ) { HubButton("Check", onCheckNow, enabled = !state.isSyncing) }
            }

            // ---- Catalog
            SectionLabel("Catalog", modifier = Modifier.padding(top = Hub.D.gap))
            Group {
                Column(modifier = Modifier.padding(Hub.D.gap + Hub.D.gapSm), verticalArrangement = Arrangement.spacedBy(Hub.D.gap)) {
                    HubTextField(url, { url = it }, "Catalog address", keyboardType = KeyboardType.Uri, onDone = { if (url.isNotBlank()) onRegistryUrl(url) }, testTag = "registry_url_field")
                    if (url.trim() != state.registryUrl) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(Hub.D.gap)) {
                            HubButton("Use this address", { onRegistryUrl(url) }, kind = ButtonKind.Primary, enabled = url.isNotBlank())
                            HubButton("Revert", { url = state.registryUrl })
                        }
                    }
                }
                GroupDivider()
                GroupRow(title = "Add an app by hand", subtitle = "List an APK that is not in the catalog. It is removed the next time the catalog refreshes.", icon = Icons.Default.Add) {
                    HubButton("Add", { showAdd = true })
                }
            }

            // ---- Administration
            if (state.hubAdmin) {
                AdminSections(state, onAdminRefresh, onAdminSetPro, onAdminSetKeys)
            }

            // ---- About
            SectionLabel("About", modifier = Modifier.padding(top = Hub.D.gap))
            Group {
                GroupRow(title = "RykerSoft Hub", subtitle = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            }

            // ---- Destructive: last, apart, in red.
            SectionLabel("Reset", modifier = Modifier.padding(top = Hub.D.gapLg))
            Group {
                GroupRow(
                    title = "Reset the catalog",
                    subtitle = "Clears the saved list, including apps you added by hand, and downloads it again. Installed apps are not touched.",
                    icon = Icons.Default.DeleteForever,
                    danger = true
                ) { HubButton("Reset", { confirmReset = true }, kind = ButtonKind.Danger, testTag = "reset_catalog_button") }
            }
        }
    }

    if (showAdd) AddAppDialog(onDismiss = { showAdd = false }, onConfirm = { n, p, v, c, u, g -> onAddApp(n, p, v, c, u, g); showAdd = false })

    if (confirmReset) {
        HubDialog(
            title = "Reset the catalog?",
            onDismiss = { confirmReset = false },
            actions = {
                HubButton("Keep it", { confirmReset = false })
                HubButton("Reset", { confirmReset = false; onResetCatalog() }, kind = ButtonKind.Danger, testTag = "confirm_reset_button")
            }
        ) {
            Text("The saved list is cleared and downloaded again. Apps you added by hand are removed from the list. Nothing is uninstalled.", style = Hub.T.body, color = Hub.C.dim)
        }
    }
}

@Composable
private fun AdminSections(
    state: MainUiState,
    onRefresh: () -> Unit,
    onSetPro: (uid: String, packageId: String, enabled: Boolean) -> Unit,
    onSetKeys: (packageId: String, values: Map<String, String>) -> Unit
) {
    var search by rememberSaveable { mutableStateOf("") }
    var openUser by rememberSaveable { mutableStateOf<String?>(null) }
    var keyDrafts by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val proApps = state.hubAdminApps.ifEmpty { AiUnlockPackages.ORDERED.map { AdminManagedApp(it, AiUnlockPackages.displayName(it)) } }
    val q = search.trim().lowercase()
    val users = remember(state.hubAdminUsers, q) {
        if (q.isBlank()) state.hubAdminUsers else state.hubAdminUsers.filter { it.uid.lowercase().contains(q) || it.email.lowercase().contains(q) }
    }

    Row(modifier = Modifier.fillMaxWidth().padding(top = Hub.D.gap), verticalAlignment = Alignment.CenterVertically) {
        SectionLabel("Admin: Pro access", count = state.hubAdminUsers.size, modifier = Modifier.weight(1f))
        HubButton("Refresh", onRefresh, enabled = !state.hubAdminBusy)
    }
    HubTextField(search, { search = it }, "Find an account by email or UID")
    Group {
        if (users.isEmpty()) {
            GroupRow(title = when {
                state.hubAdminBusy -> "Loading accounts…"
                state.hubAdminUsers.isEmpty() -> "No RykerSoft accounts yet"
                else -> "No account matches"
            })
        }
        Column(modifier = Modifier.heightIn(max = Hub.D.listMax * 2).verticalScroll(rememberScrollState())) {
            users.forEachIndexed { index, user ->
                if (index > 0) GroupDivider()
                val granted = proApps.count { user.entitlements[it.packageId] == true }
                val open = openUser == user.uid
                GroupRow(
                    title = user.email.ifBlank { user.uid },
                    subtitle = "UID ${user.uid}",
                    onClick = { openUser = if (open) null else user.uid }
                ) { Tag(if (granted == 0) "No Pro" else "$granted Pro", if (granted == 0) Tone.Plain else Tone.Ok) }
                if (open) {
                    proApps.forEach { app ->
                        val enabled = user.entitlements[app.packageId] == true
                        GroupRow(title = app.displayName, modifier = Modifier.padding(start = Hub.D.gapLg)) {
                            HubSwitch(checked = enabled, enabled = !state.hubAdminBusy, onCheckedChange = { onSetPro(user.uid, app.packageId, it) })
                        }
                    }
                }
            }
        }
    }

    SectionLabel("Admin: provider keys", modifier = Modifier.padding(top = Hub.D.gap))
    Text("Fields come from each app's capability record. A blank field leaves the saved key as it is.", style = Hub.T.bodySm, color = Hub.C.dim)
    state.hubAdminApps.forEach { app ->
        Group {
            GroupRow(title = app.displayName, subtitle = app.packageId) {
                if (app.credentialFields.isEmpty()) Tag("No keys needed", Tone.Ok)
            }
            if (app.credentialFields.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = Hub.D.gap + Hub.D.gapSm).padding(bottom = Hub.D.gap + Hub.D.gapSm), verticalArrangement = Arrangement.spacedBy(Hub.D.gap)) {
                    app.credentialFields.forEach { credential ->
                        val key = "${app.packageId}:${credential.field}"
                        HubTextField(
                            value = keyDrafts[key].orEmpty(),
                            onValueChange = { keyDrafts = keyDrafts + (key to it) },
                            label = "${credential.label} (${if (credential.field in app.configuredFields) "saved" else "missing"})",
                            secret = true
                        )
                    }
                    HubButton(
                        "Save keys",
                        {
                            onSetKeys(app.packageId, app.credentialFields.associate { it.field to keyDrafts["${app.packageId}:${it.field}"].orEmpty() })
                            keyDrafts = keyDrafts.filterKeys { !it.startsWith("${app.packageId}:") }
                        },
                        enabled = !state.hubAdminBusy
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddAppDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, packageName: String, versionName: String, versionCode: Int, apkUrl: String, isGame: Boolean) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var pkg by rememberSaveable { mutableStateOf("") }
    var versionName by rememberSaveable { mutableStateOf("1.0.0") }
    var versionCode by rememberSaveable { mutableStateOf("1") }
    var apkUrl by rememberSaveable { mutableStateOf("") }
    var isGame by rememberSaveable { mutableStateOf(false) }
    var tried by rememberSaveable { mutableStateOf(false) }
    val code = versionCode.toIntOrNull()
    val valid = name.isNotBlank() && pkg.contains('.') && apkUrl.startsWith("https://") && code != null

    HubDialog(
        title = "Add an app by hand",
        onDismiss = onDismiss,
        actions = {
            HubButton("Cancel", onDismiss)
            HubButton("Add", { tried = true; if (valid) onConfirm(name, pkg, versionName, code ?: 1, apkUrl, isGame) }, kind = ButtonKind.Primary)
        }
    ) {
        Column(modifier = Modifier.heightIn(max = Hub.D.listMax * 1.5f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(Hub.D.gap)) {
            HubTextField(name, { name = it }, "Name", imeAction = ImeAction.Next, isError = tried && name.isBlank())
            HubTextField(pkg, { pkg = it }, "Package name, like com.example.app", imeAction = ImeAction.Next, isError = tried && !pkg.contains('.'))
            HubTextField(apkUrl, { apkUrl = it }, "APK address (https://)", keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next, isError = tried && !apkUrl.startsWith("https://"))
            HubTextField(versionName, { versionName = it }, "Version name", imeAction = ImeAction.Next)
            HubTextField(versionCode, { versionCode = it }, "Version code (a whole number)", keyboardType = KeyboardType.Number, isError = tried && code == null)
            SwitchRow("This is a game", "Lists it under Games.", isGame, { isGame = it })
            if (tried && !valid) Text("Fill in the marked fields to add the app.", style = Hub.T.bodySm, color = Hub.C.danger)
        }
    }
}
