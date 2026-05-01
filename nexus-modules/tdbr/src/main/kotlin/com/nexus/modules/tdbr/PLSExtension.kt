package com.nexus.modules.tdbr

import com.nexuapicore.core.FeatureRegistry

object PLSExtension {
    private var registry: FeatureRegistry? = null

    fun init(reg: FeatureRegistry) {
        registry = reg
        val available = isAvailable()
        println("[PLSExtension] GL_EXT_shader_pixel_local_storage: $available")
    }

    fun isAvailable(): Boolean =
        registry?.isAvailable("PIXEL_LOCAL_STORAGE") ?: false

    // Stub de compilação — chamada real feita via JNI/nativo em runtime
    fun glFramebufferPixelLocalStorageSize(size: Int, width: Int, height: Int) {}
}
