package com.nexus.modules.tdbr.util

import org.lwjgl.opengles.GLES20
import org.lwjgl.opengles.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

object FullscreenQuad {
    private var vao = 0
    private var ready = false

    fun init() {
        if (ready) return
        val vertices = floatArrayOf(
            -1f, -1f,  1f, -1f,  -1f, 1f,  1f, 1f
        )
        val vaoArr = IntArray(1)
        val vboArr = IntArray(1)
        GLES30.glGenVertexArrays(vaoArr)
        GLES20.glGenBuffers(vboArr)
        vao = vaoArr[0]
        GLES30.glBindVertexArray(vao)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboArr[0])
        val buf: FloatBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
            .put(vertices).apply { flip() }
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, buf, GLES20.GL_STATIC_DRAW)
        GLES20.glEnableVertexAttribArray(0)
        GLES20.glVertexAttribPointer(0, 2, GLES20.GL_FLOAT, false, 0, 0)
        GLES30.glBindVertexArray(0)
        ready = true
        println("[FullscreenQuad] Created VAO=$vao")
    }

    fun draw() {
        if (!ready) return
        GLES30.glBindVertexArray(vao)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES30.glBindVertexArray(0)
    }
}
