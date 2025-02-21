package com.example.timeregistrering.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
class TimeRegistrationDaoTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: TimeregistreringDatabase
    private lateinit var timeRegistrationDao: TimeRegistrationDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TimeregistreringDatabase::class.java
        ).allowMainThreadQueries().build()
        
        timeRegistrationDao = database.timeRegistrationDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveTimeRegistration() = runBlocking {
        // Given
        val timeRegistration = TimeRegistration(
            date = LocalDate.now(),
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(17, 0),
            note = "Test registration"
        )

        // When
        timeRegistrationDao.insert(timeRegistration)
        val result = timeRegistrationDao.getTimeRegistrationsForDate(LocalDate.now()).first()

        // Then
        assert(result.isNotEmpty())
        assert(result[0].note == "Test registration")
    }
}
