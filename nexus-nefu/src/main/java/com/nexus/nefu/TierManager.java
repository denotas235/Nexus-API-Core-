package com.nexus.nefu;

import org.lwjgl.opengl.GL11;

public class TierManager {
    private static int cachedTier = -1;

    public static int detectTier() {
        if (cachedTier != -1) return cachedTier;
        String rawRenderer = GL11.glGetString(GL11.GL_RENDERER);
        if (rawRenderer == null) {
            System.err.println("[NEFU] TierManager: GL_RENDERER unavailable, defaulting to T5.");
            return cache(5);
        }
        String renderer = rawRenderer.toLowerCase();
        if (renderer.contains("mali-400") || renderer.contains("adreno 200")) return cache(0);
        if (renderer.contains("mali-t720") || renderer.contains("adreno 506")) return cache(1);
        if (renderer.contains("mali-g52"))                                      return cache(2);
        if (renderer.contains("mali-g76") || renderer.contains("adreno 650"))  return cache(3);
        if (renderer.contains("adreno 7") || renderer.contains("snapdragon 8 gen")) return cache(4);
        return cache(5);
    }

    private static int cache(int tier) { cachedTier = tier; return tier; }
    public static int getTier() { return cachedTier; }
}
