package com.nexus.modules.tdbr

import com.nexuapicore.core.ResourceManager

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

    fun beginGeometryPass() {
        if (!enabled) return
        try {
            val gl = Class.forName("org.lwjgl.opengl.GLES30")
            val useProgram = gl.getMethod("glUseProgram", Int::class.javaPrimitiveType)
            val depthMask = gl.getMethod("glDepthMask", Boolean::class.javaPrimitiveType)
            val clear = gl.getMethod("glClear", Int::class.javaPrimitiveType)
            val handle = ResourceManager.getPLSShaderHandle()
            if (handle != null) {
                useProgram.invoke(null, handle)
            }
            depthMask.invoke(null, true)
            clear.invoke(null, 0x00004000 or 0x00000100) // GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT
        } catch (e: Exception) {
            println("[PLSManager] GL error: ${e.message}")
        }
    }

    fun endGeometryPass() { /* nothing */ }
    fun beginLightingPass() { /* nothing */ }
    fun endLightingPass() { /* nothing */ }
}
