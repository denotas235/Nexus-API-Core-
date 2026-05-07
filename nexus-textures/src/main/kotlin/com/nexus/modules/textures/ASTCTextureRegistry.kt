package com.nexus.modules.textures

import java.util.concurrent.ConcurrentHashMap

object ASTCTextureRegistry {
    private val astcFiles = ConcurrentHashMap<String, ByteArray>()

    fun put(path: String, data: ByteArray) {
        astcFiles[path] = data
    }

    fun getASTCData(path: String): ByteArray? = astcFiles[path]

    fun hasASTCTextures(): Boolean = astcFiles.isNotEmpty()

    fun count(): Int = astcFiles.size

    fun clear() = astcFiles.clear()
}
