package com.rykersoft.appmanager

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.rykersoft.appmanager.data.AppDatabase
import com.rykersoft.appmanager.data.ManagedApp
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ManagedAppDaoSyncTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `syncRemoteApps removes stale package IDs not present in remote registry`() = runBlocking {
        val dao = db.managedAppDao()

        // Insert initial apps, including an old package ID (e.g. synth app old package)
        val oldApp = ManagedApp(
            packageName = "com.rykersoft.synthapp.old",
            name = "Synth App",
            latestVersionCode = 1,
            latestVersionName = "1.0.0",
            apkUrl = "http://example.com/old.apk"
        )
        val untouchedApp = ManagedApp(
            packageName = "com.informant.app",
            name = "INFORMANT",
            latestVersionCode = 1,
            latestVersionName = "1.0.0",
            apkUrl = "http://example.com/informant.apk"
        )
        dao.insertApp(oldApp)
        dao.insertApp(untouchedApp)

        assertEquals(2, dao.getAllApps().size)
        assertNotNull(dao.getAppByPackageName("com.rykersoft.synthapp.old"))

        // Remote registry now has updated package ID for synth app
        val newSynthApp = ManagedApp(
            packageName = "com.rykersoft.synthapp.new",
            name = "Synth App",
            latestVersionCode = 2,
            latestVersionName = "1.1.0",
            apkUrl = "http://example.com/new.apk"
        )
        val remoteApps = listOf(newSynthApp, untouchedApp)

        // Perform sync
        dao.syncRemoteApps(remoteApps)

        val currentApps = dao.getAllApps()
        assertEquals(2, currentApps.size)

        // Verify old package ID was pruned and new package ID exists
        assertNull(dao.getAppByPackageName("com.rykersoft.synthapp.old"))
        assertNotNull(dao.getAppByPackageName("com.rykersoft.synthapp.new"))
        assertNotNull(dao.getAppByPackageName("com.informant.app"))
    }
}
