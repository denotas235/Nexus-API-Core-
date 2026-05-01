package com.nexuapicore.core.pipeline

import com.nexuapicore.core.module.NexusModule

object RenderPipeline {

    private val beginFrameCallbacks  = mutableListOf<() -> Unit>()
    private val geometryPassCallbacks = mutableListOf<() -> Unit>()
    private val lightingPassCallbacks = mutableListOf<() -> Unit>()
    private val endFrameCallbacks    = mutableListOf<() -> Unit>()

    fun onBeginFrame(callback: () -> Unit)   { beginFrameCallbacks.add(callback) }
    fun onGeometryPass(callback: () -> Unit) { geometryPassCallbacks.add(callback) }
    fun onLightingPass(callback: () -> Unit) { lightingPassCallbacks.add(callback) }
    fun onEndFrame(callback: () -> Unit)     { endFrameCallbacks.add(callback) }

    fun assemble(modules: List<NexusModule>) {
        println("[RenderPipeline] Assembled with ${modules.size} module(s)")
    }

    fun executeFrame() {
        beginFrameCallbacks.forEach  { it() }
        geometryPassCallbacks.forEach { it() }
        lightingPassCallbacks.forEach { it() }
        endFrameCallbacks.forEach    { it() }
    }
}
