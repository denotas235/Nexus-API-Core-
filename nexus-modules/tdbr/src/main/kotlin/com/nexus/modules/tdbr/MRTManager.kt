package com.nexus.modules.tdbr

import com.nexuapicore.core.ResourceManager
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30

/**
 * MRTManager — G-buffer via Multiple Render Targets.
 * Usado quando PLS não está disponível (PLSExtension.isAvailable() = false).
 * Funciona em qualquer GPU com GLES 3.0+.
 *
 * G-buffer layout:
 *   Attachment 0 — RGBA8   — albedo (RGB) + oclusão (A)
 *   Attachment 1 — RGBA8   — normal (RGB comprimida)
 *   Attachment 2 — RGBA8   — material (roughness, metallic)
 *   Depth         — DEPTH24_STENCIL8
 */
object MRTManager {
    var enabled = false
        private set

    private var fbo          = 0
    private var texAlbedo    = 0
    private var texNormal    = 0
    private var texMaterial  = 0
    private var rboDepth     = 0

    private var width  = 0
    private var height = 0

    private var lightDirX = 0.6f;  private var lightDirY = 1.0f;  private var lightDirZ = 0.8f
    private var lightR    = 1.0f;  private var lightG    = 0.95f; private var lightB    = 0.85f
    private var ambient   = 0.35f

    fun setup(w: Int, h: Int) {
        width = w; height = h

        val vsh         = loadShader("pls_gbuffer.vsh")   // reutiliza o mesmo VSH
        val gbufferFsh  = loadShader("mrt_gbuffer.fsh")
        val quadVsh     = loadShader("pls_quad.vsh")
        val lightingFsh = loadShader("mrt_lighting.fsh")

        if (vsh == null || gbufferFsh == null || quadVsh == null || lightingFsh == null) {
            println("[MRT] Falha ao carregar shaders, desativando")
            return
        }

        ResourceManager.compileMRTShaders(vsh, gbufferFsh, quadVsh, lightingFsh)
        createFramebuffer(w, h)

        enabled = true
        println("[MRT] G-buffer MRT configurado [$w x $h] — FBO=$fbo")
    }

    private fun createFramebuffer(w: Int, h: Int) {
        fbo = GL30.glGenFramebuffers()
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo)

        texAlbedo   = createTexture(w, h)
        texNormal   = createTexture(w, h)
        texMaterial = createTexture(w, h)

        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, texAlbedo,   0)
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT1, GL11.GL_TEXTURE_2D, texNormal,   0)
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT2, GL11.GL_TEXTURE_2D, texMaterial, 0)

        rboDepth = GL30.glGenRenderbuffers()
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, rboDepth)
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH24_STENCIL8, w, h)
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, rboDepth)

        val drawBuffers = intArrayOf(GL30.GL_COLOR_ATTACHMENT0, GL30.GL_COLOR_ATTACHMENT1, GL30.GL_COLOR_ATTACHMENT2)
        GL30.glDrawBuffers(drawBuffers)

        val status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER)
        if (status != GL30.GL_FRAMEBUFFER_COMPLETE)
            println("[MRT] AVISO: Framebuffer incompleto — status=0x${status.toString(16)}")

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)
    }

    private fun createTexture(w: Int, h: Int): Int {
        val id = GL11.glGenTextures()
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id)
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA8, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, 0)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
        return id
    }

    fun beginGeometryPass() {
        if (!enabled) return
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo)
        GL11.glDepthMask(true)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
        val prog = ResourceManager.getMRTGBufferHandle()
        if (prog != 0) GL20.glUseProgram(prog)
    }

    fun endGeometryPass() {
        GL20.glUseProgram(0)
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)
    }

    fun beginLightingPass() {
        if (!enabled) return
        val prog = ResourceManager.getMRTLightingHandle()
        if (prog == 0) return

        GL20.glUseProgram(prog)

        // Bind G-buffer textures
        GL30.glActiveTexture(GL30.GL_TEXTURE0)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texAlbedo)
        GL20.glUniform1i(GL20.glGetUniformLocation(prog, "uAlbedo"), 0)

        GL30.glActiveTexture(GL30.GL_TEXTURE1)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texNormal)
        GL20.glUniform1i(GL20.glGetUniformLocation(prog, "uNormal"), 1)

        GL30.glActiveTexture(GL30.GL_TEXTURE2)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texMaterial)
        GL20.glUniform1i(GL20.glGetUniformLocation(prog, "uMaterial"), 2)

        // Uniforms de luz
        GL20.glUniform3f(GL20.glGetUniformLocation(prog, "uLightDir"),   lightDirX, lightDirY, lightDirZ)
        GL20.glUniform3f(GL20.glGetUniformLocation(prog, "uLightColor"), lightR, lightG, lightB)
        GL20.glUniform1f(GL20.glGetUniformLocation(prog, "uAmbient"),    ambient)

        // Draw quad fullscreen sem VBO
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
            val path = "assets/nexus-tdbr/shaders/$name"
            Thread.currentThread().contextClassLoader.getResourceAsStream(path) ?: MRTManager::class.java.classLoader.getResourceAsStream(path)
                ?.bufferedReader()?.readText()
                ?: run { println("[MRT] Shader não encontrado: $path"); null }
        } catch (e: Exception) {
            println("[MRT] Erro ao carregar $name: ${e.message}"); null
        }
    }
}
