package com.nexuapicore.core

import org.lwjgl.opengl.GLES30

object ResourceManager {
    var plsShaderHandle: Int = 0
        private set

    fun init() { }

    fun getPLSShaderHandle(): Int? = if (plsShaderHandle != 0) plsShaderHandle else null

    fun compilePLSShaders(vertexSrc: String, fragmentSrc: String) {
        val vs = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER)
        GLES30.glShaderSource(vs, vertexSrc)
        GLES30.glCompileShader(vs)
        checkCompile(vs, "vertex")

        val fs = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER)
        GLES30.glShaderSource(fs, fragmentSrc)
        GLES30.glCompileShader(fs)
        checkCompile(fs, "fragment")

        plsShaderHandle = GLES30.glCreateProgram()
        GLES30.glAttachShader(plsShaderHandle, vs)
        GLES30.glAttachShader(plsShaderHandle, fs)
        GLES30.glLinkProgram(plsShaderHandle)
        checkLink(plsShaderHandle)

        GLES30.glDeleteShader(vs)
        GLES30.glDeleteShader(fs)

        println("[ResourceManager] PLS shader program linked: $plsShaderHandle")
    }

    private fun checkCompile(shader: Int, type: String) {
        if (GLES30.glGetShaderi(shader, GLES30.GL_COMPILE_STATUS) == 0) {
            val log = GLES30.glGetShaderInfoLog(shader)
            println("[ResourceManager] Shader $type compile error: $log")
        }
    }

    private fun checkLink(program: Int) {
        if (GLES30.glGetProgrami(program, GLES30.GL_LINK_STATUS) == 0) {
            val log = GLES30.glGetProgramInfoLog(program)
            println("[ResourceManager] Program link error: $log")
        }
    }
}
