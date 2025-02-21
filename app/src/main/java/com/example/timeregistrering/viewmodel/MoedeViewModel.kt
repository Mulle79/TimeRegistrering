package com.example.timeregistrering.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timeregistrering.data.database.Moede
import com.example.timeregistrering.data.database.MoedeDao
import com.example.timeregistrering.repository.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class MoedeViewModel @Inject constructor(
    private val moedeDao: MoedeDao,
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow<LocalDateTime>(LocalDateTime.now())
    val selectedDate: StateFlow<LocalDateTime> = _selectedDate.asStateFlow()

    val moeder: StateFlow<List<Moede>> = _selectedDate
        .map { date ->
            val startOfDay = date.toLocalDate().atStartOfDay()
            val endOfDay = date.toLocalDate().plusDays(1).atStartOfDay()
            moedeDao.getMoederBetweenDates(startOfDay, endOfDay)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val calendarEvents: StateFlow<List<com.google.api.services.calendar.model.Event>> = _selectedDate
        .flatMapLatest { date ->
            val startOfDay = date.toLocalDate().atStartOfDay()
            val endOfDay = date.toLocalDate().plusDays(1).atStartOfDay()
            calendarRepository.fetchEvents(startOfDay, endOfDay)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSelectedDate(date: LocalDateTime) {
        _selectedDate.value = date
    }

    fun saveMoede(moede: Moede) {
        viewModelScope.launch {
            try {
                // Create Google Calendar event
                val event = com.google.api.services.calendar.model.Event()
                    .setSummary(moede.titel)
                    .setDescription(moede.beskrivelse)
                    .setStart(com.google.api.client.util.DateTime(
                        moede.startTid.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    ))
                    .setEnd(com.google.api.client.util.DateTime(
                        moede.slutTid.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    ))

                if (!moede.lokation.isNullOrBlank()) {
                    event.location = moede.lokation
                }

                val createdEvent = calendarRepository.createEvent(event)

                // Save meeting with Google Calendar event ID
                val updatedMoede = moede.copy(
                    googleEventId = createdEvent.id,
                    erSynkroniseret = true
                )
                moedeDao.insert(updatedMoede)
            } catch (e: Exception) {
                // If Google Calendar sync fails, save meeting without event ID
                moedeDao.insert(moede.copy(erSynkroniseret = false))
                throw e
            }
        }
    }

    fun deleteMoede(moede: Moede) {
        viewModelScope.launch {
            moedeDao.delete(moede)
            
            // Delete from Google Calendar if synced
            if (moede.erSynkroniseret && moede.googleEventId != null) {
                try {
                    calendarRepository.deleteEvent(moede.googleEventId)
                } catch (e: Exception) {
                    // Log error but don't rethrow - meeting is already deleted locally
                }
            }
        }
    }

    fun updateMoede(moede: Moede) {
        viewModelScope.launch {
            try {
                if (moede.erSynkroniseret && moede.googleEventId != null) {
                    // Update Google Calendar event
                    val event = com.google.api.services.calendar.model.Event()
                        .setSummary(moede.titel)
                        .setDescription(moede.beskrivelse)
                        .setStart(com.google.api.client.util.DateTime(
                            moede.startTid.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        ))
                        .setEnd(com.google.api.client.util.DateTime(
                            moede.slutTid.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        ))

                    if (!moede.lokation.isNullOrBlank()) {
                        event.location = moede.lokation
                    }

                    calendarRepository.updateEvent(moede.googleEventId, event)
                    moedeDao.update(moede)
                } else {
                    // If not synced, try to create new event
                    saveMoede(moede)
                }
            } catch (e: Exception) {
                // If sync fails, update local only
                moedeDao.update(moede.copy(erSynkroniseret = false))
                throw e
            }
        }
    }

    fun deleteOldMoeder(dato: LocalDateTime) {
        viewModelScope.launch {
            moedeDao.deleteOldMoeder(dato)
        }
    }
}
