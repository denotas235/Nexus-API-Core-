package com.nexuapicore.client

import com.nexuapicore.NexusAPI
import com.nexuapicore.core.pipeline.RenderPipeline
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents

class NexusAPICoreClient : ClientModInitializer {
    override fun onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register {
            // Define as bibliotecas nativas corretas ANTES de qualquer módulo usar GLES
            System.setProperty("org.lwjgl.opengles.libname", "libGLESv2.so")
            System.setProperty("org.lwjgl.egl.libname", "libEGL.so")
            println("[Nexus] Propriedades GLES definidas. Iniciando API...")
            NexusAPI.init()
        }
        WorldRenderEvents.END.register {
            try {
                RenderPipeline.executeFrame()
            } catch (t: Throwable) {
                println("[Nexus] RenderPipeline: ${t.message}")
            }
        }
    }
}
