package com.example.install

import android.content.Context

/**
 * Persists the in-flight install target so status callbacks can recover after process death
 * and so the UI can reopen the User Guide after success.
 */
object InstallSessionTracker {
    private const val PREFS = "install_session_prefs"
    private const val KEY_PACKAGE = "awaiting_package"
    private const val KEY_SESSION_ID = "awaiting_session_id"
    private const val KEY_YIELDED_SESSION = "yielded_session_id"

    fun setAwaiting(context: Context, packageName: String, sessionId: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PACKAGE, packageName)
            .putInt(KEY_SESSION_ID, sessionId)
            .remove(KEY_YIELDED_SESSION)
            .apply()
    }

    fun awaitingPackage(context: Context): String? {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_PACKAGE, null)
            ?.takeIf { it.isNotBlank() }
    }

    fun awaitingSessionId(context: Context): Int {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_SESSION_ID, -1)
    }

    /**
     * @return true the first time we should yield for this session; false on later prompts
     * (e.g. Play Protect after the install consent dialog).
     */
    fun markYieldedIfNeeded(context: Context, sessionId: Int): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val already = prefs.getInt(KEY_YIELDED_SESSION, Int.MIN_VALUE)
        if (sessionId >= 0 && already == sessionId) return false
        prefs.edit().putInt(KEY_YIELDED_SESSION, sessionId).apply()
        return true
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_PACKAGE)
            .remove(KEY_SESSION_ID)
            .remove(KEY_YIELDED_SESSION)
            .apply()
    }
}
