package com.nexus.modules.tdbr

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30

object MRTManager {
    var enabled = false
        private set

    fun setup(width: Int, height: Int) {
        enabled = true
        println("[MRTManager] MRT setup: ${width}x${height}")
    }

    fun beginGeometryPass() {
        if (!enabled) return
        GL20.glDepthMask(true)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
    }

    fun endGeometryPass() { if (!enabled) return }

    fun beginLightingPass() {
        if (!enabled) return
        // Placeholder — MRT lighting pass será implementado futuramente
    }

    fun endLightingPass() { if (!enabled) return }
}
