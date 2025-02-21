package com.example.timeregistrering.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "pending_sync")
data class PendingSyncEntity(
    @PrimaryKey
    val id: String,
    val type: String, // Type af entity der skal synkroniseres
    val data: String, // JSON data af entity
    val action: String, // CREATE, UPDATE, DELETE
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastAttempt: LocalDateTime? = null,
    val attempts: Int = 0
)
