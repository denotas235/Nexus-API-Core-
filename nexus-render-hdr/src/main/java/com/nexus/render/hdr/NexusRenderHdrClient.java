package com.nexus.render.hdr;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class NexusRenderHdrClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("[NexusRenderHDR] ═══ Module loading ═══");

        // Inicializar o pipeline HDR (seguro, sem crashes)
        HdrPipeline.init();

        // Detecção de mods complementares
        if (FabricLoader.getInstance().isModLoaded("nexus-api-core")) {
            System.out.println("[NexusRenderHDR] nexus-api-core detected – "
                + "capabilities can be registered.");
        } else {
            System.out.println("[NexusRenderHDR] nexus-api-core NOT found – working standalone.");
        }
        if (FabricLoader.getInstance().isModLoaded("nexus-maliopt")) {
            System.out.println("[NexusRenderHDR] nexus-maliopt detected – TBDR integration possible.");
        }
        if (FabricLoader.getInstance().isModLoaded("nexus-shadows")) {
            System.out.println("[NexusRenderHDR] nexus-shadows detected – enhanced shadow quality possible.");
        }

        System.out.println("[NexusRenderHDR] ═══ Module ready ═══");
    }
}
