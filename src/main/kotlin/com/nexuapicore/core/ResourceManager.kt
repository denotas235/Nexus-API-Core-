package com.nexuapicore.core

import org.lwjgl.opengl.GL20

object ResourceManager {
    private var mrtGBufferProgram = 0
    private var mrtLightingProgram = 0

    fun compileMRTShaders(
        vertexShader: String,
        gbufferFragShader: String,
        quadVertexShader: String,
        lightingFragShader: String
    ) {
        // Compile G-Buffer shader program
        mrtGBufferProgram = compileShaderProgram(vertexShader, gbufferFragShader)

        // Compile Lighting shader program
        mrtLightingProgram = compileShaderProgram(quadVertexShader, lightingFragShader)
    }

    fun getMRTGBufferHandle(): Int {
        return mrtGBufferProgram
    }

    fun getMRTLightingHandle(): Int {
        return mrtLightingProgram
    }

    private fun compileShaderProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER)
        GL20.glShaderSource(vertexShader, vertexSource)
        GL20.glCompileShader(vertexShader)

        val fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER)
        GL20.glShaderSource(fragmentShader, fragmentSource)
        GL20.glCompileShader(fragmentShader)

        val program = GL20.glCreateProgram()
        GL20.glAttachShader(program, vertexShader)
        GL20.glAttachShader(program, fragmentShader)
        GL20.glLinkProgram(program)

        GL20.glDeleteShader(vertexShader)
        GL20.glDeleteShader(fragmentShader)

        return program
    }
}
