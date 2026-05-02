package com.nexus.modules.tdbr.util

import com.nexus.modules.tdbr.log.TDBRLogger

object Profiler {
    private val gpuTimes = mutableListOf<Long>()
    private var totalFrames = 0L
    private var startTime = System.currentTimeMillis()

    fun onFrame(cpuTimeMs: Long, gpuTimeMs: Long) {
        gpuTimes.add(gpuTimeMs)
        totalFrames++
        if (totalFrames % 60 == 0L) {
            val elapsed = System.currentTimeMillis() - startTime
            val avgFPS = totalFrames * 1000.0 / elapsed
            val avgGPU = gpuTimes.average()
            TDBRLogger.log("FPS: ${"%.1f".format(avgFPS)} | GPU: ${"%.2f".format(avgGPU)} ms")
            gpuTimes.clear()
            totalFrames = 0
            startTime = System.currentTimeMillis()
        }
    }

    fun getLastGPU(): Long = gpuTimes.lastOrNull() ?: 0L
    fun getFPS(): Double {
        val elapsed = System.currentTimeMillis() - startTime
        return if (elapsed > 0) totalFrames * 1000.0 / elapsed else 0.0
    }
}
