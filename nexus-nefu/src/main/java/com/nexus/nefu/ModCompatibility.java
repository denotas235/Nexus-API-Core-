package com.nexus.nefu;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Checks for mods that are compatible (with special handling) or incompatible
 * with NEFU. Must be called before NefuCoreEngine.init() so flags are set
 * before the native library is loaded.
 */
public class ModCompatibility {

    private static final String[][] TABLE = {
        // { modId, "compatible"|"incompatible", message }
        { "sodium",     "compatible",   "NEFU batching auto-disabled; draw-call optimisation delegated to Sodium." },
        { "iris",       "compatible",   "Shader management delegated to Iris; NEFU handles GL translation only." },
        { "indium",     "compatible",   "Indium (Sodium addon) — no conflict." },
        { "optifabric", "incompatible", "OptiFabric hooks the same GL pipeline as NEFU. Remove OptiFabric." },
        { "optifine",   "incompatible", "OptiFine is incompatible with Fabric. Remove OptiFine." },
        { "canvas",     "incompatible", "Canvas replaces the entire render pipeline — conflicts with NEFU." },
    };

    public static void checkAndWarn() {
        FabricLoader loader = FabricLoader.getInstance();
        for (String[] entry : TABLE) {
            String modId   = entry[0];
            String status  = entry[1];
            String message = entry[2];
            if (!loader.isModLoaded(modId)) continue;
            if ("incompatible".equals(status)) {
                System.err.println("[NEFU] *** INCOMPATIBILITY *** " + modId + " — " + message);
            } else {
                System.out.println("[NEFU] Compatible mod: " + modId + " — " + message);
                applyEffect(modId);
            }
        }
    }

    private static void applyEffect(String modId) {
        if ("sodium".equals(modId)) {
            NexusNefuClient.CONFIG.batchingEnabled = false;
            System.out.println("[NEFU] Batching disabled — delegated to Sodium.");
        }
    }
}
