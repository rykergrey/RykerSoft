package com.rykersoft.appmanager.entitlements

import org.junit.Assert.*
import org.junit.Test

class ProCapabilityCatalogTest {
    @Test fun comicCraftingIsAvailableBeforeRemoteCatalogLoads() {
        assertTrue(AiUnlockPackages.isUnlockable("com.rykersoft.comiccrafting"))
        assertTrue(AiUnlockPackages.ORDERED.contains("com.rykersoft.comiccrafting"))
        assertEquals("ComicCraft.ing", AiUnlockPackages.displayName("com.rykersoft.comiccrafting"))
    }
    @Test fun newlyDeployedAppsDoNotRequireAnotherHubRelease() {
        assertTrue(AiUnlockPackages.isUnlockable("com.rykersoft.future", mapOf("com.rykersoft.future" to true)))
    }
    @Test fun explicitDisabledCapabilityOverridesLegacyFallback() {
        assertFalse(AiUnlockPackages.isUnlockable(AiUnlockPackages.COMICCRAFTING, mapOf(AiUnlockPackages.COMICCRAFTING to false)))
    }
    @Test fun anotherPackageDoesNotEnableThisPackage() {
        assertFalse(AiUnlockPackages.isUnlockable("com.rykersoft.unknown", mapOf(AiUnlockPackages.COMICCRAFTING to true)))
    }
}
