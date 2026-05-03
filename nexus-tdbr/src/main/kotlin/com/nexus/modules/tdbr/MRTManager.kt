package com.nexus.modules.tdbr

import org.lwjgl.opengles.GLES20
import org.lwjgl.opengles.GLES30

object MRTManager {
    var enabled = false
        private set

    fun setup(width: Int, height: Int) {
        enabled = true
        println("[MRTManager] MRT setup: ${width}x${height}")
    }

    fun beginGeometryPass() {
        if (!enabled) return
        val handle = ResourceManager.getMRTGBufferHandle()
        if (handle != 0) {
            GLES20.glUseProgram(handle)
            GLES20.glDepthMask(true)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        }
    }

    fun endGeometryPass() { if (!enabled) return }

    fun beginLightingPass() {
        if (!enabled) return
        val handle = ResourceManager.getMRTLightingHandle()
        if (handle != 0) {
            GLES20.glUseProgram(handle)
        }
    }

    fun endLightingPass() { if (!enabled) return }
}
