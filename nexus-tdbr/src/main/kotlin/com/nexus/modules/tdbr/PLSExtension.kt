package com.nexus.modules.tdbr

import com.nexuapicore.core.FeatureRegistry

object PLSExtension {
    fun init(registry: FeatureRegistry) {
        val available = registry.isAvailable("PIXEL_LOCAL_STORAGE")
        println("[PLSExtension] Pixel Local Storage available: $available")
    }
}
