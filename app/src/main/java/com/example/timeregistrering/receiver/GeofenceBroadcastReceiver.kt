package com.example.timeregistrering.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.example.timeregistrering.repository.TimeRegistrationRepository
import com.example.timeregistrering.location.LocationManager
import com.example.timeregistrering.common.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class GeofenceBroadcastReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var repository: TimeRegistrationRepository

    @Inject
    lateinit var locationManager: LocationManager

    @Inject
    lateinit var notificationHelper: NotificationHelper

    companion object {
        private const val TAG = "GeofenceReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null) {
            Log.e(TAG, "Geofencing event is null")
            return
        }

        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, "Geofencing error: $errorMessage")
            return
        }

        when (geofencingEvent.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> handleArrival()
            Geofence.GEOFENCE_TRANSITION_EXIT -> handleDeparture()
        }
    }

    private fun handleArrival() {
        Log.d(TAG, "Entered work location")
        
        // Opdater lokationsstatus
        locationManager.updateWorkStatus(true)
        
        // Start tidsregistrering
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val registration = repository.startTimeRegistration(
                    projectId = "default",
                    description = "Automatisk registrering (ankomst)"
                )
                
                // Send notifikation
                val time = LocalDateTime.now().toLocalTime()
                notificationHelper.showNotification(
                    title = "Tidsregistrering startet",
                    message = "Du er ankommet til arbejdspladsen kl. $time"
                )
                
                Log.d(TAG, "Successfully started time registration: ${registration.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting time registration", e)
                notificationHelper.showNotification(
                    title = "Fejl ved tidsregistrering",
                    message = "Kunne ikke starte tidsregistrering automatisk"
                )
            }
        }
    }

    private fun handleDeparture() {
        Log.d(TAG, "Exited work location")
        
        // Opdater lokationsstatus
        locationManager.updateWorkStatus(false)
        
        // Afslut tidsregistrering
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.endCurrentTimeRegistration()
                
                // Send notifikation
                val time = LocalDateTime.now().toLocalTime()
                notificationHelper.showNotification(
                    title = "Tidsregistrering afsluttet",
                    message = "Du har forladt arbejdspladsen kl. $time"
                )
                
                Log.d(TAG, "Successfully ended time registration")
            } catch (e: Exception) {
                Log.e(TAG, "Error ending time registration", e)
                notificationHelper.showNotification(
                    title = "Fejl ved tidsregistrering",
                    message = "Kunne ikke afslutte tidsregistrering automatisk"
                )
            }
        }
    }
}
