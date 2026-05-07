package com.maliopt.performance;

import com.maliopt.MaliOptMod;

public class PerformanceGuard {
    private static final int TARGET_FPS = 30;
    private static boolean healthy = true;
    private static int frameCount = 0;
    private static long lastCheck = 0;

    public static void init() {
        MaliOptMod.LOGGER.info("[PerfGuard] Inicializado.");
    }

    public static void onFrameEnd() {
        long now = System.nanoTime();
        frameCount++;
        if (lastCheck == 0) lastCheck = now;
        long elapsedMs = (now - lastCheck) / 1_000_000;
        if (elapsedMs >= 1000) {
            double actualFps = frameCount / (elapsedMs / 1000.0);
            healthy = actualFps >= (TARGET_FPS * 0.75);
            frameCount = 0;
            lastCheck = now;
        }
    }

    public static boolean isFpsHealthy() { return healthy; }

    // Métodos exigidos pelo Bloom e Lighting Pipeline
    public static boolean bloomEnabled() { return healthy; } // Desativa bloom se o FPS cair
    public static float bloomThreshold() { return 0.8f; }
    public static float bloomRadius() { return 1.5f; }
    public static float bloomIntensity() { return 0.4f; }
    public static boolean lightingPassEnabled() { return true; }
    public static float warmth() { return 1.0f; }
    public static float ambientOcclusion() { return 0.5f; }
}
