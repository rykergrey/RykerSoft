package com.rykersoft.appmanager.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.rykersoft.appmanager.MainActivity

object NotificationHelper {
    private const val CHANNEL_ID = "app_updates_channel"
    private const val CHANNEL_NAME = "App Update Notifications"
    private const val CHANNEL_DESC = "Notifications about new versions of your managed personal apps"
    private const val NOTIFICATION_ID_BASE = 2000
    private const val ACCOUNT_CHANNEL_ID = "admin_account_channel"
    private const val ACCOUNT_NOTIFICATION_ID = 3100

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNewAccountNotification(context: Context, emails: List<String>) {
        if (emails.isEmpty()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ACCOUNT_CHANNEL_ID,
                "RykerSoft Account Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Notifications for newly created RykerSoft accounts" }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(context, ACCOUNT_NOTIFICATION_ID, intent, flags)
        val summary = if (emails.size == 1) emails.first() else "${emails.size} new accounts"
        val notification = NotificationCompat.Builder(context, ACCOUNT_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle("New RykerSoft account")
            .setContentText(summary)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(ACCOUNT_NOTIFICATION_ID, notification)
    }

    fun showUpdateNotification(context: Context, appName: String, latestVersion: String, packageName: String) {
        // Ensure channel is created
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("target_package", packageName)
        }
        
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(context, packageName.hashCode(), intent, pendingIntentFlags)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done) // Clean standard built-in icon
            .setContentTitle("Update available for $appName")
            .setContentText("Version $latestVersion is ready to install")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(packageName.hashCode() + NOTIFICATION_ID_BASE, builder.build())
    }
}
