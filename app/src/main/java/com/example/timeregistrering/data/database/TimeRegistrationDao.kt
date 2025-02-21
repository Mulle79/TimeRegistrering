package com.example.timeregistrering.data.database

import androidx.paging.PagingSource
import androidx.room.*
import com.example.timeregistrering.data.model.TimeRegistration
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface TimeRegistrationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(registration: TimeRegistration)

    @Update
    suspend fun update(registration: TimeRegistration)

    @Delete
    suspend fun delete(registration: TimeRegistration)

    @Query("SELECT * FROM time_registrations WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getRegistrationsBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<TimeRegistration>>

    @Query("SELECT * FROM time_registrations WHERE date = :date LIMIT 1")
    fun getRegistrationForDate(date: LocalDate): Flow<TimeRegistration?>

    @Query("SELECT * FROM time_registrations WHERE date >= :startDate ORDER BY date ASC")
    fun getRegistrationsFromDate(startDate: LocalDate): Flow<List<TimeRegistration>>

    @Query("SELECT * FROM time_registrations WHERE strftime('%Y-%m', date) = :yearMonth ORDER BY date ASC")
    fun getRegistrationsForYearMonth(yearMonth: String): Flow<List<TimeRegistration>>

    @Query("SELECT SUM((julianday(endTime) - julianday(startTime)) * 24) as total_hours FROM time_registrations WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalHoursBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<Float?>

    @Query("DELETE FROM time_registrations WHERE date < :date")
    suspend fun deleteRegistrationsBeforeDate(date: LocalDate)

    @Query("SELECT COUNT(*) FROM time_registrations WHERE date BETWEEN :startDate AND :endDate AND isHoliday = 1")
    fun getHolidayCountBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<Int>

    @Query("SELECT * FROM time_registrations ORDER BY startTime DESC")
    fun getAllTimeRegistrationsPaged(): PagingSource<Int, TimeRegistration>

    @Query("SELECT * FROM time_registrations WHERE projectId = :projectId ORDER BY startTime DESC")
    fun getTimeRegistrationsByProjectPaged(projectId: Long): PagingSource<Int, TimeRegistration>

    @Query("SELECT * FROM time_registrations WHERE startTime BETWEEN :startDate AND :endDate ORDER BY startTime DESC")
    fun getTimeRegistrationsByDateRangePaged(startDate: LocalDateTime, endDate: LocalDateTime): PagingSource<Int, TimeRegistration>
}
