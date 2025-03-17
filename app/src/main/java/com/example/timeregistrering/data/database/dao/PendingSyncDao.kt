package com.example.timeregistrering.data.database.dao

import androidx.room.*
import com.example.timeregistrering.data.database.entity.PendingSyncEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingSyncDao {
    @Query("SELECT * FROM pending_sync")
    fun getAllPendingSyncs(): Flow<List<PendingSyncEntity>>
    
    @Query("SELECT * FROM pending_sync")
    suspend fun getAllPendingSyncsSync(): List<PendingSyncEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingSync(entity: PendingSyncEntity)

    @Query("DELETE FROM pending_sync WHERE registrationId = :registrationId")
    suspend fun deletePendingSync(registrationId: String)

    @Delete
    suspend fun delete(entity: PendingSyncEntity)

    @Query("DELETE FROM pending_sync")
    suspend fun deleteAll()
}
