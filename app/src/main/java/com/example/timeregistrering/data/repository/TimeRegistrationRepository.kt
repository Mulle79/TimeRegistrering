package com.example.timeregistrering.data.repository

import android.content.Context
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.*
import com.example.timeregistrering.data.database.TimeregistreringDatabase
import com.example.timeregistrering.data.database.dao.*
import com.example.timeregistrering.data.database.entity.*
import com.example.timeregistrering.model.*
import com.example.timeregistrering.util.HolidayManager
import com.example.timeregistrering.util.NetworkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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

    // Holder styr på registreringer der venter på at blive synkroniseret
    private val _pendingSyncRegistrations = MutableStateFlow<List<TimeRegistration>>(emptyList())
    val pendingSyncRegistrations: StateFlow<List<TimeRegistration>> = _pendingSyncRegistrations.asStateFlow()

    init {
        // Observer netværksstatus og synkroniser når online
        viewModelScope.launch {
            networkManager.observeNetworkState()
                .filter { it is NetworkManager.NetworkState.Available }
                .collect {
                    syncPendingRegistrations()
                }
        }
    }

    suspend fun saveTimeRegistration(registration: TimeRegistration) {
        try {
            if (networkManager.isNetworkAvailable()) {
                // Online - gem direkte
                timeRegistrationDao.insert(registration.toEntity())
            } else {
                // Offline - gem lokalt og marker til synkronisering
                timeRegistrationDao.insert(registration.toEntity())
                pendingSyncDao.insert(registration)
                updatePendingSyncList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fejl ved oprettelse af tidsregistrering", e)
            throw e
        }
    }

    fun getTimeRegistrationsForWeek(weekStartDate: LocalDate): Flow<List<TimeRegistration>> {
        val userId = authRepository.getCurrentUserId() ?: throw IllegalStateException("Ingen bruger logget ind")
        val weekEndDate = weekStartDate.plusDays(6)
        return timeRegistrationDao.getRegistrationsForDateRange(
            userId = userId,
            startDate = weekStartDate.atStartOfDay(),
            endDate = weekEndDate.plusDays(1).atStartOfDay()
        ).map { entities -> entities.map { it.toDomain() } }
    }

    fun getTimeRegistrationsForMonth(year: Int, month: Int): Flow<List<TimeRegistration>> {
        val userId = authRepository.getCurrentUserId() ?: throw IllegalStateException("Ingen bruger logget ind")
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.plusMonths(1).minusDays(1)
        return timeRegistrationDao.getRegistrationsForDateRange(
            userId = userId,
            startDate = startDate.atStartOfDay(),
            endDate = endDate.plusDays(1).atStartOfDay()
        ).map { entities -> entities.map { it.toDomain() } }
    }

    suspend fun startTimeRegistration(projectId: String, description: String = "", location: Location? = null): TimeRegistration {
        val userId = authRepository.getCurrentUserId() ?: throw IllegalStateException("Ingen bruger logget ind")
        
        val registration = TimeRegistration(
            id = UUID.randomUUID().toString(),
            startTime = LocalDateTime.now(),
            description = description,
            location = location,
            projectId = projectId,
            userId = userId
        )

        try {
            if (networkManager.isNetworkAvailable()) {
                // Online - gem direkte
                timeRegistrationDao.insert(registration.toEntity())
            } else {
                // Offline - gem lokalt og marker til synkronisering
                timeRegistrationDao.insert(registration.toEntity())
                pendingSyncDao.insert(registration)
                updatePendingSyncList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fejl ved start af tidsregistrering", e)
            throw e
        }
        
        // Start første arbejdsperiode
        val workPeriod = WorkPeriod(
            id = UUID.randomUUID().toString(),
            startTime = registration.startTime
        )
        workPeriodDao.insert(workPeriod.toEntity(registration.id))

        return registration
    }

    suspend fun endTimeRegistration(registrationId: String) {
        val registration = timeRegistrationDao.getById(registrationId)
            ?: throw IllegalArgumentException("Tidsregistrering ikke fundet")
        
        val currentWorkPeriod = workPeriodDao.getCurrentWorkPeriod()
        if (currentWorkPeriod != null) {
            workPeriodDao.update(currentWorkPeriod.copy(
                endTime = LocalDateTime.now(),
                totalWorkTime = calculateWorkTime(currentWorkPeriod)
            ))
        }

        try {
            if (networkManager.isNetworkAvailable()) {
                // Online - opdater direkte
                timeRegistrationDao.update(registration.copy(endTime = LocalDateTime.now()))
            } else {
                // Offline - gem lokalt og marker til synkronisering
                timeRegistrationDao.update(registration.copy(endTime = LocalDateTime.now()))
                pendingSyncDao.insert(registration)
                updatePendingSyncList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fejl ved stop af tidsregistrering", e)
            throw e
        }
    }

    suspend fun startBreak(workPeriodId: String) {
        val break = Break(
            id = UUID.randomUUID().toString(),
            startTime = LocalDateTime.now()
        )
        breakDao.insert(break.toEntity(workPeriodId))
    }

    suspend fun endBreak(workPeriodId: String) {
        val currentBreak = breakDao.getCurrentBreak(workPeriodId)
            ?: throw IllegalStateException("Ingen aktiv pause fundet")
        
        breakDao.update(currentBreak.copy(endTime = LocalDateTime.now()))
    }

    suspend fun deleteTimeRegistration(registration: TimeRegistration) {
        timeRegistrationDao.delete(registration.toEntity())
    }

    private suspend fun updatePendingSyncList() {
        _pendingSyncRegistrations.value = pendingSyncDao.getAll()
    }

    private suspend fun syncPendingRegistrations() {
        try {
            val pending = pendingSyncDao.getAll()
            pending.forEach { registration ->
                try {
                    // Her ville vi normalt synkronisere med en server
                    // For nu markerer vi bare som synkroniseret
                    pendingSyncDao.delete(registration)
                } catch (e: Exception) {
                    Log.e(TAG, "Fejl ved synkronisering af registrering: ${registration.id}", e)
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
        return end.toEpochSecond(java.time.ZoneOffset.UTC) - 
               start.toEpochSecond(java.time.ZoneOffset.UTC)
    }

    private fun TimeRegistrationEntity.toDomain() = TimeRegistration(
        id = id,
        startTime = startTime,
        endTime = endTime,
        description = description,
        location = if (latitude != null && longitude != null) {
            Location(latitude, longitude, address)
        } else null,
        projectId = projectId,
        userId = userId,
        isHoliday = holidayManager.isHoliday(startTime.toLocalDate())
    )

    private fun TimeRegistration.toEntity() = TimeRegistrationEntity(
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

    private fun WorkPeriod.toEntity(registrationId: String) = WorkPeriodEntity(
        id = id,
        timeRegistrationId = registrationId,
        startTime = startTime,
        endTime = endTime,
        totalWorkTime = if (endTime != null) {
            endTime.toEpochSecond(java.time.ZoneOffset.UTC) - 
            startTime.toEpochSecond(java.time.ZoneOffset.UTC)
        } else 0
    )

    private fun Break.toEntity(workPeriodId: String) = BreakEntity(
        id = id,
        workPeriodId = workPeriodId,
        startTime = startTime,
        endTime = endTime
    )

    fun getTimeRegistrationsPaged(
        pageSize: Int = 20,
        prefetchDistance: Int = pageSize,
        enablePlaceholders: Boolean = true
    ): Flow<PagingData<TimeRegistration>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                prefetchDistance = prefetchDistance,
                enablePlaceholders = enablePlaceholders
            )
        ) {
            timeRegistrationDao.getAllTimeRegistrationsPaged()
        }.flow
    }

    fun getTimeRegistrationsByProjectPaged(
        projectId: Long,
        pageSize: Int = 20,
        prefetchDistance: Int = pageSize,
        enablePlaceholders: Boolean = true
    ): Flow<PagingData<TimeRegistration>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                prefetchDistance = prefetchDistance,
                enablePlaceholders = enablePlaceholders
            )
        ) {
            timeRegistrationDao.getTimeRegistrationsByProjectPaged(projectId)
        }.flow
    }

    fun getTimeRegistrationsByDateRangePaged(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        pageSize: Int = 20,
        prefetchDistance: Int = pageSize,
        enablePlaceholders: Boolean = true
    ): Flow<PagingData<TimeRegistration>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                prefetchDistance = prefetchDistance,
                enablePlaceholders = enablePlaceholders
            )
        ) {
            timeRegistrationDao.getTimeRegistrationsByDateRangePaged(startDate, endDate)
        }.flow
    }
}
