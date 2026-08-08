package com.rykersoft.appmanager.install

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log

/**
 * Launches the system PackageInstaller / Play Protect confirmation UI, then finishes.
 *
 * Important: do NOT use startActivityForResult + resume-based cancel here. Play Protect
 * causes lifecycle blips that look like "user dismissed", which previously abandoned the
 * session and brought MainActivity back on top — burying Play Protect instantly.
 */
class InstallConfirmationActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val confirmIntent = getConfirmIntent()
        if (confirmIntent == null) {
            Log.e(TAG, "Missing confirmation intent")
            finish()
            return
        }

        try {
            confirmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(confirmIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch confirmation UI", e)
        } finally {
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

    companion object {
        private const val TAG = "InstallConfirm"
        const val EXTRA_CONFIRM_INTENT = "confirm_intent"
        const val EXTRA_SESSION_ID = "session_id"
    }
}
