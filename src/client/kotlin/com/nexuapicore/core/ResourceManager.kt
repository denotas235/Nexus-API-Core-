package com.nexuapicore.core

object ResourceManager {
    var plsShaderHandle: Int = 0
        private set

    fun init() { }

    fun getPLSShaderHandle(): Int? = if (plsShaderHandle != 0) plsShaderHandle else null

    fun compilePLSShaders(vertexSrc: String, fragmentSrc: String) {
        val vs = GLESHelper.glCompileShader(GLESHelper.GL_VERTEX_SHADER, vertexSrc)
        val fs = GLESHelper.glCompileShader(GLESHelper.GL_FRAGMENT_SHADER, fragmentSrc)
        if (vs == 0 || fs == 0) return

        plsShaderHandle = GLESHelper.glLinkProgram(vs, fs)
        println("[ResourceManager] PLS shader program linked: $plsShaderHandle")
    }
}
