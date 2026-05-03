package com.nexus.modules.tdbr.util

import org.lwjgl.opengles.GLES20
import org.lwjgl.opengles.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

object FullscreenQuad {
    private var vao = 0
    private var vbo = 0
    private var initialized = false

    fun init() {
        if (initialized) return
        val vertices = floatArrayOf(
            -1f, -1f,  1f, -1f,  -1f, 1f,  1f, 1f
        )
        val buf: FloatBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertices).apply { flip() }

        val arr = IntArray(1)
        GLES30.glGenVertexArrays(arr)
        vao = arr[0]
        GLES20.glGenBuffers(arr)
        vbo = arr[0]

        GLES30.glBindVertexArray(vao)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, buf, GLES20.GL_STATIC_DRAW)
        GLES20.glEnableVertexAttribArray(0)
        GLES20.glVertexAttribPointer(0, 2, GLES20.GL_FLOAT, false, 0, 0)
        GLES30.glBindVertexArray(0)
        initialized = true
        println("[FullscreenQuad] Quad created: vao=$vao")
    }

    fun draw() {
        if (!initialized) return
        GLES30.glBindVertexArray(vao)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES30.glBindVertexArray(0)
    }
}
