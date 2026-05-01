package com.nexuapicore.core

import org.lwjgl.opengl.GL20

object ResourceManager {
    private var plsGBufferHandle: Int  = 0
    private var plsLightingHandle: Int = 0

    fun init() {
        println("[ResourceManager] Initialized")
    }

    fun compilePLSShaders(
        gbufferVsh: String,
        gbufferFsh: String,
        quadVsh: String,
        lightingFsh: String
    ) {
        plsGBufferHandle  = linkProgram(gbufferVsh, gbufferFsh)
        plsLightingHandle = linkProgram(quadVsh, lightingFsh)
        println("[ResourceManager] PLS GBuffer  program: $plsGBufferHandle")
        println("[ResourceManager] PLS Lighting program: $plsLightingHandle")
    }

    fun getPLSShaderHandle(): Int   = plsGBufferHandle
    fun getPLSLightingHandle(): Int = plsLightingHandle

    private fun compileShader(source: String, type: Int): Int {
        val id = GL20.glCreateShader(type)
        GL20.glShaderSource(id, source)
        GL20.glCompileShader(id)
        val log = GL20.glGetShaderInfoLog(id)
        if (log.isNotEmpty()) println("[ResourceManager] Shader log: $log")
        return id
    }

    private fun linkProgram(vsh: String, fsh: String): Int {
        val v    = compileShader(vsh, GL20.GL_VERTEX_SHADER)
        val f    = compileShader(fsh, GL20.GL_FRAGMENT_SHADER)
        val prog = GL20.glCreateProgram()
        GL20.glAttachShader(prog, v)
        GL20.glAttachShader(prog, f)
        GL20.glLinkProgram(prog)
        GL20.glDeleteShader(v)
        GL20.glDeleteShader(f)
        return prog
    }
}
