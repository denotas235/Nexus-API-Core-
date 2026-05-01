package com.nexus.modules.tdbr

import com.nexuapicore.core.ResourceManager
import org.lwjgl.opengl.GLES30
import org.lwjgl.opengl.GLES31
import org.lwjgl.opengl.EXTShaderPixelLocalStorage

object PLSManager {
    private const val PLS_ALBEDO_SLOT = 0u
    private const val PLS_NORMAL_SLOT = 1u
    private const val PLS_DEPTH_SLOT  = 2u

    var enabled = false
        private set

    fun setup(width: Int, height: Int) {
        // Verificar se a extensão está disponível (já confirmado pelo FeatureRegistry)
        if (!EXTShaderPixelLocalStorage.isAvailable()) {
            println("[TDBR] PLS not available, skipping PLS setup")
            enabled = false
            return
        }

        // Configurar os slots de PLS (cada um é um render target interno do tile)
        EXTShaderPixelLocalStorage.glFramebufferPixelLocalStorageSize(3, width, height)
        enabled = true
        println("[TDBR] PLS G‑buffer configured: 3 slots (albedo, normal, depth)")
    }

    fun bindGBuffer() {
        if (!enabled) return
        // Ativar PLS; os shaders usarão os slots definidos
        val handle = ResourceManager.getPLSShaderHandle() // teremos que registar o shader
        GLES30.glUseProgram(handle)
    }

    fun unbindGBuffer() {
        // Nada a fazer; o PLS fica no tile até ao final da passagem
    }

    fun beginGeometryPass() {
        if (!enabled) return
        GLES30.glDepthMask(true)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        bindGBuffer()
    }

    fun endGeometryPass() {
        // O shading será feito no lighting pass, então apenas desvinculamos
        unbindGBuffer()
    }
}
