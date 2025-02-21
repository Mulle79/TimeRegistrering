package com.example.timeregistrering.performance

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.timeregistrering.data.database.TimeRegistrationDatabase
import com.example.timeregistrering.model.TimeRegistration
import com.example.timeregistrering.repository.TimeRegistrationRepository
import com.example.timeregistrering.util.PerformanceMonitor
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PerformanceTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var database: TimeRegistrationDatabase

    @Inject
    lateinit var repository: TimeRegistrationRepository

    @Inject
    lateinit var performanceMonitor: PerformanceMonitor

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun testDatabasePerformance() = runTest {
        withContext(Dispatchers.IO) {
            // Given: Store mange tidsregistreringer
            val registrations = (1..1000).map { day ->
                TimeRegistration(
                    date = LocalDate.now().plusDays(day.toLong()),
                    startTime = LocalTime.of(9, 0),
                    endTime = LocalTime.of(17, 0),
                    note = "Performance test $day"
                )
            }

            // When: Mål indsættelsestid
            val insertTime = performanceMonitor.measureTimeMillis {
                registrations.forEach { registration ->
                    repository.saveTimeRegistration(registration)
                }
            }

            // Then: Verificer performance
            assert(insertTime < 5000) { "Bulk insert tog for lang tid: $insertTime ms" }

            // When: Mål søgetid
            val queryTime = performanceMonitor.measureTimeMillis {
                repository.getWeeklyRegistrations(LocalDate.now())
            }

            // Then: Verificer query performance
            assert(queryTime < 100) { "Query tog for lang tid: $queryTime ms" }

            // When: Mål memory forbrug
            val memoryStats = performanceMonitor.getMemoryStats()

            // Then: Verificer memory forbrug
            assert(memoryStats.usedMemoryMB < 50) { "For højt memory forbrug: ${memoryStats.usedMemoryMB} MB" }
        }
    }

    @Test
    fun testUIPerformance() = runTest {
        // Given: Start performance tracking
        performanceMonitor.startFrameTimeTracking()

        // When: Simuler UI interaktioner
        repeat(100) {
            repository.getWeeklyRegistrations(LocalDate.now().plusWeeks(it.toLong()))
        }

        // Then: Verificer frame times
        val frameStats = performanceMonitor.stopFrameTimeTracking()
        assert(frameStats.averageFrameTimeMs < 16.67) { "Frame time for høj: ${frameStats.averageFrameTimeMs} ms" }
    }

    @Test
    fun testNetworkPerformance() = runTest {
        withContext(Dispatchers.IO) {
            // Given: Start network monitoring
            performanceMonitor.startNetworkMonitoring()

            // When: Udfør netværksoperationer
            repeat(10) {
                repository.syncWithRemote()
            }

            // Then: Verificer netværks performance
            val networkStats = performanceMonitor.getNetworkStats()
            assert(networkStats.averageResponseTimeMs < 1000) { "Netværk for langsomt: ${networkStats.averageResponseTimeMs} ms" }
            assert(networkStats.failureRate < 0.1) { "For høj fejlrate: ${networkStats.failureRate}" }
        }
    }

    @Test
    fun testConcurrentPerformance() = runTest {
        withContext(Dispatchers.IO) {
            // Given: Mange samtidige operationer
            val operations = (1..100).map { 
                async {
                    repository.getWeeklyRegistrations(LocalDate.now().plusWeeks(it.toLong()))
                }
            }

            // When: Mål concurrent performance
            val concurrentTime = performanceMonitor.measureTimeMillis {
                operations.awaitAll()
            }

            // Then: Verificer concurrent performance
            assert(concurrentTime < 5000) { "Concurrent operationer for langsomme: $concurrentTime ms" }
        }
    }
}
