package com.nexus.modules.tdbr

import org.lwjgl.opengles.GLES20

object ResourceManager {
    var plsShaderHandle = 0
        private set
    var mrtGBufferHandle = 0
        private set
    var mrtLightingHandle = 0
        private set

    fun compilePLSShaders(vertSrc: String, fragSrc: String) {
        val vs = compileShader(GLES20.GL_VERTEX_SHADER, vertSrc)
        val fs = compileShader(GLES20.GL_FRAGMENT_SHADER, fragSrc)
        if (vs == 0 || fs == 0) {
            println("[ResourceManager] PLS shader compilation failed (vs=$vs, fs=$fs)")
            return
        }
        plsShaderHandle = linkProgram(vs, fs)
        println("[ResourceManager] PLS program linked: $plsShaderHandle")
    }

    fun compileMRTShaders(gVert: String, gFrag: String, lVert: String, lFrag: String) {
        val gvs = compileShader(GLES20.GL_VERTEX_SHADER, gVert)
        val gfs = compileShader(GLES20.GL_FRAGMENT_SHADER, gFrag)
        val lvs = compileShader(GLES20.GL_VERTEX_SHADER, lVert)
        val lfs = compileShader(GLES20.GL_FRAGMENT_SHADER, lFrag)
        if (gvs != 0 && gfs != 0) {
            mrtGBufferHandle = linkProgram(gvs, gfs)
            println("[ResourceManager] MRT G-buffer program linked: $mrtGBufferHandle")
        }
        if (lvs != 0 && lfs != 0) {
            mrtLightingHandle = linkProgram(lvs, lfs)
            println("[ResourceManager] MRT lighting program linked: $mrtLightingHandle")
        }
    }

    private fun compileShader(type: Int, src: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, src)
        GLES20.glCompileShader(shader)
        val status = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] == GLES20.GL_FALSE) {
            val log = GLES20.glGetShaderInfoLog(shader, 4096)
            println("[ResourceManager] Shader compile error (type=$type): $log")
            GLES20.glDeleteShader(shader)
            return 0
        }
        return shader
    }

    private fun linkProgram(vs: Int, fs: Int): Int {
        val prog = GLES20.glCreateProgram()
        GLES20.glAttachShader(prog, vs)
        GLES20.glAttachShader(prog, fs)
        GLES20.glLinkProgram(prog)
        val status = IntArray(1)
        GLES20.glGetProgramiv(prog, GLES20.GL_LINK_STATUS, status, 0)
        if (status[0] == GLES20.GL_FALSE) {
            val log = GLES20.glGetProgramInfoLog(prog, 4096)
            println("[ResourceManager] Program link error: $log")
            GLES20.glDeleteProgram(prog)
            return 0
        }
        GLES20.glDeleteShader(vs)
        GLES20.glDeleteShader(fs)
        return prog
    }
}
