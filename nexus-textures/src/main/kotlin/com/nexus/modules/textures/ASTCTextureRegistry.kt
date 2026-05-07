package com.nexus.modules.textures

import net.minecraft.util.Identifier
import java.util.concurrent.ConcurrentHashMap

object ASTCTextureRegistry {
    private val astcFiles = ConcurrentHashMap<Identifier, ByteArray>()

    fun updateMap(resources: Map<Identifier, java.io.InputStream>) {
        astcFiles.clear()
        resources.forEach { (id, input) ->
            val bytes = input.readBytes()
            astcFiles[id] = bytes
        }
    }

    fun getASTCData(id: Identifier): ByteArray? {
        // Tenta id com extensão .astc
        val astcId = Identifier(id.namespace, id.path.replace(".png", ".astc"))
        return astcFiles[astcId]
    }
}
