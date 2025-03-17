package com.example.timeregistrering.model

/**
 * Model for en arbejdsplads lokation med geofencing-radius.
 */
data class WorkLocation(
    val latitude: Double,
    val longitude: Double,
    val name: String = "",
    val address: String = "",
    val radiusInMeters: Float = 100f
)
