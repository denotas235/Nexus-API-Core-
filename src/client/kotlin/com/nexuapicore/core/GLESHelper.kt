package com.nexuapicore.core

object GLESHelper {
    private val glClass: Class<*>? by lazy {
        try { Class.forName("org.lwjgl.opengl.GLES30") }
        catch (e: ClassNotFoundException) { null }
    }

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

    // constantes
    const val GL_COMPILE_STATUS = 0x8B81
    const val GL_LINK_STATUS = 0x8B82
    const val GL_VERTEX_SHADER = 0x8B31
    const val GL_FRAGMENT_SHADER = 0x8B30
    const val GL_COLOR_BUFFER_BIT = 0x00004000
    const val GL_DEPTH_BUFFER_BIT = 0x00000100
}
