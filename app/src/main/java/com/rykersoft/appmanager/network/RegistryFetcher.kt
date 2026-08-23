package com.rykersoft.appmanager.network

import com.rykersoft.appmanager.data.ManagedApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class RegistryFetcher(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
) {

    /**
     * Converts GitHub web 'blob' or 'raw' links to raw content links automatically.
     */
    fun sanitizeUrl(url: String): String {
        val trimmed = url.trim()
        val githubBlobRegex = Regex("""^https?://github\.com/([^/]+)/([^/]+)/(?:blob|raw)/(.+)""", RegexOption.IGNORE_CASE)
        val match = githubBlobRegex.find(trimmed)
        if (match != null) {
            val (owner, repo, path) = match.destructured
            return "https://raw.githubusercontent.com/$owner/$repo/$path"
        }
        return trimmed
    }

    /** Fetches public plain text / Markdown from a raw URL over HTTPS. */
    suspend fun fetchTextUrl(url: String): String? = withContext(Dispatchers.IO) {
        if (url.isBlank()) return@withContext null
        val targetUrl = sanitizeUrl(url)
        val requestBuilder = Request.Builder()
            .url(targetUrl)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36")
            .header("Accept", "text/plain, text/markdown, text/html, */*")
        try {
            client.newCall(requestBuilder.build()).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()?.takeIf { it.isNotBlank() }
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun fetchRegistry(url: String): List<ManagedApp> = withContext(Dispatchers.IO) {
        val targetUrl = sanitizeUrl(url)
        val timestamp = System.currentTimeMillis()
        val cacheBustUrl = if (targetUrl.contains("?")) "$targetUrl&_cb=$timestamp" else "$targetUrl?_cb=$timestamp"

        val requestBuilder = Request.Builder()
            .url(cacheBustUrl)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Mobile Safari/537.36")
            .header("Accept", "application/json, text/plain, */*")
            .header("Cache-Control", "no-cache, no-store, must-revalidate")
            .header("Pragma", "no-cache")
            .header("Expires", "0")

        val request = requestBuilder.build()

        val bodyString = try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("HTTP ${response.code}: ${response.message}")
                }
                response.body?.string() ?: throw IOException("Empty response body received from server")
            }
        } catch (e: Exception) {
            throw IOException("Network connection failed for $targetUrl: ${e.localizedMessage ?: e.message}", e)
        }

        val jsonArray = try {
            val trimmed = bodyString.trim()
            if (trimmed.startsWith("[")) {
                JSONArray(trimmed)
            } else {
                val rootObj = JSONObject(trimmed)
                rootObj.optJSONArray("apps") ?: JSONArray()
            }
        } catch (e: Exception) {
            throw IOException("Failed to parse registry JSON from response: ${e.localizedMessage ?: e.message}", e)
        }

        val resultList = mutableListOf<ManagedApp>()
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.optJSONObject(i) ?: continue
            val pkg = item.optString("packageName", "").trim()
            val name = item.optString("name", "").trim()
            if (pkg.isEmpty() || name.isEmpty()) continue

            val desc = item.optString("description", "")
            val vCode = item.optInt("latestVersionCode", item.optString("latestVersionCode", "1").toIntOrNull() ?: 1)
            val vName = item.optString("latestVersionName", "1.0.0")
            val apk = item.optString("apkUrl", "").trim()
            val exe = item.optString("exeUrl", "").trim()
            if (apk.isBlank() && exe.isBlank()) continue
            val windowsAvailable = exe.isNotBlank() ||
                pkg == "com.rykersoft.superthinking"
            val icon = item.optString("icon", "android")
            val changelog = item.optString("changelog", "")

            val screenshotsVal = if (item.has("screenshots")) {
                val rawScreenshots = item.opt("screenshots")
                if (rawScreenshots is JSONArray) {
                    val list = mutableListOf<String>()
                    for (s in 0 until rawScreenshots.length()) {
                        list.add(rawScreenshots.optString(s, ""))
                    }
                    list.joinToString(",")
                } else {
                    item.optString("screenshots", "")
                }
            } else ""

            val explicitIsGame = if (item.has("isGame")) item.optBoolean("isGame", false) else null
            val inferredIsGame = explicitIsGame ?: (
                name.contains("game", ignoreCase = true) ||
                name.contains("runner", ignoreCase = true) ||
                name.contains("puzzle", ignoreCase = true) ||
                name.contains("quest", ignoreCase = true) ||
                pkg.contains("game", ignoreCase = true)
            )

            val userGuide = item.optString("userGuide", "")
            val updatesHistory = item.optString("updatesHistory", "")
            val specs = item.optString("specs", "")

            resultList.add(
                ManagedApp(
                    packageName = pkg,
                    name = name,
                    description = desc,
                    latestVersionCode = vCode,
                    latestVersionName = vName,
                    apkUrl = apk,
                    exeUrl = exe,
                    windowsAvailable = windowsAvailable,
                    icon = icon,
                    changelog = changelog,
                    screenshots = screenshotsVal,
                    isGame = inferredIsGame,
                    userGuide = userGuide,
                    updatesHistory = updatesHistory,
                    specs = specs,
                    lastChecked = System.currentTimeMillis()
                )
            )
        }

        if (resultList.isEmpty()) {
            throw IOException("No valid applications were found in the registry JSON.")
        }

        resultList
    }
}

