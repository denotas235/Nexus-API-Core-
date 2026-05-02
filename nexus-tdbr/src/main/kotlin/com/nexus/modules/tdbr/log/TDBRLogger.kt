package com.nexus.modules.tdbr.log

import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object TDBRLogger {
    private val logFile: File by lazy {
        val gameDir = FabricLoader.getInstance().gameDir.toFile()
        val dir = File(gameDir, "logs/nexus")
        if (!dir.exists()) dir.mkdirs()
        File(dir, "tdbr.log")
    }

    fun log(message: String) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        logFile.appendText("[$timestamp] $message\n")
        println("[TDBR-LOG] $message")
    }
}
