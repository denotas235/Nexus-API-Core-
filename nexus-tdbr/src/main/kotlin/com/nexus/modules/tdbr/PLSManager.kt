package com.nexus.modules.tdbr

import com.nexuapicore.core.ResourceManager
import org.lwjgl.opengl.GLES30

object PLSManager {
    var enabled = false
    private val plsAvailable: Boolean by lazy {
        try {
            Class.forName("org.lwjgl.opengl.EXTShaderPixelLocalStorage")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    fun setup(width: Int, height: Int) {
        if (!plsAvailable) {
            println("[PLSManager] EXTShaderPixelLocalStorage not found in LWJGL, disabling PLS")
            enabled = false
            return
        }
        try {
            // Chamada direta, pois a classe já foi verificada
            val cls = Class.forName("org.lwjgl.opengl.EXTShaderPixelLocalStorage")
            val method = cls.getMethod("glFramebufferPixelLocalStorageSize", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
            method.invoke(null, 3, width, height)
            enabled = true
            println("[PLSManager] PLS G‑buffer configured: 3 slots")
        } catch (e: Exception) {
            println("[PLSManager] Failed to setup PLS: ${e.message}")
            enabled = false
        }
    }

    // ... manter restantes métodos beginGeometryPass, etc., com a mesma lógica de reflection ou assumindo que a classe já está carregada.
    // Vou colocar a implementação original, mas com enabled a proteger
    fun beginGeometryPass() {
        if (!enabled) return
        // Supondo que o ResourceManager tem o handle do shader PLS
        ResourceManager.getPLSShaderHandle()?.let { GLES30.glUseProgram(it) }
        GLES30.glDepthMask(true)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
    }
    fun endGeometryPass() { if (!enabled) return }
    fun beginLightingPass() { if (!enabled) return /* aplicar shader de iluminação */ }
    fun endLightingPass() { if (!enabled) return }
}
