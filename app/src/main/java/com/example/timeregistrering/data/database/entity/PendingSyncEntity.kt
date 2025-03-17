package com.example.timeregistrering.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "pending_sync")
data class PendingSyncEntity(
    @PrimaryKey
    val registrationId: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastAttempt: LocalDateTime? = null,
    val attempts: Int = 0
)
