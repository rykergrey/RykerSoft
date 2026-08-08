package com.rykersoft.appmanager.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rykersoft.appmanager.data.AppDatabase
import com.rykersoft.appmanager.data.AppRepository
import com.rykersoft.appmanager.data.ManagedApp
import com.rykersoft.appmanager.entitlements.AiUnlockPackages
import com.rykersoft.appmanager.entitlements.EntitlementRepository
import android.content.Intent
import com.rykersoft.appmanager.install.InstallSessionTracker
import com.rykersoft.appmanager.install.InstallStatusReceiver
import com.rykersoft.appmanager.util.ApkManager
import com.rykersoft.appmanager.util.DownloadProgress
import com.rykersoft.appmanager.util.FamilyToken
import com.rykersoft.appmanager.util.SchedulerHelper
import com.rykersoft.appmanager.ui.theme.TitleFontPreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    val icon: String,
    val changelog: String,
    val screenshots: List<String>,
    val isGame: Boolean,
    val isInstalled: Boolean,
    val installedVersionName: String?,
    val installedVersionCode: Long?,
    val isOutdated: Boolean,
    val statusText: String,
    val userGuide: String = "",
    val updatesHistory: String = "",
    val specs: String = "",
    /** True when this package supports pro unlock and the signed-in hub account has unlocked it. */
    val supportsAiUnlock: Boolean = false,
    val aiUnlocked: Boolean = false
)

