package com.nexuapicore.core

object GLESContext {
    private var initialized = false

    fun init() {
        if (initialized) return

        // Define o nome correto ANTES de qualquer referência ao GLES
        System.setProperty("org.lwjgl.opengles.libname", "libGLESv2.so")
        initialized = true

        // Verifica o contexto que o LTW já disponibilizou
        try {
            val renderer = org.lwjgl.opengl.GL11
                .glGetString(org.lwjgl.opengl.GL11.GL_RENDERER)
            println("[GLESContext] LTW renderer ativo: $renderer")
        } catch (e: Exception) {
            println("[GLESContext] Verificação GL: ${e.message}")
        }
    }
}
