package com.example.timeregistrering.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "moede")
data class Moede(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val titel: String,
    val beskrivelse: String?,
    val startTid: LocalDateTime,
    val slutTid: LocalDateTime,
    val lokation: String?,
    val googleEventId: String? = null,
    val erSynkroniseret: Boolean = false
)
