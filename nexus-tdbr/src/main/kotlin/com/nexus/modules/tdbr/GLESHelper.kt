package com.nexus.modules.tdbr

import org.lwjgl.opengles.GLES20

object GLESHelper {
    fun glCompileShader(type: Int, src: String): Int {
        val shader = GLES20.glCreateShader(type)
        if (shader == 0) return 0
        GLES20.glShaderSource(shader, src)
        GLES20.glCompileShader(shader)
        val status = GLES20.glGetShaderi(shader, GLES20.GL_COMPILE_STATUS)
        if (status == GLES20.GL_FALSE) {
            val log = GLES20.glGetShaderInfoLog(shader, 4096)
            System.err.println("[GLESHelper] Shader compile FAILED (type=$type):\n$log")
            GLES20.glDeleteShader(shader)
            return 0
        }
        println("[GLESHelper] Shader compiled OK: type=$type")
        return shader
    }

    fun glLinkProgram(vs: Int, fs: Int): Int {
        if (vs == 0 || fs == 0) return 0
        val prog = GLES20.glCreateProgram()
        GLES20.glAttachShader(prog, vs)
        GLES20.glAttachShader(prog, fs)
        GLES20.glLinkProgram(prog)
        val status = GLES20.glGetProgrami(prog, GLES20.GL_LINK_STATUS)
        if (status == GLES20.GL_FALSE) {
            val log = GLES20.glGetProgramInfoLog(prog, 4096)
            System.err.println("[GLESHelper] Program link FAILED:\n$log")
            GLES20.glDeleteProgram(prog)
            return 0
        }
        println("[GLESHelper] Program linked OK: id=$prog")
        return prog
    }

    const val GL_VERTEX_SHADER = 0x8B31
    const val GL_FRAGMENT_SHADER = 0x8B30
    const val GL_COMPILE_STATUS = 0x8B81
    const val GL_LINK_STATUS = 0x8B82
    const val GL_COLOR_BUFFER_BIT = 0x4000
    const val GL_DEPTH_BUFFER_BIT = 0x0100
}
