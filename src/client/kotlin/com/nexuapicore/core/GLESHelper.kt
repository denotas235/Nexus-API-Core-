package com.nexuapicore.core

object GLESHelper {
    private val glClass: Class<*>? by lazy {
        try { Class.forName("org.lwjgl.opengl.GLES30") }
        catch (e: ClassNotFoundException) { null }
    }

    // Getter público para acesso controlado
    fun getGlClass(): Class<*>? = glClass

    fun glUseProgram(program: Int) {
        try {
            val m = glClass?.getMethod("glUseProgram", Int::class.javaPrimitiveType)
            m?.invoke(null, program)
        } catch (e: Exception) { }
    }

    fun glDepthMask(flag: Boolean) {
        try {
            val m = glClass?.getMethod("glDepthMask", Boolean::class.javaPrimitiveType)
            m?.invoke(null, flag)
        } catch (e: Exception) { }
    }

    fun glClear(mask: Int) {
        try {
            val m = glClass?.getMethod("glClear", Int::class.javaPrimitiveType)
            m?.invoke(null, mask)
        } catch (e: Exception) { }
    }

    fun glGenVertexArrays(): Int {
        try {
            val m = glClass?.getMethod("glGenVertexArrays")
            return m?.invoke(null) as? Int ?: 0
        } catch (e: Exception) { return 0 }
    }

    fun glBindVertexArray(array: Int) {
        try {
            val m = glClass?.getMethod("glBindVertexArray", Int::class.javaPrimitiveType)
            m?.invoke(null, array)
        } catch (e: Exception) { }
    }

    fun glDrawElements(mode: Int, count: Int, type: Int, indices: Long) {
        try {
            val m = glClass?.getMethod("glDrawElements", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, Long::class.javaPrimitiveType)
            m?.invoke(null, mode, count, type, indices)
        } catch (e: Exception) { }
    }

    // Métodos de compilação e linkagem (já são públicos e usam glClass internamente)
    fun glCompileShader(type: Int, src: String): Int {
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
                println("[GLESHelper] Shader compile error: $log")
                return 0
            }
            return shader
        } catch (e: Exception) {
            println("[GLESHelper] Exception: ${e.message}")
            return 0
        }
    }

    fun glLinkProgram(vs: Int, fs: Int): Int {
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
                println("[GLESHelper] Program link error: $log")
                return 0
            }
            glDeleteShader?.invoke(null, vs)
            glDeleteShader?.invoke(null, fs)
            return program
        } catch (e: Exception) {
            println("[GLESHelper] Exception: ${e.message}")
            return 0
        }
    }

    const val GL_COMPILE_STATUS = 0x8B81
    const val GL_LINK_STATUS = 0x8B82
    const val GL_VERTEX_SHADER = 0x8B31
    const val GL_FRAGMENT_SHADER = 0x8B30
    const val GL_COLOR_BUFFER_BIT = 0x00004000
    const val GL_DEPTH_BUFFER_BIT = 0x00000100
}
