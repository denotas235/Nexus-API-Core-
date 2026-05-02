package com.nexuapicore.client

import com.nexuapicore.NexusAPI
import com.nexuapicore.core.PerformanceGuard
import com.nexuapicore.core.pipeline.RenderPipeline
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient

class NexusAPICoreClient : ClientModInitializer {
    override fun onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register {
            println("[Nexus] GL context ready — iniciando API")
            NexusAPI.init()
            PerformanceGuard.init()
        }
        WorldRenderEvents.END.register {
            try {
                RenderPipeline.executeFrame()
            } catch (e: Throwable) {
                println("[Nexus] RenderPipeline crash: ${e.message} — frame ignorado")
            }
            PerformanceGuard.onFrame(MinecraftClient.getInstance().currentFps)
        }
        ClientTickEvents.END_CLIENT_TICK.register {
            PerformanceGuard.onTick()
        }
    }
}
