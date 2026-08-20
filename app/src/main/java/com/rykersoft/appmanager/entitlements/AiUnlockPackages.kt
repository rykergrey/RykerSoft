package com.rykersoft.appmanager.entitlements

object AiUnlockPackages {
    const val SUPERTHINKING = "com.rykersoft.superthinking"
    const val BETTERTRACKING = "com.rykersoft.bettertracking"
    const val INFORMANT = "com.rykersoft.informant"
    const val PHOTOCRAFTING = "com.rykersoft.photocrafting"

    val ORDERED: List<String> = listOf(SUPERTHINKING, BETTERTRACKING, INFORMANT, PHOTOCRAFTING)
    val ALL: Set<String> = ORDERED.toSet()

    val INFORMANT_CREDENTIAL_FIELDS: List<AdminCredentialField> = listOf(
        AdminCredentialField("gemini", "Google Gemini API key", "gemini", required = false),
        AdminCredentialField("openai", "OpenAI API key", "openai", required = false),
        AdminCredentialField("youtube", "YouTube Data API key", "youtube", required = false),
        AdminCredentialField("transcript", "YouTube Transcript API key", "transcript", required = false),
        AdminCredentialField("webshare", "Webshare proxy key", "webshare", required = false),
        AdminCredentialField("elevenLabs", "ElevenLabs API key", "elevenlabs", required = false)
    )

    fun displayName(packageId: String): String = when (packageId) {
        SUPERTHINKING -> "SuperThink.ing"
        BETTERTRACKING -> "bettertracking"
        INFORMANT -> "INFORMANT"
        PHOTOCRAFTING -> "Photocraft.ing"
        else -> packageId
    }

    fun isUnlockable(packageName: String): Boolean = packageName in ALL
}
