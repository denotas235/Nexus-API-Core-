package com.nexus.modules.tdbr

import com.nexuapicore.core.ResourceManager
import org.lwjgl.opengl.GLES20
import org.lwjgl.opengl.GLES30

object PLSManager {
    var enabled = false

    fun detectPLS(): Boolean {
        val extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS)
        return extensions != null && extensions.contains("GL_EXT_shader_pixel_local_storage")
    }

    fun setup(width: Int, height: Int) {
        if (!enabled) return
        try {
            // A extensão é activada no shader, não precisamos de chamar glFramebufferPixelLocalStorageSizeEXT
            println("[PLSManager] PLS activado via shader (sem dependência LWJGL)")
        } catch (e: Exception) {
            println("[PLSManager] Erro ao configurar PLS: ${e.message}")
            enabled = false
        }
    }

    fun beginGeometryPass() {
        if (!enabled) return
        ResourceManager.getPLSShaderHandle()?.let { GLES30.glUseProgram(it) }
        GLES30.glDepthMask(true)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
    }
    fun endGeometryPass() { if (!enabled) return }
    fun beginLightingPass() { if (!enabled) return }
    fun endLightingPass() { if (!enabled) return }
}
