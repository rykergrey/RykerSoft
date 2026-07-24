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
     * Fetch registry from custom URL and update local database.
     */
    suspend fun syncWithRegistry(url: String, githubToken: String = ""): Result<List<ManagedApp>> = withContext(Dispatchers.IO) {
        if (url.isBlank()) {
            Result.failure(IllegalArgumentException("Registry URL is empty"))
        } else {
            try {
                val remoteApps = fetcher.fetchRegistry(url, githubToken)
                dao.syncRemoteApps(remoteApps)
                Result.success(remoteApps)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }
}
