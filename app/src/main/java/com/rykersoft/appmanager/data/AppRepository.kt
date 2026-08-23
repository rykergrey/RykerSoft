package com.rykersoft.appmanager.data

import android.content.Context
import com.rykersoft.appmanager.network.RegistryFetcher
import com.rykersoft.appmanager.util.ApkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AppRepository(
    private val context: Context,
    private val dao: ManagedAppDao,
    val fetcher: RegistryFetcher = RegistryFetcher()
) {
    val allAppsFlow: Flow<List<ManagedApp>> = dao.getAllAppsFlow()

    suspend fun getAllApps(): List<ManagedApp> = dao.getAllApps()

    suspend fun getApp(packageName: String): ManagedApp? = dao.getAppByPackageName(packageName)

    suspend fun addOrUpdateApp(app: ManagedApp) {
        dao.insertApp(app)
    }

    suspend fun deleteApp(packageName: String) {
        dao.deleteAppByPackage(packageName)
    }

    suspend fun clearAllApps() {
        dao.clearAll()
    }

    private fun appSlug(packageName: String, name: String): String {
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

    /**
     * Dynamically fetches description, updates history, specs, and user guide markdown files
     * from the distribution repo referenced by apkUrl or exeUrl if missing in registry.json.
     * Looks in docs/<slug>/ first (shared RykerSoft-APKs distribution repo layout),
     * then falls back to repo-root docs/ and README/CHANGELOG (per-app repo layout).
     */
    suspend fun enrichAppWithRemoteDocs(app: ManagedApp): ManagedApp = withContext(Dispatchers.IO) {
        val distributionUrl = app.apkUrl.ifBlank { app.exeUrl }
        val repoMatch = Regex("""^https?://github\.com/([^/]+)/([^/]+)""", RegexOption.IGNORE_CASE).find(distributionUrl)
            ?: return@withContext app

        val (owner, repo) = repoMatch.destructured
        val baseUrl = "https://raw.githubusercontent.com/$owner/$repo/main"
        val slug = appSlug(app.packageName, app.name)

        val desc = app.description.ifBlank {
            fetcher.fetchTextUrl("$baseUrl/docs/$slug/description.md")
                ?: fetcher.fetchTextUrl("$baseUrl/docs/description.md")
                ?: fetcher.fetchTextUrl("$baseUrl/README.md")
                ?: ""
        }

        val updates = app.updatesHistory.ifBlank {
            fetcher.fetchTextUrl("$baseUrl/docs/$slug/updates.md")
                ?: fetcher.fetchTextUrl("$baseUrl/docs/updates.md")
                ?: fetcher.fetchTextUrl("$baseUrl/CHANGELOG.md")
                ?: app.changelog
        }

        val specs = app.specs.ifBlank {
            fetcher.fetchTextUrl("$baseUrl/docs/$slug/specs.md")
                ?: fetcher.fetchTextUrl("$baseUrl/docs/specs.md")
                ?: ""
        }

        val userGuide = app.userGuide.ifBlank {
            fetcher.fetchTextUrl("$baseUrl/docs/$slug/user_guide.md")
                ?: fetcher.fetchTextUrl("$baseUrl/docs/user_guide.md")
                ?: ""
        }

        if (desc != app.description || updates != app.updatesHistory || specs != app.specs || userGuide != app.userGuide) {
            val enriched = app.copy(
                description = desc,
                updatesHistory = updates,
                specs = specs,
                userGuide = userGuide
            )
            dao.insertApp(enriched)
            enriched
        } else {
            app
        }
    }

    /**
     * Fetch registry from custom URL and update local database.
     */
    suspend fun syncWithRegistry(url: String): Result<List<ManagedApp>> = withContext(Dispatchers.IO) {
        if (url.isBlank()) {
            Result.failure(IllegalArgumentException("Registry URL is empty"))
        } else {
            try {
                val remoteApps = fetcher.fetchRegistry(url)
                val enrichedApps = remoteApps.map { app ->
                    enrichAppWithRemoteDocs(app)
                }
                dao.syncRemoteApps(enrichedApps)
                Result.success(enrichedApps)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }
}
