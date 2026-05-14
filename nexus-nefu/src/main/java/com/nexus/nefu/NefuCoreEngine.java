package com.nexus.nefu;

public class NefuCoreEngine {
    public static final int RENDERER_LTW = 0;
    public static final int RENDERER_MOBILEGLUES = 1;
    public static final int RENDERER_KRYPTON = 2;
    public static final int RENDERER_ZINK = 3;
    public static final int RENDERER_PASSTHROUGH = 4;

    private static boolean active = false;
    private static int currentRenderer = RENDERER_PASSTHROUGH;

    public static boolean isActive() { return active; }

    public static void init() {
        try {
            System.loadLibrary("nefu");
            active = nativeInit();
            if (active) {
                int tier = TierManager.detectTier();
                int renderer = selectRendererForTier(tier);
                selectRenderer(renderer);
            }
        } catch (UnsatisfiedLinkError e) {
            active = false;
            System.err.println("[NEFU] Native library failed to load. Disabling NEFU.");
        }
    }

    private static int selectRendererForTier(int tier) {
        if (tier <= 1) return RENDERER_LTW;
        if (tier == 2) return RENDERER_MOBILEGLUES;
        if (tier == 3) return RENDERER_KRYPTON;
        if (tier == 4) return NexusNefuClient.CONFIG.useZink ? RENDERER_ZINK : RENDERER_MOBILEGLUES;
        return RENDERER_PASSTHROUGH;
    }

    public static void selectRenderer(int renderer) {
        if (!active) return;
        currentRenderer = renderer;
        nativeSelectRenderer(renderer);
    }

    public static void nativeDrawArraysBatched(int mode, int[] firsts, int[] counts) {
        if (!active) return;
        nativeDrawArraysBatched(mode, firsts, counts, currentRenderer);
    }

    private static native boolean nativeInit();
    private static native void nativeSelectRenderer(int rendererId);
    private static native void nativeDrawArraysBatched(int mode, int[] firsts, int[] counts, int renderer);
}
