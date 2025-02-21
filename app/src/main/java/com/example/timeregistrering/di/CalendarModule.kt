package com.example.timeregistrering.di

import android.content.Context
import com.example.timeregistrering.repository.CalendarRepository
import com.example.timeregistrering.data.database.dao.MoedeDao
import com.example.timeregistrering.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CalendarModule {
    @Provides
    @Singleton
    fun provideCalendarRepository(
        @ApplicationContext context: Context,
        moedeDao: MoedeDao,
        authRepository: AuthRepository
    ): CalendarRepository {
        return CalendarRepository(context, moedeDao, authRepository)
    }
}
