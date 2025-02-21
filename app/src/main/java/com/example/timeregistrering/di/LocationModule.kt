package com.example.timeregistrering.di

import android.content.Context
import android.content.SharedPreferences
import com.example.timeregistrering.repository.LocationRepository
import com.example.timeregistrering.location.LocationManager
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {
    
    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("timeregistrering_prefs", Context.MODE_PRIVATE)
    }
    
    @Provides
    @Singleton
    @Named("encrypted")
    fun provideEncryptedSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("timeregistrering_prefs", Context.MODE_PRIVATE)
    }
    
    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()
    
    @Provides
    @Singleton
    fun provideLocationManager(
        @ApplicationContext context: Context,
        @Named("encrypted") sharedPreferences: SharedPreferences
    ): LocationManager {
        return LocationManager(context, sharedPreferences)
    }
    
    @Provides
    @Singleton
    fun provideLocationRepository(
        @ApplicationContext context: Context,
        sharedPreferences: SharedPreferences,
        gson: Gson
    ): LocationRepository {
        return LocationRepository(context, sharedPreferences, gson)
    }
}
