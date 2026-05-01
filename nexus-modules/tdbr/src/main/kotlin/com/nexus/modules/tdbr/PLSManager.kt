package com.nexus.modules.tdbr

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20

object PLSManager {
    private const val PLS_ALBEDO_SLOT = 0u
    private const val PLS_NORMAL_SLOT = 1u
    private const val PLS_DEPTH_SLOT  = 2u

    private var shaderHandle: Int = 0

    var enabled = false
        private set

    fun setup(width: Int, height: Int) {
        if (!PLSExtension.isAvailable()) {
            println("[TDBR] PLS not available, skipping PLS setup")
            enabled = false
            return
        }
        PLSExtension.glFramebufferPixelLocalStorageSize(3, width, height)
        enabled = true
        println("[TDBR] PLS G-buffer configured: 3 slots (albedo, normal, depth)")
    }

    fun setShaderHandle(handle: Int) {
        shaderHandle = handle
    }

    fun bindGBuffer() {
        if (!enabled) return
        GL20.glUseProgram(shaderHandle)
    }

    fun unbindGBuffer() {
        // PLS fica no tile até ao final da passagem
    }

    fun beginGeometryPass() {
        if (!enabled) return
        GL11.glDepthMask(true)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
        bindGBuffer()
    }

    fun endGeometryPass() {
        unbindGBuffer()
    }
}
