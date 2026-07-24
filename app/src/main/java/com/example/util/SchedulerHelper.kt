package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.receiver.UpdateCheckReceiver

object SchedulerHelper {
    private const val REQUEST_CODE = 4001

    fun schedulePeriodicCheck(context: Context, enabled: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, UpdateCheckReceiver::class.java)
        
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags)

        if (enabled) {
            // Schedule check every 6 hours
            val interval = AlarmManager.INTERVAL_HALF_DAY / 2 // 6 hours
            val triggerAt = System.currentTimeMillis() + interval
            try {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    interval,
                    pendingIntent
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            alarmManager.cancel(pendingIntent)
        }
    }
}
