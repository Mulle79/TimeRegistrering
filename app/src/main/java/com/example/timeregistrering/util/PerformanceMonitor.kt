package com.example.timeregistrering.util

import android.os.Build
import android.os.Debug
import android.os.Trace
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

data class MemoryStats(
    val usedMemoryMB: Long,
    val freeMemoryMB: Long,
    val totalMemoryMB: Long,
    val maxMemoryMB: Long
)

data class FrameStats(
    val totalFrames: Int,
    val droppedFrames: Int,
    val averageFrameTimeMs: Double
)

data class NetworkStats(
    val totalRequests: Int,
    val failedRequests: Int,
    val averageResponseTimeMs: Double,
    val failureRate: Double
)

@Singleton
class PerformanceMonitor @Inject constructor() {
    
    private val metrics = mutableMapOf<String, Long>()
    private var frameMetrics = mutableListOf<Long>()
    private var networkMetrics = mutableListOf<NetworkMetric>()
    
    data class NetworkMetric(
        val responseTime: Long,
        val isSuccess: Boolean
    )
    
    @RequiresApi(Build.VERSION_CODES.Q)
    inline fun <T> trace(tag: String, block: () -> T): T {
        Trace.beginSection(tag)
        val startTime = System.nanoTime()
        
        return try {
            block()
        } finally {
            val duration = System.nanoTime() - startTime
            metrics[tag] = (metrics[tag] ?: 0) + duration
            
            Log.d("Performance", "$tag took ${duration / 1_000_000}ms")
            Trace.endSection()
        }
    }
    
    fun <T> Flow<T>.measureFlow(tag: String): Flow<T> = this
        .onEach { Trace.beginSection("$tag-start") }
        .map { value ->
            val startTime = System.nanoTime()
            try {
                value
            } finally {
                val duration = System.nanoTime() - startTime
                metrics["$tag-process"] = (metrics["$tag-process"] ?: 0) + duration
                Log.d("Performance", "$tag processed in ${duration / 1_000_000}ms")
            }
        }
        .catch { e ->
            Log.e("Performance", "$tag failed: ${e.message}")
            throw e
        }
        .onEach { Trace.endSection() }
    
    fun getMetrics(): Map<String, Long> = metrics.toMap()
    
    fun resetMetrics() {
        metrics.clear()
        frameMetrics.clear()
        networkMetrics.clear()
    }
    
    fun getMemoryStats(): MemoryStats {
        val runtime = Runtime.getRuntime()
        return MemoryStats(
            usedMemoryMB = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024,
            freeMemoryMB = runtime.freeMemory() / 1024 / 1024,
            totalMemoryMB = runtime.totalMemory() / 1024 / 1024,
            maxMemoryMB = runtime.maxMemory() / 1024 / 1024
        )
    }
    
    fun logMemoryStats() {
        val stats = getMemoryStats()
        Log.d("Memory", """
            Used Memory: ${stats.usedMemoryMB} MB
            Free Memory: ${stats.freeMemoryMB} MB
            Total Memory: ${stats.totalMemoryMB} MB
            Max Memory: ${stats.maxMemoryMB} MB
        """.trimIndent())
        
        if (Debug.isDebuggerConnected()) {
            Debug.getMemoryInfo(Debug.MemoryInfo())
        }
    }

    fun startFrameTimeTracking() {
        frameMetrics.clear()
    }

    fun recordFrameTime(frameTimeMs: Long) {
        frameMetrics.add(frameTimeMs)
    }

    fun stopFrameTimeTracking(): FrameStats {
        val totalFrames = frameMetrics.size
        val droppedFrames = frameMetrics.count { it > 16.67 } // Over 60 FPS threshold
        val averageFrameTime = frameMetrics.average()
        
        return FrameStats(
            totalFrames = totalFrames,
            droppedFrames = droppedFrames,
            averageFrameTimeMs = averageFrameTime
        )
    }

    fun startNetworkMonitoring() {
        networkMetrics.clear()
    }

    fun recordNetworkRequest(responseTimeMs: Long, isSuccess: Boolean) {
        networkMetrics.add(NetworkMetric(responseTimeMs, isSuccess))
    }

    fun getNetworkStats(): NetworkStats {
        val totalRequests = networkMetrics.size
        val failedRequests = networkMetrics.count { !it.isSuccess }
        val averageResponseTime = networkMetrics.map { it.responseTime }.average()
        val failureRate = if (totalRequests > 0) failedRequests.toDouble() / totalRequests else 0.0

        return NetworkStats(
            totalRequests = totalRequests,
            failedRequests = failedRequests,
            averageResponseTimeMs = averageResponseTime,
            failureRate = failureRate
        )
    }

    inline fun measureTimeMillis(block: () -> Unit): Long {
        val startTime = System.currentTimeMillis()
        block()
        return System.currentTimeMillis() - startTime
    }
}
