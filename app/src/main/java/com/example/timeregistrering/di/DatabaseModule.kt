package com.example.timeregistrering.di

import android.content.Context
import androidx.room.Room
import com.example.timeregistrering.data.database.TimeregistreringDatabase
import com.example.timeregistrering.data.database.dao.MoedeDao
import com.example.timeregistrering.data.database.dao.TimeRegistrationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TimeregistreringDatabase {
        return Room.databaseBuilder(
            context,
            TimeregistreringDatabase::class.java,
            "timeregistrering.db"
        ).build()
    }

    @Provides
    fun provideMoedeDao(database: TimeregistreringDatabase): MoedeDao {
        return database.moedeDao()
    }

    @Provides
    fun provideTimeRegistrationDao(database: TimeregistreringDatabase): TimeRegistrationDao {
        return database.timeRegistrationDao()
    }
}
