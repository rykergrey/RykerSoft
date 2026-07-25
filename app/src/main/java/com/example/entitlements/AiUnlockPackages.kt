package com.example.entitlements

object AiUnlockPackages {
    const val SUPERTHINKING = "com.rykersoft.superthinking"
    const val BETTERTRACKING = "com.rykersoft.bettertracking"
    const val INFORMANT = "com.rykersoft.informant"
    const val PHOTOCRAFTING = "com.rykersoft.photocrafting"

    val ALL: Set<String> = setOf(SUPERTHINKING, BETTERTRACKING, INFORMANT, PHOTOCRAFTING)

    fun isUnlockable(packageName: String): Boolean = packageName in ALL
}
