package com.nexuapicore.client

import com.nexuapicore.NexusAPI
import com.nexuapicore.core.GLESContext
import com.nexuapicore.core.pipeline.RenderPipeline
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents

class NexusAPICoreClient : ClientModInitializer {
    override fun onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register {
            println("[Nexus] CLIENT_STARTED — verificando ambiente GLES...")
            GLESContext.init()

            if (!GLESContext.available) {
                println("[Nexus] AVISO: GLES não disponível. API iniciará sem o subsistema GLES.")
            }

            // Inicia a API independentemente — o módulo TDBR saberá proteger-se
            NexusAPI.init()
        }
        WorldRenderEvents.END.register {
            try {
                RenderPipeline.executeFrame()
            } catch (t: Throwable) {
                println("[Nexus] RenderPipeline ignorado: ${t.message}")
            }
        }
    }
}
