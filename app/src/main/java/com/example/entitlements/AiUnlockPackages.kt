package com.example.entitlements

object AiUnlockPackages {
    const val SUPERTHINKING = "com.rykersoft.superthinking"
    const val BETTERTRACKING = "com.rykersoft.bettertracking"
    const val INFORMANT = "com.rykersoft.informant"

    val ALL: Set<String> = setOf(SUPERTHINKING, BETTERTRACKING, INFORMANT)

    fun isUnlockable(packageName: String): Boolean = packageName in ALL
}
