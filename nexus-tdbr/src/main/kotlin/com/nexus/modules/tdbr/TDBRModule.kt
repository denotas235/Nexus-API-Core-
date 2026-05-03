package com.nexus.modules.tdbr

import com.nexuapicore.core.FeatureRegistry
import com.nexuapicore.core.module.NexusModule
import com.nexuapicore.core.pipeline.RenderPipeline
import com.nexus.modules.tdbr.log.TDBRLogger
import com.nexus.modules.tdbr.util.FullscreenQuad
import com.nexus.modules.tdbr.util.ShaderLoader
import net.minecraft.client.MinecraftClient
import org.lwjgl.opengles.GLES20
import org.lwjgl.opengles.GLES30

class TDBRModule : NexusModule {
    override val id = "tdbr"
    override val requiredCapabilities = setOf("DEFERRED_BASELINE")
    override val optionalCapabilities = setOf(
        "FRAMEBUFFER_FETCH", "PIXEL_LOCAL_STORAGE", "FAST_MSAA",
        "ADVANCED_BLENDING", "HDR_COLOR", "SRGB_CORRECTION",
        "PRIMITIVE_BOUNDING_BOX", "BUFFER_STORAGE", "DEBUG_ROBUSTNESS"
    )

    private var outputFbo = 0
    private var outputTex = 0
    private var prevFbo = 0
    private var screenW = 0
    private var screenH = 0

    override fun onInitialize(registry: FeatureRegistry) {
        TDBRLogger.log("Initializing TDBR Module")
        DiscardHandler.init()
        FullscreenQuad.init()
        PLSManager.detect()

        val mc = MinecraftClient.getInstance()
        screenW = mc?.window?.framebufferWidth ?: 1280
        screenH = mc?.window?.framebufferHeight ?: 720

        // Criar FBO de output
        val fboArr = IntArray(1)
        val texArr = IntArray(1)
        GLES30.glGenFramebuffers(fboArr)
        GLES20.glGenTextures(texArr)
        outputFbo = fboArr[0]
        outputTex = texArr[0]

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, outputTex)
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0, GLES30.GL_RGBA8,
            screenW, screenH, 0,
            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
            null as java.nio.ByteBuffer?
        )
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, outputFbo)
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D, outputTex, 0)
        val status = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER)
        if (status != GLES30.GL_FRAMEBUFFER_COMPLETE) {
            TDBRLogger.log("FBO incompleto: $status")
        } else {
            TDBRLogger.log("FBO de output criado: fbo=$outputFbo, tex=$outputTex")
        }
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)

        // Compilar shaders
        try {
            val vertSrc = ShaderLoader.load("assets/nexus-tdbr/shaders/pls_gbuffer.vsh")
            val fragSrc = ShaderLoader.load("assets/nexus-tdbr/shaders/pls_lighting.fsh")
            if (vertSrc != null && fragSrc != null) {
                ResourceManager.compilePLSShaders(vertSrc, fragSrc)
                TDBRLogger.log("PLS shaders loaded: handle=${ResourceManager.plsShaderHandle}")
            } else {
                TDBRLogger.log("Erro: PLS shaders not found")
            }
        } catch (e: Exception) {
            TDBRLogger.log("Erro ao carregar shaders PLS: ${e.message}")
        }

        companionModule = this
    }

    fun bindFboAndClear() {
        if (outputFbo == 0) return
        prevFbo = GLES20.glGetInteger(GLES30.GL_FRAMEBUFFER_BINDING)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, outputFbo)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
    }

    fun applyPostProcess() {
        if (outputFbo == 0 || ResourceManager.plsShaderHandle == 0) return
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, prevFbo)
        GLES20.glUseProgram(ResourceManager.plsShaderHandle)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, outputTex)
        val loc = GLES20.glGetUniformLocation(ResourceManager.plsShaderHandle, "uScene")
        if (loc >= 0) GLES20.glUniform1i(loc, 0)
        FullscreenQuad.draw()
        DiscardHandler.discardAfterLighting()
    }

    override fun onRegisterPipeline(pipeline: RenderPipeline) {
        pipeline.onEndFrame { applyPostProcess() }
        TDBRLogger.log("Pipeline registado")
    }

    override fun onShutdown() {
        TDBRLogger.log("Shutdown")
    }

    companion object {
        var companionModule: TDBRModule? = null

        @JvmStatic
        fun getModuleInstance(): TDBRModule? = companionModule
    }
}
