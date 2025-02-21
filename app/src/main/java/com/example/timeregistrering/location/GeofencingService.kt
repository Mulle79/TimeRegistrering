package com.example.timeregistrering.location

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofencingService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val powerManager: PowerManager
) {
    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    private val _geofenceStatus = MutableStateFlow<GeofenceStatus>(GeofenceStatus.Inactive)
    val geofenceStatus: StateFlow<GeofenceStatus> = _geofenceStatus.asStateFlow()

    private val retryPolicy = RetryPolicy(
        maxAttempts = 3,
        initialDelay = 1000L,
        maxDelay = 5000L,
        factor = 2.0
    )

    private fun hasRequiredPermissions(): Boolean {
        return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
               hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isBatteryOptimizationDisabled(): Boolean {
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    suspend fun addWorkplaceGeofence(
        latitude: Double,
        longitude: Double,
        radiusInMeters: Float = 100f
    ) {
        if (!hasRequiredPermissions()) {
            throw GeofencingException(
                "Manglende tilladelser. Venligst aktiver lokationstilladelser i indstillinger.",
                SecurityException("Missing required permissions")
            )
        }

        if (!isBatteryOptimizationDisabled()) {
            throw GeofencingException(
                "Batterioptimering er aktiveret. Dette kan påvirke geofencing. " +
                "Venligst deaktiver batterioptimering for appen i indstillinger.",
                Exception("Battery optimization enabled")
            )
        }

        try {
            _geofenceStatus.value = GeofenceStatus.Adding

            val geofence = Geofence.Builder()
                .setRequestId("workplace")
                .setCircularRegion(latitude, longitude, radiusInMeters)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(
                    Geofence.GEOFENCE_TRANSITION_ENTER or
                    Geofence.GEOFENCE_TRANSITION_EXIT
                )
                .setNotificationResponsiveness(5000) // 5 seconds
                .setLoiteringDelay(60000) // 1 minute
                .build()

            val request = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            retryPolicy.retry {
                try {
                    geofencingClient.addGeofences(request, geofencePendingIntent).await()
                    _geofenceStatus.value = GeofenceStatus.Active(
                        latitude = latitude,
                        longitude = longitude,
                        radius = radiusInMeters
                    )
                } catch (e: Exception) {
                    when {
                        e.message?.contains("RESOLUTION_REQUIRED") == true ->
                            throw GeofencingException("Lokationstjenester er deaktiveret. Venligst aktiver i indstillinger.", e)
                        e.message?.contains("GEOFENCE_NOT_AVAILABLE") == true ->
                            throw GeofencingException("Geofencing er ikke tilgængelig på denne enhed.", e)
                        e.message?.contains("TOO_MANY_GEOFENCES") == true ->
                            throw GeofencingException("For mange aktive geofences.", e)
                        else -> throw GeofencingException("Kunne ikke oprette geofence: ${e.message}", e)
                    }
                }
            }
        } catch (e: Exception) {
            _geofenceStatus.value = GeofenceStatus.Error(e.message ?: "Ukendt fejl")
            throw GeofencingException("Kunne ikke oprette geofence", e)
        }
    }

    suspend fun removeWorkplaceGeofence() {
        if (!hasRequiredPermissions()) {
            throw GeofencingException(
                "Manglende tilladelser. Venligst aktiver lokationstilladelser i indstillinger.",
                SecurityException("Missing required permissions")
            )
        }

        try {
            _geofenceStatus.value = GeofenceStatus.Removing
            
            retryPolicy.retry {
                geofencingClient.removeGeofences(geofencePendingIntent).await()
            }
            
            _geofenceStatus.value = GeofenceStatus.Inactive
        } catch (e: Exception) {
            _geofenceStatus.value = GeofenceStatus.Error(e.message ?: "Ukendt fejl")
            throw GeofencingException("Kunne ikke fjerne geofence", e)
        }
    }
}

sealed class GeofenceStatus {
    object Inactive : GeofenceStatus()
    object Adding : GeofenceStatus()
    object Removing : GeofenceStatus()
    data class Active(
        val latitude: Double,
        val longitude: Double,
        val radius: Float
    ) : GeofenceStatus()
    data class Error(val message: String) : GeofenceStatus()
}

class RetryPolicy(
    private val maxAttempts: Int,
    private val initialDelay: Long,
    private val maxDelay: Long,
    private val factor: Double
) {
    suspend fun <T> retry(block: suspend () -> T): T {
        var currentDelay = initialDelay
        repeat(maxAttempts - 1) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                if (attempt == maxAttempts - 1) throw e
                
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            }
        }
        return block() // Last attempt
    }
}

sealed class GeofencingException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    
    class PermissionDenied : GeofencingException("Manglende tilladelser")
    class LocationDisabled : GeofencingException("Lokationstjenester er deaktiveret")
    class GeofenceNotAvailable : GeofencingException("Geofencing er ikke tilgængelig")
    class TooManyGeofences : GeofencingException("For mange aktive geofences")
}
