package com.nexus.render.hdr;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class NexusRenderHdrClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("[NexusRenderHDR] Module loaded.");
        if (FabricLoader.getInstance().isModLoaded("nexus-shadows")) {
            System.out.println("[NexusRenderHDR] nexus-shadows detected.");
        }
        if (FabricLoader.getInstance().isModLoaded("nexus-maliopt")) {
            System.out.println("[NexusRenderHDR] nexus-maliopt detected.");
        }
    }
}
