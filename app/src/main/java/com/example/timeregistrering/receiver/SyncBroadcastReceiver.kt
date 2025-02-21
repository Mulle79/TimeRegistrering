package com.example.timeregistrering.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.timeregistrering.service.SyncService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SyncBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                // Start sync service når enheden genstarter eller appen opdateres
                val syncIntent = Intent(context, SyncService::class.java).apply {
                    action = SyncService.ACTION_SYNC
                }
                context.startService(syncIntent)
            }
        }
    }
}
