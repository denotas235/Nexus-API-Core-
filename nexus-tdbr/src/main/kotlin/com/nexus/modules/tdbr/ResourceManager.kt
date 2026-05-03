package com.nexus.modules.tdbr

import com.nexus.modules.tdbr.util.ShaderLoader
import org.lwjgl.opengles.GLES20

object ResourceManager {
    var plsShaderHandle = 0
        private set
    var mrtGBufferHandle = 0
        private set
    var mrtLightingHandle = 0
        private set

    fun compilePLSShaders(vertSrc: String, fragSrc: String) {
        val vs = compileShader(GLES20.GL_VERTEX_SHADER, vertSrc)
        val fs = compileShader(GLES20.GL_FRAGMENT_SHADER, fragSrc)
        if (vs == 0 || fs == 0) {
            println("[ResourceManager] PLS shader compilation failed (vs=$vs, fs=$fs)")
            return
        }
        plsShaderHandle = linkProgram(vs, fs)
        println("[ResourceManager] PLS program linked: $plsShaderHandle")
    }

    fun getMRTGBufferHandle(): Int = mrtGBufferHandle
    fun getMRTLightingHandle(): Int = mrtLightingHandle

    fun compileMRTShaders() {
        val vertSrc = ShaderLoader.load("assets/nexus-tdbr/shaders/mrt_quad.vsh")
        val gBufSrc = ShaderLoader.load("assets/nexus-tdbr/shaders/mrt_gbuffer.fsh")
        val lightSrc = ShaderLoader.load("assets/nexus-tdbr/shaders/mrt_lighting.fsh")

        if (vertSrc == null || gBufSrc == null || lightSrc == null) {
            println("[ResourceManager] MRT shaders not found in classpath")
            return
        }

        val gBufVert = compileShader(GLES20.GL_VERTEX_SHADER, vertSrc)
        val gBufFrag = compileShader(GLES20.GL_FRAGMENT_SHADER, gBufSrc)
        mrtGBufferHandle = linkProgram(gBufVert, gBufFrag)

        val lightVert = compileShader(GLES20.GL_VERTEX_SHADER, vertSrc)
        val lightFrag = compileShader(GLES20.GL_FRAGMENT_SHADER, lightSrc)
        mrtLightingHandle = linkProgram(lightVert, lightFrag)

        if (mrtGBufferHandle != 0 && mrtLightingHandle != 0) {
            println("[ResourceManager] MRT shaders compiled OK")
        } else {
            println("[ResourceManager] MRT shader compile failed")
        }
    }

    private fun compileShader(type: Int, src: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, src)
        GLES20.glCompileShader(shader)
        val status = GLES20.glGetShaderi(shader, GLES20.GL_COMPILE_STATUS)
        if (status == GLES20.GL_FALSE) {
            val log = GLES20.glGetShaderInfoLog(shader, 4096)
            println("[ResourceManager] Shader compile error (type=$type): $log")
            GLES20.glDeleteShader(shader)
            return 0
        }
        return shader
    }

    private fun linkProgram(vs: Int, fs: Int): Int {
        val prog = GLES20.glCreateProgram()
        GLES20.glAttachShader(prog, vs)
        GLES20.glAttachShader(prog, fs)
        GLES20.glLinkProgram(prog)
        val status = GLES20.glGetProgrami(prog, GLES20.GL_LINK_STATUS)
        if (status == GLES20.GL_FALSE) {
            val log = GLES20.glGetProgramInfoLog(prog, 4096)
            println("[ResourceManager] Program link error: $log")
            GLES20.glDeleteProgram(prog)
            return 0
        }
        GLES20.glDeleteShader(vs)
        GLES20.glDeleteShader(fs)
        return prog
    }
}
