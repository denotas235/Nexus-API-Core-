package com.nexus.nefu;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class NexusNefuClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("[NEFU] ================================");
        System.out.println("[NEFU] NexusFusion Render (NEFU) v1.0");
        System.out.println("[NEFU] ================================");

        NefuCoreEngine.init();
        BatchManager.init();
        TierManager.detectTier();

        if (FabricLoader.getInstance().isModLoaded("nexus-api-core")) {
            System.out.println("[NEFU] nexus-api-core detected — capabilities online.");
        }
        if (FabricLoader.getInstance().isModLoaded("nexus-maliopt")) {
            System.out.println("[NEFU] nexus-maliopt detected — TBDR integration ready.");
        }
        System.out.println("[NEFU] Module ready. Tier: " + TierManager.getCurrentTier());
    }
}
