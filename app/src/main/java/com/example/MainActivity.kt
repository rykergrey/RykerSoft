package com.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.install.InstallStatusReceiver
import com.example.ui.AppDashboard
import com.example.ui.theme.MyApplicationTheme
import com.example.util.NotificationHelper

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    NotificationHelper.createNotificationChannel(this)
    handleInstallBridge(intent)

    setContent {
      MyApplicationTheme {
        AppDashboard(
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleInstallBridge(intent)
  }

  /**
   * Starts PackageInstaller / Play Protect confirmation from this Activity so it stays in
   * the App Manager task (user does not get dumped to the launcher).
   */
  private fun handleInstallBridge(intent: Intent?) {
    if (intent?.action != InstallStatusReceiver.ACTION_START_CONFIRMATION) return

    val confirmIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      intent.getParcelableExtra(InstallStatusReceiver.EXTRA_CONFIRM_INTENT, Intent::class.java)
    } else {
      @Suppress("DEPRECATION")
      intent.getParcelableExtra(InstallStatusReceiver.EXTRA_CONFIRM_INTENT)
    }

    // Prevent re-processing on config change / redelivery.
    intent.action = null
    intent.removeExtra(InstallStatusReceiver.EXTRA_CONFIRM_INTENT)

    if (confirmIntent == null) {
      Log.e(TAG, "START_CONFIRMATION missing confirm intent")
      return
    }

    try {
      // Do not add NEW_TASK — keep confirmation in this task above the hub UI.
      startActivity(confirmIntent)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to start install confirmation", e)
      try {
        confirmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(confirmIntent)
      } catch (e2: Exception) {
        Log.e(TAG, "Fallback confirmation launch failed", e2)
      }
    }
  }

  companion object {
    private const val TAG = "MainActivity"
  }
}
