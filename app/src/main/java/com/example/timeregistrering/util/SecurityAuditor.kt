package com.example.timeregistrering.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityAuditor @Inject constructor(
    private val context: Context
) {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore")
    
    init {
        keyStore.load(null)
    }
    
    fun performSecurityAudit(): List<SecurityIssue> {
        val issues = mutableListOf<SecurityIssue>()
        
        // Check app permissions
        checkPermissions(issues)
        
        // Check encryption
        checkEncryption(issues)
        
        // Check secure preferences
        checkSecurePreferences(issues)
        
        // Check app signing
        checkAppSigning(issues)
        
        // Check OS version
        checkOSVersion(issues)
        
        return issues
    }
    
    private fun checkPermissions(issues: MutableList<SecurityIssue>) {
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )
        
        packageInfo.requestedPermissions?.forEach { permission ->
            if (permission.contains("LOCATION") && !permission.contains("FOREGROUND")) {
                issues.add(
                    SecurityIssue(
                        "Background Location",
                        "App requests background location. Ensure this is necessary.",
                        Severity.WARNING
                    )
                )
            }
        }
    }
    
    private fun checkEncryption(issues: MutableList<SecurityIssue>) {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
                
            if (!keyStore.containsAlias(masterKey.toString())) {
                issues.add(
                    SecurityIssue(
                        "Encryption",
                        "Master key not found in KeyStore",
                        Severity.HIGH
                    )
                )
            }
        } catch (e: Exception) {
            issues.add(
                SecurityIssue(
                    "Encryption",
                    "Error checking encryption: ${e.message}",
                    Severity.HIGH
                )
            )
        }
    }
    
    private fun checkSecurePreferences(issues: MutableList<SecurityIssue>) {
        try {
            EncryptedSharedPreferences.create(
                context,
                "secret_shared_prefs",
                MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            issues.add(
                SecurityIssue(
                    "Secure Preferences",
                    "Error with encrypted preferences: ${e.message}",
                    Severity.MEDIUM
                )
            )
        }
    }
    
    private fun checkAppSigning(issues: MutableList<SecurityIssue>) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (packageInfo.signingInfo.hasMultipleSigners()) {
                    issues.add(
                        SecurityIssue(
                            "App Signing",
                            "Multiple signing certificates found",
                            Severity.WARNING
                        )
                    )
                }
            }
        } catch (e: Exception) {
            issues.add(
                SecurityIssue(
                    "App Signing",
                    "Error checking app signing: ${e.message}",
                    Severity.MEDIUM
                )
            )
        }
    }
    
    private fun checkOSVersion(issues: MutableList<SecurityIssue>) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            issues.add(
                SecurityIssue(
                    "OS Version",
                    "App running on Android < 6.0 (API 23)",
                    Severity.HIGH
                )
            )
        }
    }
    
    data class SecurityIssue(
        val category: String,
        val description: String,
        val severity: Severity
    )
    
    enum class Severity {
        LOW,
        WARNING,
        MEDIUM,
        HIGH
    }
}
