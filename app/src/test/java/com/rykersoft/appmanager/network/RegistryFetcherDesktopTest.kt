package com.rykersoft.appmanager.network

import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RegistryFetcherDesktopTest {
    @Test
    fun `exe-only entries remain separate valid catalog apps`() = runTest {
        val json = """
            [
              {
                "packageName": "com.rykersoft.hyperscribedesktop",
                "name": "Hyperscribe Desktop",
                "latestVersionCode": 1,
                "latestVersionName": "2.1.0",
                "exeUrl": "https://github.com/rykergrey/RykerSoft-APKs/releases/download/hyperscribe-desktop-v2.1.0/HyperscribeDesktop-v2.1.0.exe"
              },
              {
                "packageName": "com.rykersoft.invalid",
                "name": "No Artifact"
              }
            ]
        """.trimIndent()
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(json.toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()

        try {
            val apps = RegistryFetcher(client).fetchRegistry("https://example.invalid/registry.json")

            assertEquals(1, apps.size)
            assertEquals("com.rykersoft.hyperscribedesktop", apps.single().packageName)
            assertTrue(apps.single().apkUrl.isBlank())
            assertTrue(apps.single().exeUrl.endsWith("HyperscribeDesktop-v2.1.0.exe"))
            assertTrue(apps.single().windowsAvailable)
        } finally {
            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
        }
    }
}
