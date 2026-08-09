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
    @ColumnInfo(defaultValue = "0") val windowsAvailable: Boolean = false,
    val icon: String = "android", // Default icon keyword
    val changelog: String = "",
    val screenshots: String = "", // Comma-separated image URLs or empty
    val isGame: Boolean = false, // True for Games, False for Apps
    val userGuide: String = "",
    val updatesHistory: String = "",
    val specs: String = "",
    val lastChecked: Long = System.currentTimeMillis()
)
