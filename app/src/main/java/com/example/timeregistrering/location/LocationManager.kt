package com.example.timeregistrering.location

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPreferences: SharedPreferences
) {
    private val geofencingClient = LocationServices.getGeofencingClient(context)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    
    private val _workLocation = MutableStateFlow<Location?>(null)
    val workLocation = _workLocation.asStateFlow()
    
    private val _isAtWork = MutableStateFlow(false)
    val isAtWork = _isAtWork.asStateFlow()
    
    companion object {
        private const val TAG = "LocationManager"
        private const val GEOFENCE_RADIUS_METERS = 100f
        private const val GEOFENCE_ID = "work_location"
        private const val PREFS_WORK_LAT = "work_latitude"
        private const val PREFS_WORK_LNG = "work_longitude"
    }
    
    init {
        loadWorkLocation()
    }
    
    private fun loadWorkLocation() {
        val lat = sharedPreferences.getFloat(PREFS_WORK_LAT, 0f)
        val lng = sharedPreferences.getFloat(PREFS_WORK_LNG, 0f)
        if (lat != 0f && lng != 0f) {
            val location = Location("").apply {
                latitude = lat.toDouble()
                longitude = lng.toDouble()
            }
            _workLocation.value = location
            setupGeofencing(location)
        }
    }
    
    fun setWorkLocation(latitude: Double, longitude: Double) {
        val location = Location("").apply {
            this.latitude = latitude
            this.longitude = longitude
        }
        
        sharedPreferences.edit()
            .putFloat(PREFS_WORK_LAT, latitude.toFloat())
            .putFloat(PREFS_WORK_LNG, longitude.toFloat())
            .apply()
        
        _workLocation.value = location
        setupGeofencing(location)
    }
    
    private fun setupGeofencing(location: Location) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Mangler tilladelse til præcis lokation")
            return
        }
        
        val geofence = Geofence.Builder()
            .setRequestId(GEOFENCE_ID)
            .setCircularRegion(
                location.latitude,
                location.longitude,
                GEOFENCE_RADIUS_METERS
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or
                Geofence.GEOFENCE_TRANSITION_EXIT
            )
            .build()
        
        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
        
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        
        geofencingClient.removeGeofences(pendingIntent).addOnCompleteListener {
            geofencingClient.addGeofences(request, pendingIntent)
                .addOnSuccessListener {
                    Log.d(TAG, "Geofence oprettet for arbejdsplads")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Fejl ved oprettelse af geofence", e)
                }
        }
    }
    
    fun updateWorkStatus(isAtWork: Boolean) {
        _isAtWork.value = isAtWork
    }
    
    fun getCurrentLocation(onLocation: (Location) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Mangler tilladelse til præcis lokation")
            return
        }
        
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let(onLocation)
            }
    }
}
