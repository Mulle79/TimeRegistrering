package com.example.timeregistrering.data.repository

import android.content.Context
import android.util.Log
import com.example.timeregistrering.data.database.TimeregistreringDatabase
import com.example.timeregistrering.data.database.dao.TimeRegistrationDao
import com.example.timeregistrering.data.database.dao.PendingSyncDao
import com.example.timeregistrering.data.database.dao.WorkPeriodDao
import com.example.timeregistrering.data.database.dao.BreakDao
import com.example.timeregistrering.data.database.entity.TimeRegistrationEntity
import com.example.timeregistrering.data.database.entity.PendingSyncEntity
import com.example.timeregistrering.data.database.entity.WorkPeriodEntity
import com.example.timeregistrering.data.database.entity.BreakEntity
import com.example.timeregistrering.model.*
import com.example.timeregistrering.repository.AuthRepository
import com.example.timeregistrering.util.HolidayManager
import com.example.timeregistrering.util.NetworkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "TimeRegistrationRepository"

@Singleton
class TimeRegistrationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: TimeregistreringDatabase,
    private val networkManager: NetworkManager,
    private val authRepository: AuthRepository,
    private val holidayManager: HolidayManager
) {
    private val pendingSyncDao = database.pendingSyncDao()
    private val timeRegistrationDao = database.timeRegistrationDao()
    private val workPeriodDao = database.workPeriodDao()
    private val breakDao = database.breakDao()
    private val projectDao = database.projectDao()

    // Opret et CoroutineScope for repository
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Holder styr på registreringer der venter på at blive synkroniseret
    private val _pendingSyncRegistrations = MutableStateFlow<List<TimeRegistration>>(emptyList())
    val pendingSyncRegistrations: StateFlow<List<TimeRegistration>> = _pendingSyncRegistrations.asStateFlow()

    init {
        // Observer netværksstatus og synkroniser når online
        repositoryScope.launch {
            networkManager.observeNetworkState()
                .filter { it is NetworkManager.NetworkState.Available }
                .collect {
                    syncPendingRegistrations()
                }
        }

        // Observer bruger login status
        repositoryScope.launch {
            authRepository.isSignedIn
                .filter { it }
                .collect {
                    updatePendingSyncList()
                }
        }
    }

    suspend fun saveTimeRegistration(registration: TimeRegistration) {
        try {
            val entity = registration.toEntity()
            
            if (networkManager.isNetworkAvailable()) {
                // Online - gem direkte
                timeRegistrationDao.insert(entity)
            } else {
                // Offline - gem lokalt og marker til synkronisering
                timeRegistrationDao.insert(entity)
                pendingSyncDao.insertPendingSync(PendingSyncEntity(registrationId = registration.id))
                updatePendingSyncList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fejl ved oprettelse af tidsregistrering", e)
            throw e
        }
    }

    suspend fun getTimeRegistrationsForWeek(weekStartDate: LocalDate): List<TimeRegistration> {
        val userId = authRepository.getCurrentUser()?.id ?: throw IllegalStateException("Ingen bruger logget ind")
        val weekEndDate = weekStartDate.plusDays(6)
        
        // Konverter til LocalDateTime for at matche DAO metoden
        val startDateTime = weekStartDate.atStartOfDay()
        val endDateTime = weekEndDate.plusDays(1).atStartOfDay()
        
        val registrations = timeRegistrationDao.getRegistrationsForDateRange(
            userId = userId,
            startDate = startDateTime,
            endDate = endDateTime
        )
        
        return registrations.map { entity -> entity.toDomain() }
    }

    suspend fun getTimeRegistrationsForMonth(year: Int, month: Int): List<TimeRegistration> {
        val userId = authRepository.getCurrentUser()?.id ?: throw IllegalStateException("Ingen bruger logget ind")
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.plusMonths(1).minusDays(1)
        
        // Konverter til LocalDateTime for at matche DAO metoden
        val startDateTime = startDate.atStartOfDay()
        val endDateTime = endDate.plusDays(1).atStartOfDay()
        
        val registrations = timeRegistrationDao.getRegistrationsForDateRange(
            userId = userId,
            startDate = startDateTime,
            endDate = endDateTime
        )
        
        return registrations.map { it.toDomain() }
    }

    suspend fun startTimeRegistration(projectId: String, description: String = "", location: Location? = null): TimeRegistration {
        val userId = authRepository.getCurrentUser()?.id ?: throw IllegalStateException("Ingen bruger logget ind")
        
        val now = LocalDateTime.now()
        
        val registration = TimeRegistration(
            id = UUID.randomUUID().toString(),
            startTime = now,
            endTime = null,
            description = description,
            location = location,
            projectId = projectId,
            userId = userId
        )

        try {
            val entity = registration.toEntity()
            
            if (networkManager.isNetworkAvailable()) {
                // Online - gem direkte
                timeRegistrationDao.insert(entity)
            } else {
                // Offline - gem lokalt og marker til synkronisering
                timeRegistrationDao.insert(entity)
                pendingSyncDao.insertPendingSync(PendingSyncEntity(registrationId = registration.id))
                updatePendingSyncList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fejl ved start af tidsregistrering", e)
            throw e
        }
        
        // Start første arbejdsperiode
        val workPeriod = WorkPeriod(
            id = UUID.randomUUID().toString(),
            startTime = registration.startTime,
            endTime = null
        )
        
        workPeriodDao.insert(workPeriodToEntity(workPeriod, registration.id))

        return registration
    }

    suspend fun endTimeRegistration(registrationId: String) {
        val registration = timeRegistrationDao.getById(registrationId)
            ?: throw IllegalArgumentException("Tidsregistrering ikke fundet")
        
        val currentWorkPeriod = workPeriodDao.getCurrentWorkPeriod()
        if (currentWorkPeriod != null) {
            val now = LocalDateTime.now()
            val updatedWorkPeriod = currentWorkPeriod.copy(
                endTime = now,
                totalWorkTime = calculateWorkTime(currentWorkPeriod)
            )
            workPeriodDao.update(updatedWorkPeriod)
        }

        try {
            val now = LocalDateTime.now()
            val updatedRegistration = registration.copy(endTime = now)
            
            if (networkManager.isNetworkAvailable()) {
                // Online - opdater direkte
                timeRegistrationDao.update(updatedRegistration)
            } else {
                // Offline - gem lokalt og marker til synkronisering
                timeRegistrationDao.update(updatedRegistration)
                pendingSyncDao.insertPendingSync(PendingSyncEntity(registrationId = registration.id))
                updatePendingSyncList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fejl ved stop af tidsregistrering", e)
            throw e
        }
    }

    suspend fun startBreak(workPeriodId: String) {
        val breakObj = Break(
            id = UUID.randomUUID().toString(),
            startTime = LocalDateTime.now(),
            endTime = null
        )
        
        val breakEntity = breakToEntity(breakObj, workPeriodId)
        val breakEntityToInsert = breakEntity
        breakDao.insert(breakEntityToInsert)
    }

    suspend fun endBreak(workPeriodId: String) {
        val currentBreak = breakDao.getCurrentBreak(workPeriodId)
            ?: throw IllegalStateException("Ingen aktiv pause fundet")
        
        val now = LocalDateTime.now()
        val duration = now.toEpochSecond(ZoneOffset.UTC) - 
                      currentBreak.startTime.toEpochSecond(ZoneOffset.UTC)
        
        val updatedBreak = currentBreak.copy(
            endTime = now,
            duration = duration
        )
        
        val breakEntityToUpdate = updatedBreak
        breakDao.update(breakEntityToUpdate)
    }

    suspend fun deleteTimeRegistration(registration: TimeRegistration) {
        timeRegistrationDao.delete(registration.toEntity())
    }

    private suspend fun updatePendingSyncList() {
        val pendingSyncs = pendingSyncDao.getAllPendingSyncsSync()
        val registrations = mutableListOf<TimeRegistration>()
        
        for (pendingSync in pendingSyncs) {
            val registration = timeRegistrationDao.getById(pendingSync.registrationId)
            if (registration != null) {
                registrations.add(registration.toDomain())
            }
        }
        
        _pendingSyncRegistrations.value = registrations
    }

    private suspend fun syncPendingRegistrations() {
        try {
            val pendingSyncs = pendingSyncDao.getAllPendingSyncsSync()
            for (pendingSync in pendingSyncs) {
                try {
                    // Her ville vi normalt synkronisere med en server
                    // For nu markerer vi bare som synkroniseret
                    pendingSyncDao.delete(pendingSync)
                } catch (e: Exception) {
                    Log.e(TAG, "Fejl ved synkronisering af registrering: ${pendingSync.registrationId}", e)
                }
            }
            updatePendingSyncList()
        } catch (e: Exception) {
            Log.e(TAG, "Fejl ved synkronisering af ventende registreringer", e)
        }
    }

    private fun calculateWorkTime(workPeriod: WorkPeriodEntity): Long {
        val start = workPeriod.startTime
        val end = workPeriod.endTime ?: LocalDateTime.now()
        return end.toEpochSecond(ZoneOffset.UTC) - 
               start.toEpochSecond(ZoneOffset.UTC)
    }

    // Konverteringsmetoder mellem entiteter og domænemodeller
    private fun TimeRegistrationEntity.toDomain(): TimeRegistration {
        return TimeRegistration(
            id = id,
            startTime = startTime,
            endTime = endTime,
            description = description,
            location = if (latitude != null && longitude != null) {
                Location(latitude, longitude, address)
            } else null,
            projectId = projectId,
            userId = userId
        )
    }

    private fun TimeRegistration.toEntity(): TimeRegistrationEntity {
        return TimeRegistrationEntity(
            id = id,
            startTime = startTime,
            endTime = endTime,
            description = description,
            latitude = location?.latitude,
            longitude = location?.longitude,
            address = location?.address,
            projectId = projectId,
            userId = userId
        )
    }

    private fun workPeriodToEntity(workPeriod: WorkPeriod, registrationId: String): WorkPeriodEntity {
        val totalTime = if (workPeriod.endTime != null) {
            workPeriod.endTime.toEpochSecond(ZoneOffset.UTC) - 
            workPeriod.startTime.toEpochSecond(ZoneOffset.UTC)
        } else 0L
        
        return WorkPeriodEntity(
            id = workPeriod.id,
            timeRegistrationId = registrationId,
            startTime = workPeriod.startTime,
            endTime = workPeriod.endTime,
            totalWorkTime = totalTime
        )
    }

    private fun breakToEntity(breakObj: Break, workPeriodId: String): BreakEntity {
        val breakDuration = if (breakObj.endTime != null) {
            breakObj.endTime.toEpochSecond(ZoneOffset.UTC) - 
            breakObj.startTime.toEpochSecond(ZoneOffset.UTC)
        } else 0L
        
        return BreakEntity(
            id = breakObj.id,
            workPeriodId = workPeriodId,
            startTime = breakObj.startTime,
            endTime = breakObj.endTime,
            duration = breakDuration
        )
    }

    /**
     * Henter den aktuelle igangværende tidsregistrering, hvis der er en.
     * Bruges af GeofenceBroadcastReceiver til at afslutte tidsregistrering ved afgang fra arbejdspladsen.
     */
    suspend fun getCurrentRegistration(): TimeRegistration? {
        val userId = authRepository.getCurrentUser()?.id ?: return null
        return timeRegistrationDao.getCurrentRegistration(userId)?.toDomain()
    }

    /**
     * Stopper den aktuelle igangværende tidsregistrering.
     * Bruges af GeofenceBroadcastReceiver til at afslutte tidsregistrering ved afgang fra arbejdspladsen.
     */
    suspend fun stopCurrentRegistration() {
        val userId = authRepository.getCurrentUser()?.id ?: return
        val currentRegistration = timeRegistrationDao.getCurrentRegistration(userId) ?: return
        endTimeRegistration(currentRegistration.id)
    }
}
