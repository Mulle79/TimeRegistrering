package com.example.timeregistrering.model

data class WorkLocation(
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val address: String,
    val radius: Float = 100f
)
