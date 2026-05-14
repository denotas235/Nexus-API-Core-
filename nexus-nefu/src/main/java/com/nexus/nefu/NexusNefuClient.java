package com.nexus.nefu;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

/**
 * NEFU client entry-point.
 *
 * Initialisation is split into two phases:
 *
 *   Phase 1 — onInitializeClient()
 *     Runs at mod-load time. No GL context yet. Safe for:
 *     - ModCompatibility (reads FabricLoader only)
 *     - Registering lifecycle listeners
 *
 *   Phase 2 — ClientLifecycleEvents.CLIENT_STARTED
 *     Runs after the game window and GL context are fully ready. Safe for:
 *     - HardwareInfo (calls GL11.glGetString)
 *     - NefuCoreEngine.init() (loads native lib, calls glGetString for TBDR)
 *     - BatchManager configuration
 */
public class NexusNefuClient implements ClientModInitializer {

    public static NefuConfig CONFIG = new NefuConfig();

    @Override
    public void onInitializeClient() {
        String version = FabricLoader.getInstance()
                .getModContainer("nexus-nefu")
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("?");
        System.out.println("[NEFU] Pre-init — NexusFusion Render v" + version);

        // Phase 1 — no GL required
        ModCompatibility.checkAndWarn();

        // Phase 2 — deferred until GL context is ready
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            System.out.println("[NEFU] GL context ready — completing initialisation.");
            try {
                HardwareInfo.collectAndSave();
                NefuCoreEngine.init();
                BatchManager.setEnabled(CONFIG.batchingEnabled);
                System.out.println("[NEFU] Init complete. Renderer: "
                        + NefuCoreEngine.getRendererName()
                        + " | Tier: T" + TierManager.getTier()
                        + " | Batching: " + CONFIG.batchingEnabled);
            } catch (Exception e) {
                System.err.println("[NEFU] Init error (safe, game continues): " + e.getMessage());
            }
        });
    }
}
