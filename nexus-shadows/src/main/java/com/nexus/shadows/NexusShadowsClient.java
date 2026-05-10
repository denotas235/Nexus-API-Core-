package com.nexus.shadows;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class NexusShadowsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("[Shadows] Module loaded.");
        if (FabricLoader.getInstance().isModLoaded("nexus-api-core")) {
            System.out.println("[Shadows] nexus-api-core detected.");
        }
        if (FabricLoader.getInstance().isModLoaded("nexus-maliopt")) {
            System.out.println("[Shadows] nexus-maliopt detected – enhanced shadow pass available.");
        }
        if (FabricLoader.getInstance().isModLoaded("nexus-render-hdr")) {
            System.out.println("[Shadows] nexus-render-hdr detected – HDR + Shadows combo active.");
        }
    }
}
