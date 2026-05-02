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

// Funções auxiliares em GLESHelper (completar os métodos necessários)
private fun GLESHelper.glCompileShader(type: Int, src: String): Int {
    try {
        val glCreateShader = glClass?.getMethod("glCreateShader", Int::class.javaPrimitiveType)
        val glShaderSource = glClass?.getMethod("glShaderSource", Int::class.javaPrimitiveType, String::class.java)
        val glCompileShader = glClass?.getMethod("glCompileShader", Int::class.javaPrimitiveType)
        val glGetShaderi = glClass?.getMethod("glGetShaderi", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
        val glGetShaderInfoLog = glClass?.getMethod("glGetShaderInfoLog", Int::class.javaPrimitiveType)

        val shader = glCreateShader?.invoke(null, type) as? Int ?: return 0
        glShaderSource?.invoke(null, shader, src)
        glCompileShader?.invoke(null, shader)
        val status = glGetShaderi?.invoke(null, shader, GL_COMPILE_STATUS) as? Int ?: 0
        if (status == 0) {
            val log = glGetShaderInfoLog?.invoke(null, shader) as? String ?: ""
            println("[ResourceManager] Shader compile error: $log")
            return 0
        }
        return shader
    } catch (e: Exception) {
        println("[ResourceManager] Exception: ${e.message}")
        return 0
    }
}

private fun GLESHelper.glLinkProgram(vs: Int, fs: Int): Int {
    try {
        val glCreateProgram = glClass?.getMethod("glCreateProgram")
        val glAttachShader = glClass?.getMethod("glAttachShader", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
        val glLinkProgram = glClass?.getMethod("glLinkProgram", Int::class.javaPrimitiveType)
        val glGetProgrami = glClass?.getMethod("glGetProgrami", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
        val glGetProgramInfoLog = glClass?.getMethod("glGetProgramInfoLog", Int::class.javaPrimitiveType)
        val glDeleteShader = glClass?.getMethod("glDeleteShader", Int::class.javaPrimitiveType)

        val program = glCreateProgram?.invoke(null) as? Int ?: return 0
        glAttachShader?.invoke(null, program, vs)
        glAttachShader?.invoke(null, program, fs)
        glLinkProgram?.invoke(null, program)
        val status = glGetProgrami?.invoke(null, program, GL_LINK_STATUS) as? Int ?: 0
        if (status == 0) {
            val log = glGetProgramInfoLog?.invoke(null, program) as? String ?: ""
            println("[ResourceManager] Program link error: $log")
            return 0
        }
        glDeleteShader?.invoke(null, vs)
        glDeleteShader?.invoke(null, fs)
        return program
    } catch (e: Exception) {
        return 0
    }
}
