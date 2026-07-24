package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ManagedAppDao {
    @Query("SELECT * FROM managed_apps ORDER BY name ASC")
    fun getAllAppsFlow(): Flow<List<ManagedApp>>

    @Query("SELECT * FROM managed_apps ORDER BY name ASC")
    suspend fun getAllApps(): List<ManagedApp>

    @Query("SELECT * FROM managed_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getAppByPackageName(packageName: String): ManagedApp?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<ManagedApp>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: ManagedApp)

    @Query("DELETE FROM managed_apps WHERE packageName = :packageName")
    suspend fun deleteAppByPackage(packageName: String)

    @Query("DELETE FROM managed_apps WHERE packageName NOT IN (:validPackageNames)")
    suspend fun deleteAppsNotIn(validPackageNames: List<String>)

    @Query("DELETE FROM managed_apps")
    suspend fun clearAll()

    @Transaction
    suspend fun syncRemoteApps(remoteApps: List<ManagedApp>) {
        if (remoteApps.isNotEmpty()) {
            val remotePackageNames = remoteApps.map { it.packageName }
            deleteAppsNotIn(remotePackageNames)
            insertApps(remoteApps)
        }
    }
}

