package com.example.timeregistrering.util

/**
 * Centrale konstanter for applikationen.
 * 
 * Dette object indeholder alle app-wide konstanter grupperet efter deres anvendelse:
 * - Database konfiguration
 * - Preferences keys
 * - WorkManager konfiguration
 * - Calendar synkronisering
 * - Location services
 * - Notifikationer
 *
 * BEMÆRK: Alle sensitive værdier (API nøgler, credentials) skal gemmes i local.properties,
 * ikke i denne fil.
 */
object Constants {
    // Database
    const val DATABASE_NAME = "timeregistrering_db"
    const val DATABASE_VERSION = 1

    // Preferences
    const val PREFERENCES_NAME = "timeregistrering_prefs"
    const val PREF_LAST_SYNC = "last_sync"
    const val PREF_USER_LOCATION = "user_location"

    // Work Manager
    const val SYNC_WORK_NAME = "sync_work"
    const val SYNC_INTERVAL_HOURS = 4L

    // Calendar
    const val CALENDAR_SYNC_PAGE_SIZE = 100
    const val EVENT_SYNC_DAYS_BACK = 30
    const val EVENT_SYNC_DAYS_FORWARD = 90

    // Location
    const val LOCATION_UPDATE_INTERVAL = 10000L // 10 seconds
    const val LOCATION_FASTEST_INTERVAL = 5000L // 5 seconds
    const val GEOFENCE_RADIUS = 100f // meters

    // Notifications
    const val NOTIFICATION_CHANNEL_ID = "timeregistrering_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Timeregistrering"
    const val SYNC_NOTIFICATION_ID = 1
    const val LOCATION_NOTIFICATION_ID = 2
}
