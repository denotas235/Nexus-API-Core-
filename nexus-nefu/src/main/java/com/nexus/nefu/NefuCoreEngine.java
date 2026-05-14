package com.nexus.nefu;

/**
 * Central dispatcher for the NEFU render pipeline.
 *
 * Loads libnefu.so via JNI and routes draw calls to the active translator.
 * If the native library is unavailable the engine silently disables itself
 * and the game continues to work normally (Passthrough mode).
 */
public class NefuCoreEngine {

    // Renderer IDs (must match constants in nefu_core.cpp)
    public static final int RENDERER_LTW         = 0;
    public static final int RENDERER_MOBILEGLUES = 1;
    public static final int RENDERER_KRYPTON     = 2;
    public static final int RENDERER_ZINK        = 3;
    public static final int RENDERER_PASSTHROUGH = 4;

    private static final String[] RENDERER_NAMES =
            { "LTW", "MobileGlues", "Krypton", "Zink", "Passthrough" };

    private static boolean active          = false;
    private static int     currentRenderer = RENDERER_PASSTHROUGH;

    // ── Public API ────────────────────────────────────────────────────────────
    public static boolean isActive()           { return active; }
    public static String  getRendererName()    { return RENDERER_NAMES[bound(currentRenderer)]; }

    public static void init() {
        try {
            System.loadLibrary("nefu");
            active = nativeInit();
            if (!active) {
                System.err.println("[NEFU] nativeInit() returned false — Passthrough mode.");
                return;
            }
            int tier = NexusNefuClient.CONFIG.tierOverride >= 0
                     ? NexusNefuClient.CONFIG.tierOverride
                     : TierManager.detectTier();
            selectRenderer(selectRendererForTier(tier));
            System.out.println("[NEFU] TBDR Framebuffer Fetch : " + hasTbdrFramebufferFetch());
            System.out.println("[NEFU] TBDR Buffer Storage    : " + hasTbdrBufferStorage());
        } catch (UnsatisfiedLinkError e) {
            active = false;
            System.err.println("[NEFU] Native library not found — Passthrough mode.");
        }
    }

    public static void selectRenderer(int r) {
        if (!active) return;
        currentRenderer = r;
        nativeSelectRenderer(r);
        System.out.println("[NEFU] Renderer -> " + RENDERER_NAMES[bound(r)]);
    }

    // TBDR capability queries
    public static boolean hasTbdrFramebufferFetch() { return active && nativeHasFramebufferFetch(); }
    public static boolean hasTbdrBufferStorage()    { return active && nativeHasBufferStorage(); }

    // Called by BatchManager
    public static void nativeDrawArraysBatched(int mode, int[] firsts, int[] counts) {
        if (!active) return;
        nativeBatchedDraw(mode, firsts, counts, currentRenderer);
    }

    // ── Private ───────────────────────────────────────────────────────────────
    private static int selectRendererForTier(int tier) {
        if (tier <= 1) return RENDERER_LTW;
        if (tier == 2) return RENDERER_MOBILEGLUES;
        if (tier == 3) return RENDERER_KRYPTON;
        if (tier == 4) return NexusNefuClient.CONFIG.useZink
                              ? RENDERER_ZINK : RENDERER_MOBILEGLUES;
        return RENDERER_PASSTHROUGH;
    }

    private static int bound(int r) {
        return Math.max(0, Math.min(r, RENDERER_NAMES.length - 1));
    }

    // ── JNI ───────────────────────────────────────────────────────────────────
    private static native boolean nativeInit();
    private static native void    nativeSelectRenderer(int rendererId);
    private static native void    nativeBatchedDraw(int mode, int[] firsts,
                                                    int[] counts, int renderer);
    private static native boolean nativeHasFramebufferFetch();
    private static native boolean nativeHasBufferStorage();
}
