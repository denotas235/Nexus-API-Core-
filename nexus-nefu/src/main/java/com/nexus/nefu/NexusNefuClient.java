package com.nexus.nefu;

import net.fabricmc.api.ClientModInitializer;

public class NexusNefuClient implements ClientModInitializer {
    public static NefuConfig CONFIG = new NefuConfig();

    @Override
    public void onInitializeClient() {
        System.out.println("[NEFU] Initializing NexusFusion Render...");
        NefuCoreEngine.init();
        BatchManager.setEnabled(CONFIG.batchingEnabled);
    }
}
