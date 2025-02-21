package com.example.timeregistrering.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.timeregistrering.database.MoedeDao
import com.example.timeregistrering.common.util.NotificationHelper
import com.example.timeregistrering.repository.CalendarRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class SyncService : Service() {
    @Inject
    lateinit var moedeDao: MoedeDao

    @Inject
    lateinit var calendarRepository: CalendarRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(
            NOTIFICATION_ID,
            notificationHelper.createForegroundNotification("Synkroniserer møder")
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SYNC -> performSync()
        }
        return START_NOT_STICKY
    }

    private fun performSync() {
        serviceScope.launch {
            try {
                val now = LocalDateTime.now()
                val endOfDay = now.toLocalDate().plusDays(1).atStartOfDay()
                
                // Hent møder fra Google Calendar
                val events = calendarRepository.fetchEvents(now, endOfDay).first()
                
                // Opdater lokale møder
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
                
                stopSelf()
            } catch (e: Exception) {
                notificationHelper.showErrorNotification(
                    "Synkroniseringsfejl",
                    "Kunne ikke synkronisere med Google Calendar"
                )
                stopSelf()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        const val ACTION_SYNC = "com.example.timeregistrering.action.SYNC"
    }
}
