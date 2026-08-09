package com.rykersoft.appmanager.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.net.Uri
import android.os.Build
import android.os.Process
import android.os.UserManager
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import com.rykersoft.appmanager.install.InstallSessionTracker
import com.rykersoft.appmanager.install.InstallStatusReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
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

    const val OTHER_PROFILE_CONFLICT_MESSAGE =
        "This app is still installed in another profile on this phone " +
            "(Island, Secure Folder, or a Work profile). " +
            "The main home screen can show it as not installed even when a copy exists there. " +
            "Open that profile, uninstall the app, then try again."

    const val SIGNATURE_CONFLICT_MESSAGE =
        "The installed copy was signed with a different key, so Android cannot update it. " +
            "Uninstall the existing copy from every profile, then install this release again. " +
            "Uninstalling clears that app's local data."

    /**
     * Get details of an installed app by package name (current user/profile only).
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
     * Validates the downloaded APK before opening PackageInstaller so signature, package-name,
     * and downgrade failures can be explained instead of surfacing as "App not installed".
     */
    fun validateApkForInstall(context: Context, file: File, targetPackage: String): String? {
        if (!file.exists() || file.length() <= 0L) return "Install failed: the downloaded APK is empty."

        val packageManager = context.packageManager
        val archiveInfo = getArchivePackageInfo(packageManager, file)
            ?: return "Install failed: the downloaded APK is invalid or corrupt."
        if (archiveInfo.packageName != targetPackage) {
            return "Install blocked: the APK contains ${archiveInfo.packageName}, but the hub expected $targetPackage."
        }

        val installedInfo = getInstalledPackageInfo(packageManager, targetPackage) ?: return null
        val installedSigners = signingCertificates(installedInfo)
        val archiveSigners = signingCertificates(archiveInfo)
        if (installedSigners.isEmpty() || archiveSigners.isEmpty()) {
            return "Install failed: Android could not verify the APK signing certificate."
        }
        if (installedSigners.intersect(archiveSigners).isEmpty()) {
            return SIGNATURE_CONFLICT_MESSAGE
        }

        val installedVersion = versionCode(installedInfo)
        val archiveVersion = versionCode(archiveInfo)
        if (archiveVersion < installedVersion) {
            return "Install blocked: version code $archiveVersion is older than the installed version code $installedVersion."
        }
        return null
    }

    private fun getArchivePackageInfo(packageManager: PackageManager, file: File): PackageInfo? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageArchiveInfo(
                file.absolutePath,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageArchiveInfo(
                file.absolutePath,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    PackageManager.GET_SIGNING_CERTIFICATES
                } else {
                    PackageManager.GET_SIGNATURES
                }
            )
        }
    }

    private fun getInstalledPackageInfo(packageManager: PackageManager, packageName: String): PackageInfo? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(
                    packageName,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        PackageManager.GET_SIGNING_CERTIFICATES
                    } else {
                        PackageManager.GET_SIGNATURES
                    }
                )
            }
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }

    private fun signingCertificates(packageInfo: PackageInfo): Set<String> {
        val signatures: Array<Signature> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val signingInfo = packageInfo.signingInfo ?: return emptySet()
            if (signingInfo.hasMultipleSigners()) {
                signingInfo.apkContentsSigners
            } else {
                signingInfo.signingCertificateHistory
            }
        } else {
            @Suppress("DEPRECATION")
            packageInfo.signatures ?: emptyArray()
        }
        return signatures.mapTo(linkedSetOf()) { it.toCharsString() }
    }

    private fun versionCode(packageInfo: PackageInfo): Long =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }

    /**
     * True when [packageName] is present in a related profile (e.g. Island / work) but may be
     * absent from the current profile — which causes PackageInstaller CONFLICT on install.
     */
    fun packageExistsInOtherProfile(context: Context, packageName: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        val userManager = context.getSystemService(UserManager::class.java) ?: return false
        val launcherApps = context.getSystemService(LauncherApps::class.java) ?: return false
        val myUser = Process.myUserHandle()
        for (profile in userManager.userProfiles) {
            if (profile == myUser) continue
            try {
                launcherApps.getApplicationInfo(packageName, 0, profile)
                return true
            } catch (_: Exception) {
                // Not installed in this profile, or profile not queryable.
            }
        }
        return false
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

    /** Downloads a public APK, yielding percentage progress and then the completed file. */
    fun downloadApk(context: Context, url: String, fileName: String): Flow<DownloadProgress> = flow {
        emit(DownloadProgress.Downloading(0))

        val requestBuilder = Request.Builder()
            .url(url.trim())
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Mobile Safari/537.36")

        val response = client.newCall(requestBuilder.build()).execute()

        if (!response.isSuccessful) {
            val detail = when (response.code) {
                404 -> "HTTP 404 (Not Found - verify the public release URL)"
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
     * Legacy ACTION_VIEW install path. Prefer [installViaSession] so Play Protect stays interactable.
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

    /**
     * Abandons any PackageInstaller sessions owned by this app so a previous stuck
     * Play Protect / confirmation dialog cannot block the next install forever.
     */
    fun abandonOwnedSessions(context: Context) {
        val installer = context.packageManager.packageInstaller
        for (session in installer.mySessions) {
            try {
                installer.abandonSession(session.sessionId)
                Log.i(TAG, "Abandoned stale session ${session.sessionId}")
            } catch (e: Exception) {
                Log.w(TAG, "Could not abandon session ${session.sessionId}: ${e.message}")
            }
        }
        InstallSessionTracker.clear(context)
    }

    /**
     * Installs [file] via [PackageInstaller] sessions. Status (including Play Protect
     * confirmation) is delivered to [InstallStatusReceiver].
     *
     * @return session id on success, or null if the session could not be created/committed
     */
    suspend fun installViaSession(context: Context, file: File, targetPackage: String): Int? =
        withContext(Dispatchers.IO) {
            if (!file.exists() || file.length() <= 0L) return@withContext null

            abandonOwnedSessions(context)

            val installer = context.packageManager.packageInstaller
            val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
                setAppPackageName(targetPackage)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setInstallReason(PackageManager.INSTALL_REASON_USER)
                }
                // Force the system confirmation / Play Protect path through our wrapper Activity
                // so the prompt cannot be buried under App Manager UI.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_REQUIRED)
                }
            }

            var sessionId = -1
            try {
                sessionId = installer.createSession(params)
                installer.openSession(sessionId).use { session ->
                    session.openWrite("base.apk", 0, file.length()).use { out ->
                        file.inputStream().use { input -> input.copyTo(out) }
                        session.fsync(out)
                    }

                    val callbackIntent = Intent(context, InstallStatusReceiver::class.java).apply {
                        action = InstallStatusReceiver.ACTION_INSTALL_STATUS
                        setPackage(context.packageName)
                        putExtra(InstallStatusReceiver.EXTRA_TARGET_PACKAGE, targetPackage)
                    }
                    val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            PendingIntent.FLAG_MUTABLE
                        } else {
                            0
                        }
                    val pending = PendingIntent.getBroadcast(
                        context,
                        sessionId,
                        callbackIntent,
                        flags
                    )
                    InstallSessionTracker.setAwaiting(context, targetPackage, sessionId)
                    session.commit(pending.intentSender)
                }
                Log.i(TAG, "Committed install session $sessionId for $targetPackage")
                sessionId
            } catch (e: Exception) {
                Log.e(TAG, "installViaSession failed", e)
                if (sessionId >= 0) {
                    try {
                        installer.abandonSession(sessionId)
                    } catch (_: Exception) {
                    }
                }
                InstallSessionTracker.clear(context)
                null
            }
        }

    private const val TAG = "ApkManager"
}

sealed class DownloadProgress {
    data class Downloading(val progress: Int) : DownloadProgress()
    data class Completed(val file: File) : DownloadProgress()
}
