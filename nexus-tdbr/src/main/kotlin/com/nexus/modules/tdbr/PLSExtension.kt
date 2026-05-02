package com.nexus.modules.tdbr

import com.nexuapicore.core.FeatureRegistry
import org.lwjgl.opengl.GL30

object PLSExtension {
    private var registry: FeatureRegistry? = null

    fun init(reg: FeatureRegistry) {
        registry = reg
        println("[PLSExtension] GL_EXT_shader_pixel_local_storage: ${isAvailable()}")
    }

    fun isAvailable(): Boolean =
        registry?.isAvailable("PIXEL_LOCAL_STORAGE") ?: false

    /**
     * glFramebufferParameteri(GL_FRAMEBUFFER, GL_FRAMEBUFFER_DEFAULT_WIDTH/HEIGHT, value)
     * No GLES 3.1+ / GL 4.3+ define o tamanho do framebuffer sem attachments,
     * que é o que o PLS precisa para alocar a memória on-chip correctamente.
     * Constantes: GL_FRAMEBUFFER_DEFAULT_WIDTH = 0x9310, HEIGHT = 0x9311
     */
    fun glFramebufferPixelLocalStorageSize(slots: Int, width: Int, height: Int) {
        if (!isAvailable()) return
        try {
            // Bind default framebuffer
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)
            // Define dimensões do framebuffer default para o PLS
            org.lwjgl.opengl.GL43.glFramebufferParameteri(
                GL30.GL_FRAMEBUFFER, 0x9310, width)   // GL_FRAMEBUFFER_DEFAULT_WIDTH
            org.lwjgl.opengl.GL43.glFramebufferParameteri(
                GL30.GL_FRAMEBUFFER, 0x9311, height)  // GL_FRAMEBUFFER_DEFAULT_HEIGHT
            println("[PLSExtension] PLS framebuffer size definido: ${width}x${height} slots=$slots")
        } catch (e: Throwable) {
            println("[PLSExtension] glFramebufferParameteri falhou: ${e.message} — PLS continua sem size explícito")
        }
    }
}
