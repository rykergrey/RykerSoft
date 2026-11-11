package com.rykersoft.appmanager.network

import java.io.File
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

/** Parses the repository's real registry.json, so a catalog edit that breaks the hub fails here. */
@RunWith(RobolectricTestRunner::class)
class RegistryContentFieldsTest {
    private fun clientFor(json: String) = OkHttpClient.Builder()
        .addInterceptor { chain ->
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(200).message("OK")
                .body(json.toResponseBody("application/json".toMediaType())).build()
        }
        .build()

    @Test
    fun `the live catalog parses and every entry has user-facing content`() = runTest {
        val client = clientFor(File("../registry.json").readText())
        try {
            val apps = RegistryFetcher(client).fetchRegistry("https://example.invalid/registry.json")

            assertTrue("expected the whole catalog, got ${apps.size}", apps.size >= 21)
            apps.forEach { app ->
                assertTrue("${app.name}: tagline", app.tagline.isNotBlank())
                assertTrue("${app.name}: description", app.description.isNotBlank())
                assertTrue("${app.name}: features", app.features.lines().size >= 3)
                assertTrue("${app.name}: icon", app.iconUrl.endsWith("/icons/${app.productId}.png"))
                assertTrue("${app.name}: release date", Regex("""\d{4}-\d{2}-\d{2}""").matches(app.releasedAt))
                assertTrue("${app.name}: what's new", app.changelog.isNotBlank())
                assertTrue("${app.name}: history starts at the current version", app.updatesHistory.startsWith("## ${app.latestVersionName}"))
                assertTrue("${app.name}: no user guide is published", app.userGuide.isBlank())
                assertTrue("${app.name}: icon file exists", File("../icons/${app.productId}.png").isFile)
            }
            // yoink is one product listed as two entries: Android and desktop.
            assertEquals(listOf("app.flux.download", "com.rykersoft.yoink"), apps.filter { it.productId == "yoink" }.map { it.packageName }.sorted())
        } finally {
            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
        }
    }

    @Test
    fun `extra linux packages are kept`() = runTest {
        val client = clientFor("""[{"packageName":"a.b","name":"A","latestVersionName":"1.0.0","linuxDebUrl":"https://x.invalid/download/a-v1.0.0/a.deb","features":["one","two"]}]""")
        try {
            val app = RegistryFetcher(client).fetchRegistry("https://example.invalid/registry.json").single()
            assertEquals("https://x.invalid/download/a-v1.0.0/a.deb", app.linuxDebUrl)
            assertEquals("one\ntwo", app.features)
        } finally {
            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
        }
    }
}
