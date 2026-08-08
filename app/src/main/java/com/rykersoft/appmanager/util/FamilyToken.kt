package com.rykersoft.appmanager.util

import com.rykersoft.appmanager.BuildConfig

/**
 * Family GitHub access token baked into the build from .env (FAMILY_GITHUB_TOKEN).
 * Grants read-only access to the private RykerSoft-APKs distribution repo so
 * users don't have to paste a token manually. A token entered in Settings
 * always takes priority over this value.
 */
object FamilyToken {
    fun baked(): String {
        val value = BuildConfig.FAMILY_GITHUB_TOKEN.trim()
        return if (value.isBlank() || value.startsWith("REPLACE_")) "" else value
    }
}
