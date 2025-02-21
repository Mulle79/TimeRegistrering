package com.example.timeregistrering

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.timeregistrering.data.database.TimeregistreringDatabase
import com.example.timeregistrering.model.WeekDay
import com.example.timeregistrering.repository.TimeRegistrationRepository
import com.example.timeregistrering.service.GeofencingService
import com.example.timeregistrering.util.BackupManager
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class EdgeCaseTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var database: TimeregistreringDatabase

    @Inject
    lateinit var repository: TimeRegistrationRepository

    @Inject
    lateinit var geofencingService: GeofencingService

    @Inject
    lateinit var backupManager: BackupManager

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun testDaylightSavingsTransition() = runTest {
        // Test tidsregistrering over sommertid skift
        val dstDate = LocalDate.of(2024, 3, 31) // Sommertid starter
        val startTime = LocalTime.of(1, 0)
        val endTime = LocalTime.of(3, 0)

        repository.updateWorkDay(dstDate, startTime, endTime)
        val workDay = repository.getWorkDay(dstDate)
        
        assert(workDay.totalHours == 1.0) // Skal være 1 time pga. sommertid
    }

    @Test
    fun testNewYearTransition() = runTest {
        // Test uge der går over nytår
        val dec31 = LocalDate.of(2024, 12, 31)
        val jan1 = LocalDate.of(2025, 1, 1)

        repository.updateWorkDay(dec31, LocalTime.of(8, 0), LocalTime.of(16, 0))
        repository.updateWorkDay(jan1, LocalTime.of(8, 0), LocalTime.of(16, 0))

        val weekDays = repository.getWeekDays(dec31)
        assert(weekDays.size == 7)
    }

    @Test
    fun testDeviceBatteryOptimization() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Test geofencing under battery optimization
        geofencingService.checkBatteryOptimization(context)
        
        // Verificer at geofences stadig er aktive
        val activeGeofences = geofencingService.getActiveGeofences()
        assert(activeGeofences.isNotEmpty())
    }

    @Test
    fun testDatabaseCorruption() = runTest {
        // Test backup/restore under database korruption
        val backup = backupManager.createBackup()
        
        // Simuler database korruption
        database.close()
        InstrumentationRegistry.getInstrumentation().targetContext
            .getDatabasePath(database.openHelper.databaseName).delete()
            
        // Gendan fra backup
        backupManager.restoreBackup(backup)
        
        // Verificer at data er intakt
        val today = LocalDate.now()
        repository.updateWorkDay(today, LocalTime.of(9, 0), LocalTime.of(17, 0))
        val workDay = repository.getWorkDay(today)
        assert(workDay.totalHours == 8.0)
    }

    @Test
    fun testConcurrentDatabaseAccess() = runTest {
        val date = LocalDate.now()
        
        // Simuler concurrent adgang til databasen
        val jobs = List(10) {
            async {
                repository.updateWorkDay(
                    date,
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0)
                )
            }
        }
        
        jobs.awaitAll()
        
        // Verificer data integritet
        val workDay = repository.getWorkDay(date)
        assert(workDay.startTime == LocalTime.of(9, 0))
        assert(workDay.endTime == LocalTime.of(17, 0))
    }
}
