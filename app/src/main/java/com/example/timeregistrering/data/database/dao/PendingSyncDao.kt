package com.example.timeregistrering.data.database.dao

import androidx.room.*
import com.example.timeregistrering.data.database.entity.PendingSyncEntity

@Dao
interface PendingSyncDao {
    @Query("SELECT * FROM pending_sync")
    suspend fun getAll(): List<PendingSyncEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PendingSyncEntity)

    @Delete
    suspend fun delete(entity: PendingSyncEntity)

    @Query("DELETE FROM pending_sync")
    suspend fun deleteAll()
}
