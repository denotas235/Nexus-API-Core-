package com.nexus.modules.tdbr

object ResourceManager {
    var plsShaderHandle: Int = 0
        private set

    fun compilePLSShaders(vertexSrc: String, fragmentSrc: String) {
        val vs = GLESHelper.glCompileShader(GLESHelper.GL_VERTEX_SHADER, vertexSrc)
        val fs = GLESHelper.glCompileShader(GLESHelper.GL_FRAGMENT_SHADER, fragmentSrc)
        if (vs == 0 || fs == 0) return
        plsShaderHandle = GLESHelper.glLinkProgram(vs, fs)
        println("[ResourceManager] PLS shader program linked: handle=$plsShaderHandle")
    }
}
