package com.example.timeregistrering.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.timeregistrering.calendar.GoogleCalendarManager
import com.example.timeregistrering.database.MoedeDao
import com.example.timeregistrering.network.NetworkManager
import com.example.timeregistrering.common.util.NotificationHelper
import com.example.timeregistrering.repository.CalendarRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime

/**
 * WorkManager Worker til periodisk synkronisering af data.
 * 
 * Denne worker er ansvarlig for:
 * - Synkronisering af møder med Google Calendar
 * - Synkronisering af tidsregistreringer med backend
 * - Håndtering af netværksfejl og genoprettelse
 * - Notifikationer om synkroniseringsstatus
 *
 * Implementerer WorkManager's best practices for:
 * - Batteri-optimering
 * - Netværkshåndtering
 * - Fejl-genoprettelse
 * - Background processing constraints
 *
 * @property moedeDao Repository til mødedata
 * @property calendarRepository Repository til Google Calendar integration
 * @property networkManager Manager til netværksintegration
 * @property notificationHelper Helper til at vise notifikationer
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val moedeDao: MoedeDao,
    private val calendarRepository: CalendarRepository,
    private val networkManager: NetworkManager,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Check network connectivity
            if (!networkManager.isNetworkAvailable.first()) {
                return Result.retry()
            }

            val now = LocalDateTime.now()
            val endOfDay = now.toLocalDate().plusDays(1).atStartOfDay()

            // Sync meetings
            val events = calendarRepository.fetchEvents(now, endOfDay).first()
            events.items?.forEach { event ->
                val existingMoede = moedeDao.getMoedeByGoogleEventId(event.id)
                if (existingMoede == null) {
                    val moede = Moede(
                        id = 0,
                        title = event.summary ?: "",
                        description = event.description ?: "",
                        startTime = event.start.dateTime.toLocalDateTime(),
                        endTime = event.end.dateTime.toLocalDateTime(),
                        googleEventId = event.id,
                        erSynkroniseret = true
                    )
                    moedeDao.insert(moede)
                }
            }

            Result.success()
        } catch (e: Exception) {
            notificationHelper.showErrorNotification(
                "Synkroniseringsfejl",
                "Kunne ikke synkronisere med Google Calendar"
            )
            Result.failure()
        }
    }

    companion object {
        fun setupPeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                Duration.ofHours(1) // Sync every hour
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "calendar_sync",
                    ExistingPeriodicWorkPolicy.KEEP,
                    syncRequest
                )
        }
    }
}
