package com.rykersoft.appmanager.entitlements

import org.junit.Assert.assertEquals
import org.junit.Test

class HyperscribeProviderKeysTest {
    @Test fun `desktop and mobile administration share one provider record`() {
        val mobile = "com.rykersoft.hyperscribemobile"
        assertEquals(mobile, providerKeyPackage("com.rykersoft.hyperscribedesktop"))
        assertEquals(mobile, providerKeyPackage(mobile))
        assertEquals("other.app", providerKeyPackage("other.app"))
    }
}
