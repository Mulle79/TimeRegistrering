package com.example.timeregistrering.di

import android.content.Context
import android.content.SharedPreferences
import com.example.timeregistrering.repository.LocationRepository
import com.example.timeregistrering.location.LocationManager
import com.example.timeregistrering.location.GeofencingService
import com.example.timeregistrering.util.PowerManager
import com.example.timeregistrering.util.NotificationHelper
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
    fun providePowerManager(@ApplicationContext context: Context): PowerManager {
        return PowerManager(context)
    }
    
    @Provides
    @Singleton
    fun provideNotificationHelper(@ApplicationContext context: Context): NotificationHelper {
        return NotificationHelper(context)
    }
    
    @Provides
    @Singleton
    fun provideGeofencingService(
        @ApplicationContext context: Context,
        powerManager: PowerManager
    ): GeofencingService {
        return GeofencingService(context, powerManager)
    }
    
    @Provides
    @Singleton
    fun provideLocationManager(
        @ApplicationContext context: Context,
        @Named("encrypted") sharedPreferences: SharedPreferences,
        geofencingService: GeofencingService
    ): LocationManager {
        return LocationManager(context, sharedPreferences, geofencingService)
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
