package com.nexuapicore.client

import com.nexuapicore.NexusAPI
import com.nexuapicore.core.fallback.ALLExtensionDetector
import com.nexuapicore.core.pipeline.RenderPipeline
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents

class NexusAPICoreClient : ClientModInitializer {
    override fun onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register {
            println("[Nexus] GL context ready — executando Detetor Infalível 2.0")
            ALLExtensionDetector.detectExtensions()
            NexusAPI.init()
        }
        WorldRenderEvents.END.register {
            try {
                RenderPipeline.executeFrame()
            } catch (e: Throwable) {
                println("[Nexus] RenderPipeline crash: ${e.message} — frame ignorado")
            }
        }
    }
}
