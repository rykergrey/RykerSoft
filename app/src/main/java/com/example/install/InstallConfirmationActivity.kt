package com.example.install

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.example.MainActivity

/**
 * Thin host for PackageInstaller / Play Protect confirmation UI.
 *
 * Starting the system confirmation Intent from a dedicated Activity (instead of from
 * App Manager's Compose Dialog window) keeps Play Protect tappable. If the user dismisses
 * the confirmation by tapping outside, the session can otherwise stick forever — we detect
 * that on a subsequent resume and abandon the session.
 */
class InstallConfirmationActivity : Activity() {

    private var sessionId: Int = -1
    private var confirmationLaunched = false
    private var receivedActivityResult = false
    private var isFirstResume = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionId = intent.getIntExtra(EXTRA_SESSION_ID, -1)

        val confirmIntent = getConfirmIntent()
        if (confirmIntent == null) {
            Log.e(TAG, "Missing confirmation intent")
            abandonSession()
            finish()
            return
        }

        try {
            @Suppress("DEPRECATION")
            startActivityForResult(confirmIntent, REQUEST_CONFIRM)
            confirmationLaunched = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch confirmation UI", e)
            abandonSession()
            notifyCancelled("Failed to open install confirmation: ${e.localizedMessage}")
            finish()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_CONFIRM) return
        receivedActivityResult = true
        // Final success/failure is delivered to InstallStatusReceiver; just close the host.
        finish()
    }

    override fun onResume() {
        super.onResume()
        if (!confirmationLaunched) return
        if (isFirstResume) {
            isFirstResume = false
            return
        }
        // Second resume without onActivityResult ≈ user dismissed confirmation by tapping outside.
        if (!receivedActivityResult) {
            Log.w(TAG, "Confirmation dismissed without result; abandoning session $sessionId")
            abandonSession()
            notifyCancelled()
            finish()
        }
    }

    private fun getConfirmIntent(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_CONFIRM_INTENT, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_CONFIRM_INTENT)
        }
    }

    private fun abandonSession() {
        if (sessionId < 0) return
        try {
            packageManager.packageInstaller.abandonSession(sessionId)
        } catch (_: Exception) {
            // Session may already be finished.
        }
        InstallSessionTracker.clear(this)
    }

    private fun notifyCancelled(message: String = "Install was cancelled.") {
        val target = intent.getStringExtra(InstallStatusReceiver.EXTRA_TARGET_PACKAGE)
            ?: InstallSessionTracker.awaitingPackage(this)
        val launch = Intent(this, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            )
            putExtra(InstallStatusReceiver.EXTRA_INSTALL_RESULT, InstallStatusReceiver.RESULT_FAILURE)
            putExtra(InstallStatusReceiver.EXTRA_INSTALL_MESSAGE, message)
            if (!target.isNullOrBlank()) {
                putExtra(InstallStatusReceiver.EXTRA_POST_INSTALL_PACKAGE, target)
            }
        }
        startActivity(launch)
    }

    companion object {
        private const val TAG = "InstallConfirm"
        private const val REQUEST_CONFIRM = 4401

        const val EXTRA_CONFIRM_INTENT = "confirm_intent"
        const val EXTRA_SESSION_ID = "session_id"
    }
}
