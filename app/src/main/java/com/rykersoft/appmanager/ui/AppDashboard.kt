package com.rykersoft.appmanager.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.core.util.Consumer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rykersoft.appmanager.ui.kit.graphPaper
import com.rykersoft.appmanager.ui.kit.hardShadow
import com.rykersoft.appmanager.ui.theme.Hub
import com.rykersoft.appmanager.util.ApkManager
import kotlinx.coroutines.launch

private class HubSnack(override val message: String, val isError: Boolean) : SnackbarVisuals {
    override val actionLabel: String? = null
    override val withDismissAction: Boolean = false
    override val duration: SnackbarDuration = if (isError) SnackbarDuration.Long else SnackbarDuration.Short
}

/**
 * The hub's shell: it owns which screen is showing, runs the install plumbing, and hands plain
 * state and callbacks to the three screens (catalog, app page, settings).
 */
@Composable
fun AppDashboard(
    modifier: Modifier = Modifier,
    viewModel: AppViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    // Where the user is. Search and the open page survive rotation; closing a page never clears
    // the filters, which live in the view model and its preferences.
    var search by rememberSaveable { mutableStateOf("") }
    var detailPackage by rememberSaveable { mutableStateOf<String?>(null) }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    fun say(message: String, isError: Boolean = false) {
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(HubSnack(message, isError))
        }
    }

    fun openLink(url: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) {
            say("No app on this phone can open that link.", isError = true)
        }
    }

    fun shareLink(label: String, url: String) {
        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_SUBJECT, label).putExtra(Intent.EXTRA_TEXT, url)
        try {
            context.startActivity(Intent.createChooser(send, "Send the $label link").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) {
            say("Nothing on this phone can share a link.", isError = true)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshLocalInstallations()
                viewModel.checkPendingInstall()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Install-result intents from PackageInstaller status callbacks.
    val activity = context as? ComponentActivity
    DisposableEffect(activity) {
        if (activity == null) {
            onDispose { }
        } else {
            val listener = Consumer<Intent> { intent -> viewModel.consumeInstallIntent(intent) }
            activity.addOnNewIntentListener(listener)
            viewModel.consumeInstallIntent(activity.intent)
            onDispose { activity.removeOnNewIntentListener(listener) }
        }
    }

    // An install finishing never moves the user: the completion signal is consumed, not followed.
    LaunchedEffect(state.postInstallOpenPackage) {
        if (state.postInstallOpenPackage != null) viewModel.clearPostInstallOpen()
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { say(it, isError = true); viewModel.clearError() }
    }
    LaunchedEffect(state.infoMessage) {
        state.infoMessage?.let { say(it); viewModel.clearInfo() }
    }
    LaunchedEffect(showSettings, state.hubAdmin) {
        if (showSettings && state.hubAdmin) viewModel.refreshAdminUsers(forceRefresh = true)
    }

    val detailApp = detailPackage?.let { pkg -> state.apps.find { it.packageName == pkg } }
    BackHandler(enabled = detailApp != null || showSettings) {
        if (detailApp != null) detailPackage = null else showSettings = false
    }

    Box(modifier = modifier.fillMaxSize().graphPaper()) {
        when {
            detailApp != null -> AppDetailScreen(
                app = detailApp,
                downloadingPackage = state.downloadingPackage,
                downloadProgress = state.downloadProgress,
                accountEmail = state.hubAccountEmail,
                onBack = { detailPackage = null },
                onInstall = { viewModel.downloadAndInstall(detailApp) },
                onLaunch = { if (!ApkManager.launchApp(context, detailApp.packageName)) say("${detailApp.name} has no screen to open.", isError = true) },
                onCopyLink = { label, url -> clipboard.setText(AnnotatedString(url)); say("Copied the $label link") },
                onShareLink = ::shareLink,
                onOpenLink = ::openLink
            )
            showSettings -> SettingsScreen(
                state = state,
                onBack = { showSettings = false },
                onRegistryUrl = viewModel::updateRegistryUrl,
                onNotifications = viewModel::updateNotificationsEnabled,
                onCheckNow = viewModel::syncWithRegistry,
                onAddApp = { name, pkg, versionName, versionCode, apkUrl, isGame ->
                    viewModel.addManualApp(name, pkg, versionName, versionCode, apkUrl, "", "", isGame)
                },
                onResetCatalog = viewModel::resetCatalog,
                onGoogleSignIn = { viewModel.hubGoogleSignIn(context) },
                onLinkGoogle = { viewModel.hubGoogleSignIn(context, linkLegacyAccount = true) },
                onLegacySignIn = viewModel::hubLegacySignIn,
                onPasswordReset = viewModel::hubSendPasswordReset,
                onSignOut = { viewModel.hubSignOut(context) },
                onAdminRefresh = { viewModel.refreshAdminUsers(forceRefresh = true) },
                onAdminSetPro = viewModel::setAdminUserProAccess,
                onAdminSetKeys = viewModel::setAdminProviderKeys
            )
            else -> CatalogScreen(
                state = state,
                search = search,
                onSearchChange = { search = it },
                onOpen = { detailPackage = it.packageName },
                onInstall = viewModel::downloadAndInstall,
                onLaunch = { app -> if (!ApkManager.launchApp(context, app.packageName)) say("${app.name} has no screen to open.", isError = true) },
                onKind = viewModel::setFilter,
                onPlatform = viewModel::setPlatform,
                onSort = viewModel::setSortOption,
                onSync = viewModel::syncWithRegistry,
                onSettings = { showSettings = true },
                onCancelInstall = viewModel::cancelStuckInstall
            )
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().imePadding().padding(bottom = Hub.D.touch + Hub.D.gapLg)
        ) { data ->
            val isError = (data.visuals as? HubSnack)?.isError == true
            Snackbar(
                containerColor = Hub.C.surface,
                contentColor = Hub.C.text,
                shape = Hub.S.control,
                modifier = Modifier
                    .padding(horizontal = Hub.D.gutter)
                    .hardShadow()
                    .border(Hub.D.border, if (isError) Hub.C.danger else Hub.C.border, Hub.S.control)
                    .testTag(if (isError) "snackbar_error" else "snackbar_info"),
                content = { Text(data.visuals.message, style = Hub.T.bodySm, color = if (isError) Hub.C.danger else Hub.C.text) }
            )
        }
    }
}
