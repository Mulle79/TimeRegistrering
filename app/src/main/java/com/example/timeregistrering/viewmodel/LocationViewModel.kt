package com.example.timeregistrering.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timeregistrering.location.LocationManager
import com.example.timeregistrering.model.WorkLocation
import com.example.timeregistrering.repository.LocationRepository
import com.example.timeregistrering.model.UiState
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationManager: LocationManager,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _locationState = MutableStateFlow<UiState<LatLng>>(UiState.Loading)
    val locationState: StateFlow<UiState<LatLng>> = _locationState

    val workLocation: StateFlow<WorkLocation?> = locationManager.workLocation
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val isAtWork: StateFlow<Boolean> = locationManager.isAtWork

    init {
        updateCurrentLocation()
    }

    private fun updateCurrentLocation() {
        viewModelScope.launch {
            try {
                _locationState.value = UiState.Loading
                val location = locationManager.getCurrentLocation()
                _locationState.value = UiState.Success(location)
            } catch (e: Exception) {
                _locationState.value = UiState.Error("Kunne ikke hente position: ${e.message}")
            }
        }
    }

    fun setWorkLocation(latitude: Double, longitude: Double, name: String = "", address: String = "", radiusInMeters: Float = 100f) {
        viewModelScope.launch {
            try {
                val workLocation = WorkLocation(
                    latitude = latitude,
                    longitude = longitude,
                    name = name,
                    address = address,
                    radiusInMeters = radiusInMeters
                )
                locationManager.setWorkLocation(workLocation)
            } catch (e: Exception) {
                // TODO: Implementer error handling UI for arbejdssted opdatering
            }
        }
    }

    fun clearWorkLocation() {
        viewModelScope.launch {
            try {
                locationRepository.clearWorkLocation()
            } catch (e: Exception) {
                // TODO: Implementer error handling UI for arbejdssted sletning
            }
        }
    }

    fun refreshLocation() {
        updateCurrentLocation()
    }

    fun checkIfAtWork() {
        viewModelScope.launch {
            try {
                val isAtWork = locationManager.isAtWorkLocation()
                locationManager.updateWorkStatus(isAtWork)
            } catch (e: Exception) {
                // Fejl ved kontrol af arbejdsplads lokation
            }
        }
    }
}
