package com.rykersoft.appmanager.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rykersoft.appmanager.data.AppDatabase
import com.rykersoft.appmanager.data.AppRepository
import com.rykersoft.appmanager.data.ManagedApp
import com.rykersoft.appmanager.entitlements.AdminManagedUser
import com.rykersoft.appmanager.entitlements.AdminManagedApp
import com.rykersoft.appmanager.entitlements.AiUnlockPackages
import com.rykersoft.appmanager.entitlements.EntitlementRepository
import com.rykersoft.appmanager.entitlements.GoogleSignInManager
import com.rykersoft.appmanager.entitlements.RykerSoftFirebase
import android.content.Intent
import com.rykersoft.appmanager.install.InstallSessionTracker
import com.rykersoft.appmanager.install.InstallStatusReceiver
import com.rykersoft.appmanager.util.ApkManager
import com.rykersoft.appmanager.util.DownloadProgress
import com.rykersoft.appmanager.util.InstalledAppInfo
import com.rykersoft.appmanager.util.SchedulerHelper
import com.rykersoft.appmanager.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AppUiItem(
    val packageName: String,
    val name: String,
    val description: String,
    val summaryDescription: String,
    val latestVersionCode: Int,
    val latestVersionName: String,
    val apkUrl: String,
    val exeUrl: String = "",
    val linuxUrl: String = "",
    val windowsAvailable: Boolean = false,
    val icon: String,
    val changelog: String,
    val screenshots: List<String>,
    val isGame: Boolean,
    val isInstalled: Boolean,
    val installedVersionName: String?,
    val installedVersionCode: Long?,
    val isOutdated: Boolean,
    val statusText: String,
    val updatesHistory: String = "",
    val specs: String = "",
    /** One sentence for lists. */
    val tagline: String = "",
    val features: List<String> = emptyList(),
    /** What Pro adds, in the catalog's words. Blank means the whole app is free. */
    val proNote: String = "",
    val iconUrl: String = "",
    /** ISO date (yyyy-MM-dd) of the latest release, or blank when the catalog does not say. */
    val releasedAt: String = "",
    val linuxDebUrl: String = "",
    val linuxPacmanUrl: String = "",
    /** True when this package supports pro access and the signed-in hub account is entitled to it. */
    val supportsAiUnlock: Boolean = false,
    val aiUnlocked: Boolean = false
) {
    val hasAndroid: Boolean get() = apkUrl.isNotBlank()
    val hasWindows: Boolean get() = exeUrl.isNotBlank()
    val hasLinux: Boolean get() = linuxUrl.isNotBlank() || linuxDebUrl.isNotBlank() || linuxPacmanUrl.isNotBlank()

    val isDesktopOnly: Boolean
        get() = !hasAndroid && (hasWindows || hasLinux)

    /** Every desktop file the catalog lists for this app, in display order. */
    val desktopBuilds: List<DesktopBuild>
        get() = listOfNotNull(
            exeUrl.takeIf { it.isNotBlank() }?.let { DesktopBuild(PlatformFilter.WINDOWS, if (it.endsWith(".zip", true)) "ZIP" else "EXE", it) },
            linuxUrl.takeIf { it.isNotBlank() }?.let { DesktopBuild(PlatformFilter.LINUX, if (it.contains("AppImage", true)) "AppImage" else "tar.gz", it) },
            linuxDebUrl.takeIf { it.isNotBlank() }?.let { DesktopBuild(PlatformFilter.LINUX, ".deb", it) },
            linuxPacmanUrl.takeIf { it.isNotBlank() }?.let { DesktopBuild(PlatformFilter.LINUX, "Arch package", it) }
        )

    fun isOn(platform: PlatformFilter): Boolean = when (platform) {
        PlatformFilter.ALL -> true
        PlatformFilter.ANDROID -> hasAndroid
        PlatformFilter.WINDOWS -> hasWindows
        PlatformFilter.LINUX -> hasLinux
    }

    val primaryDownloadUrl: String
        get() = if (isDesktopOnly) exeUrl.ifBlank { linuxUrl } else apkUrl
}

data class DesktopBuild(val platform: PlatformFilter, val format: String, val url: String) {
    /** The version in the file's release tag (".../download/<slug>-v1.2.3/<file>"), or null. */
    val version: String? get() = Regex("""/download/[^/]*-v(\d[\w.]*)/""").find(url)?.groupValues?.get(1)
}

