package com.rykersoft.appmanager.data

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

@Entity(tableName = "managed_apps")
data class ManagedApp(
    @PrimaryKey val packageName: String,
    val name: String,
    val description: String = "",
    val latestVersionCode: Int,
    val latestVersionName: String,
    val apkUrl: String,
    @ColumnInfo(defaultValue = "") val exeUrl: String = "",
    @ColumnInfo(defaultValue = "") val linuxUrl: String = "",
    @ColumnInfo(defaultValue = "0") val windowsAvailable: Boolean = false,
    val icon: String = "android", // Default icon keyword
    val changelog: String = "",
    val screenshots: String = "", // Comma-separated image URLs or empty
    val isGame: Boolean = false, // True for Games, False for Apps
    val userGuide: String = "",
    val updatesHistory: String = "",
    val specs: String = "",
    /** Catalog id. Entries that share one are the same product on different platforms. */
    @ColumnInfo(defaultValue = "") val productId: String = "",
    /** One sentence, shown in lists. */
    @ColumnInfo(defaultValue = "") val tagline: String = "",
    /** Newline-separated feature list. */
    @ColumnInfo(defaultValue = "") val features: String = "",
    /** What Pro adds. Blank means the whole app is free. */
    @ColumnInfo(defaultValue = "") val pro: String = "",
    @ColumnInfo(defaultValue = "") val iconUrl: String = "",
    /** ISO date of the latest release, yyyy-MM-dd. */
    @ColumnInfo(defaultValue = "") val releasedAt: String = "",
    @ColumnInfo(defaultValue = "") val linuxDebUrl: String = "",
    @ColumnInfo(defaultValue = "") val linuxPacmanUrl: String = "",
    val lastChecked: Long = System.currentTimeMillis()
)
