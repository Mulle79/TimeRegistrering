package com.example.timeregistrering.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timeregistrering.data.repository.ProjectRepository
import com.example.timeregistrering.data.repository.TimeRegistrationRepository
import com.example.timeregistrering.model.Project
import com.example.timeregistrering.model.TimeRegistration
import com.example.timeregistrering.util.NetworkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class TimeRegistrationViewModel @Inject constructor(
    private val timeRegistrationRepository: TimeRegistrationRepository,
    private val projectRepository: ProjectRepository,
    private val networkManager: NetworkManager,
    private val excelManager: ExcelManager,
    private val holidayManager: HolidayManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<TimeRegistrationUiState>(TimeRegistrationUiState.Loading)
    val uiState: StateFlow<TimeRegistrationUiState> = _uiState.asStateFlow()

    private val _errorEvents = MutableSharedFlow<ErrorEvent>()
    val errorEvents = _errorEvents.asSharedFlow()

    private val _networkState = MutableStateFlow<NetworkManager.NetworkState>(NetworkManager.NetworkState.Unavailable)
    val networkState: StateFlow<NetworkManager.NetworkState> = _networkState.asStateFlow()

    private val _selectedProject = MutableStateFlow<Project?>(null)
    val selectedProject: StateFlow<Project?> = _selectedProject

    private val _selectedWeek = MutableStateFlow<LocalDate>(LocalDate.now())
    val selectedWeek: StateFlow<LocalDate> = _selectedWeek.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _selectedMonth = MutableStateFlow<LocalDate>(LocalDate.now())
    val selectedMonth: StateFlow<LocalDate> = _selectedMonth.asStateFlow()

    private val _weeklySettings = MutableStateFlow(WeeklySettings())
    val weeklySettings: StateFlow<WeeklySettings> = _weeklySettings.asStateFlow()

    private val _timeRegistrations = MutableStateFlow<UiState<List<TimeRegistration>>>(UiState.Loading)
    val timeRegistrations: StateFlow<UiState<List<TimeRegistration>>> = _timeRegistrations

    private val _excelGenerationState = MutableStateFlow<UiState<File?>>(UiState.Success(null))
    val excelGenerationState: StateFlow<UiState<File?>> = _excelGenerationState

    val projects: StateFlow<List<Project>> = projectRepository.getProjects()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val weekRegistrations: StateFlow<List<TimeRegistration>> = _selectedWeek
        .flatMapLatest { weekStart ->
            timeRegistrationRepository.getTimeRegistrationsForWeek(weekStart)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val selectedDayRegistration: StateFlow<TimeRegistration?> = _selectedDate
        .flatMapLatest { date ->
            timeRegistrationRepository.getTimeRegistrationForDate(date)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val monthRegistrations: StateFlow<List<TimeRegistration>> = _selectedMonth
        .flatMapLatest { yearMonth ->
            timeRegistrationRepository.getTimeRegistrationsForMonth(yearMonth.year, yearMonth.monthValue)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val isSelectedDateHoliday: StateFlow<Boolean> = _selectedDate
        .flatMapLatest { date ->
            holidayManager.isHoliday(date)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val totalMonthlyHours: StateFlow<Float> = monthRegistrations
        .map { registrations ->
            calculateTotalHours(registrations)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    val totalWeeklyHours: StateFlow<Float> = weekRegistrations
        .map { registrations ->
            calculateTotalHours(registrations)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    val expectedWeeklyHours: StateFlow<Float> = weeklySettings
        .map { settings ->
            settings.workDays.size * Duration.between(
                settings.workDayStart,
                settings.workDayEnd
            ).toHours().toFloat()
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 40f)

    init {
        viewModelScope.launch {
            // Observer netværksstatus
            networkManager.observeNetworkState()
                .collect { state ->
                    _networkState.value = state
                }
        }

        // Load initial data
        loadTimeRegistrations()
        loadProjects()
        loadDefaultProject()
        // Auto-register work hours for past dates based on weekly settings
        viewModelScope.launch {
            autoRegisterWorkHours()
        }
    }

    private fun loadTimeRegistrations() {
        viewModelScope.launch {
            try {
                combine(
                    timeRegistrationRepository.getCurrentRegistration(),
                    timeRegistrationRepository.getTimeRegistrationsForWeek(LocalDate.now()),
                    timeRegistrationRepository.pendingSyncRegistrations
                ) { current, weekRegistrations, pendingSync ->
                    TimeRegistrationUiState.Success(
                        currentRegistration = current,
                        weekRegistrations = weekRegistrations,
                        pendingSyncCount = pendingSync.size,
                        isOffline = !networkManager.isNetworkAvailable()
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = TimeRegistrationUiState.Error("Kunne ikke indlæse tidsregistreringer")
                _errorEvents.emit(ErrorEvent.LoadError("Kunne ikke indlæse tidsregistreringer", e))
            }
        }
    }

    private fun loadProjects() {
        viewModelScope.launch {
            try {
                projectRepository.getProjects()
                    .collect { projects ->
                        if (_uiState.value is TimeRegistrationUiState.Success) {
                            _uiState.value = (_uiState.value as TimeRegistrationUiState.Success).copy(
                                projects = projects
                            )
                        }
                    }
            } catch (e: Exception) {
                _errorEvents.emit(ErrorEvent.LoadError("Kunne ikke indlæse projekter", e))
            }
        }
    }

    private fun loadDefaultProject() {
        viewModelScope.launch {
            projectRepository.getDefaultProject()?.let { project ->
                _selectedProject.value = project
            }
        }
    }

    fun startTimeRegistration(projectId: String, description: String = "") {
        viewModelScope.launch {
            try {
                // Validering
                if (projectId.isBlank()) {
                    _errorEvents.emit(ErrorEvent.ValidationError("Vælg venligst et projekt"))
                    return@launch
                }

                val project = projectRepository.getProjectById(projectId)
                if (project == null) {
                    _errorEvents.emit(ErrorEvent.ValidationError("Ugyldigt projekt valgt"))
                    return@launch
                }

                timeRegistrationRepository.startTimeRegistration(projectId, description)
            } catch (e: Exception) {
                _errorEvents.emit(ErrorEvent.OperationError("Kunne ikke starte tidsregistrering", e))
            }
        }
    }

    fun stopCurrentRegistration() {
        viewModelScope.launch {
            try {
                timeRegistrationRepository.stopCurrentRegistration()
            } catch (e: Exception) {
                _errorEvents.emit(ErrorEvent.OperationError("Kunne ikke stoppe tidsregistrering", e))
            }
        }
    }

    fun createManualTimeRegistration(
        projectId: String,
        startTime: LocalTime,
        endTime: LocalTime,
        description: String
    ) {
        viewModelScope.launch {
            try {
                // Validering
                if (projectId.isBlank()) {
                    _errorEvents.emit(ErrorEvent.ValidationError("Vælg venligst et projekt"))
                    return@launch
                }

                if (startTime >= endTime) {
                    _errorEvents.emit(ErrorEvent.ValidationError("Starttidspunkt skal være før sluttidspunkt"))
                    return@launch
                }

                val project = projectRepository.getProjectById(projectId)
                if (project == null) {
                    _errorEvents.emit(ErrorEvent.ValidationError("Ugyldigt projekt valgt"))
                    return@launch
                }

                timeRegistrationRepository.createManualTimeRegistration(
                    projectId = projectId,
                    startTime = startTime,
                    endTime = endTime,
                    description = description
                )
            } catch (e: Exception) {
                _errorEvents.emit(ErrorEvent.OperationError("Kunne ikke oprette manuel tidsregistrering", e))
            }
        }
    }

    fun selectProject(project: Project) {
        _selectedProject.value = project
    }

    fun setSelectedWeek(date: LocalDate) {
        _selectedWeek.value = date.minusDays(date.dayOfWeek.value.toLong() - 1)
    }

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun setSelectedMonth(yearMonth: LocalDate) {
        _selectedMonth.value = yearMonth
    }

    fun updateWeeklySettings(
        workDayStart: LocalTime? = null,
        workDayEnd: LocalTime? = null,
        workDays: Set<DayOfWeek>? = null
    ) {
        _weeklySettings.update { current ->
            current.copy(
                workDayStart = workDayStart ?: current.workDayStart,
                workDayEnd = workDayEnd ?: current.workDayEnd,
                workDays = workDays ?: current.workDays
            )
        }
    }

    fun resetWeeklySettings() {
        _weeklySettings.value = WeeklySettings()
    }

    fun startTimeRegistration(date: LocalDate = _selectedDate.value, startTime: LocalTime, endTime: LocalTime, note: String? = null) {
        viewModelScope.launch {
            try {
                _registrationState.value = UiState.Loading
                
                val isHoliday = holidayManager.isHoliday(date).first()
                val isWeekend = date.dayOfWeek.value > 5
                
                val registration = TimeRegistration(
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    note = note,
                    isHoliday = isHoliday,
                    isWeekend = isWeekend
                )
                
                timeRegistrationRepository.saveTimeRegistration(registration)
                _registrationState.value = UiState.Success(TimeRegistrationState.Active(registration))
                loadTimeRegistrations() // Genindlæs listen
            } catch (e: Exception) {
                _registrationState.value = UiState.Error("Kunne ikke starte tidsregistrering: ${e.message}")
            }
        }
    }

    fun stopTimeRegistration() {
        viewModelScope.launch {
            try {
                val currentState = _registrationState.value
                if (currentState is UiState.Success) {
                    _registrationState.value = UiState.Loading
                    timeRegistrationRepository.deleteTimeRegistration(currentState.data.registration)
                    _registrationState.value = UiState.Success(TimeRegistrationState.Inactive)
                    loadTimeRegistrations() // Genindlæs listen
                }
            } catch (e: Exception) {
                _registrationState.value = UiState.Error("Kunne ikke stoppe tidsregistrering: ${e.message}")
            }
        }
    }

    private suspend fun autoRegisterWorkHours() {
        val today = LocalDate.now()
        val lastWeek = today.minusWeeks(1)
        val settings = weeklySettings.value

        (lastWeek..today).forEach { date ->
            if (settings.workDays.contains(date.dayOfWeek)) {
                val existingRegistration = timeRegistrationRepository.getTimeRegistrationForDate(date).first()
                if (existingRegistration == null) {
                    saveTimeRegistration(
                        date = date,
                        startTime = settings.workDayStart,
                        endTime = settings.workDayEnd
                    )
                }
            }
        }
    }

    fun saveTimeRegistration(
        date: LocalDate = _selectedDate.value,
        startTime: LocalTime,
        endTime: LocalTime,
        note: String? = null
    ) {
        viewModelScope.launch {
            val isHoliday = holidayManager.isHoliday(date).first()
            val isWeekend = date.dayOfWeek.value > 5
            
            val registration = TimeRegistration(
                date = date,
                startTime = startTime,
                endTime = endTime,
                note = note,
                isHoliday = isHoliday,
                isWeekend = isWeekend
            )
            
            timeRegistrationRepository.saveTimeRegistration(registration)
        }
    }

    fun updateTimeRegistration(registration: TimeRegistration) {
        viewModelScope.launch {
            timeRegistrationRepository.saveTimeRegistration(registration)
        }
    }

    fun deleteTimeRegistration(registration: TimeRegistration) {
        viewModelScope.launch {
            timeRegistrationRepository.deleteTimeRegistration(registration)
        }
    }

    suspend fun generateExcel(startDate: LocalDate, endDate: LocalDate): File {
        val registrations = timeRegistrationRepository.getTimeRegistrationsForMonth(
            startDate.year,
            startDate.monthValue
        ).first()
        
        return excelManager.generateTimesheet(startDate, endDate, registrations)
    }

    fun calculateTotalHours(registrations: List<TimeRegistration>): Float {
        return registrations.sumOf { registration ->
            val start = registration.startTime
            val end = registration.endTime
            val hours = end.hour - start.hour + (end.minute - start.minute) / 60f
            if (registration.isHoliday || registration.isWeekend) hours * 1.5f else hours
        }.toFloat()
    }

    fun deleteOldRegistrations(beforeDate: LocalDate) {
        viewModelScope.launch {
            timeRegistrationRepository.deleteRegistrationsBeforeDate(beforeDate)
        }
    }

    fun generateExcelTimesheet() {
        viewModelScope.launch {
            try {
                _excelGenerationState.value = UiState.Loading
                
                // Hent alle registreringer for den aktuelle uge
                val registrations = timeRegistrationRepository.getTimeRegistrationsForWeek(_selectedWeek.value)
                    .first()
                    .sortedBy { it.startTime }

                if (registrations.isEmpty()) {
                    _excelGenerationState.value = UiState.Error("Ingen registreringer at eksportere")
                    return@launch
                }

                // Generer Excel fil
                val fileName = "timeseddel_uge_${_selectedWeek.value.get(WeekFields.ISO.weekOfWeekBasedYear())}.xlsx"
                val file = excelManager.generateTimesheet(registrations, fileName)
                
                _excelGenerationState.value = UiState.Success(file)
            } catch (e: Exception) {
                _excelGenerationState.value = UiState.Error("Kunne ikke generere Excel: ${e.message}")
            }
        }
    }

    fun updateRegistrationTimes(
        registrationId: String,
        newStartTime: LocalDateTime? = null,
        newEndTime: LocalDateTime? = null
    ) {
        viewModelScope.launch {
            try {
                timeRegistrationRepository.getRegistrationById(registrationId)?.let { registration ->
                    val updatedRegistration = registration.copy(
                        startTime = newStartTime ?: registration.startTime,
                        endTime = newEndTime ?: registration.endTime
                    )
                    timeRegistrationRepository.updateRegistration(updatedRegistration)
                    loadTimeRegistrations()
                }
            } catch (e: Exception) {
                // Håndter fejl
            }
        }
    }

    fun setWorkplaceLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                geofencingService.addWorkplaceGeofence(latitude, longitude)
                // Gem lokationen i preferences
                dataStore.edit { preferences ->
                    preferences[PreferencesKeys.WORKPLACE_LATITUDE] = latitude.toString()
                    preferences[PreferencesKeys.WORKPLACE_LONGITUDE] = longitude.toString()
                }
            } catch (e: Exception) {
                // Håndter fejl
            }
        }
    }

    sealed class TimeRegistrationUiState {
        object Loading : TimeRegistrationUiState()
        data class Success(
            val currentRegistration: TimeRegistration? = null,
            val weekRegistrations: List<TimeRegistration> = emptyList(),
            val projects: List<Project> = emptyList(),
            val pendingSyncCount: Int = 0,
            val isOffline: Boolean = false
        ) : TimeRegistrationUiState()
        data class Error(val message: String) : TimeRegistrationUiState()
    }

    sealed class ErrorEvent {
        data class LoadError(val message: String, val error: Exception) : ErrorEvent()
        data class ValidationError(val message: String) : ErrorEvent()
        data class OperationError(val message: String, val error: Exception) : ErrorEvent()
    }

    sealed class TimeRegistrationState {
        data class Active(val registration: TimeRegistration) : TimeRegistrationState()
        object Inactive : TimeRegistrationState()
    }

    data class WeeklySettings(
        val workDayStart: LocalTime = LocalTime.of(8, 0),
        val workDayEnd: LocalTime = LocalTime.of(16, 0),
        val workDays: Set<DayOfWeek> = setOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        )
    )
}
