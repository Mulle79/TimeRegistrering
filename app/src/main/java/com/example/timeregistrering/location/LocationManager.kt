package com.example.timeregistrering.location

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.timeregistrering.model.WorkLocation
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPreferences: SharedPreferences,
    private val geofencingService: GeofencingService
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    
    private val _workLocation = MutableStateFlow<WorkLocation?>(null)
    val workLocation = _workLocation.asStateFlow()
    
    private val _isAtWork = MutableStateFlow(false)
    val isAtWork = _isAtWork.asStateFlow()
    
    companion object {
        private const val TAG = "LocationManager"
        private const val PREFS_WORK_LAT = "work_latitude"
        private const val PREFS_WORK_LNG = "work_longitude"
        private const val PREFS_WORK_NAME = "work_name"
        private const val PREFS_WORK_ADDRESS = "work_address"
        private const val PREFS_WORK_RADIUS = "work_radius"
    }
    
    init {
        loadWorkLocation()
    }
    
    private fun loadWorkLocation() {
        val lat = sharedPreferences.getFloat(PREFS_WORK_LAT, 0f)
        val lng = sharedPreferences.getFloat(PREFS_WORK_LNG, 0f)
        if (lat != 0f && lng != 0f) {
            val name = sharedPreferences.getString(PREFS_WORK_NAME, "") ?: ""
            val address = sharedPreferences.getString(PREFS_WORK_ADDRESS, "") ?: ""
            val radius = sharedPreferences.getFloat(PREFS_WORK_RADIUS, 100f)
            
            val workLocation = WorkLocation(
                latitude = lat.toDouble(),
                longitude = lng.toDouble(),
                name = name,
                address = address,
                radiusInMeters = radius
            )
            
            _workLocation.value = workLocation
            setupGeofencing(workLocation)
        }
    }
    
    suspend fun setWorkLocation(workLocation: WorkLocation) {
        sharedPreferences.edit()
            .putFloat(PREFS_WORK_LAT, workLocation.latitude.toFloat())
            .putFloat(PREFS_WORK_LNG, workLocation.longitude.toFloat())
            .putString(PREFS_WORK_NAME, workLocation.name)
            .putString(PREFS_WORK_ADDRESS, workLocation.address)
            .putFloat(PREFS_WORK_RADIUS, workLocation.radiusInMeters)
            .apply()
        
        _workLocation.value = workLocation
        setupGeofencing(workLocation)
    }
    
    private fun setupGeofencing(workLocation: WorkLocation) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Mangler tilladelse til præcis lokation")
            return
        }
        
        try {
            // Brug GeofencingService til at håndtere geofences
            geofencingService.addWorkplaceGeofence(
                latitude = workLocation.latitude,
                longitude = workLocation.longitude,
                radiusInMeters = workLocation.radiusInMeters
            )
        } catch (e: Exception) {
            Log.e(TAG, "Fejl ved oprettelse af geofence", e)
        }
    }
    
    fun updateWorkStatus(isAtWork: Boolean) {
        _isAtWork.value = isAtWork
    }
    
    suspend fun getCurrentLocation(): LatLng = suspendCancellableCoroutine { continuation ->
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            continuation.resumeWithException(SecurityException("Mangler tilladelse til præcis lokation"))
            return@suspendCancellableCoroutine
        }
        
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    continuation.resume(LatLng(it.latitude, it.longitude))
                } ?: continuation.resumeWithException(Exception("Kunne ikke hente lokation"))
            }
            .addOnFailureListener { e ->
                continuation.resumeWithException(e)
            }
    }
    
    suspend fun isAtWorkLocation(): Boolean {
        val workLoc = _workLocation.value ?: return false
        
        try {
            val currentLocation = getCurrentLocation()
            
            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                currentLocation.latitude,
                currentLocation.longitude,
                workLoc.latitude,
                workLoc.longitude,
                results
            )
            
            return results[0] <= workLoc.radiusInMeters
        } catch (e: Exception) {
            Log.e(TAG, "Fejl ved kontrol af arbejdsplads lokation", e)
            return false
        }
    }
}
