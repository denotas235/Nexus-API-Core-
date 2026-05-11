package com.nexus.render.hdr;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NexusRenderHdrClient implements ClientModInitializer {

    public static final String MOD_ID = "nexus-render-hdr";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("[NexusRenderHDR] ═══ Module loading ═══");

        // GL NAO esta disponivel aqui — a inicializacao GL e feita no primeiro
        // frame de render via GameRendererMixin para evitar crash.

        // Detetar mods complementares do ecossistema Nexus
        if (FabricLoader.getInstance().isModLoaded("nexus-api-core")) {
            LOGGER.info("[NexusRenderHDR] nexus-api-core detetado.");
        }
        if (FabricLoader.getInstance().isModLoaded("nexus-textures")) {
            LOGGER.info("[NexusRenderHDR] nexus-textures detetado — sRGB sera aplicado apos carregamento ASTC.");
        }
        if (FabricLoader.getInstance().isModLoaded("nexus-shadows")) {
            LOGGER.info("[NexusRenderHDR] nexus-shadows detetado.");
        }

        LOGGER.info("[NexusRenderHDR] ═══ Module registado — pipeline GL inicia no primeiro frame ═══");
    }
}