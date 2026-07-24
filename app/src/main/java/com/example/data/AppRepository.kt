package com.example.data

import android.content.Context
import com.example.network.RegistryFetcher
import com.example.util.ApkManager
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

    /**
     * Dynamically fetches description, updates history, specs, and user guide markdown files
     * directly from the app's GitHub repository main branch if missing in registry.json.
     */
    suspend fun enrichAppWithRemoteDocs(app: ManagedApp, githubToken: String = ""): ManagedApp = withContext(Dispatchers.IO) {
        val repoMatch = Regex("""^https?://github\.com/([^/]+)/([^/]+)""", RegexOption.IGNORE_CASE).find(app.apkUrl)
            ?: return@withContext app

        val (owner, repo) = repoMatch.destructured
        val baseUrl = "https://raw.githubusercontent.com/$owner/$repo/main"

        val desc = app.description.ifBlank {
            fetcher.fetchTextUrl("$baseUrl/docs/description.md", githubToken)
                ?: fetcher.fetchTextUrl("$baseUrl/README.md", githubToken)
                ?: ""
        }

        val updates = app.updatesHistory.ifBlank {
            fetcher.fetchTextUrl("$baseUrl/docs/updates.md", githubToken)
                ?: fetcher.fetchTextUrl("$baseUrl/CHANGELOG.md", githubToken)
                ?: app.changelog
        }

        val specs = app.specs.ifBlank {
            fetcher.fetchTextUrl("$baseUrl/docs/specs.md", githubToken) ?: ""
        }

        val userGuide = app.userGuide.ifBlank {
            fetcher.fetchTextUrl("$baseUrl/docs/user_guide.md", githubToken) ?: ""
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
    suspend fun syncWithRegistry(url: String, githubToken: String = ""): Result<List<ManagedApp>> = withContext(Dispatchers.IO) {
        if (url.isBlank()) {
            Result.failure(IllegalArgumentException("Registry URL is empty"))
        } else {
            try {
                val remoteApps = fetcher.fetchRegistry(url, githubToken)
                val enrichedApps = remoteApps.map { app ->
                    enrichAppWithRemoteDocs(app, githubToken)
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
