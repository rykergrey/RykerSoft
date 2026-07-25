package com.example.install

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
import com.example.MainActivity
import com.example.util.ApkManager

/**
 * Handles PackageInstaller session status callbacks.
 *
 * Confirmation intents are handed to [MainActivity] so they launch in the hub's task
 * (no home-screen bounce). Do not call moveTaskToBack from here.
 */
class InstallStatusReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_INSTALL_STATUS) return

        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
        val sessionId = intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, -1)
        val targetPackage = intent.getStringExtra(EXTRA_TARGET_PACKAGE)
            ?: InstallSessionTracker.awaitingPackage(context)

        Log.i(TAG, "Install status=$status session=$sessionId package=$targetPackage msg=$message")

        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirmIntent = getConfirmIntent(intent) ?: run {
                    Log.e(TAG, "PENDING_USER_ACTION missing EXTRA_INTENT")
                    return
                }
                // Bridge through MainActivity so confirmation stays in our task stack.
                val bridge = Intent(context, MainActivity::class.java).apply {
                    action = ACTION_START_CONFIRMATION
                    putExtra(EXTRA_CONFIRM_INTENT, confirmIntent)
                    putExtra(EXTRA_TARGET_PACKAGE, targetPackage)
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    )
                }
                context.startActivity(bridge)
            }

            PackageInstaller.STATUS_SUCCESS -> {
                InstallSessionTracker.clear(context)
                bringHubToFront(
                    context,
                    targetPackage = targetPackage,
                    result = RESULT_SUCCESS,
                    message = null
                )
            }

            else -> {
                if (sessionId >= 0) {
                    try {
                        context.packageManager.packageInstaller.abandonSession(sessionId)
                    } catch (_: Exception) {
                        // already finished
                    }
                }
                InstallSessionTracker.clear(context)
                val userMessage = when (status) {
                    PackageInstaller.STATUS_FAILURE_ABORTED ->
                        "Install was cancelled or blocked."
                    PackageInstaller.STATUS_FAILURE_CONFLICT -> {
                        val elsewhere = !targetPackage.isNullOrBlank() &&
                            ApkManager.packageExistsInOtherProfile(context, targetPackage)
                        if (elsewhere || !targetPackage.isNullOrBlank()) {
                            ApkManager.OTHER_PROFILE_CONFLICT_MESSAGE
                        } else {
                            "Install conflict: a package with the same name is already on this device. " +
                                "Uninstall every copy (including Island / Secure Folder / Work), then try again."
                        }
                    }
                    PackageInstaller.STATUS_FAILURE_INVALID ->
                        "Install failed: the APK is invalid or corrupt."
                    PackageInstaller.STATUS_FAILURE_STORAGE ->
                        "Install failed: not enough storage."
                    PackageInstaller.STATUS_FAILURE_INCOMPATIBLE ->
                        "Install failed: this APK is incompatible with this device."
                    else ->
                        message?.takeIf { it.isNotBlank() } ?: "Install failed (status $status)."
                }
                bringHubToFront(
                    context,
                    targetPackage = targetPackage,
                    result = RESULT_FAILURE,
                    message = userMessage
                )
            }
        }
    }

    private fun getConfirmIntent(intent: Intent): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_INTENT)
        }
    }

    private fun bringHubToFront(
        context: Context,
        targetPackage: String?,
        result: String,
        message: String?
    ) {
        val launch = Intent(context, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            )
            putExtra(EXTRA_INSTALL_RESULT, result)
            if (!targetPackage.isNullOrBlank()) {
                putExtra(EXTRA_POST_INSTALL_PACKAGE, targetPackage)
            }
            if (!message.isNullOrBlank()) {
                putExtra(EXTRA_INSTALL_MESSAGE, message)
            }
        }
        context.startActivity(launch)
    }

    companion object {
        private const val TAG = "InstallStatusReceiver"

        const val ACTION_INSTALL_STATUS = "com.rykersoft.appmanager.INSTALL_STATUS"
        const val ACTION_START_CONFIRMATION = "com.rykersoft.appmanager.START_CONFIRMATION"
        const val EXTRA_CONFIRM_INTENT = "confirm_intent"
        const val EXTRA_TARGET_PACKAGE = "target_package"
        const val EXTRA_POST_INSTALL_PACKAGE = "post_install_package"
        const val EXTRA_INSTALL_RESULT = "install_result"
        const val EXTRA_INSTALL_MESSAGE = "install_message"

        const val RESULT_SUCCESS = "success"
        const val RESULT_FAILURE = "failure"
    }
}
