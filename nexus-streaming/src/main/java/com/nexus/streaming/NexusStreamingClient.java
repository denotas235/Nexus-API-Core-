package com.nexus.streaming;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NexusStreamingClient implements ClientModInitializer {
    public static final String MOD_ID = "nexus-streaming";
    public static final Logger LOGGER  = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("[Streaming] ═══ Module loading ═══");

        if (FabricLoader.getInstance().isModLoaded("nexus-textures")) LOGGER.info("[Streaming] nexus-textures detetado — atlas ASTC beneficia uploads incrementais.");
        if (FabricLoader.getInstance().isModLoaded("nexus-shadows"))  LOGGER.info("[Streaming] nexus-shadows detetado — buffers de geometria partilhados.");
        if (FabricLoader.getInstance().isModLoaded("nexus-render-hdr")) LOGGER.info("[Streaming] nexus-render-hdr detetado.");

        // Inicializar GL no primeiro tick (contexto GL disponivel)
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (!StreamingPipeline.isInitialized()) {
                StreamingPipeline.initGL();
            }
            // Processar fila de uploads pendentes (max 4 por tick para nao bloquear render)
            UploadQueue.processTick(4);
        });

        LOGGER.info("[Streaming] ═══ Module registado ═══");
    }
}