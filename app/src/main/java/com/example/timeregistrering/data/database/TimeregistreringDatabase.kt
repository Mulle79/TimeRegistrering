package com.example.timeregistrering.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.timeregistrering.data.database.converter.Converters
import com.example.timeregistrering.data.database.dao.*
import com.example.timeregistrering.data.database.entity.*

@Database(
    entities = [
        MoedeEntity::class,
        TimeRegistrationEntity::class,
        ProjectEntity::class,
        WorkPeriodEntity::class,
        BreakEntity::class,
        PendingSyncEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class TimeregistreringDatabase : RoomDatabase() {
    abstract fun moedeDao(): MoedeDao
    abstract fun timeRegistrationDao(): TimeRegistrationDao
    abstract fun projectDao(): ProjectDao
    abstract fun workPeriodDao(): WorkPeriodDao
    abstract fun breakDao(): BreakDao
    abstract fun pendingSyncDao(): PendingSyncDao

    companion object {
        const val DATABASE_NAME = "timeregistrering-db"
    }
}
