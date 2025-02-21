package com.example.timeregistrering.util

import android.content.Context
import android.content.pm.PackageManager

object Constants {
    // Manifest placeholders
    fun getBaseUrl(context: Context): String {
        return context.packageManager
            .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            .metaData.getString("BASE_URL") ?: ""
    }

    fun getGoogleClientId(context: Context): String {
        return context.packageManager
            .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            .metaData.getString("GOOGLE_CLIENT_ID") ?: ""
    }
    
    // Andre konstanter
    const val DATABASE_NAME = "timeregistrering.db"
    const val PREFERENCES_NAME = "timeregistrering_prefs"
    const val SYNC_WORK_NAME = "sync_work"
    const val LOCATION_UPDATE_INTERVAL = 10000L // 10 sekunder
    const val LOCATION_FASTEST_INTERVAL = 5000L // 5 sekunder
    const val GEOFENCE_RADIUS = 100f // 100 meter
    const val NOTIFICATION_CHANNEL_ID = "timeregistrering_channel"
}