data class MainUiState(
    val apps: List<AppUiItem> = emptyList(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val registryUrl: String = "",
    val githubToken: String = "",
    val notificationsEnabled: Boolean = true,
    val titleFontPreset: TitleFontPreset = TitleFontPreset.ARCADE_3D,
    val downloadingPackage: String? = null,
    val downloadProgress: Int = 0,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val filterType: FilterType = FilterType.ALL,
    val sortOption: SortOption = SortOption.RECENTLY_UPDATED,
    /**
     * After a successful install/update, UI should open this package's detail dialog.
     */
    val postInstallOpenPackage: String? = null,
    /**
     * When [postInstallOpenPackage] is set, open the Updates tab (true) or User Guide (false).
     * Updates keep the user on changelog; fresh installs land on the User Guide.
     */
    val postInstallOpenUpdatesTab: Boolean = false,
    /**
     * True while a PackageInstaller session is waiting on the user / system.
     * UI dismisses the detail Dialog (Dialog windows bury Play Protect) and shows an in-app banner.
     */
    val installSessionActive: Boolean = false,
    /** Short status for the in-app install banner. */
    val installStatusMessage: String? = null,
    val appManagerUpdateAvailable: AppUiItem? = null,
    val hubFirebaseConfigured: Boolean = false,
    val hubSignedIn: Boolean = false,
    val hubAccountEmail: String? = null,
    val hubEntitlements: Map<String, Boolean> = emptyMap(),
    val hubBusy: Boolean = false
)

enum class FilterType {
    ALL, GAMES, APPS, UPDATES_AVAILABLE, INSTALLED, NOT_INSTALLED
}

enum class SortOption(val label: String) {
    RECENTLY_UPDATED("Recently Updated"),
    NAME_ASC("Name (A to Z)"),
    NAME_DESC("Name (Z to A)"),
    VERSION_CODE_DESC("Version (Newest First)"),
    STATUS("Update Status")
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val repository: AppRepository
    private val entitlementRepository = EntitlementRepository(context)
    private val sharedPrefs = context.getSharedPreferences("app_manager_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var cachedDbApps: List<ManagedApp> = emptyList()

    /** Package whose system installer was launched; used to reopen detail after success. */
    private var awaitingInstallPackage: String? = null

    /** True when the in-flight install is an update (reopen Updates tab); false for fresh installs. */
    private var preferUpdatesTabAfterInstall: Boolean = false

    init {
        val database = AppDatabase.getDatabase(context)
        repository = AppRepository(context, database.managedAppDao())

        // Load preferences
        val defaultUrl = "https://raw.githubusercontent.com/rykergrey/RykerSoft/main/registry.json"
        val defaultToken = FamilyToken.baked()
        val savedUrl = sharedPrefs.getString("registry_url", defaultUrl) ?: defaultUrl
        val sanitizedUrl = repository.fetcher.sanitizeUrl(if (savedUrl.isBlank()) defaultUrl else savedUrl)
        val savedNotify = sharedPrefs.getBoolean("notifications_enabled", true)
        val rawToken = sharedPrefs.getString("github_token", "") ?: ""
        val sanitizedToken = if (rawToken.isBlank()) defaultToken else rawToken
        val savedPresetName = sharedPrefs.getString("title_font_preset", TitleFontPreset.ARCADE_3D.name)
        val savedPreset = try { TitleFontPreset.valueOf(savedPresetName ?: "") } catch (e: Exception) { TitleFontPreset.ARCADE_3D }
        val savedSortName = sharedPrefs.getString("sort_option", SortOption.RECENTLY_UPDATED.name)
        val savedSortOption = try { SortOption.valueOf(savedSortName ?: "") } catch (e: Exception) { SortOption.RECENTLY_UPDATED }
        _uiState.update { 
            it.copy(
                registryUrl = sanitizedUrl, 
                githubToken = sanitizedToken,
                notificationsEnabled = savedNotify, 
                titleFontPreset = savedPreset,
                sortOption = savedSortOption,
                hubFirebaseConfigured = entitlementRepository.isConfigured()
            ) 
        }

        // Start observing database and scanning system package manager
        observeApps()
        observeHubAccount()
        
        viewModelScope.launch {
            if (repository.getApp("com.rykersoft.appmanager") == null) {
                repository.addOrUpdateApp(
                    ManagedApp(
                        packageName = "com.rykersoft.appmanager",
                        name = "RykerSoft",
                        description = "Personal Android app hub and application manager. Easily check for updates, view changelogs, download, and install latest versions of RykerSoft applications.",
                        latestVersionCode = 1,
                        latestVersionName = "1.0.0",
                        apkUrl = "https://github.com/rykergrey/RykerSoft/releases/download/v1.0.0/app-release.apk",
                        icon = "android",
                        changelog = "• Official RykerSoft Application Manager package registration\n• In-app self-updating and version detection alerts",
                        isGame = false
                    )
                )
            }
            if (repository.getApp("com.informant.app") == null) {
                repository.addOrUpdateApp(
                    ManagedApp(
                        packageName = "com.informant.app",
                        name = "INFORMANT",
                        description = "Official INFORMANT Android Application from RykerSoft. Built with Capacitor Android web sync, real-time update tracking, and standalone distribution.",
                        latestVersionCode = 2,
                        latestVersionName = "1.0.1",
                        apkUrl = "https://github.com/rykergrey/INFORMANT/releases/download/v1.0.1/app-release.apk",
                        icon = "android",
                        changelog = "• Initial RykerSoft hub registration for INFORMANT\n• Android package com.informant.app version 1.0.1 (versionCode 2)\n• Capacitor Android build synced from current web app",
                        isGame = false
                    )
                )
            }
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
                        hubSignedIn = account.user != null,
                        hubAccountEmail = account.email,
                        hubEntitlements = account.entitlements
                    )
                }
                if (cachedDbApps.isNotEmpty()) {
                    mapAppsToUi(cachedDbApps)
                } else {
                    refreshLocalInstallations()
                }
            }
        }
    }

    fun hubSignIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(hubBusy = true) }
            try {
                entitlementRepository.signIn(email, password)
                _uiState.update { it.copy(infoMessage = "Signed in to RykerSoft account.") }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.localizedMessage ?: "Sign-in failed.")
                }
            } finally {
                _uiState.update { it.copy(hubBusy = false) }
            }
        }
    }

    fun hubSignUp(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(hubBusy = true) }
            try {
                entitlementRepository.signUp(email, password)
                _uiState.update { it.copy(infoMessage = "RykerSoft account created.") }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.localizedMessage ?: "Account creation failed.")
                }
            } finally {
                _uiState.update { it.copy(hubBusy = false) }
            }
        }
    }

    fun hubSignOut() {
        entitlementRepository.signOut()
        _uiState.update { it.copy(infoMessage = "Signed out of RykerSoft account.") }
    }

    fun unlockAppWithCode(packageName: String, code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(hubBusy = true) }
            try {
                val granted = entitlementRepository.unlockWithCode(code, packageName)
                _uiState.update {
                    it.copy(infoMessage = "Unlocked pro features for: ${granted.joinToString()}")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.localizedMessage ?: "Unlock failed.")
                }
            } finally {
                _uiState.update { it.copy(hubBusy = false) }
            }
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

    fun updateSettings(registryUrl: String, notificationsEnabled: Boolean, githubToken: String) {
        val sanitized = repository.fetcher.sanitizeUrl(registryUrl)
        val trimmedToken = githubToken.trim()
        // Blank saved token means "use the baked-in family token".
        val effectiveToken = trimmedToken.ifBlank { FamilyToken.baked() }
        sharedPrefs.edit()
            .putString("registry_url", sanitized)
            .putBoolean("notifications_enabled", notificationsEnabled)
            .putString("github_token", trimmedToken)
            .apply()
        _uiState.update { 
            it.copy(
                registryUrl = sanitized,
                notificationsEnabled = notificationsEnabled,
                githubToken = effectiveToken
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

    fun setTitleFontPreset(preset: TitleFontPreset) {
        sharedPrefs.edit().putString("title_font_preset", preset.name).apply()
        _uiState.update { it.copy(titleFontPreset = preset) }
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
            else -> name.lowercase().replace(Regex("[^a-z0-9]"), "")
        }
    }

    /**
     * Map database model to UI representation by combining it with current local system installations.
     */
    private fun mapAppsToUi(dbApps: List<ManagedApp>) {
        val entitlements = _uiState.value.hubEntitlements
        val uiItems = dbApps.map { app ->
            val info = ApkManager.getInstalledAppInfo(context, app.packageName)
            val currentCode = info.versionCode ?: 0L
            val isOutdated = info.isInstalled && (app.latestVersionCode > currentCode)
            val supportsAi = AiUnlockPackages.isUnlockable(app.packageName)
            val aiUnlocked = supportsAi && entitlements[app.packageName] == true
            
            val statusText = when {
                !info.isInstalled -> "Not Installed"
                isOutdated -> "Update Available"
                else -> "Up to Date"
            }

            val slug = getAppSlug(app.packageName, app.name)
            val fullDescription = app.description.ifBlank { loadAssetDoc(slug, "description.md") }.ifBlank {
                if (app.isGame) {
                    "High-action immersive arcade game featuring high-definition graphics, reactive touch controls, custom leaderboard achievements, and standalone offline gameplay built for smooth performance."
                } else {
                    "Essential utility application engineered by Ryker Grey. Built with modern Kotlin, high-speed local data persistence, clean navigation, and responsive Material Design 3 interface elements."
                }
            }

            val userGuideDoc = app.userGuide.ifBlank { loadAssetDoc(slug, "user_guide.md") }
            val updatesHistoryDoc = app.updatesHistory.ifBlank { loadAssetDoc(slug, "updates.md") }.ifBlank { app.changelog }
            val specsDoc = app.specs.ifBlank { loadAssetDoc(slug, "specs.md") }

            val parsedScreenshots = if (app.screenshots.isNotBlank()) {
                app.screenshots.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            } else {
                // Fallback default screenshots if none specified
                if (app.isGame) {
                    listOf(
                        "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800&q=80",
                        "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=800&q=80",
                        "https://images.unsplash.com/photo-1538481199705-c710c4e965fc?w=800&q=80"
                    )
                } else {
                    listOf(
                        "https://images.unsplash.com/photo-1507925921958-8a62f3d1a50d?w=800&q=80",
                        "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=800&q=80",
                        "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=800&q=80"
                    )
                }
            }

            AppUiItem(
                packageName = app.packageName,
                name = app.name,
                description = fullDescription,
                summaryDescription = markdownSummary(fullDescription),
                latestVersionCode = app.latestVersionCode,
                latestVersionName = app.latestVersionName,
                apkUrl = app.apkUrl,
                icon = app.icon,
                changelog = app.changelog.ifBlank {
                    "• Performance improvements and general bug fixes.\n• Enhanced memory management and system stability."
                },
                screenshots = parsedScreenshots,
                isGame = app.isGame,
                isInstalled = info.isInstalled,
                installedVersionName = info.versionName,
                installedVersionCode = info.versionCode,
                isOutdated = isOutdated,
                statusText = statusText,
                userGuide = userGuideDoc,
                updatesHistory = updatesHistoryDoc,
                specs = specsDoc,
                supportsAiUnlock = supportsAi,
                aiUnlocked = aiUnlocked
            )
        }
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
        // Dismiss the detail Dialog (Compose Dialog windows can bury Play Protect), keep the hub
        // in the foreground, and show an in-app waiting banner until the system prompts finish.
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

        // Updates reopen on the Updates/changelog tab; fresh installs keep User Guide.
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
                ApkManager.downloadApk(context, app.apkUrl, fileName, _uiState.value.githubToken).collect { progress ->
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
            _uiState.update { it.copy(isSyncing = true, infoMessage = "Syncing with remote registry...") }
            val result = repository.syncWithRegistry(url, _uiState.value.githubToken)
            _uiState.update { it.copy(isSyncing = false) }
            
            result.onSuccess { apps ->
                _uiState.update { 
                    it.copy(infoMessage = "Successfully synced ${apps.size} applications.") 
                }
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
     * Seed sample personal apps & games for instant out-of-the-box enjoyment!
     */
    fun seedSampleApps() {
        viewModelScope.launch {
            val samples = listOf(
                // APPS
                ManagedApp(
                    packageName = "com.informant.app",
                    name = "INFORMANT",
                    description = "Official INFORMANT Android Application from RykerSoft. Built with Capacitor Android web sync, real-time update tracking, and standalone distribution.",
                    latestVersionCode = 2,
                    latestVersionName = "1.0.1",
                    apkUrl = "https://github.com/rykergrey/INFORMANT/releases/download/v1.0.1/app-release.apk",
                    icon = "android",
                    changelog = "• Initial RykerSoft hub registration for INFORMANT\n• Android package com.informant.app version 1.0.1 (versionCode 2)\n• Capacitor Android build synced from current web app",
                    screenshots = "https://images.unsplash.com/photo-1507925921958-8a62f3d1a50d?w=800&q=80,https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=800&q=80",
                    isGame = false,
                    lastChecked = System.currentTimeMillis()
                ),
                ManagedApp(
                    packageName = "com.aistudio.todolist.sample",
                    name = "Focus Notes & Tasks",
                    description = "A powerful personal task organizer and productivity notebook. Features fast offline database synchronization, priority task tags, deadline reminders, and an interactive home screen widget.",
                    latestVersionCode = 2,
                    latestVersionName = "1.0.8",
                    apkUrl = "https://raw.githubusercontent.com/aistudio/assets/main/samples/focus_tasks.apk",
                    icon = "playlist_add_check",
                    changelog = "• Added high-speed SQLite local cache engine\n• Improved home screen quick task entry widget\n• Resolved notification badge sync latency",
                    screenshots = "https://images.unsplash.com/photo-1507925921958-8a62f3d1a50d?w=800&q=80,https://images.unsplash.com/photo-1484480974693-6ca0a78fb36b?w=800&q=80,https://images.unsplash.com/photo-1517842645767-c639042777db?w=800&q=80",
                    isGame = false,
                    lastChecked = System.currentTimeMillis()
                ),
                ManagedApp(
                    packageName = "com.aistudio.calculator.sample",
                    name = "Retro Calculator",
                    description = "A tactical scientific calculator with high-contrast OLED dark aesthetics. Includes multi-line calculation history memory, haptic tactile feedback, trigonometric functions, and unit conversion suites.",
                    latestVersionCode = 4,
                    latestVersionName = "2.3.1",
                    apkUrl = "https://raw.githubusercontent.com/aistudio/assets/main/samples/retro_calc.apk",
                    icon = "calculate",
                    changelog = "• Tactical scientific keypad layout upgrade\n• Custom haptic audio engine on touch response\n• High-contrast dark display with history export",
                    screenshots = "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=800&q=80,https://images.unsplash.com/photo-1587145820266-a5951ee6f620?w=800&q=80,https://images.unsplash.com/photo-1611162617213-7d7a39e9b1d7?w=800&q=80",
                    isGame = false,
                    lastChecked = System.currentTimeMillis()
                ),
                ManagedApp(
                    packageName = "com.aistudio.pixelplayer.sample",
                    name = "Pixel Media Player",
                    description = "Hardware-accelerated local audio and video playback suite. Supports high-resolution 10-bit video decoding, gapless FLAC audio streaming, equalizer presets, and wireless smart TV casting.",
                    latestVersionCode = 12,
                    latestVersionName = "3.4.0",
                    apkUrl = "https://raw.githubusercontent.com/aistudio/assets/main/samples/pixel_player.apk",
                    icon = "play_circle",
                    changelog = "• Native support for 10-bit HDR video streams\n• Ultra low-latency background audio playback engine\n• Improved casting protocol stability for TV devices",
                    screenshots = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=800&q=80,https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=800&q=80,https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=800&q=80",
                    isGame = false,
                    lastChecked = System.currentTimeMillis()
                ),
                // GAMES
                ManagedApp(
                    packageName = "com.aistudio.cyberrunner.game",
                    name = "Cyber Runner 2099",
                    description = "Fast-paced futuristic arcade endless runner set in a glowing synthwave metropolis. Navigate neon obstacles, collect energy cells, unlock cybernetic hoverboards, and compete on global leaderboards.",
                    latestVersionCode = 5,
                    latestVersionName = "1.2.0",
                    apkUrl = "https://raw.githubusercontent.com/aistudio/assets/main/samples/cyber_runner.apk",
                    icon = "sports_esports",
                    changelog = "• Unlocked Neon City night environment track\n• Full Bluetooth wireless gamepad controller support\n• 60 FPS frame rate rendering boost and anti-aliasing",
                    screenshots = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800&q=80,https://images.unsplash.com/photo-1511512578047-dfb367046420?w=800&q=80,https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=800&q=80",
                    isGame = true,
                    lastChecked = System.currentTimeMillis()
                ),
                ManagedApp(
                    packageName = "com.aistudio.astropuzzle.game",
                    name = "Astro Puzzle Quest",
                    description = "Mind-bending gravity puzzle game in deep space. Solve zero-gravity spatial physics challenges across 100 interstellar sectors with relaxing ambient soundscapes and offline progress saving.",
                    latestVersionCode = 1,
                    latestVersionName = "1.0.0",
                    apkUrl = "https://raw.githubusercontent.com/aistudio/assets/main/samples/astro_puzzle.apk",
                    icon = "extension",
                    changelog = "• Initial release featuring 100 orbital puzzle levels\n• Zero-gravity momentum physics engine\n• Offline single player campaign mode",
                    screenshots = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&q=80,https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=800&q=80,https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=800&q=80",
                    isGame = true,
                    lastChecked = System.currentTimeMillis()
                ),
                ManagedApp(
                    packageName = "com.aistudio.neontanks.game",
                    name = "Neon Tank Warfare",
                    description = "Action-packed tactical tank combat arena with retro arcade visuals. Battle against AI battalion waves or play locally with friends using plasma cannons, laser shields, and customizable armored treads.",
                    latestVersionCode = 3,
                    latestVersionName = "2.0.1",
                    apkUrl = "https://raw.githubusercontent.com/aistudio/assets/main/samples/neon_tanks.apk",
                    icon = "gamepad",
                    changelog = "• Added 4-player co-op arcade survival arena mode\n• New plasma shield & laser cannon supply drops\n• Custom tank armor skin builder",
                    screenshots = "https://images.unsplash.com/photo-1538481199705-c710c4e965fc?w=800&q=80,https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&q=80,https://images.unsplash.com/photo-1563089145-599997674d42?w=800&q=80",
                    isGame = true,
                    lastChecked = System.currentTimeMillis()
                )
            )
            repository.clearAllApps()
            samples.forEach { repository.addOrUpdateApp(it) }
            _uiState.update { it.copy(infoMessage = "Sandbox Games & Apps loaded successfully.") }
            refreshLocalInstallations()
        }
    }
}
