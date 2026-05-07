package com.nexus.modules.textures

import net.minecraft.util.Identifier
import java.util.concurrent.ConcurrentHashMap

object ASTCTextureRegistry {
    private val astcFiles = ConcurrentHashMap<Identifier, ByteArray>()

    fun updateMap(resources: Map<Identifier, InputStream>) {
        resources.forEach { (id, input) ->
            val bytes = input.readBytes()
            astcFiles[id] = bytes
        }
    }

    fun getASTCData(id: Identifier): ByteArray? {
        val astcId = Identifier(id.namespace, id.path.replace(".png", ".astc"))
        return astcFiles[astcId]
    }

    fun hasASTCTextures(): Boolean = astcFiles.isNotEmpty()
    
    fun put(id: Identifier, data: ByteArray) {
        astcFiles[id] = data
    }
    
    fun count(): Int = astcFiles.size
}
