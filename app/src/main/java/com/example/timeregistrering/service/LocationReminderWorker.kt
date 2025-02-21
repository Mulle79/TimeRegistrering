package com.example.timeregistrering.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.timeregistrering.R
import com.example.timeregistrering.repository.LocationRepository
import com.example.timeregistrering.repository.TimeRegistrationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class LocationReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val timeRegistrationRepository: TimeRegistrationRepository,
    private val locationRepository: LocationRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            // Tjek nuværende lokation
            val location = locationRepository.getCurrentLocation().first()
            val isAtWork = locationRepository.isAtWorkLocation(location)

            // Tjek om der er en aktiv registrering
            val activeRegistration = timeRegistrationRepository.getCurrentRegistration().first()

            // Send påmindelse hvis nødvendigt
            if (isAtWork && activeRegistration == null) {
                showNotification(
                    "Start tidsregistrering?",
                    "Du er på arbejdspladsen. Vil du starte tidsregistrering?"
                )
            } else if (!isAtWork && activeRegistration != null) {
                showNotification(
                    "Stop tidsregistrering?",
                    "Du har forladt arbejdspladsen. Vil du stoppe tidsregistrering?"
                )
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Opret notification channel for Android O og nyere
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Tidsregistrering påmindelser",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Påmindelser om at starte/stoppe tidsregistrering"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Byg notifikationen
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val CHANNEL_ID = "location_reminders"
        private const val NOTIFICATION_ID = 1

        fun scheduleWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<LocationReminderWorker>(
                15, TimeUnit.MINUTES
            )
            .setConstraints(constraints)
            .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "location_reminders",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    request
                )
        }
    }
}
