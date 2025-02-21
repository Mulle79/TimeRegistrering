package com.example.timeregistrering.di

import android.content.Context
import com.example.timeregistrering.data.database.TimeregistreringDatabase
import com.example.timeregistrering.data.repository.ProjectRepository
import com.example.timeregistrering.repository.TimeRegistrationRepository
import com.example.timeregistrering.util.HolidayManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideProjectRepository(
        @ApplicationContext context: Context
    ): ProjectRepository {
        return ProjectRepository(context)
    }

    @Provides
    @Singleton
    fun provideTimeRegistrationRepository(
        database: TimeregistreringDatabase,
        holidayManager: HolidayManager
    ): TimeRegistrationRepository {
        return TimeRegistrationRepository(database, holidayManager)
    }
}
