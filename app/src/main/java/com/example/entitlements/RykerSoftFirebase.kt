package com.example.entitlements

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object RykerSoftFirebase {
    private const val TAG = "RykerSoftFirebase"
    const val APP_NAME = "rykersoft-hub"

    @Volatile
    private var ready: Boolean = false

    fun isConfigured(): Boolean {
        return listOf(
            BuildConfig.FIREBASE_API_KEY,
            BuildConfig.FIREBASE_APP_ID,
            BuildConfig.FIREBASE_PROJECT_ID,
            BuildConfig.FIREBASE_MESSAGING_SENDER_ID
        ).all { it.isNotBlank() && !it.startsWith("REPLACE_") && it != "MY_GEMINI_API_KEY" }
    }

    fun ensureInitialized(context: Context): Boolean {
        if (ready) return true
        if (!isConfigured()) {
            Log.w(TAG, "Firebase env not configured; AI unlock disabled until .env is filled.")
            return false
        }
        synchronized(this) {
            if (ready) return true
            val existing = FirebaseApp.getApps(context).find { it.name == APP_NAME }
            if (existing == null) {
                val options = FirebaseOptions.Builder()
                    .setApiKey(BuildConfig.FIREBASE_API_KEY.trim())
                    .setApplicationId(BuildConfig.FIREBASE_APP_ID.trim())
                    .setProjectId(BuildConfig.FIREBASE_PROJECT_ID.trim())
                    .setStorageBucket(BuildConfig.FIREBASE_STORAGE_BUCKET.trim().ifBlank {
                        "${BuildConfig.FIREBASE_PROJECT_ID.trim()}.appspot.com"
                    })
                    .setGcmSenderId(BuildConfig.FIREBASE_MESSAGING_SENDER_ID.trim())
                    .build()
                FirebaseApp.initializeApp(context.applicationContext, options, APP_NAME)
            }
            ready = true
        }
        return true
    }

    fun app(context: Context): FirebaseApp? {
        if (!ensureInitialized(context)) return null
        return FirebaseApp.getInstance(APP_NAME)
    }

    fun auth(context: Context): FirebaseAuth? = app(context)?.let { FirebaseAuth.getInstance(it) }

    fun db(context: Context): FirebaseFirestore? = app(context)?.let { FirebaseFirestore.getInstance(it) }
}
