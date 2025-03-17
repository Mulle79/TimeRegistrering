package com.example.timeregistrering.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.timeregistrering.location.LocationManager
import com.example.timeregistrering.data.repository.TimeRegistrationRepository
import com.example.timeregistrering.data.repository.ProjectRepository
import com.example.timeregistrering.util.NotificationHelper
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.GeofenceStatusCodes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GeofenceBroadcastReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var timeRegistrationRepository: TimeRegistrationRepository
    
    @Inject
    lateinit var projectRepository: ProjectRepository
    
    @Inject
    lateinit var locationManager: LocationManager
    
    @Inject
    lateinit var notificationHelper: NotificationHelper
    
    companion object {
        private const val TAG = "GeofenceBroadcastReceiver"
        private const val DEFAULT_PROJECT_ID = "default_project"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        
        if (geofencingEvent == null) {
            Log.e(TAG, "Geofencing event er null")
            return
        }
        
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, "Geofencing fejl: $errorMessage")
            return
        }
        
        // Få geofence transition type
        val geofenceTransition = geofencingEvent.geofenceTransition
        
        // Kontroller om transitionen er en ankomst eller afgang
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.d(TAG, "Ankomst til arbejdsplads registreret")
            locationManager.updateWorkStatus(true)
            handleArrival()
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Log.d(TAG, "Afgang fra arbejdsplads registreret")
            locationManager.updateWorkStatus(false)
            handleDeparture()
        }
    }
    
    private fun handleArrival() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Hent standard projekt eller brug et default ID
                val defaultProject = projectRepository.getDefaultProject()
                val projectId = defaultProject?.id ?: DEFAULT_PROJECT_ID
                
                // Start tidsregistrering
                val registration = timeRegistrationRepository.startTimeRegistration(
                    projectId = projectId,
                    description = "Automatisk registrering ved ankomst"
                )
                
                // Vis notifikation
                notificationHelper.showArrivalNotification(
                    title = "Tidsregistrering startet",
                    message = "Du er ankommet til arbejdspladsen. Tidsregistrering er startet automatisk."
                )
                
                Log.d(TAG, "Tidsregistrering startet automatisk med ID: ${registration.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Fejl ved automatisk start af tidsregistrering", e)
                notificationHelper.showArrivalNotification(
                    title = "Fejl ved tidsregistrering",
                    message = "Der opstod en fejl ved automatisk start af tidsregistrering. Åbn appen for at registrere manuelt."
                )
            }
        }
    }
    
    private fun handleDeparture() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Find aktiv tidsregistrering og afslut den
                val currentRegistration = timeRegistrationRepository.getCurrentRegistration().firstOrNull()
                
                if (currentRegistration != null) {
                    timeRegistrationRepository.stopCurrentRegistration()
                    
                    // Vis notifikation
                    notificationHelper.showDepartureNotification(
                        title = "Tidsregistrering afsluttet",
                        message = "Du har forladt arbejdspladsen. Tidsregistrering er afsluttet automatisk."
                    )
                    
                    Log.d(TAG, "Tidsregistrering afsluttet automatisk for ID: ${currentRegistration.id}")
                } else {
                    Log.d(TAG, "Ingen aktiv tidsregistrering at afslutte")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fejl ved automatisk afslutning af tidsregistrering", e)
                notificationHelper.showDepartureNotification(
                    title = "Fejl ved tidsregistrering",
                    message = "Der opstod en fejl ved automatisk afslutning af tidsregistrering. Åbn appen for at afslutte manuelt."
                )
            }
        }
    }
}
