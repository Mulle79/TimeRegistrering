package com.example.timeregistrering.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.timeregistrering.MainActivity
import com.example.timeregistrering.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hjælpeklasse til at vise notifikationer i appen.
 * Bruges primært til at vise notifikationer ved ankomst og afgang fra arbejdspladsen.
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID_LOCATION = "location_channel"
        const val NOTIFICATION_ID_ARRIVAL = 1001
        const val NOTIFICATION_ID_DEPARTURE = 1002
    }
    
    init {
        createNotificationChannels()
    }
    
    /**
     * Opretter notifikationskanaler for Android 8.0 (API 26) og højere.
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val locationChannel = NotificationChannel(
                CHANNEL_ID_LOCATION,
                "Lokationsnotifikationer",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikationer ved ankomst og afgang fra arbejdspladsen"
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(locationChannel)
        }
    }
    
    /**
     * Viser en notifikation om ankomst til arbejdspladsen.
     * 
     * @param title Titlen på notifikationen
     * @param message Beskedteksten i notifikationen
     */
    fun showArrivalNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_LOCATION)
            .setSmallIcon(R.drawable.ic_time)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_ARRIVAL, notification)
        } catch (e: SecurityException) {
            // Håndter manglende tilladelse til at vise notifikationer
        }
    }
    
    /**
     * Viser en notifikation om afgang fra arbejdspladsen.
     * 
     * @param title Titlen på notifikationen
     * @param message Beskedteksten i notifikationen
     */
    fun showDepartureNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_LOCATION)
            .setSmallIcon(R.drawable.ic_time)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_DEPARTURE, notification)
        } catch (e: SecurityException) {
            // Håndter manglende tilladelse til at vise notifikationer
        }
    }
}
