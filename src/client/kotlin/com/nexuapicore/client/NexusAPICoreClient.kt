package com.nexuapicore.client

import com.nexuapicore.NexusAPI
import com.nexuapicore.core.pipeline.RenderPipeline
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents

class NexusAPICoreClient : ClientModInitializer {
    override fun onInitializeClient() {

        // 1. Init após contexto GL pronto
        ClientLifecycleEvents.CLIENT_STARTED.register {
            println("[Nexus] GL context ready — iniciando API")
            NexusAPI.init()
        }

        // 2. Pipeline TDBR a cada frame
        WorldRenderEvents.END.register {
            try {
                RenderPipeline.executeFrame()
            } catch (e: Throwable) {
                println("[Nexus] RenderPipeline crash: ${e.message} — frame ignorado")
            }
        }
    }
}
