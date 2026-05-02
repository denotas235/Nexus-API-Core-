package com.nexus.modules.tdbr.util

object FullscreenQuad {
    private var initialized = false

    fun init() {
        if (initialized) return
        // VAO e VBO serão criados quando o GL estiver disponível (usando reflection)
        initialized = true
    }

    fun draw() {
        // Placeholder – a implementação real desenhará um quad fullscreen
    }
}
