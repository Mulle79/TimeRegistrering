package com.example.timeregistrering.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Data Access Object (DAO) til haandtering af møder i databasen.
 * 
 * Denne DAO følger Room persistence library patterns og tilbyder:
 * - CRUD operationer for møder
 * - Flow-baserede queries for reaktiv programmering
 * - Synkronisering med Google Calendar
 * - Effektiv cachehåndtering
 */
@Dao
interface MoedeDao {
    @Query("SELECT * FROM moede")
    fun getAllMoeder(): Flow<List<Moede>>

    @Query("SELECT * FROM moede WHERE startTid BETWEEN :startTid AND :slutTid")
    fun getMoederBetweenDates(startTid: LocalDateTime, slutTid: LocalDateTime): Flow<List<Moede>>

    @Query("SELECT * FROM moede WHERE googleEventId = :googleEventId LIMIT 1")
    suspend fun getMoedeByGoogleEventId(googleEventId: String): Moede?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(moede: Moede)

    @Update
    suspend fun update(moede: Moede)

    @Delete
    suspend fun delete(moede: Moede)

    @Query("DELETE FROM moede WHERE startTid < :dato")
    suspend fun deleteOldMoeder(dato: LocalDateTime)
}
