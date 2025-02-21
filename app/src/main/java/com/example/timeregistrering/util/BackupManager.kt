package com.example.timeregistrering.util

import android.content.Context
import androidx.room.RoomDatabase
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.example.timeregistrering.data.database.TimeregistreringDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: TimeregistreringDatabase
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val backupDir = File(context.getExternalFilesDir(null), "backups")
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

    init {
        backupDir.mkdirs()
    }

    suspend fun createBackup(): File = withContext(Dispatchers.IO) {
        // Luk database forbindelsen midlertidigt
        database.close()

        try {
            // Opret backup fil
            val timestamp = LocalDateTime.now().format(dateFormatter)
            val backupFile = File(backupDir, "backup_$timestamp.db")

            // Krypter og gem databasen
            val encryptedFile = EncryptedFile.Builder(
                context,
                backupFile,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()

            // Kopier database fil til krypteret backup
            encryptedFile.openFileOutput().use { outputStream ->
                File(context.getDatabasePath(database.openHelper.databaseName)).inputStream().use { input ->
                    input.copyTo(outputStream)
                }
            }

            // Behold kun de seneste 5 backups
            cleanupOldBackups()

            backupFile
        } finally {
            // Genåbn database forbindelsen
            database.openHelper.writableDatabase
        }
    }

    suspend fun restoreBackup(backupFile: File) = withContext(Dispatchers.IO) {
        // Luk database forbindelsen
        database.close()

        try {
            // Dekrypter og gendan databasen
            val encryptedFile = EncryptedFile.Builder(
                context,
                backupFile,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()

            // Kopier backup til database fil
            encryptedFile.openFileInput().use { inputStream ->
                context.getDatabasePath(database.openHelper.databaseName).outputStream().use { output ->
                    inputStream.copyTo(output)
                }
            }
        } finally {
            // Genåbn database forbindelsen
            database.openHelper.writableDatabase
        }
    }

    private fun cleanupOldBackups() {
        val backups = backupDir.listFiles()?.sortedBy { it.lastModified() } ?: return
        if (backups.size > 5) {
            backups.take(backups.size - 5).forEach { it.delete() }
        }
    }

    suspend fun scheduleAutomaticBackup() {
        // Implementer WorkManager til at køre backup hver nat
        val workManager = WorkManager.getInstance(context)
        val backupWorkRequest = PeriodicWorkRequestBuilder<BackupWorker>(
            repeatInterval = 24L,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
        .setConstraints(
            Constraints.Builder()
                .setRequiresCharging(true)
                .setRequiresBatteryNotLow(true)
                .build()
        )
        .build()

        workManager.enqueueUniquePeriodicWork(
            "automatic_backup",
            ExistingPeriodicWorkPolicy.KEEP,
            backupWorkRequest
        )
    }
}
