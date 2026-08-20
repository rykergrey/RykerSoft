package com.rykersoft.appmanager.entitlements

object AiUnlockPackages {
    const val SUPERTHINKING = "com.rykersoft.superthinking"
    const val BETTERTRACKING = "com.rykersoft.bettertracking"
    const val INFORMANT = "com.rykersoft.informant"
    const val PHOTOCRAFTING = "com.rykersoft.photocrafting"

    val ORDERED: List<String> = listOf(SUPERTHINKING, BETTERTRACKING, INFORMANT, PHOTOCRAFTING)
    val ALL: Set<String> = ORDERED.toSet()

    fun displayName(packageId: String): String = when (packageId) {
        SUPERTHINKING -> "SuperThink.ing"
        BETTERTRACKING -> "bettertracking"
        INFORMANT -> "INFORMANT"
        PHOTOCRAFTING -> "Photocraft.ing"
        else -> packageId
    }

    fun isUnlockable(packageName: String): Boolean = packageName in ALL
}
