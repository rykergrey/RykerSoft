package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

data class InstalledAppInfo(
    val isInstalled: Boolean,
    val versionName: String?,
    val versionCode: Long?
)

object ApkManager {

    private val client = OkHttpClient()

    /**
     * Get details of an installed app by package name.
     */
    fun getInstalledAppInfo(context: Context, packageName: String): InstalledAppInfo {
        return try {
            val packageManager = context.packageManager
            val packageInfo: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
            InstalledAppInfo(
                isInstalled = true,
                versionName = packageInfo.versionName,
                versionCode = versionCode
            )
        } catch (e: PackageManager.NameNotFoundException) {
            InstalledAppInfo(
                isInstalled = false,
                versionName = null,
                versionCode = null
            )
        }
    }

    /**
     * Launch an app if it's installed.
     */
    fun launchApp(context: Context, packageName: String): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        return if (launchIntent != null) {
            context.startActivity(launchIntent)
            true
        } else {
            false
        }
    }

    /**
     * Downloads an APK from a remote URL, yielding percentage progress (0..100) and finally the File on completion.
     * Supports private GitHub repository release downloads when a Personal Access Token (PAT) is supplied.
     */
    fun downloadApk(context: Context, url: String, fileName: String, githubToken: String = ""): Flow<DownloadProgress> = flow {
        emit(DownloadProgress.Downloading(0))

        val trimmedToken = githubToken.trim()
        val githubReleaseRegex = Regex("""^https?://github\.com/([^/]+)/([^/]+)/releases/download/([^/]+)/(.+)""", RegexOption.IGNORE_CASE)
        val match = githubReleaseRegex.find(url.trim())

        val (downloadUrl, extraHeaders) = if (match != null && trimmedToken.isNotBlank()) {
            val (owner, repo, tag, assetFileName) = match.destructured
            val apiReleaseUrl = "https://api.github.com/repos/$owner/$repo/releases/tags/$tag"
            val apiRequest = Request.Builder()
                .url(apiReleaseUrl)
                .header("Authorization", "Bearer $trimmedToken")
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36")
                .build()

            val assetUrl = try {
                client.newCall(apiRequest).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("GitHub API release check returned HTTP ${response.code}")
                    }
                    val jsonStr = response.body?.string() ?: ""
                    val root = org.json.JSONObject(jsonStr)
                    val assets = root.optJSONArray("assets") ?: org.json.JSONArray()
                    var foundUrl: String? = null
                    for (i in 0 until assets.length()) {
                        val asset = assets.optJSONObject(i) ?: continue
                        if (asset.optString("name").equals(assetFileName, ignoreCase = true)) {
                            foundUrl = asset.optString("url")
                            break
                        }
                    }
                    foundUrl ?: throw IOException("Asset '$assetFileName' not found in private release $tag")
                }
            } catch (e: Exception) {
                null
            }

            if (assetUrl != null) {
                Pair(assetUrl, mapOf("Authorization" to "Bearer $trimmedToken", "Accept" to "application/octet-stream"))
            } else {
                Pair(url, mapOf("Authorization" to "Bearer $trimmedToken"))
            }
        } else if (trimmedToken.isNotBlank() && url.contains("github")) {
            Pair(url, mapOf("Authorization" to "Bearer $trimmedToken"))
        } else {
            Pair(url, emptyMap())
        }

        val requestBuilder = Request.Builder()
            .url(downloadUrl)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Mobile Safari/537.36")

        extraHeaders.forEach { (key, value) ->
            requestBuilder.header(key, value)
        }

        val response = client.newCall(requestBuilder.build()).execute()

        if (!response.isSuccessful) {
            val detail = when (response.code) {
                404 -> "HTTP 404 (Not Found - verify if GitHub repository/release is private or PAT token is missing/invalid)"
                403 -> "HTTP 403 (Forbidden - access denied or rate limited)"
                else -> "HTTP ${response.code}: ${response.message}"
            }
            throw IOException(detail)
        }

        val body = response.body ?: throw IOException("Empty response body")
        val contentLength = body.contentLength()

        // Create 'apks' directory in cache
        val apkDir = File(context.cacheDir, "apks")
        if (!apkDir.exists()) {
            apkDir.mkdirs()
        }
        val destinationFile = File(apkDir, fileName)
        if (destinationFile.exists()) {
            destinationFile.delete()
        }

        val buffer = ByteArray(8192)
        var bytesRead: Int
        var totalBytesRead: Long = 0

        body.byteStream().use { inputStream ->
            FileOutputStream(destinationFile).use { outputStream ->
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    if (contentLength > 0) {
                        val progress = ((totalBytesRead * 100) / contentLength).toInt()
                        emit(DownloadProgress.Downloading(progress))
                    }
                }
            }
        }

        emit(DownloadProgress.Completed(destinationFile))
    }.flowOn(Dispatchers.IO)

    /**
     * Checks if we can request package installations.
     */
    fun canInstallApks(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true // Prior to Android 8.0, the package-level permission check is not used in this way
        }
    }

    /**
     * Directs the user to the system Settings page to enable installing unknown apps.
     */
    fun launchInstallSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    /**
     * Launches the Package Installer with the specified APK file.
     */
    fun triggerInstall(context: Context, file: File): Boolean {
        if (!file.exists()) return false

        val authority = "${context.packageName}.provider"
        val apkUri = FileProvider.getUriForFile(context, authority, file)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

sealed class DownloadProgress {
    data class Downloading(val progress: Int) : DownloadProgress()
    data class Completed(val file: File) : DownloadProgress()
}
