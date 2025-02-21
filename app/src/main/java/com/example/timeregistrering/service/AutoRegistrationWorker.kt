package com.example.timeregistrering.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.timeregistrering.repository.LocationRepository
import com.example.timeregistrering.repository.TimeRegistrationRepository
import com.google.android.gms.location.LocationServices
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

@HiltWorker
class AutoRegistrationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val timeRegistrationRepository: TimeRegistrationRepository,
    private val locationRepository: LocationRepository,
    private val calendarService: CalendarService
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            // Tjek om det er en arbejdsdag
            val now = LocalDateTime.now()
            if (isWorkDay(now)) {
                // Tjek lokation
                val location = locationRepository.getCurrentLocation().first()
                val isAtWork = locationRepository.isAtWorkLocation(location)

                if (isAtWork) {
                    // Tjek kalender for møder
                    val events = calendarService.getEvents(
                        now.with(LocalTime.MIN),
                        now.with(LocalTime.MAX)
                    )

                    // Start registrering hvis der er møder eller det er normal arbejdstid
                    if (events.isNotEmpty() || isWorkingHours(now.toLocalTime())) {
                        timeRegistrationRepository.startTimeRegistration(
                            projectId = "default",
                            description = "Automatisk registrering"
                        )
                    }
                }
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

    private fun isWorkDay(dateTime: LocalDateTime): Boolean {
        return dateTime.dayOfWeek !in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    }

    private fun isWorkingHours(time: LocalTime): Boolean {
        val workStart = LocalTime.of(8, 0)
        val workEnd = LocalTime.of(16, 0)
        return !time.isBefore(workStart) && !time.isAfter(workEnd)
    }

    companion object {
        fun scheduleWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<AutoRegistrationWorker>(
                15, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES
            )
            .setConstraints(constraints)
            .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "auto_registration",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    request
                )
        }
    }
}
