package com.nexus.modules.tdbr

import com.nexus.modules.tdbr.shader.ShaderExecutionLayer
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30

object PLSManager {
    var ready = false
        private set
    private var program = 0
    private var uScene = -1
    private var outputFbo = 0
    private var outputTex = 0
    private var lastW = 0
    private var lastH = 0

    private val VERT = """
#version 310 es
out vec2 vUv;
void main() {
    vec2 uv = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2);
    vUv = uv;
    gl_Position = vec4(uv * 2.0 - 1.0, 0.0, 1.0);
}
"""
    private val FRAG = """
#version 310 es
precision mediump float;
uniform sampler2D uScene;
in vec2 vUv;
out vec4 fragColor;
void main() {
    vec4 base = texture(uScene, vUv);
#ifdef NEXUS_PLS
    // PLS ativo — a extensão é declarada no driver, não no shader
    base.r += 0.01; // placeholder visual
#endif
    fragColor = base;
}
"""

    fun setup(w: Int, h: Int) {
        ShaderExecutionLayer.init()
        if (!ShaderExecutionLayer.plsAvailable) {
            println("[PLSManager] PLS não disponível")
            return
        }
        try {
            val vs = ShaderExecutionLayer.compile(GL20.GL_VERTEX_SHADER, VERT, "PLS_vert")
            val fs = ShaderExecutionLayer.compile(GL20.GL_FRAGMENT_SHADER, FRAG, "PLS_frag")
            if (vs == 0 || fs == 0) return
            program = GL20.glCreateProgram()
            GL20.glAttachShader(program, vs)
            GL20.glAttachShader(program, fs)
            GL20.glLinkProgram(program)
            GL20.glDeleteShader(vs)
            GL20.glDeleteShader(fs)
            if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
                println("[PLSManager] Link falhou: ${GL20.glGetProgramInfoLog(program)}")
                GL20.glDeleteProgram(program)
                program = 0
                return
            }
            GL20.glUseProgram(program)
            GL20.glUniform1i(GL20.glGetUniformLocation(program, "uScene"), 0)
            uScene = GL20.glGetUniformLocation(program, "uScene")
            GL20.glUseProgram(0)
            rebuildOutputFbo(w, h)
            ready = true
            println("[PLSManager] ✅ PLS pass inicializado (fullscreen triangle)")
        } catch (e: Exception) {
            println("[PLSManager] Erro: ${e.message}")
            ready = false
        }
    }

    fun render(sceneTexture: Int, fboTarget: Int, w: Int, h: Int) {
        if (!ready || program == 0) return
        if (w != lastW || h != lastH) rebuildOutputFbo(w, h)
        if (outputFbo == 0) return

        val prevFbo = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING)
        val prevProg = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM)
        val prevViewport = IntArray(4)
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, prevViewport)

        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glDisable(GL11.GL_BLEND)

        // Render para FBO interno
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, outputFbo)
        GL11.glViewport(0, 0, w, h)
        GL20.glUseProgram(program)
        GL13.glActiveTexture(GL13.GL_TEXTURE0)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, sceneTexture)
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)

        // Blit para o FBO alvo
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, outputFbo)
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, fboTarget)
        GL30.glBlitFramebuffer(0, 0, w, h, 0, 0, w, h, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST)

        // Restaurar estado GL
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, prevFbo)
        GL20.glUseProgram(prevProg)
        GL11.glViewport(prevViewport[0], prevViewport[1], prevViewport[2], prevViewport[3])
        GL11.glEnable(GL11.GL_DEPTH_TEST)
    }

    private fun rebuildOutputFbo(w: Int, h: Int) {
        if (outputFbo != 0) { GL30.glDeleteFramebuffers(outputFbo); outputFbo = 0 }
        if (outputTex != 0) { GL11.glDeleteTextures(outputTex); outputTex = 0 }
        outputTex = GL11.glGenTextures()
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, outputTex)
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, 0L)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
        outputFbo = GL30.glGenFramebuffers()
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, outputFbo)
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, outputTex, 0)
        val status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER)
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)
        if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
            println("[PLSManager] FBO incompleto: $status")
            GL30.glDeleteFramebuffers(outputFbo)
            GL11.glDeleteTextures(outputTex)
            outputFbo = 0
            outputTex = 0
            ready = false
            return
        }
        lastW = w
        lastH = h
        println("[PLSManager] FBO RGBA8: ${w}x${h}")
    }
}
