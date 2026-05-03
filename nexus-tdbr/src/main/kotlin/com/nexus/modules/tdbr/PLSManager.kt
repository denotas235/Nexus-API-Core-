package com.nexus.modules.tdbr

import com.nexus.modules.tdbr.util.FullscreenQuad
import org.lwjgl.opengles.GLES20
import org.lwjgl.opengles.GLES30

object PLSManager {
    var enabled = false
    private var outputFbo = 0
    private var outputTex = 0
    private var width = 0
    private var height = 0

    fun setup(w: Int, h: Int) {
        width = w
        height = h
        enabled = true
        println("[PLSManager] PLS activado — criando FBO de output")

        // Cria FBO de output
        val arr = IntArray(1)
        GLES30.glGenFramebuffers(arr)
        outputFbo = arr[0]
        GLES20.glGenTextures(arr)
        outputTex = arr[0]

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, outputTex)
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES30.GL_RGBA8, w, h, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, outputFbo)
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, outputTex, 0)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)

        println("[PLSManager] FBO created: id=$outputFbo, tex=$outputTex")
    }

    fun beginFrame() {
        if (!enabled) return
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, outputFbo)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
    }

    fun endFrame() {
        if (!enabled) return
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
    }

    fun renderOutput() {
        if (!enabled || ResourceManager.plsShaderHandle == 0) return
        GLES20.glUseProgram(ResourceManager.plsShaderHandle)
        FullscreenQuad.draw()
    }
}
