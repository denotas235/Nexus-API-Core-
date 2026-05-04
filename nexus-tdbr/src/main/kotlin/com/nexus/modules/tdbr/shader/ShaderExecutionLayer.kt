package com.nexus.modules.tdbr.shader

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20

object ShaderExecutionLayer {
    var plsAvailable = false
        private set
    var fbFetchAvailable = false
        private set
    var initialized = false
        private set

    fun init() {
        if (initialized) return

        val renderer = GL11.glGetString(GL11.GL_RENDERER) ?: ""
        val extensions = GL11.glGetString(GL11.GL_EXTENSIONS) ?: ""

        plsAvailable = extensions.contains("GL_EXT_shader_pixel_local_storage")
        fbFetchAvailable = extensions.contains("GL_ARM_shader_framebuffer_fetch")

        // Modo forçado Mali — ativa PLS se o hardware for Mali e a extensão não aparecer
        // (o LTW ou outro layer pode estar a esconder)
        if (!plsAvailable && renderer.contains("Mali")) {
            plsAvailable = true
            println("[SEL] Modo forçado Mali: PLS ativado (hardware Bifrost/Valhall confirmado)")
        }

        initialized = true
        println("[SEL] PLS: $plsAvailable, FB_Fetch: $fbFetchAvailable (renderer: $renderer)")
    }

    fun compile(type: Int, source: String, debugName: String): Int {
        val transformed = injectDefines(source)
        val id = GL20.glCreateShader(type)
        GL20.glShaderSource(id, transformed)
        GL20.glCompileShader(id)
        if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            val log = GL20.glGetShaderInfoLog(id)
            System.err.println("[SEL] Shader $debugName compile FAILED:\n$log")
            GL20.glDeleteShader(id)
            return 0
        }
        println("[SEL] Shader $debugName compiled OK")
        return id
    }

    private fun injectDefines(source: String): String {
        val sb = StringBuilder()
        sb.append("// ── Nexus ShaderExecutionLayer ──\n")
        sb.append("#define NEXUS_OPT 1\n")
        if (plsAvailable) sb.append("#define NEXUS_PLS 1\n")
        if (fbFetchAvailable) sb.append("#define NEXUS_FB_FETCH 1\n")
        sb.append("// ──────────────────────────────────\n")
        val versionIdx = source.indexOf("#version")
        return if (versionIdx >= 0) {
            val endOfVersionLine = source.indexOf('\n', versionIdx)
            source.substring(0, endOfVersionLine + 1) + sb.toString() + source.substring(endOfVersionLine + 1)
        } else {
            sb.toString() + source
        }
    }
}
