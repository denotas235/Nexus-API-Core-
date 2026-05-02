package com.nexuapicore.core

object ResourceManager {
    var plsShaderHandle: Int = 0
        private set
    var mrtGBufferHandle: Int = 0
        private set
    var mrtLightingHandle: Int = 0
        private set

    fun init() { }

    fun getPLSShaderHandle(): Int = plsShaderHandle

    fun compilePLSShaders(vertexSrc: String, fragmentSrc: String) {
        val vs = GLESHelper.glCompileShader(GLESHelper.GL_VERTEX_SHADER, vertexSrc)
        val fs = GLESHelper.glCompileShader(GLESHelper.GL_FRAGMENT_SHADER, fragmentSrc)
        if (vs == 0 || fs == 0) return
        plsShaderHandle = GLESHelper.glLinkProgram(vs, fs)
        println("[ResourceManager] PLS shader program linked: $plsShaderHandle")
    }

    fun getMRTGBufferHandle(): Int = mrtGBufferHandle
    fun getMRTLightingHandle(): Int = mrtLightingHandle

    fun compileMRTShaders(gbufferVert: String, gbufferFrag: String, lightingVert: String, lightingFrag: String) {
        val gvs = GLESHelper.glCompileShader(GLESHelper.GL_VERTEX_SHADER, gbufferVert)
        val gfs = GLESHelper.glCompileShader(GLESHelper.GL_FRAGMENT_SHADER, gbufferFrag)
        if (gvs != 0 && gfs != 0) {
            mrtGBufferHandle = GLESHelper.glLinkProgram(gvs, gfs)
            println("[ResourceManager] MRT G‑buffer program linked: $mrtGBufferHandle")
        }
        val lvs = GLESHelper.glCompileShader(GLESHelper.GL_VERTEX_SHADER, lightingVert)
        val lfs = GLESHelper.glCompileShader(GLESHelper.GL_FRAGMENT_SHADER, lightingFrag)
        if (lvs != 0 && lfs != 0) {
            mrtLightingHandle = GLESHelper.glLinkProgram(lvs, lfs)
            println("[ResourceManager] MRT lighting program linked: $mrtLightingHandle")
        }
    }
}
