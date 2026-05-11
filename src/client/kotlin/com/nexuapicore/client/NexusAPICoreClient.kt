package com.nexuapicore.client

import com.nexuapicore.NexusAPI
import com.nexuapicore.core.fallback.ALLExtensionDetector
import com.nexuapicore.core.pipeline.RenderPipeline
import com.maliopt.pipeline.ShadowPass
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents

class NexusAPICoreClient : ClientModInitializer {
    override fun onInitializeClient() {

        ClientLifecycleEvents.CLIENT_STARTED.register { client ->
            // 1. Detectar extensões GPU
            println("[Nexus] GL context ready — executando Detetor Infalivel 2.0")
            val detected = ALLExtensionDetector.detectExtensions()
            println("[Nexus] Detetor concluido — ${detected.size} extensoes encontradas.")

            // 2. Inicializa API com extensoes reais
            NexusAPI.init(detected)

            // 3. Inicializa ShadowPass (cria FBO + shaders)
            ShadowPass.init()
        }

        // Shadow map — ANTES das entidades (gera depth map da cena)
        WorldRenderEvents.BEFORE_ENTITIES.register {
            try {
                val mc = net.minecraft.client.MinecraftClient.getInstance()
                if (mc != null && mc.world != null && ShadowPass.isReady()) {
                    ShadowPass.render(mc)
                }
            } catch (t: Throwable) {
                println("[Nexus] ShadowPass.render erro: ${t.message}")
            }
        }

        // Post-process — APOS tudo renderizado pelo Minecraft
        WorldRenderEvents.END.register {
            try {
                RenderPipeline.executeFrame()
            } catch (e: Throwable) {
                println("[Nexus] RenderPipeline crash: ${e.message} — frame ignorado")
            }
        }
    }
}
