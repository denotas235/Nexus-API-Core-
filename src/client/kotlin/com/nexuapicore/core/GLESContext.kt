package com.nexuapicore.core

import org.lwjgl.opengles.GLES
import org.lwjgl.opengles.GLES20

object GLESContext {
    var available = false
        private set

    fun init() {
        try {
            // Apenas verifica se o contexto GLES já está ativo (LTW, Zink, etc.)
            val caps = GLES.getCapabilities()
            if (caps != null) {
                available = true
                val renderer = GLES20.glGetString(GLES20.GL_RENDERER)
                println("[GLESContext] Contexto GLES já ativo. Renderer: $renderer")
            }
        } catch (e: Exception) {
            println("[GLESContext] Contexto GLES não disponível: ${e.message}")
        }
    }
}
