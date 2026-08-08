package com.rykersoft.appmanager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rykersoft.appmanager.data.AppDatabase
import com.rykersoft.appmanager.network.RegistryFetcher
import com.rykersoft.appmanager.util.ApkManager
import com.rykersoft.appmanager.util.FamilyToken
import com.rykersoft.appmanager.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpdateCheckReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("app_manager_prefs", Context.MODE_PRIVATE)
        val defaultUrl = "https://raw.githubusercontent.com/rykergrey/RykerSoft/main/registry.json"
        val rawUrl = prefs.getString("registry_url", defaultUrl) ?: defaultUrl
        val rawToken = prefs.getString("github_token", "") ?: ""
        val token = if (rawToken.isBlank()) FamilyToken.baked() else rawToken
        val fetcher = RegistryFetcher()
        val registryUrl = fetcher.sanitizeUrl(if (rawUrl.isBlank()) defaultUrl else rawUrl)
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        
        if (!notificationsEnabled || registryUrl.isEmpty()) return

        // Fetch registry and compare versions
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val remoteApps = fetcher.fetchRegistry(registryUrl, token)
                
                // Sync local DB (remove obsolete package IDs and update remote apps)
                db.managedAppDao().syncRemoteApps(remoteApps)
                
                // Scan and notify
                for (app in remoteApps) {
                    val info = ApkManager.getInstalledAppInfo(context, app.packageName)
                    if (info.isInstalled) {
                        val currentVersionCode = info.versionCode ?: 0L
                        if (app.latestVersionCode > currentVersionCode) {
                            NotificationHelper.showUpdateNotification(
                                context = context,
                                appName = app.name,
                                latestVersion = app.latestVersionName,
                                packageName = app.packageName
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
