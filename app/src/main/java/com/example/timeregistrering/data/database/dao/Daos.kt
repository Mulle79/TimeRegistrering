package com.example.timeregistrering.data.database.dao

import androidx.room.*
import com.example.timeregistrering.data.database.entity.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface TimeRegistrationDao {
    @Query("SELECT * FROM time_registrations WHERE userId = :userId ORDER BY startTime DESC")
    fun getAllForUser(userId: String): Flow<List<TimeRegistrationEntity>>

    @Query("SELECT * FROM time_registrations WHERE id = :id")
    suspend fun getById(id: String): TimeRegistrationEntity?

    @Query("SELECT * FROM time_registrations WHERE endTime IS NULL AND userId = :userId")
    suspend fun getCurrentRegistration(userId: String): TimeRegistrationEntity?

    @Query("""
        SELECT * FROM time_registrations 
        WHERE userId = :userId 
        AND startTime >= :startDate 
        AND startTime < :endDate 
        ORDER BY startTime DESC
    """)
    suspend fun getRegistrationsForDateRange(
        userId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<TimeRegistrationEntity>

    @Query("""
        SELECT * FROM time_registrations 
        WHERE userId = :userId 
        AND date(startTime) = date(:date)
        ORDER BY startTime DESC
    """)
    suspend fun getRegistrationsForDate(
        userId: String,
        date: LocalDateTime
    ): List<TimeRegistrationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(registration: TimeRegistrationEntity)

    @Update
    suspend fun update(registration: TimeRegistrationEntity)

    @Delete
    suspend fun delete(registration: TimeRegistrationEntity)
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects")
    fun getAll(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getById(id: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: ProjectEntity)

    @Update
    suspend fun update(project: ProjectEntity)

    @Delete
    suspend fun delete(project: ProjectEntity)
}

@Dao
interface WorkPeriodDao {
    @Transaction
    @Query("SELECT * FROM work_periods WHERE timeRegistrationId = :timeRegistrationId")
    fun getForTimeRegistration(timeRegistrationId: String): Flow<List<WorkPeriodEntity>>

    @Query("SELECT * FROM work_periods WHERE endTime IS NULL")
    suspend fun getCurrentWorkPeriod(): WorkPeriodEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workPeriod: WorkPeriodEntity)

    @Update
    suspend fun update(workPeriod: WorkPeriodEntity)
}

@Dao
interface BreakDao {
    @Query("SELECT * FROM breaks WHERE workPeriodId = :workPeriodId")
    fun getForWorkPeriod(workPeriodId: String): Flow<List<BreakEntity>>

    @Query("SELECT * FROM breaks WHERE endTime IS NULL AND workPeriodId = :workPeriodId")
    suspend fun getCurrentBreak(workPeriodId: String): BreakEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(break: BreakEntity)

    @Update
    suspend fun update(break: BreakEntity)
}
