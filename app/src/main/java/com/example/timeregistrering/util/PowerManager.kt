package com.example.timeregistrering.util

import android.content.Context
import android.os.PowerManager as AndroidPowerManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wrapper-klasse for Android's PowerManager for at hu00e5ndtere batterioptimering.
 * Dette er vigtigt for at sikre pu00e5lidelig geofencing og baggrundstjenester.
 */
@Singleton
class PowerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val powerManager: AndroidPowerManager by lazy {
        context.getSystemService(Context.POWER_SERVICE) as AndroidPowerManager
    }
    
    /**
     * Kontrollerer om appen er undtaget fra batterioptimering.
     * Dette er vigtigt for pu00e5lidelig geofencing.
     */
    fun isIgnoringBatteryOptimizations(packageName: String): Boolean {
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }
    
    /**
     * Kontrollerer om enheden er i Doze-tilstand.
     * I Doze-tilstand kan baggrundstjenester og lokationsbaserede funktioner vu00e6re begru00e6nsede.
     */
    fun isDeviceIdleMode(): Boolean {
        return powerManager.isDeviceIdleMode
    }
    
    /**
     * Kontrollerer om sku00e6rmen er tu00e6ndt.
     */
    fun isInteractive(): Boolean {
        return powerManager.isInteractive
    }
}
