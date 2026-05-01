package com.nexus.modules.tdbr

import org.lwjgl.opengl.GL30

/**
 * Liberta depth e stencil do tile on-chip após o lighting pass.
 * No Mali TBDR, se não descartares explicitamente, o driver escreve
 * depth+stencil de volta para VRAM — largura de banda desperdiçada.
 * glInvalidateFramebuffer() diz ao driver: "descarta on-chip, não guardes".
 */
object DiscardHandler {

    private val DEPTH_STENCIL = intArrayOf(GL30.GL_DEPTH_STENCIL_ATTACHMENT)
    private val DEPTH_ONLY    = intArrayOf(GL30.GL_DEPTH_ATTACHMENT)

    private var initialized = false

    fun init() {
        initialized = true
        println("[DiscardHandler] Inicializado — depth+stencil descartados após frame")
    }

    /** Chama no fim do lighting pass, APÓS o draw call do quad. */
    fun discardAfterLighting() {
        if (!initialized) return
        GL30.glInvalidateFramebuffer(GL30.GL_FRAMEBUFFER, DEPTH_STENCIL)
    }

    /** Usa quando stencil ainda é necessário (ex: UI com stencil test). */
    fun discardDepthOnly() {
        if (!initialized) return
        GL30.glInvalidateFramebuffer(GL30.GL_FRAMEBUFFER, DEPTH_ONLY)
    }
}
