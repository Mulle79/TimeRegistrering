package com.example.timeregistrering.repository

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import com.example.timeregistrering.model.WorkLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    
    private val _workLocation = MutableStateFlow<WorkLocation?>(null)
    val workLocation: Flow<WorkLocation?> = _workLocation.asStateFlow()

    companion object {
        private const val PREF_WORK_LOCATION = "work_location"
    }

    init {
        loadWorkLocation()
    }

    // Current Location funktioner
    suspend fun getCurrentLocation(): LatLng = suspendCancellableCoroutine { continuation ->
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        continuation.resume(LatLng(it.latitude, it.longitude))
                    } ?: continuation.resumeWithException(Exception("Location not available"))
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        } catch (e: SecurityException) {
            continuation.resumeWithException(e)
        }
    }

    // Work Location funktioner
    private fun loadWorkLocation() {
        val json = sharedPreferences.getString(PREF_WORK_LOCATION, null)
        if (json != null) {
            _workLocation.value = gson.fromJson(json, WorkLocation::class.java)
        }
    }

    fun saveWorkLocation(location: WorkLocation) {
        val json = gson.toJson(location)
        sharedPreferences.edit()
            .putString(PREF_WORK_LOCATION, json)
            .apply()
        _workLocation.value = location
    }

    fun clearWorkLocation() {
        sharedPreferences.edit()
            .remove(PREF_WORK_LOCATION)
            .apply()
        _workLocation.value = null
    }

    suspend fun isAtWorkLocation(): Boolean {
        val currentLocation = getCurrentLocation()
        val workLoc = _workLocation.value ?: return false
        
        val results = FloatArray(1)
        Location.distanceBetween(
            currentLocation.latitude,
            currentLocation.longitude,
            workLoc.latitude,
            workLoc.longitude,
            results
        )
        
        return results[0] <= workLoc.radiusInMeters
    }
}
