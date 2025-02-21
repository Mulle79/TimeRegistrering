package com.example.timeregistrering.util

import android.content.Context
import androidx.room.RoomDatabase
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import androidx.test.core.app.ApplicationProvider
import com.example.timeregistrering.database.TimeRegistrationDatabase
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BackupManagerTest {
    
    private lateinit var context: Context
    private lateinit var database: TimeRegistrationDatabase
    private lateinit var backupManager: BackupManager
    private lateinit var backupDir: File
    
    @Before
    fun setup() {
        context = mockk(relaxed = true)
        database = mockk(relaxed = true)
        
        every { context.getExternalFilesDir(null) } returns File("test/backups")
        backupDir = File(context.getExternalFilesDir(null), "backups")
        backupDir.mkdirs()
        
        backupManager = BackupManager(context, database)
    }
    
    @After
    fun tearDown() {
        backupDir.deleteRecursively()
    }
    
    @Test
    fun `createBackup creates encrypted backup file`() = runTest {
        // Given
        val databaseFile = File(backupDir, "test.db")
        databaseFile.createNewFile()
        
        every { context.getDatabasePath(any()) } returns databaseFile
        every { database.openHelper.databaseName } returns "test.db"
        
        // When
        val backupFile = backupManager.createBackup()
        
        // Then
        assertTrue(backupFile.exists())
        assertTrue(backupFile.name.startsWith("backup_"))
        assertTrue(backupFile.name.endsWith(".db"))
    }
    
    @Test
    fun `restoreBackup restores database from backup`() = runTest {
        // Given
        val backupFile = File(backupDir, "test_backup.db")
        backupFile.createNewFile()
        
        val databaseFile = File(backupDir, "test.db")
        every { context.getDatabasePath(any()) } returns databaseFile
        every { database.openHelper.databaseName } returns "test.db"
        
        // When
        backupManager.restoreBackup(backupFile)
        
        // Then
        verify {
            database.close()
            database.openHelper.writableDatabase
        }
    }
    
    @Test
    fun `cleanupOldBackups keeps only 5 most recent backups`() = runTest {
        // Given
        repeat(7) { i ->
            File(backupDir, "backup_$i.db").createNewFile()
            Thread.sleep(100) // Ensure different timestamps
        }
        
        // When
        backupManager.createBackup()
        
        // Then
        val backups = backupDir.listFiles()?.filter { it.name.startsWith("backup_") }
        assertEquals(5, backups?.size)
    }
    
    @Test
    fun `scheduleAutomaticBackup creates periodic work request`() = runTest {
        // Given
        val workManager = mockk<WorkManager>(relaxed = true)
        every { WorkManager.getInstance(any()) } returns workManager
        
        // When
        backupManager.scheduleAutomaticBackup()
        
        // Then
        verify {
            workManager.enqueueUniquePeriodicWork(
                "automatic_backup",
                ExistingPeriodicWorkPolicy.KEEP,
                any()
            )
        }
    }
}
