package com.example.entitlements

import java.security.MessageDigest

object UnlockCodeHasher {
    fun sha256Hex(rawCode: String): String {
        val normalized = rawCode.trim().lowercase()
        val digest = MessageDigest.getInstance("SHA-256").digest(normalized.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
