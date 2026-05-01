package com.nexus.modules.tdbr

import com.nexuapicore.core.ResourceManager
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20

object PLSManager {
    var enabled = false
        private set

    // Direção da luz — aproximação do sol do Minecraft
    private var lightDirX  = 0.6f
    private var lightDirY  = 1.0f
    private var lightDirZ  = 0.8f
    private var lightR     = 1.0f
    private var lightG     = 0.95f
    private var lightB     = 0.85f
    private var ambient    = 0.35f

    fun setup(width: Int, height: Int) {
        if (!PLSExtension.isAvailable()) {
            println("[TDBR] PLS not available, skipping")
            enabled = false
            return
        }

        val vsh         = loadShader("pls_gbuffer.vsh")
        val gbufferFsh  = loadShader("pls_gbuffer.fsh")
        val quadVsh     = loadShader("pls_quad.vsh")
        val lightingFsh = loadShader("pls_lighting.fsh")

        if (vsh == null || gbufferFsh == null || quadVsh == null || lightingFsh == null) {
            println("[TDBR] Falha ao carregar shaders, desativando")
            enabled = false
            return
        }

        ResourceManager.compilePLSShaders(vsh, gbufferFsh, quadVsh, lightingFsh)

        PLSExtension.glFramebufferPixelLocalStorageSize(3, width, height)
        enabled = true
        println("[TDBR] PLS G-buffer configurado [$width x $height]")
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

    fun beginLightingPass() {
        if (!enabled) return
        val prog = ResourceManager.getPLSLightingHandle()
        if (prog == 0) return

        GL20.glUseProgram(prog)

        // Enviar luz para o shader
        GL20.glUniform3f(GL20.glGetUniformLocation(prog, "uLightDir"),   lightDirX, lightDirY, lightDirZ)
        GL20.glUniform3f(GL20.glGetUniformLocation(prog, "uLightColor"), lightR, lightG, lightB)
        GL20.glUniform1f(GL20.glGetUniformLocation(prog, "uAmbient"),    ambient)

        // Desenhar quad fullscreen (sem VBO — o vertex shader gera os vértices)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3)
        GL11.glEnable(GL11.GL_DEPTH_TEST)

        GL20.glUseProgram(0)
    }

    fun updateLight(dirX: Float, dirY: Float, dirZ: Float,
                    r: Float, g: Float, b: Float, amb: Float) {
        lightDirX = dirX; lightDirY = dirY; lightDirZ = dirZ
        lightR = r; lightG = g; lightB = b; ambient = amb
    }

    private fun loadShader(name: String): String? {
        return try {
            val path = "/assets/nexus-tdbr/shaders/$name"
            PLSManager::class.java.getResourceAsStream(path)
                ?.bufferedReader()
                ?.readText()
                ?: run { println("[TDBR] Shader não encontrado: $path"); null }
        } catch (e: Exception) {
            println("[TDBR] Erro ao carregar $name: ${e.message}"); null
        }
    }
}
