package com.nexus.modules.tdbr

import org.lwjgl.opengl.GL43

/**
 * Liberta depth e stencil do tile on-chip após o lighting pass.
 * glInvalidateFramebuffer é GL 4.3 core (GL43 no LWJGL).
 * No Android GLES 3.0+ é core também — mesmo efeito.
 */
object DiscardHandler {

    private val DEPTH_STENCIL = intArrayOf(GL43.GL_DEPTH_STENCIL_ATTACHMENT)
    private val DEPTH_ONLY    = intArrayOf(GL43.GL_DEPTH_ATTACHMENT)

    private var initialized = false

    fun init() {
        initialized = true
        println("[DiscardHandler] Inicializado — depth+stencil descartados após frame")
    }

    fun discardAfterLighting() {
        if (!initialized) return
        GL43.glInvalidateFramebuffer(GL43.GL_FRAMEBUFFER, DEPTH_STENCIL)
    }

    fun discardDepthOnly() {
        if (!initialized) return
        GL43.glInvalidateFramebuffer(GL43.GL_FRAMEBUFFER, DEPTH_ONLY)
    }
}
