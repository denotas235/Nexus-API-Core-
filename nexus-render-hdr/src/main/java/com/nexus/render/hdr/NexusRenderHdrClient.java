package com.nexus.render.hdr;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class NexusRenderHdrClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("[NexusRenderHDR] ═══ Module loading ═══");
        // HdrPipeline.init() será chamado no primeiro frame (GameRendererMixin)
        if (FabricLoader.getInstance().isModLoaded("nexus-api-core")) {
            System.out.println("[NexusRenderHDR] nexus-api-core detected.");
        }
        if (FabricLoader.getInstance().isModLoaded("nexus-maliopt")) {
            System.out.println("[NexusRenderHDR] nexus-maliopt detected.");
        }
        if (FabricLoader.getInstance().isModLoaded("nexus-shadows")) {
            System.out.println("[NexusRenderHDR] nexus-shadows detected.");
        }
        System.out.println("[NexusRenderHDR] ═══ Module ready ═══");
    }
}