/** The system filter. All is the default; the others narrow the catalog to one system. */
enum class PlatformFilter(val label: String) {
    ALL("All systems"), ANDROID("Android"), WINDOWS("Windows"), LINUX("Linux")
}

data class MainUiState(
    val apps: List<AppUiItem> = emptyList(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val registryUrl: String = "",
    val notificationsEnabled: Boolean = true,
    val platform: PlatformFilter = PlatformFilter.ALL,
    /** When the catalog was last fetched successfully in this session, or null. */
    val lastSyncAt: Long? = null,
    val downloadingPackage: String? = null,
    val downloadProgress: Int = 0,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val filterType: FilterType = FilterType.ALL,
    val sortOption: SortOption = SortOption.RECENTLY_UPDATED,
    /** Legacy install-completion marker; the UI consumes it without navigating. */
    val postInstallOpenPackage: String? = null,
    /** Legacy completion metadata retained while an in-flight install finishes. */
    val postInstallOpenUpdatesTab: Boolean = false,
    /**
     * True while a PackageInstaller session is waiting on the user / system.
     * UI keeps the current detail card/tab open and shows an in-app banner.
     */
    val installSessionActive: Boolean = false,
    /** Short status for the in-app install banner. */
    val installStatusMessage: String? = null,
    val appManagerUpdateAvailable: AppUiItem? = null,
    val hubFirebaseConfigured: Boolean = false,
    val hubGoogleConfigured: Boolean = false,
    val hubSignedIn: Boolean = false,
    val hubAccountEmail: String? = null,
    val hubHasGoogleProvider: Boolean = false,
    val hubHasPasswordProvider: Boolean = false,
    val hubEntitlements: Map<String, Boolean> = emptyMap(),
    val hubAdmin: Boolean = false,
    val hubAdminUsers: List<AdminManagedUser> = emptyList(),
    val hubAdminApps: List<AdminManagedApp> = emptyList(),
    val hubAdminBusy: Boolean = false,
    val hubBusy: Boolean = false
)

enum class FilterType {
    ALL, GAMES, APPS, UPDATES_AVAILABLE, INSTALLED, NOT_INSTALLED
}

enum class SortOption(val label: String) {
    RECENTLY_UPDATED("Latest release"),
    NAME_ASC("Name, A to Z"),
    STATUS("Needs update first")
}

/** Orders the catalog. "Latest release" uses the catalog's release date, never a version number. */
fun sortApps(apps: List<AppUiItem>, option: SortOption): List<AppUiItem> = when (option) {
    SortOption.RECENTLY_UPDATED -> apps.sortedWith(compareByDescending<AppUiItem> { it.releasedAt }.thenBy { it.name.lowercase() })
    SortOption.NAME_ASC -> apps.sortedBy { it.name.lowercase() }
    SortOption.STATUS -> apps.sortedWith(
        compareBy<AppUiItem> { if (it.isOutdated) 0 else if (it.isInstalled) 1 else 2 }
            .thenByDescending { it.releasedAt }
            .thenBy { it.name.lowercase() }
    )
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val repository: AppRepository
    private val entitlementRepository = EntitlementRepository(context)
    private val sharedPrefs = context.getSharedPreferences("app_manager_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var cachedDbApps: List<ManagedApp> = emptyList()
    private var adminDirectoryJob: Job? = null
    private var capabilityJob: Job? = null
    private var capabilityAccountUid: String? = null
    private var proCapabilities: Map<String, Boolean> = emptyMap()

    /** Package whose system installer was launched; used to detect completion. */
    private var awaitingInstallPackage: String? = null

    /** Legacy completion metadata distinguishing updates from fresh installs. */
    private var preferUpdatesTabAfterInstall: Boolean = false

    init {
        val database = AppDatabase.getDatabase(context)
        repository = AppRepository(context, database.managedAppDao())

        // Load preferences
        val defaultUrl = "https://raw.githubusercontent.com/rykergrey/RykerSoft/main/registry.json"
        val savedUrl = sharedPrefs.getString("registry_url", defaultUrl) ?: defaultUrl
        val sanitizedUrl = repository.fetcher.sanitizeUrl(if (savedUrl.isBlank()) defaultUrl else savedUrl)
        val savedNotify = sharedPrefs.getBoolean("notifications_enabled", true)
        // Remove credentials saved by releases that supported private GitHub distribution.
        sharedPrefs.edit().remove("github_token").apply()
        val savedPlatform = try { PlatformFilter.valueOf(sharedPrefs.getString("platform_filter", null) ?: "") } catch (e: Exception) { PlatformFilter.ALL }
        val savedSortName = sharedPrefs.getString("sort_option", SortOption.RECENTLY_UPDATED.name)
        val savedSortOption = try { SortOption.valueOf(savedSortName ?: "") } catch (e: Exception) { SortOption.RECENTLY_UPDATED }
                _uiState.update {
                    it.copy(
                        registryUrl = sanitizedUrl,
                notificationsEnabled = savedNotify, 
                platform = savedPlatform,
                sortOption = savedSortOption,
                hubFirebaseConfigured = entitlementRepository.isConfigured(),
                hubGoogleConfigured = entitlementRepository.isGoogleConfigured()
            ) 
        }

        // Start observing database and scanning system package manager
        observeApps()
        observeHubAccount()
        
        viewModelScope.launch {
            refreshLocalInstallations()
            syncWithRegistry()
        }
    }

    private fun observeApps() {
        viewModelScope.launch {
            repository.allAppsFlow.collect { dbApps ->
                cachedDbApps = dbApps
                mapAppsToUi(dbApps)
            }
        }
    }

    private fun observeHubAccount() {
        viewModelScope.launch {
        entitlementRepository.accountState().collect { account ->
                _uiState.update {
                    it.copy(
                        hubFirebaseConfigured = account.configured,
                        hubGoogleConfigured = account.googleConfigured,
                        hubSignedIn = account.user != null,
                        hubAccountEmail = account.email,
                        hubHasGoogleProvider = account.hasGoogleProvider,
                        hubHasPasswordProvider = account.hasPasswordProvider,
                        hubAdmin = account.isAdmin,
                        hubEntitlements = account.entitlements,
                        hubAdminUsers = if (account.isAdmin) it.hubAdminUsers else emptyList()
                    )
                }
                if (capabilityAccountUid != account.user?.uid) {
                    capabilityJob?.cancel()
                    capabilityAccountUid = account.user?.uid
                    proCapabilities = emptyMap()
                    if (account.user != null) {
                        capabilityJob = viewModelScope.launch {
                            entitlementRepository.observeProCapabilities().collect { capabilities ->
                                proCapabilities = capabilities
                                mapAppsToUi(cachedDbApps)
                                if (_uiState.value.hubAdmin) refreshAdminUsers(forceRefresh = true)
                            }
                        }
                    }
                }
                if (account.isAdmin) {
                    observeAdminDirectory()
                } else {
                    adminDirectoryJob?.cancel()
                    adminDirectoryJob = null
                    _uiState.update { it.copy(hubAdminUsers = emptyList(), hubAdminApps = emptyList(), hubAdminBusy = false) }
                }
                if (cachedDbApps.isNotEmpty()) {
                    mapAppsToUi(cachedDbApps)
                } else {
                    refreshLocalInstallations()
                }
            }
        }
    }

    fun hubGoogleSignIn(activityContext: Context, linkLegacyAccount: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(hubBusy = true) }
            try {
                val idToken = GoogleSignInManager.requestIdToken(
                    activityContext,
                    RykerSoftFirebase.webClientId()
                )
                if (linkLegacyAccount) {
                    entitlementRepository.linkGoogleAccount(idToken)
                    _uiState.update {
                        it.copy(infoMessage = "Google account linked. Your existing RykerSoft data and entitlements were preserved.")
                    }
                } else {
                    entitlementRepository.signInWithGoogle(idToken)
                    _uiState.update { it.copy(infoMessage = "Signed in to RykerSoft with Google.") }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.localizedMessage ?: "Google sign-in failed.")
                }
            } finally {
                _uiState.update { it.copy(hubBusy = false) }
            }
        }
    }

    fun hubLegacySignIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(hubBusy = true) }
            try {
                entitlementRepository.signInLegacy(email, password)
                _uiState.update {
                    it.copy(infoMessage = "Legacy account verified. Link Google now to preserve this account's UID and entitlements.")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.localizedMessage ?: "Legacy sign-in failed.")
                }
            } finally {
                _uiState.update { it.copy(hubBusy = false) }
            }
        }
    }

    fun hubSendPasswordReset(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(hubBusy = true) }
            try {
                entitlementRepository.sendPasswordReset(email)
                _uiState.update { it.copy(infoMessage = "Password reset email sent for the legacy account.") }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.localizedMessage ?: "Password reset failed.")
                }
            } finally {
                _uiState.update { it.copy(hubBusy = false) }
            }
        }
    }

    fun hubSignOut(activityContext: Context) {
        viewModelScope.launch {
            entitlementRepository.signOut()
            runCatching { GoogleSignInManager.clearCredentialState(activityContext) }
            _uiState.update { it.copy(infoMessage = "Signed out of RykerSoft account.") }
        }
    }

    fun setFilter(filter: FilterType) {
        _uiState.update { it.copy(filterType = filter) }
        refreshLocalInstallations()
    }

    fun setSortOption(sort: SortOption) {
        sharedPrefs.edit().putString("sort_option", sort.name).apply()
        _uiState.update { it.copy(sortOption = sort) }
        refreshLocalInstallations()
    }

    fun updateSettings(registryUrl: String, notificationsEnabled: Boolean) {
        val sanitized = repository.fetcher.sanitizeUrl(registryUrl)
        sharedPrefs.edit()
            .putString("registry_url", sanitized)
            .putBoolean("notifications_enabled", notificationsEnabled)
            .remove("github_token")
            .apply()
        _uiState.update { 
            it.copy(
                registryUrl = sanitized,
                notificationsEnabled = notificationsEnabled
            ) 
        }
        SchedulerHelper.schedulePeriodicCheck(context, notificationsEnabled)
        syncWithRegistry()
    }

    fun updateRegistryUrl(url: String) {
        val sanitized = repository.fetcher.sanitizeUrl(url)
        sharedPrefs.edit().putString("registry_url", sanitized).apply()
        _uiState.update { it.copy(registryUrl = sanitized) }
        syncWithRegistry()
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("notifications_enabled", enabled).apply()
        _uiState.update { it.copy(notificationsEnabled = enabled) }
        SchedulerHelper.schedulePeriodicCheck(context, enabled)
    }

    fun setPlatform(platform: PlatformFilter) {
        sharedPrefs.edit().putString("platform_filter", platform.name).apply()
        _uiState.update { it.copy(platform = platform) }
    }

    fun clearPostInstallOpen() {
        _uiState.update {
            it.copy(
                postInstallOpenPackage = null,
                postInstallOpenUpdatesTab = false
            )
        }
    }

    /**
     * Clears a stuck "install in progress" lock and abandons any leftover PackageInstaller sessions
     * so the user can retry.
     */
    fun cancelStuckInstall() {
        ApkManager.abandonOwnedSessions(context)
        awaitingInstallPackage = null
        preferUpdatesTabAfterInstall = false
        pendingInstallFile = null
        _uiState.update {
            it.copy(
                installSessionActive = false,
                installStatusMessage = null,
                downloadingPackage = null,
                postInstallOpenUpdatesTab = false,
                infoMessage = "Previous install cancelled. You can try again."
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearInfo() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    fun refreshAdminUsers(forceRefresh: Boolean = false, isAdmin: Boolean = _uiState.value.hubAdmin) {
        val shouldUseCurrentAdminState = isAdmin || _uiState.value.hubAdmin
        val isAdminUser = shouldUseCurrentAdminState
        if (!isAdminUser) {
            _uiState.update { it.copy(hubAdminUsers = emptyList(), hubAdminBusy = false) }
            return
        }
        viewModelScope.launch {
            if (!forceRefresh && _uiState.value.hubAdminUsers.isNotEmpty()) return@launch
            _uiState.update { it.copy(hubAdminBusy = true) }
            try {
                val users = entitlementRepository.listUsersForAdmin()
                val apps = entitlementRepository.listAppsForAdmin()
                _uiState.update {
                    it.copy(
                        hubAdminUsers = users,
                        hubAdminApps = apps,
                        hubAdminBusy = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        hubAdminBusy = false,
                        errorMessage = e.localizedMessage ?: "Failed to load admin user list."
                    )
                }
            }
        }
    }

    private fun observeAdminDirectory() {
        if (adminDirectoryJob?.isActive == true) return
        adminDirectoryJob = viewModelScope.launch {
            entitlementRepository.observeAdminUserDirectory().collect { directory ->
                val currentIds = directory.map { it.uid }.toSet()
                val preferenceKey = "admin_known_user_ids"
                val knownIds = sharedPrefs.getStringSet(preferenceKey, emptySet()).orEmpty().toSet()
                if (knownIds.isNotEmpty()) {
                    val newUsers = directory.filter { it.uid !in knownIds }
                    if (newUsers.isNotEmpty()) {
                        NotificationHelper.showNewAccountNotification(context, newUsers.map { it.email })
                    }
                }
                sharedPrefs.edit().putStringSet(preferenceKey, knownIds + currentIds).apply()
                refreshAdminUsers(forceRefresh = true, isAdmin = true)
            }
        }
    }

    fun setAdminUserProAccess(targetUid: String, packageId: String, enabled: Boolean) {
        if (!_uiState.value.hubAdmin) {
            _uiState.update {
                it.copy(errorMessage = "Only the designated RykerSoft administrator can change pro access.")
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(hubAdminBusy = true) }
            try {
                entitlementRepository.setUserAppProAccess(
                    targetUid = targetUid,
                    packageId = packageId,
                    enabled = enabled
                )
                refreshAdminUsers(forceRefresh = true)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.localizedMessage ?: "Failed to update pro access.")
                }
            } finally {
                _uiState.update { it.copy(hubAdminBusy = false) }
            }
        }
    }

    fun setAdminProviderKeys(packageId: String, values: Map<String, String>) {
        if (!_uiState.value.hubAdmin) return
        viewModelScope.launch {
            _uiState.update { it.copy(hubAdminBusy = true) }
            try {
                entitlementRepository.setProviderKeys(packageId, values)
                _uiState.update { it.copy(infoMessage = "Provider credentials updated for $packageId.") }
                refreshAdminUsers(forceRefresh = true)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.localizedMessage ?: "Failed to update provider credentials.") }
            } finally {
                _uiState.update { it.copy(hubAdminBusy = false) }
            }
        }
    }

    /**
     * Handles install-result intents delivered by [InstallStatusReceiver] / confirmation host.
     */
    fun consumeInstallIntent(intent: Intent?) {
        if (intent == null) return
        val result = intent.getStringExtra(InstallStatusReceiver.EXTRA_INSTALL_RESULT) ?: return
        val packageName = intent.getStringExtra(InstallStatusReceiver.EXTRA_POST_INSTALL_PACKAGE)
        val message = intent.getStringExtra(InstallStatusReceiver.EXTRA_INSTALL_MESSAGE)

        // Prevent re-processing the same intent on configuration changes.
        intent.removeExtra(InstallStatusReceiver.EXTRA_INSTALL_RESULT)
        intent.removeExtra(InstallStatusReceiver.EXTRA_POST_INSTALL_PACKAGE)
        intent.removeExtra(InstallStatusReceiver.EXTRA_INSTALL_MESSAGE)

        _uiState.update {
            it.copy(
                installSessionActive = false,
                installStatusMessage = null
            )
        }
        InstallSessionTracker.clear(context)

        when (result) {
            InstallStatusReceiver.RESULT_SUCCESS -> {
                if (!packageName.isNullOrBlank()) {
                    awaitingInstallPackage = packageName
                }
                _uiState.update {
                    it.copy(infoMessage = "Install completed.")
                }
                refreshLocalInstallations()
            }
            else -> {
                awaitingInstallPackage = null
                preferUpdatesTabAfterInstall = false
                _uiState.update {
                    it.copy(
                        errorMessage = message ?: "Install failed.",
                        postInstallOpenPackage = null,
                        postInstallOpenUpdatesTab = false
                    )
                }
                refreshLocalInstallations()
            }
        }
    }

    private fun loadAssetDoc(slug: String, docName: String): String {
        return try {
            context.assets.open("app_docs/$slug/$docName").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            ""
        }
    }

    private fun getAppSlug(packageName: String, name: String): String {
        val pkg = packageName.lowercase()
        val n = name.lowercase()
        return when {
            pkg.contains("appmanager") || n.contains("rykersoft") -> "rykersoft"
            pkg.contains("informant") || n.contains("informant") -> "informant"
            pkg.contains("rush") || n.contains("rush") -> "rush"
            pkg.contains("synthing") || n.contains("synthing") -> "synthing"
            pkg.contains("superthinking") || n.contains("superthink") -> "superthinking"
            pkg.contains("bettertracking") || n.contains("bettertracking") -> "bettertracking"
            pkg.contains("hyperscribemobile") -> "hyperscribe-mobile"
            pkg.contains("hyperscribedesktop") -> "hyperscribe-desktop"
            else -> name.lowercase().replace(Regex("[^a-z0-9]"), "")
        }
    }

    private fun mergeProducts(items: List<AppUiItem>, productIds: Map<String, String>): List<AppUiItem> {
        val groups = items.groupBy { productIds[it.packageName].orEmpty() }
        val absorbed = mutableSetOf<String>()
        val merged = items.map { item ->
            val id = productIds[item.packageName].orEmpty()
            val siblings = if (id.isBlank() || !item.hasAndroid) emptyList() else groups[id].orEmpty().filter { it.isDesktopOnly }
            if (siblings.isEmpty()) item else {
                absorbed += siblings.map { it.packageName }
                val desktop = siblings.first()
                item.copy(
                    name = if (desktop.name.length < item.name.length) desktop.name else item.name,
                    exeUrl = item.exeUrl.ifBlank { desktop.exeUrl },
                    linuxUrl = item.linuxUrl.ifBlank { desktop.linuxUrl },
                    linuxDebUrl = item.linuxDebUrl.ifBlank { desktop.linuxDebUrl },
                    linuxPacmanUrl = item.linuxPacmanUrl.ifBlank { desktop.linuxPacmanUrl },
                    screenshots = item.screenshots.ifEmpty { desktop.screenshots }
                )
            }
        }
        return merged.filterNot { it.packageName in absorbed }
    }

    /**
     * Map database model to UI representation by combining it with current local system installations.
     */
    private fun mapAppsToUi(dbApps: List<ManagedApp>) {
        val entitlements = _uiState.value.hubEntitlements
        val rawItems = dbApps.map { app ->
            val isDesktopOnly = app.apkUrl.isBlank() && (app.exeUrl.isNotBlank() || app.linuxUrl.isNotBlank())
            val info = if (isDesktopOnly) {
                InstalledAppInfo(false, null, null)
            } else {
                ApkManager.getInstalledAppInfo(context, app.packageName)
            }
            val currentCode = info.versionCode ?: 0L
            val isOutdated = info.isInstalled && (app.latestVersionCode > currentCode)
            val supportsAi = AiUnlockPackages.isUnlockable(app.packageName, proCapabilities)
            val aiUnlocked = supportsAi && entitlements[app.packageName] == true
            
            val statusText = when {
                isDesktopOnly -> "Desktop Downloads"
                !info.isInstalled -> "Not Installed"
                isOutdated -> "Update Available"
                else -> "Up to Date"
            }

            val slug = getAppSlug(app.packageName, app.name)
            // Only real content is shown: the catalog's own text, then the packaged docs, then nothing.
            val fullDescription = app.description.ifBlank { loadAssetDoc(slug, "description.md") }
            val updatesHistoryDoc = app.updatesHistory.ifBlank { loadAssetDoc(slug, "updates.md") }
            val specsDoc = app.specs.ifBlank { loadAssetDoc(slug, "specs.md") }
            val parsedScreenshots = app.screenshots.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            AppUiItem(
                packageName = app.packageName,
                name = app.name,
                description = fullDescription,
                summaryDescription = markdownSummary(fullDescription),
                latestVersionCode = app.latestVersionCode,
                latestVersionName = app.latestVersionName,
                apkUrl = app.apkUrl,
                exeUrl = app.exeUrl,
                linuxUrl = app.linuxUrl,
                windowsAvailable = app.windowsAvailable,
                icon = app.icon,
                changelog = app.changelog,
                screenshots = parsedScreenshots,
                isGame = app.isGame,
                isInstalled = info.isInstalled,
                installedVersionName = info.versionName,
                installedVersionCode = info.versionCode,
                isOutdated = isOutdated,
                statusText = statusText,
                updatesHistory = updatesHistoryDoc,
                specs = specsDoc,
                tagline = app.tagline.ifBlank { markdownSummary(fullDescription) },
                features = app.features.split("\n").map { it.trim() }.filter { it.isNotEmpty() },
                proNote = app.pro,
                iconUrl = app.iconUrl,
                releasedAt = app.releasedAt,
                linuxDebUrl = app.linuxDebUrl,
                linuxPacmanUrl = app.linuxPacmanUrl,
                supportsAiUnlock = supportsAi,
                aiUnlocked = aiUnlocked
            )
        }
        // Entries that share a catalog id are one product. Where an Android entry exists, it carries
        // its desktop sibling's download links and the sibling is not listed separately.
        val productIds = dbApps.associate { it.packageName to it.productId }
        val uiItems = mergeProducts(rawItems, productIds)
        val appManagerUpdate = uiItems.find { it.packageName == context.packageName && it.isOutdated }

        val awaiting = awaitingInstallPackage
        var postInstallOpen = _uiState.value.postInstallOpenPackage
        var postInstallUpdatesTab = _uiState.value.postInstallOpenUpdatesTab
        var clearInstallSession = false
        if (awaiting != null) {
            val installedItem = uiItems.find { it.packageName == awaiting }
            if (installedItem != null && installedItem.isInstalled && !installedItem.isOutdated) {
                postInstallOpen = awaiting
                postInstallUpdatesTab = preferUpdatesTabAfterInstall
                awaitingInstallPackage = null
                preferUpdatesTabAfterInstall = false
                clearInstallSession = true
                InstallSessionTracker.clear(context)
            }
        }

        _uiState.update { 
            it.copy(
                apps = uiItems,
                appManagerUpdateAvailable = appManagerUpdate,
                postInstallOpenPackage = postInstallOpen,
                postInstallOpenUpdatesTab = postInstallUpdatesTab,
                installSessionActive = if (clearInstallSession) false else it.installSessionActive,
                installStatusMessage = if (clearInstallSession) null else it.installStatusMessage,
                infoMessage = if (clearInstallSession && it.infoMessage == null) {
                    "Install completed."
                } else {
                    it.infoMessage
                }
            ) 
        }
    }

    /**
     * Manually scans package manager to ensure all displayed version statuses are 100% in sync.
     */
    fun refreshLocalInstallations() {
        viewModelScope.launch {
            val dbApps = repository.getAllApps()
            mapAppsToUi(dbApps)
        }
    }

    private var pendingInstallFile: java.io.File? = null

    fun checkPendingInstall() {
        val file = pendingInstallFile
        if (file != null && file.exists() && ApkManager.canInstallApks(context)) {
            val inferredPackage = file.name.substringBefore("_v").takeIf { it.isNotBlank() }
                ?: return
            pendingInstallFile = null
            viewModelScope.launch {
                beginSessionInstall(file, inferredPackage)
            }
        }
    }

    private suspend fun beginSessionInstall(file: java.io.File, packageName: String) {
        // Keep the hub and its current detail tab in place, and show an in-app waiting banner
        // underneath the system confirmation UI until the install finishes.
        _uiState.update {
            it.copy(
                installSessionActive = true,
                installStatusMessage = "Waiting for install confirmation… Complete the system prompts when they appear.",
                infoMessage = null
            )
        }
        awaitingInstallPackage = packageName
        val sessionId = ApkManager.installViaSession(context, file, packageName)
        if (sessionId == null) {
            awaitingInstallPackage = null
            _uiState.update {
                it.copy(
                    installSessionActive = false,
                    installStatusMessage = null,
                    errorMessage = "Failed to start package installer session."
                )
            }
        }
    }

    /**
     * Downloads APK from registry URL and installs via PackageInstaller session API.
     */
    fun downloadAndInstall(app: AppUiItem) {
        if (app.apkUrl.isBlank()) {
            _uiState.update { it.copy(errorMessage = "${app.name} is a Windows download and cannot be installed on Android.") }
            return
        }
        if (_uiState.value.downloadingPackage != null) {
            _uiState.update { it.copy(errorMessage = "A download is already in progress.") }
            return
        }

        // Catch Island / Secure Folder / Work copies before download — otherwise install fails
        // with a cryptic CONFLICT even though the main profile shows Not Installed.
        if (!app.isInstalled && ApkManager.packageExistsInOtherProfile(context, app.packageName)) {
            _uiState.update { it.copy(errorMessage = ApkManager.OTHER_PROFILE_CONFLICT_MESSAGE) }
            return
        }

        // Retain update/install completion metadata without changing the current UI location.
        preferUpdatesTabAfterInstall = app.isOutdated

        // If a prior install got stuck (Play Protect buried, session orphaned), clear it and retry
        // instead of permanently blocking the user.
        if (_uiState.value.installSessionActive ||
            InstallSessionTracker.awaitingPackage(context) != null ||
            context.packageManager.packageInstaller.mySessions.isNotEmpty()
        ) {
            ApkManager.abandonOwnedSessions(context)
            _uiState.update {
                it.copy(
                    installSessionActive = false,
                    installStatusMessage = null
                )
            }
        }

        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    downloadingPackage = app.packageName,
                    downloadProgress = 0,
                    infoMessage = "Starting download of ${app.name}..."
                )
            }

            val fileName = "${app.packageName}_v${app.latestVersionName}.apk"
            try {
                ApkManager.downloadApk(context, app.apkUrl, fileName).collect { progress ->
                    when (progress) {
                        is DownloadProgress.Downloading -> {
                            _uiState.update { it.copy(downloadProgress = progress.progress) }
                        }
                        is DownloadProgress.Completed -> {
                            _uiState.update { 
                                it.copy(
                                    downloadingPackage = null,
                                    downloadProgress = 100,
                                    infoMessage = null
                                )
                            }

                            val validationError = ApkManager.validateApkForInstall(
                                context = context,
                                file = progress.file,
                                targetPackage = app.packageName
                            )
                            if (validationError != null) {
                                preferUpdatesTabAfterInstall = false
                                pendingInstallFile = null
                                _uiState.update {
                                    it.copy(
                                        installSessionActive = false,
                                        installStatusMessage = null,
                                        postInstallOpenUpdatesTab = false,
                                        errorMessage = validationError
                                    )
                                }
                            } else if (ApkManager.canInstallApks(context)) {
                                pendingInstallFile = null
                                beginSessionInstall(progress.file, app.packageName)
                            } else {
                                pendingInstallFile = progress.file
                                awaitingInstallPackage = app.packageName
                                _uiState.update { 
                                    it.copy(
                                        errorMessage = "Install permission required. Please allow unknown sources in settings."
                                    )
                                }
                                ApkManager.launchInstallSettings(context)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                preferUpdatesTabAfterInstall = false
                _uiState.update { 
                    it.copy(
                        downloadingPackage = null,
                        installSessionActive = false,
                        installStatusMessage = null,
                        postInstallOpenUpdatesTab = false,
                        errorMessage = "Failed to download APK: ${e.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    /**
     * Pull remote JSON and synchronize with local DB.
     */
    fun syncWithRegistry() {
        val url = _uiState.value.registryUrl
        if (url.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid Registry URL in settings.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            val result = repository.syncWithRegistry(url)
            _uiState.update { it.copy(isSyncing = false) }
            
            result.onSuccess {
                _uiState.update { it.copy(lastSyncAt = System.currentTimeMillis()) }
                refreshLocalInstallations()
            }.onFailure { error ->
                _uiState.update { 
                    it.copy(errorMessage = "Sync failed: ${error.localizedMessage ?: "Connection error"}") 
                }
            }
        }
    }

    /**
     * Manually add an app to the local list.
     */
    fun addManualApp(
        name: String,
        packageName: String,
        versionName: String,
        versionCode: Int,
        apkUrl: String,
        icon: String,
        changelog: String,
        isGame: Boolean = false
    ) {
        viewModelScope.launch {
            val app = ManagedApp(
                packageName = packageName.trim(),
                name = name.trim(),
                latestVersionCode = versionCode,
                latestVersionName = versionName.trim(),
                apkUrl = apkUrl.trim(),
                icon = icon.trim().ifEmpty { if (isGame) "sports_esports" else "apps" },
                changelog = changelog.trim(),
                isGame = isGame,
                lastChecked = System.currentTimeMillis()
            )
            repository.addOrUpdateApp(app)
            val typeLabel = if (isGame) "Game" else "App"
            _uiState.update { it.copy(infoMessage = "$typeLabel '${app.name}' added successfully!") }
            refreshLocalInstallations()
        }
    }

    /**
     * Delete an app from the manager.
     */
    fun deleteApp(packageName: String) {
        viewModelScope.launch {
            val app = repository.getApp(packageName)
            repository.deleteApp(packageName)
            _uiState.update { it.copy(infoMessage = "${app?.name ?: "Item"} removed from list.") }
            refreshLocalInstallations()
        }
    }

    /**
     * Clears the local catalog, including apps added by hand, and fetches it again from the registry.
     */
    fun resetCatalog() {
        viewModelScope.launch {
            repository.clearAllApps()
            refreshLocalInstallations()
            syncWithRegistry()
        }
    }
}
