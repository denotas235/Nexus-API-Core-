package com.nexus.modules.tdbr

import com.nexuapicore.core.ResourceManager
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20

object PLSManager {
    private const val PLS_ALBEDO_SLOT = 0u
    private const val PLS_NORMAL_SLOT = 1u
    private const val PLS_DEPTH_SLOT  = 2u

    var enabled = false
        private set

    fun setup(width: Int, height: Int) {
        if (!PLSExtension.isAvailable()) {
            println("[TDBR] PLS not available, skipping PLS setup")
            enabled = false
            return
        }

        // Carregar shaders do resources
        val vsh = loadShader("pls_gbuffer.vsh")
        val gbufferFsh = loadShader("pls_gbuffer.fsh")
        val lightingFsh = loadShader("pls_lighting.fsh")

        if (vsh == null || gbufferFsh == null || lightingFsh == null) {
            println("[TDBR] Falha ao carregar shaders PLS, desativando")
            enabled = false
            return
        }

        ResourceManager.compilePLSShaders(vsh, gbufferFsh, lightingFsh)

        PLSExtension.glFramebufferPixelLocalStorageSize(3, width, height)
        enabled = true
        println("[TDBR] PLS G-buffer configurado: 3 slots (albedo, normal, depth) [$width x $height]")
    }

    fun bindGBuffer() {
        if (!enabled) return
        val handle = ResourceManager.getPLSShaderHandle()
        if (handle == 0) return
        GL20.glUseProgram(handle)
    }

    fun unbindGBuffer() {
        GL20.glUseProgram(0)
    }

    fun beginGeometryPass() {
        if (!enabled) return
        GL11.glDepthMask(true)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
        bindGBuffer()
    }

    fun endGeometryPass() {
        unbindGBuffer()
    }

    private fun loadShader(name: String): String? {
        return try {
            val path = "/assets/nexus-tdbr/shaders/$name"
            PLSManager::class.java.getResourceAsStream(path)
                ?.bufferedReader()
                ?.readText()
                ?: run {
                    println("[TDBR] Shader não encontrado: $path")
                    null
                }
        } catch (e: Exception) {
            println("[TDBR] Erro ao carregar shader $name: ${e.message}")
            null
        }
    }
}
